package gov.usgs.aqcu.model;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionType;

public enum AqcuExtendedCorrectionType  {	
	Freehand;
	
	public static AqcuExtendedCorrectionType fromCorrection(Correction correction) {
		if(correction.getType().equals(CorrectionType.CopyPaste) && correction.getComment().toLowerCase().contains(Freehand.toString().toLowerCase())) {
			return Freehand;
		}
		
		return null;
	}
}
