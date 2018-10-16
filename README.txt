README

Audio Sample Batch Analyzer


(IMPORTANT: This program relies on the JSyn library, found in this package in “libs”. It must be added to the build path of the project.)


This program reads monophonic audio from a .wav or .aiff file (an “audio sample”) in order to let the user perform transformations on it. Currently, the two transformations implemented are:

* (micro)phasing


* adding or subtracting another sample (mixing)


MORE on PHASING

To “phase” a sample is to combine the original sample with a delayed copy of itself. (“Combined” means sounding together.) For this reason you can think of it as a “stuttering effect.” When complex waveforms (such as the example file oneTone.wav) are phased, they produce not a stuttering effect but rather a “subtle filtering” one. (link: https://docs.cycling74.com/max7/tutorials/15_delaychapter01)

Because phasing only demands specifying the amount of time by which to delay the copy, and since standard audio formats store 44,100 frames of audio data per second, tens of thousands of results (or more) can be generated for a single one-second sample, and some of them can sound radically unique.

What makes this program an “analyzer” is that after generating such a large “batch” of results, it can analyze each sample in the batch for two characteristics——frequencies and peaks——and prompt the user to select the most unique results yielded. This mitigates the trial-and-error nature of microphasing by hand, that is, “going looking” for the interesting results by merely guessing at the interval by which to delay the copy.

Often, the most interesting results are yielded from phasing by a very small amount, only microseconds, or one to a few dozen frames. This is why I have written the program. For this reason I refer to this as “microphasing.”


MIXING

This program also implements additive and subtractive synthesis of two samples. This program takes a naive approach of adding or subtracting each frame of the otherSample from/to the currentSample to arrive at the resultSample.

Audio data is stored as a double from -1.0 to 1.0. The naive approach does not yet normalize the results, or check for clipping (further audio processing concepts), but scaling has been implemented.


MAIN MENU

The program will play the currentSample under consideration. The user can:

1. Print info about this sample.
2. Re-analyze this sample for its peaks.
3. (Micro)phase this sample and generate a batch of results.
4. Write this sample to a file.
5. Save this sample for later recall.
6. Recall a sample for manipulation.
7. Mix this sample with a recalled sample (sum or difference).
8. Scale this sample's amplitude.
9. Load another sample.
10. Quit.


USER INPUT DURING PHASING

In order to generate a result, the program needs to know which frame of the audio data represents the point where the delay should finish and the copy should begin. This is called the “delay frame.” “example” phased at 600frames would yield “example_phased600frames.”

The user can specify the first delay frame, as well as an interval by which to increment to get each subsequent delay frame, and finally a maximum number of results to generate.

“Specify the first delay frame: “
“Specify the interval to further increment (in frames): “
"Specify the maximum number of iterations to run: "

If a user supplied 1, 3, 10, the program would generate 10 results using these as the delay frames:
3, 13, 23, 33, 43, 53, 63, 73, 83, 93, 103 —- assuming the file had at least 103 frames. For this reason the length of the sample in frames is always printed.

If a user supplied 1, 1, 10000, the program would generate 10,000 results.


ANALYZING AND SELECTING THE RESULTS

The results can be immediately saved to individual files, or the user can select from the results according to total changes in frequency or peaks. (We are looking for transformation of the sample, not something that sounds the same.)

The program uses an external class written by Greg Milette to measure the frequency (pitch) of a sample by counting zero crossings. (Audio data spikes and falls from -1.0 to 1.0, crossing 0.0 along the way.)

The program has original code to measure the peaks of a sample, or its rhythmic “notes” or onsets. (Think of this as whether or not a “stutter” is perceptible as a separate rhythmic event.) The user must provide measures of sensitivity for the analysis: a halfLife and a window. More detail in “Technical Design”, below.

The program has original code to measure the absolute change (delta) in frequency and peaks.

The user can specify the maximum number of results to audition (hear). If the user is selecting on frequency, and specifies 10, the program will display (up to) 10 results that differed most in frequency. Same for peaks. The user can play all 10 results before selecting one that becomes the new currentSample to be manipulated.

Samples with silences (such as fourTones.wav) provide obvious peaks, and so the analysis can get more accurate in these cases. Whether the analysis is accurate for the parentSample or not, it at least provides a baseline for measuring changes.


ITERATING

The process is designed to be iterable. Phase and mix to your heart’s content, and when you find a sound you like, save it to memory and mash things up more!



TECHNICAL DESIGN

class BatchAnalyzer: contains main(); specifies currentSample being manipulated; holds storedSamples in memory for later recall.

class IterableSample; represents the sample being manipulated, holds info about frequency, peaks, peakAnalysisParameters. Computation done here.

class SampleBatch: holds an IterableSample that is the parentSample, holds IterableSamples childSamples keyed on the delay frame that generated them, holds measurements of changes in frequency and peaks. Computation on the entire batch done here.

class ZeroCrossing is authored by Greg Milette. The frequency analyzer in JSyn is designed for real-time audio processing, and it was not feasible in the time available for the first academic purpose of the project to add that complexity.

