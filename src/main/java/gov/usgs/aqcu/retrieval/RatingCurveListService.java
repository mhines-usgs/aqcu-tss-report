package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.Instant;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.aqcu.util.AqcuTimeUtils;

@Repository
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
		Map<Integer, List<PeriodOfApplicability>> fullPeriodMap = new HashMap<>();
		Map<Integer, List<PeriodOfApplicability>> filteredPeriodMap;

		//Get Full List of Curve Periods
		for(int i = 0; i < responseCurves.size(); i++) {
			fullPeriodMap.put(i, responseCurves.get(i).getPeriodsOfApplicability());
		}

		//Filter Full Period List
		filteredPeriodMap = getRatingCurvePeriodsWithinReportRange(fullPeriodMap, startDate, endDate);

		//Build Filtered Curve List from Filtered Period Map
		for(Integer curveIndex : filteredPeriodMap.keySet()) {
			//Clone the curve
			RatingCurve curve = cloneCurve(responseCurves.get(curveIndex));
			curve.setPeriodsOfApplicability(new ArrayList<>(filteredPeriodMap.get(curveIndex)));
			filteredCurves.add(curve);
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

	protected Map<Integer, List<PeriodOfApplicability>> getRatingCurvePeriodsWithinReportRange(Map<Integer, List<PeriodOfApplicability>> fullPeriodMap, Instant startDate, Instant endDate) {
		Map<Integer, List<PeriodOfApplicability>> includePeriodMap = new HashMap<>();
		List<ImmutablePair<Integer, PeriodOfApplicability>> includePairs;
		List<ImmutablePair<Integer, PeriodOfApplicability>> pairList = new ArrayList<>();

		//Convert Period Map to Pair List
		for(Map.Entry<Integer, List<PeriodOfApplicability>> entry : fullPeriodMap.entrySet()) {
			for(PeriodOfApplicability period : entry.getValue()) {
				pairList.add(new ImmutablePair<Integer,PeriodOfApplicability>(entry.getKey(), period));
			}
		}

		includePairs = getRatingPeriodsWithinReportRange(pairList, startDate, endDate);

		//Convert Included Pair List to Map
		for(ImmutablePair<Integer,PeriodOfApplicability> pair : includePairs) {
			if(includePeriodMap.get(pair.getKey()) == null) {
				includePeriodMap.put(pair.getKey(), new ArrayList<>(Arrays.asList(pair.getValue())));
			} else {
				List<PeriodOfApplicability> mapList = includePeriodMap.get(pair.getKey());
				mapList.add(pair.getValue());
				includePeriodMap.put(pair.getKey(), mapList);
			}
		}

		return includePeriodMap;
	}

	protected List<ImmutablePair<Integer, PeriodOfApplicability>> getRatingPeriodsWithinReportRange(List<ImmutablePair<Integer, PeriodOfApplicability>> pairList, Instant startDate, Instant endDate) {
		List<ImmutablePair<Integer, PeriodOfApplicability>> includePairs = new ArrayList<>();

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

	protected RatingCurve cloneCurve(RatingCurve origCurve) {
		Gson cloneGson = new GsonBuilder().create();
		return cloneGson.fromJson(cloneGson.toJson(origCurve), RatingCurve.class);
	}
}
