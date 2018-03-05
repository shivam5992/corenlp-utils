package com.innovaccer.sae.model.pas;

import java.util.List;
import java.util.ArrayList;


public class PaStructList {
	
	List<PaStruct> paslist = new ArrayList<PaStruct>();
	
	public void addToList(PaStruct pas){
		this.paslist.add(pas);
	}
}
