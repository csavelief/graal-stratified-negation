package fr.lirmm.graphik;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.errorprone.annotations.Var;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.forward_chaining.SccChase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

class Utils {

  public static KBBuilder readKB(KBBuilder kbb, String fileRules, String fileFacts) {

    /* Parsing Rules */
    if (fileRules != null) {
      System.out.println("Rules : parsing of '" + fileRules + "'");
      try {
        InputStream ips = new FileInputStream(fileRules);
        InputStreamReader ipsr = new InputStreamReader(ips, UTF_8);
        BufferedReader br = new BufferedReader(ipsr);
        @Var
        String ligne;

        while ((ligne = br.readLine()) != null) {
          if (ligne.charAt(0) != '%')
            kbb.add(DlgpParserNeg.parseRule(ligne));
        }

        br.close();
        ipsr.close();
        ips.close();

      } catch (Exception e) {
        System.out.println("Caca" + e.toString());
        e.printStackTrace();
      }
    }

    /* Parsing Facts */

    if (fileFacts != null) {
      System.out.println("Facts : parsing of '" + fileFacts + "'");
      try {
        InputStream ips = new FileInputStream(fileFacts);
        InputStreamReader ipsr = new InputStreamReader(ips, UTF_8);
        BufferedReader br = new BufferedReader(ipsr);
        @Var
        String ligne;

        while ((ligne = br.readLine()) != null) {
          if (ligne.charAt(0) != '%')
            kbb.add(DlgpParser.parseAtom(ligne));
        }

        br.close();
        ipsr.close();
        ips.close();

      } catch (Exception e) {
        System.out.println(e.toString());
      }
    }

    return kbb;
  }

  public static String displayFacts(AtomSet facts) {
    StringBuilder s = new StringBuilder("== Saturation ==\n");

    try {
      for (CloseableIterator<Atom> itAtom = facts.iterator(); itAtom.hasNext();) {
        s.append(itAtom.next().toString());
        s.append(".\n");
      }
    } catch (IteratorException e) {
      e.printStackTrace();
    }

    return s.toString();
  }

  public static String getSaturationFromFile(String src,
      DefaultLabeledGraphOfRuleDependencies grd) {
    KBBuilder kbb = new KBBuilder();
    Utils.readKB(kbb, null, src);

    KnowledgeBase kb = kbb.build();

    SccChase<AtomSet> chase = new SccChase<>(grd, kb.getFacts());
    try {
      chase.execute();
    } catch (ChaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return Utils.displayFacts(kb.getFacts());
  }

  public static String getRulesText(Iterable<Rule> rules) {
    StringBuilder s = new StringBuilder("====== RULE SET ======\n");
    for (Rule r : rules) {
      s.append(r.toString());
      s.append('\n');
    }
    return s.toString();
  }

  public static String getGRDText(DefaultLabeledGraphOfRuleDependencies grd) {
    StringBuilder s = new StringBuilder("======== GRD =========\n");
    for (Rule r1 : grd.getRules()) {
      for (Rule r2 : grd.getTriggeredRules(r1)) {
        s.append("[");
        s.append(r1.getLabel());
        s.append("] ={+}=> [");
        s.append(r2.getLabel());
        s.append("]\n");
      }
      for (Rule r2 : grd.getInhibitedRules(r1)) {
        s.append("[");
        s.append(r1.getLabel());
        s.append("] ={-}=> [");
        s.append(r2.getLabel());
        s.append("]\n");
      }
    }
    return s.toString();
  }

  public static String getSCCText(StronglyConnectedComponentsGraph<Rule> scc) {
    StringBuilder s = new StringBuilder("======== SCC =========\n");
    for (int i = 0; i < scc.getNbrComponents(); i++) {
      @Var
      boolean first = true;
      s.append("C");
      s.append(i);
      s.append(" = {");
      for (Rule r : scc.getComponent(i)) {
        if (first)
          first = false;
        else
          s.append(", ");
        s.append(r.getLabel());
      }
      s.append("}\n");
    }
    return s.toString();
  }
}
