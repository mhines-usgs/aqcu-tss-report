package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;

import gov.usgs.aqcu.model.ExtendedCorrection;

@Component
public class CorrectionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	public CorrectionListServiceResponse getRawResponse(String timeseriesUniqueId, Instant startDate, Instant endDate) {
		CorrectionListServiceRequest request = new CorrectionListServiceRequest()
				.setTimeSeriesUniqueId(timeseriesUniqueId)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		CorrectionListServiceResponse correctionListResponse = executePublishApiRequest(request);
		return correctionListResponse;
	}

	public List<ExtendedCorrection> getAqcuExtendedCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate, List<String> excludedCorrections) {
		return createAqcuExtendedCorrectionsFromCorrections(getRawResponse(timeseriesUniqueId, startDate, endDate).getCorrections(), excludedCorrections);
	}

	public List<ExtendedCorrection> getAqcuExtendedCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate) {
		return getAqcuExtendedCorrectionList(timeseriesUniqueId, startDate, endDate, null);
	}

	private List<ExtendedCorrection> createAqcuExtendedCorrectionsFromCorrections(List<Correction> sourceCorrections, List<String> excludedCorrections) {
		List<ExtendedCorrection> correctionList = new ArrayList<>();

		//Convert and Filter Corrections
		if(!sourceCorrections.isEmpty()) {
			for(Correction corr :  sourceCorrections) {
				//Convert to TSS Correction Object to allow for "CopyPaste" type replacement
				Boolean doAdd = true;
				ExtendedCorrection aqcuCorr = new ExtendedCorrection(corr);

				if(excludedCorrections.isEmpty()) {
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
