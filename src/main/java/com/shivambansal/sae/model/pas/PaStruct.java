package com.innovaccer.sae.model.pas;

import java.util.HashMap;

public class PaStruct {
	HashMap<String, String> subj = new HashMap<String, String>();
	HashMap<String, String> verb = new HashMap<String, String>();
	HashMap<String, String> obj = new HashMap<String, String>();

	public PaStruct(HashMap<String, String> subj, HashMap<String, String> verb, HashMap<String, String> obj) {
		this.subj = subj;
		this.verb = verb;
		this.obj = obj;
	}
}
