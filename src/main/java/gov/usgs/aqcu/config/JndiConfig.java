package gov.usgs.aqcu.config;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JndiConfig {

	private final Context ctx;

	public JndiConfig() throws NamingException {
		ctx = new InitialContext();
	}

	@Bean
	public String aquariusUrl() throws Exception {
		return ctx.lookup("java:comp/env/aquarius.service.endpoint").toString().replaceAll("/AQUARIUS/", "");
	}

	@Bean
	public String aquariusUser() throws Exception {
		return ctx.lookup("java:comp/env/aquarius.service.user").toString();
	}

	@Bean
	public String aquariusPassword() throws Exception {
		return ctx.lookup("java:comp/env/aquarius.service.password").toString();
	}

}
