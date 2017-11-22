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
    public static final boolean preInitializeGraph = System.getProperty("pre_initialize_graph") != null;
    public static final String cassandraAddress = System.getProperty("storage.hostname") != null ? System.getProperty("storage.hostname") : "localhost";
    public static final String sparkMasterAddress = System.getProperty("spark.master") != null ? System.getProperty("spark.master") : AppConstants.SPARK_MASTER_VALUE_STANDALONE;
    public static final String hadoopGremlinLibs = System.getProperty("hadoop_gremlin_libs") != null ? System.getProperty("hadoop_gremlin_libs") : "/Users/lolski/grakn.ai/grakn/grakn-dist/target/grakn-dist-1.0.0-SNAPSHOT/services/lib/";
    // Make sure to set the path as an absolute path. Spark standalone is most likely started from a different location when compared with this program. In that case, a relative path will be a problem
    public static final String sparkOutputLocationRoot = System.getProperty("spark_output_location_root") != null ? System.getProperty("spark_output_location_root") : "/Users/lolski/Playground/janusgraph/g-out";

    public static void main( String[] args ) throws InterruptedException, ExecutionException {
//        Pair<Graph, GraphComputer> graphAndGraphComputer = localSparkWithKryoHadoopGraph();
//        Pair<Graph, GraphComputer> graphAndGraphComputer = standaloneSparkWithKryoHadoopGraph(hadoopGremlinLibs);
        Pair<Graph, GraphComputer> graphAndGraphComputer = standaloneSparkWithJanusHadoopGraph(preInitializeGraph, hadoopGremlinLibs, cassandraAddress, sparkMasterAddress, sparkOutputLocationRoot);

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

    public static Pair<Graph, GraphComputer> standaloneSparkWithJanusHadoopGraph(boolean initialize, String hadoopGremlinLibs, String cassandraAddress, String sparkMasterAddress, String sparkOutputLocationRoot) {
        System.out.println("--- CONFIGURATIONS --- ");
        System.out.println("- pre_initialize_graph " + "'" + initialize + "'");
        System.out.println("- storage.hostname " + "'" + cassandraAddress + "'");
        System.out.println("- spark.master " + "'" + sparkMasterAddress + "'");
        System.out.println("- hadoop_gremlin_libs " + "'" + hadoopGremlinLibs + "'");
        System.out.println("- spark_output_location_root " + "'" + sparkOutputLocationRoot + "'");

        System.setProperty("HADOOP_GREMLIN_LIBS", hadoopGremlinLibs);
        return StandaloneSparkWithJanusHadoopGraph.newStandaloneSparkWithJanusHadoopSparkComputer(initialize, cassandraAddress, sparkMasterAddress, sparkOutputLocationRoot);
    }
}

