package fr.lirmm.graphik;

import java.util.ArrayList;
import com.google.common.base.Throwables;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismWithCompilation;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.homomorphism.SmartHomomorphism;
import fr.lirmm.graphik.util.profiler.AbstractProfilable;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorAdapter;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

class HomomorphismWithNegation extends AbstractProfilable
    implements HomomorphismWithCompilation<Object, AtomSet> {

  private static HomomorphismWithNegation instance_;

  private HomomorphismWithNegation() {

  }

  public static synchronized HomomorphismWithNegation instance() {
    if (instance_ == null) {
      instance_ = new HomomorphismWithNegation();
    }
    return instance_;
  }

  private boolean verifSub(Substitution sub, AtomSet negPart, AtomSet factBase)
      throws AtomSetException {

    LinkedListAtomSet res = new LinkedListAtomSet();
    sub.apply(negPart, res);

    for (Predicate p : res.getPredicates()) {
      try (CloseableIteratorWithoutException<Atom> itAtom = res.atomsByPredicate(p)) {
        while (itAtom.hasNext()) {
          Atom a = itAtom.next();
          if (factBase.contains(a)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean exist(Object q, AtomSet a, RulesCompilation compilation) {
    return exist(q, a);
  }

  @Override
  public boolean exist(Object q, AtomSet a) {
    try (CloseableIterator<Substitution> l = SmartHomomorphism.instance().execute(
        new DefaultConjunctiveQuery(((DefaultConjunctiveQueryWithNegation) q).getPositiveAtomSet()),
        a)) {
      while (l.hasNext()) {
        Substitution s = l.next();
        if (verifSub(s, ((DefaultConjunctiveQueryWithNegation) q).getNegativeAtomSet(), a)) {
          return true;
        }
      }
    } catch (HomomorphismException | IteratorException | AtomSetException e) {
      Throwables.getRootCause(e).printStackTrace();
    }
    return false;
  }

  @Override
  public CloseableIterator<Substitution> execute(Object q, AtomSet a) {

    ArrayList<Substitution> list = new ArrayList<>();

    try (CloseableIterator<Substitution> l = SmartHomomorphism.instance().execute(
        new DefaultConjunctiveQuery(((DefaultConjunctiveQueryWithNegation) q).getPositiveAtomSet()),
        a)) {
      while (l.hasNext()) {
        Substitution s = l.next();
        if (verifSub(s, ((DefaultConjunctiveQueryWithNegation) q).getNegativeAtomSet(), a)) {
          list.add(s);
        }
      }
    } catch (HomomorphismException | IteratorException | AtomSetException e) {
      Throwables.getRootCause(e).printStackTrace();
    }
    return new CloseableIteratorAdapter<>(list.iterator());
  }

  @Override
  public CloseableIterator<Substitution> execute(Object q, AtomSet a,
      RulesCompilation compilation) {
    return execute(q, a);
  }
}
