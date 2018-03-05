package com.innovaccer.sae.model.ner;

import java.util.ArrayList;
import java.util.List;
public class nerlist {
	List<nermodel> nerlist = new ArrayList<nermodel>();
		
	public void addToList(nermodel nerm){
		this.nerlist.add(nerm);
	}
}
