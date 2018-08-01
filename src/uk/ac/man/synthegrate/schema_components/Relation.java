package uk.ac.man.synthegrate.schema_components;

import java.util.ArrayList;
import java.util.HashSet;

import uk.ac.man.synthegrate.utils.SchemaComponentsUtils;

public class Relation implements Comparable<Relation>{

	String name;
	DBSchema parentSchema;
	Attribute primaryKey;
	ArrayList<Attribute> attributes;
	ArrayList<InclusionDependency> inds;
	ArrayList<KeyConstraint> candidateKeys;
	ArrayList<Tuple> tuples;
	long cardinality = -1;
	long null_estimation = -1;
	
	//if the relation was written in file already
	boolean written = false;
	
	
	public Relation(){
		
		inds = new ArrayList<>();
		attributes= new ArrayList<>();
		candidateKeys = new ArrayList<>();
		inds = new ArrayList<>();
		primaryKey = null;
		parentSchema = null;
		written = false;
	}
	
	public String getName() {
	
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DBSchema getParentSchema() {
		return parentSchema;
	}

	public void setParentSchema(DBSchema parentSchema) {
		this.parentSchema = parentSchema;
	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}
	
	public ArrayList<String> getAttributeNames()
	{
		ArrayList<String> result = new ArrayList<>();
		
		for (Attribute attribute: attributes)
		{
			result.add(attribute.getName());
		}
		return result;
	}
	
	public ArrayList<String> getExpectedAttributeNames()
	{
		ArrayList<String> result = new ArrayList<>();
		
		for (Attribute attribute: attributes)
		{
			if (attribute.isExpected_in_merge())
				result.add(attribute.getName());
		}
		
		return result;
	}
	
	public Attribute getAttributeByName(String attributeName)
	{
		for (Attribute attribute: attributes)
		{
			if (attribute.getName().equals(attributeName))
				return attribute;
		}
		return null;
	}
	
	public boolean checkDuplicateParameters(Attribute newAttribute)
	{
		for (Attribute attribute: attributes){
			
			if (attribute.getParameters()!=null && attribute.getParameters().equals(newAttribute.getParameters()))
				return true;
		}
		
		return false;
	}

	public void addAttribute(Attribute newAttribute)
	{
		if (attributes==null)
			attributes = new ArrayList<>();
		
		if (!attributes.contains(newAttribute))
		{
			attributes.add(newAttribute);
		}
		if (newAttribute.getParentRelation()==null || newAttribute.getParentRelation()!=this)
			newAttribute.setParentRelation(this);
	}

	public void setAttributes(ArrayList<Attribute> attributes) {
		
		this.attributes = attributes;
	}


	public ArrayList<Tuple> getTuples() {
		if (tuples==null)
			tuples = new ArrayList<>();
		return tuples;
	}

	public ArrayList<String> getValuesByAttribute(String attributeName)
	{
		ArrayList<String> values = new ArrayList<>();
		
		for (Tuple tuple: tuples)
		{
			values.add(tuple.getValueByAttribute(attributeName).getValue());
		}
		return values;
	}


	public void setTuples(ArrayList<Tuple> tuples) {
		this.tuples = tuples;
	}

	@Override
	public String toString() {
		if (parentSchema!=null)
			return parentSchema.getName()+"."+name+attributes.toString();
		else
			return name+attributes.toString();
	}
	
	public String getNameWSchema()
	{
		if (parentSchema!=null)
			return parentSchema.getName()+"."+name;
		else
			return name;
	}
	
	public String displayAllInfo(){
		String all_info = "\n";
		if (parentSchema!=null)
			all_info += "Schema: "+parentSchema.getName()+"\n";
		else
			all_info += "Schema: "+"not defined"+"\n";
		all_info += "Name: "+name+"\n";
		all_info += "Attributes["+attributes.size()+"]: "+attributes.toString()+"\n";
		if (primaryKey!=null)
			all_info += "Primary key: " + primaryKey.toString()+"\n";
		else
			all_info += "Primary key: " + "not defined"+"\n";
		ArrayList<String> fks_strings= getFKs_strings();
		all_info += "Foreign keys["+fks_strings.size()+"]: " + fks_strings.toString()+"\n";
		
		all_info += "Cardinality: " + cardinality +"\n";
		all_info += "INDs["+inds.size()+"]: " + inds.toString();
		return all_info;
	}
	
	public ArrayList<String> getFKs_strings(){
		ArrayList<String> result = new ArrayList<>();
		for (Attribute attribute:attributes){
			if (attribute.is_foreign_key())
				result.add(attribute.display_FK_relationship());
		}
		
		return result;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentSchema == null) ? 0 : parentSchema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relation other = (Relation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentSchema == null) {
			if (other.parentSchema != null)
				return false;
		} else if (!parentSchema.equals(other.parentSchema))
			return false;
		return true;
	}

	@Override
	public int compareTo(Relation o) {
		
		return this.getName().compareTo(o.getName());
		
//		Integer index1 = Integer.parseInt(this.name.substring(9));
//		Integer index2 = Integer.parseInt(o.name.substring(9));
//		return index1.compareTo(index2);
	}

	public long getCardinality() {
		return cardinality;
	}

	public void setCardinality(long cardinality) {
		this.cardinality = cardinality;
	}

	public long getNull_estimation() {
		return null_estimation;
	}

	public void setNull_estimation(long null_estimation) {
		this.null_estimation = null_estimation;
	}
	
	public Attribute getPrimaryKey() {
		return primaryKey;
	}
	
	public void setPrimaryKey(Attribute primaryKey) {
		this.primaryKey = primaryKey;
		primaryKey.set_unique(true);
		primaryKey.setParentRelation(this);
		
		KeyConstraint key = SchemaComponentsUtils.generateKey(this, primaryKey);
		candidateKeys.add(key);
		parentSchema.addPK(primaryKey);
	}

	public boolean containsPK() {
		
		for (Attribute attribute: attributes)
			if (attribute.is_primary_key()){
				if (primaryKey==null)
					primaryKey = attribute;
				return true;
			}
		return false;
	}

	public boolean containsFK()
	{
		for (Attribute attribute: attributes){
			if (attribute.is_foreign_key())
				return true;
		}
		 return false;
		
	}
	
	public ArrayList<Attribute> getFKs()
	{
		ArrayList<Attribute> fks = new ArrayList<>();
		for (Attribute attribute:attributes)
		{
			if (attribute.is_foreign_key())
			{
				fks.add(attribute);
			}
		}
		
		return fks;
	}
	
	public boolean hasINDs()
	{
		if (inds.isEmpty())
			return false;
		else
			return true;
	}
	
	public ArrayList<InclusionDependency> getINDs(){
		return inds;
	}
	
	public void addIND(InclusionDependency ind)
	{
		if (ind==null)
			return;
		
		if (inds==null)
			inds = new ArrayList<>();
		
		if (!inds.contains(ind))
			inds.add(ind);
		
	}
	
	public void addCandidateKey(KeyConstraint key)
	{
		if (key==null)
			return;
		
		if (candidateKeys==null)
			candidateKeys = new ArrayList<>();
		
		if (!candidateKeys.contains(key))
			candidateKeys.add(key);
	}

	public KeyConstraint getCandidateKeywPK() {
		for (KeyConstraint key:candidateKeys)
		{
			if (key.isPrimaryKey())
				return key;
		}
		return null;
	}
	
	public ArrayList<InclusionDependency> getInds() {
		return inds;
	}

	public void setInds(ArrayList<InclusionDependency> inds) {
		this.inds = inds;
	}

	public ArrayList<KeyConstraint> getCandidateKeys() {
		
		/* This should not be done like this, but there is a key missing each time */
		candidateKeys = new ArrayList<>();
		for (Attribute a: attributes)
		{
			if (a.is_unique){
				if (a.is_primary_key){
					KeyConstraint key = SchemaComponentsUtils.generateKey(this, a);
					candidateKeys.add(key);
				}
				else
				{
					KeyConstraint key = SchemaComponentsUtils.generateCandidateKey(this, a);
					candidateKeys.add(key);
				}
			}
		}
		
		
		return candidateKeys;
	}

	public void setCandidateKeys(ArrayList<KeyConstraint> candidateKeys) {
		this.candidateKeys = candidateKeys;
	}

	public boolean isWritten() {
		return written;
	}

	public void setWritten(boolean written) {
		this.written = written;
	}

	public void addINDs(ArrayList<InclusionDependency> pinds) {
		if (pinds == null)
			return;
		
		if (inds == null)
			inds = new ArrayList<>();
		
		inds.addAll(pinds);
		
		//Eliminate duplicates
		inds = new ArrayList<>(new HashSet<>(inds));
	}
	
	public void deleteFKeyWithPK(Attribute pk)
	{
		for (Attribute a:attributes)
		{
			if (a.is_foreign_key())
				if (a.getPrimary_key_attribute().equals(pk)){
					a.setPrimary_key_attribute(null);
					a.set_foreign_key(false);
				}
				
		}
		
		
	}
	
	public ArrayList<Attribute> getMatchedAttributes()
	{
		ArrayList<Attribute> matched_attributes = new ArrayList<>();
		
		for (Attribute a: attributes)
		{
			if (a.target_attribute)
				matched_attributes.add(a);
		}
		return matched_attributes;
	}
	
	public int getSizeMatchedAttributes()
	{
		int size = 0;
		for (Attribute a: attributes)
		{
			if (a.target_attribute)
				size++;
		}
		return size;
	}
	
	public ArrayList<Attribute> getUniqueAttributes()
	{
		ArrayList<Attribute> unique_attributes = new ArrayList<>();
		
		for (Attribute a: attributes)
		{
			if (a.is_unique)
				unique_attributes.add(a);
		} 
		
		return unique_attributes;
	}
	
	public boolean containsAttribute(Attribute a)
	{
		return attributes.contains(a);
	}
}
