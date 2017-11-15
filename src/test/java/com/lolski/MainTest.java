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
 * Unit test for simple Main.
 */
public class MainTest {
    public void a() throws InterruptedException, ExecutionException {
        Pair<Graph, GraphComputer> graphAndGraphComputer = LocalSparkWithKryoHadoopGraph.newLocalSparkWithKryoHadoopGraph();

        Graph graph = graphAndGraphComputer.getLeft();
        GraphComputer graphComputer = graphAndGraphComputer.getRight();

        System.out.println("--- PROGRAM STARTING --- ");
        graphComputer.program(PeerPressureVertexProgram.build().create(graph)).mapReduce(ClusterPopulationMapReduce.build().create());
        Future<ComputerResult> work = graphComputer.submit();

        System.out.println(" result =  " + work.get().memory().get("clusterPopulation"));
        System.out.println("--- PROGRAM ENDED --- ");
    }

    public void b() throws InterruptedException, ExecutionException {
        Pair<Graph, GraphComputer> graphAndGraphComputer = StandaloneSparkWithKryoHadoopGraph.newStandaloneSparkWithKryoHadoopGraph();

        Graph graph = graphAndGraphComputer.getLeft();
        GraphComputer graphComputer = graphAndGraphComputer.getRight();

        System.out.println("--- PROGRAM STARTING --- ");
        graphComputer.program(PeerPressureVertexProgram.build().create(graph)).mapReduce(ClusterPopulationMapReduce.build().create());
        Future<ComputerResult> work = graphComputer.submit();

        System.out.println(" result =  " + work.get().memory().get("clusterPopulation"));
        System.out.println("--- PROGRAM ENDED --- ");
    }

    public void c() throws InterruptedException, ExecutionException {
        Pair<Graph, GraphComputer> graphAndGraphComputer = StandaloneSparkWithJanusHadoopGraph.newStandaloneSparkWithJanusHadoopSparkComputer(true);

        Graph graph = graphAndGraphComputer.getLeft();
        GraphComputer graphComputer = graphAndGraphComputer.getRight();

        System.out.println("--- PROGRAM STARTING --- ");
        graphComputer.program(PeerPressureVertexProgram.build().create(graph)).mapReduce(ClusterPopulationMapReduce.build().create());
        Future<ComputerResult> work = graphComputer.submit();

        System.out.println(" result =  " + work.get().memory().get("clusterPopulation"));
        System.out.println("--- PROGRAM ENDED --- ");
    }
}
