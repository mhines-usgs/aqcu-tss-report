package gov.usgs.aqcu;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;

import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;

public class ControllerTest {

	@Mock
	private TimeSeriesMetadataService timeSeriesMetadataService;
	private TheController controller;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new TheController(timeSeriesMetadataService);
	}

	@Test
	public void aTest() {
		when(timeSeriesMetadataService.get(anyString())).thenReturn(null);
		TimeSeriesDescriptionListByUniqueIdServiceResponse response = controller.getReport(null, null, null, null, null, null, null);
		assertNull(response);
		verify(timeSeriesMetadataService).get(anyString());
	}

}
