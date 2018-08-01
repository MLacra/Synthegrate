package uk.ac.man.synthegrate.schema_components;

public class Value {

	String value;
	Attribute parentAttribute;
	Tuple parentTuple;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Attribute getParentAttribute() {
		return parentAttribute;
	}
	public void setParentAttribute(Attribute parentAttribute) {
		this.parentAttribute = parentAttribute;
	}
	public Tuple getParentTuple() {
		return parentTuple;
	}
	public void setParentTuple(Tuple parentTuple) {
		this.parentTuple = parentTuple;
	}
	@Override
	public String toString(){
		return value;
	}
	
	
	
}
