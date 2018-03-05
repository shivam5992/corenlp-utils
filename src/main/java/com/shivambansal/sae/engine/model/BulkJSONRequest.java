package com.innovaccer.sae.engine.model;

/**
 * Created by sreeram on 16/7/14.
 */
public class BulkJSONRequest {
    String[] texts;
    public void setTexts(String[] texts){
        this.texts = texts;
    }
    public String[] getTexts(){
        return this.texts;
    }
}
