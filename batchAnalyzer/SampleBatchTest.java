package batchAnalyzer;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

public class SampleBatchTest {

	float[] dummyData = {-1.0f, -0.75f, -0.5f, -0.25f, 0.0f, 0.25f, 0.50f, 0.75f, 1.0f};
	IterableSample dummyParent = new IterableSample("dummyParent", 9, dummyData);
	HashMap<Integer, IterableSample> dummyChildren = new HashMap<Integer, IterableSample>();
	SampleBatch dummyBatch = new SampleBatch(dummyParent, dummyChildren);
	TreeMap<Double, Integer> dummyFreqs = new TreeMap<Double, Integer>();
	TreeMap<Integer, Integer> dummyPeaks = new TreeMap<Integer, Integer>();

	SampleBatch dummyPhased1 = dummyParent.phase(1, 1, 8);
	SampleBatch dummyPhased6 = dummyParent.phase(1, 1, 6);
	
	@Before
	public void setUp() throws Exception {
	}
	
	
	@Test
	public void testSampleBatch() {
		assertTrue(dummyBatch.getParentSample().equals(dummyParent));
		assertTrue(dummyBatch.getChildSamples().equals(dummyChildren));
		assertTrue(dummyBatch.getDiffFreqs().equals(dummyFreqs));
		assertTrue(dummyBatch.getDiffPeaks().equals(dummyPeaks));
	}

	

	@Test
	public void testGetParentSample() {
		assertTrue(dummyBatch.getParentSample().equals(dummyParent));
	}

	@Test
	public void testSetParentSample() {
		IterableSample dummyParent2 = new IterableSample("dummyParent2", 9, dummyData);
		dummyBatch.setParentSample(dummyParent2);
		assertTrue(dummyBatch.getParentSample().equals(dummyParent2));
	}

	@Test
	public void testGetChildSamples() {
		assertTrue(dummyBatch.getChildSamples().equals(dummyChildren));
	}

	@Test
	public void testSetChildSamples() {
		HashMap<Integer, IterableSample> dummyChildren2 = new HashMap<Integer, IterableSample>();
		dummyBatch.setChildSamples(dummyChildren2);
		assertTrue(dummyBatch.getChildSamples().equals(dummyChildren2));
	}

	@Test
	public void testGetBatchname() {
		dummyBatch.setBatchname("banana");
		assertEquals("banana", dummyBatch.getBatchname());
	}

	@Test
	public void testSetBatchname() {
		dummyBatch.setBatchname("orange");
		assertEquals("orange", dummyBatch.getBatchname());
	}

	@Test
	public void testGetDiffPeaks() {
		assertTrue(dummyBatch.getDiffPeaks().equals(dummyPeaks));
	}

	@Test
	public void testGetDiffFreqs() {
		assertTrue(dummyBatch.getDiffPeaks().equals(dummyFreqs));
	}

	@Test
	public void testDiffFreqs() {
		dummyParent.setFrequency(4410);
		dummyPhased1.diffFreqs();
		//no frequency change -- no change in zero crossings
		//check the delta (0) was put with value 1 (previous key)
		
		System.out.println(dummyParent.getFrequency());
		System.out.println(dummyPhased1.getDiffFreqs());
		assertTrue(dummyPhased1.getDiffFreqs().get(0.0).equals(1));
		//frequency change! 
		assertTrue(dummyPhased6.getDiffFreqs().get(1470.0).equals(6));
		
		
	}

	@Test
	public void testDiffPeaks() {
		//each phase operation produced a new peak
		assertTrue(dummyPhased1.getDiffPeaks().get(1).equals(1));
		assertTrue(dummyPhased1.getDiffPeaks().get(5).equals(5));
	}

}
