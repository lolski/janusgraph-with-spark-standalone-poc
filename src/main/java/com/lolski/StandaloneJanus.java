package com.lolski;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.example.GraphOfTheGodsFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StandaloneJanus {
    public static final String keyspace = generateUniqueKeyspaceName();
    public static Pair<Graph, GraphComputer> newStandaloneJanusSparkComputer() {
        Map<String, Object> janusConfig = newStandaloneJanusConfigurations();
        loadGraphOfTheGodsGraph(janusConfig);

        Map<String, Object> hadoopConfig = newJanusHadoopConfiguration(janusConfig);
        HadoopGraph hadoopGraph = loadFromJanus(hadoopConfig);

        GraphComputer computer = newStandaloneJanusSparkComputer(hadoopGraph);
        return Pair.of(hadoopGraph, computer);
    }

    public static Map<String, Object> newStandaloneJanusConfigurations() {
        Map<String, Object> map = new HashMap<>();
        map.put(AppConstants.STORAGE_HOSTNAME, "localhost");
        map.put(AppConstants.STORAGE_BACKEND, "cassandra");
        map.put(AppConstants.STORAGE_CASSANDRA_KEYSPACE, keyspace);
        map.put(AppConstants.GREMLIN_GRAPH, AppConstants.GREMLIN_GRAPH_VALUE_HADOOP);

        return map;
    }

    public static Map<String, Object> newJanusHadoopConfiguration(Map<String, Object> configuration) {
        Map<String, Object> newConfiguration = new HashMap<String, Object>(configuration);
        newConfiguration.put(AppConstants.JANUSMR_IOFORMAT_CONF_STORAGE_CASSANDRA_KEYSPACE, keyspace);
        return newConfiguration;
    }

    public static GraphComputer newStandaloneJanusSparkComputer(Graph graph) {
        SparkGraphComputer computer = graph.compute(SparkGraphComputer.class);
        computer.configure(AppConstants.SPARK_MASTER, AppConstants.SPARK_MASTER_VALUE_STANDALONE);
        computer.configure(AppConstants.SPARK_SERIALIZER, AppConstants.SPARK_SERIALIZER_VALUE);

        return computer;
    }

    public static JanusGraph loadGraphOfTheGodsGraph(Map<String, Object> configuration) {
        Configuration config = new MapConfiguration(configuration);
        JanusGraph graph = JanusGraphFactory.open(config);
        GraphOfTheGodsFactory.loadWithoutMixedIndex(graph, false);
        graph.newTransaction().commit();
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
