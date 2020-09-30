package ie.dnd4j.loader.aurora;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ie.dnd4j.configuration.CompendiumConfiguration;
import ie.dnd4j.configuration.CompendiumSourcesConfiguration;
import ie.dnd4j.loader.CompendiumLoader;

public class WebCompendiumLoader implements CompendiumLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCompendiumLoader.class);
    
    private CompendiumSourcesConfiguration compendiumConfiguration;
    
    private RestTemplate restTemplate;
    
    
    /***
     * 
     */ 
    @Override
    public void loadCompendiums() {
	
	if(compendiumConfiguration != null) {
	    Map<String, CompendiumConfiguration> configurations = compendiumConfiguration.getCompendiums();
	    
	    configurations.entrySet().stream().forEach( element ->{
		LOGGER.info("Loading compendium {}", element.getKey());
		
		CompendiumConfiguration config = element.getValue();
		
		config.getUrls().entrySet().stream().forEach(subElement -> {
			LOGGER.info(" - Loading section {} : {}", subElement.getKey(), subElement.getValue());
			
			
			ResponseEntity<String> xml = restTemplate.getForEntity(subElement.getValue(),String.class);
			LOGGER.info(xml.getBody());
		});
		
	    });
	}
	
    }
    


    public CompendiumSourcesConfiguration getCompendiumConfiguration() {
        return compendiumConfiguration;
    }



    public void setCompendiumConfiguration(CompendiumSourcesConfiguration compendiumConfiguration) {
        this.compendiumConfiguration = compendiumConfiguration;
    }



    public RestTemplate getRestTemplate() {
	return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
	this.restTemplate = restTemplate;
    }


}
