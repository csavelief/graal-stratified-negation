package fr.lirmm.graphik;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import com.google.errorprone.annotations.Var;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

class IndexedByBodyPredicateRuleSetWithNegation extends LinkedListRuleSet {

  private final TreeMap<Predicate, RuleSet> map_;

  public IndexedByBodyPredicateRuleSetWithNegation(Iterable<Rule> rules) {

    super();

    map_ = new TreeMap<>();

    for (Rule r : rules) {
      add(r);
    }
  }

  public Iterable<Rule> getRulesByPredicates(Iterable<Predicate> predicates) {

    ArrayList<Rule> res = new ArrayList<>();

    for (Predicate p : predicates) {
      RuleSet rules = map_.get(p);
      if (rules != null) {
        for (Rule r : map_.get(p)) {
          res.add(r);
        }
      }
    }
    return res;
  }

  @Override
  public boolean add(Rule rule) {
    try (CloseableIteratorWithoutException<Atom> it = rule.getBody().iterator()) {
      while (it.hasNext()) {
        add(it.next().getPredicate(), rule);
      }
    }
    try (CloseableIteratorWithoutException<Atom> it =
        ((RuleWithNegation) rule).getNegativeBody().iterator()) {
      while (it.hasNext()) {
        add(it.next().getPredicate(), rule);
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends Rule> c) {
    for (Rule rule : c) {
      add(rule);
    }
    return true;
  }

  @Override
  public boolean remove(Rule rule) {
    boolean res = super.remove(rule);
    try (CloseableIteratorWithoutException<Atom> it = rule.getBody().iterator()) {
      while (it.hasNext()) {
        Atom a = it.next();
        remove(a.getPredicate(), rule);
      }
    }
    return res;
  }

  @Override
  public void clear() {
    super.clear();
    map_.clear();
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Rule) {
      return remove((Rule) o);
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    @Var
    boolean res = false;
    for (Object o : c) {
      res = remove(o) || res;
    }
    return res;
  }

  @SuppressWarnings("ModifyingCollectionWithItself")
  @Override
  public boolean retainAll(Collection<?> c) {
    boolean res = super.retainAll(c);
    map_.clear();
    addAll(this);
    return res;
  }

  private void add(Predicate p, Rule r) {
    @Var
    RuleSet rules = map_.get(p);
    if (rules == null) {
      rules = new LinkedListRuleSet();
      map_.put(p, rules);
    }
    rules.add(r);
  }

  private void remove(Predicate p, Rule r) {
    RuleSet rules = map_.get(p);
    if (rules != null) {
      rules.remove(r);
    }
  }
}
