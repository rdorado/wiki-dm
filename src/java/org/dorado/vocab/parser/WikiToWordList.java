package org.dorado.vocab.parser;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dorado.WordnetDataReader;
import org.dorado.vocab.core.BagOfWords;
import org.dorado.vocab.core.Orderer;
import org.dorado.vocab.core.Parser;
import org.dorado.vocab.core.Target;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.util.CoreMap;
import parser.WikiBaseParser;
import text.TextHelper;


public class WikiToWordList extends WikiBaseParser{

	public WikiToWordList(String outfilename, String[] options) {
		super(outfilename);
		
	}
	
/*
	boolean removeCDATA=false;

	boolean readText=false;
	boolean readTitle=false;

	boolean leaverel=false;
	String title="";
	String text="";
	String[] options;

	StanfordCoreNLP pipeline;



	public WikiToWordList(String outfilename, String... options) {


		super(outfilename);
		this.options=options;
		if(options!=null && options.length>0){
			for(String opt : options){
				if(opt.equals("-r")){
					leaverel=true;
				}
			}
		}

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		//props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);

	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if(qName.equals("text")){
				output.write("  <text>");
				readText=true;
			}
			else if(qName.equals("title")){
				output.write("  <title>");
				readTitle=true;
			}
			else if(qName.equals("page")){
				output.write(" <page>");
				output.newLine();
			}
			else if(qName.equals("document")){
				output.write("<document>");
				output.newLine();
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {

			if(qName.equals("text")){
				text=text.toLowerCase();

				if(removeCDATA){
					text=TextHelper.removeCDATA(text);
				}

				text=TextHelper.removeAllWikiInfo(text,"{{","}}");
				text=TextHelper.removeAllWikiInfo(text,"{|","|}");

				text=TextHelper.replaceXMLSymbols(text);

				text=TextHelper.removeAllWikiInfo(text,"<ref","</ref>");
				text=TextHelper.removeAllWikiInfo(text,"<ref","/>");
				text=TextHelper.removeAllWikiInfo(text,"<gallery","</gallery>");
				text=TextHelper.removeAllWikiInfo(text,"<div","</div>");
				text=TextHelper.removeAllWikiInfo(text,"<math","</math>");
				text=TextHelper.removeAllWikiInfo(text,"<sup","</sup>");
				text=TextHelper.removeAllWikiInfo(text,"<br",">");
				text=TextHelper.removeAllWikiInfo(text,"</br",">");

				text=text.replaceAll("</b>", "");
				text=text.replaceAll("<center>", "");
				text=text.replaceAll("</center>", "");

				text=text.replaceAll("<sub>", "_");
				text=text.replaceAll("</sub>", "");

				text=text.replaceAll("''", "");
				text=text.replaceAll("&", "&amp;");
				text=text.replaceAll("\\*", " ");
				text=text.replaceAll("<blockquote>", "");
				text=text.replaceAll("</blockquote>", "");


				System.out.println(title);
				WordnetDataReader wdr = new WordnetDataReader("data/train_wupn.dat");
				//WordnetDataReader wdr = new WordnetDataReader("data/train_lch.dat");
				System.out.println( title.toLowerCase().replaceAll("_", " ") );
				Target t = wdr.getTarget(title.toLowerCase().replaceAll("_", " "));

				ArrayList<String> keywords = new ArrayList<String>();
				text = removeAllWikiInfo(text,"[[","]]","|", keywords);

				BagOfWords bow = new BagOfWords();	
				
				bow.transform(text);

				int tp = 0;
				System.out.println("\nFreelinks vs WordNet best terms: ");
				System.out.print("   ["); 
				for(String tword : t.getRelations() ){
					
					if(keywords.contains(tword)){
						System.out.print(tword+",");
						tp++;
					}					
				}
				System.out.println("]");
				//System.out.println("WordNet Terms: "+tp);

				
				Set<String> keys = bow.getDictionary().keySet();
				Orderer<String> ord = new Orderer<String>();
				for(String tword : keys ){
					ord.add(bow.getAugmentedTF(tword), tword);
				}
				
				int N=1000;
				
				tp = 0;
				ArrayList<String> best = ord.get(N);
				System.out.println("\nAugmented TF score: ");
				System.out.print("   ["); 
				for(String tword : best ){
					
					if(t.getRelations().contains(tword)){
						System.out.print(tword+",");
						tp++;
					}
					
				}
				System.out.println("]");
				printBestN(ord,10,"   Best 10:");
				//System.out.println("Augmented TF score: "+tp);
				
				
				ord = new Orderer<String>();
				for(String tword : keys ){
					ord.add(bow.getLogTF(tword), tword);
				}
				
				
				
				
				tp = 0;
				best = ord.get(N);
				System.out.println("\nLog TF score: ");
				System.out.print("   ["); 
				for(String tword : best ){
					
					if(t.getRelations().contains(tword)){
						System.out.print(tword+",");
						tp++;
					}
					
				}
				System.out.println("]");
				printBestN(ord,10,"   Best 10:");
				//System.out.println("Log-TF score: "+tp);				
				
				
				ord = new Orderer<String>();
				for(String tword : keys ){
					ord.add(bow.getProb(tword), tword);
				}
				
				
				tp = 0;
				best = ord.get(N);
				System.out.println("\nProbabilistic approach: ");
				System.out.print("   ["); 
				for(String tword : best ){					
					if(t.getRelations().contains(tword)){
						System.out.print(tword+",");
						tp++;
					}
					
				}
				System.out.println("]");
				//System.out.println("Prob: "+tp);				
				printBestN(ord,10,"   Best 10:");				
				
				
				Parser parser = Parser.load();


				DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));


				ArrayList<String> nSubjTerms = new ArrayList<String>();
				ArrayList<String> dObjTerms = new ArrayList<String>();
				ArrayList<String> allTerms = new ArrayList<String>();
				Annotation document = new Annotation(text);
				pipeline.annotate(document);
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
				for(CoreMap sentence: sentences) {

					GrammaticalStructure gs = parser.parse(sentence.toString());					
					SemanticGraph graph = SemanticGraphFactory.generateCollapsedDependencies(gs);
										
					
					SemgrexMatcher matcher = parser.match(graph,"<nsubj");
					while (matcher.find()) {
						  IndexedWord nodeA = matcher.getNode("A");
						  String word = nodeA.get(TextAnnotation.class);
						  
						  if(!nSubjTerms.contains(word)) nSubjTerms.add( word );
						  if(!allTerms.contains(word)) allTerms.add( word );
						  //IndexedWord nodeB = matcher.getNode("B");
					      //System.out.println(nodeA + " <nsubj " + nodeB);
					      //System.out.println( nodeB.get(TextAnnotation.class)+" "+nodeB.getString(PartOfSpeechAnnotation.class) );
					}
					
					matcher = parser.match(graph,"<dobj");
					while (matcher.find()) {
						  IndexedWord nodeA = matcher.getNode("A");
						  //IndexedWord nodeB = matcher.getNode("B");
						  String word = nodeA.get(TextAnnotation.class);
						  
						  if(!dObjTerms.contains(word)) dObjTerms.add( word );
						  if(!allTerms.contains(word)) allTerms.add( word );
					      //System.out.println(nodeA + " <dobj " + nodeB);
					      //System.out.println( nodeB.get(TextAnnotation.class)+" "+nodeB.getString(PartOfSpeechAnnotation.class) );
					}
					
				}


				tp = 0;
				best = ord.get(N);
				System.out.println("\nDependency parser: ");
				System.out.print("   ["); 
				for(String tword : allTerms ){					
					if(t.getRelations().contains(tword)){
						System.out.print(tword+",");
						tp++;
					}
					
				}
				System.out.println("]");
				printBestN(ord,10,"   Best 10:");
				/*
					


					System.out.println( sentence.toString() );
				}



				/*Reader reader = new StringReader(text);
				DocumentPreprocessor dp = new DocumentPreprocessor(reader);
				List<String> sentenceList = new ArrayList<String>();

				for (List<HasWord> sentence : dp) {
					String sentenceString = Sentence.listToString(sentence);
					System.out.println(sentenceString);
					//sentenceList.add(sentenceString.toString());
				}*/


				/*
				if(!leaverel){
					text=TextHelper.removeAllWikiInfo(text,"[[","]]","|");

					text=text.replaceAll("====", "");
					text=text.replaceAll("===", "");
					text=text.replaceAll("==", "");
				}


				//	text=text.replaceAll(" http://(\\w*.)+ ", " ");

				output.write(text+"</text>");
				output.newLine();
				text="";
				readText=false;

			}
			else if(qName.equals("page")){
				output.write(" </page>");
				output.newLine();
			}
			else if(qName.equals("title")){

				if(removeCDATA){
					title=TextHelper.removeCDATA(title);
				}
				if(title.contains("&")) title=title.replaceAll("&", "&amp;");
				output.write(title+"</title>");
				output.newLine();
				readTitle=false;
			}
			else if(qName.equals("document")){
				output.write("</document>");
				output.newLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			output.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printBestN(Orderer<String> ord, int n, String message){
		ArrayList<String> best = ord.get(n);
		System.out.print(message+"["); 
		for(String tword : best ){
			System.out.print(tword+",");			
		}
		System.out.println("]");
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		try {

			if(readTitle){
				title = new String(ch, start, length);
			}
			else if(readText){
				text = text + new String(ch, start, length);
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String removeAllWikiInfo(String text, String startSymbol, String endSymbol, String separator, ArrayList<String> keywords) {
		String strtmp = text;
		while( (strtmp=removeWikiInfo(text,startSymbol,endSymbol,separator, keywords))!=null ){
			text=strtmp;
		}
		return text;
	}

	public static String removeWikiInfo(String text, String startSymbol, String endSymbol, String separator, ArrayList<String> keywords) {
		int stIndx=-1;
		int endIndx=0;
		do{
			endIndx = text.indexOf(endSymbol, endIndx+endSymbol.length());
			if(endIndx==-1) return null;

			stIndx = text.substring(0, endIndx).lastIndexOf(startSymbol);

		}while(stIndx==-1);


		String tmp = text.substring(stIndx+startSymbol.length(), endIndx);
		if(tmp.contains(separator)) tmp = tmp.substring(tmp.indexOf(separator)+separator.length());

		tmp = tmp.replace(" ", "_");
		keywords.add(tmp);

		return text.substring(0,stIndx)+tmp+text.substring(endIndx+endSymbol.length());
	}*/
}
