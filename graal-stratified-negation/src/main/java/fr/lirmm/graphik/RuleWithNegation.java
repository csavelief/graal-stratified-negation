package fr.lirmm.graphik;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

class RuleWithNegation extends DefaultRule {

  private final InMemoryAtomSet negativeBody_;
  private final int indice_;

  public RuleWithNegation(String label, InMemoryAtomSet positiveBody, InMemoryAtomSet negativeBody,
      InMemoryAtomSet head) {

    super(label, positiveBody, head);

    negativeBody_ = negativeBody;
    indice_ = Integer.parseInt(getLabel());
  }

  /**
   * Get the negative body (the hypothesis) of this rule.
   *
   * @return the body of this rule.
   */
  public InMemoryAtomSet negativeBody() {
    return negativeBody_;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendTo(sb);
    return sb.toString();
  }

  @Override
  public void appendTo(StringBuilder builder) {

    if (!getLabel().isEmpty()) {
      builder.append('[');
      builder.append(getLabel());
      builder.append("] ");
    }

    builder.append("[");

    // Positive body
    for (Predicate p : getBody().getPredicates()) {
      try (CloseableIteratorWithoutException<Atom> itAtom = getBody().atomsByPredicate(p)) {
        while (itAtom.hasNext()) {
          Atom a = itAtom.next();
          builder.append(a.toString());
          builder.append(" , ");
        }
      }
    }

    builder.replace(builder.length() - 2, builder.length(), "");

    // Negative body
    for (Predicate p : negativeBody().getPredicates()) {
      try (CloseableIteratorWithoutException<Atom> itAtom = negativeBody().atomsByPredicate(p)) {
        while (itAtom.hasNext()) {
          Atom a = itAtom.next();
          builder.append(" , !");
          builder.append(a.toString());
        }
      }
    }

    builder.append("] -> ");
    builder.append(getHead());
  }

  public int indice() {
    return indice_;
  }
}
