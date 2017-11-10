package com.lolski;

import org.apache.tinkerpop.gremlin.hadoop.Constants;
import org.apache.tinkerpop.gremlin.process.computer.*;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.ClusterCountMapReduce;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.ClusterPopulationMapReduce;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.traversal.TraversalVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.example.GraphOfTheGodsFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main( String[] args ) throws InterruptedException, ExecutionException {
        getClasspath();
        Graph graph = loadGraphOfTheGodsGraph();
        GraphComputer computer = getConfiguredSparkGraphComputer(graph);
        p("--- // WOTT --- // WOTT--- // WOTT--- // WOTT--- // WOTT--- // WOTT--- // WOTT");
        computer.program(PeerPressureVertexProgram.build().create(graph)).mapReduce(ClusterPopulationMapReduce.build().create());
        Future<ComputerResult> work = computer.submit();

        p(" result =  " + work.get().memory().get("clusterPopulation"));
        p("a");
    }

    public static Graph loadGraphOfTheGodsGraph() {
        Graph graph = GraphFactory.open("config.properties");
        return graph;
    }

    public static void p(String print) {
        System.out.println(print);
    }

    public static void getClasspath() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            System.out.println(url.getFile());
        }
    }

    public static GraphComputer getConfiguredSparkGraphComputer(Graph graph) {
        SparkGraphComputer computer = graph.compute(SparkGraphComputer.class);
        computer.configure("spark.master", "local[1]");
//        computer.configure("spark.executor.memory", "1g");
//        computer.configure("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        computer.configure(Constants.GREMLIN_HADOOP_INPUT_LOCATION, "./g-in");
        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_READER, "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoInputFormat");
        computer.configure(Constants.GREMLIN_HADOOP_OUTPUT_LOCATION, "./g-out");
        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_WRITER, "org.apache.tinkerpop.gremlin.hadoop.structure.io.gryo.GryoOutputFormat");
//        computer.configure(Constants.GREMLIN_HADOOP_JARS_IN_DISTRIBUTED_CACHE, true);
//        computer.configure(Constants.GREMLIN_HADOOP_DEFAULT_GRAPH_COMPUTER, "org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer");
//        computer.configure(Constants.MAPREDUCE_INPUT_FILEINPUTFORMAT_INPUTDIR, "./g-in");
//        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_INPUT_FORMAT, "./g-in");

//        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_INPUT_FORMAT, "");
//        computer.configure(Constants.GREMLIN_HADOOP_GRAPH_OUTPUT_FORMAT, "");
//        computer.configure(Constants.GREMLIN_HADOOP_JARS_IN_DISTRIBUTED_CACHE, "");
//        computer.configure(Constants.GREMLIN_HADOOP_INPUT_LOCATION, "tinkerpop-modern.kryo");
//        computer.configure(Constants.GREMLIN_HADOOP_OUTPUT_LOCATION, "./g-out");
        return computer;
    }
}