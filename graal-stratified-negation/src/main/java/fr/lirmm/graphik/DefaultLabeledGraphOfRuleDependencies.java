package fr.lirmm.graphik;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.google.errorprone.annotations.Var;

import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;


class DefaultLabeledGraphOfRuleDependencies implements GraphOfRuleDependencies {

  private final DirectedGraph<Rule, DefaultDirectedLabeledEdge> graph;

  private final Iterable<Rule> rules;

  private boolean computeCircuits;
  private List<List<Rule>> circuits;

  private boolean computeScc;
  private StronglyConnectedComponentsGraph<Rule> Scc;

  private int nbNodes;
  private int nbEdges;

  // /////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  // /////////////////////////////////////////////////////////////////////////

  public DefaultLabeledGraphOfRuleDependencies(File src) {
    this(readRules(src), true);
  }

  private DefaultLabeledGraphOfRuleDependencies(Iterable<Rule> rules, boolean computeDep) {

    this.graph = new DefaultDirectedGraph<>(DefaultDirectedLabeledEdge.class);

    this.rules = rules;

    this.nbNodes = 0;
    for (Rule r : rules) {
      graph.addVertex(r);
      this.nbNodes++;
    }

    this.nbEdges = 0;
    if (computeDep) {
      this.computeDependencies();

      this.computeCircuits = false;
      this.hasCircuit();

      this.computeScc = false;
      this.Scc = this.getStronglyConnectedComponentsGraph();
    }
  }

  // /////////////////////////////////////////////////////////////////////////
  // PRIVATE METHODS
  // /////////////////////////////////////////////////////////////////////////

  private static Iterable<Rule> readRules(File src) {
    KBBuilder kbb = new KBBuilder();

    /* Parsing Rules */
    try {
      InputStream ips = new FileInputStream(src);
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
      System.out.println(e.toString());
    }
    return kbb.build().getOntology();
  }

  // /////////////////////////////////////////////////////////////////////////
  // PUBLIC METHODS
  // /////////////////////////////////////////////////////////////////////////

  private void computeDependencies() {

    // Building index
    IndexedByBodyPredicateRuleSetWithNegation index =
        new IndexedByBodyPredicateRuleSetWithNegation(this.rules);

    int coeurs = Runtime.getRuntime().availableProcessors();

    ArrayList<ArrayList<Rule>> l = new ArrayList<>();
    for (int i = 0; i < coeurs; i++) {
      l.add(new ArrayList<>());
    }

    @Var
    int k = 0;
    for (Rule rule : rules) {
      l.get(k).add(rule);
      k = (k + 1) % coeurs;
    }

    try {
      ArrayList<ThreadDependency> threads = new ArrayList<>();
      for (int i = 0; i < coeurs; i++) {
        ThreadDependency t = new ThreadDependency(l.get(i), index, graph);
        threads.add(t);
        t.start();
      }
      for (int i = 0; i < coeurs; i++) {
        threads.get(i).join();
        this.nbEdges += threads.get(i).getNbDep();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean existUnifier(Rule src, Rule dest) {
    return (this.graph.getEdge(src, dest) != null);
  }

  @Override
  public Set<Substitution> getUnifiers(Rule src, Rule dest) {
    return null;
  }

  @Override
  public Set<Rule> getTriggeredRules(Rule src) {

    Set<Rule> set = new HashSet<>();

    for (DefaultDirectedLabeledEdge i : this.graph.outgoingEdgesOf(src)) {
      if (i.getLabel() == '+')
        set.add(this.graph.getEdgeTarget(i));
    }

    return set;
  }

  public Set<Rule> getInhibitedRules(Rule src) {
    Set<Rule> set = new HashSet<>();
    for (DefaultDirectedLabeledEdge i : this.graph.outgoingEdgesOf(src)) {
      if (i.getLabel() == '-')
        set.add(this.graph.getEdgeTarget(i));
    }
    return set;
  }

  @Override
  public Set<Pair<Rule, Substitution>> getTriggeredRulesWithUnifiers(Rule src) {
    return null;
  }

  @Override
  public GraphOfRuleDependencies getSubGraph(Iterable<Rule> ruleSet) {

    DefaultLabeledGraphOfRuleDependencies subGRD =
        new DefaultLabeledGraphOfRuleDependencies(ruleSet, false);

    for (Rule src : ruleSet) {
      for (Rule target : ruleSet) {
        if (this.graph.getEdge(src, target) != null) {

          subGRD.addDependency(src, target, this.graph.getEdge(src, target).getLabel());
        }
      }
    }

    hasCircuit();
    getStronglyConnectedComponentsGraph();
    return subGRD;
  }

  private void addDependency(Rule src, Rule target, char label) {
    graph.addEdge(src, target,
        new DefaultDirectedLabeledEdge(((DefaultRuleWithNegation) src).getIndice(),
            ((DefaultRuleWithNegation) target).getIndice(), label));
  }

  @Override
  public Iterable<Rule> getRules() {
    return this.rules;
  }

  @Override
  public StronglyConnectedComponentsGraph<Rule> getStronglyConnectedComponentsGraph() {
    if (!computeScc) {
      Scc = new StronglyConnectedComponentsGraph<>(this.graph);
      computeScc = true;
    }
    return Scc;
  }

  @Override
  public boolean hasCircuit() {
    if (!computeCircuits) {
      TarjanSimpleCycles<Rule, DefaultDirectedLabeledEdge> inspector =
          new TarjanSimpleCycles<>(this.graph);
      this.circuits = inspector.findSimpleCycles();
      computeCircuits = true;
    }
    return !circuits.isEmpty();
  }

  public boolean hasCircuitWithNegativeEdge() {

    if (!computeCircuits)
      hasCircuit();

    if (circuits.isEmpty())
      return false;

    for (List<Rule> c : circuits) {
      if (containsNegativeEdge(c)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsNegativeEdge(List<Rule> circuit) {
    for (int i = 0; i < circuit.size() - 1; i++) { // Following the circuit
      for (DefaultDirectedLabeledEdge e : this.graph.outgoingEdgesOf(circuit.get(i))) {
        if (e.getHead() == ((DefaultRuleWithNegation) circuit.get(i + 1)).getIndice()) { // Wanted
                                                                                         // edge
                                                                                         // found
          if (e.getLabel() == '-')
            return true;
          break;
        }
      }
    }
    int i = circuit.size() - 1;
    for (DefaultDirectedLabeledEdge e : this.graph.outgoingEdgesOf(circuit.get(i))) {
      if (e.getHead() == ((DefaultRuleWithNegation) circuit.get(0)).getIndice()) { // Wanted edge
                                                                                   // found
        if (e.getLabel() == '-')
          return true;
        break;
      }
    }
    return false;
  }

  public Iterable<DefaultDirectedLabeledEdge> getBadEdges(List<Rule> c) {
    ArrayList<DefaultDirectedLabeledEdge> l = new ArrayList<>();
    for (int i = 0; i < c.size() - 1; i++) { // Following the circuit
      for (DefaultDirectedLabeledEdge e : this.graph.outgoingEdgesOf(c.get(i))) {
        if (e.getHead() == ((DefaultRuleWithNegation) c.get(i + 1)).getIndice()) { // Wanted edge
                                                                                   // found
          l.add(e);
          break;
        }
      }
    }

    int i = c.size() - 1;
    for (DefaultDirectedLabeledEdge e : this.graph.outgoingEdgesOf(c.get(i))) {
      if (e.getHead() == ((DefaultRuleWithNegation) c.get(0)).getIndice()) { // Wanted edge found
        l.add(e);
        break;
      }
    }
    return l;
  }

  public ArrayList<List<Rule>> getBadCircuits() {
    ArrayList<List<Rule>> l = new ArrayList<>();

    if (!hasCircuit())
      return l;

    if (!hasCircuitWithNegativeEdge())
      return l;

    for (List<Rule> c : this.circuits) {
      if (containsNegativeEdge(c)) {
        l.add(c);
      }
    }
    return l;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("Rules :\n");
    for (Rule r : this.rules)
      s.append(r.toString());
    s.append("\n");
    return s.append(graph.toString()).toString();
  }

  public int getNodeCount() {
    return this.nbNodes;
  }

  public int getEdgeCount() {
    return this.nbEdges;
  }
}
