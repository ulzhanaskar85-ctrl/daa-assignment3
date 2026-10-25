import java.util.*;

/**
 * Implement the six methods and add your route-reconstruction helper.
 * Input extraction is provided; all algorithm state belongs to the current request.
 * See ../../README.md for contracts and Java guidance.
 */
public final class Algorithms {
    /** DSU with path compression and union by size/rank; return states. */
    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        List<List<Integer>> states = new ArrayList<>();

        int components = n;
        int largest = n > 0 ? 1 : 0;

        for (int[] operation : operations) {
            int u = find(parent, operation[0]);
            int v = find(parent, operation[1]);

            if (u != v) {
                if (size[u] < size[v]) {
                    int temp = u;
                    u = v;
                    v = temp;
                }

                parent[v] = u;
                size[u] += size[v];

                components--;
                largest = Math.max(largest, size[u]);
            }

            states.add(Arrays.asList(components, largest));
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("states", states);

        return result;
    }

    private static int find(int[] parent, int vertex) {
        while (parent[vertex] != vertex) {
            parent[vertex] = parent[parent[vertex]];
            vertex = parent[vertex];
        }

        return vertex;
    }

    /** Separate Kruskal implementation; return cost, edge_ids, components. */
    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        int m = edges.length;

        Integer[] order = new Integer[m];

        for (int i = 0; i < m; i++) {
            order[i] = i;
        }

        Arrays.sort(order, (a, b) ->
                Integer.compare(edges[a][2], edges[b][2]));

        int[] parent = new int[n];
        int[] size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        long cost = 0;
        int components = n;

        List<Integer> edgeIds = new ArrayList<>();

        for (int id : order) {
            int u = find(parent, edges[id][0]);
            int v = find(parent, edges[id][1]);

            if (u == v) {
                continue;
            }

            if (size[u] < size[v]) {
                int temp = u;
                u = v;
                v = temp;
            }

            parent[v] = u;
            size[u] += size[v];

            cost += edges[id][2];
            edgeIds.add(id);
            components--;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);

        return result;
    }

    /** Separate Prim implementation; restart for every unvisited component. */
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        for (int id = 0; id < edges.length; id++) {
            int u = edges[id][0];
            int v = edges[id][1];

            adj.get(u).add(id);
            adj.get(v).add(id);
        }

        PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> {
            int comparison = Integer.compare(edges[a][2], edges[b][2]);

            if (comparison != 0) {
                return comparison;
            }

            return Integer.compare(a, b);
        });

        boolean[] visited = new boolean[n];

        long cost = 0;
        int components = 0;

        List<Integer> edgeIds = new ArrayList<>();

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            components++;
            visited[start] = true;

            for (int id : adj.get(start)) {
                pq.add(id);
            }

            while (!pq.isEmpty()) {
                int id = pq.poll();

                int u = edges[id][0];
                int v = edges[id][1];

                if (visited[u] && visited[v]) {
                    continue;
                }

                int next = visited[u] ? v : u;

                visited[next] = true;

                cost += edges[id][2];
                edgeIds.add(id);

                for (int nextId : adj.get(next)) {
                    int a = edges[nextId][0];
                    int b = edges[nextId][1];

                    if (!(visited[a] && visited[b])) {
                        pq.add(nextId);
                    }
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);

        return result;
    }

    /** Kosaraju's two DFS passes; return component and exact condensation. */
    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();
        List<List<Integer>> reverseAdj = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            reverseAdj.add(new ArrayList<>());
        }

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];

            adj.get(u).add(v);
            reverseAdj.get(v).add(u);
        }

        boolean[] visited = new boolean[n];
        int[] next = new int[n];
        List<Integer> order = new ArrayList<>();

        // First DFS pass: calculate finishing order.
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited[start] = true;

            while (!stack.isEmpty()) {
                int v = stack.peek();

                if (next[v] < adj.get(v).size()) {
                    int to = adj.get(v).get(next[v]++);

                    if (!visited[to]) {
                        visited[to] = true;
                        stack.push(to);
                    }
                } else {
                    order.add(stack.pop());
                }
            }
        }

        // Second DFS pass: find components in the reversed graph.
        int[] component = new int[n];
        Arrays.fill(component, -1);

        int componentCount = 0;

        for (int i = order.size() - 1; i >= 0; i--) {
            int start = order.get(i);

            if (component[start] != -1) {
                continue;
            }

            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            component[start] = componentCount;

            while (!stack.isEmpty()) {
                int v = stack.pop();

                for (int to : reverseAdj.get(v)) {
                    if (component[to] == -1) {
                        component[to] = componentCount;
                        stack.push(to);
                    }
                }
            }

            componentCount++;
        }

        // Build the condensation graph without duplicate edges.
        Set<Long> uniqueEdges = new HashSet<>();
        List<List<Integer>> condensation = new ArrayList<>();

        for (int[] edge : edges) {
            int from = component[edge[0]];
            int to = component[edge[1]];

            if (from != to) {
                long key = (long) from * componentCount + to;

                if (uniqueEdges.add(key)) {
                    condensation.add(Arrays.asList(from, to));
                }
            }
        }

        List<Integer> componentList = new ArrayList<>();

        for (int value : component) {
            componentList.add(value);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", componentList);
        result.put("condensation", condensation);

        return result;
    }

    /** Three-state DFS and reverse finish order; return acyclic and order. */
    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        for (int[] edge : edges) {
            adj.get(edge[0]).add(edge[1]);
        }

        int[] state = new int[n];
        int[] next = new int[n];
        List<Integer> order = new ArrayList<>();

        // 0 = unvisited, 1 = visiting, 2 = finished
        for (int start = 0; start < n; start++) {
            if (state[start] != 0) {
                continue;
            }

            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            state[start] = 1;

            while (!stack.isEmpty()) {
                int v = stack.peek();

                if (next[v] < adj.get(v).size()) {
                    int to = adj.get(v).get(next[v]++);

                    if (state[to] == 1) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("acyclic", false);
                        result.put("order", new ArrayList<Integer>());
                        return result;
                    }

                    if (state[to] == 0) {
                        state[to] = 1;
                        stack.push(to);
                    }
                } else {
                    state[v] = 2;
                    order.add(stack.pop());
                }
            }
        }

        Collections.reverse(order);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acyclic", true);
        result.put("order", order);

        return result;
    }

    /** Whole-graph cycle check and DAG relaxation; return acyclic, distance, parent_edge. */
    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        for (int id = 0; id < edges.length; id++) {
            adj.get(edges[id][0]).add(id);
        }

        // Find a topological order.
        List<List<Integer>> graph = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        for (int[] edge : edges) {
            graph.get(edge[0]).add(edge[1]);
        }

        int[] state = new int[n];
        int[] next = new int[n];
        List<Integer> order = new ArrayList<>();

        for (int start = 0; start < n; start++) {
            if (state[start] != 0) {
                continue;
            }

            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            state[start] = 1;

            while (!stack.isEmpty()) {
                int v = stack.peek();

                if (next[v] < graph.get(v).size()) {
                    int to = graph.get(v).get(next[v]++);

                    if (state[to] == 1) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("acyclic", false);
                        result.put("distance", new ArrayList<Integer>());
                        result.put("parent_edge", new ArrayList<Integer>());
                        return result;
                    }

                    if (state[to] == 0) {
                        state[to] = 1;
                        stack.push(to);
                    }
                } else {
                    state[v] = 2;
                    order.add(stack.pop());
                }
            }
        }

        Collections.reverse(order);

        // Calculate shortest distances.
        Long[] distance = new Long[n];
        int[] parentEdge = new int[n];

        Arrays.fill(parentEdge, -1);
        distance[source] = 0L;

        for (int u : order) {
            if (distance[u] == null) {
                continue;
            }

            for (int id : adj.get(u)) {
                int v = edges[id][1];
                long weight = edges[id][2];

                long candidate = distance[u] + weight;

                if (distance[v] == null || candidate < distance[v]) {
                    distance[v] = candidate;
                    parentEdge[v] = id;
                }
            }
        }

        List<Long> distanceList = Arrays.asList(distance);
        List<Integer> parentEdgeList = new ArrayList<>();

        for (int id : parentEdge) {
            parentEdgeList.add(id);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acyclic", true);
        result.put("distance", distanceList);
        result.put("parent_edge", parentEdgeList);

        return result;
    }
    static List<Integer> reconstructRoute(
            int source,
            int target,
            int[][] edges,
            int[] parentEdge) {

        List<Integer> route = new ArrayList<>();

        if (source == target) {
            route.add(source);
            return route;
        }

        if (target < 0 || target >= parentEdge.length
                || source < 0 || source >= parentEdge.length) {
            return route;
        }

        int current = target;

        while (current != source) {
            int id = parentEdge[current];

            if (id < 0 || id >= edges.length) {
                return new ArrayList<>();
            }

            if (edges[id][1] != current) {
                return new ArrayList<>();
            }

            route.add(current);
            current = edges[id][0];

            if (route.size() > parentEdge.length) {
                return new ArrayList<>();
            }
        }

        route.add(source);
        Collections.reverse(route);

        return route;
    }
}
