# Janus Distributed OLAP Proof of Concept
This project contains 3 things:
1. A bash script `setup-3-nodes-cluster-for-janus-olap.sh`
2. a Java project `janus-distributed-olap`
3. A Grakn Docker image on `docker-grakn-oracle-java-8`

The main entry point is the script `setup-3-nodes-cluster-for-janus-olap.sh`. When started, this script will configure a cassandra, hadoop, and spark cluster automatically across three docker instances. Afterwards it will start the Janus Distributed OLAP Java program on the first node (i.e. `janus-olap-node1`, which then does a distributed OLAP operation on the cluster.

### Expected Result
An execution takes around 3-5 minutes, and once finished it will print some result, e.g., `result =  {4096=4}` which indicates a success. An empty result `result =  {}` or a halted execution indicates a failure

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

### Build Grakn with Cluster Capabilities
```
cd /path/to/grakn/repository
mvn package -T 2.5C -DskipTests=true
```

### Build Janus-Distributed-OLAP Java Program
```
cd janus-distributed-olap
mvn package -T 2.5C -DskipTests=true
```

### Download the depencencies
1. Hadoop 2.6.5
2. Spark 1.6.3

Yes. The you have to get the version exactly right as otherwise it won't be compatible with the JanusGraph 0.2.0 which are used by the Janus-Distributed-OLAP Java program.

## Script Configuration
Configure the path to Grakn, Hadoop, and Spark which we have built and download at the configuration section of `setup-3-nodes-cluster-for-janus-olap.sh`
```
# ====================================================================================
# configurations
# ====================================================================================

### input ###
grakn_tar_fullpath=/path/to/grakn/repository/grakn-dist/target/grakn-dist-1.0.0-SNAPSHOT.tar.gz
spark_tar_fullpath=/Users/lolski/Downloads/spark-1.6.3-bin-hadoop2.6.tgz
hadoop_dir=/Users/lolski/Downloads/hadoop-2.6.5

```
