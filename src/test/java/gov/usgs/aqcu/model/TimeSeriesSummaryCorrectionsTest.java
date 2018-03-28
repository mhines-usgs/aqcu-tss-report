package gov.usgs.aqcu.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.usgs.aqcu.retrieval.CorrectionListServiceTest;

public class TimeSeriesSummaryCorrectionsTest {
    List<ExtendedCorrection> corrList;

    @Before
    public void setup() {
        corrList = new ArrayList<>();
        corrList.add(new ExtendedCorrection(CorrectionListServiceTest.CORR_A));
        corrList.add(new ExtendedCorrection(CorrectionListServiceTest.CORR_B));
    }

    @Test
	public void constructorTestt() {
        TimeSeriesSummaryCorrections corrections = new TimeSeriesSummaryCorrections(corrList, "corr-url");
        assertEquals(corrections.getCorrUrl(), "corr-url");
		assertEquals(corrections.getPreProcessing().size(), 0);
		assertEquals(corrections.getNormal().size(), 1);
		assertThat(corrections.getNormal(), containsInAnyOrder(corrList.get(0)));
		assertEquals(corrections.getPostProcessing().size(), 1);
		assertThat(corrections.getPostProcessing(), containsInAnyOrder(corrList.get(1)));
	}
}