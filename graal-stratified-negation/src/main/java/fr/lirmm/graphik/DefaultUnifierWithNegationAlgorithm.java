package fr.lirmm.graphik;

import javax.swing.text.StyledEditorKit.ItalicAction;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.unifier.UnifierChecker;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.core.unifier.DefaultUnifierAlgorithm;
import fr.lirmm.graphik.graal.core.unifier.checker.ProductivityChecker;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;
	

public class DefaultUnifierWithNegationAlgorithm {
		
		private static DefaultUnifierWithNegationAlgorithm instance;
		private UnifierChecker tab[] = {};
		

		// /////////////////////////////////////////////////////////////////////////
		// PUBLIC METHODS
		// /////////////////////////////////////////////////////////////////////////
		
		
		public static synchronized DefaultUnifierWithNegationAlgorithm instance() {
			if (instance == null)
				instance = new DefaultUnifierWithNegationAlgorithm();
	
			return instance;
		}
		
		
		public boolean existPositiveDependency(DefaultRuleWithNegation src , DefaultRuleWithNegation dest)
		{			
			DefaultRuleWithNegation r1 = this.createImageOf(src, DefaultUnifierAlgorithm.getSourceVariablesSubstitution());
			DefaultRuleWithNegation r2 = this.createImageOf(dest, DefaultUnifierAlgorithm.getTargetVariablesSubstitution());

			CloseableIteratorWithoutException<Substitution> sigmas = 
					DefaultUnifierAlgorithm.instance().computePieceUnifier(r1, r2, ProductivityChecker.instance()); // Compute Piece unifiers
			
			for( ; sigmas.hasNext() ; ) {
				Substitution s = sigmas.next();

				if(isValidPositiveUnifier(r1, r2, s)) {
					sigmas.close();
					return true;
				}
			}
				
			sigmas.close();
			
			return false;
		}
		
		
		public boolean existNegativeDependency(DefaultRuleWithNegation src , DefaultRuleWithNegation dest)
		{
			DefaultRuleWithNegation r1 = this.createImageOf(src, DefaultUnifierAlgorithm.getSourceVariablesSubstitution());
			DefaultRuleWithNegation r2 = this.createImageOf(dest, DefaultUnifierAlgorithm.getTargetVariablesSubstitution());
			
			//System.out.println("test iterator existentielles : ");
			try {
				for(CloseableIterator<Atom> itAtom = r1.getHead().iterator() ; itAtom.hasNext() ; )
				{
					Atom a = itAtom.next();
					//System.out.println("Parcours terme de " + a);
					for(Term t : a.getTerms())
					{
						if(t.isVariable())
						{
							if(r1.getExistentials().contains(t))
							{
								System.out.println("existentielles " + t + " pour atome : " + a);
								r1.getHead().remove(a);
								
								break;
								//return false;
							}
						}
					}
				}
			} catch (IteratorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*if(!r1.getExistentials().isEmpty())
				return false;*/
			

			r1 = this.createImageOf(r1, DefaultUnifierAlgorithm.getSourceVariablesSubstitution());
			r2 = this.createImageOf(r2, DefaultUnifierAlgorithm.getTargetVariablesSubstitution());
			
			CloseableIteratorWithoutException<Substitution> sigmas = 
					DefaultUnifierAlgorithm.instance().computePieceUnifier(r1, r2.getNegativeBody(), tab); // Compute Piece unifiers
			
			for( ; sigmas.hasNext() ; ) {
				
				if(isValidNegativeUnifier(r1, r2, sigmas.next())) {
					sigmas.close();
					return true;
				}
			}
				
			sigmas.close();
			
			return false;
		}
		
		
		// /////////////////////////////////////////////////////////////////////////
		// PRIVATE METHODS
		// /////////////////////////////////////////////////////////////////////////
		
		
		private boolean isValidPositiveUnifier(DefaultRuleWithNegation r1 , DefaultRuleWithNegation r2 , Substitution s) {
			
			/* Application substitution */
			InMemoryAtomSet bpi = s.createImageOf(r1.getBody());
			InMemoryAtomSet bni = s.createImageOf(r1.getNegativeBody());
		
			InMemoryAtomSet bpj = s.createImageOf(r2.getBody());
			InMemoryAtomSet bnj = s.createImageOf(r2.getNegativeBody());
			
			InMemoryAtomSet hi = s.createImageOf(r1.getHead());
			InMemoryAtomSet hj = s.createImageOf(r2.getHead());
			
			
			boolean i = !hasIntersection(bpi, bni);
			
			boolean ii = !hasIntersection(bpi, bnj);
			
			boolean iii = !hasIntersection(bpj, bnj);
			
			InMemoryAtomSet bpjBis = s.createImageOf(bpj);
			bpjBis.removeAll(hi);
			boolean iv = !hasIntersection(bni, bpjBis);
			
			InMemoryAtomSet union = new LinkedListAtomSet();
			union.add(s.createImageOf(r1.getHead().iterator().next())); // Atomic heads
			union.addAll(bpi);
			union.addAll(bpj);
			boolean v = false;
			try {
				for(CloseableIterator<Atom> itAtom = hj.iterator() ; itAtom.hasNext() ; )
				{
					if(!union.contains(itAtom.next()))
					{
						v = true;
						break;
					}
				}
			} catch (IteratorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			boolean vi = !hasIntersection(bnj, hi);
			
			boolean vii = false;
			try {
				for(CloseableIterator<Atom> itAtom = bpj.iterator() ; itAtom.hasNext() ; )
				{
					if(!bpi.contains(itAtom.next()))
					{
						vii = true;
						break;
					}
				}
			}
			catch (IteratorException e)
			{
				e.printStackTrace();
			}
			
			
			
			bpi.clear();
			bni.clear();
			bpj.clear();
			bpjBis.clear();
			bnj.clear();
			hi.clear();
			hj.clear();
			union.clear();
			
			
			return (i && ii && iii && iv && v && vi);
		}
		
		
		private boolean isValidNegativeUnifier(DefaultRuleWithNegation r1 , DefaultRuleWithNegation r2 , Substitution s) {
			
			/* Application substitution */
			InMemoryAtomSet bpi = s.createImageOf(r1.getBody());
			InMemoryAtomSet bni = s.createImageOf(r1.getNegativeBody());
		
			InMemoryAtomSet bpj = s.createImageOf(r2.getBody());
			InMemoryAtomSet bnj = s.createImageOf(r2.getNegativeBody());
		
			InMemoryAtomSet uPos = new LinkedListAtomSet();
				uPos.addAll(bpi);
				uPos.addAll(bpj);
		
			InMemoryAtomSet uNeg = new LinkedListAtomSet();
				uNeg.addAll(bni);
				uNeg.addAll(bnj);
		
			/* (i) */
			boolean inter = hasIntersection(uPos , uNeg); // inter = (B+1 , B+2) inter (B-1 , B-2)
			
			
			bpi.clear();
			bni.clear();
			bpj.clear();
			bnj.clear();
			uPos.clear();
			uNeg.clear();
			
			return (!inter);
		}
		
		
		private static boolean hasIntersection(InMemoryAtomSet a1 , InMemoryAtomSet a2) {
			
			return a1.removeAll(a2);
		}
		
		
		
		/*********************************************************************/
		/********************   A DEPLACER ***********************************/
		/*********************************************************************/
	
		private DefaultRuleWithNegation createImageOf(DefaultRuleWithNegation rule , Substitution s)
		{
			DefaultRuleWithNegation substitut = new DefaultRuleWithNegation(
					rule.getLabel() ,
					s.createImageOf(rule.getBody()) ,
					s.createImageOf(rule.getNegativeBody()) ,
					s.createImageOf(rule.getHead()));
			
			return substitut;
		}
	}
