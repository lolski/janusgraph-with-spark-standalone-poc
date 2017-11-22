package com.lolski;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.hadoop.Constants;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

import java.util.HashMap;
import java.util.Map;

public class StandaloneSparkWithKryoHadoopGraph {
    public static Pair<Graph, GraphComputer> newStandaloneSparkWithKryoHadoopGraph() {
        Map<String, Object> config = newStandaloneKryoConfigurations();
        Graph graph = GraphFactory.open(config);
        GraphComputer computer = newStandaloneSparkComputer(graph);
        return Pair.of(graph, computer);
    }

    public static Map<String, Object> newStandaloneKryoConfigurations() {
        Map<String, Object> map = new HashMap<>();
        map.put(AppConstants.GREMLIN_GRAPH, AppConstants.GREMLIN_GRAPH_VALUE_HADOOP);
        map.put(Constants.GREMLIN_HADOOP_INPUT_LOCATION, AppConstants.GREMLIN_HADOOP_INPUT_LOCATION_VALUE_STANDALONE_SPARK);
        map.put(Constants.GREMLIN_HADOOP_GRAPH_READER, AppConstants.GREMLIN_HADOOP_GRAPH_READER_VALUE);
        map.put(Constants.GREMLIN_HADOOP_OUTPUT_LOCATION, AppConstants.GREMLIN_HADOOP_OUTPUT_LOCATION_VALUE_STANDALONE_SPARK);
        map.put(Constants.GREMLIN_HADOOP_GRAPH_WRITER, AppConstants.GREMLIN_HADOOP_GRAPH_WRITER_VALUE);

        return map;
    }

    public static GraphComputer newStandaloneSparkComputer(Graph graph) {
        SparkGraphComputer computer = graph.compute(SparkGraphComputer.class);
        computer.configure(AppConstants.SPARK_MASTER, AppConstants.SPARK_MASTER_VALUE_STANDALONE);
        computer.configure(AppConstants.SPARK_SERIALIZER, AppConstants.SPARK_SERIALIZER_VALUE);

        computer.configure(Constants.GREMLIN_HADOOP_INPUT_LOCATION, AppConstants.GREMLIN_HADOOP_INPUT_LOCATION_VALUE_STANDALONE_SPARK);
        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_READER, AppConstants.GREMLIN_HADOOP_GRAPH_READER_VALUE);
        computer.configure(Constants.GREMLIN_HADOOP_OUTPUT_LOCATION, AppConstants.GREMLIN_HADOOP_OUTPUT_LOCATION_VALUE_STANDALONE_SPARK);
        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_WRITER, AppConstants.GREMLIN_HADOOP_GRAPH_WRITER_VALUE);

        return computer;
    }
}
