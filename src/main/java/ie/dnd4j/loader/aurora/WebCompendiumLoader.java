package ie.dnd4j.loader.aurora;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

	if (compendiumConfiguration != null) {
	    Map<String, CompendiumConfiguration> configurations = compendiumConfiguration.getCompendiums();

	    configurations.entrySet().stream().forEach(element -> {
		LOGGER.info("Loading compendium {}", element.getKey());

		CompendiumConfiguration config = element.getValue();

		config.getUrls().entrySet().stream().forEach(subElement -> {
		    LOGGER.info(" - Loading section {} : {}", subElement.getKey(), subElement.getValue());

		    ResponseEntity<String> xml = restTemplate.getForEntity(subElement.getValue(), String.class);
		    
		    Document document = parseXML(xml.getBody());
		    
		    if(document != null) {
			LOGGER.info("Building compendium");
			
			NodeList list =  document.getElementsByTagName("element");
			LOGGER.info("Elements: {}", list.getLength());
		    }
		});

	    });
	}

    }
    
    private Document parseXML(String xml) {
	return parseXML(xml, "UTF-8");
    }

    private Document parseXML(String xml, String encoding) {
	if(encoding == null) {
	    encoding = "UTF-8";
	}
	    
	LOGGER.info("Parsing XML - length {}", xml.length());
	Document doc = null;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setValidating(false);
	factory.setNamespaceAware(false);
	factory.setIgnoringElementContentWhitespace(true);
	DocumentBuilder builder;
	try {
	    builder = factory.newDocumentBuilder();
	    BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(xml.getBytes("UTF-8")));
	    doc = builder.parse(is);
	} catch (ParserConfigurationException | SAXException | IOException e) {
	    LOGGER.error("XML: {}", xml);
	    LOGGER.error("Failed to read document", e);
	}

	return doc;
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
