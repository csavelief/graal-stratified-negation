package fr.lirmm.graphik;

import fr.lirmm.graphik.util.graph.DirectedEdge;

class DirectedLabeledEdge implements DirectedEdge {

  private final int tail_;
  private final int head_;
  private final char label_;

  public DirectedLabeledEdge(int tail, int head, char label) {
    tail_ = tail;
    head_ = head;
    label_ = label;
  }

  @Override
  public int getFirst() {
    return getTail();
  }

  @Override
  public int getSecond() {
    return getHead();
  }

  @Override
  public int getHead() {
    return head_;
  }

  @Override
  public int getTail() {
    return tail_;
  }

  public char getLabel() {
    return label_;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(tail_);
    sb.append("=");
    sb.append(label_);
    sb.append("=>");
    sb.append(head_);
    sb.append(")");
    return sb.toString();
  }
}
