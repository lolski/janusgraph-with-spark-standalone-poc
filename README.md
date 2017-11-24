# Janus Distributed OLAP Proof of Concept
The main entry point is the script `setup-3-nodes-cluster-for-janus-olap.sh`. When started, this script will configure a cluster - it will spawn three docker instances and setup a cassandra, hadoop and spark cluster automatically. Once done, it start a program `janus-distributed-olap` on the first node (i.e. `janus-olap-node1`, which then does a distributed OLAP operation on the cluster.

## Cluster Configuration
Spark and Hadoop are configured in a master-slave fashion. The instance `janus-olap-node1` hosts Spark and Hadoop master while `janus-olap-node2` and `janus-olap-node3` host the slaves. The slave instances are where the OLAP operation will take place. Cassandra is clustered across the three instances in a multi-master setup.

The port 8080 is open so you can go to localhost:8080 to view the Spark dashboard. Additionally, the port 5005 is open on the master host for remote debugging purpose.
```
~/grakn.ai/janus-dsistributed-olap-poc(master*) » docker ps
CONTAINER ID        IMAGE                 COMMAND             CREATED             STATUS              PORTS                                            NAMES
c0c9e4fef1bc        grakn/oracle-java-8   "/sbin/my_init"     4 minutes ago       Up 4 minutes        0.0.0.0:8082->8080/tcp                           janus-olap-node3
96e3d3be6b0c        grakn/oracle-java-8   "/sbin/my_init"     4 minutes ago       Up 4 minutes        0.0.0.0:8081->8080/tcp                           janus-olap-node2
a3bc57780c5a        grakn/oracle-java-8   "/sbin/my_init"     4 minutes ago       Up 4 minutes        0.0.0.0:5005->5005/tcp, 0.0.0.0:8080->8080/tcp   janus-olap-node1
```

## Prerequisites
### Install Docker
Get the Docker Community Edition for your operating system at http://docker.com

### Build The Docker Image
Build the image `grakn/oracle-java-8:latest` which is required for this project
```
cd docker-grakn-oracle-java-8
docker build -t grakn/oracle-java-8:latest .
```

### Build Janus-Distributed-OLAP Java Program
```
cd janus-distributed-olap
mvn package -T 2.5C -DskipTests=true
```

### Download the depencencies
1. Hadoop 2.6.5
2. Spark 1.6.3

## Configuration
The path pointing to a Grakn distribution, Hadoop
```
# ====================================================================================
# configurations
# ====================================================================================

### input ###
grakn_tar_fullpath=/Users/lolski/grakn.ai/grakn/grakn-dist/target/grakn-dist-1.0.0-SNAPSHOT.tar.gz
spark_tar_fullpath=/Users/lolski/Downloads/spark-1.6.3-bin-hadoop2.6.tgz
hadoop_dir=/Users/lolski/Downloads/hadoop-2.6.5

```
