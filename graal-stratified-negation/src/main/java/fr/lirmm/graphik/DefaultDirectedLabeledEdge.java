package fr.lirmm.graphik;

import fr.lirmm.graphik.util.graph.DirectedEdge;

class DefaultDirectedLabeledEdge implements DirectedEdge {

  private final int tail;
  private final int head;
  private final char label;


  // /////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  // /////////////////////////////////////////////////////////////////////////


  public DefaultDirectedLabeledEdge(int tail, int head, char label) {
    this.tail = tail;
    this.head = head;
    this.label = label;
  }


  // /////////////////////////////////////////////////////////////////////////
  // PUBLIC METHODS
  // /////////////////////////////////////////////////////////////////////////

  @Override
  public int getFirst() {
    return this.getTail();
  }

  @Override
  public int getSecond() {
    return this.getHead();
  }

  @Override
  public int getHead() {
    return this.head;
  }

  @Override
  public int getTail() {
    return this.tail;
  }

  public char getLabel() {
    return this.label;
  }

  @Override
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
