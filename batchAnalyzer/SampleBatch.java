package batchAnalyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

public class SampleBatch {
	
	private IterableSample parentSample;
	private HashMap<Integer, IterableSample> childSamples;
	private String batchname;
	private TreeMap<Integer, Integer> diffPeaks;
	private TreeMap<Double, Integer> diffFreqs;


	/*
	 * Constructor: childSamples are stored in a HashMap
	 * keyed on the phasing interval that created them in
	 * parentSample's .phase()
	 */
	public SampleBatch(IterableSample parentSample, 
			HashMap<Integer, IterableSample> childSamples) {
		this.parentSample = parentSample;
		this.childSamples = childSamples;
		diffPeaks = new TreeMap<Integer, Integer>();
		diffFreqs = new TreeMap<Double, Integer>();
	}


	public IterableSample getParentSample() {
		return parentSample;
	}

	public void setParentSample(IterableSample parentSample) {
		this.parentSample = parentSample;
	}

	public HashMap<Integer, IterableSample> getChildSamples() {
		return childSamples;
	}

	public void setChildSamples(HashMap<Integer, IterableSample> childSamples) {
		this.childSamples = childSamples;
	}

	public String getBatchname() {
		return batchname;
	}

	public void setBatchname(String batchname) {
		this.batchname = batchname;
	}


	public TreeMap<Integer, Integer> getDiffPeaks() {
		return diffPeaks;
	}


	public TreeMap<Double, Integer> getDiffFreqs() {
		return diffFreqs;
	}



	/**
	 * For each sample in the batch, measure the delta in frequency
	 * with the parent sample. Set the TreeMap keyed on frequency and
	 * with the value that will be the key to find the childSamples.
	 */
	public void diffFreqs() {
		for (int k: childSamples.keySet()) {
			double deltaFreq = Math.abs(parentSample.getFrequency() 
					- childSamples.get(k).getFrequency());
			diffFreqs.put(deltaFreq, k);
		}
	}


	/**
	 * For each sample in the batch, measure the delta in peaks
	 * with the parent sample. Use the same analysis parameters
	 * as the source. Set the TreeMap keyed on frequency with
	 * the value that will be the key to find the childSamples.
	 */
	public void diffPeaks() {

		//analyze the peaks first, use parent's analysis parameters
		for (IterableSample s: childSamples.values()) {
			s.setPeakAnalysisParams(parentSample.getPeakAnalysisHalfLife(),
					parentSample.getPeakAnalysisWindow());
			s.analyzePeaks();
		}

		//get the deltas
		for (int k: childSamples.keySet()) {
			int deltaPeaks = Math.abs(parentSample.getPeaks() - childSamples.get(k).getPeaks());
			diffPeaks.put(deltaPeaks, k);
		}
	}


	/**
	 * Select from the current batch for greater changes in frequency with
	 * respect to the parentSample.
	 */
	public SampleBatch selectOnFreqs(int numSelected) {
		//declare map for results
		HashMap<Integer, IterableSample> smallBatch = new HashMap<Integer, IterableSample>();

		//iterate through keys (deltas of freqs) in descending order
		NavigableSet<Double> reversed = diffFreqs.descendingKeySet();
		Iterator<Double> it = reversed.iterator();
		int i = 0;
			while (it.hasNext() && i<numSelected) {
				//get the key
				double k = (double) it.next();
				//get the key for childSamples
				int keyForChild = diffFreqs.get(k); //this was a phasing interval
				//put in results
				smallBatch.put(keyForChild, childSamples.get(keyForChild));
				i++;
			}

		return new SampleBatch(parentSample, smallBatch);
	}

	/**
	 * Select from the current batch for greater changes in peaks with
	 * respect to the parentSample.
	 */
	public SampleBatch selectOnPeaks(int numSelected) {
		//declare map for results
		HashMap<Integer, IterableSample> smallBatch = new HashMap<Integer, IterableSample>();

		//iterate through keys (deltas of peaks) in descending order
		NavigableSet<Integer> reversed = diffPeaks.descendingKeySet();
		Iterator<Integer> it = reversed.iterator();
		int i = 0;
			while (it.hasNext() && i<numSelected) {
				//get the key
				int k = (int) it.next();
				//get the key for childSamples
				int keyForChild = diffPeaks.get(k); //this was a phasing interval
				//put in results
				smallBatch.put(keyForChild, childSamples.get(keyForChild));
				i++;
			}

		return new SampleBatch(parentSample, smallBatch);
	}

	public void audition() {
		System.out.println("Playing samples.");
		for (IterableSample s: childSamples.values()) {
			s.printInfo();
			s.play();
		}
	}

	public void printChildren() {
		for (int k: childSamples.keySet()) {
			System.out.println("key: " + k + "  " + childSamples.get(k).toString());
		}
	}
}
