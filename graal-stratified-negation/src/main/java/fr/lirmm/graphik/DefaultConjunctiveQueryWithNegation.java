package fr.lirmm.graphik;

import java.util.List;
import java.util.Objects;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

class DefaultConjunctiveQueryWithNegation extends DefaultConjunctiveQuery
    implements ConjunctiveQueryWithNegation {

  private final InMemoryAtomSet positiveAtomSet_;
  private final InMemoryAtomSet negativeAtomSet_;
  private List<Term> responseVariables_;
  private String label_;

  /**
   * Constructor.
   * 
   * @param label the name of this query
   * @param positiveAtomSet the conjunction of atom representing the query
   * @param negativeAtomSet the conjunction of atom representing the query
   * @param ans the list of answer variables
   */
  private DefaultConjunctiveQueryWithNegation(String label, InMemoryAtomSet positiveAtomSet,
      InMemoryAtomSet negativeAtomSet, List<Term> ans) {
    positiveAtomSet_ = positiveAtomSet;
    negativeAtomSet_ = negativeAtomSet;
    responseVariables_ = ans;
    label_ = label;
  }

  @Override
  public String getLabel() {
    return label_;
  }

  @Override
  public void setLabel(String label) {
    label_ = label;
  }

  @Override
  public InMemoryAtomSet getPositiveAtomSet() {
    return positiveAtomSet_;
  }

  @Override
  public InMemoryAtomSet getNegativeAtomSet() {
    return negativeAtomSet_;
  }

  @Override
  public List<Term> getAnswerVariables() {
    return responseVariables_;
  }

  @Override
  public void setAnswerVariables(List<Term> v) {
    responseVariables_ = v;
  }

  @Override
  public boolean isBoolean() {
    return responseVariables_.isEmpty();
  }

  @Override
  public CloseableIteratorWithoutException<Atom> positiveIterator() {
    return getPositiveAtomSet().iterator();
  }

  @Override
  public CloseableIteratorWithoutException<Atom> negativeIterator() {
    return getNegativeAtomSet().iterator();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendTo(sb);
    return sb.toString();
  }

  @Override
  public void appendTo(StringBuilder sb) {
    sb.append("ANS(");
    for (int i = 0; i < responseVariables_.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(responseVariables_.get(i));
    }
    sb.append(") : ");
    sb.append(positiveAtomSet_);
    sb.append(", !");
    sb.append(negativeAtomSet_);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConjunctiveQueryWithNegation)) {
      return false;
    }
    ConjunctiveQueryWithNegation other = (ConjunctiveQueryWithNegation) obj;
    return getAnswerVariables().equals(other.getAnswerVariables())
        && getPositiveAtomSet().equals(other.getPositiveAtomSet())
        && getNegativeAtomSet().equals(other.getNegativeAtomSet());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAnswerVariables(), getPositiveAtomSet(), getNegativeAtomSet());
  }

  @Override
  public CloseableIteratorWithoutException<Atom> iterator() {
    return null;
  }
}
