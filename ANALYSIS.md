Assignment 3 - Algorithm Analysis

1. Environment and Verification

Language: Java

Environment: Windows 11, IntelliJ IDEA, Java SDK 25.

The implementation uses Java collections and iterative graph traversals. Although the environment uses Java 25, the implementation is intended to use Java features compatible with Java 17.

Commands

Run the public test suite:

py -3 tools/run.py --check

Run the custom JSON Lines inputs:

py -3 tools/run.py --input tests/my_cases.jsonl

The public test suite passed all 36 published cases, including deep-chain cases and state-reset checks.

Eight additional JSON Lines cases were executed. Their outputs were checked against independently derived expected results. Four direct route-helper scenarios were also executed: a reachable target, the source itself, an unreachable target, and parallel edges.

2. Disjoint Set Union (DSU)

Algorithm

Each vertex starts in its own set. The implementation uses path compression in find() and union by size when joining two sets.

For every union operation, the algorithm finds the roots of both vertices. If they are different, it joins the smaller set to the larger one, decreases the component count, and updates the largest component size. It records the component count and largest component size after every operation.

Invariant

Each vertex belongs to exactly one DSU set. Every root represents one component, and size[root] stores that component’s size. The component counter equals the number of roots.

Correctness

Initially, each of the n vertices is a separate component. A union between different roots merges exactly two components. A union between vertices already in the same component changes nothing. Therefore, after every operation, the component count and largest component size are correct.

Complexity

For n vertices and q operations, the amortized time complexity is O(n + q α(n)), where α is the inverse Ackermann function. Memory complexity is O(n + q), including the recorded states.

Boundary cases

The tests include an empty graph, repeated unions, self-unions, and disconnected components.

3. Kruskal’s Minimum Spanning Forest

Algorithm

The implementation sorts edge IDs by increasing edge weight. It processes edges in that order and uses DSU to determine whether an edge joins two different components. An edge is selected only when its endpoints have different roots.

The implementation records the original edge IDs, adds selected weights to a long total, and decreases the number of components after every successful union.

Invariant

After each processed edge, the selected edges form a forest. The DSU sets correspond exactly to the connected components formed by the selected edges.

Correctness

Kruskal processes edges in nondecreasing weight order. Whenever an edge connects two different components, it is a safe minimum-spanning-forest choice by the cut property. Edges that would create a cycle are rejected. Processing all edges produces a minimum spanning tree for every connected component, including isolated vertices.

Complexity

Time complexity is O(n + m log(m+1)). Memory complexity is O(n + m).

Boundary cases

The public tests include empty graphs, isolated vertices, negative weights, self-loops, parallel edges, tied optimal solutions, and totals beyond 32-bit integer range.

4. Prim’s Minimum Spanning Forest

Algorithm

The implementation builds an adjacency list and uses a priority queue of original edge IDs. It starts a new traversal from every unvisited vertex. From each visited vertex, it adds incident edges to the queue.

When the minimum-weight edge is removed, it is selected only if exactly one endpoint has already been visited. The newly reached vertex is marked visited, and its incident edges are added to the queue.

Invariant

Within each component being processed, all visited vertices are connected by the selected edges. Every selected edge connects the current tree to a new vertex, so the selected edges never form a cycle.

Correctness

At each step, Prim selects a minimum-weight edge crossing the cut between the visited vertices and the remaining vertices. By the cut property, this is a safe choice for a minimum spanning tree. Restarting from each unvisited vertex ensures that disconnected components and isolated vertices are handled.

Complexity

Time complexity is O(n + m log(m+1)) with the lazy priority queue. Memory complexity is O(n + m).

Boundary cases

The public tests include empty graphs, isolated vertices, negative weights, self-loops, parallel edges, tied optima, and large totals. Kruskal and Prim were also run on the same custom graph and both returned the optimal cost of 2.

5. Strongly Connected Components (Kosaraju)

Algorithm

The implementation builds the original graph and its transpose.

The first iterative DFS pass records vertices in finishing order. The second pass processes vertices in reverse finishing order on the transposed graph and assigns a component label to each vertex.

After component labels are known, the implementation scans all original edges. It creates a condensation edge for every distinct pair of different components, using a hash set to remove duplicates.

Invariant

After the second pass, every vertex has exactly one component label. Each component is a maximal set of vertices that can reach one another. The condensation graph contains only edges between distinct components.

Correctness

Kosaraju’s algorithm uses the finishing order from the first DFS to choose the correct order for traversals on the transposed graph. Each second-pass traversal discovers exactly one strongly connected component. Scanning the original edges then identifies all cross-component edges. Deduplication ensures that each distinct condensation edge occurs once.

Complexity

Expected time complexity is O(n + m), assuming expected constant-time hash-set operations. Memory complexity is O(n + m).

Boundary cases

The tests include an empty graph, isolated vertices, duplicate edges, self-loops, and a deep chain.

6. Topological Sorting

Algorithm

The implementation uses iterative depth-first search with three states:

* 0: unvisited;
* 1: currently being visited;
* 2: finished.

An explicit stack and a per-vertex neighbour index preserve the progress of each DFS traversal. If an edge points to a vertex in state 1, the graph contains a directed cycle. Otherwise, vertices are appended in finishing order and that order is reversed.

Invariant

A vertex in state 1 belongs to the current DFS path. A vertex in state 2 has had all outgoing edges processed. For an acyclic graph, reversing finishing order produces a valid topological ordering.

Correctness

An edge to a state-1 vertex identifies a back edge and therefore a directed cycle. In that case, no topological ordering exists. If no such edge is found, the graph is acyclic, and reverse finishing order respects every directed edge.

Complexity

Time complexity is O(n + m). Memory complexity is O(n + m).

Boundary cases

The tests include an empty graph, isolated vertices, parallel edges, a self-cycle, a cycle in a disconnected component, and a deep chain.

7. DAG Shortest Paths

Algorithm

The implementation first checks the entire graph for cycles and constructs a topological order. If any cycle exists, it returns acyclic = false with empty distance and parent-edge arrays.

Otherwise, it initializes the source distance to zero and all other distances to null. It processes vertices in topological order and relaxes outgoing edges only from reachable vertices. Distances use Long, and parent_edge[v] stores the original edge ID selected to reach vertex v.

Invariant

When a vertex is processed in topological order, all its predecessors have already been processed. Therefore, every possible path into that vertex has been considered before its outgoing edges are relaxed.

Correctness

A DAG has a topological ordering. Since every edge goes forward in this ordering, relaxing outgoing edges in that order computes the shortest distance to every reachable vertex, including when negative edge weights are present. Vertices that cannot be reached retain a null distance. The graph is checked for cycles even in components unreachable from the source.

Complexity

Time complexity is O(n + m). Memory complexity is O(n + m).

Boundary cases

The public tests include singleton graphs, unreachable vertices, cycles outside the source’s reachable component, self-loops, tied paths, parallel edges, large totals, and deep chains.

8. Route Reconstruction Helper

The helper reconstructRoute(source, target, edges, parentEdge) follows parent edge IDs backwards from the target to the source. It checks that each selected edge ends at the current vertex, then reverses the collected vertex sequence.

The helper returns an empty list for an unreachable target and [source] when the source and target are identical.

Executed cases

1. Reachable target: source 0, target 3. Result: [0, 1, 2, 3]. Selected edge IDs: [0, 2, 3]. Total weight: 4 - 3 + 2 = 3.
2. Source itself: source 0, target 0. Result: [0]. Total weight: 0.
3. Unreachable target: source 0, target 4. Result: [].
4. Parallel edges: source 0, target 2. Result: [0, 1, 2]. The parent edge for vertex 1 selects the second parallel edge, ID 1, with weight 2; the edge from 1 to 2 has ID 2 and weight 3. Total weight: 5.

The route-helper tests were executed directly from a Java test class. The output matched the expected results.

Complexity

For a route containing L vertices, reconstruction takes O(L) time and O(L) output space.

9. State Reset and Deep DFS

All algorithm state is stored in local variables created during each method call. No DSU arrays, visited states, component labels, distances, or parent-edge arrays are stored in mutable static fields.

The public test suite passed the state-reset test, which processes multiple batches in one process. It also passed the published deep-chain tests for SCC, topological sorting, and DAG shortest paths.

Iterative DFS is used in graph traversals to avoid Java call-stack overflow on deep graphs.

10. Benchmarking and Performance Evaluation

Environment

The benchmark was executed on Windows using OpenJDK 25.0.1 and IntelliJ IDEA 2025.3. The timer was System.nanoTime(). Java compilation and process startup were excluded from the measured intervals because the benchmark was executed inside an already-running Java program.

Graph Generation

Three undirected weighted graphs were generated with the following sizes:

Graph	Vertices (n)	Edges (m)	Random seed
1	1,000	4,000	20262025
2	3,000	12,000	20264025
3	6,000	24,000	20267025

Each graph contains a chain connecting all vertices, followed by randomly generated edges. This guarantees that every generated graph is connected. Edge weights are positive integers from 1 to 1,000. Parallel edges are allowed, and self-loops are not generated.

The graph was generated once per size and reused by both algorithms. Graph generation and correctness validation were outside the timed sections. The measured intervals included the algorithm call and its input-processing overhead, but excluded graph generation, validation, compilation, and process startup.

Correctness Validation

Before collecting timing measurements, both algorithms were executed on each graph. The benchmark checked that:

* Both algorithms returned the same total cost.
* Every selected edge ID was valid.
* The selected edges did not contain a cycle.
* The sum of selected edge weights matched the reported cost.
* The reported number of components was correct.
* The selected edges formed a spanning tree containing exactly n − 1 edges.

All three graphs passed these checks for both algorithms.

Timing Results

The JVM was warmed up with two executions of each algorithm for every graph. Each algorithm was then measured three times per graph. Times are reported in milliseconds.

Vertices (n)	Edges (m)	Kruskal runs (ms)	Kruskal median (ms)	Prim runs (ms)	Prim median (ms)
1,000	4,000	2.322, 2.381, 2.653	2.381	4.614, 4.268, 2.363	4.268
3,000	12,000	10.441, 10.731, 12.311	10.731	7.040, 5.812, 9.207	7.040
6,000	24,000	19.001, 21.529, 17.069	19.001	29.196, 18.567, 9.642	18.567

Observations

Both algorithms produced valid minimum spanning trees with matching costs for every tested graph.

Kruskal had a lower median time on the graph with 1,000 vertices. Prim had a lower median time on the graph with 3,000 vertices, while their median times were relatively close on the graph with 6,000 vertices.

The individual measurements varied, particularly for Prim on the largest graph. This variability may result from JVM warm-up effects, garbage collection, operating-system scheduling, and other runtime factors. Only three timed runs were collected per algorithm and graph, so these measurements should be treated as a small experiment rather than a definitive performance comparison.

The theoretical time complexities are O(m log m) for Kruskal and O((n + m) log n) for the implemented heap-based Prim algorithm. The observed results do not establish that one algorithm is universally faster. Performance depends on graph structure, implementation details, and the execution environment.

Reproducibility

The benchmark can be reproduced by running Benchmark.main() from IntelliJ IDEA after compiling the project. The generator uses a fixed seed for each graph size, making the generated graphs reproducible. The benchmark source is src/Java/Benchmark.java.

Limitations

The experiment used one runtime environment, three graph sizes, and three timed runs per algorithm and graph. No formal statistical analysis was performed. The results should not be generalized to all graph types or hardware configurations.

11. Academic Integrity and AI Assistance

ChatGPT was used to assist with implementation guidance, explanations, and preparation of test cases and analysis text. The student ran the published test suite, executed custom inputs, and directly tested the route-reconstruction helper.

The student is responsible for reviewing the implementation, understanding the algorithms, independently verifying results, and ensuring that the final submission complies with the course’s academic-integrity rules.