package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;

import gov.usgs.aqcu.model.ExtendedCorrection;

@Repository
public class CorrectionListService  {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public CorrectionListService(
		AquariusRetrievalService aquariusRetrievalService
	) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	public CorrectionListServiceResponse getRawResponse(String timeseriesUniqueId, Instant startDate, Instant endDate) {
		CorrectionListServiceRequest request = new CorrectionListServiceRequest()
				.setTimeSeriesUniqueId(timeseriesUniqueId)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		CorrectionListServiceResponse correctionListResponse = aquariusRetrievalService.executePublishApiRequest(request);
		return correctionListResponse;
	}

	public List<ExtendedCorrection> getExtendedCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate, List<String> excludedCorrections) {
		return createExtendedCorrectionsFromCorrections(getRawResponse(timeseriesUniqueId, startDate, endDate).getCorrections(), excludedCorrections);
	}

	public List<ExtendedCorrection> getExtendedCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate) {
		return getExtendedCorrectionList(timeseriesUniqueId, startDate, endDate, null);
	}

	private List<ExtendedCorrection> createExtendedCorrectionsFromCorrections(List<Correction> sourceCorrections, List<String> excludedCorrections) {
		List<ExtendedCorrection> correctionList = new ArrayList<>();

		//Convert and Filter Corrections
		if(!sourceCorrections.isEmpty()) {
			for(Correction corr :  sourceCorrections) {
				//Convert to TSS Correction Object to allow for "CopyPaste" type replacement
				Boolean doAdd = true;
				ExtendedCorrection aqcuCorr = new ExtendedCorrection(corr);

				if(excludedCorrections != null && !excludedCorrections.isEmpty()) {
					//Filter Excluded Corrections
					for(String exclude : excludedCorrections) {
						if(exclude.equalsIgnoreCase(aqcuCorr.getDominantType())) {
							doAdd = false;
							break;
						}
					}
				}

				if(doAdd) {
					correctionList.add(aqcuCorr);
				}
			}
		}

		return correctionList;
	}
}
