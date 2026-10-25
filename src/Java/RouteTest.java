import java.util.Arrays;
import java.util.List;

public class RouteTest {

    public static void main(String[] args) {

        // Graph edges: [from, to, weight]
        int[][] edges = {
                {0, 1, 4},
                {0, 2, 10},
                {1, 2, -3},
                {2, 3, 2}
        };

        // Test 1: Reachable target 3
        int[] parents = {-1, 0, 2, 3, -1};

        List<Integer> route = Algorithms.reconstructRoute(
                0, 3, edges, parents
        );

        System.out.println("Reachable target: " + route);
        System.out.println("Expected: [0, 1, 2, 3]");

        // Test 2: Source to itself
        List<Integer> sourceRoute = Algorithms.reconstructRoute(
                0, 0, edges, parents
        );

        System.out.println("Source to itself: " + sourceRoute);
        System.out.println("Expected: [0]");

        // Test 3: Unreachable target 4
        List<Integer> unreachableRoute = Algorithms.reconstructRoute(
                0, 4, edges, parents
        );

        System.out.println("Unreachable target: " + unreachableRoute);
        System.out.println("Expected: []");

        // Test 4: Parallel edges
        int[][] parallelEdges = {
                {0, 1, 5},
                {0, 1, 2},
                {1, 2, 3}
        };

        int[] parallelParents = {-1, 1, 2};

        List<Integer> parallelRoute = Algorithms.reconstructRoute(
                0, 2, parallelEdges, parallelParents
        );

        System.out.println("Parallel edges: " + parallelRoute);
        System.out.println("Expected: [0, 1, 2]");
    }
}