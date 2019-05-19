package fr.lirmm.graphik;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import com.google.common.base.Throwables;
import com.google.errorprone.annotations.Var;
import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;

class DefaultLabeledGraphOfRuleDependencies implements GraphOfRuleDependencies {

  private final DirectedGraph<Rule, DefaultDirectedLabeledEdge> graph_;
  private final Iterable<Rule> rules_;

  private boolean computeCircuits_;
  private List<List<Rule>> circuits_;
  private boolean computeScc_;
  private StronglyConnectedComponentsGraph<Rule> scc_;

  public DefaultLabeledGraphOfRuleDependencies(File src) {
    this(readRules(src), true);
  }

  private DefaultLabeledGraphOfRuleDependencies(Iterable<Rule> rules, boolean computeDep) {

    this.graph_ = new DefaultDirectedGraph<>(DefaultDirectedLabeledEdge.class);
    this.rules_ = rules;

    for (Rule r : rules) {
      graph_.addVertex(r);
    }

    if (computeDep) {
      this.computeDependencies();
      this.computeCircuits_ = false;
      this.hasCircuit();
      this.computeScc_ = false;
      this.scc_ = this.getStronglyConnectedComponentsGraph();
    }
  }

  private static Iterable<Rule> readRules(File src) {

    KBBuilder kbb = new KBBuilder();

    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(new FileInputStream(src), UTF_8))) {

      @Var
      String ligne;

      while ((ligne = br.readLine()) != null) {
        if (ligne.charAt(0) != '%')
          kbb.add(DlgpParserNeg.parseRule(ligne));
      }
    } catch (Exception e) {
      Throwables.getRootCause(e).printStackTrace();
    }
    return kbb.build().getOntology();
  }

  private void computeDependencies() {

    IndexedByBodyPredicateRuleSetWithNegation index =
        new IndexedByBodyPredicateRuleSetWithNegation(this.rules_);
    int cores = Runtime.getRuntime().availableProcessors();

    ArrayList<ArrayList<Rule>> l = new ArrayList<>();
    for (int i = 0; i < cores; i++) {
      l.add(new ArrayList<>());
    }

    @Var
    int k = 0;
    for (Rule rule : rules_) {
      l.get(k).add(rule);
      k = (k + 1) % cores;
    }

    try {

      List<ThreadDependency> threads = new ArrayList<>();

      for (int i = 0; i < cores; i++) {
        ThreadDependency thread = new ThreadDependency(l.get(i), index, graph_);
        threads.add(thread);
        thread.start();
      }

      for (int i = 0; i < cores; i++) {
        threads.get(i).join();
      }
    } catch (InterruptedException e) {
      Throwables.getRootCause(e).printStackTrace();
    }
  }

  @Override
  public boolean existUnifier(Rule src, Rule dest) {
    return (this.graph_.getEdge(src, dest) != null);
  }

  @Override
  public Set<Substitution> getUnifiers(Rule src, Rule dest) {
    return null;
  }

  @Override
  public Set<Rule> getTriggeredRules(Rule src) {
    Set<Rule> set = new HashSet<>();
    for (DefaultDirectedLabeledEdge i : this.graph_.outgoingEdgesOf(src)) {
      if (i.getLabel() == '+') {
        set.add(this.graph_.getEdgeTarget(i));
      }
    }
    return set;
  }

  public Set<Rule> getInhibitedRules(Rule src) {
    Set<Rule> set = new HashSet<>();
    for (DefaultDirectedLabeledEdge i : this.graph_.outgoingEdgesOf(src)) {
      if (i.getLabel() == '-') {
        set.add(this.graph_.getEdgeTarget(i));
      }
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
        if (this.graph_.getEdge(src, target) != null) {
          subGRD.addDependency(src, target, this.graph_.getEdge(src, target).getLabel());
        }
      }
    }

    hasCircuit();
    getStronglyConnectedComponentsGraph();
    return subGRD;
  }

  private void addDependency(Rule src, Rule target, char label) {
    graph_.addEdge(src, target,
        new DefaultDirectedLabeledEdge(((DefaultRuleWithNegation) src).getIndice(),
            ((DefaultRuleWithNegation) target).getIndice(), label));
  }

  @Override
  public Iterable<Rule> getRules() {
    return this.rules_;
  }

  @Override
  public StronglyConnectedComponentsGraph<Rule> getStronglyConnectedComponentsGraph() {
    if (!computeScc_) {
      scc_ = new StronglyConnectedComponentsGraph<>(this.graph_);
      computeScc_ = true;
    }
    return scc_;
  }

  @Override
  public boolean hasCircuit() {
    if (!computeCircuits_) {
      TarjanSimpleCycles<Rule, DefaultDirectedLabeledEdge> inspector =
          new TarjanSimpleCycles<>(this.graph_);
      this.circuits_ = inspector.findSimpleCycles();
      computeCircuits_ = true;
    }
    return !circuits_.isEmpty();
  }

  public boolean hasCircuitWithNegativeEdge() {

    if (!computeCircuits_) {
      hasCircuit();
    }
    if (circuits_.isEmpty()) {
      return false;
    }

    for (List<Rule> c : circuits_) {
      if (containsNegativeEdge(c)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsNegativeEdge(List<Rule> circuit) {
    for (int i = 0; i < circuit.size() - 1; i++) { // Following the circuit
      for (DefaultDirectedLabeledEdge e : this.graph_.outgoingEdgesOf(circuit.get(i))) {

        // Wanted edge found
        if (e.getHead() == ((DefaultRuleWithNegation) circuit.get(i + 1)).getIndice()) {
          if (e.getLabel() == '-') {
            return true;
          }
          break;
        }
      }
    }

    int i = circuit.size() - 1;

    for (DefaultDirectedLabeledEdge e : this.graph_.outgoingEdgesOf(circuit.get(i))) {

      // Wanted edge found
      if (e.getHead() == ((DefaultRuleWithNegation) circuit.get(0)).getIndice()) {
        if (e.getLabel() == '-') {
          return true;
        }
        break;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Rules :\n");
    for (Rule r : this.rules_) {
      sb.append(r.toString());
    }
    sb.append("\n");
    return sb.append(graph_.toString()).toString();
  }

  class ThreadDependency extends Thread {

    private final ArrayList<Rule> src_;
    private final IndexedByBodyPredicateRuleSetWithNegation index_;
    private final DirectedGraph<Rule, DefaultDirectedLabeledEdge> graph_;

    private int nbDep;

    public ThreadDependency(ArrayList<Rule> src, IndexedByBodyPredicateRuleSetWithNegation index,
        DirectedGraph<Rule, DefaultDirectedLabeledEdge> graph) {
      this.src_ = src;
      this.index_ = index;
      this.graph_ = graph;
      this.nbDep = 0;
    }

    @Override
    public void run() {
      for (Rule r1 : src_) {
        Iterable<Rule> candidates = index_.getRulesByPredicates(r1.getHead().getPredicates());
        if (candidates != null) {
          for (Rule r2 : candidates) {
            synchronized (this.graph_) {
              if (!graph_.containsEdge(r1, r2)) {

                // Negative Dependency
                if (DefaultUnifierWithNegationAlgorithm.instance().existNegativeDependency(
                    (DefaultRuleWithNegation) r1, (DefaultRuleWithNegation) r2)) {
                  addEdge(r1, r2, '-');
                }
                // Positive Dependency
                else if (DefaultUnifierWithNegationAlgorithm.instance().existPositiveDependency(
                    (DefaultRuleWithNegation) r1, (DefaultRuleWithNegation) r2)) {
                  addEdge(r1, r2, '+');
                }
              }
            }
          }
        }
      }
    }

    private void addEdge(Rule r1, Rule r2, char label) {
      synchronized (this.graph_) {
        this.nbDep++;
        graph_.addEdge(r1, r2,
            new DefaultDirectedLabeledEdge(((DefaultRuleWithNegation) r1).getIndice(),
                ((DefaultRuleWithNegation) r2).getIndice(), label));
      }
    }

    public int getNbDep() {
      return this.nbDep;
    }
  }
}
