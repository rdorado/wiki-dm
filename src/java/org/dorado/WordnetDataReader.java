package org.dorado;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.dorado.vocab.core.Target;

public class WordnetDataReader {

	static ArrayList<Target> targets = new ArrayList<Target>();
	static boolean loaded = false;
	static boolean toLower = true;

	private static WordnetDataReader instance=null;
		
	public WordnetDataReader(String filename){
		if(!loaded){
			try {
				BufferedReader input= new BufferedReader(new FileReader(filename) );
				String line = "";			
				while((line=input.readLine())!=null){
					String[] fSplit = line.split(":");
					Target t = new Target();
					//System.out.println( fSplit[0] );
					t.setWord(fSplit[0]);
					
					if(fSplit.length > 1){
						String[] rels = fSplit[1].split(" ");
						for(String rel : rels){
							
							
							if(rel.contains(",")){
								String[] slps = rel.split(",");
								if(toLower) t.add(slps[0].toLowerCase(),slps[1]);
								else t.add(slps[0],slps[1]);
							}
							else{
								
								if(toLower) t.add(rel.toLowerCase(),"1.0");
								else t.add(rel,"1.0");
							}
								
						}
					}	
					targets.add(t);
					loaded=true;
				}
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		//WordnetDataReader wdr = new WordnetDataReader("data/train.dat");
		//WordnetDataReader wdr = new WordnetDataReader("data/workfile.tmp");
		WordnetDataReader wdr = new WordnetDataReader("data/wikitextwn.dat");
		
		Target t = wdr.getTarget("dog");
		t.print();
	}

	public Target getTarget(String string) {
		for(Target t : targets){
			if(t.getWord().equals(string)) return t;
		}
		return null;
	}

}
