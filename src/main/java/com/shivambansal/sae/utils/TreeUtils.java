package com.innovaccer.sae.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class TreeUtils {
	static Queue<String> strList = new LinkedList<String>();
	static tree tr;
    static final Logger logger = LoggerFactory.getLogger(TreeUtils.class);

	public static tree getTree(String treeString) {
		for (String string : treeString.split("\n")) {
			strList.add(string);
		}
        int status = 0;
        try {
            status = getTree(strList);
        }
        catch(Exception e){
            System.err.println("Invalid Tree returned");
            tr = new tree("Error - Invalid Sentence");
            return tr;
        }
        if(status == 1002){

            System.err.println("Invalid Tree returned");
            tr = new tree("Error - Invalid Sentence");
        }
		return tr;
	}

	private static int getTree(Queue<String> strList) throws Exception {
		int lastlevel = 0;
		tree lasTree = null;
		tree parent = null;
		while (!strList.isEmpty()) {
			String hs = strList.poll();
			if (hs.toLowerCase().contains("root")) {
				tr = new tree(hs);
				lasTree = tr;
			} else {
				int level = hs.indexOf("->") / 2;
				if(level >lastlevel) {
                    if(tr == null){
                        logger.error("Tree Generation", "Invalid Tree Generated");
                        System.err.println("No head to the tree so It is an invalid sentence");
                        return 1002;
                    }
					lasTree.subTree.add(new tree(hs));
					parent = lasTree;
					lasTree = lasTree.subTree.get(lasTree.subTree.size()-1);
				}
				else if(level<lastlevel){
                    if(tr == null){
                        logger.error("Tree Generation", "Invalid Tree Generated");
                        System.err.println("No head to the tree so It is an invalid sentence");
                        return 1002;
                    }
					tree temp = getNthChild(tr,level-1);					
					temp.subTree.add(new tree(hs));
					parent = temp;
					lasTree = temp.subTree.get(temp.subTree.size()-1);
				}else{
                    if(parent == null){
                        logger.debug("Tree Generation", "Invalid Tree Generated");
                        System.err.println("No head to the tree so It is an invalid sentence");
                        return 1002;
                    }
					parent.subTree.add(new tree(hs));
					lasTree  = parent.subTree.get(parent.subTree.size()-1);
				}
				lastlevel = level;
				
			}
		}
        return 1001;
	}

	private static tree getNthChild(tree trd, int loc) {
		tree temp = trd;
		if(loc == 0)
			return trd;
		for(int i=0; i<loc;i++)
			temp = temp.subTree.get(temp.subTree.size()-1);
		return temp;
	}
	

	public static List<tree> getChilds(Object key, tree t) {
		List<tree> list = new ArrayList<tree>();
		try{
			list = getSubTreeForNodes(key, t);
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<tree> getSubTreeForNodes(Object key, tree t){
		List<tree> lst = new ArrayList<tree>();
		if(t.node.contains(key.toString())){
			if(t.subTree.size()>0){
				for(tree tr: t.subTree){
					lst.add(tr);
				}
			}
				
		}else{
			for(tree tr : t.subTree){
				for(tree trre: getSubTreeForNodes(key, tr))
					lst.add(trre);
			}
		}
		return lst;
		
	}
	
	public static List<tree> getAllChildAsList(tree t){
		List<tree> lst = new ArrayList<tree>();
		for(tree tr:t.subTree){
			lst.add(tr);
			if(tr.subTree.size()>0){
				for(tree tt: getAllChildAsList(tr)){
					lst.add(tt);
				}
			}
		}
		return lst;
	}
}
