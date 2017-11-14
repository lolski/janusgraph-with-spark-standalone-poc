package com.lolski;

class AppConstants {
    public static final String GREMLIN_GRAPH = "gremlin.graph";

    public static final String SPARK_MASTER = "spark.master";
    public static final String SPARK_MASTER_VALUE_LOCAL = "local[1]";

    public static final String SPARK_SERIALIZER = "spark.serializer";
    public static final String SPARK_SERIALIZER_VALUE = "org.apache.spark.serializer.KryoSerializer";

    public static final String SPARK_EXECUTOR_MEMORY = "spark.executor.memory";

    public static final String STORAGE_CASSANDRA_KEYSPACE = "storage.cassandra.keyspace";
    public static final String JANUSMR_IOFORMAT_CONF_STORAGE_CASSANDRA_KEYSPACE = "janusgraphmr.ioformat.conf.storage.cassandra.keyspace";
}
