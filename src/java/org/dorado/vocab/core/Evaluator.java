package org.dorado.vocab.core;

import java.util.ArrayList;

public class Evaluator {

	public static PrecisionRecall calcultePrecisionRecall(ArrayList<String> gold, ArrayList<String> test) {
		int relevant = gold.size();
		int selected = test.size();
		int truePositives = 0;
		
		for(String wrd : test){
			if(gold.contains(wrd)) truePositives++;
		}
		
		return new PrecisionRecall(relevant, selected, truePositives);
	}

	public static ArrayList<String> getFound(ArrayList<String> gold, ArrayList<String> test) {
		ArrayList<String> resp = new ArrayList<String>();
		for(String wrd : test){
			if(gold.contains(wrd)) resp.add(wrd);
		}
		return resp;
	}

	
	
}
