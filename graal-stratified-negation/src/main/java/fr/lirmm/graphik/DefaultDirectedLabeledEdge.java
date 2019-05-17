 package fr.lirmm.graphik;

import fr.lirmm.graphik.util.graph.DirectedEdge;

class DefaultDirectedLabeledEdge implements DirectedEdge {

	private int tail;
	private int head;
	private char label;
	
	
	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////
	
	
	public DefaultDirectedLabeledEdge(int tail , int head , char label)
	{
		this.tail = tail;
		this.head = head;
		this.label = label;
	}
	
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	
	public int getFirst() {
		return this.getTail();
	}

	public int getSecond() {
		return this.getHead();
	}

	public int getHead() {
		return this.head;
	}

	public int getTail() {
		return this.tail;
	}
	
	public char getLabel() {
		return this.label;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("(");
		s.append(tail);
		s.append("=");
		s.append(label);
		s.append("=>");
		s.append(head);
		s.append(")");
		return s.toString();
	}

}
