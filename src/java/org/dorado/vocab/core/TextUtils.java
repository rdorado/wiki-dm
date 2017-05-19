package org.dorado.vocab.core;

import java.util.ArrayList;

public class TextUtils {

	public static ArrayList<String> checkUnique(ArrayList<String> array) {
		ArrayList<String> resp = new ArrayList<String>();
		for(String wrd : array){
			if(wrd.trim().equals("")) continue;
			if(!resp.contains(wrd)) resp.add(wrd);
		}
		return resp;
	}

}
