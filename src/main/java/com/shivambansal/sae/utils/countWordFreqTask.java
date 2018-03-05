package com.innovaccer.sae.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/*
* Takes a sentences and returns a <Key, Value> pair of word Frequencies
*/
public class countWordFreqTask implements Callable{

    HashMap<String, Integer> wordFreq;
    StanfordCoreNLP mPipeline;
    CoreMap mSentence;

    public countWordFreqTask(CoreMap sentence, StanfordCoreNLP pipeline){
        this.mPipeline = pipeline;
        this.mSentence = sentence;
    }

    public HashMap<String, Integer> call(){
        wordFreq = new HashMap<String, Integer>();
        List<CoreLabel> tokens = mSentence.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token: tokens){
            String token_text = token.get(CoreAnnotations.TextAnnotation.class);
            if(wordFreq.containsKey(token_text)){
                Integer freq = wordFreq.get(token_text);
                freq = new Integer(freq.intValue() + 1);
                wordFreq.put(token_text, freq);
            }
            else{
                Integer freq = new Integer(1);
                wordFreq.put(token_text, freq);
            }
        }
        return wordFreq;
    }
}
