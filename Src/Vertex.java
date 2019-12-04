/*
 * Written by Ed Hong UWT Feb. 19, 2003.
 * Modified by Donald Chinn May 14, 2003.
 * Modified by Donald Chinn December 11, 2003.
 */

import java.util.*;

/**
 * Class that represents a vertex in a graph.
 * A name (usually a string, but it can be an arbitrary object)
 * can be associated with the vertex.
 * 
 * Data (also represented by an object (e.g., a string)) can also be
 * associated with a vertex.  This could be useful, for example, if you
 * need to mark a vertex as being visited in some graph traversal.
 * 
 * @author edhong
 * @version 0.0
 */
public class Vertex {
    /** the edge list for this vertex */
    LinkedList incidentEdgeList;

    private Object data;              // an object associated with this vertex
    private Object name;              // a name associated with this vertex
    
    // Params for Preflow-Push
    LinkedList adjacencyList;
    Integer current;
    
    private Double excess;
    private Integer height;
    
    
    /**
     * Constructor that allows data and a name to be associated
     * with the vertex.
     * @param data     an object to be associated with this vertex
     * @param name     a name to be associated with this vertex
     */
    public Vertex(Object data, Object name) {
        this.data = data;
        this.name = name;
        this.incidentEdgeList = new LinkedList();
        
        // Params for Preflow-Push
        this.excess = 0.0;
        this.height = 0;
        this.adjacencyList = new LinkedList();
        this.current = 0;
    }
    
    // Override for Preflow-Push
    public Vertex(Object data, Object name, Double excess, Integer height) {
        this.data = data;
        this.name = name;
        this.incidentEdgeList = new LinkedList();
        
        this.excess = excess;
        this.height = height;
        this.adjacencyList = new LinkedList();
        this.current = 0;
    }
    
    /**
     * Return the name associated with this vertex.
     * @return  the name of this vertex
     */
    public Object getName(){
        return this.name;
    }
    
    /**
     * Return the data associated with this vertex.
     * @return  the data of this vertex
     */
    public Object getData() {
        return this.data;
    }
    
    public Double getExcess() {
    	return this.excess;
    }
    
    public Integer getHeight() {
    	return this.height;
    }
    
    /**
     * Set the data associated with this vertex.
     * @param data  the data of this vertex
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    public void setExcess(Double excess) {
        this.excess = excess;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    /**
     * Return whether this vertex is source/sink or not
     * @return  boolean
     */
    public boolean isSource() {
    	if (name.equals("s")) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public boolean isSink() {
    	if (name.equals("t")) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
    // Relabel the height
    public void relabel() {
    	height++;
    }
    
    // Update excess of this vertex
    public void updateExcess() {
		LinkedList incidentEdges = this.incidentEdgeList;
		Double excessCnt = 0.0;
				
		for (int i=0; i<incidentEdges.size(); i++) {
			Edge e = (Edge) incidentEdges.get(i);
			
			if (e.getFirstEndpoint().equals(this)) {
				excessCnt = excessCnt - e.getFlow();
			}else {
				excessCnt = excessCnt + e.getFlow();
			}
		}
		
		this.setExcess(excessCnt);
    }
}
