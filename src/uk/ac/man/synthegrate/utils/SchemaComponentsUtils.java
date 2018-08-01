package uk.ac.man.synthegrate.utils;

import java.util.ArrayList;
import java.util.HashSet;

import uk.ac.man.synthegrate.schema_components.Attribute;
import uk.ac.man.synthegrate.schema_components.DBSchema;
import uk.ac.man.synthegrate.schema_components.FKconstraint;
import uk.ac.man.synthegrate.schema_components.InclusionDependency;
import uk.ac.man.synthegrate.schema_components.KeyConstraint;
import uk.ac.man.synthegrate.schema_components.ProfileData;
import uk.ac.man.synthegrate.schema_components.Relation;

public class SchemaComponentsUtils {

	public static DBSchema createDBSchema(int relations_number) {
		DBSchema new_schema = new DBSchema();
		new_schema.setName(Utils.generateSchemaName());

		for (int i = 0; i < relations_number; i++) {
			new_schema.addRelation(create_empty_relation(0));
		}
		return new_schema;
	}

	public static Relation create_empty_relation(int attributes_number) {
		Relation new_relation = new Relation();
		new_relation.setName(Utils.generateRelationName());

		for (int i = 0; i < attributes_number; i++) {
			new_relation.addAttribute(createAttribute());

		}
		return new_relation;
	}

	public static Relation create_relation_with_data(int attributes_number, long max_cardinal, long min_cardinal) {
		Relation new_relation = new Relation();
		new_relation.setName(Utils.generateRelationName());
		new_relation.setCardinality(Utils.get_random_index_range(min_cardinal, max_cardinal, true));

		System.out.println("Creating attribute parameters...");
		for (int i = 0; i < attributes_number; i++) {
			Attribute new_attribute = createAttribute_with_parameters(new_relation.getCardinality());
			
			new_relation.addAttribute(new_attribute);
		}
		System.out.println("Created attribute parameters...");
		return new_relation;
	}

	public static Attribute createAttribute() {
		Attribute new_attribute = new Attribute();
		new_attribute.setName(Utils.generateAttributeName());

		return new_attribute;
	}

	public static Attribute createAttribute(Attribute copied_attribute) {
		Attribute new_attribute = new Attribute(copied_attribute);
//		Attribute new_attribute = copied_attribute;
		// new_attribute.setName(Utils.generateAttributeName());

		return new_attribute;
	}

	public static Attribute createAttribute_with_parameters(long relation_size) {
		Attribute new_attribute = new Attribute();
		new_attribute.setName(Utils.generateAttributeName());

		new_attribute.setParameters(Parameters.generate_random_parameters(relation_size));
		return new_attribute;
	}

	/**
	 * Add some random number of attributes to a relation so it has the arity
	 * between min_arity and max_arity.
	 * 
	 * @param relation
	 * @param min_arity
	 * @param max_arity
	 * @return relation
	 */
	public static Relation generateRandomAttributes(Relation relation, long min_arity, long max_arity) {
		
		int count_attributes = relation.getAttributes().size();
		long add_attributes = 0;

		if (max_arity - count_attributes>0)
			add_attributes = Utils.get_random_index_range(0 , max_arity - count_attributes, true);
		
		for (int i = 0; i < add_attributes; i++) {
			//if we create it with parameters => might create join opportunities
//			Attribute attribute = createAttribute_with_parameters(relation.getCardinality());
			Attribute attribute = createAttribute();
			if (attribute!=null)
				relation.addAttribute(attribute);
		}
		
		System.out.println(add_attributes +" random attributes were created for relation "+relation.getNameWSchema());
		return relation;
	}

	/**
	 * Each IND has a "mirroring" version where the including attribute becomes
	 * the included and viceversa, so we will return an array of two elements
	 * where one will be the full IND and the other will be the "mirroring"
	 * (partial) IND.
	 * 
	 * This method generates the parameters within the attributes so that one is
	 * fully included in the other.
	 * 
	 * @param dictionary_size
	 * @param min_cardinality
	 * @param max_cardinality
	 * @param dependent_relation
	 * @param referenced_relation
	 * @param dependent_attribute
	 * @param referenced_attribute
	 * @return
	 */
	public static ArrayList<InclusionDependency> generateIND(long min_cardinality,
			long max_cardinality, Relation dependent_relation, Relation referenced_relation, Attribute dependent_attribute,
			Attribute referenced_attribute) {

		ArrayList<InclusionDependency> newINDs = new ArrayList<>();

		// 1 - set the cardinality of the relations
		if (dependent_relation.getCardinality() < 0 && referenced_relation.getCardinality() < 0) {
			/*
			 * neither of the relations has the cardinality set
			 */
			long size1 = Utils.get_random_index_range(min_cardinality, max_cardinality, true);
			referenced_relation.setCardinality(size1);

			/*
			 * we assume the included relation is smaller that the including
			 * relation
			 */
			long size2 = Utils.get_random_index_range(min_cardinality, size1, true);
			dependent_relation.setCardinality(size2);
		} else if (dependent_relation.getCardinality() > 0 && referenced_relation.getCardinality() < 0) {
			/*
			 * included relation has the cardinality set. We suppose the
			 * included relation is smaller that the including relation
			 */
			long size = Utils.get_random_index_range(dependent_relation.getCardinality(), max_cardinality, true);
			referenced_relation.setCardinality(size);
		} else if (dependent_relation.getCardinality() < 0 && referenced_relation.getCardinality() > 0) {
			/*
			 * including relation has the cardinality set. we suppose the
			 * included relation is smaller that the including relation
			 */
			long size = Utils.get_random_index_range(min_cardinality, referenced_relation.getCardinality(), true);
			dependent_relation.setCardinality(size);
		}
		if (dependent_attribute.getDistinct_values() == -1)
			dependent_attribute.setDistinct_values(dependent_relation.getCardinality());

		if (referenced_attribute.getDistinct_values() == -1)
			referenced_attribute.setDistinct_values(referenced_relation.getCardinality());

		// 2 - create the attributes used in the ind

		if (referenced_attribute.getParameters() == null) {
			Parameters new_parameters = Parameters.generate_random_parameters(referenced_relation.getCardinality());
			referenced_attribute.setParameters(new_parameters);
			referenced_relation.addAttribute(referenced_attribute);
			referenced_attribute.setParentRelation(referenced_relation);
		}

		dependent_attribute.setParameters(new Parameters(referenced_attribute.getParameters()));
		dependent_relation.addAttribute(dependent_attribute);
		dependent_attribute.setParentRelation(dependent_relation);

		// full IND
		InclusionDependency newIND = new InclusionDependency();
		newIND.setIncluded_distinct_values(dependent_relation.getCardinality());
		newIND.setIncludedAttribute(dependent_attribute);
		newIND.setIncludingAttribute(referenced_attribute);
		newIND.setIncludedRelation(dependent_relation);
		newIND.setIncludingRelation(referenced_relation);

		newIND.setCoefficient(1.0);

		// "mirroring" IND
		InclusionDependency mirroringIND = new InclusionDependency();
		mirroringIND.setIncluded_distinct_values(referenced_relation.getCardinality());
		mirroringIND.setIncludedAttribute(referenced_attribute);
		mirroringIND.setIncludingAttribute(dependent_attribute);
		mirroringIND.setIncludedRelation(referenced_relation);
		mirroringIND.setIncludingRelation(dependent_relation);

		mirroringIND.setCoefficient(
				(double) dependent_attribute.getDistinct_values() / referenced_attribute.getDistinct_values());

		dependent_relation.addIND(newIND);
		referenced_relation.addIND(newIND);

		dependent_relation.addIND(mirroringIND);
		referenced_relation.addIND(mirroringIND);

		newINDs.add(newIND);
		newINDs.add(mirroringIND);

		return newINDs;
	}

	/**
	 * Each partial IND has a "mirroring" version where the including attribute becomes
	 * the included and viceversa, so we will return an array of two elements
	 * where one will be the partial IND and the other will be the "mirroring"
	 * partial IND.
	 * 
	 * This method generates the parameters within the attributes so that one is
	 * partially included in the other and viceversa.
	 * 
	 * If the number of common values is not set, then create a random overlap.
	 * 
	 * @param dictionary_size
	 * @param min_cardinality
	 * @param max_cardinality
	 * @param dependent_relation
	 * @param referenced_relation
	 * @param dependent_attribute
	 * @param referenced_attribute
	 * @param max_tries
	 * @return 2 partial INDs
	 */
	public static ArrayList<InclusionDependency> generate_partialIND( long min_cardinality,
			long max_cardinality, Relation dependent_relation, Relation referenced_relation, Attribute dependent_attribute,
			Attribute referenced_attribute, long common_values, long max_tries) {

//		System.out.println("Generate partial IND");
		ArrayList<InclusionDependency> pinds = new ArrayList<>();
		
		double overlap = 0.0;

		// 1 - set the cardinality of the relations
		if (referenced_relation.getCardinality() < 0) {
			/*
			 * including relation doens't have the cardinality set
			 */
			long size = Utils.get_random_index_range(0, max_cardinality, true);
			referenced_relation.setCardinality(size);
		}

		if (dependent_relation.getCardinality() < 0) {
			/*
			 * included relation doens't have the cardinality set
			 */
			long size = Utils.get_random_index_range(0, max_cardinality, true);
			dependent_relation.setCardinality(size);
		}
		
		dependent_attribute.setDistinct_values(dependent_relation.getCardinality());
		referenced_attribute.setDistinct_values(referenced_relation.getCardinality());
		
		//If the number of common values is not set, then create a random overlap
		if (common_values<0)
		{
			overlap = Utils.get_random_overlap();
			common_values = (long) Math.ceil(overlap * Math.min(dependent_attribute.getDistinct_values(), referenced_attribute.getDistinct_values()));
			
			//this part shouldn't be needed anymore as selecting the minimum of the two should output a number that is lower than both cardinalities
			int tries = 0;
			while (common_values > dependent_relation.getCardinality()
					|| common_values > referenced_relation.getCardinality()) {
				overlap = Utils.get_random_overlap();
				common_values = (long) Math.ceil(overlap * referenced_attribute.getDistinct_values());
				tries++;
				if (tries >= max_tries)
					break;
			}
			if (tries >= max_tries) {
				System.err.println("Abandoned creating partial IND between " + dependent_relation.getName() + " and "
						+ referenced_relation.getName());
				return null;
			}
		}
		// 2 - set the attributes used in the partial ind
		if (dependent_attribute.getParameters()==null&&referenced_attribute.getParameters()==null){
			//the chosen dictionary needs to fit all values (referenced+dependent-common_values)
			Parameters new_parameters = Parameters.generate_random_parameters(referenced_relation.getCardinality()+dependent_attribute.getDistinct_values()-common_values);
			referenced_attribute.setParameters(new_parameters);
			
			/* we don't want to create a duplicate attribute that has the same values - this shouldn;t be needed anymore with the free_chunk_offset thingy*/
//			while (referenced_relation.checkDuplicateParameters(referenced_attribute)) {
//				new_parameters = Parameters.generate_random_parameters(referenced_relation.getCardinality());
//				referenced_attribute.setParameters(new_parameters);
//			}
			
			long new_offset = Parameters.get_partial_ind_offset(new_parameters, referenced_relation.getCardinality(),
					common_values);

			dependent_attribute.setParameters(new Parameters(new_offset, new_parameters.getShift(),
					new_parameters.getStep(), new_parameters.getDictionary().getDimension() - new_offset,new_parameters.getDictionary()));
		}else
			
			//The below 2 cases do not work with the checking of the dictionary thingy as the parameters are already set
			if (referenced_attribute.getParameters()!=null){//included_attribute.getParameters()==null&&
				Parameters relative_params = referenced_attribute.getParameters();
				long new_offset = Parameters.get_partial_ind_offset(relative_params, referenced_relation.getCardinality(),
						common_values);

				dependent_attribute.setParameters(new Parameters(new_offset, relative_params.getShift(),
						relative_params.getStep(), relative_params.getDictionary().getDimension() - new_offset,relative_params.getDictionary()));
				
				System.out.println("The parameters are set - the dictionary might not be really good in coverage");
			}else
				if (dependent_attribute.getParameters()!=null){//&&including_attribute.getParameters()==null
					Parameters relative_params = dependent_attribute.getParameters();
					long new_offset = Parameters.get_partial_ind_offset(relative_params, dependent_relation.getCardinality(),
							common_values);

					referenced_attribute.setParameters(new Parameters(new_offset, relative_params.getShift(),
							relative_params.getStep(), relative_params.getDictionary().getDimension() - new_offset,relative_params.getDictionary()));
					
					System.out.println("The parameters are set - the dictionary might not be really good in coverage");
				}
		
		
		referenced_relation.addAttribute(referenced_attribute);
		dependent_relation.addAttribute(dependent_attribute);

		// create the partial IND
		InclusionDependency newIND = new InclusionDependency();
		newIND.setIncluded_distinct_values(dependent_relation.getCardinality());
		newIND.setIncludedAttribute(dependent_attribute);
		newIND.setIncludingAttribute(referenced_attribute);
		newIND.setIncludedRelation(dependent_relation);
		newIND.setIncludingRelation(referenced_relation);

		newIND.setCoefficient((double) common_values / dependent_attribute.getDistinct_values());

		dependent_relation.addIND(newIND);
		referenced_relation.addIND(newIND);
		pinds.add(newIND);

		// create the "mirroring" partial IND
		InclusionDependency mirroringIND = new InclusionDependency();
		mirroringIND.setIncluded_distinct_values(referenced_relation.getCardinality());
		mirroringIND.setIncludedAttribute(referenced_attribute);
		mirroringIND.setIncludingAttribute(dependent_attribute);
		mirroringIND.setIncludedRelation(referenced_relation);
		mirroringIND.setIncludingRelation(dependent_relation);

		mirroringIND.setCoefficient((double) common_values / referenced_attribute.getDistinct_values());

		dependent_relation.addIND(mirroringIND);
		referenced_relation.addIND(mirroringIND);
		pinds.add(mirroringIND);

		return pinds;

	}
	
	public static KeyConstraint generateCandidateKey(Relation relation, Attribute key_attribute) {
		KeyConstraint new_key = new KeyConstraint();
		new_key.setIsPrimaryKey(false);
		new_key.setRelation(relation);
		ArrayList<Attribute> keyAttrib = new ArrayList<>();
		keyAttrib.add(key_attribute);
		new_key.setKeyAttributes(keyAttrib);

		return new_key;
	}
	
	public static KeyConstraint generateKey(Relation relation, Attribute key_attribute) {
		KeyConstraint new_key = new KeyConstraint();
		new_key.setIsPrimaryKey(true);
		new_key.setRelation(relation);
		
		ArrayList<Attribute> keyAttrib = new ArrayList<>();
		keyAttrib.add(key_attribute);
		new_key.setKeyAttributes(keyAttrib);
		
		return new_key;
	}

	public static FKconstraint generateFK(Relation pk_relation, Relation fk_relation, KeyConstraint pk,
			Attribute fk_attribute) {
		FKconstraint newFK = new FKconstraint();
		newFK.setReferencedPK(pk);
		newFK.setFKTable(fk_relation);
		newFK.setPKTable(pk_relation);
		newFK.setFkAttribute(fk_attribute);

		return newFK;
	}

	public static FKconstraint generateFK(Relation pk_relation, Relation fk_relation, Attribute pk_attribute,
			Attribute fk_attribute) {
		KeyConstraint pk = generateKey(pk_relation, pk_attribute);

		FKconstraint newFK = new FKconstraint();
		newFK.setReferencedPK(pk);
		newFK.setFKTable(fk_relation);
		newFK.setPKTable(pk_relation);
		newFK.setFkAttribute(fk_attribute);

		return newFK;
	}

	/**
	 * Get the (partial) INDs between the attributes of two relations (if any).
	 * This will detect only unary INDs.
	 * @param relation1
	 * @param relation2
	 * @return
	 */
	public static ArrayList<InclusionDependency> getINDs(Relation relation1, Relation relation2) {
		
		ArrayList<InclusionDependency> inds = new ArrayList<>();
		
		ArrayList<Attribute> attributes1 = relation1.getAttributes();
		ArrayList<Attribute> attributes2 = relation2.getAttributes();
		
		for (Attribute a1:attributes1){
			for (Attribute a2:attributes2){
				InclusionDependency ind = getIND(a1,a2);
				if (ind!=null){
					inds.add(ind);
//					ind = getIND(a2,a1);
//					if (ind!=null)
//						inds.add(ind);
				}
			}
		}
		
		return inds;
	}
	
	/**
	 * Get the IND between two attributes.
	 * a1 = included, a2 = including
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static InclusionDependency getIND(Attribute a1, Attribute a2) {
		
		Parameters params1 = a1.getParameters();
		Parameters params2 = a2.getParameters();
		
		if (params1==null||params2==null)
			return null;
		
		if (params1.getStep()!= params2.getStep())
			return null;
		
		if (!params1.getDictionary().equals(params2.getDictionary()))
			return null;
		
		long step = params1.getStep();
		
		long common_values = (Math.min(params1.getOffset()+a1.getDistinct_values()*params1.getStep(), params2.getOffset()+a2.getDistinct_values()*params2.getStep())
				-Math.max(params1.getOffset(), params2.getOffset()))/step;
		
		if (common_values<=0)
			return null;
		
		double coefficient = (double)common_values/a1.getDistinct_values();
		
		if (coefficient==0)
			return null;
		
		InclusionDependency newIND = new InclusionDependency();
		
		newIND.setIncluded_distinct_values(a1.getDistinct_values());
		newIND.setIncludedAttribute(a1);
		newIND.setIncludingAttribute(a2);
		newIND.setIncludedRelation(a1.getParentRelation());
		newIND.setIncludingRelation(a2.getParentRelation());
		newIND.setCoefficient(coefficient);
		
		return newIND;
	}

	/**
	 * Return the (partial) INDs and keys of the input schemas.
	 * @param source_schemas
	 * @return
	 */
	public static ProfileData generateProfileData(
			ArrayList<DBSchema> source_schemas) {
		
		ArrayList<InclusionDependency> allINDs = new ArrayList<>();
		ArrayList<KeyConstraint> allKeys = new ArrayList<>();
		
		ArrayList<Relation> relations = new ArrayList<>();
		
		for (DBSchema dbschema:source_schemas)
		{
			relations.addAll(dbschema.getRelations());
//			allKeys.addAll(dbschema.getKeyCandidates());
			allKeys.addAll(dbschema.getPkConstraints());
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
	
	/**
	 * Input is an empty schema which needs parameters on all attributes so that dataFiller populates the FKs.
	 * @param schema
	 */
	public static void generateDataParameters(DBSchema schema, long min_cardinality, long max_cardinality) {
		
		if (schema==null)
			return;
		
		ArrayList<FKconstraint> fks = schema.getFkConstraints();
		
		for (FKconstraint fk:fks) {
			SchemaComponentsUtils.generateIND( min_cardinality, max_cardinality,
					fk.getFKTable(), fk.getPKTable(), fk.getFkAttribute(), fk.getReferencedPK().getKeyAttributes().get(0));
		}
		
		ArrayList<Relation> relations = schema.getRelations();
		for (Relation relation:relations)
		{
			if (relation.getCardinality()==-1)
				relation.setCardinality(Utils.get_random_index_range(min_cardinality, max_cardinality, false));
		}
		
	}
	
}
