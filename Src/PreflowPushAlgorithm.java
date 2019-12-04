import java.io.*;
import java.util.*;


public class PreflowPushAlgorithm {

	//
    // Search all the possible adjacent vertices lower than a given vertex,
	// then create an adjacency list of searched vertices for the given vertex
	//
    public static void updateAdjacencyList(SimpleGraph g, Vertex v) {
    	// Clear the adjacency list and set current to the start
    	v.adjacencyList.clear();
    	v.current = 0;
    	
    	// Update the adjacency list of v
    	for (int i=0; i<v.incidentEdgeList.size(); i++) {
    		Edge e = (Edge) v.incidentEdgeList.get(i);
    		Vertex w = g.opposite(v, e);
    		
    		// If height of w is 1 less than v
    		if ((int) v.getHeight() == (int) (w.getHeight() + 1) ) {
    			// If e is a forward edge
    			if (g.direction(v, w)
    					// For forward edge, if there is room for flow to increase
    					&& (double) e.getFlow() < (double) e.getData()) {
    				v.adjacencyList.add(w);
    			}
    			// If e is a backward edge
    			else if (!g.direction(v, w) 
    					// For backward edge, if there is room for flow to decrease
    					&& (double) e.getFlow() > 0.0) {
    				v.adjacencyList.add(w);
    			}
    		}
    	}
    }
    	
    //
    // Push operation
    //
	public static boolean push(SimpleGraph g, Vertex v, Vertex w) {
		Edge e = g.findEdge(v, w);
		Double delta = 0.0;
		boolean isSaturating = false;
		
		// Forward edge
		if (g.direction(v, w)) {			
			// Update the smaller one
			if ((double) v.getExcess() < (double) ((Double)e.getData()-e.getFlow())) {
				delta = v.getExcess();
			}else {
				delta = (Double)e.getData() - e.getFlow();
				isSaturating = true;
			}
		}
		// Backward edge
		else {
			// Update the smaller one
			if ((double) v.getExcess() < (double) e.getFlow()) {
				delta = -v.getExcess();
			}else {
				delta = -e.getFlow();
				isSaturating = true;
			}
		}
			
		e.setFlow(e.getFlow() + delta);
		
		//Update excess of v and w after changing a flow
		v.updateExcess();
		w.updateExcess();
		
		// False for a nonsaturating push, true for a saturating push
		return isSaturating;
	}

	//
	// Relabel operation
	//
	public static void relabel(SimpleGraph g, Vertex v) {
		v.relabel();
		
		// Update adjacency list of v, once v is relabeled
		updateAdjacencyList(g, v);
		
		// Update adjacency list of related vertices concerning v, once v is relabeled
		for (int i=0; i<v.incidentEdgeList.size(); i++) {
			Edge e = (Edge) v.incidentEdgeList.get(i);
			Vertex w = g.opposite(v, e);
			
			// Only update vertices that have v in their adjacency lists
			if (w.adjacencyList.contains(v)) {
				updateAdjacencyList(g, w);
			}
		}
	}
	
	//
	// Preflow-Push algorithm main process
	//
	public static void PreflowPush(SimpleGraph graph, Hashtable table) {
		LinkedList eList = graph.edgeList;
		LinkedList vList = graph.vertexList;
		
		// Use a max heap to store vertices which have positive excess
		PriorityQueue<Vertex> excessMaxHeap = new PriorityQueue<Vertex>(
				graph.numVertices(), new Comparator<Vertex>() { 
		    @Override
		    public int compare(Vertex v, Vertex w) {
		    	return w.getHeight() - v.getHeight();
		    }
		});
		
		// Initialize height for all the vertices		
		for (int i=0; i<vList.size(); i++) {
			Vertex v = (Vertex) vList.get(i);
			
			if (v.isSource()) {
				v.setHeight(graph.numVertices());
			}else {
				v.setHeight(0);
			}
		}

		// Initialize flow for all the edges
		for (int i=0; i<eList.size(); i++) {
			Edge e = (Edge) eList.get(i);
			
			if (e.getFirstEndpoint().getName().equals("s") 
					|| e.getSecondEndpoint().getName().equals("s")) {
				e.setFlow((Double) e.getData());
			}else {
				e.setFlow(0.0);
			}
		}
	
		// Update excess for all the vertices
		// Initially add positive-excess vertices to max heap 
		for (int i=0; i<vList.size(); i++) {
			Vertex v = (Vertex) vList.get(i);
			v.updateExcess();
			
			if ((double) v.getExcess() > 0.0) {
				excessMaxHeap.add(v);
			}
		}

		// Start algorithm
		while (!excessMaxHeap.isEmpty()) {
			
			Vertex v = excessMaxHeap.poll();
			System.out.println("Poll out " + v.getName());
			
			// If v hasn't set up adjacency list yet, set it up
			if (v.adjacencyList.isEmpty()) {
				updateAdjacencyList(graph, v);
			}
			
			// Case 1: Relabel
			if ((int) v.current == v.adjacencyList.size()) {
				relabel(graph, v); 
				System.out.println("Relabel " + v.getName() + " with [height: " + v.getHeight() + "]");
				
				// Add v to max heap again if excess of v is positive
				if ((double) v.getExcess() > 0.0 
						&& !v.getName().equals("t") 
						&& !excessMaxHeap.contains(v)) {
					excessMaxHeap.add(v);
					System.out.println("Add " + v.getName() + " with [excess: " + v.getExcess() + 
						", height: " + v.getHeight() + "]");
				}
			}
			// Case 2: Push
			else {
				Vertex w = (Vertex) v.adjacencyList.get(v.current);
				Edge e = graph.findEdge(v, w);
				boolean isSaturatingPush;
				
				isSaturatingPush = push(graph, v, w);	
				
				if (isSaturatingPush) {
					System.out.println("Saturating Push " + v.getName() + " to " + w.getName() + 
							" with [flow: " + e.getFlow() + "]");
				}else {
					System.out.println("Nonsaturating Push " + v.getName() + " to " + w.getName() + 
							" with [flow: " + e.getFlow() + "]");
				}
				
				// Add v to max heap again if excess of v is positive
				if ((double) v.getExcess() > 0.0 
						&& !v.getName().equals("t") 
						&& !excessMaxHeap.contains(v)) {
					excessMaxHeap.add(v);
					System.out.println("Add " + v.getName() + " with [excess: " + v.getExcess() + 
							", height: " + v.getHeight() + "]");
				}
				
				// Add v to max heap again if excess of v is positive
				if ((double) w.getExcess() > 0.0 
						&& !w.getName().equals("t") 
						&& !excessMaxHeap.contains(w)) {
					excessMaxHeap.add(w);
					System.out.println("Add " + w.getName() + " with [excess: " + w.getExcess() + 
							", height: " + w.getHeight() + "]");
				}
				
				// If the push is a saturating push, move to next edge
				if (isSaturatingPush) {
					v.current++;
				}
			}
		}
		
		System.out.println("\nPreflow-Push finished");
		
		// Calculate the maximum flow
		double maxFlow = 0.0;
		Vertex s = (Vertex) table.get("s");
		
		for (int i=0; i<s.incidentEdgeList.size(); i++) {
			Edge e = (Edge) s.incidentEdgeList.get(i);
			maxFlow = maxFlow + e.getFlow();
		}
		System.out.println("Maximum flow of the graph is " + maxFlow);
	}
	
		
	//
	// Simple tests
	//
	public static void main(String args[]) {
		String fileName0 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\SimpleG.txt"; // 8.0
		String fileName1 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\Bipartite1.txt"; // 150.0 
		String fileName2 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\Bipartite2.txt"; // 898.0
		String fileName3 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\20v-3out-4min-355max.txt"; // 368.0
		String fileName4 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\100v-5out-25min-200max.txt"; // 517.0
		String fileName5 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\smallMesh.txt"; // 6.0
		String fileName6 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\mediumMesh.txt"; // 39.0
		String fileName7 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\n10-m10-cmin5-cmax10-f30.txt"; // 25.0
		String fileName8 = "F:\\JAVA\\JDK11\\graphCode\\Graph\\n100-m100-cmin10-cmax20-f949.txt"; // 949.0
		
		SimpleGraph g = new SimpleGraph();
		Hashtable t = GraphInput.LoadSimpleGraph(g, fileName8);
		
		System.out.println("");
		
		// Start recording running time in ms
	    long start = System.currentTimeMillis(); 
	    
		PreflowPush(g, t);
		
		// End recording running time in ms
		long end = System.currentTimeMillis();
		
		System.out.println("");
		System.out.println("Runtime: " + (end - start) + "ms");
	}
}
