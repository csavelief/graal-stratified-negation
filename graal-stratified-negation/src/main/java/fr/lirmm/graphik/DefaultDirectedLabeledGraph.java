package fr.lirmm.graphik;

import java.util.ArrayList;
import java.util.Iterator;

import fr.lirmm.graphik.graal.api.core.Rule;

@Deprecated
public class DefaultDirectedLabeledGraph<V,E> implements DirectedLabeledGraph{

	private ArrayList<ArrayList<Pair<Integer, Character>>> adjacencyList;
	private int nbVertices;
	
	public DefaultDirectedLabeledGraph(Iterable<Rule> rules) {
		
		this.adjacencyList = new ArrayList<ArrayList<Pair<Integer, Character>>>();
		this.nbVertices = 0;
		
		for(Iterator<Rule> i = rules.iterator() ; i.hasNext() ; i.next())
		{
			this.adjacencyList.add(new ArrayList<Pair<Integer,Character>>());
			this.nbVertices++;
		}
	}
	
	public int nbVertices() {
		return this.nbVertices;
	}

	public ArrayList<Pair<Integer, Character>> adjacencyList(int v) {
		return this.adjacencyList.get(v);
	}

	public ArrayList<Pair<Integer, Character>> getEdges(int src)
	{
		return adjacencyList.get(src);
	}

	public void add(DefaultDirectedLabeledEdge e) {
		this.addEdge(e.getTail(), e.getHead(), e.getLabel());
		
	}
	
	public void addEdge(int tail, int head, char label) {
		ArrayList<Pair<Integer, Character>> l = adjacencyList.get(tail);
		l.add(new Pair<Integer, Character>(head, label));
		
		adjacencyList.set(tail, l);
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		int i = 0;
		for(Iterator<ArrayList<Pair<Integer, Character>>> it = adjacencyList.iterator() ; it.hasNext() ; i++)
		{
			s.append("R");
			s.append(i);
			s.append(" : ");
			s.append(it.next().toString());
			s.append("\n");
		}
		return s.toString();
	}
}
