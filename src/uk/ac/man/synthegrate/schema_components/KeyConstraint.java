package uk.ac.man.synthegrate.schema_components;

import java.util.ArrayList;

import uk.ac.man.synthegrate.utils.Utils;


public class KeyConstraint {

	String name;
	Relation relation;
	ArrayList<Attribute> keyAttributes;
	Boolean isPrimaryKey;
	
	public KeyConstraint()
	{
		keyAttributes = new ArrayList<>();
		isPrimaryKey = false;
	}

	

	public Relation getRelation() {
		return relation;
	}



	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public void setRelationName(String name){
		this.relation = new Relation();
		relation.setName(name);
	}


	public ArrayList<Attribute> getKeyAttributes() {
		return keyAttributes;
	}

	public void setKeyAttributes(ArrayList<Attribute> keyAttributes) {
		this.keyAttributes = ((ArrayList<Attribute>) keyAttributes);
		
	}
	
	public void setKeyAttributesNames(ArrayList<String> keyAttributesNames) {
		
		for (String name:keyAttributesNames){
			Attribute newAttribute = new Attribute();
			newAttribute.setName(name);
			this.keyAttributes.add(newAttribute);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

	public Boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setIsPrimaryKey(Boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
		for (Attribute attribute:keyAttributes){
			attribute.set_primary_key(isPrimaryKey);
		}
	}
	
	public ArrayList<String> getKeyAttributesNames(){
		
		ArrayList<String> result = new ArrayList<>();
		
		for (Attribute attribute:keyAttributes){
			
			result.add(attribute.getName());
		}
		return result;
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
		KeyConstraint other = (KeyConstraint) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		if (keyAttributes == null) {
			if (other.keyAttributes != null)
				return false;
		} 
		if (Utils.arraysEqual(keyAttributes, other.keyAttributes))
			return true;
		else
			return false;
		
//		return true;
	}

	@Override
	public String toString() {
		return "KeyConstraint [keyAttributes=" + keyAttributes + ", isPrimaryKey=" + isPrimaryKey+ "]";
	} 
	
	
	
	
}
