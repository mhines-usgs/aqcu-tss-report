package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.math.BigDecimal;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShiftPoint;

public class TimeSeriesSummaryRatingShift {	
	private String curveNumber;
	private String remarks;
	private Instant applicableStartDateTime;
	private Instant applicableEndDateTime;
	private List<BigDecimal> shiftPoints;
	private List<BigDecimal> stagePoints;
	
	public TimeSeriesSummaryRatingShift(RatingShift sourceShift, String curveNumber) {
		shiftPoints = new ArrayList<>();
		stagePoints = new ArrayList<>();
		this.curveNumber = curveNumber;
		applicableStartDateTime = sourceShift.getPeriodOfApplicability().getStartTime();
		applicableEndDateTime = sourceShift.getPeriodOfApplicability().getEndTime();
		remarks =  sourceShift.getPeriodOfApplicability().getRemarks();
		
		for(RatingShiftPoint point : sourceShift.getShiftPoints()) {
			shiftPoints.add(BigDecimal.valueOf(point.getShift()));
			stagePoints.add(BigDecimal.valueOf(point.getInputValue()));
		}
	}
	
	public String getCurveNumber() {
		return curveNumber;
	}
	
	public String getRemarks() {
		return remarks;
	}
	
	public Instant getApplicableStartDateTime() {
		return applicableStartDateTime;
	}
	
	public Instant getApplicableEndDateTime() {
		return applicableEndDateTime;
	}
	
	public List<BigDecimal> getShiftPoints() {
		return shiftPoints;
	}
	
	public List<BigDecimal> getStagePoints() {
		return stagePoints;
	}
	
	public void setCurveNumber(String value) {
		curveNumber =  value;
	}
	
	public void setRemarks(String value) {
		remarks =  value;
	}
	
	public void setApplicableStartDateTime(Instant value) {
		applicableStartDateTime =  value;
	}
	
	public void setApplicableEndDateTime(Instant value) {
		applicableEndDateTime =  value;
	}
	
	public void setShiftPoints(List<BigDecimal> value) {
		shiftPoints =  value;
	}
	
	public void setStagePoints(List<BigDecimal> value) {
		stagePoints =  value;
	}
}
	
