package fr.inria.main;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectConfiguration;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.stats.Stats;

/**
 * 
 * @author Matias Martinez
 * 
 */
public abstract class AbstractMain {

	protected MutationSupporter mutSupporter;
	protected Factory factory;
	protected ProjectRepairFacade rep;

	static Options options = new Options();

	CommandLineParser parser = new BasicParser();

	static {
		options.addOption("id", true, "(Optional) Name/identified of the project to evaluate (Default: folder name)");
		options.addOption("mode", true, " (Optional) Mode (Default: Statement Mode)");
		options.addOption("location", true, "URL of the project to manipulate");
		options.addOption("dependencies", true, "dependencies of the application, separated by char "
				+ File.pathSeparator);
		options.addOption("package", true, "package to instrument e.g. org.commons.math");
		options.addOption("failing", true, "failing test case");
		options.addOption("out", true,
				"(Optional) Out dir: place were solutions and intermediate program variants are stored. (Default: ./outputMutation/)");
		options.addOption("help", false, "print help and usage");
		options.addOption("bug280", false, "Run the bug 280 from Apache Commons Math");
		options.addOption("bug288", false, "Run the bug 288 from Apache Commons Math");
		options.addOption("bug309", false, "Run the bug 309 from Apache Commons Math");
		options.addOption("bug428", false, "Run the bug 428 from Apache Commons Math");
		options.addOption("bug340", false, "Run the bug 340 from Apache Commons Math");

		// Optional parameters
		options.addOption(
				"jvm4testexecution",
				true,
				"(Optional) location of JVM that executes the mutated version of a program (Folder that contains java script ). For the examples, run jdk 6");
		options.addOption("maxgen", true, "(Optional) max number of generation a program variant is evolved");
		options.addOption("population", true,
				"(Optional)number of population (program variants) that the approach evolves");
		
		options.addOption("maxtime", true, "(Optional) maximum time (in minutes) to execute the whole experiment");

		options.addOption("validation", true, "(Optional) type of validation: process|thread|local ");
		options.addOption("flthreshold", true, "(Optional) threshold for Fault locatication. Default 0.5 ");

		options.addOption("reintroduce", true,
				"(Optional) indicates whether it reintroduces the original program in each generation (default:true)");
		options.addOption("tmax1", true,
				"(Optional) maximum time (in miliseconds) for validating the failing test case ");
		options.addOption("tmax2", true,
				"(Optional) maximum time (in miliseconds) for validating the regression test cases ");
		options.addOption("stopfirst", true,
				"(Optional) Indicates whether it stops when it finds the first solution (default: true)");
		options.addOption(
				"allgens",
				true,
				"(Optional) True if analyze all gens of a program validation during a generation. False for analyzing only one gen per generation.");

		options.addOption("savesolution", false,
				"(Optional) Save on disk intermediate program variants (even those that do not compile)");
		options.addOption("saveall", false, "(Optional) Save on disk the solution variants");

		options.addOption("testbystep", false, "(Optional) Executes each test cases in a separate process.");

		options.addOption(
				"genlistnavigation",
				true,
				"(Optional) Method to navigate the gen space of a variant: inorder, random, weight random (according to the gen's suspicous value)");

		options.addOption("mutationrate", true,
				"(Optional) Value between 0 and 1 that indicates the probability of modify one gen (default: 1)");

		options.addOption("srcjavafolder", true,
				"(Optional) folder with the application source code files (default: /src/java/main/)");

		options.addOption("srctestfolder", true,
				"(Optional) folder with the test cases source code files (default: /src/test/main/)");

		options.addOption("binjavafolder", true,
				"(Optional) folder with the application binaries (default: /target/classes/)");

		options.addOption("bintestfolder", true,
				"(Optional) folder with the test cases binaries (default: /target/test-classes/)");
	
		options.addOption("multigenmodif", false,
				"(Optional) An element of a program variant (i.e., gen) can be modified several times in different generation");
	
		

	}

	public void createFactory() {

		if (factory == null) {
			factory = ProjectRepairFacade.createFactory();
			factory.getEnvironment().setDebug(true);
		}
		mutSupporter = new MutationSupporter(factory);
	}

	public abstract void run(String location, String projectName, String dependencies, String packageToInstrument)
			throws Exception;

	public abstract void run(String location, String projectName, String dependencies, String packageToInstrument,
			double thfl, String failing) throws Exception;

	public boolean processArguments(String[] args) throws Exception {

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (UnrecognizedOptionException e) {
			System.out.println("Error: " + e.getMessage());
			help();
			return false;
		}
		if (cmd.hasOption("help")) {
			help();
			return false;
		}

		if (cmd.hasOption("jvm4testexecution")) {
			ConfigurationProperties.properties
					.setProperty("jvm4testexecution", cmd.getOptionValue("jvm4testexecution"));

		}

		if (!ProjectConfiguration.validJDK()){
			System.out.println("Error: invalid jdk folder");
			return false;
		}
			//throw new IllegalArgumentException("jdk folder not found");

		String dependenciespath = cmd.getOptionValue("dependencies");
		String failing = cmd.getOptionValue("failing");
		String location = cmd.getOptionValue("location");
		String packageToInstrument = cmd.getOptionValue("package");

		// Process mandatory parameters.
		if (dependenciespath == null || failing == null || location == null || packageToInstrument == null) {
			help();
			return false;
		}

		ConfigurationProperties.properties.setProperty("dependenciespath", dependenciespath);
		ConfigurationProperties.properties.setProperty("failing", failing);
		ConfigurationProperties.properties.setProperty("location", location);
		ConfigurationProperties.properties.setProperty("packageToInstrument", packageToInstrument);

		if (cmd.hasOption("id"))
			ConfigurationProperties.properties.setProperty("projectIdentifier", cmd.getOptionValue("id"));

		if (cmd.hasOption("mode"))
			ConfigurationProperties.properties.setProperty("mode", cmd.getOptionValue("mode"));

		if (cmd.hasOption("out"))
			ConfigurationProperties.properties.setProperty("workingDirectory", cmd.getOptionValue("out"));

		// Process optional values.
		if (cmd.hasOption("maxgen"))
			ConfigurationProperties.properties.setProperty("maxGeneration", cmd.getOptionValue("maxgen"));

		if (cmd.hasOption("population"))
			ConfigurationProperties.properties.setProperty("population", cmd.getOptionValue("population"));

		if (cmd.hasOption("validation"))
			ConfigurationProperties.properties.setProperty("validation", cmd.getOptionValue("validation"));

		if (cmd.hasOption("flthreshold")) {
			try {
				double thfl = Double.valueOf(cmd.getOptionValue("flthreshold"));
				ConfigurationProperties.properties.setProperty("flthreshold", cmd.getOptionValue("flthreshold"));

			} catch (Exception e) {
				System.out.println("Error: threshold not valid");
				help();
				return false;
			}
		}

		if (cmd.hasOption("reintroduce"))
			ConfigurationProperties.properties.setProperty("reintroduce", cmd.getOptionValue("reintroduce"));

		if (cmd.hasOption("tmax1"))
			ConfigurationProperties.properties.setProperty("tmax1", cmd.getOptionValue("tmax1"));

		if (cmd.hasOption("tmax2"))
			ConfigurationProperties.properties.setProperty("tmax2", cmd.getOptionValue("tmax2"));

		if (cmd.hasOption("stopfirst"))
			ConfigurationProperties.properties.setProperty("stopfirst", cmd.getOptionValue("stopfirst"));

		if (cmd.hasOption("allgens"))
			ConfigurationProperties.properties.setProperty("allgens", cmd.getOptionValue("allgens"));

		if (cmd.hasOption("savesolution"))
			ConfigurationProperties.properties.setProperty("savesolution", "true");

		if (cmd.hasOption("saveall"))
			ConfigurationProperties.properties.setProperty("saveall", "true");

		if (cmd.hasOption("testbystep"))
			ConfigurationProperties.properties.setProperty("testbystep", "true");

		if (cmd.hasOption("genlistnavigation"))
			ConfigurationProperties.properties
					.setProperty("genlistnavigation", cmd.getOptionValue("genlistnavigation"));

		if (cmd.hasOption("mutationrate"))
			ConfigurationProperties.properties.setProperty("mutationrate", cmd.getOptionValue("mutationrate"));

		if (cmd.hasOption("srcjavafolder"))
			ConfigurationProperties.properties.setProperty("srcjavafolder", cmd.getOptionValue("srcjavafolder"));

		if (cmd.hasOption("srctestfolder"))
			ConfigurationProperties.properties.setProperty("srctestfolder", cmd.getOptionValue("srctestfolder"));

		if (cmd.hasOption("binjavafolder"))
			ConfigurationProperties.properties.setProperty("binjavafolder", cmd.getOptionValue("binjavafolder"));

		if (cmd.hasOption("bintestfolder"))
			ConfigurationProperties.properties.setProperty("bintestfolder", cmd.getOptionValue("bintestfolder"));

		if (cmd.hasOption("maxtime"))
			ConfigurationProperties.properties.setProperty("maxtime", cmd.getOptionValue("maxtime"));

		if (cmd.hasOption("multigenmodif"))
			ConfigurationProperties.properties.setProperty("multigenmodif", "true");
		return true;
	}

	/**
	 * Finds an example to test in the command line
	 * 
	 * @param cmd
	 * @return
	 * @throws Exception
	 */
	public boolean executeExample(String[] args) throws Exception {

		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("bug280")) {
			String dependenciespath = "examples/Math-issue-280/lib/junit-4.4.jar";
			String folder = "Math-issue-280";
			String failing = "org.apache.commons.math.distribution.NormalDistributionTest";
			File f = new File("examples/Math-issue-280/");
			String location = f.getParent();
			String packageToInstrument = "org.apache.commons";
			double thfl = 0.5;
			this.run(location, folder, dependenciespath, packageToInstrument, thfl, failing);
			return true;
		}

		if (cmd.hasOption("bug288")) {
			String dependenciespath = "examples/Math-issue-288/lib/junit-4.4.jar";
			String folder = "Math-issue-288";
			String failing = "org.apache.commons.math.optimization.linear.SimplexSolverTest";
			File f = new File("examples/Math-issue-288/");
			String location = f.getParent();
			String packageToInstrument = "org.apache.commons";
			double thfl = 0.2;
			this.run(location, folder, dependenciespath, packageToInstrument, thfl, failing);
			return true;
		}
		if (cmd.hasOption("bug309")) {
			String dependenciespath = "examples/Math-issue-309/lib/junit-4.4.jar";
			String folder = "Math-issue-309";
			String failing = "org.apache.commons.math.random.RandomDataTest";
			File f = new File("examples/Math-issue-309/");
			String location = f.getParent();
			String packageToInstrument = "org.apache.commons";
			double thfl = 0.5;
			this.run(location, folder, dependenciespath, packageToInstrument, thfl, failing);
			return true;
		}
		if (cmd.hasOption("bug340")) {
			String dependenciespath = "examples/Math-issue-340/lib/junit-4.4.jar";
			String folder = "Math-issue-340";
			String failing = "org.apache.commons.math.fraction.BigFractionTest";
			File f = new File("examples/Math-issue-340/");
			String location = f.getParent();
			String packageToInstrument = "org.apache.commons";
			double thfl = 0.5;
			this.run(location, folder, dependenciespath, packageToInstrument, thfl, failing);
			return true;
		}
		if (cmd.hasOption("bug428")) {
			String dependenciespath = "examples/Lang-issue-428/lib/easymock-2.5.2.jar" + File.pathSeparator
					+ "examples/Lang-issue-428/lib/junit-4.7.jar";
			String folder = "Lang-issue-428";
			String failing = "org.apache.commons.lang3.StringUtilsIsTest";
			File f = new File("examples/Lang-issue-428/");
			String location = f.getParent();
			String packageToInstrument = "org.apache.commons";
			double thfl = 0.5;
			this.run(location, folder, dependenciespath, packageToInstrument, thfl, failing);
			return true;
		}

		return false;
	}

	private static void help() {

		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.out.println("More options and default values at 'configuration.properties' file");

		System.exit(0);

	}

	protected Factory getFactory() {
		//Factory facade = FactoryImpl.getLauchingFactory();

		if (factory == null) {
			factory = rep.createFactory();
			factory.getEnvironment().setDebug(true);
		}
		return factory;
	}

	/*
	 * protected ProjectRepairFacade getProject(String location, String method,
	 * String regressiontest, List<String> failingTestCases, String
	 * dependencies, boolean srcWithMain) throws Exception { File locFile = new
	 * File(location); String projectname = locFile.getName();
	 * 
	 * return getProject(location, projectname, method, regressiontest,
	 * failingTestCases, dependencies, srcWithMain); }
	 */

	protected ProjectRepairFacade getProject(String location, String projectIdentifier, String method,
			List<String> failingTestCases, String dependencies, boolean srcWithMain)
			throws Exception {

		if (projectIdentifier == null || projectIdentifier.equals("")) {
			File locFile = new File(location);
			projectIdentifier = locFile.getName();
		}

		String key = File.separator + method + "-" + projectIdentifier + File.separator;
		String inResult = ConfigurationProperties.getProperty("workingDirectory") + key + "/src/";
		String outResult = ConfigurationProperties.getProperty("workingDirectory") + key + "/bin/";
		String originalProjectRoot = location + File.separator;
		// location + File.separator + projectIdentifier + File.separator;

		List<String> src = determineMavenFolders(srcWithMain, originalProjectRoot);

		String libdir = dependencies;

		//String mainClassTest = regressionTest;

		ProjectConfiguration properties = new ProjectConfiguration();
		properties.setInDir(inResult);
		properties.setOutDir(outResult);
		properties.setOriginalAppBinDir(originalProjectRoot + File.separator
				+ ConfigurationProperties.getProperty("binjavafolder"));
		properties.setOriginalTestBinDir(originalProjectRoot + File.separator
				+ ConfigurationProperties.getProperty("bintestfolder"));
		properties.setFixid(projectIdentifier);

		properties.setOriginalProjectRootDir(originalProjectRoot);
		properties.setOriginalDirSrc(src);
		properties.setLibPath(libdir);
		properties.setFailingTestCases(failingTestCases);

		properties.setPackageToInstrument(ConfigurationProperties.getProperty("packageToInstrument"));

		ProjectRepairFacade ce = new ProjectRepairFacade(properties);

		return ce;
	}

	private List<String> determineMavenFolders(boolean srcWithMain, String originalProjectRoot) {

		File srcdefault = new File(originalProjectRoot + File.separator
				+ ConfigurationProperties.getProperty("srcjavafolder"));
		
		File testdefault = new File(originalProjectRoot + File.separator
				+ ConfigurationProperties.getProperty("srctestfolder"));
		
		if (srcdefault.exists() && testdefault.exists())
			return Arrays.asList(new String[] { ConfigurationProperties.getProperty("srcjavafolder"),
					ConfigurationProperties.getProperty("srctestfolder") });

		File src = new File(originalProjectRoot + File.separator + "src/main/java");
		if (src.exists())
			return Arrays.asList(new String[] { "src/main/java", "src/test/java" });

		src = new File(originalProjectRoot + File.separator + "src/java");
		if (src.exists())
			return Arrays.asList(new String[] { "src/java", "src/test" });

		return Arrays.asList(new String[] { "src", "test" });

	}

}
