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

import gov.usgs.aqcu.model.AqcuExtendedCorrection;

@Component
public class CorrectionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	public CorrectionListServiceResponse getRawResponse(String timeseriesUniqueId, Instant startDate, Instant endDate) throws Exception {
		CorrectionListServiceRequest request = new CorrectionListServiceRequest()
				.setTimeSeriesUniqueId(timeseriesUniqueId)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		CorrectionListServiceResponse correctionListResponse = executePublishApiRequest(request);
		return correctionListResponse;
	}
	
	public List<Correction> getCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate) throws Exception  {
		return getRawResponse(timeseriesUniqueId, startDate, endDate).getCorrections();
	}
	
	public List<AqcuExtendedCorrection> getAqcuExtendedCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate, List<String> excludedCorrections) throws Exception  {
		return createAqcuExtendedCorrectionsFromCorrections(getCorrectionList(timeseriesUniqueId, startDate, endDate), excludedCorrections);
	}
	
	public List<AqcuExtendedCorrection> getAqcuExtendedCorrectionList(String timeseriesUniqueId, Instant startDate, Instant endDate) throws Exception  {
		return getAqcuExtendedCorrectionList(timeseriesUniqueId, startDate, endDate, null);
	}
	
	public List<AqcuExtendedCorrection> createAqcuExtendedCorrectionsFromCorrections(List<Correction> sourceCorrections, List<String> excludedCorrections) {
		List<AqcuExtendedCorrection> correctionList = new ArrayList<>();
		
		//Convert and Filter Corrections
		if(sourceCorrections.size() > 0) {
			for(Correction corr :  sourceCorrections) {
				//Convert to TSS Correction Object to allow for "CopyPaste" type replacement
				Boolean doAdd = true;
				AqcuExtendedCorrection aqcuCorr = new AqcuExtendedCorrection(corr);
			
				if(excludedCorrections != null && excludedCorrections.size() > 0) {
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
