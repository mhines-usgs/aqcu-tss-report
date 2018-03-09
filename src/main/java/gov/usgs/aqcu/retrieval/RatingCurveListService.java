package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.Map;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class RatingCurveListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	public RatingCurveListServiceResponse getRawResponse(String ratingModelIdentifier, Double utcOffset, Instant startDate, Instant endDate) throws Exception {
		RatingCurveListServiceRequest request = new RatingCurveListServiceRequest()
				.setRatingModelIdentifier(ratingModelIdentifier)
				.setUtcOffset(utcOffset)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		RatingCurveListServiceResponse ratingCurveResponse = executePublishApiRequest(request);
		return ratingCurveResponse;
	}
	
	/* WIP
	public List<RatingCurve> getAqcuFilteredRatingCurves(RatingCurveListServiceResponse response, Instant startDate, Instant endDate) {
		List<RatingCurve> responseCurves = response.getRatingCurves();
		List<RatingCurve> filteredCurves = new ArrayList<>();
		List<Pair<int, PeriodOfApplicability>> fullPeriodList = new ArrayList<>();
		
		//Get Full List of Curve Periods
		for(var i = 0; i < responseCurves.size(); i++) {
			for(PeriodOfApplicability period : responseCurves.get(i).getPeriodsOfApplicability()) {
				fullPeriodList.add(new Pair<>(i, period);
			}
		}
		
		//Sort Period List by Start Date
		Collections.sort(fullPeriodList, new Comparator<Pair<int, PeriodOfApplicability>>() {
			@Override
			public int compare(final Pair<int, PeriodOfApplicability> o1, final Pair<int, PeriodOfApplicability> o2) {
				return o1.getValue().getStartTime().compareTo(o2.getValue().getStartTime());
			}
		});
		
		//Apply Fake End Times to Period List
		for(var i = 0; i < fullPeriodList.size(); i++) {
			Pair<int,PeriodOfApplicability> pair = fullPeriodList.get(i);
			
			if(isOpenEndedTime(pair.getValue().getEndTime())) {
				
			}
		}

		//Track the first rating that starts after the report period
		RatingCurve firstCurveAfterPeriod = null;
		Instant firstCurveAfterPeriodMin = null;
		Instant firstCurveAfterPeriodMax = null;
		
		for(RatingCurve curve : responseCurves) {
			Pair<Instant,Instant> curveMinMax = getCurveMinMaxTimes(curve);
			Instant curveMin = curveMinMax.getKey();
			Instant curveMax = curveMinMax.getValue();
			
			if(curveMin.compareTo(endDate) <=0 && curveMax.compareTo(startDate) >= 0) {
				filteredCurves.add(curve);
			} else if(curveMin.compareTo(endDate) >= 0) {
				if(firstCurveAfterPeriod == null || curveMin.compareTo(firstCurveAfterPeriodMin) <= 0) {
					firstCurveAfterPeriod = curve;
					firstCurveAfterPeriodMin = curveMin;
					firstCurveAfterPeriodMax = curveMax;
				}
			}
		}
		
		if(firstCurveAfterPeriod != null && isOpenTimePeriod(firstCurveAfterPeriodMax)) {
			filteredCurves.add(firstCurveAfterPeriod);
		}
		
		return filteredCurves;
	}
	
	public List<RatingShift> getAqcuFilteredRatingShifts(RatingCurveListServiceResponse response, Instant startDate, Instant endDate) {
		//Assign Fake End Times to shifts
		
	}
	
	public List<RatingCurve> getAqcuFilteredRatingCurves(String ratingModelIdentifier, Double utcOffset, Instant requestStartDate, Instant requestEndDate, Instant filterStartDate, Instant filterEndDate) throws Exception {
		return getAqcuFilteredRatingCurves(getRawResponse(ratingModelIdentifier, utcOffset, requestStartDate, requestEndDate), filterStartDate, filterEndDate);
	}
	
	private Pair<Instant,Instant> getCurveMinMaxTimes(RatingCurve curve) {
		Instant curveStart = curve.getPeriodsOfApplicability().get(0).getStartTime();
		Instant curveEnd = curve.getPeriodsOfApplicability().get(0).getEndTime();
			
		for(PeriodOfApplicability period : curve.getPeriodsOfApplicability()) {
			if(period.getStartTime().compareTo(curveStart) < 0) {
				curveStart = period.getStartTime();
			}
			
			if(period.getEndTime().compareTo(curveEnd) > 0) {
				curveEnd = period.getEndTime();
			}
		}
		
		return new Pair<>(curveStart,curveEnd);
	}
	
	private boolean isOpenEndedTime(Instant time) {
		Instant openStart = Instant.parse("0001-01-01T00:00:00Z");
		Instant openEnd = Instant.parse("9999-12-31T00:00:00Z");
		
		if(time.compareTo(openStart) <= 0 || time.compareTo(openEnd) >= 0) {
			return true;
		}
		
		return false;
	}
	*/
}
