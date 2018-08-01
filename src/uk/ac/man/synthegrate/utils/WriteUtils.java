package uk.ac.man.synthegrate.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import uk.ac.man.synthegrate.generator.MappingNode;
import uk.ac.man.synthegrate.generator.Synthegrate;
import uk.ac.man.synthegrate.schema_components.Attribute;
import uk.ac.man.synthegrate.schema_components.DBSchema;
import uk.ac.man.synthegrate.schema_components.InclusionDependency;
import uk.ac.man.synthegrate.schema_components.KeyConstraint;
import uk.ac.man.synthegrate.schema_components.ProfileData;
import uk.ac.man.synthegrate.schema_components.Relation;

public class WriteUtils {

	static final String DEFAULT_FOLDER = "resources/top_down_scale_scenarios";
	static final String PROFILE_FILE_NAME = "profiling_input.xml";
	static final String DISTRACTION_FILE_NAME = "distractions.in";
	static final String DATASOURCE_FILE_NAME = "datasource.vada";
	static final String MATCH_FILE_NAME = "match.vada";
	static final String EXPECTED_SQL_MAPPING = "expected_mapping.sql";
	static final String SOURCE_TARGET_PAIRS_FILE_NAME = "sourceTargetPairs.vada";
	static final String DATABASE_PREFIX = "jdbc:postgresql://";
	
	static final String DATABASE_NAME = "tests_generator_chain_join";
	
	static final String DATABASE_URL = "localhost:5432/"+DATABASE_NAME;
	static final String DATABASE_URL_SPICY = "localhost:5432/"+DATABASE_NAME+"_spicy_target";
	static final String DATABASE_NAME_SPICY = DATABASE_NAME+"_spicy_target";
	static final String DATABASE_DRIVER = "JDBC_POSTGRES";
	static final String SPICY_MAPTASK = "spicy_maptask.xml";
	static String SPICY_MAPPING = "spicy_mapping";
	
	

	public static void write_scenario(ArrayList<DBSchema> source_dbSchemas, DBSchema target_dbSchema,
			String folder_path, String scenario_name, ProfileData profileData, MappingNode root_node) {

//		String file_path = folder_path + "/mto1_tests/run_files/scenario_" + scenario_name;
		String file_path = folder_path + "/" + scenario_name;
		File file = new File(file_path);
		if (!file.exists()) {
			if (file.mkdirs()) {
				System.out.println(file_path + " directory is created!");
			} else {
				System.out.println("Failed to create " + file_path + " directory!");
			}
		}

		ArrayList<DBSchema> target_param = new ArrayList<>();
		target_param.add(target_dbSchema);

		// write the files for the target
		write_schema_sql_files(target_param, file_path);
		write_schema_sql_files(source_dbSchemas, file_path);
		System.out.println("[4 out of 12 file] Wrote target and source SQL/datafiller files...");

		write_profile_data_file(profileData, file_path);
		System.out.println("[5 out of 12 file] Profile data file...");
		
		write_match_file(source_dbSchemas, file_path);
		System.out.println("[6 out of 12 file] Match data file...");
		
		write_datasources_file(source_dbSchemas, target_dbSchema, file_path);
		System.out.println("[7 out of 12 file] Datasource data file...");
		
		write_source_target_pairs(source_dbSchemas, target_dbSchema, file_path);
		System.out.println("[8 out of 12 file] Source-target pairs data file...");
		
		write_expected_sql_query(root_node,file_path);
		System.out.println("[9 out of 12 file] Expected SQL file...");
		
		write_spicy_scenario(file_path, source_dbSchemas, target_dbSchema );
		System.out.println("[12 out of 12 file] Spicy task files...");
	}
	
	/***
	 * Write in an SQL file the expected query (mapping).
	 */
	private static void write_expected_sql_query(MappingNode root_node,String file_path) {
		
		if (root_node==null)
			return;
				
		BufferedWriter writer;
		try {
			
			writer = new BufferedWriter(new FileWriter(file_path + "/" + Synthegrate.getUnionsJoinsString()+"_"+EXPECTED_SQL_MAPPING));
			writer.write("-- Final SQL mapping: \n");
			writer.write(root_node.toString());
			writer.write("\n/*"+root_node.getSimplifiedMapping(1)+"*/");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the file where it states where to find the input schema (database url, db driver)
	 * @param dbSchemas
	 * @param targetSchema
	 * @param file_path
	 */
	private static void write_datasources_file(ArrayList<DBSchema> dbSchemas, DBSchema targetSchema, String file_path) {
		try {
			// Writer for datasource file
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(file_path + "/" + DATASOURCE_FILE_NAME));
			writer.write("datasource("+targetSchema.getName()+","+DATABASE_URL+","+DATABASE_DRIVER+",null).\n");
		
			for (DBSchema schema : dbSchemas) {

				writer.write("datasource("+schema.getName()+","+DATABASE_URL+","+DATABASE_DRIVER+",null).\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Error writing schemas in datasource file!");
			e.printStackTrace();
		}


	}

	private static void write_source_target_pairs(ArrayList<DBSchema> dbSchemas, DBSchema targetSchema, String file_path){
		try {
			// Writer for datasource file
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(file_path + "/" + SOURCE_TARGET_PAIRS_FILE_NAME));
			
			for (DBSchema schema : dbSchemas) {

				writer.write("map_schemas("+schema.getName()+","+targetSchema.getName()+").\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Error writing source-target pairs file!");
			e.printStackTrace();
		}

	}
	
	private static void write_match_file(ArrayList<DBSchema> dbSchemas, String folder_path) {

		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(folder_path + "/" + MATCH_FILE_NAME));
			for (DBSchema schema : dbSchemas) {

				ArrayList<Relation> relations = schema.getRelations();

				if (relations.size() > 0) {
					
					for (Relation relation:relations){
						ArrayList<Attribute> attributes = relation.getAttributes();
					
						if (attributes==null||attributes.size()<1)
							continue;
					
						for (Attribute attribute:attributes){
							
							if (attribute.isTarget_attribute())
								//the confidence is 1.0
								writer.write("Match("+attribute.getFullName()+","+attribute.getMatched_target_attribute().getFullName()+",1.0).\n");
						}
					}
				
				}
			}
			writer.close();

		} catch (IOException e) {
			System.err.println("Error writing schemas in SQL/datafiller file!");
			e.printStackTrace();
		}

		
	}

	private static void write_schema_sql_files(ArrayList<DBSchema> dbSchemas, String folder_path) {

		try {

			for (DBSchema schema : dbSchemas) {

				ArrayList<Relation> relations = schema.getRelations();

				if (relations.size() > 0) {
					// Writer for SQL files
					BufferedWriter writer;
					writer = new BufferedWriter(new FileWriter(folder_path + "/schema_" + schema.getName() + ".sql"));
					writer.write("DROP SCHEMA IF EXISTS " + schema.getName() + " CASCADE;\n");
					writer.write("CREATE SCHEMA " + schema.getName() + ";\n");

					// Writer for datafiller files
					BufferedWriter dfwriter;
					dfwriter = new BufferedWriter(
							new FileWriter(folder_path + "/datafiller_" + schema.getName() + ".sql"));
					write_datafiller_dictionaries(dfwriter);
					write_schema(writer, dfwriter, schema);
					writer.close();
					dfwriter.close();
				}
			}

		} catch (IOException e) {
			System.err.println("Error writing schemas in SQL/datafiller file!");
			e.printStackTrace();
		}

	}
	
	private static void write_datafiller_dictionaries(BufferedWriter dfwriter) throws IOException{
		
		for (Dictionary dictionary:Constants.dictionaries){
			dfwriter.write("--df "+dictionary.name+": word=./dictionaries/"+dictionary.filename+" \n ");
		}
		
	}

	private static void write_schema(BufferedWriter writer, BufferedWriter dfwriter, DBSchema schema)
			throws IOException {
		ArrayList<Relation> relations = schema.getRelations();
		Collections.sort(relations);

		for (Relation relation : relations) {
			write_relation_sql_without_constraints(writer, dfwriter, relation);
			writer.flush();
			//System.out.println("Written relation"+ relation.getNameWSchema());
		}
		
		if (dfwriter!=null) {
			System.out.println("Writing datafiller files ..");
			for (Relation relation : relations) {
				write_datafiller_sql_relation(dfwriter, relation);
				dfwriter.flush();
			}
		}
		
		System.out.println("Writing schema constraints..");
		for (Relation relation : relations) {
			write_relation_constraints(writer, dfwriter, relation);
			writer.flush();
		}

	}

	private static void write_relation_sql_without_constraints(BufferedWriter writer, BufferedWriter dfwriter, Relation relation) throws IOException {
		if (!relation.isWritten() && relation.getParentSchema() != null) {
			String create = "DROP TABLE IF EXISTS "+relation.getNameWSchema()+" CASCADE ;\n"+
					"CREATE TABLE " + relation.getNameWSchema() + " (\n";
					ArrayList<Attribute> attributes = relation.getAttributes();

					for (int i = 0; i < attributes.size() - 1; i++) {
						Attribute attribute = attributes.get(i);
						create += attribute.getName() + " " + attribute.getType();
						if (attribute.is_unique())
							if (!attribute.is_primary_key())
								create += " UNIQUE";
						if (attribute.isNot_null())
							create += " NOT NULL";

						if (attribute.is_primary_key())
							create += " PRIMARY KEY";
						
						create += ", ";
						if (attribute.isTarget_attribute())
							create += "-- target attribute = "+attribute.getMatched_target_attribute().getNameWRelation();
						create += "\n";

					}
					Attribute last_attribute = attributes.get(attributes.size() - 1);
					create += last_attribute.getName() + " "
							+ last_attribute.getType();
					if (last_attribute.is_unique())
						if (last_attribute.is_primary_key())
							create += " UNIQUE";
					
					if (last_attribute.isNot_null())
						create += " NOT NULL";
					
					if (last_attribute.is_primary_key())
						create += " PRIMARY KEY";
					
					if (last_attribute.is_foreign_key())
						create += " REFERENCES " + last_attribute.getPrimary_key_attribute().getParentRelation().getNameWSchema()
								+ "(" + last_attribute.getPrimary_key_attribute().getName() + ")";
					
					if (last_attribute.isTarget_attribute())
						create += "-- target attribute = "+last_attribute.getMatched_target_attribute().getNameWRelation()+"\n";
					
					create += ");\n\n";

					writer.write(create);
					relation.setWritten(true);
		}
		
		
	}
	
	private static void write_relation_constraints(BufferedWriter writer, BufferedWriter dfwriter, Relation relation) throws IOException {
		
		
		if (!relation.isWritten())
			return;
		
		String create = "";
		int fk_count = 0;
		
		ArrayList<Attribute> attributes = relation.getAttributes();
		
		for (int i = 0; i < attributes.size() - 1; i++) {
			Attribute attribute = attributes.get(i);
			fk_count++;
			
		if (attribute.is_foreign_key())
			create += "\n\nADD CONSTRAINT FK"+fk_count+"_"+attribute.getName()+"_"+attribute.getPrimary_key_attribute().getName()
			+"FOREIGN KEY ("+attribute.getName()+")\n"
			+"REFERENCES " + attribute.getPrimary_key_attribute().getParentRelation().getNameWSchema()
					+ "(" + attribute.getPrimary_key_attribute().getName() + ") MATCH SIMPLE\n"
					+"    ON UPDATE NO ACTION\n"  
					+"    ON DELETE NO ACTION;";
		}
		writer.write(create);
	}
	
	@SuppressWarnings("unused")
	private static void write_relation_sql(BufferedWriter writer, BufferedWriter dfwriter, Relation relation)
			throws IOException {

		if (!relation.isWritten() && relation.getParentSchema() != null) {
			if (relation.containsFK()) {
				ArrayList<Attribute> fks = relation.getFKs();
				for (Attribute fk : fks) {
					/*
					 * we need to write the PK relations first so that we don't
					 * get an invalid SQL file - can't reference something that
					 * doesn't exist
					 */
					Relation pk_relation = fk.getPrimary_key_attribute().getParentRelation();
					write_relation_sql(writer, dfwriter, pk_relation);

				}
			}
			/*
			 * After writing all the tables referenced by this relation, we can
			 * write it.
			 */
			
			write_datafiller_sql_relation(dfwriter, relation);

			String create = "DROP TABLE IF EXISTS "+relation.getNameWSchema()+" CASCADE ;\n"+
			"CREATE TABLE " + relation.getNameWSchema() + " (\n";
			ArrayList<Attribute> attributes = relation.getAttributes();

			for (int i = 0; i < attributes.size() - 1; i++) {
				Attribute attribute = attributes.get(i);
				create += attribute.getName() + " " + attribute.getType();
				if (attribute.is_unique())
					if (!attribute.is_primary_key())
						create += " UNIQUE";
				if (attribute.isNot_null())
					create += " NOT NULL";

				if (attribute.is_primary_key())
					create += " PRIMARY KEY";
				
				if (attribute.is_foreign_key())
					create += " REFERENCES " + attribute.getPrimary_key_attribute().getParentRelation().getNameWSchema()
							+ "(" + attribute.getPrimary_key_attribute().getName() + ")";
				create += ", ";
				if (attribute.isTarget_attribute())
					create += "-- target attribute = "+attribute.getMatched_target_attribute().getNameWRelation();
				create += "\n";

			}
			
			Attribute last_attribute = attributes.get(attributes.size() - 1);
			create += last_attribute.getName() + " "
					+ last_attribute.getType();
			if (last_attribute.is_unique())
				if (last_attribute.is_primary_key())
					create += " UNIQUE";
			
			if (last_attribute.isNot_null())
				create += " NOT NULL";
			
			if (last_attribute.is_primary_key())
				create += " PRIMARY KEY";
			
			if (last_attribute.is_foreign_key())
				create += " REFERENCES " + last_attribute.getPrimary_key_attribute().getParentRelation().getNameWSchema()
						+ "(" + last_attribute.getPrimary_key_attribute().getName() + ")";
			
			if (last_attribute.isTarget_attribute())
				create += "-- target attribute = "+last_attribute.getMatched_target_attribute().getNameWRelation()+"\n";
			
			create += ");\n\n";

			writer.write(create);

			relation.setWritten(true);
		}
	}

	static ArrayList<String> prefixes = new ArrayList<>();
	private static void write_datafiller_sql_relation(BufferedWriter writer, Relation relation) throws IOException {
		
		if (writer==null) {
			return;
		}
		String create = "";
		long cardinality = ((relation.getCardinality() == -1 || relation.getCardinality() == 0) ? 1
				: relation.getCardinality());
		create += "CREATE TABLE " + relation.getNameWSchema() + " ( --df: size=" + cardinality + " \n";
		ArrayList<Attribute> attributes = relation.getAttributes();
		
		for (int i = 0; i < attributes.size() - 1; i++) {
			Attribute attribute = attributes.get(i);
			
			create += attribute.getName() + " " + attribute.getType();
			create += " UNIQUE";
			create += " NOT NULL";
			
			create += ",";
			if (attribute.getParameters() != null)
				create += "--df: use="+attribute.getParameters().getDictionary().name+" offset=" + attribute.getParameters().getOffset() + " step="
						+ attribute.getParameters().getStep() + " shift=" + attribute.getParameters().getShift()
						+ " size=" + attribute.getParameters().getSize();
			else {
				String prefix = Utils.generateRandomString(10);
				while (prefixes.contains(prefix))
					prefix = Utils.generateRandomString(10);
				prefixes.add(prefix);
				create +="--df: prefix="+ prefix;
			}
			create += "\n";
			
		}
		
		Attribute last_attribute = attributes.get(attributes.size() - 1);
		create += last_attribute.getName() + " " + last_attribute.getType();
		create += " UNIQUE";
		create += " NOT NULL";
		if (last_attribute.getParameters() != null)
			create += "--df: use="+last_attribute.getParameters().getDictionary().name+" offset=" + last_attribute.getParameters().getOffset() + " step="
					+ last_attribute.getParameters().getStep() + " shift=" + last_attribute.getParameters().getShift()
					+ " size=" + last_attribute.getParameters().getSize() + "\n";
		else{
			String prefix = Utils.generateRandomString(2);
			while (prefixes.contains(prefix))
				prefix = Utils.generateRandomString(2);
			prefixes.add(prefix);
			create +="--df: prefix="+ prefix;
		}
		create += "\n);\n\n";
		
		writer.write(create);
	}

	private static void write_profile_data_file(ProfileData profileData, String folder_path) {
		System.out.println("Writing "+profileData.getInds().size()+" inclusion dependencies\n");
		System.out.println("Writing "+profileData.getKeys().size()+" candiate keys \n");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(folder_path + "/" + PROFILE_FILE_NAME));

			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write("<profile-data>\n");

			// Write Candidate Keys
			writeCandidateKeys(writer, profileData.getKeys());

			// Write (partial) INDs
			writeINDs(writer, profileData.getInds());

			writer.write("</profile-data>");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void writeINDs(BufferedWriter writer, ArrayList<InclusionDependency> inds) throws IOException {
		writer.write("\t<inclusion-dependencies>\n");
		
		for (InclusionDependency ind:inds){
			writer.write("\t\t<inclusion-dependency>\n");
			if (ind.is_distraction())
				writer.write("\t\t\t<!-- DISTRACTION  -->\n");
			//is-included
			writer.write("\t\t\t<is-included distinct_values = \""+ ind.getIncluded_distinct_values()+"\">\n");
			writer.write("\t\t\t\t<attribute schema = \""+ind.getIncludedAttribute().getParentRelation().getParentSchema().getName()
					+"\" relation = \""+ind.getIncludedAttribute().getParentRelation().getName()
					+"\" name = \""+ ind.getIncludedAttribute().getName() +"\"/>\n");
			writer.write("\t\t\t</is-included>\n");
			
			//includes
			writer.write("\t\t\t<includes>\n");
			writer.write("\t\t\t\t<attribute schema = \""+ind.getIncludingAttribute().getParentRelation().getParentSchema().getName()
					+"\" relation =\""+ind.getIncludingAttribute().getParentRelation().getName()
					+"\" name = \""+ ind.getIncludingAttribute().getName() +"\"/>\n");
			writer.write("\t\t\t</includes>\n");
			
			//coefficient
			writer.write("\t\t\t<coefficient value = \""+ind.getCoefficient()+"\"></coefficient>\n");
			
			writer.write("\t\t</inclusion-dependency>\n");
		
		}
		
		writer.write("\t</inclusion-dependencies>");
		
	}

	private static void writeCandidateKeys(BufferedWriter writer, ArrayList<KeyConstraint> keys) throws IOException {
		writer.write("\t<keyCandidates>\n");

		for (KeyConstraint key : keys) {
			System.out.println(key.toString());
			if (key.getKeyAttributes().size() == 1) {
				writer.write("\t\t<attribute schema = \""
						+ key.getKeyAttributes().get(0).getParentRelation().getParentSchema().getName() + "\" relation = \""
						+ key.getKeyAttributes().get(0).getParentRelation().getName() + "\" name = \""
						+ key.getKeyAttributes().get(0).getName() + "\"/>\n");
			} else {
				writer.write("\t\t<multiple>\n");
				for (Attribute a : key.getKeyAttributes()) {
					writer.write("\t\t<attribute schema = \"" + a.getParentRelation().getParentSchema().getName()
							+ "\" relation = \"" + a.getParentRelation().getName() + "\" name = \"" + a.getName() + "\"/>\n");
				}
				writer.write("\t\t</multiple>\n");
			}
		}

		writer.write("\t</keyCandidates>\n");
	}

	public static void write_distraction (String distraction_string,String folder_path, String scenario_name){
		
		String file_path = DEFAULT_FOLDER;
//		if (folder_path==null||scenario_name==null)
//			file_path = DEFAULT_FOLDER;
//		else
//			file_path = folder_path + "/" + scenario_name;
		
		File file = new File(file_path);
		if (!file.exists()) {
			if (file.mkdirs()) {
				System.out.println(file_path + " directory is created!");
			} else {
				System.out.println("Failed to create " + file_path + " directory!");
			}
		}
		
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file_path + "/d_"+ DISTRACTION_FILE_NAME));
			
			writer.append(distraction_string+"\n");
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void write_headers(String output_file_path, String toWrite, boolean ifAppend) {
		
		if (output_file_path==null)
			return;
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		

		try {

			fw = new FileWriter(output_file_path,ifAppend);
			bw = new BufferedWriter(fw);
			bw.write(toWrite+"\n");
			bw.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
    public static void write_plot_headers(String output_file_path, String toWrite, boolean ifAppend) {
		
		if (output_file_path==null)
			return;
		
		String plot_file = output_file_path+"_plot.csv";
		
		BufferedWriter bw_plot = null;
		FileWriter fw_plot = null;

		try {
			fw_plot = new FileWriter(plot_file,ifAppend);
			bw_plot = new BufferedWriter(fw_plot);
			bw_plot.write(toWrite+"\n");
			bw_plot.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (bw_plot != null)
					bw_plot.close();

				if (fw_plot != null)
					fw_plot.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * The maptask looks only in the public schema.
	 * @param output_file_path
	 * @param dbSchemas
	 * @param target_dbSchema
	 */
	public static void write_spicy_scenario(String output_file_path, ArrayList<DBSchema> dbSchemas,
			DBSchema target_dbSchema) {

		// Spicy cannot handle more than 1 source schema and there is no point in
		// creating the maptask if there are no source schemas
		if (dbSchemas.size() > 1 || dbSchemas.size() == 0) {
			System.out.println("Spicy files were not created because the number source schemas is 0 or > 1.");
			return;
		}

		DBSchema source_schema = dbSchemas.get(0);
		ArrayList<Relation> relations = source_schema.getRelations();
		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(output_file_path + "/" + SPICY_MAPTASK));
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write("<mappingtask>\n" 
					+ "  <config>\n" 
					+ " <rewriteSubsumptions>true</rewriteSubsumptions>\n"
					+ "    <rewriteCoverages>true</rewriteCoverages>\n"
					+ "    <rewriteSelfJoins>true</rewriteSelfJoins>\n" 
					+ "    <rewriteEGDs>true</rewriteEGDs>\n"
					+ "    <sortStrategy>-1</sortStrategy>\n" 
					+ "    <skolemTableStrategy>-1</skolemTableStrategy>\n"
					+ "    <useLocalSkolem>false</useLocalSkolem>\n" 
					+ "  </config>\n" 
					+ "  <source>\n"
					+ "    <type>relational</type>\n" 
					+ "    <relational>\n"
					+ "      <driver>org.postgresql.Driver</driver>\n" 
					+ " <uri>" + DATABASE_PREFIX + DATABASE_URL
					+ "</uri>\n" + " <login>lara</login>\n" + " <password>postgres</password> \n" + " </relational>\n"
					+ "    <inclusions />\n" 
					+ "    <exclusions />\n" 
					+ "    <duplications />\n"
					+ "    <functionalDependencies />\n" 
					+ "    <selectionConditions />\n" );
			
			String joinConditions = " <joinConditions>\n";
			for (Relation relation:relations) {
				ArrayList<Attribute> fks = relation.getFKs();
				
				for (Attribute a: fks) {
					Attribute referencedAttribute = a.getPrimary_key_attribute();
					joinConditions+= " <joinCondition>\n";
					joinConditions+= " <join>\n";
					joinConditions+= "<from>"+DATABASE_NAME+"."+relation.getName()+"."+relation.getName()+"Tuple."+a.getName()+"</from>\n";
					joinConditions+= "<to>"+DATABASE_NAME+"."+referencedAttribute.getParentRelation().getName()
							+"."+referencedAttribute.getParentRelation().getName()+"Tuple."
							+referencedAttribute.getName()+"</to>\n"
							+"</join>";
					joinConditions+= " <foreignKey>true</foreignKey>\n";
					joinConditions+= "  <mandatory>false</mandatory>\n";
					joinConditions+= "</joinCondition>\n";
				}
				
			}
			joinConditions += " </joinConditions>\n";
			writer.write( joinConditions);
					writer.write( 
							"  </source> \n" + " <target>\n" + "    <type>relational</type>\n" + "    <relational>\n"
					+ "      <driver>org.postgresql.Driver</driver>\n" + "      <uri> " + DATABASE_PREFIX
					+ DATABASE_URL_SPICY + "</uri>\n" + " <login>lara</login>\n"
					+ "      <password>postgres</password>\n" + "    </relational>\n" + "    <inclusions />\n"
					+ "    <exclusions />\n" + "    <duplications />\n" + "    <functionalDependencies />\n"
					+ "    <selectionConditions />\n" + "    <joinConditions />\n" + "  </target> \n");

			writer.write("<correspondences>\n");
			

			if (relations.size() > 0) {

				for (Relation relation : relations) {
					
					ArrayList<Attribute> attributes = relation.getAttributes();

					if (attributes == null || attributes.size() < 1)
						continue;

					for (Attribute attribute : attributes) {

						if (attribute.isTarget_attribute())
							// the confidence is 1.0
							write_spicy_correspondence(writer, relation.getName(), attribute.getName(),
									attribute.getMatched_target_attribute().getParentRelation().getName(),
									attribute.getMatched_target_attribute().getName());
					}
				}

			}
			writer.write("</correspondences>\n");
			writer.write("</mappingtask>");
			writer.flush();
			writer.close();
			
			write_spicy_target_sql(output_file_path,target_dbSchema);
			write_spicy_source_sql(output_file_path,source_schema);
			
			SPICY_MAPPING = Synthegrate.getUnionsJoinsString()+"_spicy_mapping.sql";
			writer = new BufferedWriter(new FileWriter(output_file_path + "/" + SPICY_MAPPING));
			writer.write("SELECT "+String.join(",", target_dbSchema.getRelations().get(0).getAttributeNames())
				 		+" FROM "+target_dbSchema.getName()+"."+target_dbSchema.getRelations().get(0).getName());
			writer.flush();
			writer.close();

		} catch (IOException e) {
			System.err.println("Error writing Spicy maptask files!");
			e.printStackTrace();
		}

	}

	private static void write_spicy_source_sql(String output_file_path, DBSchema source_schema) {
		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(output_file_path + "/"+source_schema.getName()+"_spicy_source.sql"));
			source_schema.setRelationsWritten(false);
			source_schema.setName("public");
			write_schema(writer, null, source_schema);
			
			writer.close();

		} catch (IOException e) {
			System.err.println("Error writing Spicy source sql file!");
			e.printStackTrace();
		}
	}

	private static void write_spicy_target_sql(String output_file_path, DBSchema target_dbSchema) {
		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(output_file_path + "/"+target_dbSchema.getName()+"_spicy_target.sql"));
			target_dbSchema.setRelationsWritten(false);
			target_dbSchema.setName("target");
			write_schema(writer, null, target_dbSchema);
			writer.flush();
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(output_file_path + "/spicy_target_public_schema.sql"));
			target_dbSchema.setRelationsWritten(false);
			target_dbSchema.setName("public");
			write_schema(writer, null, target_dbSchema);
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			System.err.println("Error writing Spicy target sql files!");
			e.printStackTrace();
		}
		
	}

	private static void write_spicy_correspondence(BufferedWriter writer, String source_relation,
			String source_attribute, String target_relation, String target_attribute) throws IOException {

		writer.write("<correspondence>\n");
		writer.write("<source-paths>\n");
		writer.write("<source-path>" + DATABASE_NAME + "." + source_relation + "." + source_relation + "Tuple."
				+ source_attribute + "</source-path>\n");
		writer.write("</source-paths>\n");
		writer.write("<target-path>" + DATABASE_NAME_SPICY + "." + target_relation + "." + target_relation + "Tuple."
				+ target_attribute + "</target-path>\n");
		writer.write("<transformation-function>" + DATABASE_NAME + "." + source_relation + "." + source_relation
				+ "Tuple." + source_attribute + "</transformation-function>\n");

		writer.write(" <confidence>" + "1.0" + "</confidence>\n");
		writer.write("</correspondence>\n");

		writer.flush();

	}
	
	public static String getSpicyMappingSQL() {
		return SPICY_MAPPING;
	}
}
