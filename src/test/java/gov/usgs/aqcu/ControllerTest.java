package gov.usgs.aqcu;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.GsonBuilder;

import gov.usgs.aqcu.model.TimeSeriesSummaryReport;

import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;
import gov.usgs.aqcu.retrieval.UpchainProcessorListService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;

import gov.usgs.aqcu.builder.TimeSeriesSummaryReportBuilderService;

public class ControllerTest {

	@Mock
	private TimeSeriesMetadataService timeSeriesMetadataService;
	@Mock
	private UpchainProcessorListService upchainProcessorListService;
	@Mock
	private RatingCurveListService ratingCurveListService;
	@Mock
	private TimeSeriesSummaryReportBuilderService reportBuilderService;
	private TheController controller;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new TheController(timeSeriesMetadataService, upchainProcessorListService, ratingCurveListService, reportBuilderService);
	}
	
	@Test(expected = java.lang.NullPointerException.class)
	public void noParametersTest() {
		when(timeSeriesMetadataService.get(anyString())).thenReturn(null);
		when(upchainProcessorListService.get(anyString(), anyObject(), anyObject())).thenReturn(null);
		TimeSeriesSummaryReport report = controller.getReport(null, null, null, null, null, null);
	}
	
	@Test
	public void noRatingModelTest() {
		TimeSeriesSummaryReport blankReport = new TimeSeriesSummaryReport();
		when(timeSeriesMetadataService.get(anyString())).thenReturn(null);
		when(upchainProcessorListService.get(anyString(), anyObject(), anyObject())).thenReturn(null);
		when(reportBuilderService.buildTimeSeriesSummaryReport(anyObject(), anyObject(), anyObject(), anyObject(), anyString())).thenReturn(blankReport);
		
		TimeSeriesSummaryReport report = controller.getReport(null, null, null, "2017-01-01T00:00:00Z", "2017-02-01T00:00:00Z", null);
		
		verify(timeSeriesMetadataService).get(anyString());
		verify(upchainProcessorListService).get(anyString(), anyObject(), anyObject());
		verify(ratingCurveListService, never()).get(anyString(), anyObject(), anyObject(), anyObject());
		verify(reportBuilderService).buildTimeSeriesSummaryReport(anyObject(), anyObject(), anyObject(), anyObject(), anyString());
		
		assertEquals(report, blankReport);
	}
}
