package fr.lirmm.graphik;

import java.util.ArrayList;

import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismWithCompilation;
import fr.lirmm.graphik.graal.homomorphism.SmartHomomorphism;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.util.profiler.AbstractProfilable;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorAdapter;
import fr.lirmm.graphik.util.stream.IteratorException;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Atom;


public class HomomorphismWithNegation extends AbstractProfilable implements HomomorphismWithCompilation<Object, AtomSet>{

	private static HomomorphismWithNegation instance;
	
	
	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////
		
		
	public HomomorphismWithNegation() {
	
	}
	
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	public static synchronized HomomorphismWithNegation instance()
	{
		if(instance == null)
			instance = new HomomorphismWithNegation();
		
		return instance;
	}
	
	
	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	private boolean verifSub(Substitution sub , AtomSet negPart , AtomSet factBase) throws AtomSetException {
		
		LinkedListAtomSet res = new LinkedListAtomSet();
		
		sub.apply(negPart , res);
		
		
		for(Predicate p : res.getPredicates()) {
			
			CloseableIteratorWithoutException<Atom> itAtom = res.atomsByPredicate(p);
			
			for( ; itAtom.hasNext() ; ) {
				
				Atom a = itAtom.next();
				if(factBase.contains(a))
					return false;
			}
			
			itAtom.close();
		}
		
		return true;
	}

	
	

	public boolean exist(Object q, AtomSet a, RulesCompilation compilation)
			throws HomomorphismException {
		
		return exist(q , a);
	}


	public boolean exist(Object q, AtomSet a) throws HomomorphismException {
		try {
			CloseableIterator<Substitution> l = SmartHomomorphism.instance().execute(new DefaultConjunctiveQuery(((DefaultConjunctiveQueryWithNegation)q).getPositiveAtomSet()), a);
			
			for( ; l.hasNext() ; )
			{
				Substitution s = l.next();
				
				if(verifSub(s, ((DefaultConjunctiveQueryWithNegation)q).getNegativeAtomSet() , a))
					return true;
			}
			
			l.close();
			
		} catch (HomomorphismException e) {
			e.printStackTrace();
		} catch (IteratorException e) {
			e.printStackTrace();
		} catch (AtomSetException e) {
			e.printStackTrace();
		}
		
		return false;
	}


	public CloseableIterator<Substitution> execute(Object q, AtomSet a)
			throws HomomorphismException {
		
		
		ArrayList<Substitution> liste = new ArrayList<Substitution>();
		
		try {
			CloseableIterator<Substitution> l =SmartHomomorphism.instance().execute(new DefaultConjunctiveQuery(((DefaultConjunctiveQueryWithNegation)q).getPositiveAtomSet()), a);
			
			for( ; l.hasNext() ; )
			{
				Substitution s = l.next();
				
				if(verifSub(s, ((DefaultConjunctiveQueryWithNegation)q).getNegativeAtomSet() , a))
					liste.add(s);
			}
			
			l.close();
			
		} catch (HomomorphismException e) {
			e.printStackTrace();
		} catch (IteratorException e) {
			e.printStackTrace();
		} catch (AtomSetException e) {
			e.printStackTrace();
		}
		
		return new CloseableIteratorAdapter<Substitution>(liste.iterator());
	}


	public CloseableIterator<Substitution> execute(Object q, AtomSet a,
			RulesCompilation compilation) throws HomomorphismException {
		
		return execute(q , a);
	}



}
