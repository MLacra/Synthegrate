package uk.ac.man.synthegrate.utils;

import java.io.File;
import java.util.ArrayList;

public class Constants {

	public static final String JOIN_OPERATION = "JOIN";
	public static final String UNION_OPERATION = "UNION";
	public static final String FULL_OUTER_JOIN_OPERATION = "FULL OUTER JOIN";
	public static final String ON = "ON";
	
	public static final Integer JOIN_OPERATION_INT = 1;
	public static final Integer UNION_OPERATION_INT = 2;
	public static final Integer FULL_OUTER_JOIN_OPERATION_INT = 3;
	
	public static final int TRUE_NEGATIVE = 0;
	public static final int TRUE_POSITIVE = 1;
	public static final int FALSE_POSITIVE = 2;
	public static final int FALSE_NEGATIVE = 3;
	
	public static final String NULL = "NULL";
	
	public static ArrayList<Dictionary> dictionaries =  new ArrayList<>();
	
	/***** OUTPUT FILES ****/
	public static String OUTPUT_FOLDER ="output.folder";
	public static String PROFILE_DATA_FILENAME = "output.profile";
	public static String MATCHES_FILENAME = "output.match";
	public static String DATASOURCE_FILENAME = "output.sources";
	public static String SOURCE_TARGET_PAIRS_FILENAME = "output.source_target";
	public static String EXPECTED_MAPPING_FILENAME = "output.expected_mapping";
	public static String SPICY_MAPTASK_FILENAME = "output.spicy_maptask";
	
	/***** SCENARIO PROPERTIES ****/
	public static final String MAX_SOURCE_SCHEMAS_NUMBER = "scenario.schemas_number";
	public static final String MAX_SOURCE_RELATIONS ="scenario.max_source_relations";
	public static final String MIN_CARDINALITY = "scenario.min_cardinality";
	public static final String MAX_CARDINALITY = "scenario.max_cardinality";
	public static final String MIN_ARITY = "scenario.min_arity";
	public static final String MAX_ARITY = "scenario.max_arity";
	public static final String MAX_TRIES = "scenario.tries";
	public static final String MAX_TARGET_RELATIONS = "scenario.target_relations";
	public static final String TARGET_PRIMARY_KEYS_NUMBER = "scenario.target_pks_number";
	public static final String MAX_JOINS_NUMBER = "scenario.max_joins_number";
	public static final String MAX_UNIONS_NUMBER = "scenario.max_unions_number";
	public static final String MAX_EXPLICIT_FOREIGN_KEYS_RATIO = "scenario.fks_ratio";
	public static final String MAX_DISJOINT_UNIONS_RATIO = "scenario.disjoint_unions";
	public static final String RATIO_REUSE_JOIN_ATTRIBUTES = "scenario.reuse_join_attributes";
	public static final String CREATE_UNIONS_FIRST = "scenario.create_unions_first";
	public static final String CREATE_JOINS_FIRST = "scenario.create_joins_first";

	//JOIN TYPES
	public static final String STAR_JOIN_TYPE = "scenario.star_join";
	public static final String CHAIN_JOIN_TYPE = "scenario.chain_join";
	public static final String BALANCED_JOIN_TYPE = "scenario.balanced_join";
	
	//DICTIONARIES FOR POPULATING THE TUPLES
	public static final String DICTIONARIES_FOLDER_PATH = "input.dictionaries_folder";
	
	/***** DATABASE PROPERTIES ****/
	public static final String DATABASE_USER = "database.user";
	public static final String DATABASE_PASSWORD = "database.password";
	public static final String DATABASE_LOCATION = "database.location";
	public static final String DATABASE_PORT= "database.port";
	public static final String DATABASE_NAME= "database.name";
	public static final String DATABASE_SCHEMA= "database.schema";
	public static final String DATABASE_JDBC_DRIVER= "database.jdbc_driver";
	public static final String DATABASE_JDBC_PREFIX= "database.jdbc_prefix";

	public static void  setupDictionaries(String dictionariesFolder){
		
		File folder = new File(dictionariesFolder);
		
		if (!folder.exists())
		{
			System.err.println("Dictionaries folder not found: "+dictionariesFolder);
			System.exit(1);
		}
		
		Utils.DICTIONARIES_FOLDER_PATH = dictionariesFolder;
		ArrayList<String> dictionary_names = Utils.getFileNames(dictionariesFolder);
		
		for (String dictionary_name:dictionary_names) {
			dictionaries.add(Utils.readDictionary(dictionary_name));
		}
		
	}

	
	public static void addDictionary(Dictionary newDictionary) {
		dictionaries.add(newDictionary);
	}
	
	
	
}