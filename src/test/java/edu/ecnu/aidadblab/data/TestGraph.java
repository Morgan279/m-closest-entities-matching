package edu.ecnu.aidadblab.data;

import edu.ecnu.aidadblab.data.model.Graph;
import edu.ecnu.aidadblab.data.model.Vertex;

public class TestGraph {

    public Graph loadTestQueryGraph1() {
        Graph queryGraph = new Graph();
        Vertex u0 = new Vertex("u0", "A");
        Vertex u1 = new Vertex("u1", "B");
        Vertex u2 = new Vertex("u2", "C");
        Vertex u3 = new Vertex("u3", "D");
        queryGraph.addVertex(u0);
        queryGraph.addVertex(u1);
        queryGraph.addVertex(u2);
        queryGraph.addVertex(u3);
        queryGraph.addEdge(u0, u1);
        queryGraph.addEdge(u0, u2);
        queryGraph.addEdge(u1, u2);
        queryGraph.addEdge(u3, u1);
        queryGraph.addEdge(u3, u2);
        return queryGraph;
    }


    public Graph loadTestDataGraph1() {
        Graph dataGraph = new Graph();
        Vertex v0 = new Vertex("v0", "A");
        Vertex v1 = new Vertex("v1", "C");
        Vertex v2 = new Vertex("v2", "B");
        Vertex v3 = new Vertex("v3", "C");
        Vertex v4 = new Vertex("v4", "B");
        Vertex v5 = new Vertex("v5", "C");
        Vertex v6 = new Vertex("v6", "B");
        Vertex v7 = new Vertex("v7", "C");
        Vertex v8 = new Vertex("v8", "D");
        Vertex v9 = new Vertex("v9", "D");
        Vertex v10 = new Vertex("v10", "D");
        Vertex v11 = new Vertex("v11", "D");
        Vertex v12 = new Vertex("v12", "D");
        Vertex v13 = new Vertex("v13", "D");
        Vertex v14 = new Vertex("v14", "D");
        dataGraph.addVertex(v0);
        dataGraph.addVertex(v1);
        dataGraph.addVertex(v2);
        dataGraph.addVertex(v3);
        dataGraph.addVertex(v4);
        dataGraph.addVertex(v5);
        dataGraph.addVertex(v6);
        dataGraph.addVertex(v7);
        dataGraph.addVertex(v8);
        dataGraph.addVertex(v9);
        dataGraph.addVertex(v10);
        dataGraph.addVertex(v10);
        dataGraph.addVertex(v11);
        dataGraph.addVertex(v12);
        dataGraph.addVertex(v13);
        dataGraph.addVertex(v14);
        dataGraph.addEdge(v0, v1);
        dataGraph.addEdge(v0, v2);
        dataGraph.addEdge(v0, v3);
        dataGraph.addEdge(v0, v4);
        dataGraph.addEdge(v0, v5);
        dataGraph.addEdge(v0, v6);
        dataGraph.addEdge(v0, v7);
        dataGraph.addEdge(v1, v8);
        dataGraph.addEdge(v1, v2);
        dataGraph.addEdge(v2, v9);
        dataGraph.addEdge(v2, v10);
        dataGraph.addEdge(v9, v10);
        dataGraph.addEdge(v3, v4);
        dataGraph.addEdge(v3, v10);
        dataGraph.addEdge(v4, v10);
        dataGraph.addEdge(v4, v5);
        dataGraph.addEdge(v4, v11);
        dataGraph.addEdge(v4, v12);
        dataGraph.addEdge(v5, v12);
        dataGraph.addEdge(v6, v12);
        dataGraph.addEdge(v6, v13);
        dataGraph.addEdge(v7, v14);
        return dataGraph;
    }


    public Graph loadTestQueryGraph2() {
        Graph queryGraph = new Graph();
        Vertex u0 = new Vertex("u0", "A");
        Vertex u1 = new Vertex("u1", "B");
        Vertex u2 = new Vertex("u2", "C");
        Vertex u3 = new Vertex("u3", "A");
        queryGraph.addVertex(u0);
        queryGraph.addVertex(u1);
        queryGraph.addVertex(u2);
        queryGraph.addVertex(u3);
        queryGraph.addEdge(u0, u1);
        queryGraph.addEdge(u0, u2);
        queryGraph.addEdge(u0, u3);
        queryGraph.addEdge(u1, u3);
        queryGraph.addEdge(u2, u3);
        return queryGraph;
    }

    public Graph loadTestDataGraph2() {
        Graph dataGraph = new Graph();
        Vertex v0 = new Vertex("v0", "A");
        Vertex v1 = new Vertex("v1", "A");
        Vertex v2 = new Vertex("v2", "C");
        Vertex v3 = new Vertex("v3", "B");
        Vertex v4 = new Vertex("v4", "A");
        Vertex v5 = new Vertex("v5", "C");
        Vertex v6 = new Vertex("v6", "A");
        Vertex v7 = new Vertex("v7", "B");
        Vertex v8 = new Vertex("v8", "A");
        Vertex v9 = new Vertex("v9", "C");
        Vertex v10 = new Vertex("v10", "A");
        Vertex v11 = new Vertex("v11", "B");
        Vertex v12 = new Vertex("v12", "B");
        Vertex v13 = new Vertex("v13", "C");
        Vertex v14 = new Vertex("v14", "B");
        Vertex v15 = new Vertex("v15", "C");
        dataGraph.addVertex(v0);
        dataGraph.addVertex(v1);
        dataGraph.addVertex(v2);
        dataGraph.addVertex(v3);
        dataGraph.addVertex(v4);
        dataGraph.addVertex(v5);
        dataGraph.addVertex(v6);
        dataGraph.addVertex(v7);
        dataGraph.addVertex(v8);
        dataGraph.addVertex(v9);
        dataGraph.addVertex(v10);
        dataGraph.addVertex(v10);
        dataGraph.addVertex(v11);
        dataGraph.addVertex(v12);
        dataGraph.addVertex(v13);
        dataGraph.addVertex(v14);
        dataGraph.addVertex(v15);

        dataGraph.addEdge(v0, v2);
        dataGraph.addEdge(v0, v3);
        dataGraph.addEdge(v0, v4);
        dataGraph.addEdge(v1, v4);
        dataGraph.addEdge(v1, v6);
        dataGraph.addEdge(v1, v7);
        dataGraph.addEdge(v1, v8);
        dataGraph.addEdge(v1, v9);
        dataGraph.addEdge(v2, v10);
        dataGraph.addEdge(v3, v4);
        dataGraph.addEdge(v4, v11);
        dataGraph.addEdge(v4, v5);
        dataGraph.addEdge(v5, v6);
        dataGraph.addEdge(v6, v12);
        dataGraph.addEdge(v6, v13);
        dataGraph.addEdge(v7, v8);
        dataGraph.addEdge(v8, v14);
        dataGraph.addEdge(v8, v9);
        dataGraph.addEdge(v9, v15);
        return dataGraph;
    }


    public Graph loadTestQueryGraph3() {
        Graph queryGraph = new Graph();
        Vertex u0 = new Vertex("u0", "A");
        Vertex u1 = new Vertex("u1", "B");
        Vertex u2 = new Vertex("u2", "C");
        Vertex u3 = new Vertex("u3", "D");
        queryGraph.addVertex(u0);
        queryGraph.addVertex(u1);
        queryGraph.addVertex(u2);
        queryGraph.addVertex(u3);
        queryGraph.addEdge(u0, u1);
        queryGraph.addEdge(u0, u2);
        queryGraph.addEdge(u0, u3);
        queryGraph.addEdge(u1, u3);
        queryGraph.addEdge(u2, u3);
        return queryGraph;
    }

    public Graph loadTestDataGraph3() {
        Graph dataGraph = new Graph();
        Vertex v0 = new Vertex("v0", "A");
        Vertex v1 = new Vertex("v1", "B");
        Vertex v2 = new Vertex("v2", "B");
        Vertex v3 = new Vertex("v3", "C");
        Vertex v4 = new Vertex("v4", "C");
        Vertex v5 = new Vertex("v5", "C");
        Vertex v6 = new Vertex("v6", "D");
        Vertex v7 = new Vertex("v7", "D");
        Vertex v8 = new Vertex("v8", "D");
        Vertex v9 = new Vertex("v9", "D");
        Vertex v10 = new Vertex("v10", "D");
        Vertex v11 = new Vertex("v11", "D");
        Vertex v12 = new Vertex("v12", "D");

        dataGraph.addVertex(v0);
        dataGraph.addVertex(v1);
        dataGraph.addVertex(v2);
        dataGraph.addVertex(v3);
        dataGraph.addVertex(v4);
        dataGraph.addVertex(v5);
        dataGraph.addVertex(v6);
        dataGraph.addVertex(v7);
        dataGraph.addVertex(v8);
        dataGraph.addVertex(v9);
        dataGraph.addVertex(v10);
        dataGraph.addVertex(v11);
        dataGraph.addVertex(v12);

        dataGraph.addEdge(v0, v1);
        dataGraph.addEdge(v0, v2);
        dataGraph.addEdge(v0, v3);
        dataGraph.addEdge(v0, v4);
        dataGraph.addEdge(v0, v5);
        dataGraph.addEdge(v0, v6);
        dataGraph.addEdge(v0, v7);
        dataGraph.addEdge(v0, v8);
        dataGraph.addEdge(v0, v9);
        dataGraph.addEdge(v0, v10);
        dataGraph.addEdge(v0, v11);
        dataGraph.addEdge(v0, v12);
        dataGraph.addEdge(v1, v6);
        dataGraph.addEdge(v1, v7);
        dataGraph.addEdge(v1, v8);
        dataGraph.addEdge(v2, v9);
        dataGraph.addEdge(v2, v10);
        dataGraph.addEdge(v2, v11);
        dataGraph.addEdge(v2, v12);
        dataGraph.addEdge(v3, v6);
        dataGraph.addEdge(v3, v7);
        dataGraph.addEdge(v3, v8);
        dataGraph.addEdge(v4, v9);
        dataGraph.addEdge(v4, v10);
        dataGraph.addEdge(v5, v11);
        dataGraph.addEdge(v5, v12);
        return dataGraph;
    }

}
