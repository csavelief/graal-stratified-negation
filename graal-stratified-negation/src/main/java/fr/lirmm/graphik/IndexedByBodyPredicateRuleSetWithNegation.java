package fr.lirmm.graphik;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

class IndexedByBodyPredicateRuleSetWithNegation extends LinkedListRuleSet{

	private final TreeMap<Predicate, RuleSet> map;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	public IndexedByBodyPredicateRuleSetWithNegation(Iterable<Rule> rules) {
		super();
		this.map = new TreeMap<>();
		for (Rule r : rules) {
			this.add(r);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// SPECIFIC METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	public Iterable<Rule> getRulesByPredicates(Iterable<Predicate> predicates)
	{
		ArrayList<Rule> res = new ArrayList<>();
		
		for(Predicate p : predicates)
		{
			RuleSet rules = map.get(p);
			if(rules != null)
			{
				for(Rule r : map.get(p))
				{
					res.add(r);
				}
			}
		}
		
		return res;
	}

	// /////////////////////////////////////////////////////////////////////////
	// OVERRIDE METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public boolean add(Rule rule) {
		//super.add(rule);
		CloseableIteratorWithoutException<Atom> it = rule.getBody().iterator();
		while (it.hasNext()) {
			add(it.next().getPredicate(), rule);
		}
		
		it = ((DefaultRuleWithNegation)rule).getNegativeBody().iterator();
		while (it.hasNext()) {
			add(it.next().getPredicate(), rule);
		}
		it.close();
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Rule> c) {
		//boolean res = super.addAll(c);
		for (Rule rule : c) {
			this.add(rule);
		}
		return true;
	}

	@Override
	public boolean remove(Rule rule) {
		boolean res = super.remove(rule);
		CloseableIteratorWithoutException<Atom> it = rule.getBody().iterator();
		while (it.hasNext()) {
			Atom a = it.next();
			remove(a.getPredicate(), rule);
		}
		return res;
	}

	@Override
	public void clear() {
		super.clear();
		this.map.clear();
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Rule) {
			return this.remove((Rule) o);
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for (Object o : c) {
			res = this.remove(o) || res;
		}
		return res;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean res = super.retainAll(c);
		this.map.clear();
		this.addAll(this);
		return res;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private void add(Predicate p, Rule r) {
		RuleSet rules = this.map.get(p);
		if (rules == null) {
			rules = new LinkedListRuleSet();
			this.map.put(p, rules);
		}
		rules.add(r);
	}

	private void remove(Predicate p, Rule r) {
		RuleSet rules = this.map.get(p);
		if (rules != null) {
			rules.remove(r);
		}
	}
}
