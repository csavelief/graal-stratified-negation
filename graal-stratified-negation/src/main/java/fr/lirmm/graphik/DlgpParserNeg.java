package fr.lirmm.graphik;

import java.util.List;
import java.util.LinkedList;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.io.ParseException;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

public class DlgpParserNeg {
	
	private static int i = -1;
	
	public static RuleWithNegation parseRule(String s) throws ParseException {
		
		LinkedListAtomSet posBody = new LinkedListAtomSet() ;
		LinkedListAtomSet negBody = new LinkedListAtomSet();

		Rule r = DlgpParser.parseRule(s);
		
		i++;

		for(Predicate itPred : r.getBody().getPredicates()) {
			
			CloseableIteratorWithoutException<Atom> itAtom = r.getBody().atomsByPredicate(itPred);
			for( ; itAtom.hasNext() ; ) {
				Atom a = itAtom.next();
				
				if(a.getPredicate().toString().startsWith("not_")) {
					
					Predicate p = new Predicate(a.getPredicate().getIdentifier().toString().replaceAll("not_", "") , a.getPredicate().getArity());	
					a.setPredicate(p);
					negBody.add(a);
				}
				else {
					
					posBody.add(a);
				}
			}
			itAtom.close();
		}
		
		return new DefaultRuleWithNegation(i + "", posBody, negBody, r.getHead());
	}
	
public static ConjunctiveQueryWithNegation parseQuery(String s) throws ParseException {
		
		LinkedListAtomSet posBody = new LinkedListAtomSet() ;
		LinkedListAtomSet negBody = new LinkedListAtomSet();
		ConjunctiveQuery q = DlgpParser.parseQuery(s);
		List<Term>  ans = q.getAnswerVariables();
		
		for(Predicate itPred : q.getAtomSet().getPredicates())
		{
			
			CloseableIteratorWithoutException<Atom> itAtom = q.getAtomSet().atomsByPredicate(itPred);
			for( ; itAtom.hasNext() ; ) {
				Atom a = itAtom.next();
				
				if(a.getPredicate().toString().startsWith("not_")){
					
					Predicate p = new Predicate(a.getPredicate().getIdentifier().toString().replaceAll("not_", "") , a.getPredicate().getArity());	
					a.setPredicate(p);
					negBody.add(a);
				}
				else {
					
					posBody.add(a);
				}
			}
			itAtom.close();
		}
		
		return new DefaultConjunctiveQueryWithNegation(posBody, negBody, ans);
	}
}
