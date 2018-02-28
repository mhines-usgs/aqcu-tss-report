package gov.usgs.aqcu;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
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

}
