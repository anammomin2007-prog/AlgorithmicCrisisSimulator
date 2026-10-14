
package acs;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GraphExample {

    public static void main(String[] args) {

        Map<String, List<String>> graph = new HashMap<>();

        graph.put("A", List.of("B", "D"));
        graph.put("B", List.of("A", "C"));
        graph.put("C", List.of("B", "D"));
        graph.put("D", List.of("A"));

        System.out.println(graph.get("C"));
        System.out.println(graph.get("D"));    }
}
