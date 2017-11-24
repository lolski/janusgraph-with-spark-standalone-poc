# Janus Distributed OLAP Proof of Concept
When started, this script will spawn three docker instances, setup a hadoop and spark cluster, and start a program "janus-distributed-olap" which then does a distributed OLAP operation on the cluster.

This is how the docker instances will be configured. The port 8080 is open so you can go to localhost:8080 to view the Spark dashboard. Addiionally, the port 5005 is open on the master host for remote debugging purpose.
```
~/grakn.ai/janus-dsistributed-olap-poc(master*) » docker ps
CONTAINER ID        IMAGE                 COMMAND             CREATED             STATUS              PORTS                                            NAMES
c0c9e4fef1bc        grakn/oracle-java-8   "/sbin/my_init"     4 minutes ago       Up 4 minutes        0.0.0.0:8082->8080/tcp                           janus-olap-node3
96e3d3be6b0c        grakn/oracle-java-8   "/sbin/my_init"     4 minutes ago       Up 4 minutes        0.0.0.0:8081->8080/tcp                           janus-olap-node2
a3bc57780c5a        grakn/oracle-java-8   "/sbin/my_init"     4 minutes ago       Up 4 minutes        0.0.0.0:5005->5005/tcp, 0.0.0.0:8080->8080/tcp   janus-olap-node1
```
