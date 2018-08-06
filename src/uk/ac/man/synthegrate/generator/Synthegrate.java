package uk.ac.man.synthegrate.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import uk.ac.man.synthegrate.utils.Constants;
import uk.ac.man.synthegrate.utils.Utils;
import uk.ac.man.synthegrate.utils.WriteUtils;

public class Synthegrate {
	
//	private static final Logger log = LoggerFactory.getLogger(Synthegrate.class);

	protected static Properties synthegrateProperties;

	static int max_source_relations = 1000;
	static long min_cardinality = 100;
	static long max_cardinality = 300;
	static int min_arity = 5;
	static int max_arity = 56;
	static int tries = 1000;
	static int schemas_number = 1;
	static int target_relations = 1;
	static int target_pks_number = 1;
	
	static int max_joins_number = 55;
	static int max_unions_number = 0;
	//out of the number of expected combinations joins + unions
	static double max_distractions_ratio = 0.0;
	static double fks_ratio = 0.0;
	static double disjoint_unions = 1.0;
	static double reuse_join_attributes = 0.0;
	static boolean create_unions_first = false;
	static boolean create_joins_first = true;
	
	static boolean star_join = false;
	static boolean chain_join = true;
	static boolean balanced_join = false;
	
	static String output_folder = "";
	
	
	
	public static void main(String[] args) {
		
		Options options = new Options();

		Option input = new Option("p", "properties", true, "properties file path");
		input.setRequired(true);
		options.addOption(input);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);

			String propertiesFilePath = cmd.getOptionValue("properties");
			File propertiesFile = new File(propertiesFilePath);

			if (!propertiesFile.exists()) {
				System.err.println("Properties file " + propertiesFilePath + " could not be found!");
				System.exit(1);
			}

			Properties arg_properties = new Properties();

			arg_properties.load(new FileInputStream(propertiesFile));
			setProperties(arg_properties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			formatter.printHelp("utility-name", options);
			System.exit(1);
			return;
		}

		
//		long max_distractions =Math.round( max_distractions_ratio*(max_joins_number+max_unions_number));
		long max_distractions = 0l;

		ScenarioGenerator generator = new ScenarioGenerator(max_source_relations, target_relations,
				target_pks_number, min_cardinality, max_cardinality, max_joins_number, max_unions_number,
				max_distractions, fks_ratio, tries, min_arity, max_arity, schemas_number, disjoint_unions,
				reuse_join_attributes, create_unions_first, create_joins_first, star_join, chain_join, balanced_join);

		generator.setFolder_path(output_folder);
		generator.generate_scenario();
		
		Utils.print_last_names();

	}
	
	public static String getUnionsJoinsString() {
		return "U"+max_unions_number+"_"+"J"+max_joins_number;
	}
	
	private static void setProperties(Properties properties) {
		
		synthegrateProperties = properties;

		if (synthegrateProperties == null) {
			System.err.println("Unknown cause: The project properties were not set.");
			System.exit(1);
		}

		String tmp_string ="";

		tmp_string = synthegrateProperties.getProperty(Constants.OUTPUT_FOLDER);
		if (tmp_string != null) {
			File output_dir = new File(tmp_string);
			
			if (!output_dir.exists())
				if (output_dir.mkdirs())
					System.err.println("Output directory was not found and it could not be created:"+output_folder);
			
			output_folder = tmp_string.trim();
		}
		
		max_source_relations = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_SOURCE_RELATIONS,"1000").trim());
		min_cardinality = Integer.parseInt(synthegrateProperties.getProperty(Constants.MIN_CARDINALITY,"100").trim());
		max_cardinality = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_CARDINALITY,"1000").trim());
		min_arity = Integer.parseInt(synthegrateProperties.getProperty(Constants.MIN_ARITY,"3").trim());
		max_arity = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_ARITY,"10").trim());
		tries = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_TRIES,"1000").trim());
		schemas_number = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_SOURCE_SCHEMAS_NUMBER,"1").trim());
		target_relations = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_TARGET_RELATIONS,"1").trim());
		target_pks_number = Integer.parseInt(synthegrateProperties.getProperty(Constants.TARGET_PRIMARY_KEYS_NUMBER,"1").trim());
		
		max_joins_number = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_JOINS_NUMBER,"1").trim());
		max_unions_number = Integer.parseInt(synthegrateProperties.getProperty(Constants.MAX_UNIONS_NUMBER,"1").trim());
		
		fks_ratio = Double.parseDouble(synthegrateProperties.getProperty(Constants.MAX_EXPLICIT_FOREIGN_KEYS_RATIO,"1.0").trim());
		disjoint_unions = Double.parseDouble(synthegrateProperties.getProperty(Constants.MAX_DISJOINT_UNIONS_RATIO,"1.0").trim());
		reuse_join_attributes = Double.parseDouble(synthegrateProperties.getProperty(Constants.RATIO_REUSE_JOIN_ATTRIBUTES,"0.0").trim());
		
		create_unions_first = Boolean.parseBoolean(synthegrateProperties.getProperty(Constants.CREATE_UNIONS_FIRST,"true").trim());
		create_joins_first = Boolean.parseBoolean(synthegrateProperties.getProperty(Constants.CREATE_JOINS_FIRST,"false").trim());
		
		star_join = Boolean.parseBoolean(synthegrateProperties.getProperty(Constants.STAR_JOIN_TYPE,"false").trim());
		chain_join = Boolean.parseBoolean(synthegrateProperties.getProperty(Constants.CHAIN_JOIN_TYPE,"true").trim());
		balanced_join = Boolean.parseBoolean(synthegrateProperties.getProperty(Constants.BALANCED_JOIN_TYPE,"false").trim());

		/*
		 * DATABASE PROPERTIES & OUTPUT FILES 
		 */
		WriteUtils.set_properties(synthegrateProperties);
		
	}

	public static long getMax_cardinality() {
		return max_cardinality;
	}

}
