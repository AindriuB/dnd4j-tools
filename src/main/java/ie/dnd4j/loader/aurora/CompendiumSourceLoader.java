package ie.dnd4j.loader.aurora;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ie.dnd4j.configuration.CompendiumConfiguration;
import ie.dnd4j.configuration.CompendiumSourcesConfiguration;

public class CompendiumSourceLoader extends XMLReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompendiumSourceLoader.class);

    private CompendiumSourcesConfiguration compendiumConfiguration;

    private RestTemplate restTemplate;
    
    public Map<String, Document> loadSources(){
	
	Map<String, Document> documentMap = new HashMap<String, Document>();

	if (compendiumConfiguration != null) {
	    Map<String, CompendiumConfiguration> configurations = compendiumConfiguration.getCompendiums();

	    configurations.entrySet().stream().forEach(element -> {
		LOGGER.info("Loading compendium {}", element.getKey());
		ResponseEntity<String> xml = restTemplate.getForEntity(element.getValue().getSourceUrl(), String.class);

		Document document = this.parseXML(xml.getBody());
		NodeList list = document.getElementsByTagName("file");
		for(int i = 0; i < list.getLength(); i++) {
		    Node node = list.item(i);
		    String url = node.getAttributes().getNamedItem("url").getNodeValue();
		    String name = node.getAttributes().getNamedItem("name").getNodeValue();
		    LOGGER.info("{}: {}",name, url);
		}

	    });
	}
	return documentMap;
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

    public static Logger getLogger() {
        return LOGGER;
    }
    
    
    
}
