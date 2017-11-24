#!/bin/bash

# ====================================================================================
# configurations
# ====================================================================================

### input ###
grakn_tar_fullpath=/Users/lolski/grakn.ai/grakn/grakn-dist/target/grakn-dist-1.0.0-SNAPSHOT.tar.gz
spark_tar_fullpath=/Users/lolski/Downloads/spark-1.6.3-bin-hadoop2.6.tgz
hadoop_dir=/Users/lolski/Downloads/hadoop-2.6.5
hadoop_preconfigured_conf_dir=./hadoop-conf
janus_poc_jar=/Users/lolski/Playground/janusgraph/janus-distributed-olap/target/janus-distributed-olap-1.0-SNAPSHOT.jar
janus_poc_lib=/Users/lolski/Playground/janusgraph/janus-distributed-olap/target/lib

### output ###
hadoop_conf_tmp_dir_destination=/tmp/hadoop-conf
hadoop_dir_destination=/hadoop
janus_poc_lib_destination=/janusgraph-lib

hadoop_tmp_dir=/hadoop-tmp
home_dir_in_docker=/root

### name of docker instances ###
node1="janus-olap-node1"
node2="janus-olap-node2"
node3="janus-olap-node3"

# ====================================================================================
# Common helpers
# ====================================================================================
docker_inspect_get_ip() {
  docker inspect --format '{{ .NetworkSettings.IPAddress }}' $1
}

docker_run() {
  local NODE=$1
  local image=$2
  local additional_arguments=$3
  echo docker run --rm --detach --name $NODE -h $NODE $additional_arguments grakn/oracle-java-8
  docker run --rm --detach --name $NODE -h $NODE $additional_arguments grakn/oracle-java-8
  return $?
}

#
# replace an occurrence of a string in a file within a container using sed
#
str_replace_with_sed() {
  local NODE=$1
  local old_string=$2
  local new_string=$3
  local file=$4

  echo docker exec $NODE /bin/bash -c "sed -i.bak s/'$old_string'/'$new_string'/g '$file'"
  docker exec $NODE /bin/bash -c "sed -i.bak s/'$old_string'/'$new_string'/g '$file'"
}

# ====================================================================================
# SSH bullshit needed to get hadoop working
# ====================================================================================
ssh_generate_rsa_keypair_and_restart() {
  local NODE=$1

  echo docker exec $NODE /bin/bash -c "ssh-keygen -f $home_dir_in_docker/.ssh/id_rsa -t rsa -N ''"
  docker exec $NODE /bin/bash -c "ssh-keygen -f $home_dir_in_docker/.ssh/id_rsa -t rsa -N ''"
  echo docker exec $NODE /bin/bash -c "dpkg-reconfigure openssh-server"
  docker exec $NODE /bin/bash -c "dpkg-reconfigure openssh-server"
  echo docker exec $NODE /bin/bash -c "service ssh start"
  docker exec $NODE /bin/bash -c "service ssh start"
}

ssh_add_node_a_pubkey_to_node_b() {
  local node_a=$1
  local node_b=$2

  local node_a_pubkey=`docker exec $node_a /bin/bash -c "cat $home_dir_in_docker/.ssh/id_rsa.pub"`

  echo docker exec $node_b /bin/bash -c "echo '$node_a_pubkey' >> $home_dir_in_docker/.ssh/authorized_keys"
  docker exec $node_b /bin/bash -c "echo '$node_a_pubkey' >> $home_dir_in_docker/.ssh/authorized_keys"
}

ssh_add_to_known_host() {
  local NODE=$1
  local hostname=$2

  echo docker exec $NODE /bin/bash -c "ssh-keyscan $hostname >> $home_dir_in_docker/.ssh/known_hosts"
  docker exec $NODE /bin/bash -c "ssh-keyscan $hostname >> $home_dir_in_docker/.ssh/known_hosts"
}

# ====================================================================================
# Cluster helpers
# ====================================================================================
storage_cluster_join() {
  local NODE=$1
  local cluster=$2
  local local_ip=$3

  echo docker exec $NODE /bin/bash -c "cd grakn-dist-1.0.0-SNAPSHOT && ./grakn cluster configure $cluster $local_ip"
  docker exec $NODE /bin/bash -c "cd grakn-dist-1.0.0-SNAPSHOT && ./grakn cluster configure $cluster $local_ip"

  echo docker exec $NODE /bin/bash -c "cd grakn-dist-1.0.0-SNAPSHOT && ./grakn server start storage"
  docker exec $NODE /bin/bash -c "cd grakn-dist-1.0.0-SNAPSHOT && ./grakn server start storage"
}

spark_cluster_join() {
  local NODE=$1
  local master_node_ip=$2
  local spark_node_type=$3

  local start_spark_cmd=
  if [ "$spark_node_type" == 'spark-master-node' ]; then
    start_spark_cmd="./sbin/start-master.sh -h $master_node_ip -p 5678"
  else
    start_spark_cmd="./sbin/start-slave.sh spark://$master_node_ip:5678"
  fi

  echo docker exec $NODE /bin/bash -c "cd spark-1.6.3-bin-hadoop2.6 && $start_spark_cmd"
  docker exec $NODE /bin/bash -c "cd spark-1.6.3-bin-hadoop2.6 && $start_spark_cmd"
}

hadoop_cluster_setup_configuration() {
  local NODE=$1
  local master_node_hostname=$2

  local hadoop_conf_dir="$hadoop_preconfigured_conf_dir/master"
  echo docker cp $hadoop_conf_dir $NODE:$hadoop_conf_tmp_dir_destination
  docker cp $hadoop_conf_dir $NODE:$hadoop_conf_tmp_dir_destination

  echo docker exec $NODE /bin/bash -c "mv $hadoop_conf_tmp_dir_destination/* $hadoop_dir_destination/etc/hadoop/"
  docker exec $NODE /bin/bash -c "mv $hadoop_conf_tmp_dir_destination/* $hadoop_dir_destination/etc/hadoop/"

  # change config
  str_replace_with_sed $NODE "localhost" "$master_node_hostname" "/hadoop/etc/hadoop/core-site.xml"
  str_replace_with_sed $NODE "localhost" "$master_node_hostname" "/hadoop/etc/hadoop/mapred-site.xml"
  str_replace_with_sed $NODE "localhost" "$master_node_hostname" "/hadoop/etc/hadoop/hdfs-site.xml"
}

hadoop_cluster_setup_register_slave_nodes() {
  local NODE=$1
  local slave_node_hostnames=${@:2}

  echo "list of slaves $slave_node_hostnames"
  docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && echo -n > ./etc/hadoop/slaves"
  for slave_node_hostname in $slave_node_hostnames; do
    echo docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && echo $slave_node_hostname >> ./etc/hadoop/slaves"
    docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && echo $slave_node_hostname >> ./etc/hadoop/slaves"
  done
}

hadoop_cluster_setup() {
  local NODE=$1
  local master_node_hostname=$2
  local spark_node_type=$3
  local slave_node_hostnames=${@:4}

  echo docker exec $NODE /bin/bash -c "mkdir $hadoop_tmp_dir"
  docker exec $NODE /bin/bash -c "mkdir $hadoop_tmp_dir"

  hadoop_cluster_setup_configuration $NODE $master_node_hostname

  echo docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && ./bin/hadoop namenode -format"
  docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && ./bin/hadoop namenode -format"

  if [ "$spark_node_type" == 'spark-master-node' ]; then
    hadoop_cluster_setup_register_slave_nodes $NODE $slave_node_hostnames

    # start dfs
    echo docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && ./sbin/start-dfs.sh"
    docker exec $NODE /bin/bash -c "cd $hadoop_dir_destination && ./sbin/start-dfs.sh"
  fi
}

# ====================================================================================
# Install distribution helpers
# ====================================================================================
copy_distribution_into_docker_container() {
  local NODE=$1
  local grakn_tar_fullpath=$2
  local spark_tar_fullpath=$3

  local grakn_dir="/grakn-dist-1.0.0-SNAPSHOT"

  echo docker cp $grakn_tar_fullpath $NODE:/
  docker cp $grakn_tar_fullpath $NODE:/
  echo docker exec $NODE tar -xf /`basename $grakn_tar_fullpath`
  docker exec $NODE tar -xf /`basename $grakn_tar_fullpath`

  echo docker cp $spark_tar_fullpath $NODE:/
  docker cp $spark_tar_fullpath $NODE:/
  echo docker exec $NODE tar -xf /`basename $spark_tar_fullpath`
  docker exec $NODE tar -xf /`basename $spark_tar_fullpath`

  echo docker cp $janus_poc_jar $NODE:/
  docker cp $janus_poc_jar $NODE:/

  echo docker cp $janus_poc_lib $NODE:$janus_poc_lib_destination
  docker cp $janus_poc_lib $NODE:$janus_poc_lib_destination

  echo docker cp $hadoop_dir $NODE:$hadoop_dir_destination
  docker cp $hadoop_dir $NODE:$hadoop_dir_destination
}

spawn_container_setup_ssh_and_install_distribution() {
  local NODE="$1"
  local grakn_tar_fullpath="$2"
  local spark_tar_fullpath="$3"
  local additional_arguments="$4"
  echo "spawning '$NODE'..."
  docker_run "$NODE" "grakn/oracle-java-8" "$additional_arguments"

  echo "copy distribution into '$NODE'"
  copy_distribution_into_docker_container "$NODE" "$grakn_tar_fullpath" "$spark_tar_fullpath"
}

# ====================================================================================
# Main routine: distributed olap orchestration methods
# ====================================================================================
execute_init_nodes() {
  echo "--------------------------- olap - init ---------------------------"
  spawn_container_setup_ssh_and_install_distribution $node1 $grakn_tar_fullpath $spark_tar_fullpath "-p8080:8080 -p5005:5005"
  spawn_container_setup_ssh_and_install_distribution $node2 $grakn_tar_fullpath $spark_tar_fullpath '-p8081:8080'
  spawn_container_setup_ssh_and_install_distribution $node3 $grakn_tar_fullpath $spark_tar_fullpath '-p8082:8080'

  local node1_ip=`docker_inspect_get_ip $node1`
  local node1_hostname=`docker exec $node1 /bin/hostname`
  local node2_ip=`docker_inspect_get_ip $node2`
  local node2_hostname=`docker exec $node2 /bin/hostname`
  local node3_ip=`docker_inspect_get_ip $node3`
  local node3_hostname=`docker exec $node3 /bin/hostname`

  docker exec $node1 /bin/bash -c "echo $node1_ip $node1_hostname >> /etc/hosts"
  docker exec $node1 /bin/bash -c "echo $node2_ip $node2_hostname >> /etc/hosts"
  docker exec $node1 /bin/bash -c "echo $node3_ip $node3_hostname >> /etc/hosts"
  docker exec $node2 /bin/bash -c "echo $node1_ip $node1_hostname >> /etc/hosts"
  docker exec $node2 /bin/bash -c "echo $node2_ip $node2_hostname >> /etc/hosts"
  docker exec $node2 /bin/bash -c "echo $node3_ip $node3_hostname >> /etc/hosts"
  docker exec $node3 /bin/bash -c "echo $node1_ip $node1_hostname >> /etc/hosts"
  docker exec $node3 /bin/bash -c "echo $node2_ip $node2_hostname >> /etc/hosts"
  docker exec $node3 /bin/bash -c "echo $node3_ip $node3_hostname >> /etc/hosts"

  ssh_generate_rsa_keypair_and_restart $node1
  ssh_generate_rsa_keypair_and_restart $node2
  ssh_generate_rsa_keypair_and_restart $node3

  ssh_add_node_a_pubkey_to_node_b $node1 $node2
  ssh_add_to_known_host $node2 $node1_hostname
  ssh_add_node_a_pubkey_to_node_b $node1 $node3
  ssh_add_to_known_host $node3 $node1_hostname
  ssh_add_node_a_pubkey_to_node_b $node1 $node1
  ssh_add_to_known_host $node1 $node1_hostname
  ssh_add_node_a_pubkey_to_node_b $node2 $node1
  ssh_add_to_known_host $node1 $node2_hostname
  ssh_add_node_a_pubkey_to_node_b $node3 $node1
  ssh_add_to_known_host $node1 $node3_hostname

  echo "--------------------------- end init ---------------------------"
  echo ""
  echo ""
}

execute_cluster_join() {
  echo "--------------------------- olap - initiate cluster join ---------------------------"
  local master_node_ip=`docker_inspect_get_ip $node1`
  local master_node_hostname=`docker exec $node1 /bin/hostname`
  local node2_ip=`docker_inspect_get_ip $node2`
  local node2_hostname=`docker exec $node2 /bin/hostname`
  local node3_ip=`docker_inspect_get_ip $node3`
  local node3_hostname=`docker exec $node3 /bin/hostname`

  storage_cluster_join $node1 $master_node_ip $master_node_ip
  storage_cluster_join $node2 $master_node_ip $node2_ip
  storage_cluster_join $node3 $master_node_ip $node3_ip

  spark_cluster_join $node1 $master_node_ip 'spark-master-node'
  spark_cluster_join $node2 $master_node_ip 'spark-slave-node'
  spark_cluster_join $node3 $master_node_ip 'spark-slave-node'

  hadoop_cluster_setup $node2 $master_node_hostname 'spark-slave-node'
  hadoop_cluster_setup $node3 $master_node_hostname 'spark-slave-node'
  hadoop_cluster_setup $node1 $master_node_hostname 'spark-master-node' $node2_hostname $node3_hostname

  echo "--------------------------- end initiate cluster join ---------------------------"
  echo ""
  echo ""
}

execute_distributed_olap() {
  local return_value=

  echo "--------------------------- olap - execute_distributed_olap ---------------------------"
  local master_node_ip=`docker_inspect_get_ip $node1`
  local hadoop_gremlin_libs="/grakn-dist-1.0.0-SNAPSHOT/services/lib/"

  echo docker exec $node1 /bin/bash -c "java -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp '/janus-distributed-olap-1.0-SNAPSHOT.jar:$janus_poc_lib_destination/*:' -Dspark_output_location_root=/out -Dhadoop_gremlin_libs=$hadoop_gremlin_libs -Dspark.master=spark://$master_node_ip:5678 -Dstorage.hostname=$master_node_ip -Dfs.defaultFS=hdfs://janus-olap-node1:54310 -Dpre_initialize_graph com.lolski.Main"
  docker exec $node1 /bin/bash -c "java -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp '/janus-distributed-olap-1.0-SNAPSHOT.jar:$janus_poc_lib_destination/*:' -Dspark_output_location_root=/out -Dhadoop_gremlin_libs=$hadoop_gremlin_libs -Dspark.master=spark://$master_node_ip:5678 -Dstorage.hostname=$master_node_ip -Dfs.defaultFS=hdfs://janus-olap-node1:54310 -Dpre_initialize_graph com.lolski.Main"
  echo "--------------------------- end execute_distributed_olap ---------------------------"
  echo ""
  echo ""

  return $return_value
}

# ====================================================================================
# Main routine
# ====================================================================================
set -e

execute_init_nodes
execute_cluster_join
execute_distributed_olap
