package com.innovaccer.sae.model.ner;

public class nermodel {
	String word = "";
	String ner = "";
    String pos = "";
	
	public nermodel(String wrd, String ner, String pos){
		this.word =wrd;
		this.ner = ner;
        this.pos = pos;
	}
}
