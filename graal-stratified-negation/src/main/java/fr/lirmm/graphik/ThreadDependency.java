package fr.lirmm.graphik;

import java.util.ArrayList;

import org.jgrapht.DirectedGraph;

import fr.lirmm.graphik.graal.api.core.Rule;

class ThreadDependency extends Thread{

	private final ArrayList<Rule> src;
	private final IndexedByBodyPredicateRuleSetWithNegation index;
	private final DirectedGraph<Rule , DefaultDirectedLabeledEdge> graph;
	
	private int nbDep;
	
	public ThreadDependency(ArrayList<Rule> src , IndexedByBodyPredicateRuleSetWithNegation index , DirectedGraph<Rule, DefaultDirectedLabeledEdge> graph)
	{
		this.src = src;
		this.index = index;
		this.graph = graph;
		this.nbDep = 0;
	}

	@Override
	public void run()
	{
		for(Rule r1 : src)
		{
			Iterable<Rule> candidates = index.getRulesByPredicates(r1.getHead().getPredicates());
			if(candidates != null)
			{
				for(Rule r2 : candidates)
				{
					synchronized (this.graph) {
						if(!graph.containsEdge(r1, r2))
						{
							if(DefaultUnifierWithNegationAlgorithm.instance().existNegativeDependency((DefaultRuleWithNegation)r1, (DefaultRuleWithNegation)r2)) // Negative Dependency
							{						
								addEdge(r1, r2, '-');
							}
							else if(DefaultUnifierWithNegationAlgorithm.instance().existPositiveDependency((DefaultRuleWithNegation)r1, (DefaultRuleWithNegation)r2)) // Positive Dependency
							{
								addEdge(r1, r2, '+');
							}
						}
					}
				}
			}	
		}	
	}
		
	
	private  void addEdge(Rule r1 , Rule r2 , char label)
	{
		synchronized (this.graph) {
		
		this.nbDep++;
				
		graph.addEdge(r1, r2 , new DefaultDirectedLabeledEdge(((DefaultRuleWithNegation)r1).getIndice(),
				((DefaultRuleWithNegation)r2).getIndice(),
				label)
				);
		}
	}
	
	public int getNbDep()
	{
		return this.nbDep;
	}
}
