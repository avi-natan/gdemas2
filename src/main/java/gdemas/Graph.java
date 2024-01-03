package gdemas;

import java.util.*;

class Graph {
    private int V; // Number of vertices
    private List<List<Integer>> adjacencyMatrix;

    public Graph(int V) {
        this.V = V;
        this.adjacencyMatrix = new ArrayList<>(V);

        for (int i = 0; i < V; i++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(V, Integer.MAX_VALUE));
            row.set(i, 0); // Distance to itself is 0
            adjacencyMatrix.add(row);
        }
    }

    public void addEdge(int from, int to, int weight) {
        adjacencyMatrix.get(from).set(to, weight);
        // If it's an undirected graph, you can also add the reverse edge
        // adjacencyMatrix.get(to).set(from, weight);
    }

    public void floydWarshall() {
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (adjacencyMatrix.get(i).get(k) != Integer.MAX_VALUE &&
                            adjacencyMatrix.get(k).get(j) != Integer.MAX_VALUE) {
                        int throughK = adjacencyMatrix.get(i).get(k) + adjacencyMatrix.get(k).get(j);
                        if (throughK < adjacencyMatrix.get(i).get(j)) {
                            adjacencyMatrix.get(i).set(j, throughK);
                        }
                    }
                }
            }
        }
    }

    public void printShortestPaths() {
        for (int i = 0; i < V; ++i) {
            for (int j = 0; j < V; ++j) {
                if (adjacencyMatrix.get(i).get(j) == Integer.MAX_VALUE) {
                    System.out.print("INF ");
                } else {
                    System.out.print(adjacencyMatrix.get(i).get(j) + " ");
                }
            }
            System.out.println();
        }
    }

    public int getMaxPathLen () {
        int maxLen = 0;
        for (List<Integer> matrix : this.adjacencyMatrix) {
            for (Integer integer : matrix) {
                if (integer > maxLen) {
                    maxLen = integer;
                }
            }
        }
        return maxLen;
    }

    public int getMinPathLen () {
        int minLen = Integer.MAX_VALUE;
        for (List<Integer> matrix : this.adjacencyMatrix) {
            for (Integer integer : matrix) {
                if (integer < minLen) {
                    minLen = integer;
                }
            }
        }
        return minLen;
    }

    public double getAvgParthLen () {
        int lenSum = 0;
        int pathCount = 0;
        for (List<Integer> matrix : this.adjacencyMatrix) {
            for (Integer integer : matrix) {
                if (integer == Integer.MAX_VALUE) {
                    return Double.MAX_VALUE;
                }
                lenSum += integer;
                pathCount += 1;
            }
        }
        return lenSum * 1.0 / pathCount;
    }

    public static void main(String[] args) {
        int V = 4; // Number of vertices
        Graph graph = new Graph(V);

        // Adding edges with weights
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);

        System.out.println("Original adjacency matrix:");
        graph.printShortestPaths();

        // Run Floyd-Warshall algorithm
        graph.floydWarshall();

        System.out.println("\nShortest paths after Floyd-Warshall algorithm:");
        graph.printShortestPaths();
    }
}
