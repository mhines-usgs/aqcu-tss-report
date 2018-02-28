package gov.usgs.aqcu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.lang.reflect.Field;

import com.aquaticinformatics.aquarius.sdk.timeseries.serializers.InstantSerializer;
import com.aquaticinformatics.aquarius.sdk.timeseries.serializers.InstantDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingStrategy;

import java.time.Instant;

@Configuration
public class GsonConfig {

	@Bean
	public GsonBuilder gsonBuilder() {
		return new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Instant.class, new InstantSerializer())
			.registerTypeAdapter(Instant.class, new InstantDeserializer())
			.setFieldNamingStrategy(LOWER_CASE_CAMEL_CASE);
	}
	
	private FieldNamingStrategy LOWER_CASE_CAMEL_CASE = new FieldNamingStrategy() {  
		@Override
		public String translateName(Field f) {
			return f.getName().substring(0, 1).toLowerCase() + f.getName().substring(1);
		}
	};
}
