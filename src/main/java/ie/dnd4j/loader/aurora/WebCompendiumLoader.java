package ie.dnd4j.loader.aurora;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.xml.DomUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ie.dnd4j.Compendium;
import ie.dnd4j.configuration.CompendiumConfiguration;
import ie.dnd4j.configuration.CompendiumSourcesConfiguration;
import ie.dnd4j.items.Tools;
import ie.dnd4j.loader.CompendiumLoader;

public class WebCompendiumLoader implements CompendiumLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebCompendiumLoader.class);

    private CompendiumSourcesConfiguration compendiumConfiguration;

    private RestTemplate restTemplate;

    private Compendium compendium;

    public WebCompendiumLoader() {
	compendium = new Compendium();
    }

    /***
     * 
     */
    @Override
    public void loadCompendiums() {
	Map<String, Document> documentMap = new HashMap<String, Document>();

	if (compendiumConfiguration != null) {
	    Map<String, CompendiumConfiguration> configurations = compendiumConfiguration.getCompendiums();

	    configurations.entrySet().stream().forEach(element -> {
		LOGGER.info("Loading compendium {}", element.getKey());

		CompendiumConfiguration config = element.getValue();

		config.getUrls().entrySet().stream().forEach(subElement -> {
		    LOGGER.info(" - Loading section {} : {}", subElement.getKey(), subElement.getValue());

		    ResponseEntity<String> xml = restTemplate.getForEntity(subElement.getValue(), String.class);

		    Document document = parseXML(xml.getBody());

		    if (document != null) {

			documentMap.put(subElement.getKey(), document);

		    }
		});

	    });
	    buildCompendium(documentMap);
	}
	

    }

    private Document parseXML(String xml) {
	return parseXML(xml, "UTF-8");
    }

    private Document parseXML(String xml, String encoding) {
	if (encoding == null) {
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

    private void buildCompendium(Map<String, Document> documents) {
	if(documents.isEmpty()) {
	    return;
	}
	
	documents.entrySet().stream().forEach(entrySet -> {
	    LOGGER.info("Building compendium from document {}", entrySet.getKey() );
	    Document document = entrySet.getValue();
	    NodeList list =  document.getElementsByTagName("element");
	    
	    for(int i = 0; i < list.getLength(); i++) {
		addNode((Element) list.item(i));
	    }
	    LOGGER.info("Elements: {}", list.getLength());	 
	});
    }

    
    private void addNode(Element  element) {
	String type = element.getAttributes().getNamedItem("type").getNodeValue();
	
	LOGGER.debug("Processing node of type: {}", type);
	
	switch(type) {
	case "Item":
	    processItemNode(element);
	    break;
	default:
	    break;
	}
	
    }
    
    
    private void processItemNode(Element  element) {

	Map<String, String> settersMap = new HashMap<String, String>();
	
	String name = element.getAttribute("name");
	String type = element.getAttribute("type");	
	String source = element.getAttribute("source");
	String id = element.getAttribute("id");
	
	
	
	List<Element> setters = DomUtils.getChildElementsByTagName(element, "setters");
	for(Element e : setters) {
	   List<Element> sets = DomUtils.getChildElementsByTagName(e, "set");
	   sets.stream().forEach( node -> {
	       settersMap.put(node.getAttribute("name").toLowerCase() , node.getTextContent();
	   });		
	}
	
	if(settersMap.containsKey("category")) {
	    
	    String category = settersMap.get("category");
	    switch(category.toLowerCase()) {
	    case "tools":
		Tools tools = new Tools();
		tools.setCost(Integer.valueOf(settersMap.get("cost")));
		tools.setName(name);
		tools.setProficiency(settersMap.get("proficiency"));
		tools.setSource(source);
		tools.setType(type);
		tools.tag(id);
		this.compendium.getItems().put(id, tools);
		break;
		default:
	    
	    }
	    
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

    @Override
    public Compendium getCompendium() {
	return compendium;
    }

}
