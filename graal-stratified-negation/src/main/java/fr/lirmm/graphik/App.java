package fr.lirmm.graphik;

import java.io.File;
import java.util.Map;

import org.graphstream.graph.Graph;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;

class App {

	private static final String PROGRAM_NAME = "graal-stratified-negation";
	private static final String VERSION = "1.0";
	public static final Map<String, RuleSetProperty> propertyMap = RuleSetPropertyHierarchy.generatePropertyMap();

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		// new Window(true);

		App options = new App();
		JCommander commander = null;
		try {
			commander = new JCommander(options, args);
		} catch (com.beust.jcommander.ParameterException e) {

			System.err.println(e.getMessage());
			System.exit(1);
		}

		if (options.help) {
			System.out.println(
					"For more details about this tool see : https://github.com/arthur-boixel/graal-stratified-negation ");
			commander.usage();
			System.exit(0);
		}

		if (options.version) {
			printVersion();
			System.exit(0);
		}

		if (options.gui) {
			new Window(true);
		} else if (options.input_filepath.compareTo("-") == 0) {
			System.out.println("Error, you need a Rule Base or at least launch the GUI");
			System.exit(0);
		} else {

			// init GRD
			DefaultLabeledGraphOfRuleDependencies grd = new DefaultLabeledGraphOfRuleDependencies(new File(options.input_filepath));

			if (options.print_ruleset) {
				String s = Window.getRulesText(grd.getRules());
				System.out.println(s);
			}

			if (options.print_grd) {
				String s = Window.getGRDText(grd);
				System.out.println(s);
			}

			if (options.print_scc) {
				String s = Window.getSCCText(grd.getStronglyConnectedComponentsGraph());
				System.out.println(s);
			}

			if (options.print_gscc) {
				Graph sccDisp = DefaultGraphOfRuleDependenciesViewer.instance().getSCCGraph(grd);
				String s = Window.getGSCCText(sccDisp);
				System.out.println(s);
			}

			System.out.println();

			System.out.print("===== ANALYSIS : ");
			if (!grd.hasCircuitWithNegativeEdge()) {
				System.out.println("STRATIFIABLE =====");

				if (options.facts_filepath.compareTo("-") != 0) {
					KBBuilder kbb = new KBBuilder();
					String s = Window.getSaturationFromFile(options.facts_filepath, grd);
					System.out.println(s);
				}
			} else {
				System.out.println("NOT STRATITIFABLE =====");
			}
		}

		System.out.println();
	}

	private static void printVersion() {
		System.out.println(PROGRAM_NAME + " version " + VERSION);
	}

	@Parameter(names = { "-f", "--input-file" }, description = "Rule set input file.")
	private final String input_filepath = "-";

	@Parameter(names = { "-g", "--grd" }, description = "Print the Graph of Rule Dependencies.")
	private final boolean print_grd = false;

	@Parameter(names = { "-s", "--print-scc" }, description = "Print the Strongly Connected Components.")
	private final boolean print_scc = false;

	@Parameter(names = { "-G",
			"--print-gscc" }, description = "Print the graph of the GRD Strongly Connected Components.")
	private final boolean print_gscc = false;

	@Parameter(names = { "-r",
			"--rule-set" }, description = "Print the rule set.")
	private final boolean print_ruleset = false;

	@Parameter(names = { "-c",
			"--forward-chaining" }, description = "Apply forward chaining on the specified Fact Base.")
	private final String facts_filepath = "-";

	@Parameter(names = { "-w", "--window" }, description = "Launch the GUI.")
	private final boolean gui = false;

	@Parameter(names = { "-h", "--help" }, description = "Print this message.")
	private final boolean help = false;

	@Parameter(names = { "-v", "--version" }, description = "Print version information")
	private final boolean version = false;

}
