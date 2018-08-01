package uk.ac.man.synthegrate.schema_components;

import java.util.ArrayList;

import uk.ac.man.synthegrate.utils.Parameters;

public class Attribute {

	String name;
	String type;
	ArrayList<Object> values;
	boolean is_primary_key;
	boolean is_foreign_key;
	boolean is_unique;
	boolean not_null;
	
	boolean target_attribute;
	boolean expected_in_merge;
	
	//this will be !=null if target_attribute = true
	Attribute matched_target_attribute;
	
	//datafiller parameters 
	Parameters parameters = null;
	
	//this will be !=null if is_foreign_key = true;
	Attribute primary_key_attribute;
	
	long distinct_values = -1;
	
	Relation parentRelation;
	
	public Attribute()
	{
		type = "text";
		is_primary_key= false;
		is_foreign_key=false;
		is_unique=false;
		not_null = false;
		primary_key_attribute = null;
		parameters = null;
		target_attribute = false;
		expected_in_merge = false;
		matched_target_attribute = null;
	}
	
	/**
	 * Distinct values, parent relation, name are not copied.
	 * @param copied_attribute
	 */
	public Attribute(Attribute copied_attribute)
	{
		type = copied_attribute.type;
		is_primary_key= copied_attribute.is_primary_key;
		is_foreign_key=copied_attribute.is_foreign_key;
		is_unique = copied_attribute.is_unique;
		target_attribute = copied_attribute.target_attribute;
		not_null = copied_attribute.not_null;
		primary_key_attribute = copied_attribute.primary_key_attribute;
		parameters = copied_attribute.parameters;
		name = copied_attribute.name;
		expected_in_merge = copied_attribute.expected_in_merge;
		matched_target_attribute = copied_attribute.matched_target_attribute;
	}
	
	public boolean isNot_null() {
		return not_null;
	}

	public void setNot_null(boolean not_null) {
		this.not_null = not_null;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return name;
	}
	public ArrayList<Object> getValues() {
		return values;
	}
	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Attribute other = (Attribute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		return true;
	}
	public long getDistinct_values() {
		
		return distinct_values;
	}
	public void setDistinct_values(long distinct_values) {
		this.distinct_values = distinct_values;
	}

	public void set_primary_key(boolean b) {
		is_primary_key = b;
	}

	public boolean is_primary_key() {
		return is_primary_key;
	}

	public boolean is_unique() {
		return is_unique;
	}

	public void set_unique(boolean is_unique) {
		this.is_unique = is_unique;
	}

	public boolean is_foreign_key() {
		return is_foreign_key;
	}

	public void set_foreign_key(boolean is_foreign_key) {
		this.is_foreign_key = is_foreign_key;
		
		if (!is_foreign_key)
			primary_key_attribute = null;
	}

	public Attribute getPrimary_key_attribute() {
		return primary_key_attribute;
	}

	public void setPrimary_key_attribute(Attribute primary_key_attribute) {
		this.primary_key_attribute = primary_key_attribute;
	}

	public boolean isIs_primary_key() {
		return is_primary_key;
	}

	public void setIs_primary_key(boolean is_primary_key) {
		this.is_primary_key = is_primary_key;
	}

	public boolean isIs_foreign_key() {
		return is_foreign_key;
	}

	public void setIs_foreign_key(boolean is_foreign_key) {
		this.is_foreign_key = is_foreign_key;
	}

	public boolean isIs_unique() {
		return is_unique;
	}

	public void setIs_unique(boolean is_unique) {
		this.is_unique = is_unique;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	public String display_FK_relationship(){
		if (is_foreign_key)
			return name+"->"+primary_key_attribute.toString();
		else
			return "not FK";
	}

	public Relation getParentRelation() {
		return parentRelation;
	}

	public void setParentRelation(Relation parentRelation) {
		this.parentRelation = parentRelation;
		
		
//		if (is_primary_key)
//			parentRelation.setPrimaryKey(this);
//		
//		if (is_unique)
//			parentRelation.addCandidateKey(SchemaComponentsUtils.generateKey(parentRelation, this));
	}

	public boolean isTarget_attribute() {
		return target_attribute;
	}

	public void setTarget_attribute(boolean target_attribute) {
		this.target_attribute = target_attribute;
	}

	public boolean isExpected_in_merge() {
		return expected_in_merge;
	}

	public void setExpected_in_merge(boolean expected_in_merge) {
		this.expected_in_merge = expected_in_merge;
	}

	
	public Attribute getMatched_target_attribute() {
		return matched_target_attribute;
	}
	

	public void setMatched_target_attribute(Attribute matched_target_attribute) {
		this.matched_target_attribute = matched_target_attribute;
	}

	public String getInfo()
	{
		String info = "";
		
		info+="\n name:"+name;
		info+="\n relation:"+parentRelation.getName();
		info+="\n is_primary_key:"+is_primary_key;
		info+="\n is_FK_key:"+is_foreign_key;
		info+="\n is_unique:"+is_unique;
		info+="\n not_null:"+not_null;
		info+="\n expected_in_merge:"+expected_in_merge;
		info+="\n target_attribute:"+target_attribute;
		
		return info;
	}
	
	public String getFullName()
	{
		return parentRelation.getNameWSchema()+"."+getName();
	}
	
	public String getNameWRelation(){
		return parentRelation.getName()+"."+getName();
	}
}
