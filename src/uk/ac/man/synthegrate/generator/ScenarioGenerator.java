package uk.ac.man.synthegrate.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import uk.ac.man.synthegrate.schema_components.Attribute;
import uk.ac.man.synthegrate.schema_components.DBSchema;
import uk.ac.man.synthegrate.schema_components.FKconstraint;
import uk.ac.man.synthegrate.schema_components.InclusionDependency;
import uk.ac.man.synthegrate.schema_components.KeyConstraint;
import uk.ac.man.synthegrate.schema_components.ProfileData;
import uk.ac.man.synthegrate.schema_components.Relation;
import uk.ac.man.synthegrate.utils.Constants;
import uk.ac.man.synthegrate.utils.SchemaComponentsUtils;
import uk.ac.man.synthegrate.utils.Utils;
import uk.ac.man.synthegrate.utils.WriteUtils;

public class ScenarioGenerator {

	int max_source_relations;
	int target_relations_number;
	int target_pks_number;

	long min_cardinality; // minimum number of tuples in a relation
	long max_cardinality; // maximum number of tuples in a relation
	int joins_number;
	int unions_number;
	int disjoint_unions; //how many unionable tables should be disjoint
	int reuse_join_attributes; //how many times should reuse an attribute when creating a join 

	// e.g. if the joins should have other ways of joining
	// if = 0 then the joins will have just one way of joining
	double distractions;

	// a join can be produced if there is an explicit FK relationship or not
	// if fks_ratio = 1.0; then all joins will be based on an explicit FK
	// relationship
	double max_fks_ratio;
	// keep it as a global as we need it in several methods
	int created_fks = 0;

	public static int tries;
	long min_arity;
	long max_arity;
	int schemas_number;
	
	//used to keep track of the created elements
	int created_joins = 0;
	int created_unions = 0;
	int created_disjoint_unions = 0;
	int created_reused_attribute_joins = 0;
	int created_relations = 0;
	
	boolean create_unions_first;
	boolean create_joins_first;
	
	boolean star_join = false;
	boolean chain_join= false;
	boolean balanced_join = false;

	String folder_path = "";

	

	ArrayList<Integer> OPERATIONS_LIST = new ArrayList<>();

	ArrayList<MappingNode> leaves_nodes = new ArrayList<>();
	
	public ScenarioGenerator(int max_src_relations, int target_relations_number, int target_pks_number, long min_cardinality,
			long max_cardinality, int joins_number, int unions_number, double distractions, double fks_ratio, int ttries, long min_arity, long max_arity, 
			int schemas_number, double disj_unions, double reuse_join_attributes_ratio, boolean pcreate_unions_first, boolean pcreate_joins_first, boolean pstar_join, boolean pchain_join, boolean pbalanced_join) {
		super();
		this.max_source_relations = max_src_relations;
		this.target_relations_number = target_relations_number;
		this.target_pks_number = target_pks_number;
		this.max_cardinality = max_cardinality;
		this.min_cardinality = min_cardinality;
		this.joins_number = joins_number;
		this.unions_number = unions_number;
		this.distractions = distractions;
		this.max_fks_ratio = fks_ratio;
		tries = ttries;
		this.min_arity = min_arity;
		this.max_arity = max_arity;
		this.schemas_number = schemas_number;
		this.disjoint_unions = (int)Math.round(disj_unions*unions_number);
		this.reuse_join_attributes = (int)Math.round(reuse_join_attributes_ratio*joins_number);
		this.create_unions_first = pcreate_unions_first;
		this.create_joins_first = pcreate_joins_first;
		this.star_join = pstar_join;
		this.chain_join = pchain_join;
		this.balanced_join = pbalanced_join;

		OPERATIONS_LIST = new ArrayList<>();
		OPERATIONS_LIST.add(Constants.JOIN_OPERATION_INT);
		OPERATIONS_LIST.add(Constants.UNION_OPERATION_INT);

		System.out.println("Generating scenario with parameters:");
		System.out.println("max_source_relations = "+ max_source_relations);
		System.out.println("target_relations = "+ target_relations_number);
		System.out.println("target_pks_number = "+ target_pks_number);
		System.out.println("max_cardinality = "+ max_cardinality);
		System.out.println("min_cardinality = "+ min_cardinality);
		System.out.println("joins_number = "+ joins_number);
		System.out.println("reuse_join_attributes = "+ reuse_join_attributes);
		System.out.println("unions_number = "+ unions_number);
		System.out.println("disjoint_unions_number = "+ disjoint_unions);
		System.out.println("distractions = "+ distractions);
		System.out.println("max_fks_ratio = "+ max_fks_ratio);
		System.out.println("min_arity = "+ min_arity);
		System.out.println("max_arity = "+ max_arity);
		System.out.println("schemas_number = "+ schemas_number);
		System.out.println("tries = "+ tries);
		
		created_joins = 0;
		created_unions = 0;
		created_relations = 0;
		
	}

	public String getFolder_path() {
		return folder_path;
	}

	public void setFolder_path(String folder_path) {
		this.folder_path = folder_path;
		
	}
	
	public int getJoins_number() {
		return joins_number;
	}

	public void setJoins_number(int joins_number) {
		this.joins_number = joins_number;
	}

	public int getUnions_number() {
		return unions_number;
	}

	public void setUnions_number(int unions_number) {
		this.unions_number = unions_number;
	}

	public void generate_scenario() {
		System.out.println("1. Generating target schema...");
		DBSchema target_schema = generateTargetSchema();
		System.out.println("1. Generated target schema...");

		System.out.println("1. Generating source schemas...");
		ArrayList<DBSchema> source_schemas = generateSourceSchemas(target_schema);
		System.out.println("2. Generated source schemas with "+created_relations+"(max = "+max_source_relations+") source relations in total ("+created_joins+" joins, "+created_unions+" unions)...");
			
		// Add a random number of attributes so that the relations don't have
		// the exact relation schema
		System.out.println("\n4. Generating random attributes ...");
		long count_relations = generateRandomAttributes(source_schemas);
		System.out.println("4. Generated random attributes in the source relations (not needed in the target)...");

		
		System.out.println("\n5. Generating profile data... Please wait..");
		ProfileData profileData = generateProfileData(source_schemas);
		System.out.println("5. Generated profile data...");
		
		// Write the scenario in files
		System.out.println("\n6. Writing scenario to files...");
		String scenario_name = "scenario_" +  created_joins
				+ "_" + created_unions + "_" + min_arity + "_" + max_arity +"_"+min_cardinality + "_" + max_cardinality + "_" + count_relations+"_"+(int)(distractions) 
				+ "_" + profileData.getInds().size()
				+ "_" + profileData.getKeys().size() ;
		
		WriteUtils.write_scenario(source_schemas, target_schema, folder_path, scenario_name, profileData, target_node);
		System.out.println("\n\nFinished generating and writing the scenario!\nCheck "+folder_path+"/"+scenario_name+" for the scenario files!");
		
		File folder = new File(folder_path);
		
		System.out.println("Created joins= "+created_joins);
		System.out.println("Created unions= "+created_unions);
		System.out.println("Min_arity = "+min_arity);
		System.out.println("Max_arity = "+max_arity);
		System.out.println("Min_size = "+min_cardinality);
		System.out.println("Max_size = "+max_cardinality);
		System.out.println("Created source relations= "+count_relations);
		System.out.println("#distractions = "+distractions);
		System.out.println("#inds = "+profileData.getInds().size());
		System.out.println("#candidate_keys = "+profileData.getKeys().size());
		
		System.out.println("\n\ntest_scale,"+folder.getAbsolutePath()+"/statistics/synthetic_join/"+Synthegrate.getUnionsJoinsString()+"_expected_mapping.sql,"+folder.getAbsolutePath()+"/statistics/synthetic_join/"+Synthegrate.getUnionsJoinsString()+"_dynamap_mapping.sql"+","+target_schema.getPkConstraints().get(0).getKeyAttributesNames().get(0));
		System.out.println("test_scale,"+folder.getAbsolutePath()+"/statistics/synthetic_join/"+Synthegrate.getUnionsJoinsString()+"_expected_mapping.sql,"+folder.getAbsolutePath()+"/statistics/synthetic_join/"+WriteUtils.getSpicyMappingSQL()+","+target_schema.getPkConstraints().get(0).getKeyAttributesNames().get(0));

	}

	private long generateRandomAttributes(ArrayList<DBSchema> source_schemas) {
		long count_relations = 0;
		for (DBSchema schema : source_schemas) {
			
			ArrayList<Relation> relations = schema.getRelations();
			count_relations += relations.size();
			
			ArrayList<Relation> relations_less_min_arity = new ArrayList<>();
			
			/**
			 * Make sure that all relations have the min number of attributes (>=min_arity)
			 */
			for (Relation relation : relations)
				if (relation.getAttributes().size()<min_arity)
					relations_less_min_arity.add(relation);
			/**
			 * Add the number of missing attributes
			 */
			for (Relation relation : relations_less_min_arity){
				while (relation.getAttributes().size()<min_arity) {
					System.out.println("here");
					SchemaComponentsUtils.generateRandomAttributes(relation, min_arity, max_arity);}
			}
			
			/**
			 * Randomly add attributes to all relations so we have as much variation as possible
			 */
			for (Relation relation : relations)
				if (relation.getAttributeNames().size() < max_arity)
					SchemaComponentsUtils.generateRandomAttributes(relation, min_arity, max_arity);
		}
		
		return count_relations;
		
	}
	
	
	/**
	 * Return the (partial) INDs and keys of the sources.
	 * @param source_schemas
	 * @return
	 */
	private ProfileData generateProfileData(
			ArrayList<DBSchema> source_schemas) {
		
		ArrayList<InclusionDependency> allINDs = new ArrayList<>();
		ArrayList<KeyConstraint> allKeys = new ArrayList<>();
		
		ArrayList<Relation> relations = new ArrayList<>();
		
		for (MappingNode node:leaves_nodes)
		{
			Relation relation = node.getRelation();
			relations.add(relation);
			allKeys.addAll(relation.getCandidateKeys());
		}
		
		for (Relation relation1:relations){
			for (Relation relation2:relations){
				
				if (relation1!=relation2){
					ArrayList<InclusionDependency> inds = SchemaComponentsUtils.getINDs(relation1, relation2);
					if (inds!=null&&!inds.isEmpty())
						allINDs.addAll(inds);
				}
			}
		}
		
		allINDs = new ArrayList<>(new HashSet<>(allINDs));
		
		ProfileData pd = new ProfileData();
		pd.setKeys(allKeys);
		pd.setInds(allINDs);
		
		return pd;
	}

	@SuppressWarnings("unused")
	private MappingNode get_MappingNode_byRelation(Relation inner_relation) {

		for (MappingNode mn : leaves_nodes) {
			if (mn.getRelation().equals(inner_relation))
				return mn;
		}
		return null;
	}

	private DBSchema generateTargetSchema() {

		DBSchema target_schema = SchemaComponentsUtils.createDBSchema(0);
		ArrayList<Relation> target_relations = new ArrayList<>();

		for (int i = 0; i < target_relations_number; i++) {
			int arity = 0;
			if(min_arity<=joins_number) {
				if(joins_number>max_arity)//because a join creates 2 source relations
					//the set number of joins is not possible with the set max_arity so we lower the number of joins that are to be generated
					joins_number = (int)max_arity-1;
				arity = (int) Utils.get_random_index_range(joins_number+1, max_arity, true);
			}else
				arity = (int) Utils.get_random_index_range(min_arity, max_arity, true);
		
			long min_card_target = 0;
			if (joins_number>0&& max_cardinality-joins_number>max_cardinality)
			{
				System.err.println("Change the max cardinality ( increase needed ) as this will not accomodate the requested scenario!");
				System.exit(1);
			}
			
			if (unions_number>0) {
				if( max_cardinality-unions_number>min_cardinality)
					min_card_target = max_cardinality-unions_number;
				else {
					System.err.println("Change the min cardinality or max_cardinality ( need (max_cardinality - min_cardinality) > #unions )  as this will not accomodate the requested scenario!");
					System.exit(1);
				}
			}
			System.out.println("min_card_target=" +min_card_target);
			Relation new_relation = SchemaComponentsUtils.create_relation_with_data(arity, max_cardinality,
					min_card_target);

			ArrayList<Attribute> attributes = new_relation.getAttributes();

			for (Attribute new_attribute : attributes) {
				new_attribute.setTarget_attribute(true);
				new_attribute.setExpected_in_merge(true);
				new_attribute.setMatched_target_attribute(new_attribute);
			}

			new_relation.setParentSchema(target_schema);
			target_relations.add(new_relation);
			target_schema.addRelation(new_relation);

			
		}

		// setting primary keys
		Collections.sort(target_relations);
		for (int i = 0; i < target_pks_number; i++) {
			// we can't have more than one pk in each relation => pks<= number
			// of relations
			if (i < target_relations.size()) {
				Relation relation = target_relations.get(i);

				Attribute new_attribute = (Attribute) Utils.choose_random(relation.getAttributes());
				new_attribute.set_primary_key(true);
				new_attribute.set_unique(true);
				relation.setPrimaryKey(new_attribute);
				System.out.println(
						"Generate new target relation (relation index = " + i + "): \n" + relation.displayAllInfo());
			}
			
		}
		System.out.println("Generated target primary keys ...");

		return target_schema;
	}

	MappingNode target_node = null;
	private ArrayList<DBSchema> generateSourceSchemas(DBSchema target_schema) {
		ArrayList<DBSchema> source_schemas = new ArrayList<>();
		
		//total tries is used for trying to create an operation (join/union)
		int total_tries = 0;
		int allowed_tries = tries + joins_number+ unions_number;

		// create empty source schemas
		for (int i = 0; i < schemas_number; i++) {
			source_schemas.add(SchemaComponentsUtils.createDBSchema(0));
		}

		// TODO change this for a target schema with more than one target
		// relation - this is hard-coded now
		target_node = new MappingNode();
		target_node.setRelation(target_schema.getRelations().get(0));
		target_node.setRoot(true);
		leaves_nodes.add(target_node);

		int operation = 0;
		
		//the first relation split is considered to be the target relation
		created_relations = 1;
		/*
		 * We will build a binary tree so we need to keep track of the leaves of
		 * the tree which in the end will be the actual source relations
		 */
		while ((created_joins < joins_number || created_unions < unions_number)&&(created_relations<max_source_relations)) {
			//sort leaves by number of attributes
			Collections.sort(leaves_nodes, new Comparator<MappingNode>() {
				@Override
				public int compare(MappingNode o1, MappingNode o2) {
					return o1.getRelation().getSizeMatchedAttributes() - o2.getRelation().getSizeMatchedAttributes();
				}
			});
			
			if (total_tries>=allowed_tries){
				break;
			}
			
			total_tries++;
			// choose the operation we want to generate next
			operation = choose_operation(created_joins, created_unions);

			if (operation == Constants.JOIN_OPERATION_INT) {
				//get last leave - with max number of attribs
				MappingNode extended_node = (MappingNode) leaves_nodes.get(leaves_nodes.size()-1);
				int current_tries = 0;
				boolean is_extendible = false;
				while (current_tries<tries){
					is_extendible = Utils.is_relation_JOIN_extendible(extended_node);
					if (!is_extendible){
						current_tries++;
						if (balanced_join)
							extended_node = (MappingNode) Utils.most_matches(leaves_nodes);
						else
							extended_node = (MappingNode) Utils.choose_random(leaves_nodes);
					} else
						break;
				}
				
				if (current_tries>=tries || !is_extendible)
					continue;
				
				ArrayList<MappingNode> new_relations = createJoin(extended_node, source_schemas, target_schema);
				current_tries = 0;
				
				//this should not happen as we choose an extendible join
				while (new_relations.isEmpty()) {
					extended_node = (MappingNode) Utils.choose_random(leaves_nodes);
					new_relations = createJoin(extended_node, source_schemas, target_schema);
					
					if (current_tries >= tries)
						break;
				}
				if (current_tries < tries) {
					created_joins++;
					leaves_nodes.remove(extended_node);
					leaves_nodes.addAll(new_relations);
					System.out.println("Extended relation " + extended_node.getRelation().getName()
							+ " into the JOIN of [" + new_relations.get(0).getRelation().toString() + ", "
							+ new_relations.get(1).getRelation().toString() + "]\nJOIN condition:"+extended_node.getCondition()+"\n");
					
					/* Reset the total tries of creating extending a new relation 
					   We should create extensions as long as we can find a suitable relation to extend */
					total_tries = 0;
					
					//add just 1 because we've used 1 relation to split
					created_relations++;
				} else {
					System.out.println("Abandoned creating JOIN");
				}
			} else if (operation == Constants.UNION_OPERATION_INT) {
				int current_tries = 0;
				
				MappingNode extended_relation = get_union_extendible_mapping_node(leaves_nodes);
				boolean ifDisjoint = false;
				
				if (created_disjoint_unions < disjoint_unions) {
					ifDisjoint = true;
				}
				
				boolean is_extendible = false;
				while (current_tries<tries){
					
					is_extendible = Utils.is_relation_UNION_extendible(extended_relation.getRelation(),ifDisjoint);
					
					/* We shouldn't try to extend a relation which was extended in a 
					 * JOIN (might create a FK violation by splitting the PK used in the FK relationship). */
					if (!is_extendible||(extended_relation.getRelation().containsPK()&&joins_number>0&&created_joins!=0)){
						current_tries++;
						extended_relation = (MappingNode) Utils.choose_random(leaves_nodes);
					} else
						break;
				}
				
				if (current_tries>=tries || !is_extendible){
					System.out.println("Could not find a relation to extend using a UNION");
					continue;
				}

				ArrayList<MappingNode> new_relations = createUnion(extended_relation, source_schemas, target_schema, ifDisjoint);
				current_tries = 0;
				while (new_relations == null || new_relations.isEmpty()) {
					// if the chosen relation could not be extended, then we try
					// with another one - limited number of tries
					extended_relation = (MappingNode) Utils.choose_random(leaves_nodes);
					new_relations = createUnion(extended_relation, source_schemas, target_schema,ifDisjoint);
					
					if (current_tries >= tries)
						break;
					current_tries++;
				}
				if (current_tries < tries) {
					
					leaves_nodes.remove(extended_relation);
					leaves_nodes.addAll(new_relations);
					System.out.println("Extended relation " + extended_relation.getRelation().getName()
							+ " into the UNION of [" + new_relations.get(0).getRelation().toString() + ", "
							+ new_relations.get(1).getRelation().toString() + "]\n");
					
					/* Reset the total tries of creating extending a new relation 
					   We should create extensions as long as we can find a suitable relation to extend */
					total_tries = 0;
					
					if (ifDisjoint)
						created_disjoint_unions++;
					
					created_unions++;
					//add just 1 because we've used 1 relation to split
					created_relations++;
				} else {
					System.out.println("Abandoned creating UNION");
				}
			} else if (operation == Constants.FULL_OUTER_JOIN_OPERATION_INT) {
				// CURRENTLY NOT SUPPORTED
			}

		}

		System.out.println("\nFinal Mapping: \n" + target_node.toString());

		return source_schemas;

	}

	private ArrayList<MappingNode> createJoin(MappingNode extended_node, ArrayList<DBSchema> source_schemas,
			DBSchema target_schema) {

		Relation extended_relation = extended_node.getRelation();
		ArrayList<Attribute> old_attributes = extended_relation.getAttributes();
		
		// remove the extended relation from the schemas as it will no longer be
		// a source relation(on the leaves)
		DBSchema parent_schema = extended_relation.getParentSchema();
		if (target_schema != parent_schema) {
			parent_schema.removeRelation(extended_relation);
			extended_relation.setParentSchema(null);
		}

		ArrayList<MappingNode> result = new ArrayList<>();

		//child relations
		ArrayList<Relation> new_relations = new ArrayList<>();
		Relation left_child = null;
		Relation right_child = null;
		
		Relation pk_relation = null;
		Relation fk_relation = null;
		
		Attribute pk_attribute = null;
		Attribute fk_attribute = null;
		
		
		boolean if_reuse_attribute = false;
//		if (reuse_join_attributes>0&&created_reused_attribute_joins<reuse_join_attributes)
//			if_reuse_attribute = true;
		
		if (created_joins+1==joins_number&&created_unions<unions_number)
			if_reuse_attribute = true;
		
		Attribute reused_attribute = null;
		//siblingNode=null when the parent is the root
		
		MappingNode siblingNode = (extended_node.is_root ? null:extended_node.getParent().getOtherSibling(extended_node));
		
		if (if_reuse_attribute)
		{//picking a random attribute from the extended relation
			left_child = SchemaComponentsUtils.create_empty_relation(0);
			right_child = SchemaComponentsUtils.create_empty_relation(0);
			
			reused_attribute = (Attribute)Utils.choose_random(extended_relation.getMatchedAttributes());
			pk_attribute = SchemaComponentsUtils.createAttribute(reused_attribute);
			pk_attribute.set_foreign_key(false);
			pk_attribute.setPrimary_key_attribute(null);
			
			fk_attribute = SchemaComponentsUtils.createAttribute(reused_attribute);
			fk_attribute.setName(Utils.generateAttributeName());
			fk_attribute.set_foreign_key(false);
			fk_attribute.setPrimary_key_attribute(null);
			
			if (!extended_node.isRoot())
				extended_node.getParent().getOtherSibling(extended_node).getRelation().deleteFKeyWithPK(reused_attribute);
		}else
		{	// create new attributes to use in the join
			// Create each relation with the attribute used in the join
			left_child = SchemaComponentsUtils.create_empty_relation(1);
			right_child = SchemaComponentsUtils.create_empty_relation(1);
		}

		new_relations.add(left_child);
		new_relations.add(right_child);

		// setting fk_constraints
		long needed_FKs = Math.round(max_fks_ratio * joins_number);
		boolean generateFK = false;

		// decide whether to create explicit FK
		if (needed_FKs > created_fks) {
			generateFK = Utils.getRandomBoolean();
		}
		if (generateFK) {
			pk_relation = left_child;
			fk_relation = right_child;
			
		} else {
			pk_relation = (Relation) Utils.choose_random(new_relations);
			if (pk_relation == left_child)
				fk_relation = right_child;
			else
				fk_relation = left_child;
		}
		
		if (if_reuse_attribute){
			pk_relation.addAttribute(pk_attribute);
			pk_attribute.setParentRelation(pk_relation);
			fk_relation.addAttribute(fk_attribute);
			fk_attribute.setParentRelation(fk_relation);
			System.out.println("(Below extension)"+reused_attribute.getFullName()+" reused in the JOIN condition \n"
			+ pk_relation.getName()+"."+pk_attribute.getName()+" is referenced, "+fk_attribute.getNameWRelation()+" is FK");
		}
		else
		{
			pk_attribute = pk_relation.getAttributes().get(0);
			fk_attribute = fk_relation.getAttributes().get(0);
			pk_relation.addAttribute(pk_attribute);
			pk_attribute.setParentRelation(pk_relation);
			fk_relation.addAttribute(fk_attribute);
			fk_attribute.setParentRelation(fk_relation);
			System.out.println("New attributes created for the JOIN condition.\n"
			+ pk_relation.getName()+"."+pk_attribute.getName()+" is referenced, "+fk_attribute.getNameWRelation()+" is FK");
		}

		// the cardinal of the result will always be the same as the cardinal of
		// the FK relation
		fk_relation.setCardinality(extended_relation.getCardinality());
		System.out.println("here11");
		if (siblingNode!=null&&siblingNode.operation.equals(Constants.JOIN_OPERATION)) {
			System.out.println("here1");
			pk_relation.setCardinality(Utils.get_random_index_range(fk_relation.getCardinality(), siblingNode.getRelation().getCardinality(), true));
			System.out.println("here2");
		}
		System.out.println("here12");
		if (generateFK) {
			// add the relations to the same source schema as we need the
			// explicit FK relationship
			DBSchema common_schema = (DBSchema) Utils.choose_random(source_schemas);
			common_schema.addRelation(pk_relation);
			pk_relation.setParentSchema(common_schema);
			common_schema.addRelation(fk_relation);
			fk_relation.setParentSchema(common_schema);

			pk_attribute = pk_relation.getAttributes().get(0);
			pk_attribute.set_primary_key(true);
			pk_attribute.setExpected_in_merge(true);
			pk_relation.setPrimaryKey(pk_attribute);

			fk_attribute = fk_relation.getAttributes().get(0);
			fk_attribute.set_foreign_key(true);
			fk_attribute.setExpected_in_merge(true);
			fk_attribute.setPrimary_key_attribute(pk_attribute);

			// Gen KeyConstraint
			KeyConstraint pk = SchemaComponentsUtils.generateKey(pk_relation, pk_attribute);
			pk_relation.addCandidateKey(pk);

			// Gen FKConstraint
			FKconstraint newFK = SchemaComponentsUtils.generateFK(pk_relation, fk_relation, pk, fk_attribute);

			common_schema.addFK(newFK);
			common_schema.addPK(pk);
		} else {
			/*
			 * we don't need explicit FK creation - but we will create the join
			 * opportunity add the relations to the same source schema as we
			 * need the explicit FK relationship
			 */

			// we can put the relations in separate schemas
			DBSchema common_schema = (DBSchema) Utils.choose_random(source_schemas);
			common_schema.addRelation(pk_relation);
			pk_relation.setParentSchema(common_schema);

			common_schema = (DBSchema) Utils.choose_random(source_schemas);
			common_schema.addRelation(fk_relation);
			fk_relation.setParentSchema(common_schema);

			// referenced attribute
			pk_attribute = pk_relation.getAttributes().get(0);
			pk_attribute.set_unique(true);
			pk_attribute.setExpected_in_merge(true);
			pk_attribute.setParentRelation(pk_relation);

			fk_attribute = fk_relation.getAttributes().get(0);
			fk_attribute.setExpected_in_merge(true);
			
			// Gen KeyConstraint
			KeyConstraint pk = SchemaComponentsUtils.generateCandidateKey(pk_relation, pk_attribute);
			pk_relation.addCandidateKey(pk);
		}
		
		SchemaComponentsUtils.generateIND( min_cardinality, max_cardinality,
				fk_relation, pk_relation, fk_attribute, pk_attribute);
		
		
		//Try to split the attributes so that we have "splittable" tables
		ArrayList<Attribute> matched_attributes = new ArrayList<>();
		matched_attributes.addAll(extended_relation.getMatchedAttributes());
		if (reused_attribute!=null)
			matched_attributes.remove(reused_attribute);
		if (matched_attributes.size()>1)
		{
			//make sure that both relations contain at least one matched attribute to the target 
			copy_attribute_in_new_relation(extended_node, fk_relation, matched_attributes.get(0));
			copy_attribute_in_new_relation(extended_node, pk_relation,  matched_attributes.get(1));
		}
		else
			if (matched_attributes.size()==1){
				//make sure that the FK relation is not subsumed 
				copy_attribute_in_new_relation(extended_node, fk_relation, matched_attributes.get(0));
			}
			else
			{
				System.err.println("!! Re-run is necessary: This shouldn't happen: the FK relation("+fk_relation.getNameWSchema()+") might be subsumed by PK("+pk_relation.getNameWSchema()+") relation\n");
			}
		
		if (balanced_join&&matched_attributes.size()>1)
		{
			
			for (int i=2;i<=matched_attributes.size()/2;i++)
			{
				copy_attribute_in_new_relation(extended_node, fk_relation, matched_attributes.get(i));
			}
			
			for (int i=matched_attributes.size()/2+1;i<matched_attributes.size();i++)
			{
				copy_attribute_in_new_relation(extended_node, pk_relation, matched_attributes.get(i));
			}
			
		}
		if (chain_join&&!star_join) {
			//CHAIN JOIN
			//Add all expected in merge attributes in the PK relation which becomes inextensible because it will have only 1 matched attr put above
			for (Attribute old_attribute : old_attributes) {
				
				if (old_attribute.equals(reused_attribute))
					continue;
				
				if (pk_relation.containsAttribute(old_attribute)||fk_relation.containsAttribute(old_attribute))
					continue;
				
				if (old_attribute.isExpected_in_merge()&&!old_attribute.isTarget_attribute())
					copy_attribute_in_new_relation(extended_node, pk_relation, old_attribute);
				else
					copy_attribute_in_new_relation(extended_node, fk_relation, old_attribute);
			}
		} else 
			if (!chain_join&&star_join){
			//STAR JOIN
			for (Attribute old_attribute : old_attributes) {
				
				if (old_attribute.equals(reused_attribute))
					continue;
				if (!old_attribute.isExpected_in_merge())
					continue;
				if (pk_relation.containsAttribute(old_attribute)||fk_relation.containsAttribute(old_attribute))
					continue;
				copy_attribute_in_new_relation(extended_node, fk_relation, old_attribute);
			}
			for (Attribute old_attribute : old_attributes) {
				if (old_attribute.equals(reused_attribute))
					continue;
				if (pk_relation.containsAttribute(old_attribute)||fk_relation.containsAttribute(old_attribute))
					continue;
				copy_attribute_in_new_relation(extended_node, pk_relation, old_attribute);
			}
		}
			else
			{//MIXED JOIN type
				for (Attribute old_attribute : old_attributes) {
					if (old_attribute.equals(reused_attribute))
						continue;
					if (pk_relation.containsAttribute(old_attribute)||fk_relation.containsAttribute(old_attribute))
						continue;
					
					Relation to_be_added_relation = (Relation) Utils.choose_random(new_relations);
					copy_attribute_in_new_relation(extended_node, to_be_added_relation, old_attribute);
				}
			}
		
		String condition = pk_attribute.getNameWRelation() + " = " + fk_attribute.getNameWRelation();;

		MappingNode left_child_node = new MappingNode();
//		left_child_node.setMerge_opportunity(Constants.CASE4_CANDIDATE_KEY_IND);
		left_child_node.setRelation(pk_relation);
		left_child_node.setUnion_extendible(false);
		MappingNode right_child_node = new MappingNode();
//		right_child_node.setMerge_opportunity(Constants.CASE4_CANDIDATE_KEY_IND);
		right_child_node.setRelation(fk_relation);
		extended_node.setLeft_child(left_child_node);
		left_child_node.setParent(extended_node);
		extended_node.setRight_child(right_child_node);
		right_child_node.setParent(extended_node);
		extended_node.setCondition(condition);
		extended_node.setOperation(Constants.JOIN_OPERATION);

		result.add(right_child_node);
		result.add(left_child_node);

		return result;
	}
	
	private Attribute copy_attribute_in_new_relation(MappingNode node, Relation new_relation, Attribute old_attribute){
		
		Attribute new_attribute = SchemaComponentsUtils.createAttribute(old_attribute);
		new_attribute.setParentRelation(new_relation);
		new_attribute.setDistinct_values(new_relation.getCardinality());
		new_attribute.setExpected_in_merge(true);
		new_relation.addAttribute(new_attribute);
		/*
		 * One of the "inherited" attributes could have been a PK in the
		 * parent, but we've just created a PK so we can't have several,
		 * so we will transform the old PK into a Candidate key setting
		 * the unique constraint on true
		 */

		if (old_attribute.is_primary_key() || old_attribute.is_unique()) {
			new_attribute.set_primary_key(false);
			new_attribute.set_unique(true);
			KeyConstraint key = SchemaComponentsUtils.generateCandidateKey(new_relation, new_attribute);
			new_relation.addCandidateKey(key);
			if (old_attribute.is_primary_key()&&!node.isRoot())
				node.getParent().getOtherSibling(node).getRelation().deleteFKeyWithPK(old_attribute);
		}
		/*
		 * One of the "inherited" attributes could have been a FK in the
		 * parent, so we will delete the old FK information for the new
		 * attribute
		 */
		if (old_attribute.is_foreign_key()) {
			// destroy the previously created FK
			new_attribute.set_foreign_key(false);
			new_attribute.setPrimary_key_attribute(null);
		}
		
		return new_attribute;
	}

	private ArrayList<MappingNode> createUnion(MappingNode node, ArrayList<DBSchema> source_schemas,
			DBSchema target_schema, boolean ifDisjoint) {

		ArrayList<MappingNode> result = new ArrayList<>();

		Relation extended_relation = node.getRelation();

		// Create each relation with no attributes
		Relation left_child = SchemaComponentsUtils.create_empty_relation(0);
		Relation right_child = SchemaComponentsUtils.create_empty_relation(0);

		ArrayList<Attribute> old_attributes = extended_relation.getAttributes();
		
		long common_values = -1;

		/*
		 * 1 - set the cardinality of the relations we cannot have a child
		 * relation with a cardinality greater than the extended_relation
		 * cardinality
		 */
		if (ifDisjoint) {
			long size = Math.round(extended_relation.getCardinality()/2);//Utils.get_random_index_range(min_cardinality, extended_relation.getCardinality(), true);
			left_child.setCardinality(size);
			right_child.setCardinality(extended_relation.getCardinality()-size);
			common_values = 0;
			
			System.out.println("Trying to create disjoint UNION tables with cardinalities a)"+ left_child.getCardinality()+" b) "+right_child.getCardinality() 
			+" of extended relation's cardinality = "+extended_relation.getCardinality());
		} else {
			if (left_child.getCardinality() < 0) {
				/*
				 * left_child relation doens't have the cardinality set
				 */
				long size = Utils.get_random_index_range(min_cardinality, extended_relation.getCardinality(), true);
				left_child.setCardinality(size);
			}
			int tries = 0;
			if (right_child.getCardinality() < 0) {
				/*
				 * right_child relation doens't have the cardinality set
				 */
				long size = Utils.get_random_index_range(min_cardinality, extended_relation.getCardinality(), true);
				while (size + left_child.getCardinality() < extended_relation.getCardinality()) {
					if (tries > ScenarioGenerator.tries)
						break;
					size = Utils.get_random_index_range(min_cardinality, extended_relation.getCardinality(), true);
					tries++;
				}
				//it doesn't matter if we set the wrong cardinality because it will stop the creation of UNION in the next below if
				right_child.setCardinality(size);
			}

			if (tries >= ScenarioGenerator.tries) {
				System.err.println("Abandoned creating partial IND between " + left_child.getName() + " and "
						+ right_child.getName());
				return null;
			}

			/* Decide how much overlap we want between the two relations. */
			common_values = (right_child.getCardinality() + left_child.getCardinality())
					- extended_relation.getCardinality();

		}
		
		for (Attribute old_attribute : old_attributes) {

			/* Create left attribute */
			Attribute new_left_attribute =  new Attribute(old_attribute);
			new_left_attribute.setName(old_attribute.getName());
			new_left_attribute.setExpected_in_merge(true);
			new_left_attribute.setParentRelation(left_child);
			left_child.addAttribute(new_left_attribute);

			/* Create right attribute */
			Attribute new_right_attribute = new Attribute(old_attribute);
			new_right_attribute.setExpected_in_merge(true);
			new_right_attribute.setName(Utils.generateAttributeName());
			new_right_attribute.setParentRelation(right_child);
			right_child.addAttribute(new_right_attribute);
			
			/*
			 * One of the "inherited" attributes could have been a PK in the
			 * parent, but we've just created a PK so we can't have several,
			 * so we will transform the old PK into a Candidate key setting
			 * the unique constraint on true
			 */

			if (old_attribute.is_primary_key() || old_attribute.is_unique()) {
				//left relation
				new_left_attribute.set_primary_key(false);
				new_left_attribute.set_unique(true);
				KeyConstraint key = SchemaComponentsUtils.generateCandidateKey(left_child, new_left_attribute);
				left_child.addCandidateKey(key);
				
				//right relation
				new_right_attribute.set_primary_key(false);
				new_right_attribute.set_unique(true);
				key = SchemaComponentsUtils.generateCandidateKey(right_child, new_right_attribute);
				right_child.addCandidateKey(key);
				
				if (old_attribute.is_primary_key()&&!node.isRoot())
					node.getParent().getOtherSibling(node).getRelation().deleteFKeyWithPK(old_attribute);
			}
			/*
			 * One of the "inherited" attributes could have been a FK in the
			 * parent, so we will delete the old FK information for the new
			 * attribute
			 */
			if (old_attribute.is_foreign_key()) {
				// destroy the previously created FK
				new_left_attribute.set_foreign_key(false);
				new_left_attribute.setPrimary_key_attribute(null);
				new_right_attribute.set_foreign_key(false);
				new_right_attribute.setPrimary_key_attribute(null);
			}

			/* Create partial INDs between the two newly created attributes */
//			ArrayList<InclusionDependency> pinds = 
					SchemaComponentsUtils.generate_partialIND(
					min_cardinality, extended_relation.getCardinality(), right_child, left_child, new_right_attribute,
					new_left_attribute, common_values, tries);
			
		}
		
		// remove the extended relation from the parent schema as it will no
		// longer be
		// a source relation (on the leaves of the binary tree)
		DBSchema parent_schema = extended_relation.getParentSchema();
		if (target_schema != parent_schema) {
			parent_schema.removeRelation(extended_relation);
			extended_relation.setParentSchema(null);
		}

		/*
		 * Put the new relations into schemas - We can put the relations in
		 * SEPARATE schemas
		 */
		DBSchema common_schema = (DBSchema) Utils.choose_random(source_schemas);
		common_schema.addRelation(left_child);
		left_child.setParentSchema(common_schema);

		common_schema = (DBSchema) Utils.choose_random(source_schemas);
		common_schema.addRelation(right_child);
		right_child.setParentSchema(common_schema);

		MappingNode left_child_node = new MappingNode();
//		left_child_node.setMerge_opportunity(Constants.CASE1_PARTIAL_IND);
		left_child_node.setRelation(left_child);
		
		MappingNode right_child_node = new MappingNode();
//		right_child_node.setMerge_opportunity(Constants.CASE1_PARTIAL_IND);
		right_child_node.setRelation(right_child);
		
		node.setLeft_child(left_child_node);
		left_child_node.setParent(node);
		node.setRight_child(right_child_node);
		right_child_node.setParent(node);
		node.setOperation(Constants.UNION_OPERATION);

		result.add(right_child_node);
		result.add(left_child_node);

		return result;
	}

	private int choose_operation(int created_joins, int created_unions) {
		int operation = 0;
		
		//create unions first
		if (created_unions!=unions_number&&create_unions_first)
			return Constants.UNION_OPERATION_INT;
		
		//create joins first
		if (created_joins!=joins_number&&create_joins_first)
			return Constants.JOIN_OPERATION_INT;
		
		//randomly choose an operation (or whichever needs to be created)
		if (created_joins != joins_number && created_unions != unions_number) {
			operation = (Integer) Utils.choose_random(OPERATIONS_LIST);
		} else if (created_joins == joins_number && created_unions != unions_number) {
			operation = Constants.UNION_OPERATION_INT;
		} else if (created_joins != joins_number && created_unions == unions_number) {
			operation = Constants.JOIN_OPERATION_INT;
		}
		return operation;
	}

	/**
	 * This will return the first leave that is union extendible. 
	 * We choose the first occurrence as the leave will be sorted according to their cardinality.
	 * @return
	 */
	private MappingNode get_union_extendible_mapping_node(ArrayList<MappingNode> nodes) {
		
		MappingNode result = null;
		Collections.sort(leaves_nodes, new Comparator<MappingNode>() {
			@Override
			public int compare(MappingNode o1, MappingNode o2) {
				return (int)o1.getRelation().getCardinality() - (int)o2.getRelation().getCardinality() ;
			}
		});
		for (int i=nodes.size()-1;i>=0;i--) {
			MappingNode node = nodes.get(i);
			if (node.isUnion_extendible()&&node.getRelation().getCardinality()>1) {
				result = node;
				break;
			}
		}
		return result;
	}
	
}
