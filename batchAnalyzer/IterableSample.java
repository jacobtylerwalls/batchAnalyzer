package batchAnalyzer;

import java.io.File;
import java.math.*;
import java.util.HashMap;
import java.util.Scanner;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.FixedRateMonoReader;
import com.jsyn.unitgen.LineOut;
import com.jsyn.util.WaveFileWriter;

public class IterableSample {

	private String prefix;
	private int length;
	private float[] audioData;
	private int frequency;
	private int peaks;
	private double peakAnalysisHalfLife;
	private int peakAnalysisWindow;

	/*
	 * Constructor
	 */
	public IterableSample(String filePrefix, int numFrames, float[] audioIn) {
		prefix = filePrefix;
		length = numFrames;
		audioData = audioIn;
		peaks = 0;
		peakAnalysisHalfLife = 0.0;
		peakAnalysisWindow = 0;
		frequency = this.analyzeFrequency();
	}


	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void setPeaks(int peaks) {
		this.peaks = peaks;
	}

	public int getPeaks() {
		return peaks;
	}

	public double getPeakAnalysisHalfLife() {
		return peakAnalysisHalfLife;
	}

	public int getPeakAnalysisWindow() {
		return peakAnalysisWindow;
	}

	public void setPeakAnalysisParams(double halfLife, int window) {
		this.peakAnalysisHalfLife = halfLife;
		this.peakAnalysisWindow = window;
	}

	public String getPrefix() {
		return prefix;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public int getLength() {
		return length;
	}


	public float[] getAudioData() {
		return audioData;
	}

	/**
	 * Subtract another sample's values from this sample and return the mix
	 * as a new IterableSample.
	 * @param otherSample
	 * @return new IterableSample
	 */
	public IterableSample diff(IterableSample otherSample) {
		String resultPrefix = this.prefix + "_diff_" + otherSample.prefix;
		IterableSample longerSample = otherSample;
		IterableSample shorterSample = this;

		//is the current sample longer? if so, reverse
		if (this.length > otherSample.length) {
			longerSample = this;
			shorterSample = otherSample;
		}

		int resultLength = longerSample.getLength();
		float[] diffedAudio = new float[resultLength];
		for (int i = 0; i < shorterSample.getLength(); i++) {
			diffedAudio[i] = this.audioData[i] - otherSample.audioData[i];
		}
		for (int i = shorterSample.getLength(); i < longerSample.getLength(); i++) {
			diffedAudio[i] = longerSample.audioData[i];
		}
		return new IterableSample(resultPrefix, resultLength, diffedAudio);
	}

	/***
	 * Sum this sample with another sample and return the mix.
	 * @param otherSample
	 * @return new IterableSample
	 */
	public IterableSample sum(IterableSample otherSample) {
		String resultPrefix = this.prefix + "_sum_" + otherSample.prefix;
		IterableSample longerSample = otherSample;
		IterableSample shorterSample = this;

		//is the current sample longer? if so, reverse
		if (this.length > otherSample.length) {
			longerSample = this;
			shorterSample = otherSample;
		}

		int resultLength = longerSample.getLength();
		float[] summedAudio = new float[resultLength];
		for (int i = 0; i < shorterSample.getLength(); i++) {
			summedAudio[i] = shorterSample.audioData[i] + longerSample.audioData[i];
		}
		for (int i = shorterSample.getLength(); i < longerSample.getLength(); i++) {
			summedAudio[i] = longerSample.audioData[i];
		}
		return new IterableSample(resultPrefix, resultLength, summedAudio);
	}


	/***
	 * Provide crude estimate of the frequency of this sample using a 
	 * zero-crossing counter	method written by Greg Milette. (JSyn's 
	 * pitch tracking and zero-crossing tools are designed for real-time tracking, 
	 * not sample analysis, so I had to look elsewhere.)
	 * @return frequency
	 */
	public int analyzeFrequency() {
		setFrequency(ZeroCrossing.calculate(44100, this.audioData));
		return frequency;
	}


	/***
	 * My own method to analyze peaks (onsets of rhythmic events) of a sample.
	 * @param firstAttack (0.0 to 1.0)
	 * @param halfLife in seconds
	 * @param windowSize in frames
	 */
	public int analyzePeaks(double halfLife, int windowSize) {
		if (windowSize < 1 || windowSize > (this.length)/2) {
			throw new IllegalArgumentException(
					"windowSize must be a positive integer less than 1/2 the # of frames");
		}
		//initialize maxima and counter
		double lastPeak = 0;
		int lastPeakIndex = 0;
		int peakCounter = 0;
		double lastPeakDecay = 0.0;

		//loop through audio frames by interval of 1 window
		for (int i=1; i < (length/windowSize); i++) {

			//get the audio data and the decay of last peak
			double frame = audioData[i*windowSize];

			//halflife formula for decay
			lastPeakDecay = lastPeak * Math.pow(
					0.5, ((i*windowSize)-lastPeakIndex)/(441000*halfLife));

			//compare this frame to decay of last peak
			if (frame > lastPeakDecay) {
				//set new peak, increment counter
				lastPeak = frame;
				lastPeakIndex = i*windowSize;
				peakCounter++;
			}
		}
		this.peaks = peakCounter;
		peakAnalysisHalfLife = halfLife;
		peakAnalysisWindow = windowSize;
		return peakCounter;
	}


	/**
	 * Overloaded method to use current values or set a default.
	 */
	public int analyzePeaks() {

		if (peakAnalysisHalfLife == 0.0 || peakAnalysisWindow < 0) {
			//supply default values
			setPeakAnalysisParams(1.4, 10);
		}
		return analyzePeaks(peakAnalysisHalfLife, peakAnalysisWindow);
	}


	/**
	 * Allow the user to try different parameters for peak analysis.
	 */
	public void auditionPeaks() {
		//audition peaks
		boolean auditionAgain = true;
		while (auditionAgain == true) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Supply a new halflife in seconds: ");
			int halfLife = scanner.nextInt();
			System.out.println("Supply a new sampling window in frames: ");
			int window = scanner.nextInt();
			analyzePeaks(halfLife, window);
			play();
			printInfo();
			System.out.println("Is this peak analysis satisfactory? (y/n)");
			String response = scanner.next();
			if (response.equals("y")){
				auditionAgain = false;
			}
			scanner.close();
		}
	}

	/**
	 * Iterable (Micro)phaser: performs multiple phasing operations on the current audio sample.
	 * User can specify where in the sample to begin the phasing operation, and at what
	 * interval the process should be repeated. User can also set a maximum number of 
	 * iterations to calculate.
	 * @param delayStart
	 * @param delayInterval
	 * @param maxIter
	 * @return SampleBatch of parent sample with hashmap of child samples keyed on
	 * phasing interval
	 */
	public SampleBatch phase(int delayStart, int delayInterval, int maxIter) {
		int iterationCounter = 0;
		int delayFrames = delayStart;
		//initialize the HashMap of results
		HashMap<Integer, IterableSample> phaseResults = new HashMap<Integer, IterableSample>();
		while (delayFrames < length && iterationCounter < maxIter) {
			// Create a float array to contain audio data.
			float[] phasedData = new float[length + delayFrames];
			// fill the array
			for(int i=0; i<length-1; i++) {
				//before the phasing
				if (i < delayFrames) {
					phasedData[i] = audioData[i];
				}
				//during the phasing
				else {
					phasedData[i] = audioData[i] + audioData[i-delayFrames];
				}
			}
			//after the phasing
			for (int j=0; j < delayFrames; j++) {
				phasedData[length + j] = audioData[length - delayFrames + j];
			}
			//create the IterableSample and put in the results
			IterableSample resultSample = new IterableSample(prefix + "_phased" + 
					delayFrames + "frames", length + delayFrames, phasedData);
			phaseResults.put(delayFrames, resultSample);
			//increment the delayFrames
			delayFrames += delayInterval;
			//count how many files have been written
			iterationCounter++;
		}
		return new SampleBatch(this, phaseResults);
	}



	/**
	 * Plays the current sample to the native audio output.
	 */
	public void play() {
		//read a FloatSample from the audioData
		FloatSample fs = new FloatSample(audioData);
		//create synth and unit generators
		Synthesizer synth = JSyn.createSynthesizer();
		synth.start();
		synth.setRealTime(false);
		FixedRateMonoReader myPlayer = new FixedRateMonoReader();
		LineOut myOut = new LineOut();
		//connect everything
		synth.add(myPlayer);
		synth.add(myOut);
		myPlayer.output.connect(myOut.input);
		//load this sample and hit play
		myPlayer.dataQueue.queue(fs);
		myOut.start();
		System.out.println("playing sample...");
		//close everything
		synth.stop();
	}


	/**
	 * Write the specified IterableSample to a file.
	 */
	public static void writeFile(IterableSample input) {
		File resultFile = new File (input.prefix + ".wav");
		try {
			WaveFileWriter writer = new WaveFileWriter(resultFile);
			writer.write(input.audioData);
			System.out.println(input.prefix + "frequency calculated as: " +
					ZeroCrossing.calculate(44100, input.audioData));
			writer.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Write all results from the batch to separate files.
	 */
	public static void writeAllFiles(SampleBatch batch) {
		for (IterableSample sample: batch.getChildSamples().values()) {
			IterableSample.writeFile(sample);
		}
	}

	/**
	 * Scale the current sample's amplitude by a decimal factor and make
	 * it the new current sample.
	 */
	public IterableSample scale(float scalingFactor) {
		float[] scaledAudio = new float[length];
		for (int i = 0; i < length; i++) {
			scaledAudio[i] = this.audioData[i] * scalingFactor;
		}
		String newPrefix = prefix + "_scaled" + scalingFactor;
		return new IterableSample(newPrefix, length, scaledAudio);
	}
	
	@Override
	public String toString() {
		return "Sample: " + this.getPrefix();
	}

	/**
	 * Print the parameters of the current sample being manipulated.
	 */
	public void printInfo() {
		System.out.println(toString());
		System.out.println("Length: " + getLength() + " frames");
		System.out.println("Frequency: " + getFrequency() + " hz");
		if (peaks != 0) {
			System.out.println("Peaks: " + getPeaks() + "***analyzed with a halflife of "
					+ getPeakAnalysisHalfLife() + " seconds and " + getPeakAnalysisWindow()
					+ " frames.");
		}
		else {
			System.out.println("Peaks not yet analyzed.");
		}
	}
}
