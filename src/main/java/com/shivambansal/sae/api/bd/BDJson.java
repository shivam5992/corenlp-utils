package com.innovaccer.sae.api.bd;

import com.google.gson.Gson;

import java.util.concurrent.Callable;

/**
 * Created by sreeram on 12/6/14.
 */
public class BDJson implements Callable {
    Gson gson;
    String mInput;
    String[] mKeys;
    /*
    * Takes the keys of the json for which we need to parse and the id's to identify them.
    * Json Format:
    *
    * @param key for which json value has to be parsed.
    * @param inputJson  Json whose values have to be parsed.
    * @return outputJson with key as key given and JsonArray of parsed documents.
    */
    public BDJson(String[] key, String inputJson){
        gson = new Gson();
        mInput = inputJson;
        mKeys = key;
    }

    public Object call(){
        return null;
    }

}
