package org.dorado.vocab.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;


public class BagOfWords {

	
	int VECTOR_SIZE=10000;
	private static final Pattern UNDESIRABLES = Pattern.compile("[1234567890\"\\[\\]=(){},.:;!?<>%/–-]");
	HashMap<String, Integer> dict = new HashMap<String, Integer>();
	int counts[] = new int[VECTOR_SIZE]; 
	int n=0;
	int v=0;
	double maxf=0;
	
	public HashMap<String, Integer> getDictionary(){
		return dict;
	}
	
	public void transform(String text, int minlen){
		
		text = removePunct(text);
		text.replaceAll("'", " ");
		String[] words = text.split(" ");
		for(String wrd:words){
			wrd=wrd.trim();
			if(wrd.equals("")) continue;
			if(wrd.length()<minlen) continue;
			
			
			if(TextResources.stopwords.contains(wrd)) continue;
			
			if(!dict.containsKey(wrd)) dict.put(wrd, v++);
			counts[dict.get(wrd)]++;
			n++;
		}
		
		maxf = 0;
		for(int i=0;i<v;i++){
			if(maxf < counts[i]) maxf = counts[i];
		}
	}
	
	public void addWord(String word){
		if(!dict.containsKey(word)) dict.put(word, v++);
		counts[dict.get(word)]++;
		if(maxf < counts[dict.get(word)]) maxf = counts[dict.get(word)];
	}
	
	public void transform(String text){
		transform(text, 3);
	}
	
	private static String removePunct(String x) {
	    return UNDESIRABLES.matcher(x).replaceAll(" ");
	}
	
	public double getTF(String word) {
		if(!dict.containsKey(word)) return 0;
		return (double)counts[dict.get(word)]/n;
	}

	public double getScaledTF(String word, double k) {
		if(!dict.containsKey(word)) return 0;
		return (1-k) + k*( counts[dict.get(word)]/((double)maxf) );
	}
	
	public double getLogTF(String word) {
		if(!dict.containsKey(word)) return 0;
		return Math.log( counts[dict.get(word)])/Math.log((double)maxf );
	}
	
	public double getProb(String word) {
		if(!dict.containsKey(word)) return 0;
		return (double)(counts[dict.get(word)])/n;
	}
	 /* Map word to position
			for (String[] doc: docs) {
			     new ....
			    for (String word: doc) {
			         vector[dict.get(word)]++;
			    }
			    // Print vector
			}
	*/
	public ArrayList<String> getKMostFrequent(int k){
		Orderer<String> ord = new Orderer<String>();
		Set<String> keys = dict.keySet();
        for(String key: keys){
            ord.add(dict.get(key), key);
        }
		return ord.get(k);
	}

	public void removeNumbers() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
