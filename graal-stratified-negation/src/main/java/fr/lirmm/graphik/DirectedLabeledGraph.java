package fr.lirmm.graphik;

import java.util.ArrayList;

@Deprecated
public interface DirectedLabeledGraph {

	int nbVertices();

	ArrayList<Pair<Integer, Character>> adjacencyList(int v);
	/**
	 * @param e
	 */
	void add(DefaultDirectedLabeledEdge e);

	/**
	 * @param v1
	 * @param v2
	 */
	void addEdge(int tail, int head, char label);

}
