package com.innovaccer.sae.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokensRegexAnnotator;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Return Incase parallel execution s necessary.
 */
public class CountWordTask implements Callable {
    StanfordCoreNLP mPipeline;
    CoreMap mSentence;

    public CountWordTask(CoreMap sentence, StanfordCoreNLP pipeline){
        mSentence = sentence;
        mPipeline = pipeline;
    }
    public Integer call(){
        List<CoreLabel> tokens = mSentence.get(CoreAnnotations.TokensAnnotation.class);
        Integer count = new Integer(tokens.size());
        return count;
    }
}
