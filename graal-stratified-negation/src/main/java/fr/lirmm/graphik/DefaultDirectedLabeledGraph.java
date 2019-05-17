package fr.lirmm.graphik;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.errorprone.annotations.Var;
import fr.lirmm.graphik.graal.api.core.Rule;

@Deprecated
public class DefaultDirectedLabeledGraph<V,E> {

	private final ArrayList<ArrayList<Pair<Integer, Character>>> adjacencyList;

	public DefaultDirectedLabeledGraph(Iterable<Rule> rules) {
		this.adjacencyList = new ArrayList<>();
		for(Iterator<Rule> i = rules.iterator() ; i.hasNext() ; i.next())
		{
			this.adjacencyList.add(new ArrayList<>());
		}
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		@Var int i = 0;
		for(Iterator<ArrayList<Pair<Integer, Character>>> it = adjacencyList.iterator() ; it.hasNext() ; i++)
		{
			s.append("R");
			s.append(i);
			s.append(" : ");
			s.append(it.next().toString());
			s.append("\n");
		}
		return s.toString();
	}
}
