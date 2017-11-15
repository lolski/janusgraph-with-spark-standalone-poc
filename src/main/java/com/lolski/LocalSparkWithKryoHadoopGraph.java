package com.lolski;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.hadoop.Constants;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

import java.util.HashMap;
import java.util.Map;

class LocalSparkWithKryoHadoopGraph {
    public static Pair<Graph, GraphComputer> newLocalKryoSparkComputer() {
        Map<String, Object> config = newLocalKryoConfigurations();
        Graph graph = GraphFactory.open(config);
        GraphComputer computer = newLocalSparkComputerMinimalConfiguration(graph);
        return Pair.of(graph, computer);
    }

    public static Map<String, Object> newLocalKryoConfigurations() {
        Map<String, Object> map = new HashMap<>();
        map.put(AppConstants.GREMLIN_GRAPH, "org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph");
        map.put(Constants.GREMLIN_HADOOP_INPUT_LOCATION, "./g-in/tinkerpop-modern.kryo");
        map.put(Constants.GREMLIN_HADOOP_GRAPH_READER, "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoInputFormat");
        map.put(Constants.GREMLIN_HADOOP_OUTPUT_LOCATION, "./g-out");
        map.put(Constants.GREMLIN_HADOOP_GRAPH_WRITER, "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoOutputFormat");

        return map;
    }

    public static GraphComputer newLocalSparkComputerMinimalConfiguration(Graph graph) {
        SparkGraphComputer computer = graph.compute(SparkGraphComputer.class);
        computer.configure(AppConstants.SPARK_MASTER, AppConstants.SPARK_MASTER_VALUE_LOCAL);
        return computer;
    }
}
