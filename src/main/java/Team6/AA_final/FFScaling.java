package Team6.AA_final;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Team6.graph.Edge;
import Team6.graph.GraphInput;
import Team6.graph.SimpleGraph;
import Team6.graph.Vertex;

public class FFScaling {

	public static void main(String[] args) {
		SimpleGraph graph = new SimpleGraph();
		GraphInput.LoadSimpleGraph(graph, "src\\main\\java\\Team6\\graphGenerationCode\\Random\\n100-m100-cmin10-cmax20-f949.txt");
		SimpleGraph flowGraph = toFlowGraph(graph);
		HashMap<String, Vertex> vertices = getVertexMap(flowGraph);
		System.out.println(FFScaling(flowGraph, vertices));
	}

	@SuppressWarnings("unchecked")
	public static Double FFScaling(SimpleGraph flowGraph, HashMap<String, Vertex> vertices) {
		Double maxFlow = 0.0;
		// Initial delta
		Integer delta = 1;
		Double maxCapOutS = 0.0, t = 0.0;

		for (Iterator<Edge> iterator = flowGraph.incidentEdges(vertices.get("s")); iterator.hasNext();) {
			if ((t = ((Double[]) iterator.next().getData())[0]) > maxCapOutS) {
				maxCapOutS = t;
			}
		}
		while (2 * delta < maxCapOutS) {
			delta *= 2;
		}

		while (delta >= 1) {
			SimpleGraph residualGraph = toResidualGraph(flowGraph);
			SimpleGraph limitedResidualGraph = toLimitedResidualGraph(residualGraph, delta);

			ArrayList<String> path;
			while (null != (path = findPath(limitedResidualGraph))) {
				Double bottleneck = getBottleneck(limitedResidualGraph, path, getVertexMap(limitedResidualGraph));
				maxFlow += bottleneck;
				updateFlowGraph(flowGraph, path, bottleneck);
				residualGraph = toResidualGraph(flowGraph);
				limitedResidualGraph = toLimitedResidualGraph(residualGraph, delta);
			}
			delta /= 2;
		}
		return maxFlow;
	}

	@SuppressWarnings("unchecked")
	private static void updateFlowGraph(SimpleGraph flowGraph, ArrayList<String> path, Double bottleneck) {
		HashMap<String, Vertex> vertices = getVertexMap(flowGraph);
		for (int i = 0; i < path.size() - 1; i++) {
			Vertex v = vertices.get(path.get(i));
			boolean foundForwardEdge = false;
			for (Iterator<Edge> iterator = flowGraph.incidentEdges(v); iterator.hasNext();) {
				Edge e;
				if ((e = iterator.next()).getSecondEndpoint().getName().equals(path.get(i + 1))) {
					Double[] data = (Double[]) e.getData();
					data[1] += bottleneck;
					e.setData(data);
					foundForwardEdge = true;
				}
			}
			if (!foundForwardEdge) {
				v = vertices.get(path.get(i + 1));
				for (Iterator<Edge> iterator = flowGraph.incidentEdges(v); iterator.hasNext();) {
					Edge e;
					if ((e = iterator.next()).getSecondEndpoint().getName().equals(path.get(i))) {
						Double[] data = (Double[]) e.getData();
						data[1] -= bottleneck;
						e.setData(data);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<String> findPath(SimpleGraph limitedResidualGraph) {
		ArrayList<String> list = new ArrayList<>();
		HashMap<String, Vertex> vertices = getVertexMap(limitedResidualGraph);
		if (null == vertices.get("s") || null == vertices.get("t")) {
			return list;
		}

		Queue<Vertex> queue = new LinkedList<Vertex>();
		HashMap<String, String> postV = new HashMap<>();
		queue.add(vertices.get("s"));
		postV.put("s", null);
		while (!queue.isEmpty()) {
			Vertex v = queue.poll();
			for (Iterator<Edge> iterator = limitedResidualGraph.incidentEdges(v); iterator.hasNext();) {
				Edge e = iterator.next();
				if (e.getSecondEndpoint().getName() == "t") {
					postV.put("t", (String) v.getName());
					break;
				} else {
					if (!postV.containsKey((String) e.getSecondEndpoint().getName())) {
						postV.put((String) e.getSecondEndpoint().getName(), (String) v.getName());
						queue.add(e.getSecondEndpoint());
					}
				}
			}
		}
		if (!postV.containsKey("t")) {
			return null;
		}
		String currentV = "t";
		while (postV.get(currentV) != null) {
			list.add(currentV);
			currentV = postV.get(currentV);
		}
		list.add("s");
		Collections.reverse(list);
		return list;
	}

	@SuppressWarnings("unchecked")
	private static Double getBottleneck(SimpleGraph residualGraph, List<String> path,
			HashMap<String, Vertex> residualGraphVertices) {
		Double bottleneck = Double.MAX_VALUE;
		for (int i = 0; i < path.size() - 1; i++) {
			Vertex v = residualGraphVertices.get(path.get(i));
			for (Iterator<Edge> iterator = residualGraph.incidentEdges(v); iterator.hasNext();) {
				Edge e;
				if ((e = iterator.next()).getSecondEndpoint().getName().equals(path.get(i + 1))) {
					bottleneck = bottleneck > ((Double[]) e.getData())[0] ? ((Double[]) e.getData())[0] : bottleneck;
				}
			}
		}
		return bottleneck;
	}

	@SuppressWarnings("unchecked")
	private static SimpleGraph toLimitedResidualGraph(SimpleGraph residualGraph, Integer delta) {
		SimpleGraph limitedResidualGraph = new SimpleGraph();
		for (Iterator<Vertex> iterator = residualGraph.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			limitedResidualGraph.insertVertex(v.getData(), v.getName());
		}
		HashMap<String, Vertex> vertices = getVertexMap(limitedResidualGraph);
		for (Iterator<Edge> iterator = residualGraph.edges(); iterator.hasNext();) {
			Edge e = iterator.next();
			Double[] data = (Double[]) e.getData();
			if (data[0] >= delta) {
				limitedResidualGraph.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
						vertices.get(e.getSecondEndpoint().getName()), new Double[] { data[0], 0.0 }, e.getName());
			}
		}
		return limitedResidualGraph;
	}

	@SuppressWarnings("unchecked")
	public static SimpleGraph toResidualGraph(SimpleGraph flowGraph) {
		SimpleGraph residualGraph = new SimpleGraph();
		for (Iterator<Vertex> iterator = flowGraph.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			residualGraph.insertVertex(v.getData(), v.getName());
		}
		HashMap<String, Vertex> vertices = getVertexMap(residualGraph);
		for (Iterator<Edge> iterator = flowGraph.edges(); iterator.hasNext();) {
			Edge e = iterator.next();
			Double[] data = (Double[]) e.getData();
			if (data[1] == 0) {
				residualGraph.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
						vertices.get(e.getSecondEndpoint().getName()), new Double[] { data[0], 0.0 }, e.getName());
			} else if (data[0] == data[1]) {
				residualGraph.insertEdge(vertices.get(e.getSecondEndpoint().getName()),
						vertices.get(e.getFirstEndpoint().getName()), new Double[] { data[1], 0.0 }, e.getName());
			} else {
				residualGraph.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
						vertices.get(e.getSecondEndpoint().getName()), new Double[] { data[0] - data[1], 0.0 },
						e.getName());
				residualGraph.insertEdge(vertices.get(e.getSecondEndpoint().getName()),
						vertices.get(e.getFirstEndpoint().getName()), new Double[] { data[1], 0.0 }, e.getName());
			}
		}
		return residualGraph;
	}

	@SuppressWarnings("unchecked")
	public static SimpleGraph toFlowGraph(SimpleGraph graph) {
		SimpleGraph flowGraph = new SimpleGraph();
		for (Iterator<Vertex> iterator = graph.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			flowGraph.insertVertex(v.getData(), v.getName());
		}
		HashMap<String, Vertex> vertices = getVertexMap(flowGraph);
		for (Iterator<Edge> iterator = graph.edges(); iterator.hasNext();) {
			Edge e = iterator.next();
			Double data = (Double) e.getData();
			flowGraph.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
					vertices.get(e.getSecondEndpoint().getName()), new Double[] { data, 0.0 }, e.getName());
		}
		return flowGraph;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Vertex> getVertexMap(SimpleGraph flowGraph) {
		HashMap<String, Vertex> map = new HashMap<>();
		for (Iterator<Vertex> iterator = flowGraph.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			map.put((String) v.getName(), v);
		}
		return map;
	}
}
