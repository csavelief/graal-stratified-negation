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

	private InMemoryAtomSet positiveAtomSet;
	private InMemoryAtomSet negativeAtomSet;
	private List<Term> responseVariables;
	private String label;
	
	
	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	
	public DefaultConjunctiveQueryWithNegation() {
		
		this.positiveAtomSet = DefaultAtomSetFactory.instance().create();
		this.negativeAtomSet = DefaultAtomSetFactory.instance().create();
		this.responseVariables = new LinkedList<>();
		this.label = "";
	}
	
	
	public DefaultConjunctiveQueryWithNegation(InMemoryAtomSet positiveAtomSet , InMemoryAtomSet negativeAtomSet) {
		
		this.positiveAtomSet = positiveAtomSet;
		this.negativeAtomSet = negativeAtomSet;
		this.responseVariables = new LinkedList<>(positiveAtomSet.getVariables()); // Condition de Safety ?
		this.label = "";
	}
	
	
	public DefaultConjunctiveQueryWithNegation(InMemoryAtomSet positiveAtomSet, InMemoryAtomSet negagtiveAtomSet , List<Term> ans) {
		this("", positiveAtomSet, negagtiveAtomSet, ans);
	}
	

	/**
	 * 
	 * @param label
	 *            the name of this query
	 * @param atomSet
	 *            the conjunction of atom representing the query
	 * @param ans
	 *            the list of answer variables
	 */
	public DefaultConjunctiveQueryWithNegation(String label, InMemoryAtomSet positiveAtomSet, InMemoryAtomSet negativeAtomSet, List<Term> ans) {
		
		this.positiveAtomSet = positiveAtomSet;
		this.negativeAtomSet = negativeAtomSet;
		this.responseVariables = ans;
		this.label = label;
	}

	// copy constructor
	public DefaultConjunctiveQueryWithNegation(ConjunctiveQueryWithNegation query) {
		
		this.positiveAtomSet = DefaultAtomSetFactory.instance().create(query.getPositiveAtomSet());
		this.negativeAtomSet = DefaultAtomSetFactory.instance().create(query.getNegativeAtomSet());
		this.responseVariables = new LinkedList<>(query.getAnswerVariables());
		this.label = query.getLabel();
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

	
	public void setPositiveAtomSet(InMemoryAtomSet positiveAtomSet) {
		this.positiveAtomSet = positiveAtomSet;
	}
	
	
	/**
	 * Returns the negative facts of the query.
	 */
	public InMemoryAtomSet getNegativeAtomSet() {
		return this.negativeAtomSet;
	}

	
	public void setNegativeAtomSet(InMemoryAtomSet negativeAtomSet) {
		this.negativeAtomSet = negativeAtomSet;
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

	
	public boolean equals(ConjunctiveQueryWithNegation other) {
		return this.getAnswerVariables().equals(other.getAnswerVariables())
		       && this.getPositiveAtomSet().equals(other.getPositiveAtomSet())
		       && this.getNegativeAtomSet().equals(other.getNegativeAtomSet());
	}

	public CloseableIteratorWithoutException<Atom> iterator() {
		
		return null;
	}
}
