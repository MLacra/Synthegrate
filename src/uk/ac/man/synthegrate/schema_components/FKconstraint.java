package uk.ac.man.synthegrate.schema_components;

public class FKconstraint {

	String name;
	Relation FKTable;
	Relation PKTable;
	KeyConstraint pkAttribute;
	Attribute fkAttribute;
	
	long distinct_values_FK = 0;
	
	public FKconstraint()
	{
		name = "undefined";
	}
	
	public KeyConstraint getReferencedPK() {
		return pkAttribute;
	}
	public void setReferencedPK(KeyConstraint referencedPK) {
		this.pkAttribute = referencedPK;
		
		//TODO - this does not work for multi-attribute FKs
		if (fkAttribute!=null&&fkAttribute.getPrimary_key_attribute()==null)
			fkAttribute.setPrimary_key_attribute(referencedPK.keyAttributes.get(0));
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	
	public Relation getFKTable() {
		return FKTable;
	}

	public void setFKTable(Relation fKTable) {
		FKTable = fKTable;
	}

	public Relation getPKTable() {
		return PKTable;
	}

	public void setPKTable(Relation pKTable) {
		PKTable = pKTable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pkAttribute == null) ? 0 : pkAttribute.hashCode());
		result = prime * result + ((fkAttribute == null) ? 0 : fkAttribute.hashCode());
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
		FKconstraint other = (FKconstraint) obj;
		if (pkAttribute == null) {
			if (other.pkAttribute != null)
				return false;
		} else if (!pkAttribute.equals(other.pkAttribute))
			return false;
		if (fkAttribute == null) {
			if (other.fkAttribute != null)
				return false;
		} else if (!fkAttribute.equals(other.fkAttribute))
			return false;
		return true;
	}


	public long getDistinct_values_FK() {
		return distinct_values_FK;
	}

	public void setDistinct_values_FK(long distinct_values_FK) {
		this.distinct_values_FK = distinct_values_FK;
	}

	public Attribute getFkAttribute() {
		return fkAttribute;
	}

	public void setFkAttribute(Attribute fkAttribute) {
		this.fkAttribute = fkAttribute;
	}
	
	
	
}
