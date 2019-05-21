package fr.lirmm.graphik;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.google.common.base.Throwables;
import com.google.errorprone.annotations.Var;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.io.ParseException;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.forward_chaining.SccChase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

class Utils {

  private static int i = -1;

  public static RuleWithNegation parseRule(String s) throws ParseException {

    LinkedListAtomSet posBody = new LinkedListAtomSet();
    LinkedListAtomSet negBody = new LinkedListAtomSet();

    Rule r = DlgpParser.parseRule(s);

    i++;

    for (Predicate itPred : r.getBody().getPredicates()) {
      try (CloseableIteratorWithoutException<Atom> itAtom = r.getBody().atomsByPredicate(itPred)) {
        for (; itAtom.hasNext();) {
          Atom a = itAtom.next();
          if (!a.getPredicate().toString().startsWith("not_")) {
            posBody.add(a);
          } else {
            Predicate p =
                new Predicate(a.getPredicate().getIdentifier().toString().replaceAll("not_", ""),
                    a.getPredicate().getArity());
            a.setPredicate(p);
            negBody.add(a);
          }
        }
      }
    }
    return new RuleWithNegation(i + "", posBody, negBody, r.getHead());
  }

  public static KBBuilder readKB(KBBuilder kbb, String fileRules, String fileFacts) {

    // Parsing Rules
    if (fileRules != null) {

      System.out.println("Rules : parsing of '" + fileRules + "'");

      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(new FileInputStream(fileRules), UTF_8))) {

        @Var
        String row;

        while ((row = br.readLine()) != null) {
          if (row.length() > 0 && row.charAt(0) != '%') {
            kbb.add(parseRule(row));
          }
        }
      } catch (Exception e) {
        Throwables.getRootCause(e).printStackTrace();
      }
    }

    // Parsing Facts
    if (fileFacts != null) {

      System.out.println("Facts : parsing of '" + fileFacts + "'");

      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(new FileInputStream(fileFacts), UTF_8))) {

        @Var
        String row;

        while ((row = br.readLine()) != null) {
          if (row.length() > 0 && row.charAt(0) != '%') {
            kbb.add(DlgpParser.parseAtom(row));
          }
        }
      } catch (Exception e) {
        Throwables.getRootCause(e).printStackTrace();
      }
    }
    return kbb;
  }

  public static String displayFacts(AtomSet facts) {
    StringBuilder sb = new StringBuilder("== Saturation ==\n");
    try (CloseableIterator<Atom> itAtom = facts.iterator()) {
      while (itAtom.hasNext()) {
        sb.append(itAtom.next().toString());
        sb.append(".\n");
      }
    } catch (IteratorException e) {
      Throwables.getRootCause(e).printStackTrace();
    }
    return sb.toString();
  }

  public static String getSaturationFromFile(String src, LabeledGraphOfRuleDependencies grd) {

    KBBuilder kbb = new KBBuilder();
    Utils.readKB(kbb, null, src);
    KnowledgeBase kb = kbb.build();
    SccChase<AtomSet> chase = new SccChase<>(grd, kb.getFacts());

    try {
      chase.execute();
    } catch (ChaseException e) {
      Throwables.getRootCause(e).printStackTrace();
    }
    return Utils.displayFacts(kb.getFacts());
  }

  public static String getRulesText(Iterable<Rule> rules) {
    StringBuilder sb = new StringBuilder("====== RULE SET ======\n");
    for (Rule r : rules) {
      sb.append(r.toString());
      sb.append('\n');
    }
    return sb.toString();
  }

  public static String getGRDText(LabeledGraphOfRuleDependencies grd) {
    StringBuilder sb = new StringBuilder("======== GRD =========\n");
    for (Rule r1 : grd.getRules()) {
      for (Rule r2 : grd.getTriggeredRules(r1)) {
        sb.append("[");
        sb.append(r1.getLabel());
        sb.append("] ={+}=> [");
        sb.append(r2.getLabel());
        sb.append("]\n");
      }
      for (Rule r2 : grd.getInhibitedRules(r1)) {
        sb.append("[");
        sb.append(r1.getLabel());
        sb.append("] ={-}=> [");
        sb.append(r2.getLabel());
        sb.append("]\n");
      }
    }
    return sb.toString();
  }

  public static String getSCCText(StronglyConnectedComponentsGraph<Rule> scc) {
    StringBuilder sb = new StringBuilder("======== SCC =========\n");
    for (int i = 0; i < scc.getNbrComponents(); i++) {
      @Var
      boolean first = true;
      sb.append("C");
      sb.append(i);
      sb.append(" = {");
      for (Rule r : scc.getComponent(i)) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(r.getLabel());
      }
      sb.append("}\n");
    }
    return sb.toString();
  }
}
