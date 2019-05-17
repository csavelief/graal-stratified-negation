package fr.lirmm.graphik;

import java.util.ArrayList;

@Deprecated
public interface DirectedLabeledGraph {

	int nbVertices();

	ArrayList<Pair<Integer, Character>> adjacencyList(int v);

	void add(DefaultDirectedLabeledEdge e);

	void addEdge(int tail, int head, char label);
}
