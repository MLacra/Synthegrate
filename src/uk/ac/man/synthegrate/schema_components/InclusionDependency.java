package uk.ac.man.synthegrate.schema_components;

import java.text.DecimalFormat;


public class InclusionDependency {
	
//	private static DecimalFormat df2 = new DecimalFormat(".##");
	private static DecimalFormat df1 = new DecimalFormat(".#");

	/*
	 * In the pair - first attribute is included by the second attribute
	 */
	Relation includedRelation;
	Relation includingRelation;

	Attribute includedAttribute;
	Attribute includingAttribute;
	

	// The overlap - what fraction of the values from the first attributes are
	// included in the values from the second attributes.
	// Partial INDs are only for single attributes.
	Double coefficient = 0.0;

	long included_distinct_values = 0l;
	
	boolean is_distraction = false;

	public InclusionDependency() {
		is_distraction = false;
	}
	
	public String display_all_info(){
		String info = "\n";
		info+="ov("+includedRelation.getName()+"."+includedAttribute.getName()+", "+includingRelation.getName()+"."+includingAttribute.getName()+") = "+coefficient+"\n";
		info += "Included relation: "+includedRelation.getName()+"\n";
		info += "Including relation: "+includingRelation.getName()+"\n";
		info += "Included attribute: "+includedAttribute.getName()+"\n";
		info += "Included attribute paramteres: "+includedAttribute.getParameters().toString()+"\n";
		info += "Including attribute: "+includingAttribute.getName()+"\n";
		info += "Including attribute paramteres: "+includingAttribute.getParameters().toString()+"\n";
		info += "Coefficient : "+coefficient+"\n";
		info += "Included distinct values: "+ included_distinct_values+"\n";
		info += "Common values: "+ coefficient*included_distinct_values+"\n";
		return info;
	}
	
	


	public Double getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(Double coefficient) {
		//keep just the first two decimals - testing purposes
		if (!coefficient.isNaN())
			this.coefficient = coefficient;
		else
			this.coefficient = 0.0;
		//this.coefficient = Double.parseDouble(df2.format(coefficient));
	}

	/**
	 * Get the coefficient only with the first decimal. This is used when comparing two INDs.
	 * @return double
	 */
	public Double getFirstDecimalCoefficient()
	{
		if (coefficient!=null)
			return Double.parseDouble(df1.format(coefficient));
		else return null;
	}
	
	public Relation getIncludedRelation() {
		return includedRelation;
	}

	public void setIncludedRelation(Relation includedRelation) {
		this.includedRelation = includedRelation;
	}

	public Relation getIncludingRelation() {
		return includingRelation;
	}

	public void setIncludingRelation(Relation includingRelation) {
		this.includingRelation = includingRelation;
	}


	/**
	 * Checks if the parameter are the included part.
	 * 
	 * @param attributes
	 * @return
	 */
	public boolean equalIncluded(Attribute attribute) {

		if (attribute.equals(includedAttribute))
			return true;
		return false;
	}

	/**
	 * Checks if the parameter are the including part.
	 * 
	 * @param attributes
	 * @return
	 */
	public boolean equalIncluding(Attribute attribute) {
		
		if (attribute.equals(includingAttribute))
			return true;
		
		return false;
	}

	
	@Override
	public String toString() {
		return "ov(" + includedAttribute.toString() +","+includingAttribute.toString()+ ") = " + getCoefficient() + " dV("+includedAttribute.toString()+" )= "+included_distinct_values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getFirstDecimalCoefficient() == null) ? 0 : getFirstDecimalCoefficient().hashCode());
		
		result = prime * result + ((includedAttribute == null) ? 0 :includedAttribute.hashCode());
		result = prime * result + ((includingAttribute == null) ? 0 :includingAttribute.hashCode());
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
		InclusionDependency other = (InclusionDependency) obj;
		if (coefficient == null) {
			if (other.coefficient != null)
				return false;
		} else if (!this.getFirstDecimalCoefficient().equals(other.getFirstDecimalCoefficient()))
			return false;
		if (includedAttribute == null) {
			if (other.includedAttribute != null)
				return false;
		} else 
			if (!includedAttribute.equals(other.includedAttribute))
				return false;
			else if (included_distinct_values != other.included_distinct_values)
				return false;
		if (includingAttribute == null) {
			if (other.includingAttribute != null)
				return false;
		}
			else 
				if (!includingAttribute.equals(other.includingAttribute))
					return false;
		return true;
	}

	public long getIncluded_distinct_values() {
		return included_distinct_values;
	}

	public void setIncluded_distinct_values(long included_distinct_values) {
		this.included_distinct_values = included_distinct_values;
	}


	public Attribute getIncludedAttribute() {
		return includedAttribute;
	}


	public void setIncludedAttribute(Attribute includedAttribute) {
		this.includedAttribute = includedAttribute;
	}


	public Attribute getIncludingAttribute() {
		return includingAttribute;
	}


	public void setIncludingAttribute(Attribute includingAttribute) {
		this.includingAttribute = includingAttribute;
	}

	public boolean is_distraction() {
		return is_distraction;
	}

	public void set_distraction(boolean is_distraction) {
		this.is_distraction = is_distraction;
	}

	
	
	

}
