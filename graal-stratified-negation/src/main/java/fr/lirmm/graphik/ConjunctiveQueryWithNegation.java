package fr.lirmm.graphik;

import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.util.stream.CloseableIterableWithoutException;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

interface ConjunctiveQueryWithNegation extends Query, CloseableIterableWithoutException<Atom> {

	/**
	 * The label (the name) for this query.
	 * 
	 * @return the label of this query.
	 */
	String getLabel();
	
	
	/**
	 * Get the atom conjunction representing the query.
	 * @return an atom set representing the atom conjunction of the query.
	 */
	InMemoryAtomSet getPositiveAtomSet();
	
	
	/**
	 * Get the negative atom conjunction representing the query.
	 * @return an atom set representing the atom conjunction of the query.
	 */
	InMemoryAtomSet getNegativeAtomSet();

	
	/**
	 * Get the answer variables
	 * @return an Collection of Term representing the answer variables.
	 */
	List<Term> getAnswerVariables();

	
	void setAnswerVariables(List<Term> ans);

	
	/**
	 * Return an iterator over the positive atoms conjunction of the query.
	 */
	CloseableIteratorWithoutException<Atom> positiveIterator();
	
	
	/**
	 * Return an iterator over the negative atoms conjunction of the query.
	 */
	CloseableIteratorWithoutException<Atom> negativeIterator();
}
