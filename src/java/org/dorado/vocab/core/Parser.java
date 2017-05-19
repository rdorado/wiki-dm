package org.dorado.vocab.core;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class Parser {

	static String modelPath = DependencyParser.DEFAULT_MODEL;
	static String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	static MaxentTagger tagger = new MaxentTagger(taggerPath);
	static DependencyParser dependencyParser = DependencyParser.loadFromModelFile(modelPath);
    
	//static TregexPattern p = TregexPattern.compile("ROOT << (NP=np $++ VP=vp) ");
	
	//static SemgrexPattern semgrex = SemgrexPattern.compile("{}=A <nsubj {}=B");	
    
    
	public GrammaticalStructure parse(String text){
		GrammaticalStructure gs = null;
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader( text ));
		for (List<HasWord> sentence : tokenizer){
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			gs = dependencyParser.predict(tagged);
		}		
		return gs;
	}

	static Parser parser;
	public static Parser load() {
		if(parser==null){
			parser = new Parser();
		}
		return parser;
	}
	
	public SemgrexMatcher match(SemanticGraph graph, String rel) {
		SemgrexPattern semgrex = SemgrexPattern.compile("{}=A "+rel+" {}=B");
		return semgrex.matcher(graph);
	}

	public void processPattern(String pattern, BagOfWords collector, SemanticGraph graph) {
		SemgrexPattern pat = SemgrexPattern.compile(pattern);
		SemgrexMatcher matcher = pat.matcher(graph);					
		
		while (matcher.find()) {
			IndexedWord nodeA = matcher.getNode("A");						
			String word = nodeA.get(TextAnnotation.class);
			collector.addWord(word);
		}
		
		
	}
	
}
