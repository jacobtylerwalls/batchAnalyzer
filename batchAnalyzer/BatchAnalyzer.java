package batchAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.SortingFocusTraversalPolicy;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;

public class BatchAnalyzer {

	static IterableSample currentSample;
	static HashMap<String, IterableSample> savedSamples = 
			new HashMap<String, IterableSample>();
	static boolean sampleMissing = true;

	/**
	 * @throws IOException *
	 * 
	 */
	public static void loadFile(String filePath) throws IOException {

		File fileToRead = new File(filePath);

		//get the prefix from the file's name
		String[] tokens = filePath.split("\\.");
		String filePrefix = tokens[0];

		//read the file into a JSyn FloatSample
		FloatSample inputSample = SampleLoader.loadFloatSample(fileToRead);

		//get the number of frames and the frames themselves
		int numFrames = inputSample.getNumFrames();
		float[] audioData = new float[numFrames];
		inputSample.read(audioData);

		//instantiate my class
		currentSample = new IterableSample (filePrefix, numFrames, audioData);

		//report success
		sampleMissing = false;

	}


	public static void main(String[] args) {

		System.out.println("Audio Sample Microphasing Analyzer");
		Scanner scanner = new Scanner(System.in);

		//main menu
		while (true) {

			//first, we must have the sample from the user
			while (sampleMissing) {
				System.out.println("Specify the filepath for the audio sample to manipulate, or enter 0 to quit:");

				//get file path
				String filePath = scanner.nextLine();

				//allow user to quit
				if (filePath.equals("0")) {
					System.exit(0);
				}
				try {
					//make this file the current sample
					loadFile(filePath);
				}

				catch (Exception IOException) {
					System.out.println("File not found.");
				}
			}

			System.out.println(currentSample.toString());
			System.out.println("1. Play this sample and print info. \n"
					+ "2. Perform a peak analysis. \n"
					+ "3. (Micro)phase this sample and generate a batch of"
					+ " results.\n4. Write this sample to a file. \n"
					+ "5. Save this sample for later recall. \n6. Recall a sample "
					+ "for manipulation. \n7. Mix this sample with a recalled sample "
					+ "(sum or difference). \n8. Scale this sample's amplitude."
					+ "\n9. Load another sample. \n10. Quit");

			int programChoice = scanner.nextInt();

			//play and print info
			if (programChoice == 1) {
				currentSample.printInfo();
				currentSample.play();
			}

			//analyze peaks
			if (programChoice == 2) {

				boolean auditionAgain = true;

				while (auditionAgain) {

					//get analysis parameters from user
					System.out.println("Enter the halflife to use "
							+ "(in seconds--decimals OK): ");
					double halfLife = scanner.nextDouble();

					System.out.println("Enter the sampling window to use "
							+ "(in frames--integers only): ");
					int window = scanner.nextInt();

					//analyze and display result
					currentSample.analyzePeaks(halfLife, window);
					//currentSample.play();
					currentSample.printInfo();

					//analyze again?
					System.out.println("Is this peak analysis satisfactory? (y/n)");
					String response = scanner.next();
					if (response.equals("y")){
						auditionAgain = false;
					}
				}
			}

			//phasing
			if (programChoice == 3) {
				currentSample.printInfo();

				//get parameters from user
				System.out.println("Specify the first delay frame: ");
				int delayFrame = scanner.nextInt();
				System.out.println("Specify the interval "
						+ "to further increment (in frames): ");
				int delayInterval = scanner.nextInt();
				System.out.println("Specify the maximum number of iterations to run: ");
				int maxIter = scanner.nextInt();

				//do the phasing
				SampleBatch phaseResults = currentSample.phase(
						delayFrame, delayInterval, maxIter);

				//ask user what to do with results
				System.out.println("1. Select from (and hear) results.");
				System.out.println("2. Write all results to files.");
				int resultChoice = scanner.nextInt();

				//select from batch
				if (resultChoice == 1) {
					System.out.println("1. Select among results for frequency changes. \n"
							+ "2. Select among results for rhythmic/peak changes.");
					int selectChoice = scanner.nextInt();

					//select on frequency
					if (selectChoice == 1) {

						//analyze
						phaseResults.diffFreqs();
						System.out.println("There were " + phaseResults.getChildSamples().size()
								+ " results.");

						//ask user for how many
						System.out.println("The results with greatest frequency changes "
								+ "will be auditioned.\nMaximum number?");
						int maxToAudition = scanner.nextInt();
						SampleBatch selections = phaseResults.selectOnFreqs(maxToAudition);

						//ask user which one to play
						selections.printChildren();
						boolean auditioningChildren = true;
						int lastKey = 0;
						while (auditioningChildren) {
							System.out.println("Play which? Enter its key. Or 0 to select "
									+ "the last played sample and return to main menu.");
							int userChoice = scanner.nextInt();
							if (userChoice != 0 && 
									selections.getChildSamples().containsKey(userChoice)) {
								lastKey = userChoice;
								selections.getChildSamples().get(lastKey).play();
							}
							else {
								currentSample = selections.getChildSamples().get(lastKey);
								auditioningChildren = false;
							}
						}
					}

					//select on peaks
					if (selectChoice == 2) {
						phaseResults.diffPeaks();
						System.out.println("There were " + 
								phaseResults.getChildSamples().size()
								+ " results.");

						//ask user for how many
						System.out.println("The results with greatest changes in number of"
								+ " peaks will be auditioned."
								+ "\nMaximum number?");
						int maxToAudition = scanner.nextInt();
						SampleBatch selections = phaseResults.selectOnPeaks(maxToAudition);

						//ask user which one to play
						selections.printChildren();
						boolean auditioningChildren = true;
						int lastKey = 0;
						while (auditioningChildren) {
							System.out.println("Play which? Enter its key. Or 0 to select "
									+ "the last played sample and return to main menu.");
							int userChoice = scanner.nextInt();
							if (userChoice != 0 && 
									selections.getChildSamples().containsKey(userChoice)) {
								lastKey = userChoice;
								selections.getChildSamples().get(lastKey).play();
							}
							else {
								currentSample = selections.getChildSamples().get(lastKey);
								auditioningChildren = false;
							}
						}
					}
				}

				//write the batch to individual files
				if (resultChoice == 2) {
					IterableSample.writeAllFiles(phaseResults);
				}

			}

			//write to file
			if (programChoice == 4) {
				IterableSample.writeFile(currentSample);
			}

			//rename and store sample
			if (programChoice == 5) {
				System.out.println("Rename the sample: ");
				scanner.nextLine();
				String rename = scanner.nextLine();
				currentSample.setPrefix(rename);
				savedSamples.put(currentSample.getPrefix(), currentSample);
			}

			//recall a sample
			if (programChoice == 6) {
				System.out.println("Saved samples: \n" + savedSamples.keySet().toString());
				System.out.println("Enter the sample to recall: ");

				//advance scanner
				scanner.nextLine();
				//get input
				String recalled = scanner.nextLine();
				if (savedSamples.keySet().contains(recalled)) {
					currentSample = savedSamples.get(recalled);
				}
			}


			//mix two samples together (add or subtract)
			if (programChoice == 7) {
				System.out.println("MIXER \nSaved samples: \n" + 
						savedSamples.keySet().toString());
				System.out.println("Enter the sample to mix with: ");

				//advance the scanner and get input
				scanner.nextLine();
				String recalled = scanner.nextLine();

				//do the mixing
				//declare otherSample, first with dummy values
				IterableSample otherSample = new IterableSample(currentSample.getPrefix(), 
						currentSample.getLength(), currentSample.getAudioData());
				//replace dummy values with the recalled sample if it exists
				if (savedSamples.keySet().contains(recalled)) {
					otherSample = savedSamples.get(recalled);
				}
				System.out.println("Add (1.) or subtract (2.) this sample to/from the original.");
				int userChoice = scanner.nextInt();
				if (userChoice == 1) {
					IterableSample result = currentSample.sum(otherSample);
					currentSample = result;
				}
				else {
					IterableSample result = currentSample.diff(otherSample);
					currentSample = result;
				}
			}

			//scale the current sample
			if (programChoice == 8) {
				System.out.println("Enter the scaling factor in decimal form:");
				float scalingFactor = scanner.nextFloat();
				IterableSample scaledSample = currentSample.scale(scalingFactor);
				currentSample = scaledSample;
			}
			
			//load a different sample
			if (programChoice == 9) {
				sampleMissing = true;
			}

			//quit
			if (programChoice == 10) {
				System.out.println("quitting...");
				System.exit(0);
			}
		}
	}
}