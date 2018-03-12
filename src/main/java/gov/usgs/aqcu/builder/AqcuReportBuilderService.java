package gov.usgs.aqcu.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import gov.usgs.aqcu.retrieval.GradeLookupService;
import gov.usgs.aqcu.retrieval.QualifierLookupService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListService;

@Component
public abstract class AqcuReportBuilderService {	
	private static final Logger LOG = LoggerFactory.getLogger(AqcuReportBuilderService.class);

	//Common Builder Services
	protected AqcuDataGapListBuilderService dataGapListBuilderService;
	protected AqcuReportUrlBuilderService reportUrlBuilderService;

	//Common Lookup Services
	protected GradeLookupService gradeLookupService;
	protected QualifierLookupService qualifierLookupService;

	//Common Retrieval Services
	protected LocationDescriptionListService locationDescriptionListService;
	protected TimeSeriesDescriptionListService timeSeriesDescriptionListService;

	public AqcuReportBuilderService(
			AqcuDataGapListBuilderService dataGapListBuilderService,
			AqcuReportUrlBuilderService reportUrlBuilderService,
			GradeLookupService gradeLookupService, 
			QualifierLookupService qualifierLookupService,
			LocationDescriptionListService locationDescriptionListService,
			TimeSeriesDescriptionListService timeSeriesDescriptionListService) {
		this.dataGapListBuilderService = dataGapListBuilderService;
		this.reportUrlBuilderService = reportUrlBuilderService;
		this.qualifierLookupService = qualifierLookupService;
		this.gradeLookupService = gradeLookupService;
		this.locationDescriptionListService = locationDescriptionListService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
	}
}
	
