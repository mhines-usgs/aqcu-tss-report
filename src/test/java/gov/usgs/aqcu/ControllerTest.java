package gov.usgs.aqcu;

import org.mockito.Mock;

import com.google.gson.Gson;

import gov.usgs.aqcu.retrieval.UpchainProcessorListService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.LocationDescriptionService;

import gov.usgs.aqcu.builder.TimeSeriesSummaryReportBuilderService;
import gov.usgs.aqcu.client.JavaToRClient;

public class ControllerTest {
	@Mock
	private UpchainProcessorListService upchainProcessorListService;
	@Mock
	private RatingCurveListService ratingCurveListService;
	@Mock
	private LocationDescriptionService locationDescriptionListService;
	@Mock
	private TimeSeriesSummaryReportBuilderService reportBuilderService;
	@Mock
	private JavaToRClient client;
	private Gson gson;
	private Controller controller;

	/*
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new Controller(timeSeriesMetadataService, upchainProcessorListService, ratingCurveListService, reportBuilderService);
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
	*/
}
