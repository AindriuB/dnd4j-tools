package ie.dnd4j.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateAutoConfiguration {
    
    @Bean
    public RestTemplate restTemplate() {
	RestTemplate restTemplate = new RestTemplate();
	return restTemplate;
    }
    

}
