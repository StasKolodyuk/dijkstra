import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Algo {

    public static void main(String[] args) throws IOException {
        Graph graph = readGraphFromFile("src/main/resources/edges.txt");

        findShortestPathsFrom(graph, "0");
        showShortestPathTo(graph, "5");

        graph.display();
    }

    static void addEdge(Graph graph, int start, int end, int weight) {
        Edge edge = graph.addEdge(start + "-" + end, String.valueOf(start), String.valueOf(end));
        edge.setAttribute("ui.label", weight);
        edge.setAttribute("weight", weight);
        edge.getSourceNode().setAttribute("ui.label", start);
        edge.getTargetNode().setAttribute("ui.label", end);
    }

    public static void findShortestPathsFrom(Graph graph, String from) {
        graph.getNodeIterator().forEachRemaining(node -> node.setAttribute("visited", false));
        graph.getNodeIterator().forEachRemaining(node -> node.setAttribute("length", 1000));
        graph.getNode(from).setAttribute("length", 0);

        walk(graph, from);
    }

    public static void showShortestPathTo(Graph graph, String to) {
        Node toNode = graph.getNode(to);
        String ancestor = toNode.getAttribute("from", String.class);

        if (ancestor == null) {
            return;
        }

        toNode.getEdgeBetween(ancestor).setAttribute("ui.style", "fill-color: red;");
        showShortestPathTo(graph, ancestor);
    }

    public static void walk(Graph graph, String from) {
        Node fromNode = graph.getNode(from);

        fromNode.getEachLeavingEdge().forEach(edge -> {
            Node toNode = edge.getOpposite(fromNode);

            int newLength = fromNode.getAttribute("length", Integer.class) + edge.getAttribute("weight", Integer.class);

            if (newLength < toNode.getAttribute("length", Integer.class)) {
                toNode.setAttribute("length", newLength);
                toNode.setAttribute("from", fromNode.getId());
            }

        });

        fromNode.setAttribute("visited", true);

        Optional<Node> nextNode = graph.getNodeSet().stream()
                .filter(node -> !node.getAttribute("visited", Boolean.class))
                .sorted(Comparator.comparing(node -> node.getAttribute("length", Integer.class)))
                .findFirst();

        if (nextNode.isPresent()) {
            walk(graph, nextNode.get().getId());
        }
    }

    public static Graph readGraphFromFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));

        Graph graph = new SingleGraph("Graph");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        for (String line : lines) {
            String[] tokens = line.split(" ");
            Integer edgeStart = Integer.parseInt(tokens[0]);
            Integer edgeEnd = Integer.parseInt(tokens[1]);
            Integer edgeWeight = Integer.parseInt(tokens[2]);

            addEdge(graph, edgeStart, edgeEnd, edgeWeight);
        }

        return graph;
    }

}
