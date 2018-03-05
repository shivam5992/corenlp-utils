package com.innovaccer.sae.model.sentiment;

import java.util.ArrayList;
import java.util.List;

import com.innovaccer.sae.engine.model.Response;


public class Sentiment implements Response {
	
	List<senti> sentiments;
	public Sentiment() {
		sentiments = new ArrayList<senti>();
	}
	
	public void addToList(senti snt){
		this.sentiments.add(snt);
	}
}


