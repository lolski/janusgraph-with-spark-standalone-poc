package com.lolski;

class AppConstants {
    public static final String GREMLIN_GRAPH = "gremlin.graph";
    public static final String GREMLIN_GRAPH_VALUE_HADOOP = "org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph";

    public static final String SPARK_MASTER = "spark.master";
    public static final String SPARK_MASTER_VALUE_LOCAL = "local[1]";
    public static final String SPARK_MASTER_VALUE_STANDALONE = "spark://127.0.0.1:5678";

    public static final String SPARK_SERIALIZER = "spark.serializer";
    public static final String SPARK_SERIALIZER_VALUE = "org.apache.spark.serializer.KryoSerializer";

    public static final String SPARK_EXECUTOR_MEMORY = "spark.executor.memory";

    public static final String STORAGE_CASSANDRA_KEYSPACE = "storage.cassandra.keyspace";
    public static final String JANUSMR_IOFORMAT_CONF_STORAGE_CASSANDRA_KEYSPACE = "janusgraphmr.ioformat.conf.storage.cassandra.keyspace";

    public static final String GREMLIN_HADOOP_INPUT_LOCATION_VALUE_LOCAL_SPARK = "./g-in/tinkerpop-modern.kryo";
    public static final String GREMLIN_HADOOP_INPUT_LOCATION_VALUE_STANDALONE_SPARK = "/Users/lolski/Playground/janusgraph/g-in/tinkerpop-modern.kryo";
    public static final String GREMLIN_HADOOP_GRAPH_READER_VALUE = "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoInputFormat";

    public static final String GREMLIN_HADOOP_OUTPUT_LOCATION_VALUE_LOCAL_SPARK = "./g-out";
    public static final String GREMLIN_HADOOP_OUTPUT_LOCATION_VALUE_STANDALONE_SPARK = "/Users/lolski/Playground/janusgraph/g-out";
    public static final String GREMLIN_HADOOP_GRAPH_WRITER_VALUE = "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoOutputFormat";


}
