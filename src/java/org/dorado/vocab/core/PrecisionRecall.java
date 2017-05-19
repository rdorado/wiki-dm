package org.dorado.vocab.core;

public class PrecisionRecall {

	private double precision;
	private double recall;
	private double f1;
	private int relevant;
	private int selected;
	private int truePositives;
	
	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public int getRelevant() {
		return relevant;
	}

	public void setRelevant(int relevant) {
		this.relevant = relevant;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}

	public PrecisionRecall(int relevant, int selected, int truePositives){		
		this.relevant=relevant;
		this.selected=selected;
		this.truePositives=truePositives;
		
		precision = (double)truePositives/selected;
		recall = (double)truePositives/relevant;		
	}

	public double getFBetaScore(double beta) {		
		return (1+beta*beta)*(beta*beta*precision*recall)/(precision + recall);
	}
	
	public double getF1Score() {
		return 2*(precision*recall)/(precision + recall);
	}
	
}
