package gov.usgs.aqcu.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="javaToR")
public interface JavaToRClient {

	@RequestMapping(method=RequestMethod.POST, value="/aqcu-java-to-r/report/{reportType}")
	byte[] render(@RequestParam("requestingUser") String requestingUser, @PathVariable("reportType") String reportType, @RequestBody String reportJson);

}
