package fr.lirmm.graphik;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.errorprone.annotations.Var;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

import static java.nio.charset.StandardCharsets.UTF_8;

class Utils {
	
	public static KBBuilder readKB(KBBuilder kbb , String fileRules , String fileFacts) {

		/* Parsing Rules */
		if(fileRules != null)
		{
			System.out.println("Rules : parsing of '" + fileRules + "'");
			try {
				InputStream ips = new FileInputStream(fileRules);
				InputStreamReader ipsr = new InputStreamReader(ips, UTF_8);
				BufferedReader br = new BufferedReader(ipsr);
				@Var String ligne;

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
				InputStream ips = new FileInputStream(fileFacts);
				InputStreamReader ipsr = new InputStreamReader(ips, UTF_8);
				BufferedReader br = new BufferedReader(ipsr);
				@Var String ligne;

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
