package com.innovaccer.sae.model.tree;

import java.util.ArrayList;
import java.util.List;
public class Treemodel {
	String label;
	List<Treemodel> nodes = new ArrayList<Treemodel>();
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Treemodel> getNodes() {
		return nodes;
	}

	public void addTonodes(Treemodel nodes) {
		this.nodes.add(nodes);
	}
}