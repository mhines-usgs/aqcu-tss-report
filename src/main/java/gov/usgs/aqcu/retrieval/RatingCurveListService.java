package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.Instant;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;

import gov.usgs.aqcu.util.AqcuTimeUtils;

@Component
public class RatingCurveListService {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public RatingCurveListService(
		AquariusRetrievalService aquariusRetrievalService
	) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	public RatingCurveListServiceResponse getRawResponse(String ratingModelIdentifier, Double utcOffset, Instant startDate, Instant endDate) {
		RatingCurveListServiceRequest request = new RatingCurveListServiceRequest()
				.setRatingModelIdentifier(ratingModelIdentifier)
				.setUtcOffset(utcOffset)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		RatingCurveListServiceResponse ratingCurveResponse = aquariusRetrievalService.executePublishApiRequest(request);
		return ratingCurveResponse;
	}

	public List<RatingCurve> getAqcuFilteredRatingCurves(List<RatingCurve> responseCurves, Instant startDate, Instant endDate) {
		Set<RatingCurve> filteredCurves = new HashSet<>();
		List<ImmutablePair<Integer,PeriodOfApplicability>> fullPeriodList = new ArrayList<>();
		List<ImmutablePair<Integer,PeriodOfApplicability>> filteredPeriodList = new ArrayList<>();

		//Get Full List of Curve Periods
		for(int i = 0; i < responseCurves.size(); i++) {
			for(PeriodOfApplicability period : responseCurves.get(i).getPeriodsOfApplicability()) {
				fullPeriodList.add(new ImmutablePair<Integer,PeriodOfApplicability>(i, period));
			}
		}

		//Filter Full Period List
		filteredPeriodList = getRatingPeriodsWithinReportRange(fullPeriodList, startDate, endDate);

		//Build Filtered Curve List from Filtered Period List
		for(ImmutablePair<Integer,PeriodOfApplicability> pair : filteredPeriodList) {
			filteredCurves.add(responseCurves.get(pair.getKey()));
		}

		return new ArrayList<>(filteredCurves);
	}
	
	public List<RatingShift> getAqcuFilteredRatingShifts(List<RatingCurve> curves, Instant startDate, Instant endDate) {
		List<RatingShift> curveShifts = new ArrayList<>();
		List<RatingShift> filteredShifts = new ArrayList<>();
		List<ImmutablePair<Integer,PeriodOfApplicability>> fullPeriodList = new ArrayList<>();
		List<ImmutablePair<Integer,PeriodOfApplicability>> filteredPeriodList = new ArrayList<>();

		//Build full Shift List and Shift Period List
		for(RatingCurve curve : curves) {
			for(RatingShift shift : curve.getShifts()) {
				int shiftIndex = curveShifts.size();
				curveShifts.add(shift);
				fullPeriodList.add(new ImmutablePair<Integer,PeriodOfApplicability>(shiftIndex, shift.getPeriodOfApplicability()));
			}
		}

		//Filter Full Period List
		filteredPeriodList = getRatingPeriodsWithinReportRange(fullPeriodList, startDate, endDate);

		//Build Filtered Shift List from Filtered Period List
		for(ImmutablePair<Integer,PeriodOfApplicability> pair : filteredPeriodList) {
			filteredShifts.add(curveShifts.get(pair.getKey()));
		}

		return filteredShifts;
	}

	protected List<ImmutablePair<Integer,PeriodOfApplicability>> getRatingPeriodsWithinReportRange(List<ImmutablePair<Integer,PeriodOfApplicability>> pairList, Instant startDate, Instant endDate) {
		List<ImmutablePair<Integer,PeriodOfApplicability>> includePairs = new ArrayList<>();

		PeriodOfApplicability reportPeriod = new PeriodOfApplicability();
		reportPeriod.setStartTime(startDate);
		reportPeriod.setEndTime(endDate);

		//Sort Pair List by Period Start Time
		Collections.sort(pairList, new Comparator<ImmutablePair<Integer, PeriodOfApplicability>>() {
			@Override
			public int compare(final ImmutablePair<Integer, PeriodOfApplicability> o1, final ImmutablePair<Integer, PeriodOfApplicability> o2) {
				return o1.getValue().getStartTime().compareTo(o2.getValue().getStartTime());
			}
		});

		//Filter Pair List
		boolean foundFirstRating = false;
		for(int i = 0; i < pairList.size(); i++) {
			ImmutablePair<Integer,PeriodOfApplicability> pair = pairList.get(i);
			ImmutablePair<Integer,PeriodOfApplicability> prevPair = (i > 0) ? pairList.get(i-1) : null;
			PeriodOfApplicability nextPeriod = (i < pairList.size()-1) ? pairList.get(i+1).getValue() : null;

			PeriodOfApplicability effectiveRatingPeriod = createEffectiveRatingPeriod(pair.getValue(), nextPeriod);

			if(AqcuTimeUtils.doPeriodsOverlap(effectiveRatingPeriod, reportPeriod)) {
				//Include if Rating Period overlaps Report Period
				includePairs.add(pair);

				//If this is the first included Rating Period mark the first rating as having been found
				if(!foundFirstRating) {
					foundFirstRating = true;

					// If the previous Period (first Period prior to the Report Start date) is open-ended then include it
					if(prevPair != null && AqcuTimeUtils.isOpenEndedTime(prevPair.getValue().getEndTime())) {
						includePairs.add(prevPair);
					}
				}
			} else if(pair.getValue().getStartTime().compareTo(endDate) >= 0) {
				//If this Rating Period is after the Report End Date then this is the last period to process
				//If the previous Period was inlcuded and open-ended then also include this Period
				if(prevPair != null && includePairs.contains(prevPair) && AqcuTimeUtils.isOpenEndedTime(prevPair.getValue().getEndTime())) {
					includePairs.add(pair);
				}
				break;
			}
		}

		return includePairs;
	}

	protected PeriodOfApplicability createEffectiveRatingPeriod(PeriodOfApplicability ratingPeriod, PeriodOfApplicability nextRatingPeriod) {
		PeriodOfApplicability effectivePeriod = new PeriodOfApplicability();
		effectivePeriod.setStartTime(ratingPeriod.getStartTime());
		effectivePeriod.setRemarks(ratingPeriod.getRemarks());

		//Periods with open-ended End Times are "ended" by the Start Time of the next Period, if it exists
		if(AqcuTimeUtils.isOpenEndedTime(ratingPeriod.getEndTime()) && nextRatingPeriod != null) {
			effectivePeriod.setEndTime(nextRatingPeriod.getStartTime());
		} else {
			effectivePeriod.setEndTime(ratingPeriod.getEndTime());
		}

		return effectivePeriod;
	}
}
