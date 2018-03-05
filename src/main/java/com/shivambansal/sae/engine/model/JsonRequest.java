package com.innovaccer.sae.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonRequest {
    String sentence;
    boolean bd;
    boolean sentiment;
    String delimiter;
    //boolean isDelimiterRegex;

    @JsonProperty("sentiment")
    public boolean getSentiment(){return sentiment;}

    @JsonProperty("sentence")
    public String getSentence(){
        return sentence;
    }
    //public String getDelimiter(){
     //   return delimiter;
    //}
    public void setSentence(String input){this.sentence = input;}
    public void setSentimentOpt(boolean sentiment){this.sentiment = sentiment;}
    public void setBDOpt(boolean bd){this.bd = bd;}

    @JsonProperty("bd")
    public boolean getBd(){return bd;}

    @JsonProperty("delimiter")
    public String getDelimiter(){return delimiter;}



}
