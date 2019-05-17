package fr.lirmm.graphik;


import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.graal.api.core.Atom;

public class DefaultRuleWithNegation extends DefaultRule implements RuleWithNegation {

	private final InMemoryAtomSet negativeBody;
	private int indice;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	public DefaultRuleWithNegation(String label, InMemoryAtomSet positiveBody, InMemoryAtomSet negativeBody, InMemoryAtomSet head) {
		
		super(label , positiveBody , head);
		this.negativeBody = negativeBody;
		
		try {
			this.indice = Integer.parseInt(getLabel());
		}
		catch (NumberFormatException e) {
			System.out.println("Error not a number : '" + getLabel() + "'");
		}
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	
	public InMemoryAtomSet getNegativeBody() {
		return this.negativeBody;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		this.appendTo(builder);
		
		return builder.toString();
	}

	@Override
	public void appendTo(StringBuilder builder) {
		
		if(!this.getLabel().isEmpty()) {
			builder.append('[');
			builder.append(this.getLabel());
			builder.append("] ");
		}
		
		/* Positive body */
		builder.append("[");
		
		
		for(Predicate p : this.getBody().getPredicates()) {
			
			CloseableIteratorWithoutException<Atom> itAtom = this.getBody().atomsByPredicate(p);
			for( ; itAtom.hasNext() ; )
			{
				Atom a = itAtom.next();
				builder.append(a.toString());
				builder.append(" , ");
			}
			itAtom.close();
			
		}
		
		builder.replace(builder.length() - 2, builder.length(), "");
		
		
		/* Negative body */
		
		for(Predicate p : this.getNegativeBody().getPredicates()) {
			
			CloseableIteratorWithoutException<Atom> itAtom = this.getNegativeBody().atomsByPredicate(p);
			for( ; itAtom.hasNext() ; )
			{
				Atom a = itAtom.next();
				builder.append(" , !");
				builder.append( a.toString());
			}
			itAtom.close();
		}
		
		builder.append("] -> ");
		builder.append(this.getHead());
	}
	
	public int getIndice()
	{
		return this.indice;
	}
}
