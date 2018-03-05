package com.innovaccer.sae.engine.model;
/**
 * 
 * @author Mohsin Akhtar
 *
 */
public class Request {

	String text;
	String params[];
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String[] getParams() {
		return params;
	}
	public void setParams(String[] params) {
		this.params = params;
	}
	
	
}
