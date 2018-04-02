package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

@Repository
public class GradeLookupService {
	private static final Logger LOG = LoggerFactory.getLogger(GradeLookupService.class);

	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public GradeLookupService(
		AquariusRetrievalService aquariusRetrievalService
	) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	public Map<String, GradeMetadata> getByGradeList(List<Grade> includeGrades) {
		List<GradeMetadata> gradeList = new ArrayList<>();
		List<String> gradeIdentifiers = buildIdentifierList(includeGrades);

		try {
			gradeList = get();
		} catch (Exception e) {
			String msg = "An unexpected error occurred while attempting to fetch GradeMetadata from Aquarius: ";
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}

		return filterList(gradeIdentifiers, gradeList);
	}

	protected List<String> buildIdentifierList(List<Grade> includeGrades) {
		return includeGrades.stream()
				.map(x -> x.getGradeCode())
				.collect(Collectors.toList());
	}

	protected List<GradeMetadata> get() {
		GradeListServiceRequest request = new GradeListServiceRequest();
		GradeListServiceResponse gradeListResponse = aquariusRetrievalService.executePublishApiRequest(request);
		return gradeListResponse.getGrades();
	}

	protected Map<String, GradeMetadata> filterList(List<String> includeIdentifiers, List<GradeMetadata> gradeList) {
		Map<String, GradeMetadata> filtered = gradeList.stream()
				.filter(x -> includeIdentifiers.contains(x.getIdentifier()))
				.collect(Collectors.toMap(x -> x.getIdentifier(), x -> x));

		return filtered;
	}
}