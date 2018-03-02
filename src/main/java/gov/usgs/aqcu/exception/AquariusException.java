package gov.usgs.aqcu.exception;

import java.util.List;

import net.servicestack.client.WebServiceException;
import net.servicestack.client.ResponseError;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class AquariusException extends RuntimeException {
	
	public AquariusException(String message) {
		super(message);
	}
}
