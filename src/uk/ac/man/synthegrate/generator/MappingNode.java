package uk.ac.man.synthegrate.generator;

import uk.ac.man.synthegrate.schema_components.Relation;
import uk.ac.man.synthegrate.utils.Constants;

public class MappingNode {

	Relation relation;
	
	MappingNode parent;
	
	MappingNode left_child;
	MappingNode right_child;
	
	String operation="";
	
	String condition ="";
	
	boolean is_root = false;
	
	//it will be union extendible if by extending it doesn't violate a FK constraint. 
	//It will be set on false only if the relation contains the PK for the FK constraint.
	boolean union_extendible = true;
	
	int merge_opportunity = -1;
	
	@Override
	public String toString()
	{
		//if it is a leave node then there is no child to explore
		if (!is_root&&right_child==null&&left_child==null)
				if (parent.operation.equals(Constants.UNION_OPERATION))
					return "(SELECT "+ String.join(",", relation.getExpectedAttributeNames())+ " FROM "+relation.getNameWSchema()+")";
				else
					//if (parent.operation.equals(""))
						return relation.getNameWSchema();
		
		//it's and intermediate node
		String mapping ="";
		if (is_root){
			mapping = "SELECT "+String.join(",", relation.getAttributeNames())+" FROM ";
		}
		
		if (operation == Constants.JOIN_OPERATION){
				mapping += "(SELECT "+ String.join(",", relation.getAttributeNames())+ " FROM ";//getExpectedAttributeNames
				mapping	+= left_child.toString()+" AS "+left_child.getRelation().getName()+" ";
				mapping += operation +" ";
				mapping += right_child.toString()+" AS "+right_child.getRelation().getName();
				mapping += " "+Constants.ON+" "+condition;
				mapping += ")";
			}else
				if (operation == Constants.UNION_OPERATION){
					mapping += "(";
					mapping	+= left_child.toString();
					mapping += operation +" ";
					mapping += right_child.toString();
					mapping += ") ";
				}
		
			if (is_root){
				mapping += " as "+relation.getName()+" ORDER BY "+String.join(",", relation.getAttributeNames());
			}
			
		return mapping; 
	}
	
	public String getSimplifiedMapping(int level) {
		// if it is a leave node then there is no child to explore
		if (!is_root && right_child == null && left_child == null)
			if (parent.operation.equals(Constants.UNION_OPERATION))
				return relation.getNameWSchema();
			else
				// if (parent.operation.equals(""))
				return relation.getNameWSchema();

		// it's and intermediate node
		String mapping = "";
		if (is_root) {
			mapping = "SELECT " + String.join(",", relation.getAttributeNames()) + " FROM ";
		}
		String indents = "";
		for (int i = 0; i < level; i++)
			indents += "\t";
		mapping += "\n" + indents + "(\n";
		if (operation == Constants.JOIN_OPERATION) {

			mapping += indents + "\t" + left_child.getSimplifiedMapping(level + 1) + " AS "
					+ left_child.getRelation().getName() + "\n";
			mapping += indents + operation + "\n";
			mapping += indents + "\t" + right_child.getSimplifiedMapping(level + 1) + " AS "
					+ right_child.getRelation().getName() + "\n";
			mapping += indents + Constants.ON + " " + condition + "\n";

		} else if (operation == Constants.UNION_OPERATION) {
			mapping += indents + "\t" + left_child.getSimplifiedMapping(level + 1) + "\n";
			mapping += indents + operation + "\n ";
			mapping += indents + "\t" + right_child.getSimplifiedMapping(level + 1) + "\n";
		}
		mapping += indents + ") ";
		if (is_root) {
			mapping += "\n as " + relation.getName() + "\nORDER BY " + String.join(",", relation.getAttributeNames());
		}

		return mapping;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public MappingNode getParent() {
		return parent;
	}

	public void setParent(MappingNode parent) {
		this.parent = parent;
	}

	public MappingNode getLeft_child() {
		return left_child;
	}

	public void setLeft_child(MappingNode left_child) {
		this.left_child = left_child;
	}

	public MappingNode getRight_child() {
		return right_child;
	}

	public void setRight_child(MappingNode right_child) {
		this.right_child = right_child;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public boolean isRoot() {
		return is_root;
	}

	public void setRoot(boolean is_root) {
		this.is_root = is_root;
	}

	public int getMerge_opportunity() {
		return merge_opportunity;
	}
	
	public void setMerge_opportunity(int merge_opportunity) {
		this.merge_opportunity = merge_opportunity;
	}
	
	public MappingNode getOtherSibling(MappingNode child){
		
		if (child.equals(left_child))
			return right_child;
		else
			return left_child;
		
	}

	
	public boolean isUnion_extendible() {
		return union_extendible;
	}

	
	public void setUnion_extendible(boolean union_extendible) {
		this.union_extendible = union_extendible;
	}
}
