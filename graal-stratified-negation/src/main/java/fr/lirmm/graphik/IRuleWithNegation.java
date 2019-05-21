package fr.lirmm.graphik;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;

interface IRuleWithNegation extends Rule {

  /**
   * Get the negative body (the hypothesis) of this rule.
   * 
   * @return the body of this rule.
   */
  InMemoryAtomSet getNegativeBody();
}
