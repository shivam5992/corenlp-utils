package com.innovaccer.sae.utils;

import com.google.gson.Gson;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/*
*Calculates Basic Dependency graph for TextCorpus
*/
public class BDTask implements Callable{
    StanfordCoreNLP mPipeline;
    String mSentenceRawText;
    CoreMap mSentence;
    boolean isFile;
    SemanticGraph dependencies = null;
    final Logger logger = LoggerFactory.getLogger(BDTask.class);

    public ArrayList<Object> call() {
        ArrayList<Object> ObjectResult = new ArrayList<Object>();
        if(isFile) {
            Gson gson = new Gson();
            Annotation document = new Annotation(mSentenceRawText);
            mPipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                logger.info(sentence.toString());
                dependencies = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                Object result = TreeUtils.getTree(dependencies.toString());
                logger.info("File: Done");
                ObjectResult.add(result);
            }
            logger.info("Thread Execution Done");
            return ObjectResult;
        }
        else{
            dependencies = mSentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            Object result = TreeUtils.getTree(dependencies.toString());
            logger.info("JsonRequest: Done");
            ObjectResult.add(result);
            return ObjectResult;
        }

    }

    public BDTask(String sentence, StanfordCoreNLP pipeline) {
        mSentenceRawText = sentence;
        isFile = true;
        mPipeline = pipeline;

    }

    public BDTask(CoreMap sentence, StanfordCoreNLP pipeline) {
        mSentence = sentence;
        isFile = false;
        mPipeline = pipeline;
    }

}
