package com.example.park.blenavigation;

import java.util.*;

public class shortestPath {

    final static int VERTEX_NUMBER = 25;

    Vector<Edge>[] graph = new Vector[VERTEX_NUMBER];

    public shortestPath(){
        for(int i = 0; i < VERTEX_NUMBER; i++) {
            graph[i] = new Vector<Edge>();
        }
        graph[24].add(new Edge(1, 1));
        graph[1].add(new Edge(24, 1));
        graph[1].add(new Edge(2, 1));
        graph[2].add(new Edge(1, 1));
        graph[2].add(new Edge(3, 1));
        graph[3].add(new Edge(2, 1));
        graph[3].add(new Edge(4, 1));
        graph[3].add(new Edge(5, 1));
        graph[4].add(new Edge(3, 1));
        graph[5].add(new Edge(3, 1));
        graph[5].add(new Edge(6, 1));
        graph[6].add(new Edge(5, 1));
        graph[6].add(new Edge(8, 1));
        graph[6].add(new Edge(10, 1));
        graph[6].add(new Edge(13, 1));
        graph[7].add(new Edge(8, 1));
        graph[8].add(new Edge(6, 1));
        graph[8].add(new Edge(7, 1));
        graph[8].add(new Edge(9, 1));
        graph[9].add(new Edge(8, 1));
        graph[10].add(new Edge(6, 1));
        graph[10].add(new Edge(11, 1));
        graph[11].add(new Edge(10, 1));
        graph[11].add(new Edge(12, 1));
        graph[12].add(new Edge(11, 1));
        graph[13].add(new Edge(6, 1));
        graph[13].add(new Edge(14, 1));
        graph[14].add(new Edge(13, 1));
        graph[14].add(new Edge(15, 1));
        graph[14].add(new Edge(16, 1));
        graph[14].add(new Edge(17, 1));
        graph[15].add(new Edge(14, 1));
        graph[16].add(new Edge(14, 1));
        graph[17].add(new Edge(14, 1));
        graph[17].add(new Edge(18, 1));
        graph[18].add(new Edge(17, 1));
        graph[18].add(new Edge(19, 1));
        graph[18].add(new Edge(20, 1));
        graph[19].add(new Edge(18, 1));
        graph[20].add(new Edge(18, 1));
        graph[20].add(new Edge(21, 1));
        graph[21].add(new Edge(20, 1));
        graph[21].add(new Edge(22, 1));
        graph[21].add(new Edge(23, 1));
        graph[22].add(new Edge(21, 1));
        graph[23].add(new Edge(21, 1));
    }

    public static int[] Dijkstra(Vector<Edge>[] G, int src, int dest){
        int vertexs = G.length;
        int[] minDistance = new int[vertexs];
        int[] pastVertex = new int[vertexs];
        boolean[] confirmed = new boolean[vertexs];

        /* find shortest distance */
        Arrays.fill(minDistance, 987654321);
        minDistance[src] = 0;

        while(true){
            //1. pick shortest V (confirmed[V] == false)
            int v = -1, mind = 987654321;
            for(int i = 0; i < vertexs; i++){
                if(confirmed[i]) continue;
                if(minDistance[i] < mind){
                    v = i;
                    mind = minDistance[i];
                }
            }
            if(v < 0) break;

            // 2. set mindistance of v
            confirmed[v] = true;

            // 3. renewal distance of adj[v]
            for(Edge edge : G[v]){
                int next = edge.to;
                int newDistance = mind + edge.cost;
                if(minDistance[next] > newDistance){
                    minDistance[next] = newDistance;
                    pastVertex[next] = v;
                }
            }
        }

        /* find shortest path */
        int[] minPath = new int[vertexs];
        int pathLength = 0;
        int curVertex = dest;
        pastVertex[src] = -1;
        while(curVertex >= 0){
            minPath[pathLength++] = curVertex;
            curVertex = pastVertex[curVertex];
        }

        return minPath;
    }
}

class Edge{
    int to, cost;

    public Edge(int to, int cost){
        this.to = to;
        this.cost = cost;
    }
}
