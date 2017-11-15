package com.lolski;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.ClusterPopulationMapReduce;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.lolski.Helpers.*;

/**
 * Hello world with JanusGraph & Spark Standalone!
 * This PoC works with Spark in both in memory mode (e.g. local[1]) and standalone mode (e.g. spark://127.0.0.1:5678)
 * Needs cassandra to run
 *
 */
public class Main {
    public static void main( String[] args ) throws InterruptedException, ExecutionException {
//        Pair<Graph, GraphComputer> graphAndGraphComputer = LocalKryo.newLocalKryoSparkComputer();
//        Pair<Graph, GraphComputer> graphAndGraphComputer = StandaloneKryo.newStandaloneKryoSparkComputer();
        Pair<Graph, GraphComputer> graphAndGraphComputer = StandaloneJanus.newStandaloneJanusSparkComputer(false);

        Graph graph = graphAndGraphComputer.getLeft();
        GraphComputer graphComputer = graphAndGraphComputer.getRight();

        p("--- PROGRAM STARTING --- ");
        graphComputer.program(PeerPressureVertexProgram.build().create(graph)).mapReduce(ClusterPopulationMapReduce.build().create());
        Future<ComputerResult> work = graphComputer.submit();

        p(" result =  " + work.get().memory().get("clusterPopulation"));
        p("--- PROGRAM ENDED --- ");
    }
}

