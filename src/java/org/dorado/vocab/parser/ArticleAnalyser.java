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
import org.dorado.vocab.core.Evaluator;
import org.dorado.vocab.core.Orderer;
import org.dorado.vocab.core.Parser;
import org.dorado.vocab.core.PrecisionRecall;
import org.dorado.vocab.core.Target;
import org.dorado.vocab.core.TextUtils;
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
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.util.CoreMap;
import parser.WikiBaseParser;
import text.TextHelper;


public class ArticleAnalyser extends WikiBaseParser{

	boolean removeCDATA=false;
	boolean readText=false;
	boolean readTitle=false;
	boolean leaverel=false;
	String title="";
	String text="";
	String[] options;

	StanfordCoreNLP pipeline;

	/*WordModel model = new WordModel();
	PhraseModel phraseModel = new PhraseModel();
	 */
	public ArticleAnalyser(String outfilename, String... options) {

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

		Parser.load();
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


				boolean printEverything = false;
				boolean printLatex = false;
				boolean printExampleTable = false;
				boolean printCombined = false;
				boolean printNumbers = true;
				
				double[] prec = new double[3];
				double[] reca = new double[3];
				double[] f1sc = new double[3];
				int i=0;
				int ntar=40;
				
				double alpha = 0.08;
				double beta = 0.8;
				double gamma = 0.12;
				
				
				/** 
				 *  Read the gold standard
				 */
				if(!printNumbers){
					System.out.print(title);				
				}
				//WordnetDataReader wdr = new WordnetDataReader("data/workfile.tmp");
				WordnetDataReader wdr = new WordnetDataReader("data/wikitextwn07.dat");
				//WordnetDataReader wdr = new WordnetDataReader("data/train_lch.dat");
				
				Target term = wdr.getTarget(title.toLowerCase().replaceAll(" ", "_"));	
				if(printEverything){
					System.out.println(" Number of target terms: "+term.getRelations().size());
				}

				ArrayList<String> wikilinks = new ArrayList<String>();
				text = removeAllWikiInfo(text,"[[","]]","|", wikilinks);

				ArrayList<String> objective = term.getRelations();
				objective = TextUtils.checkUnique(objective);	
				ntar = objective.size();
				
				
				if(printEverything){
					System.out.println(" Target items: "+objective);
				}
				else if(printLatex){
					System.out.print(" & "+ntar);
				}
				
				
				/***
				 * 
				 * Using free links to obtain information
				 * 
				 */							
				wikilinks = TextUtils.checkUnique(wikilinks);				
				PrecisionRecall pr = Evaluator.calcultePrecisionRecall(objective, wikilinks);
				
				prec[i] = pr.getPrecision();
				reca[i] = pr.getRecall();
				f1sc[i] = pr.getF1Score();
				i++;
				
				if(printEverything){
					System.out.println(" Freelinks vs WordNet best terms: ");
					System.out.println("   Precision: "+pr.getPrecision());
					System.out.println("   Recall: "+pr.getRecall());
					System.out.println("   Found terms: "+Evaluator.getFound(objective, wikilinks));
					System.out.println("   Extracted items: "+wikilinks);
				}
					


				/***
				 * Preprocess text
				 *
				 */

				BagOfWords bow = new BagOfWords();
				bow.removeNumbers();
				bow.transform(text);
				
				/**
				 * Augmented TF-score
				 *  
				 */
				//Filtering original list of terms augmenting with using augmented TF score
				Orderer<String> tfScore = new Orderer<String>();
				Orderer<String> scaledTfScore = new Orderer<String>();
				Orderer<String> logTfScore = new Orderer<String>();
				Set<String> keys = bow.getDictionary().keySet();
				for(String tword : keys ){
					tfScore.add(bow.getTF(tword), tword);
					scaledTfScore.add(bow.getScaledTF(tword, 0.8), tword);
					logTfScore.add(bow.getLogTF(tword), tword);
				}
				ArrayList<String> tfBest = TextUtils.checkUnique(tfScore.get((int)(ntar*1.5))); 
				
				pr = Evaluator.calcultePrecisionRecall(objective, tfBest);
				prec[i] = pr.getPrecision();
				reca[i] = pr.getRecall();
				f1sc[i] = pr.getF1Score();
				i++;
				
				if(printEverything){
					System.out.println(" Augmented TF-score: ");
					System.out.println("   Precision: "+pr.getPrecision());
					System.out.println("   Recall: "+pr.getRecall());
					System.out.println("   Found terms: "+Evaluator.getFound(objective, tfBest));
					System.out.println("   Extracted items: "+tfBest);
				}
				
				
				/**
				 * Probability
				 *  
				 *
				ord = new Orderer<String>();
				for(String tword : keys ){
					ord.add(bow.getProb(tword), tword);
				}
				keywords = TextUtils.checkUnique(ord.get(N));	 
				
				pr = Evaluator.calcultePrecisionRecall(objective, keywords);
				if(printEverything){
					System.out.println(" Probability: ");
					System.out.println("   Precision: "+pr.getPrecision());
					System.out.println("   Recall: "+pr.getRecall());
					System.out.println("   Found terms: "+Evaluator.getFound(objective, keywords));
					System.out.println("   Extracted items: "+keywords);
				}
				/*else if(printLatex){
					System.out.printf(" & %.3f & %.3f ",pr.getPrecision(),pr.getRecall());
				}*/
				
				/**
				 * 
				 * Dependency parser
				 * 
				 */

				Parser parser = Parser.load();
				
				//DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

				
				Annotation document = new Annotation(text);
				pipeline.annotate(document);
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
				BagOfWords counts = new BagOfWords();
				for(CoreMap sentence: sentences) {
					
					String strSent = sentence.toString();
					
					if(strSent.contains("==")) continue;
					if(strSent.contains("\n")) continue;
					if(strSent.contains("|")) continue;
					
					//System.out.println(" Sent: ("+sentence.size()+") '"+sentence.toString()+"'");					
					
					GrammaticalStructure gs = parser.parse(sentence.toString());		
					SemanticGraph graph = SemanticGraphFactory.generateCollapsedDependencies(gs);
					
					
					
					//String pattern = "word:"+title.toLowerCase()+" <nsubj";
					//String pattern = "{word:"+title.toLowerCase()+"}=B <nsubj {tag:/VB.*/}=A";
					
					//String pattern = "{tag:NN}=A <=rel {word:"+title.toLowerCase()+"}=B";
					parser.processPattern("{tag:NN}=A <=rel {word:"+title.toLowerCase()+"}=B", counts, graph);
					parser.processPattern("{tag:NN}=A >=rel {word:"+title.toLowerCase()+"}=B", counts, graph);
					parser.processPattern("{word:"+title.toLowerCase()+"}=B <nsubj {tag:/VB.*/}=A", counts, graph);
					
					
					/*SemgrexPattern pat = SemgrexPattern.compile(pattern);
					SemgrexMatcher matcher = pat.matcher(graph);					
					
					while (matcher.find()) {
						IndexedWord nodeA = matcher.getNode("A");						
						IndexedWord nodeB = matcher.getNode("B");
						
						String word = nodeA.get(TextAnnotation.class);
						counts.addWord(word);
						  
					}*/
					
				}
				
				/*
				 * Counting the number of times they appear on the relation and display the best k
				 */
				
				ArrayList<String> dependency = counts.getKMostFrequent(400); 
				
				pr = Evaluator.calcultePrecisionRecall(objective, dependency);
				prec[i] = pr.getPrecision();
				reca[i] = pr.getRecall();
				f1sc[i] = pr.getF1Score();
				i++;
				if(printEverything){
					System.out.println(" Parser: ");
					System.out.println("   Precision: "+pr.getPrecision());
					System.out.println("   Recall: "+pr.getRecall());
					System.out.println("   Found terms: "+Evaluator.getFound(objective, dependency));
					System.out.println("   Extracted items: "+dependency);
				}

				
				/**
				 * Combined score
				 * 
				 */
				ArrayList<String> allWords = new ArrayList<String>();
				for(String wrd : wikilinks) allWords.add(wrd);
				for(String wrd : tfBest) allWords.add(wrd);
				for(String wrd : dependency) allWords.add(wrd);
				
				double score=0, tf=0;
				Orderer<String> combinedTfScore = new Orderer<String>();
				Orderer<String> combinedScaledTfScore = new Orderer<String>();
				Orderer<String> combinedLogTfScore = new Orderer<String>();				
				
				for(String wrd : allWords){
					double wl = wikilinks.contains(wrd)?1:0;
					double dp = dependency.contains(wrd)?1:0;
										
					tf = tfScore.getScore(wrd).doubleValue();										
					score = alpha*wl + beta*tf + gamma*dp;
					combinedTfScore.add(score, wrd);
					
					tf = scaledTfScore.getScore(wrd).doubleValue();										
					score = alpha*wl + beta*tf + gamma*dp;
					combinedScaledTfScore.add(score, wrd);
					
					tf = logTfScore.getScore(wrd).doubleValue();										
					score = alpha*wl + beta*tf + gamma*dp;
					combinedLogTfScore.add(score, wrd);
					
				}
				
				
				double[] prec2 = new double[3];
				double[] reca2 = new double[3];
				double[] f1sc2 = new double[3];
				i=0;
				ArrayList<String> best = null;
				best = TextUtils.checkUnique(combinedTfScore.get(ntar*2));	 				
				pr = Evaluator.calcultePrecisionRecall(objective, best);								
				prec2[i] = pr.getPrecision();
				reca2[i] = pr.getRecall();
				f1sc2[i] = pr.getF1Score();
				i++;
				
				best = TextUtils.checkUnique(combinedScaledTfScore.get(ntar*2));	 				
				pr = Evaluator.calcultePrecisionRecall(objective, best);
				prec2[i] = pr.getPrecision();
				reca2[i] = pr.getRecall();
				f1sc2[i] = pr.getF1Score();
				i++;				
				
				best = TextUtils.checkUnique(combinedLogTfScore.get(ntar*2));	 				
				pr = Evaluator.calcultePrecisionRecall(objective, best);
				prec2[i] = pr.getPrecision();
				reca2[i] = pr.getRecall();
				f1sc2[i] = pr.getF1Score();
				i++;	
				
				
				
				if(printCombined){
			
					
					double maxreca = max(reca2);
					double maxprec = max(prec2);
					double maxf1sc = max(f1sc2);
					
					for(int j=0;j<3;j++){
						if(prec2[j]==maxprec)
							System.out.printf(" & \\textbf{%.3f}",prec2[j]);
						else
							System.out.printf(" & %.3f",prec2[j]);
						
						if(reca2[j]==maxreca)
							System.out.printf(" & \\textbf{%.3f}",reca2[j]);
						else
							System.out.printf(" & %.3f",reca2[j]);
						
						
						if(f1sc2[j]==maxf1sc)
							System.out.printf(" & \\textbf{%.3f}",f1sc2[j]);
						else
							System.out.printf(" & %.3f",f1sc2[j]);
						
					}
					
					System.out.println(" \\\\");
					
				}
				
				
				/**
				 * Print recall precision table.
				 * 
				 * */
				if(printLatex){
					
					double maxreca = max(reca);
					double maxprec = max(prec);
					double maxf1sc = max(f1sc);
					
					for(int j=0;j<3;j++){
						if(prec[j]==maxprec)
							System.out.printf(" & \\textbf{%.3f}",prec[j]);
						else
							System.out.printf(" & %.3f",prec[j]);
						
						if(reca[j]==maxreca)
							System.out.printf(" & \\textbf{%.3f}",reca[j]);
						else
							System.out.printf(" & %.3f",reca[j]);
						
						
						if(f1sc[j]==maxf1sc)
							System.out.printf(" & \\textbf{%.3f}",f1sc[j]);
						else
							System.out.printf(" & %.3f",f1sc[j]);
						
					}
				}
				
				if(printLatex){
					System.out.println(" \\\\");
					
				}
				
				
				/**
				 * Print examples table.
				 * 
				 */
				if(printExampleTable){
					System.out.print(" & "+getFirstKAsString(wikilinks,25));
					System.out.print(" & "+getFirstKAsString(tfBest,25));
					System.out.print(" & "+getFirstKAsString(dependency,25));					
					System.out.println("\\\\");
				}	
				

				
				if(printNumbers){
					//System.out.print(ntar+" + ");
					//System.out.printf("%.3f + ",prec2[2]);
					System.out.printf("%.3f + ",reca2[2]);
					//System.out.printf("%.3f + ",f1sc2[1]);
				}
				
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

	private String getFirstKAsString(ArrayList<String> array, int k) {
		String resp = "";
		for(int i=0;i<Math.min(k, array.size());i++) resp+=array.get(i).replaceAll("_", " ")+", ";
		if(resp.length()==0) return resp;
		return resp.substring(0,resp.length()-2);
	}

	private double max(double[] arr) {
		double resp=0;
		for(int i=0;i<arr.length;i++)
			if(resp < arr[i]) resp = arr[i];
		return resp;
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
		/*if(readText){
			String text = new String(ch, start, length);
			//model.addwordsAsText(text, phraseModel);
		}*/


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
	}
}
