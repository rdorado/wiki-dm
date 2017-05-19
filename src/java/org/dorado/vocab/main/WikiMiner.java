package org.dorado.vocab.main;

import org.dorado.vocab.parser.ArticleAnalyser;

import filesystem.ParserRunner;
import filesystem.WikiFileSplitter;

public class WikiMiner {

	public static void main(String[] args) {
			
		//WikiFileSplitter.execute("/home/rdorado/Downloads/miniwiki/articles.xml", "/home/rdorado/Downloads/miniwiki/training/");
		
		ParserRunner.execute("/home/rdorado/project/wiki-dm-project/data/wikipedia/articles", "/home/rdorado/project/wiki-dm-project/data/wikipedia/output", ArticleAnalyser.class, true, "");
		
		//ParserRunner.execute("/home/rdorado/Downloads/miniwiki/output/", "/home/rdorado/Downloads/miniwiki/output", ArticleAnalyser.class, true, "");
		
	}
	
	
	
}
