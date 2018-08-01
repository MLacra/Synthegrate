package uk.ac.man.synthegrate.schema_components;

import java.util.ArrayList;
import java.util.HashSet;

public class DBSchema {

	String name;
	ArrayList<Relation> relations;
	ArrayList<FKconstraint> fkConstraints;
	
	//All primary keys in all tables 
	ArrayList<KeyConstraint> pkConstraints;
	
	//All unique constraints = key candidates
	ArrayList<KeyConstraint> keyCandidates;
	
	public DBSchema()
	{
		name = "undefined";
		relations = new ArrayList<>();
		fkConstraints = new ArrayList<>();
		pkConstraints = new ArrayList<>();
		keyCandidates = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Relation> getRelations() {
		return relations;
	}
	public void setRelations(ArrayList<Relation> relations) {
		this.relations = relations;
	}
	public ArrayList<FKconstraint> getFkConstraints() {
		return fkConstraints;
	}
	public void setFkConstraints(ArrayList<FKconstraint> fkConstraints) {
		this.fkConstraints = fkConstraints;
	}
	public ArrayList<KeyConstraint> getPkConstraints() {
		pkConstraints = new ArrayList<>(new HashSet<>(pkConstraints));
		return pkConstraints;
	}
	public void setPkConstraints(ArrayList<KeyConstraint> pkConstraints) {
		this.pkConstraints = pkConstraints;
	}
	public KeyConstraint getPKConstraintByName(String pkName){
		for (KeyConstraint pk:pkConstraints)
		{
			if (pk.getName().equals(pkName))
				return pk;
		}
		return null;
	}
	
	public Relation getRelationByName(String tableName)
	{
		for (Relation relation:relations)
		{
			if (relation.getName().equals(tableName))
				return relation;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DBSchema other = (DBSchema) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public ArrayList<KeyConstraint> getKeyCandidates() {
		return keyCandidates;
	}
	
	public void setKeyCandidates(ArrayList<KeyConstraint> keyCandidates) {
		this.keyCandidates = keyCandidates;
	}

	public void addRelation (Relation newR){
		if (relations==null)
			relations = new ArrayList<>();
		
		relations.add(newR);
	}
	
	public void removeRelation (Relation relation){
		if (relation==null)
			return;
		if (relations == null)
			return;
		
		relation.setParentSchema(null);	
		relations.remove(relation);
	}
	
	public void addPK (Attribute newPK){
		KeyConstraint pk = new KeyConstraint();
		pk.setIsPrimaryKey(true);
		pk.setRelation(newPK.getParentRelation());
		ArrayList<Attribute> keyAttrib = new ArrayList<>();
		keyAttrib.add(newPK);
		pk.setKeyAttributes(keyAttrib);
		
		if (pkConstraints==null)
			pkConstraints = new ArrayList<>();
		
		pkConstraints.add(pk);
	}
	
	public void addPK (KeyConstraint newPK){
		if (newPK!=null)
			pkConstraints.add(newPK);
	}

	public void addFK(FKconstraint new_fk) {
		if (fkConstraints==null)
			fkConstraints = new ArrayList<>();
		
		fkConstraints.add(new_fk);
		
	}
	
	public void addCK(KeyConstraint newCK) {
		if (newCK!=null)
			keyCandidates.add(newCK);
	}
	
	public void setRelationsWritten (boolean isWritten) {
		
		if (relations==null||relations.isEmpty())
			return;
		
		for (Relation relation : relations) {
			relation.setWritten(isWritten);
		}
	}
}
