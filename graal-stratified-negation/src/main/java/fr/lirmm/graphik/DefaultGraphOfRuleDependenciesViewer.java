package fr.lirmm.graphik;

import java.util.List;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;

public class DefaultGraphOfRuleDependenciesViewer {

	private static DefaultGraphOfRuleDependenciesViewer instance;
	
	
	public static synchronized DefaultGraphOfRuleDependenciesViewer instance() {
		if (instance == null)
			instance = new DefaultGraphOfRuleDependenciesViewer();

		return instance;
	}
	
	/***********/
	/*** GRD ***/
	/***********/
	public Graph getGraph(GraphOfRuleDependencies g)
	{
		Graph graphDisp = new SingleGraph("Graph Of Rules Dependencies");
		computeVertices(g , graphDisp);
		addDependencies(g , graphDisp);
		
		graphDisp.addAttribute("ui.stylesheet", "url(file:styles.css)");
		
		graphDisp.addAttribute("ui.quality");
		graphDisp.addAttribute("ui.antialias");
		
		return graphDisp;
	}
	
	
	private void computeVertices(GraphOfRuleDependencies g , Graph graphDisp)
	{
		for(Rule r : g.getRules())
		{
			Node n = graphDisp.addNode(r.getLabel());
			n.addAttribute("label", r.getLabel());
			n.addAttribute("rule", r.toString());
		}
		
		/* Colors */
		List<List<Rule>> l = ((DefaultLabeledGraphOfRuleDependencies)g).getBadCircuits();
		
		if(!l.isEmpty())
		{
			System.out.println("Bad circuits :  " + l.toString());
			for(List<Rule> c : l)
			{
				for(Rule r : c)
				{
					graphDisp.getNode(r.getLabel()).addAttribute("ui.class", "bad");
				}
			}
		}
	}
	
	
	private void addDependencies(GraphOfRuleDependencies g , Graph graphDisp)
	{
		for(Rule src : g.getRules())
		{
			for(Rule dest : g.getTriggeredRules(src))
			{
				StringBuilder edgeID = new StringBuilder();
				edgeID.append(src.getLabel());
				edgeID.append("|");
				edgeID.append(dest.getLabel());
					
				Edge e = graphDisp.addEdge(edgeID.toString(),
						src.getLabel(),
						dest.getLabel(),
						true
						);
				e.setAttribute("label", '+');
				e.addAttribute("ui.class", "plus");
			}
			
			if(g instanceof DefaultLabeledGraphOfRuleDependencies)
			{
				for(Rule dest : ((DefaultLabeledGraphOfRuleDependencies)g).getInhibitedRules(src))
				{
					StringBuilder edgeID = new StringBuilder();
					edgeID.append(src.getLabel());
					edgeID.append("|");
					edgeID.append(dest.getLabel());
						
					Edge e = graphDisp.addEdge(edgeID.toString(),
							src.getLabel(),
							dest.getLabel(),
							true
							);
					e.setAttribute("label", '-');
					e.addAttribute("ui.class", "moins");
				}
			}
		}
		
		/* Colors on edges */
		List<List<Rule>> l = ((DefaultLabeledGraphOfRuleDependencies)g).getBadCircuits();
		
		if(!l.isEmpty())
		{
			for(List<Rule> c : l)
			{
				for(DefaultDirectedLabeledEdge e : ((DefaultLabeledGraphOfRuleDependencies)g).getBadEdges(c))
				{
					if(graphDisp.getEdge(e.getFirst() + "|" + e.getSecond()).getAttribute("ui.class").toString().compareTo("moins") == 0)
						graphDisp.getEdge(e.getFirst() + "|" + e.getSecond()).addAttribute("ui.class", "bad" );
				}
			}
		}
	}
	/***********/
	/*** SCC ***/
	/***********/
	public Graph getSCCGraph(GraphOfRuleDependencies graph) {
		StronglyConnectedComponentsGraph<Rule> sccGraph = graph.getStronglyConnectedComponentsGraph();
		Graph graphDisp = new SingleGraph("Graph of Strongly Connected Components");
		
		computeSccVertices(sccGraph , graphDisp , graph);
		addSccDependencies(sccGraph, graphDisp, graph);
		
		
		/* Style */
		graphDisp.addAttribute("ui.stylesheet", "url(file:styles.css)");
		
		return graphDisp;
	}
		
	private void computeSccVertices(StronglyConnectedComponentsGraph<Rule> sccGraph , Graph graphDisp , GraphOfRuleDependencies graph)
	{
		/* Vertices */
		
		for(int i = 0 ; i < sccGraph.getNbrComponents() ; i++)
		{
			Node n = graphDisp.addNode("C" + i);
			n.addAttribute("label", "C"+i);
			n.addAttribute("c", sccGraph.getComponent(i).toString());
		}
		
		/* Colors on vertices */
		List<List<Rule>> l = ((DefaultLabeledGraphOfRuleDependencies)graph).getBadCircuits();
		
		if(l != null)
		{
			boolean touch;
			
			for(int i = 0 ; i < sccGraph.getNbrComponents() ; i++)
			{
				touch = false;
				for(List<Rule> c : l)
				{
					for(Rule r : c)
					{
						if(sccGraph.getComponent(i).contains(r))
						{
							graphDisp.getNode("C" + i).addAttribute("ui.class", "bad");
							touch = true;
							break;
						}
					}
					
					if(touch)
						break;
				}
			}
		}
	}
	
	private void addSccDependencies(StronglyConnectedComponentsGraph<Rule> sccGraph , Graph graphDisp , GraphOfRuleDependencies graph)
	{
		/* Edges */
		for(int i = 0 ; i < sccGraph.getNbrComponents() ; i++)
		{
			for(Rule src : sccGraph.getComponent(i))
			{
				for(int j = 0 ; j < sccGraph.getNbrComponents() ; j++)
				{
					Set<Rule> s = sccGraph.getComponent(j);
					String edgeID = "C" + i + "|C" + j;
					
					for(Rule dest : ((DefaultLabeledGraphOfRuleDependencies) graph).getInhibitedRules(src))
					{
						if(s.contains(dest) && graphDisp.getEdge(edgeID) == null && i != j)
						{
							Edge e = graphDisp.addEdge(edgeID,
									"C" + i,
									"C" + j,
									true
									);
							e.setAttribute("label", '-');
							e.addAttribute("ui.class", "moins");
						}
					}
					
					for(Rule dest : graph.getTriggeredRules(src))
					{
						if(s.contains(dest) && graphDisp.getEdge(edgeID) == null && i != j)
						{
							Edge e = graphDisp.addEdge(edgeID,
									"C" + i,
									"C" + j,
									true
									);
							e.setAttribute("label", '+');
							e.addAttribute("ui.class", "plus");
						}
					}
				}
			}
		}
	}
	
	
	/********************/
	/*** Fast Display ***/
	/********************/
	public void display(GraphOfRuleDependencies g)
	{
		Graph graphDisp = new SingleGraph("Graph Of Rules Dependencies");
		computeVertices(g , graphDisp);
		addDependencies(g , graphDisp);

	    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	    
	    graphDisp.addAttribute("ui.stylesheet", "url(file:styles.css)");
		graphDisp.addAttribute("ui.quality");
		graphDisp.addAttribute("ui.antialias");
		graphDisp.display();			
	}
	
	
}
