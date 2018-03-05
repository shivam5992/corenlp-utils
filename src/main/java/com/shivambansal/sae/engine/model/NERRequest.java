package com.innovaccer.sae.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sreeram on 2/7/14.
 */
public class NERRequest {
    public String[] nouns;
    public String[] getNouns(){return nouns;}
    public void setNouns(String[] nouns){this.nouns = nouns;}
}

