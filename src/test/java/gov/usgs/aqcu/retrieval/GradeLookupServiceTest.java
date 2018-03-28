package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class GradeLookupServiceTest {
	@MockBean
	private AquariusRetrievalService aquariusService;
	private GradeLookupService service;
	
	GradeMetadata gradeMA = new GradeMetadata()
		.setColor("color-a")
		.setDescription("desc-a")
		.setDisplayName("name-a")
		.setIdentifier("id-a");
	GradeMetadata gradeMB = new GradeMetadata()
		.setColor("color-b")
		.setDescription("desc-b")
		.setDisplayName("name-b")
		.setIdentifier("id-b");
	GradeMetadata gradeMC = new GradeMetadata()
		.setColor("color-c")
		.setDescription("desc-c")
		.setDisplayName("name-c")
		.setIdentifier("id-c");
	Grade gradeA = new Grade()
		.setGradeCode("id-a");
	Grade gradeB = new Grade()
		.setGradeCode("id-b");


    @Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new GradeLookupService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new GradeListServiceResponse()
				.setGrades(new ArrayList<GradeMetadata>(Arrays.asList(gradeMA, gradeMB, gradeMC))));
	}

	@Test
	public void getByGradeListEmptyTest() {
		Map<String, GradeMetadata> actual = service.getByGradeList(new ArrayList<>());
		assertEquals(actual.size(), 0);
	}

	@Test
	public void buildItentifierListTest() {
		List<String> actual = service.buildIdentifierList(Arrays.asList(gradeA, gradeB));
		assertEquals(2, actual.size());
		assertThat(actual, containsInAnyOrder("id-a", "id-b"));
		}

	@Test
	public void filterListTest() {
		Map<String, GradeMetadata> actual = service.filterList(Arrays.asList("id-a", "id-b"), Arrays.asList(gradeMA, gradeMB, gradeMC));
		assertEquals(2, actual.size());
		assertThat(actual, IsMapContaining.hasEntry("id-a", gradeMA));
		assertThat(actual, IsMapContaining.hasEntry("id-b", gradeMB));
		}

	@Test
	public void getTest() throws Exception {
		List<GradeMetadata> actual = service.get();
		assertEquals(3, actual.size());
		assertThat(actual, containsInAnyOrder(gradeMA, gradeMB, gradeMC));
	}

	@Test
	public void getByGradeListTest() {
		Map<String, GradeMetadata> actual = service.getByGradeList(Arrays.asList(gradeA, gradeB));
		assertEquals(actual.size(), 2);
		assertThat(actual, IsMapContaining.hasEntry("id-a", gradeMA));
		assertThat(actual, IsMapContaining.hasEntry("id-b", gradeMB));
	}
}