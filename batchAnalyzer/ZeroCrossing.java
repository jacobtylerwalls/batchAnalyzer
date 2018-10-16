package batchAnalyzer;

//THIS IS NOT MY CODE -- THIS IS AN EXTERNAL I HAVE CITED IN MY README FILE

	/**
	 * Calculates zero crossings to estimate frequency
	 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
	 */
	public class ZeroCrossing {

	    /**
	     * calculate frequency using zero crossings
	     */
	    public static int calculate(int sampleRate, float [] audioData)
	    {
	        int numSamples = audioData.length;
	        int numCrossing = 0;
	        for (int p = 0; p < numSamples-1; p++)
	        {
	            if ((audioData[p] > 0 && audioData[p + 1] <= 0) || 
	                (audioData[p] < 0 && audioData[p + 1] >= 0))
	            {
	                numCrossing++;
	            }
	        }

	        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
	        float numCycles = numCrossing/2;
	        float frequency = numCycles/numSecondsRecorded;

	        return (int)frequency;
	    }
	}
