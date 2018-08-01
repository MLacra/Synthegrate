package uk.ac.man.synthegrate.schema_components;

import java.util.ArrayList;

public class ProfileData {
	
	ArrayList<InclusionDependency> inds ;
	ArrayList<KeyConstraint> keys;
	
	
	public ProfileData(){
		inds = new ArrayList<>();
		keys = new ArrayList<>();
	}
	
	public ArrayList<InclusionDependency> getInds() {
		return inds;
	}
	public void setInds(ArrayList<InclusionDependency> inds) {
		this.inds = inds;
	}
	public ArrayList<KeyConstraint> getKeys() {
		return keys;
	}
	public void setKeys(ArrayList<KeyConstraint> keys) {
		this.keys = keys;
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
		
		if (keys==null)
			keys = new ArrayList<>();
		
		if (!keys.contains(key))
			keys.add(key);
	}


}
