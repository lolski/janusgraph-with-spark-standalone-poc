package com.lolski;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.ClusterPopulationMapReduce;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Hello world with JanusGraph & Spark Standalone!
 * This PoC works with Spark in both in memory mode (e.g. local[1]) and standalone mode (e.g. spark://127.0.0.1:5678)
 * Needs cassandra to run
 *
 */
public class Main {
    public static final String hadoopGremlinLibs = "/Users/lolski/grakn.ai/grakn/grakn-dist/target/grakn-dist-1.0.0-SNAPSHOT/services/lib/";

    public static void main( String[] args ) throws InterruptedException, ExecutionException {
//        Pair<Graph, GraphComputer> graphAndGraphComputer = localSparkWithKryoHadoopGraph();
//        Pair<Graph, GraphComputer> graphAndGraphComputer = standaloneSparkWithKryoHadoopGraph(hadoopGremlinLibs);
        Pair<Graph, GraphComputer> graphAndGraphComputer = standaloneSparkWithJanusHadoopGraph(true, hadoopGremlinLibs);

        Graph graph = graphAndGraphComputer.getLeft();
        GraphComputer graphComputer = graphAndGraphComputer.getRight();

        System.out.println("--- PROGRAM STARTING --- ");
        graphComputer.program(PeerPressureVertexProgram.build().create(graph)).mapReduce(ClusterPopulationMapReduce.build().create());
        Future<ComputerResult> work = graphComputer.submit();

        System.out.println(" result =  " + work.get().memory().get("clusterPopulation"));
        System.out.println("--- PROGRAM ENDED --- ");
    }

    public static Pair<Graph, GraphComputer> localSparkWithKryoHadoopGraph() {
        return LocalSparkWithKryoHadoopGraph.newLocalSparkWithKryoHadoopGraph();
    }

    public static Pair<Graph, GraphComputer> standaloneSparkWithKryoHadoopGraph(String hadoopGremlinLibs) {
        System.setProperty("HADOOP_GREMLIN_LIBS", hadoopGremlinLibs);
        return StandaloneSparkWithKryoHadoopGraph.newStandaloneSparkWithKryoHadoopGraph();
    }

    public static Pair<Graph, GraphComputer> standaloneSparkWithJanusHadoopGraph(boolean initialize, String hadoopGremlinLibs) {
        System.setProperty("HADOOP_GREMLIN_LIBS", hadoopGremlinLibs);
        return StandaloneSparkWithJanusHadoopGraph.newStandaloneSparkWithJanusHadoopSparkComputer(initialize);
    }
}

