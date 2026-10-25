Own Tests and Expected Results

Test 1: DSU — repeated union and disconnected components

Input: my_cases.jsonl, line 1.

Expected output:

{"states":[[4,2],[3,3],[3,3],[2,3]]}

Explanation: The first union creates a component of size 2. The second creates a component of size 3. The repeated union does not change the state. The last union joins vertices 3 and 4.

Test 2: Kruskal — negative weights and self-loop

Input: my_cases.jsonl, line 2.

Expected result: Cost = 2, components = 2, edge IDs = [1,2].

Explanation: The selected edge weights are -2 and 4. The self-loop with weight -100 is not selected because it does not connect different components.

Test 3: Prim — negative weights and self-loop

Input: my_cases.jsonl, line 3.

Expected result: Cost = 2, components = 2, edge IDs = [2,1].

Explanation: Prim may choose a different edge order from Kruskal. Both produce a minimum spanning forest with the same optimal cost.

Test 4: SCC — two strongly connected components

Input: my_cases.jsonl, line 4.

Expected result: Vertices 0 and 1 form one SCC; vertices 2 and 3 form another. The condensation graph contains one edge from the first component to the second.

Explanation: Each pair is mutually reachable, and the edge from vertex 1 to vertex 2 connects the components.

Test 5: Topological sort — acyclic graph

Input: my_cases.jsonl, line 5.

Expected result: acyclic = true, order [0,2,1,3].

Explanation: Every directed edge goes from an earlier vertex to a later vertex in this order.

Test 6: Topological sort — directed cycle

Input: my_cases.jsonl, line 6.

Expected result: acyclic = false, order [].

Explanation: The edges 0 → 1, 1 → 2 and 2 → 0 form a directed cycle.

Test 7: DAG shortest paths — negative edge

Input: my_cases.jsonl, line 7.

Expected result: Distances [0,4,1,3,null]; parent edges [-1,0,2,3,-1].

Explanation: The shortest path to vertex 3 is 0 → 1 → 2 → 3, with total weight 4 - 3 + 2 = 3. Vertex 4 is unreachable.

Test 8: DAG shortest paths — unreachable component

Input: my_cases.jsonl, line 8.

Expected result: Distances [0,2,null,null]; parent edges [-1,0,-1,-1].

Explanation: Vertices 2 and 3 are unreachable from source 0. Their distances must be null.

Commands

Run the custom inputs from the repository root:

py -3 tools/run.py --input tests/my_cases.jsonl

Run the public test suite:

py -3 tools/run.py --check

The custom input command prints actual results but does not automatically compare them with these expected results. The results were checked independently against the graph structures and algorithm contracts.