import java.util.*;
public class Benchmark {
    private static final int[] SIZES = {1000, 3000, 6000};
    private static final int REPEATS = 3;
    private static final int WARMUP_RUNS = 2;
    private static final long SEED = 20261025L;
    public static void main(String[] args) {
        System.out.println("Kruskal vs Prim benchmark");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Timer: System.nanoTime()");
        System.out.println("Graph generation and validation are outside timed sections.");
        System.out.println();
        for (int n : SIZES) {
            Map<String, Object> graph = generateGraph(n, SEED + n);
            int m = ((int[][]) graph.get("_edges")).length;
            // The algorithms expect JSON-style rows, so create a separate request.
            Map<String, Object> request = makeRequest(n, (int[][]) graph.get("_edges"));
            System.out.println("Graph: n = " + n + ", m = " + m);
            // Validate correctness before collecting timings.
            Map<String, Object> kruskalResult = Algorithms.kruskal(request);
            Map<String, Object> primResult = Algorithms.prim(request);
            validateForest(n, (int[][]) graph.get("_edges"), kruskalResult);
            validateForest(n, (int[][]) graph.get("_edges"), primResult);
            long kruskalCost = ((Number) kruskalResult.get("cost")).longValue();
            long primCost = ((Number) primResult.get("cost")).longValue();
            if (kruskalCost != primCost) {
                throw new IllegalStateException("Kruskal and Prim returned different costs!");
            }
            System.out.println("Correctness: PASS; cost = " + kruskalCost);
            // Warm up the JVM before measuring.
            for (int i = 0; i < WARMUP_RUNS; i++) {
                Algorithms.kruskal(request);
                Algorithms.prim(request);
            }
            long[] kruskalTimes = new long[REPEATS];
            long[] primTimes = new long[REPEATS];
            for (int i = 0; i < REPEATS; i++) {
                long start = System.nanoTime();
                Algorithms.kruskal(request);
                kruskalTimes[i] = System.nanoTime() - start;
                start = System.nanoTime();
                Algorithms.prim(request);
                primTimes[i] = System.nanoTime() - start;
            }
            System.out.println("Kruskal (ms): " + formatTimes(kruskalTimes));
            System.out.println("Prim    (ms): " + formatTimes(primTimes));
            System.out.printf(Locale.US, "Kruskal median: %.3f ms%n",
                    median(kruskalTimes) / 1_000_000.0);
            System.out.printf(Locale.US, "Prim median:    %.3f ms%n",
                    median(primTimes) / 1_000_000.0);
            System.out.println();
        }
        System.out.println("Benchmark completed.");
    }
    private static Map<String, Object> generateGraph(int n, long seed) {
        Random random = new Random(seed);
        int m = 4 * n;
        int[][] edges = new int[m][3];
        // A chain guarantees that the graph is connected.
        for (int i = 0; i < n - 1; i++) {
            edges[i][0] = i;
            edges[i][1] = i + 1;
            edges[i][2] = 1 + random.nextInt(1000);
        }
        // Add random edges. Parallel edges are allowed.
        for (int i = n - 1; i < m; i++) {
            int u = random.nextInt(n);
            int v = random.nextInt(n - 1);
            if (v >= u) {
                v++;
            }
            edges[i][0] = u;
            edges[i][1] = v;
            edges[i][2] = 1 + random.nextInt(1000);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("_edges", edges);
        return result;
    }
    private static Map<String, Object> makeRequest(int n, int[][] edges) {
        List<List<Integer>> rows = new ArrayList<>();
        for (int[] edge : edges) {
            rows.add(List.of(edge[0], edge[1], edge[2]));
        }
        Map<String, Object> request = new HashMap<>();
        request.put("n", n);
        request.put("edges", rows);
        return request;
    }
    private static void validateForest(
            int n, int[][] edges, Map<String, Object> result) {
        List<?> selectedIds = (List<?>) result.get("edge_ids");
        long reportedCost = ((Number) result.get("cost")).longValue();
        int reportedComponents =
                ((Number) result.get("components")).intValue();
        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        long actualCost = 0;
        for (Object value : selectedIds) {
            int id = ((Number) value).intValue();
            if (id < 0 || id >= edges.length) {
                throw new IllegalStateException("Invalid selected edge ID: " + id);
            }
            int u = edges[id][0];
            int v = edges[id][1];
            int rootU = find(parent, u);
            int rootV = find(parent, v);
            if (rootU == rootV) {
                throw new IllegalStateException("Selected edges contain a cycle.");
            }
            union(parent, size, rootU, rootV);
            actualCost += edges[id][2];
        }
        int actualComponents = 0;
        for (int i = 0; i < n; i++) {
            if (find(parent, i) == i) {
                actualComponents++;
            }
        }
        if (actualCost != reportedCost) {
            throw new IllegalStateException("Reported cost does not match selected edges.");
        }
        if (actualComponents != reportedComponents) {
            throw new IllegalStateException("Reported component count is incorrect.");
        }
        // The generated graph is connected, so a spanning tree must have n - 1 edges.
        if (selectedIds.size() != n - 1 || actualComponents != 1) {
            throw new IllegalStateException("The selected edges are not a spanning tree.");
        }
    }
    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }
    private static void union(int[] parent, int[] size, int a, int b) {
        if (size[a] < size[b]) {
            int temp = a;
            a = b;
            b = temp;
        }
        parent[b] = a;
        size[a] += size[b];
    }
    private static long median(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }
    private static String formatTimes(long[] values) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (long value : values) {
            joiner.add(String.format(Locale.US, "%.3f", value / 1_000_000.0));
        }
        return joiner.toString();
    }
}