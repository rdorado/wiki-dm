package org.dorado.vocab.core;

import java.util.ArrayList;
import java.util.Collections;

import sun.applet.Main;

public class Orderer<K> {

	ArrayList<Element> elements = new ArrayList<Element>();
	boolean ordered=false;
	
	public void add(Number n, K item){
		elements.add(new Element(n, item));
		ordered=false;
	}
	
	public Number getScore(K item){
		for(Element e : elements) if(e.data.equals(item)) return e.count;
		return 0;
	}
	
	public void order(){
		Collections.sort(elements);
		ordered=true;
	}
	
	public ArrayList<K> get(int n){
		if(!ordered) order();
		ArrayList<K> resp = new ArrayList<K>();
		int m = Math.min(n, elements.size());
		for(int i=0;i<m;i++){
			//System.out.println("-->() "+elements.get(i).data+" "+elements.get(i).count);
			resp.add( elements.get(i).data );
		}
		return resp;
	}
	
	class Element implements Comparable<Element>{
		K data;
		Number count;
		Element(Number count, K data){ this.data=data; this.count=count; }
		@Override
		public int compareTo(Element e) {
			return (int)(e.count.doubleValue()*100000 - count.doubleValue()*100000);
		}

	}
	
	public static void main(String[] args) {
		Orderer<String> tmp = new Orderer<String>();
		tmp.add(1, "a");
		tmp.add(3, "c");
		tmp.add(2, "b");
		
		System.out.println(tmp.get(2));
	}
	
}
