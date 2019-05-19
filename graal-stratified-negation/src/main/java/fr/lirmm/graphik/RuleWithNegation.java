package fr.lirmm.graphik;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.util.string.AppendableToStringBuilder;

interface RuleWithNegation extends Comparable<Rule>, AppendableToStringBuilder, Rule {

  /**
   * Get the negative body (the hypothesis) of this rule.
   * 
   * @return the body of this rule.
   */
  InMemoryAtomSet getNegativeBody();
}
