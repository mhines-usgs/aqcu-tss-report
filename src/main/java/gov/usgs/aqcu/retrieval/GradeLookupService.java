package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import gov.usgs.aqcu.exception.AquariusException;

@Component
public class GradeLookupService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(GradeLookupService.class);

	public List<GradeMetadata> get() throws AquariusException {
		GradeListServiceRequest request = new GradeListServiceRequest();
		GradeListServiceResponse gradeListResponse = executePublishApiRequest(request);
		return gradeListResponse.getGrades();
	}

	public List<GradeMetadata> getByIdentifierList(List<String> includeIdentifiers) throws AquariusException {
		List<GradeMetadata> filtered = new ArrayList<>();
		List<GradeMetadata> metadataList = get();
		for(GradeMetadata metadata : metadataList) {
			if(includeIdentifiers.contains(metadata.getIdentifier())) {
				filtered.add(metadata);
			}
		}

		return filtered;
	}

	public List<GradeMetadata> getByGradeList(List<Grade> includeGrades) throws AquariusException {
		List<String> gradeIdentifiers = new ArrayList<>();

		for(Grade grade : includeGrades) {
			gradeIdentifiers.add(grade.getGradeCode());
		}

		return getByIdentifierList(gradeIdentifiers);
	}
}
