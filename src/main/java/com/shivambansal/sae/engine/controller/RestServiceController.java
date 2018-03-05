package com.innovaccer.sae.engine.controller;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;



import com.google.gson.stream.JsonWriter;
import com.innovaccer.sae.engine.model.BasicAPIRequest;
import com.innovaccer.sae.engine.model.BulkJSONRequest;
import com.innovaccer.sae.engine.model.JsonRequest;
import com.innovaccer.sae.engine.model.NERRequest;

import com.innovaccer.sae.utils.BDTask;
import com.innovaccer.sae.utils.countWordFreqTask;
//import com.sun.javafx.fxml.expression.Expression;
import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.gson.Gson;
import com.innovaccer.sae.model.ner.nerlist;
import com.innovaccer.sae.model.ner.nermodel;
import com.innovaccer.sae.model.postagger.poslist;
import com.innovaccer.sae.model.postagger.posmodel;
import com.innovaccer.sae.model.sentiment.*;
import com.innovaccer.sae.model.tree.Treelist;
import com.innovaccer.sae.model.tree.Treemodel;
import com.innovaccer.sae.utils.tree;

import static com.innovaccer.sae.utils.TreeUtils.getTree;
import static com.innovaccer.sae.utils.TreeUtils.getChilds;
import static com.innovaccer.sae.utils.TreeUtils.getAllChildAsList;

import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.graph.DirectedMultiGraph;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/analytics")
public class RestServiceController {
    Properties props = new Properties();
    AbstractSequenceClassifier classifier;
    final Logger logger = LoggerFactory.getLogger(RestServiceController.class);
    StanfordCoreNLP pipeline;
    public RestServiceController() {
        props.setProperty("annotators",
                "tokenize, ssplit, pos, parse, lemma, sentiment");
        pipeline = new StanfordCoreNLP(props);
        String serializedClassifier = "english.all.3class.distsim.crf.ser.gz";
        classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
    }

    /*
    * Takes a request in the form of JsonRequestModel (com.innovaccer.sae.model.JsonRequest
    * It has several parameters on what the user wants to do.
    * BdOPT - When set, True if dependency parse tree is needed, it will return it.
    * Sentiment Tree - When set, True Sentiment analysis is also returned.
    * @param request api parameters in Post Request
    * @return JsonString that we parsed sentences.
    */
    @RequestMapping(value = "analyse", method = RequestMethod.POST)
    public @ResponseBody String name(@RequestBody JsonRequest request) {
        logger.info(request.getSentence());
        Gson gson = new Gson();
        return gson.toJson(getbd(request.getSentence()));

    }

    @RequestMapping(value = "bulkanalyze", method=RequestMethod.POST)
    public @ResponseBody String name(@RequestBody BulkJSONRequest request){
        String[] texts= request.getTexts();
        Gson gson = new Gson();
        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
        CompletionService< ArrayList<Object> > cservice = new ExecutorCompletionService < ArrayList<Object> > (eservice);
        int NUM_OF_TASKS = 0;
        for(String text: texts){
            cservice.submit(new BDTask(text, pipeline));
            NUM_OF_TASKS++;
        }
        ArrayList<Object> resultList = new ArrayList<Object>();
        try {
            for (int i = 0; i < NUM_OF_TASKS; i++) {
                ArrayList<Object> result = cservice.take().get();
                resultList.add(result);
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        try {
            eservice.shutdown();
            if (!eservice.awaitTermination(10, TimeUnit.MICROSECONDS)) {
                logger.debug("Still waiting after 10ms: calling System.exit(0)...");
            }
        }
        catch(Exception e){e.printStackTrace();}
        return gson.toJson(resultList);
    }
    /**
     * Takes a file as input and returns the json request
     * @param file File containing sentences that need to be parsed.
     * @return String Json String that will be returned.
     */
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public
    @ResponseBody
    String handleFileUpload(@RequestParam("file") MultipartFile file) {
        long begTest = new java.util.Date().getTime();
        ArrayList<String> sentences = new ArrayList<String>();
        if (file.isEmpty()) {
            return "Empty File uploaded";
        }
        //Store it in a buffer.
        logger.info("File Upload", "File Successfully Uploaded");
        String content_type = file.getContentType();
        logger.info("Content-Type: " + content_type);
        logger.info("Size: " + Long.toString(file.getSize()));
        logger.info("File Name: " + file.getOriginalFilename());


        /* Parallel Processing configuration */
        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("No Of Processors given are: "+Integer.toString(nrOfProcessors));
        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
        CompletionService< ArrayList<Object> > cservice = new ExecutorCompletionService < ArrayList<Object> > (eservice);
        int NUM_OF_TASKS = 0;
        logger.debug(Integer.toString(sentences.size()));
        try {
            InputStream in = file.getInputStream();
            Scanner scanner =  new Scanner(in);
            scanner.useDelimiter(".\n");
            while (scanner.hasNext()) {
                String sentence = scanner.next();
                logger.info("Sentence is: "+sentence);
                if(!sentence.isEmpty()) {
                    cservice.submit(new BDTask(sentence, pipeline));
                    NUM_OF_TASKS++;
                    sentences.add(sentence);
                }
            }
        } catch (Exception e) {
            logger.debug("Error Reading File");
            e.printStackTrace();
        }
        ArrayList<Object> finalResult = new ArrayList<Object>();
        for(int index = 0; index < NUM_OF_TASKS; index++) {
            try {
                ArrayList<Object> result = cservice.take().get();
                for (Object singleObject : result) {
                    finalResult.add(singleObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if(finalResult.isEmpty()){
            logger.info("No tasks done, Strings not given");
        }
        Double secs = new Double((new java.util.Date().getTime() - begTest)*0.001);

        logger.debug("run time " + secs + " secs");
        return "Task Done Successfully";
    }

    /*
    * Gives the word count in a sentence
    * @param textCorpus  text that is given by the user.
    * @return count      No of words in the sentence
    *
    */
    @RequestMapping(value = "wordcount", method = RequestMethod.POST)
    public @ResponseBody String wordCount(@RequestBody BasicAPIRequest request){
        Annotation document = new Annotation(request.getTextCorpus());
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        int wordCount = 0;
        for(CoreMap sentence: sentences){
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            wordCount += tokens.size();
        }
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        try {
            writer.beginObject();
            writer.name("count").value(Integer.toString(wordCount));
            writer.endObject();
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return sw.toString();
    }

    /*
    * Gives the frequency of each word in the corpus
    * @param textCorpus text that is given by the user.
    * @return HashTable Returns a json object with key as word and freq as value.
    */
    @RequestMapping(value="wordfreqcount", method = RequestMethod.POST)
    public @ResponseBody String wordfreqCount(@RequestBody BasicAPIRequest request){
        long begTest = new java.util.Date().getTime();
        Gson gson = new Gson();
        Annotation document = new Annotation(request.getTextCorpus());
        pipeline.annotate(document);
        //Concurrent HashMap has not been used because thread safe conditions are hard to enforce.
        HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("No Of Processors given are: "+Integer.toString(nrOfProcessors));
        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
        CompletionService< HashMap<String, Integer> > cservice = new ExecutorCompletionService <  HashMap<String, Integer>  > (eservice);
        int NUM_OF_TASKS = 0;

        for(CoreMap sentence: sentences){
            cservice.submit(new countWordFreqTask(sentence, pipeline));
            NUM_OF_TASKS++;
        }

        for (int counter=0; counter<NUM_OF_TASKS; counter++){
            try{
                HashMap<String, Integer> wordMap = cservice.take().get();
                Iterator<Entry<String, Integer>> iterator = wordMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Entry<String, Integer> entry= iterator.next();
                    String keyEntry = entry.getKey();
                    logger.info("Word: "+keyEntry);
                    if(wordFreq.containsKey(keyEntry)){
                           Integer  mapValue = entry.getValue();
                           Integer freqValue = wordFreq.get(keyEntry);
                           Integer incrementedValue =  new Integer(freqValue.intValue() + mapValue.intValue());
                           wordFreq.put(keyEntry, incrementedValue);
                    }
                    else{
                        Integer newValue = new Integer(1);
                        wordFreq.put(keyEntry, newValue);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }

        Double secs = new Double((new java.util.Date().getTime() - begTest)*0.001);
        logger.debug("run time " + secs + " secs");
        return gson.toJson(wordFreq);
    }

    /*
    * Returns a word freq
    * @param textCorpus TextCorpus to tokenize
    * @return words     List of tokens
    */
    @RequestMapping(value="gettokens", method = RequestMethod.POST)
    public @ResponseBody String getTokens(@RequestBody BasicAPIRequest request){
        String corpus =request.getTextCorpus();
        Annotation document = new Annotation(corpus);
        ArrayList<String> words = new ArrayList<String>();
        pipeline.annotate(document);
        for(CoreMap sentence: document.get(SentencesAnnotation.class)){
            for(CoreLabel token: sentence.get(TokensAnnotation.class)){
                String token_text = token.get(TextAnnotation.class);
                words.add(token_text);
            }
        }
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        try {
            writer.beginObject();
            writer.name("words");
            writer.beginArray();
            Iterator<String> iterator = words.iterator();
            while(iterator.hasNext()){
                writer.value(iterator.next());
            }
            writer.endArray();
            writer.endObject();
            writer.close();
        }catch(Exception e){
            e.printStackTrace();

        }

        return sw.toString();
    }

    /*
    * Returns all the verbs and the type of verb
    * @param TextCorpus Corpus on which we need to classify verbs
    * @return Verbs     JsonDict of verbs
    */
    @RequestMapping(value = "classifyverbs", method = RequestMethod.POST)
    public @ResponseBody String classifyVerbs(@RequestBody BasicAPIRequest request){
        String corpus = request.getTextCorpus();
        logger.info("Request: "+corpus);
        Annotation document = new Annotation(corpus);
        Gson gson = new Gson();
        String[] possible_pos = {"VB","VBD","VBG","VBN","VBP","VBZ"};
        List<String> verb_types = Arrays.asList(possible_pos);
        HashMap<String, String> verbs = new HashMap<String, String>();
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences){
            logger.info(sentence.toString());
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            for(CoreLabel token: tokens) {
                String token_text = token.get(TextAnnotation.class);
                String token_pos = token.get(PartOfSpeechAnnotation.class);
                logger.info("POS: "+token_pos);
                if(verb_types.contains(token_pos)){
                    logger.info(token_text);
                    verbs.put(token_text, token_pos);
                }
            }
        }
        return gson.toJson(verbs);

    }

    /*
    * Generates a parse tree for a sentence
    * @param  TextCorpus    Corpus of a text
    * @return Parsetree     jsonDict of parse trees
     */
    @RequestMapping(value = "parse", method = RequestMethod.POST)
    public @ResponseBody String parseSentence(@RequestBody BasicAPIRequest request){
        String corpus = request.getTextCorpus();
        logger.info("\n\n\n"+corpus);
        Annotation document = new Annotation(corpus);
        pipeline.annotate(document);
        ArrayList<String> parsed_sentences = new ArrayList<String>();
        List<CoreMap> sentences =  document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences){
            Tree tree = sentence.get(TreeAnnotation.class);
            StringBuilder builder = new StringBuilder();
            StringBuilder sb = tree.toStringBuilder(builder);
            parsed_sentences.add(sb.toString());
            logger.info("\n\nString: " + sb.toString());
        }

        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        try{
            writer.beginObject();
            writer.name("parsed_sentences");
            writer.beginArray();
            for(String parsed_sentence: parsed_sentences) {
                writer.value(parsed_sentence);
            }
            writer.endArray();
            writer.endObject();
        }catch(Exception e){
            e.printStackTrace();
        }
        return sw.toString();
    }

    /*
    * Word Clouds : given the word frequency, It makes a world cloud.
    * Input: TextCorpus
    * Output: SVG or PDF
    *
    */
    @RequestMapping(value="/cloud", method=RequestMethod.POST)
    public @ResponseBody Object generateCloud(@RequestBody BasicAPIRequest request){
        String Corpus = request.getTextCorpus();
        String[] possibleColors = {"s","s"};

        return "";
    }
    /*
    *Return the sentence tokenizer
    * @param TextCorpus     Text Corpus sent by the user.
    * @return JsonDict      Sentences
    */
    @RequestMapping(value = "getsentences", method = RequestMethod.POST)
    public @ResponseBody String sentenceTokenize(@RequestBody BasicAPIRequest request) {
        String corpus = request.getTextCorpus();
        logger.info("\n\n\n" + corpus);
        Annotation document = new Annotation(corpus);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        try {

            writer.beginObject();
            writer.name("sentences");
            writer.beginArray();
            for (CoreMap sentence : sentences) {
                writer.value(sentence.toString());

            }
            writer.endArray();
            writer.endObject();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return sw.toString();
    }

    @RequestMapping(value= "getner", method= RequestMethod.POST)
    public @ResponseBody String nerrequest(@RequestBody BasicAPIRequest request){
        Gson gson = new Gson();
        String cleaned_sentence;
        String sentence = request.getTextCorpus();
        cleaned_sentence = preprocessSentence(sentence);
        logger.info("Cleaned Sentence: "+cleaned_sentence);
        return gson.toJson(getner(cleaned_sentence));
    }

    private String preprocessSentence(String raw_sentence){
        Annotation document = new Annotation(raw_sentence);
        pipeline.annotate(document);
        StringBuilder newCorpus = new StringBuilder();
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences){
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            ArrayList<String> capSentence = new ArrayList<String>();
            for(CoreLabel token: tokens){
                String pos = token.get(PartOfSpeechAnnotation.class);
                String text = token.get(TextAnnotation.class);
                if(pos.charAt(0) == 'N'){
                    text = text.substring(0, 1).toUpperCase() + text.substring(1);    
                }
                capSentence.add(text);
            }
            StringBuilder newSentence = new StringBuilder();
            for(String word: capSentence){
                newSentence.append(" "+word);
            }
            newCorpus.append(newSentence);
        }
        return newCorpus.toString();
    }

    private Object getpsvo(String text) {
        Object objLst = new Object();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        SemanticGraph dependencies;
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            dependencies = sentence
                    .get(CollapsedCCProcessedDependenciesAnnotation.class);
            objLst = processTD(dependencies, sentence);
        }
        return objLst;
    }

    @SuppressWarnings({"unused", "unchecked", "rawtypes"})
    Object processTD(SemanticGraph dependencies, CoreMap sentence) {
        List<Object> subj = new ArrayList<Object>();
        List<Object> verb = new ArrayList<Object>();
        List<Object> obj = new ArrayList<Object>();
        List<Object> othr = new ArrayList<Object>();
        HashMap<String, String> hs = null;
        List<String> edges = new ArrayList<String>();
        DirectedMultiGraph<IndexedWord, SemanticGraphEdge> das;
        tree tr = getTree(dependencies.toString());
        try {
            Field flds = dependencies.getClass().getDeclaredField("graph");
            flds.setAccessible(true);
            das = (DirectedMultiGraph<IndexedWord, SemanticGraphEdge>) flds
                    .get(dependencies);
            Field fld = das.getClass().getDeclaredField("outgoingEdges");
            fld.setAccessible(true);
            hs = (HashMap<String, String>) fld.get(das);
            das.getAllEdges();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Iterator it = hs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            HashMap<String, String> hash = new HashMap<String, String>();
            hash.put(pairs.getKey().toString(), pairs.getValue().toString());

            if (pairs.getKey().toString().contains("VB")
                    || pairs.getKey().toString().contains("NNS")) {
                edges.add(getSVO(pairs, tr));
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        return edges;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    String getSVO(Entry pairs, tree tr) {
        StringBuilder strs = new StringBuilder();
        HashMap hs = (HashMap) pairs.getValue();
        HashMap hs2 = new HashMap();
        HashMap hs3 = new HashMap();
        Iterator it = hs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            hs2.put(pair.getKey().toString(), pair.getValue().toString());
            hs3.put(pair.getKey(), pair.getValue());
            it.remove();
        }
        it = hs3.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String str = pair.toString();
            List<com.innovaccer.sae.utils.tree> t = getChilds(pair.getKey(), tr);
            if (t.size() > 0) {
                for (tree tre : t) {
                    if (!(tre.node.contains("conj_"))
                            || tre.node.contains("NNP")) {
                        if (tre.subTree.size() > 0) {

                            for (tree st : getAllChildAsList(tre)) {
                                strs.append(st.node.toString());
                            }
                        }
                        strs.append(tre.node.toString());
                    }
                }
            }
            strs.append(" " + str + "\n");
            it.remove();
        }

        return strs.toString();
    }

    @SuppressWarnings("unchecked")
    private Object getpas(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        SemanticGraph dependencies = null;
        HashMap<String, String> hs = null;
        List<HashMap<String, String>> edges = new ArrayList<HashMap<String, String>>();
        DirectedMultiGraph<IndexedWord, SemanticGraphEdge> das;
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            dependencies = sentence
                    .get(CollapsedCCProcessedDependenciesAnnotation.class);
            try {
                Field flds = dependencies.getClass().getDeclaredField("graph");
                flds.setAccessible(true);
                das = (DirectedMultiGraph<IndexedWord, SemanticGraphEdge>) flds
                        .get(dependencies);
                Field fld = das.getClass().getDeclaredField("outgoingEdges");
                fld.setAccessible(true);
                hs = (HashMap<String, String>) fld.get(das);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Iterator<Entry<String, String>> it = hs.entrySet().iterator();
            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry pairs = (Map.Entry) it.next();
                HashMap<String, String> hash = new HashMap<String, String>();
                if (pairs.toString().contains("subj")
                        || pairs.toString().contains("obj")) {
                    hash.put(pairs.getKey().toString(), pairs.getValue()
                            .toString());
                    edges.add(hash);
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        return edges;
    }

    /*
    Generates Basic Dependency tree
    @Param Sentence to generate text
    @Return Basic Dependency Tree Object
     */
    private Object getbd(String text) {

        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        SemanticGraph dependencies = null;
        List<Object> data = new ArrayList<Object>();
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            dependencies = sentence.get(BasicDependenciesAnnotation.class);
            data.add(getTree(dependencies.toString()));
        }
        return data;
    }

    private Treelist gettree(String text) {
        Treelist trlst = new Treelist();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(TreeAnnotation.class);
            trlst.addToList(printfy(tree));
        }
        return trlst;
    }

    private nerlist getner(String text) {
        nerlist ner = new nerlist();
        HashMap<String, String> wordSet = new HashMap<String, String>();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences =  document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences){
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            for(CoreLabel token: tokens){
                wordSet.put(token.get(TextAnnotation.class), token.get(PartOfSpeechAnnotation.class));
            }
        }
        @SuppressWarnings("unchecked")
        List<List<CoreLabel>> out = classifier.classify(text);
        for (List<CoreLabel> sentence : out) {
            for (CoreLabel word : sentence) {

                logger.info(word.word() + '/'
                        + word.get(AnswerAnnotation.class) + ' ' + wordSet.get(word.word()));
                ner.addToList(new nermodel(word.word(), word
                        .get(AnswerAnnotation.class), wordSet.get(word.word())));
            }
        }
        return ner;
    }

    private poslist getTag(String text) {
        MaxentTagger tagger = new MaxentTagger(
                "english-bidirectional-distsim.tagger");
        String tagged = tagger.tagString(text);
        poslist pos = new poslist();
        for (String str : tagged.split(" ")) {
            pos.addToList(new posmodel(str.split("_")[0], str.split("_")[1]));
        }
        System.out.println(tagged);
        return pos;
    }

    private Sentiment getSentiment(String text) {
        Sentiment snts = new Sentiment();
        Long time = Calendar.getInstance().getTimeInMillis();
        System.out.println(time);
        for (String str : text.split("\\r?\\n")) {
            Annotation annotation = new Annotation(str);
            pipeline.annotate(annotation);
            for (CoreMap sentence : annotation
                    .get(SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                snts.addToList(new senti(sentence.toString(), sentiment));
            }
        }
        System.out.println("time taken : "
                + (Calendar.getInstance().getTimeInMillis() - time));
        return snts;
    }

    private static Treemodel printfy(Tree tree) {
        Treemodel tr = new Treemodel();
        if (tree.isLeaf()) {
            if (tree.label() != null) {
                tr.setLabel(tree.label().value());
                tr.addTonodes(null);
            }
            return tr;
        } else {
            if (tree.label() != null) {
                if (tree.value() != null) {
                    tr.setLabel(tree.label().value());
                }
            }
            Tree[] kids = tree.children();
            if (kids != null) {
                for (Tree kid : kids) {
                    tr.addTonodes(printfy(kid));
                }
            }
        }
        return tr;
    }
}