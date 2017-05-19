package org.dorado.vocab.core;

import java.util.ArrayList;

public class Target{
	String word;
	ArrayList<Float> weights = new ArrayList<Float>();
	ArrayList<String> rels = new ArrayList<String>();
	
	public ArrayList<String> getRelations(){
		return rels;
	}
	
	public String getWord(){
		return word;
	}
	
	public void setWord(String w) {
		word=w;		
	}
	
	public void add(String rel, String weight){
		rels.add(rel);
		weights.add( Float.parseFloat(weight) );
	}
	
	public String toString(){
		String resp=word+":";
		for (int i = 0; i < rels.size(); i++) {
			resp+=" "+rels.get(i);
		}
		return resp;
	}

	public void print() {
		System.out.println(this);		
	}
}