package gov.usgs.aqcu.builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;

import gov.usgs.aqcu.model.AqcuReportMetadata;

@Component
public class AqcuReportMetadataBuilderService {
	private static final Logger LOG = LoggerFactory.getLogger(AqcuReportMetadataBuilderService.class);

	public AqcuReportMetadata createBaseReportMetadata(
		String reportType,
		String reportTitle,
		String primaryTimeSeriesIdentifier,
		String primaryParameter,
		Double utcOffset,
		String stationName,
		String stationId,
		List<GradeMetadata> gradeMetadataList,
		List<QualifierMetadata> qualifierMetadataList,
		Instant startDate,
		Instant endDate,
		Map<String,String> urlParams,
		String requestingUser) {
		AqcuReportMetadata metadata = new AqcuReportMetadata();
		
		metadata.setRequestingUser(requestingUser);
		metadata.setTimezone("Etc/GMT+" + (int)(-1 * utcOffset));
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);
		metadata.setTitle(reportTitle);
		metadata.setReportType(reportType);
		metadata.setAdvancedOptions(urlParams);
		metadata.setPrimaryParameter(primaryParameter);
		metadata.setStationName(stationName);
		metadata.setStationId(stationId);
		metadata.setQualifierMetadata(qualifierMetadataList);
		metadata.setGradeMetadata(gradeMetadataList);
		metadata.setPrimaryTimeSeriesIdentifier(primaryTimeSeriesIdentifier);
		
		return metadata;
	}
}
	
