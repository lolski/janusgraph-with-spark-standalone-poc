1) fix etc host
echo "172.17.0.2      cassandra-node1
172.17.0.3      cassandra-node2
172.17.0.4      cassandra-node3" >> /etc/hosts

2) apt install python && docker cp cassandra

--- hadoop single node cluster ---
http://www.michael-noll.com/tutorials/running-hadoop-on-ubuntu-linux-single-node-cluster/

- vim etc/hadoop/hadoop-env.sh set export JAVA_HOME=/usr/lib/jvm/java-8-oracle
- configure ssh
  - ssh-keygen -t rsa -P ""
  - cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys
  - dpkg-reconfigure openssh-server
  - service ssh start
- mkdir /hadoop-tmp
- vim etc/hadoop/core-site.xml
    <property>
      <name>hadoop.tmp.dir</name>
      <value>/hadoop-tmp</value>
    </property>
    <property>
      <name>fs.default.name</name>
      <value>hdfs://localhost:54310</value>
    </property>
- vim etc/hadoop/mapred-site.xml
    <property>
      <name>mapred.job.tracker</name>
      <value>localhost:54311</value>
    </property>
- vim etc/hadoop/hdfs-site.xml
    <property>
      <name>dfs.replication</name>
      <value>1</value>
    </property>
- ./bin/hadoop namenode -format (the output should have a line 'util.ExitUtil: Exiting with status 0')
- ./sbin/start-all.sh

--- hadoop single node cluster ---
http://www.michael-noll.com/tutorials/running-hadoop-on-ubuntu-linux-multi-node-cluster/

- make sure every node has their hadoop shut down (./sbin/stop-all.sh)
- do hadoop single node cluster on a couple of nodes which you want to make a cluster from
- on each machine, do step #1 above (i.e., fix etc host)
- make sure you can ssh from master to every slaves, and vice-versa. this can be done by adding the public keys into authorized_keys file of each machine
- vim etc/hadoop/slaves, add all slaves (e.g. cassandra-node1, cassandra-node2, cassandra-node3). each item should be on its own line
- vim etc/hadoop/core-site.xml, replace localhost with master-node (e.g cassandra-node1)
- vim etc/hadoop/mapred-site.xml, , replace localhost with master-node (e.g cassandra-node1)
- from master node ./sbin/start-dfs.sh
