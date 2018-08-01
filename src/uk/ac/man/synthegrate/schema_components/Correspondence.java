package uk.ac.man.synthegrate.schema_components;

public class Correspondence {

	String id;
	String sourceSchema;
	String sourceRelation;
	String sourceAttribute;
	String targetSchema;
	String targetRelation;
	String targetAttribute;
	
	String coefficient = "0.0";

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCoefficient() {
		return coefficient;
	}
	public void setCoefficient(String coefficient) {
		this.coefficient = coefficient;
	}
	public String getSourceSchema() {
		return sourceSchema;
	}
	public void setSourceSchema(String sourceSchema) {
		this.sourceSchema = sourceSchema;
	}
	public String getSourceRelation() {
		return sourceRelation;
	}
	public void setSourceRelation(String sourceRelation) {
		this.sourceRelation = sourceRelation;
	}
	public String getSourceAttribute() {
		return sourceAttribute;
	}
	public void setSourceAttribute(String sourceAttribute) {
		this.sourceAttribute = sourceAttribute;
	}
	public String getTargetSchema() {
		return targetSchema;
	}
	public void setTargetSchema(String targetSchema) {
		this.targetSchema = targetSchema;
	}
	public String getTargetRelation() {
		return targetRelation;
	}
	public void setTargetRelation(String targetRelation) {
		this.targetRelation = targetRelation;
	}
	public String getTargetAttribute() {
		return targetAttribute;
	}
	public void setTargetAttribute(String targetAttribute) {
		this.targetAttribute = targetAttribute;
	}
	
	public String getFullSourcePath(){
		return sourceSchema+"."+sourceRelation+"."+sourceAttribute;
	}
	
	public String getFullTargetPath(){
		return targetSchema+"."+targetRelation+"."+targetAttribute;
	}
	
}
