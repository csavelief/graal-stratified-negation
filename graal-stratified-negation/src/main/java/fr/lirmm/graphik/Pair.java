package fr.lirmm.graphik;

class Pair<L, R> {

  private final L first_;
  private final R last_;

  public Pair(L first, R last) {
    this.first_ = first;
    this.last_ = last;
  }

  public L getFirst() {
    return first_;
  }

  public R getLast() {
    return last_;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    sb.append(first_);
    sb.append(",");
    sb.append(last_);
    sb.append(">");
    return sb.toString();
  }
}
