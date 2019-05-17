package fr.lirmm.graphik;

import java.util.List;

import com.google.errorprone.annotations.Var;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Term;

public class DefaultConjunctiveQueryWithNegation extends DefaultConjunctiveQuery implements ConjunctiveQueryWithNegation {

	private final InMemoryAtomSet positiveAtomSet;
	private final InMemoryAtomSet negativeAtomSet;
	private List<Term> responseVariables;
	private String label;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	public DefaultConjunctiveQueryWithNegation(InMemoryAtomSet positiveAtomSet, InMemoryAtomSet negagtiveAtomSet , List<Term> ans) {
		this("", positiveAtomSet, negagtiveAtomSet, ans);
	}

	/**
	 * 
	 * @param label
	 *            the name of this query
	 * @param positiveAtomSet
	 *            the conjunction of atom representing the query
	 * @param ans
	 *            the list of answer variables
	 */
	private DefaultConjunctiveQueryWithNegation(String label, InMemoryAtomSet positiveAtomSet, InMemoryAtomSet negativeAtomSet, List<Term> ans) {
		this.positiveAtomSet = positiveAtomSet;
		this.negativeAtomSet = negativeAtomSet;
		this.responseVariables = ans;
		this.label = label;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the positive facts of the query.
	 */
	@Override
	public InMemoryAtomSet getPositiveAtomSet() {
		return this.positiveAtomSet;
	}
	
	/**
	 * Returns the negative facts of the query.
	 */
	@Override
	public InMemoryAtomSet getNegativeAtomSet() {
		return this.negativeAtomSet;
	}
	
	/**
	 * Returns the answer variables of the query.
	 */
	@Override
	public List<Term> getAnswerVariables() {
		return this.responseVariables;
	}

	@Override
	public void setAnswerVariables(List<Term> v) {
		this.responseVariables = v;
	}

	@Override
	public boolean isBoolean() {
		return responseVariables.isEmpty();
	}

	
	// /////////////////////////////////////////////////////////////////////////
	// OVERRIDE METHODS
	// /////////////////////////////////////////////////////////////////////////

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
		this.appendTo(sb);
		return sb.toString();
	}

	@Override
	public void appendTo(StringBuilder sb) {
		sb.append("ANS(");
		@Var boolean first = true;
		for (Term t : this.responseVariables) {
			if(!first) {
				sb.append(',');
			}
			first = false;
			sb.append(t);
		}
		sb.append(") : ");
		sb.append(this.positiveAtomSet);
		sb.append(", !");
		sb.append(this.negativeAtomSet);
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
		return this.equals(other);
	}

	private boolean equals(ConjunctiveQueryWithNegation other) {
		return this.getAnswerVariables().equals(other.getAnswerVariables())
		       && this.getPositiveAtomSet().equals(other.getPositiveAtomSet())
		       && this.getNegativeAtomSet().equals(other.getNegativeAtomSet());
	}

	@Override
	public CloseableIteratorWithoutException<Atom> iterator() {
		return null;
	}
}
