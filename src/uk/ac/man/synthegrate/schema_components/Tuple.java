package uk.ac.man.synthegrate.schema_components;

import java.util.HashMap;

public class Tuple {

	//the key is the column and the value is the actual value in the cell
	HashMap<Attribute,Value> values;

	
	public Tuple ()
	{
		values = new HashMap<>();
	}

	public HashMap<Attribute, Value> getValues() {
		return values;
	}
	
	public Value getValueByAttribute(String attributeName)
	{
		for (Attribute attribute:values.keySet())
		{
			if (attribute.getName().equals(attributeName))
				return values.get(attribute);
		}
		return null;
	}

	public void putValue(Attribute attribute, Value val)
	{
		if (values==null)
			values = new HashMap<>();
		values.put(attribute, val);
	}
	
	public void setValues(HashMap<Attribute, Value> values) {
		this.values = values;
	}


	

}
