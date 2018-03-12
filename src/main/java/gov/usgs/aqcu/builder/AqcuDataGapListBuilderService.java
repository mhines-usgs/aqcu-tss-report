package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;

import gov.usgs.aqcu.model.AqcuDataGap;
import gov.usgs.aqcu.model.AqcuDataGapExtent;

@Component
public class AqcuDataGapListBuilderService {	
	private static final Logger LOG = LoggerFactory.getLogger(AqcuDataGapListBuilderService.class);

	public List<AqcuDataGap> buildGapList(List<TimeSeriesPoint> timeSeriesPoints, Instant startDate, Instant endDate) {
		List<AqcuDataGap> gapList = new ArrayList<>();

		for(int i = 0; i < timeSeriesPoints.size(); i++) {
			TimeSeriesPoint point = timeSeriesPoints.get(i);
			Instant preTime = (i > 0) ?  timeSeriesPoints.get(i-1).getTimestamp().getDateTimeOffset() : null;
			Instant postTime = (i < (timeSeriesPoints.size() -1)) ? timeSeriesPoints.get(i+1).getTimestamp().getDateTimeOffset() : null;

			if(point.getValue().getNumeric() == null) {
				//Gap Marker Found
				AqcuDataGap gap = new AqcuDataGap();
				gap.setStartTime(preTime);
				gap.setEndTime(postTime);

				//Determine where this gap is
				if(preTime != null && postTime != null) {
					gap.setGapExtent(AqcuDataGapExtent.CONTAINED);
				} else if(preTime == null) {
					gap.setGapExtent(AqcuDataGapExtent.OVER_START);
				} else if(postTime == null) {
					gap.setGapExtent(AqcuDataGapExtent.OVER_END);
				} else {
					gap.setGapExtent(AqcuDataGapExtent.OVER_ALL);
				}

				gapList.add(gap);
			}
		}

		return gapList;
	}
}
	
