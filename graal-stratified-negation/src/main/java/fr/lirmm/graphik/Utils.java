package fr.lirmm.graphik;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.Rules;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Utils {
	
	public static KBBuilder readKB(KBBuilder kbb , String fileRules , String fileFacts) {

		/* Parsing Rules */
		if(fileRules != null)
		{
			System.out.println("Rules : parsing of '" + fileRules + "'");
			try {
				InputStream ips = new FileInputStream(fileRules);
				InputStreamReader ipsr = new InputStreamReader(ips);
				BufferedReader br = new BufferedReader(ipsr);
				String ligne;

				while ((ligne = br.readLine()) != null) {
					if(ligne.charAt(0) != '%')
						kbb.add(DlgpParserNeg.parseRule(ligne));
				}

				br.close();
				ipsr.close();
				ips.close();

			}
			catch (Exception e) {
				System.out.println("Caca" + e.toString());
				e.printStackTrace();
			}
		}

		/* Parsing Facts */

		if(fileFacts != null)
		{
			System.out.println("Facts : parsing of '" + fileFacts + "'");
			try {
				InputStream ips;

				ips = new FileInputStream(fileFacts);
				InputStreamReader ipsr = new InputStreamReader(ips);
				BufferedReader br = new BufferedReader(ipsr);
				String ligne;

				while ((ligne = br.readLine()) != null){
					if(ligne.charAt(0) != '%')
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

	
	public static LinkedListRuleSet compactRules(Iterator<Rule> iterator)
	{	
		HashMap<String, Pair<InMemoryAtomSet, InMemoryAtomSet>> mapBody = new HashMap<>();
		HashMap<String, InMemoryAtomSet> mapHead = new HashMap<>();

		StringBuilder sBody = new StringBuilder();

		for(; iterator.hasNext() ;)
		{
			DefaultRuleWithNegation r = (DefaultRuleWithNegation) iterator.next();
			sBody.delete(0, sBody.length());
			sBody.append(r.getBody().toString());
			sBody.append(r.getNegativeBody().toString());



			if(mapBody.get(sBody.toString()) == null)
				mapBody.put(sBody.toString() , new Pair<>(r.getBody(), r.getNegativeBody()));

			InMemoryAtomSet h = mapHead.get(sBody.toString());
			if(h != null)
			{
				h.addAll(r.getHead());
			}
			else
			{
				h = r.getHead();
			}

			mapHead.put(sBody.toString() , h);
		}

		LinkedListRuleSet res = new LinkedListRuleSet();
		for(String b : mapHead.keySet())
		{
			DefaultRuleWithNegation r = new DefaultRuleWithNegation(b, mapBody.get(b).getFirst(), mapBody.get(b).getLast(), mapHead.get(b));
			res.add(r);
		}

		mapBody.clear();
		mapHead.clear();

		return res;
	}

	
	public static String writeAtoms(CloseableIterator<Atom> itAt , boolean not)
	{
		StringBuilder s = new StringBuilder();


		try {
			for( ; itAt.hasNext() ;)
			{
				Atom a = itAt.next();

				if(not)
					s.append("not_");

				s.append(a.getPredicate().getIdentifier());
				s.append("(");
				boolean f = true;

				for(Term t : a.getTerms())
				{
					if(f)
						f = false;
					else
						s.append(", ");

					s.append(t.toString());
				}
				s.append(").\n");
			}

			itAt.close();
		} catch (IteratorException e) {
			e.printStackTrace();
		}

		return s.toString();		
	}


	public static Collection<DefaultRuleWithNegation> decompose(Iterator<Rule> iterator)
	{
		Collection <DefaultRuleWithNegation> res = new LinkedList<>();

		for( ; iterator.hasNext() ;)
		{
			DefaultRuleWithNegation r = (DefaultRuleWithNegation) iterator.next();
			Collection<Rule> c = Rules.computeAtomicHead(r);

			//System.out.println("decompo de : " + r + "\n\n" );
			for(Rule r2 : c)
			{
				DefaultRuleWithNegation rFinal = new DefaultRuleWithNegation(r2.getLabel(), r2.getBody(), r.getNegativeBody(), r2.getHead());
				res.add(rFinal);
				//System.out.println(rFinal);
			}

		}

		return res;
	}
	
	public static String displayFacts(AtomSet facts)
	{
		StringBuilder s = new StringBuilder("== Saturation ==\n");
		
		try {
			for(CloseableIterator<Atom> itAtom = facts.iterator() ; itAtom.hasNext() ; )
			{
				s.append(itAtom.next().toString());
				s.append(".\n");
			}
		} catch (IteratorException e) {
			e.printStackTrace();
		}
		
		return s.toString();
	}
}
