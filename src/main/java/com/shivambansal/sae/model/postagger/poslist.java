package com.innovaccer.sae.model.postagger;

import java.util.ArrayList;
import java.util.List;


public class poslist {
	List<posmodel> poslist = new ArrayList<posmodel>();
	
	public void addToList(posmodel posm){
		this.poslist.add(posm);
	}
}
