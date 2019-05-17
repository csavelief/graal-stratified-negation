package fr.lirmm.graphik;

import java.util.LinkedList;
import java.util.List;

import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
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

	public String getLabel() {
		return this.label;
	}

	
	public void setLabel(String label) {
		this.label = label;
	}

	
	/**
	 * Returns the positive facts of the query.
	 */
	public InMemoryAtomSet getPositiveAtomSet() {
		return this.positiveAtomSet;
	}
	
	/**
	 * Returns the negative facts of the query.
	 */
	public InMemoryAtomSet getNegativeAtomSet() {
		return this.negativeAtomSet;
	}
	
	/**
	 * Returns the answer variables of the query.
	 */
	public List<Term> getAnswerVariables() {
		return this.responseVariables;
	}

	
	public void setAnswerVariables(List<Term> v) {
		this.responseVariables = v;
	}

	public boolean isBoolean() {
		return responseVariables.isEmpty();
	}

	
	// /////////////////////////////////////////////////////////////////////////
	// OVERRIDE METHODS
	// /////////////////////////////////////////////////////////////////////////

	public CloseableIteratorWithoutException<Atom> positiveIterator() {
		return getPositiveAtomSet().iterator();
	}
	
	
	public CloseableIteratorWithoutException<Atom> negativeIterator() {
		return getNegativeAtomSet().iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.appendTo(sb);
		return sb.toString();
	}

	
	public void appendTo(StringBuilder sb) {
		sb.append("ANS(");
		boolean first = true;
		for (Term t : this.responseVariables) {
			if(!first) {
				sb.append(',');
			}
			first = false;
			sb.append(t);
		}

		sb.append(") : ");
		sb.append(this.positiveAtomSet);
		sb.append(", !" + this.negativeAtomSet);
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

	public CloseableIteratorWithoutException<Atom> iterator() {
		
		return null;
	}
}
