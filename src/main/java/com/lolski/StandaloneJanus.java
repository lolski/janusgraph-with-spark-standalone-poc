package com.lolski;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StandaloneJanus {
    public static final String keyspace = "janusgraph";
    public static Pair<Graph, GraphComputer> newStandaloneJanusSparkComputer() {
        Map<String, Object> janusConfig = newStandaloneJanusConfigurations();
        createSimpleJanusGraph(janusConfig);

        Map<String, Object> hadoopConfig = newJanusHadoopConfiguration(janusConfig);
        HadoopGraph hadoopGraph = loadFromJanus(hadoopConfig);

        GraphComputer computer = newStandaloneJanusSparkComputer(hadoopGraph);
        return Pair.of(hadoopGraph, computer);
    }

    public static Map<String, Object> newStandaloneJanusConfigurations() {
        Map<String, Object> map = new HashMap<>();
//        map.put(AppConstants.STORAGE_HOSTNAME, "localhost");
//        map.put(AppConstants.STORAGE_BACKEND, "cassandra");
//        map.put(AppConstants.STORAGE_CASSANDRA_KEYSPACE, keyspace);
//        map.put(AppConstants.GREMLIN_GRAPH, AppConstants.GREMLIN_GRAPH_VALUE_HADOOP);
//        map.put(Constants.GREMLIN_HADOOP_INPUT_LOCATION, null);

        map.put("cassandra.input.keyspace", keyspace);
//        map.put("gremlin.hadoop.jarsInDistributedCache", true);
//        map.put("cassandra.input.predicate", "0c00020b0001000000000b000200000000020003000800047fffffff0000");
//        map.put("janusgraphmr.ioformat.cf-name", "edgestore");
//        map.put("janusgraphmr.ioformat.conf.storage.hostname", "localhost");
//        map.put("cassandra.thrift.framed.size_mb", 1024);
        map.put("janusgraphmr.ioformat.conf.storage.cassandra.keyspace", keyspace);
//        map.put("cassandra.input.partitioner.class", "org.apache.cassandra.dht.Murmur3Partitioner");
//        map.put("gremlin.hadoop.outputLocation", AppConstants.GREMLIN_HADOOP_OUTPUT_LOCATION_VALUE_STANDALONE_SPARK);
//        map.put("janusmr.ioformat.conf.storage.backend", "cassandra");
        map.put("janusmr.ioformat.cf-name", "edgestore");
        map.put("cassandra.input.columnfamily", "edgestore");
        map.put("janusmr.ioformat.conf.storage.cassandra.keyspace", keyspace);
//        map.put("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        map.put("gremlin.graph", "org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph");
        map.put("gremlin.hadoop.graphWriter", "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoOutputFormat");
        map.put("storage.hostname", "localhost");
        map.put("janusgraphmr.ioformat.conf.storage.backend", "cassandra");
        map.put("janusmr.ioformat.conf.storage.hostname", "localhost");
//        map.put("gremlin.hadoop.graphReader", "org.janusgraph.hadoop.formats.cassandra.CassandraInputFormat");
//        map.put("gremlin.hadoop.inputLocation", "none");
        map.put(AppConstants.STORAGE_BACKEND, "cassandra");

        return map;
    }

    public static Map<String, Object> newJanusHadoopConfiguration(Map<String, Object> configuration) {
        Map<String, Object> newConfiguration = new HashMap<>(configuration);
        newConfiguration.put(AppConstants.JANUSMR_IOFORMAT_CONF_STORAGE_CASSANDRA_KEYSPACE, keyspace);
        return newConfiguration;
    }

    public static GraphComputer newStandaloneJanusSparkComputer(Graph graph) {
        SparkGraphComputer computer = graph.compute(SparkGraphComputer.class);
        computer.configure(AppConstants.SPARK_MASTER, AppConstants.SPARK_MASTER_VALUE_STANDALONE);
        computer.configure(AppConstants.SPARK_SERIALIZER, AppConstants.SPARK_SERIALIZER_VALUE);

        computer.configure("cassandra.input.keyspace", keyspace);
//        computer.configure("gremlin.hadoop.jarsInDistributedCache", true);
//        computer.configure("cassandra.input.predicate", "0c00020b0001000000000b000200000000020003000800047fffffff0000");
//        computer.configure("janusgraphmr.ioformat.cf-name", "edgestore");
        computer.configure("janusgraphmr.ioformat.conf.storage.hostname", "localhost");
//        computer.configure("cassandra.thrift.framed.size_mb", 1024);
        computer.configure("janusgraphmr.ioformat.conf.storage.cassandra.keyspace", keyspace);
        computer.configure("cassandra.input.partitioner.class", "org.apache.cassandra.dht.Murmur3Partitioner");
        computer.configure("gremlin.hadoop.outputLocation", AppConstants.GREMLIN_HADOOP_OUTPUT_LOCATION_VALUE_STANDALONE_SPARK);
        computer.configure("janusmr.ioformat.conf.storage.backend", "cassandra");
        computer.configure("janusmr.ioformat.cf-name", "edgestore");
        computer.configure("cassandra.input.columnfamily", "edgestore");
        computer.configure("janusmr.ioformat.conf.storage.cassandra.keyspace", keyspace);
        computer.configure("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        computer.configure("gremlin.graph", "org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph");
        computer.configure("gremlin.hadoop.graphWriter", "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoOutputFormat");
        computer.configure("storage.hostname", "localhost");
        computer.configure("janusgraphmr.ioformat.conf.storage.backend", "cassandra");
        computer.configure("janusmr.ioformat.conf.storage.hostname", "localhost");
        computer.configure("gremlin.hadoop.graphReader", "org.janusgraph.hadoop.formats.cassandra.CassandraInputFormat");
        computer.configure("gremlin.hadoop.inputLocation", "none");
        computer.configure(AppConstants.STORAGE_BACKEND, "cassandra");

        return computer;
    }

    public static JanusGraph createGraphOfTheGodsGraph(Map<String, Object> configuration) {
        Configuration config = new MapConfiguration(configuration);
        JanusGraph graph = JanusGraphFactory.open(config);
        JanusGraphTransaction tx = graph.newTransaction();
//        GraphOfTheGodsFactory.loadWithoutMixedIndex(graph, false);
        tx.commit();
        return graph;
    }

    public static Graph createSimpleJanusGraph(Map<String, Object> configuration) {
        Configuration config = new MapConfiguration(configuration);
        JanusGraph graph = JanusGraphFactory.open(config);
        JanusGraphTransaction tx = graph.newTransaction();
        tx.addVertex(T.label, "wo");
        tx.commit();
        return graph;
    }
//
    public static HadoopGraph loadFromJanus(Map<String, Object> configuration) {
        Graph hadoopGraph = GraphFactory.open(configuration);
        return (HadoopGraph) hadoopGraph;
    }

    public static String generateUniqueKeyspaceName() {
        return ("wow-" + UUID.randomUUID()).replace("-", "").substring(0, 15);
    }

}
