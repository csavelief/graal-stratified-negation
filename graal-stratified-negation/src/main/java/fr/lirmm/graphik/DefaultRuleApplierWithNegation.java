package fr.lirmm.graphik;

import java.util.LinkedList;

import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.forward_chaining.rule_applier.AbstractRuleApplier;

public class DefaultRuleApplierWithNegation<T extends AtomSet> extends AbstractRuleApplier<T> {

	
	public DefaultRuleApplierWithNegation(){
		this(HomomorphismWithNegation.instance());
	}

	public DefaultRuleApplierWithNegation(Homomorphism<? super ConjunctiveQuery, ? super T> homomorphismSolver) {
			super(homomorphismSolver);
		}
	
	
	@Override
	protected ConjunctiveQuery generateQuery(Rule rule) {
		LinkedList<Term> ans = new LinkedList<>(rule.getFrontier());
		return new DefaultConjunctiveQueryWithNegation(rule.getBody() ,
				((DefaultRuleWithNegation)rule).getNegativeBody() ,
				ans);
	}

}
