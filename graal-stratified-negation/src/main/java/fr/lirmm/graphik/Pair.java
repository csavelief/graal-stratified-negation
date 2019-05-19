package fr.lirmm.graphik;

class Pair<L, R> {

  private final L first;
  private final R last;

  public Pair(L first, R last) {
    this.first = first;
    this.last = last;
  }

  public L getFirst() {
    return first;
  }

  public R getLast() {
    return last;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("<");
    s.append(first);
    s.append(",");
    s.append(last);
    s.append(">");
    return s.toString();
  }
}
