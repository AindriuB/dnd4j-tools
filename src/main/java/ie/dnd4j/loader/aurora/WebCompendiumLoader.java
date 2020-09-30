package ie.dnd4j.loader.aurora;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ie.dnd4j.Compendium;
import ie.dnd4j.configuration.CompendiumConfiguration;
import ie.dnd4j.configuration.CompendiumSourcesConfiguration;
import ie.dnd4j.items.Armour;
import ie.dnd4j.items.Tools;
import ie.dnd4j.items.Weapon;
import ie.dnd4j.loader.CompendiumLoader;
import ie.dnd4j.spells.Spell;

public class WebCompendiumLoader implements CompendiumLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebCompendiumLoader.class);

    private CompendiumSourcesConfiguration compendiumConfiguration;

    private RestTemplate restTemplate;

    private Compendium compendium;

    public WebCompendiumLoader() {
	compendium = new Compendium();
	compendium.setSource("Aurora");
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
	if (documents.isEmpty()) {
	    return;
	}

	documents.entrySet().stream().forEach(entrySet -> {
	    LOGGER.info("Building compendium from document {}", entrySet.getKey());
	    Document document = entrySet.getValue();
	    NodeList list = document.getElementsByTagName("element");

	    for (int i = 0; i < list.getLength(); i++) {
		processNode((Element) list.item(i));
	    }
	    LOGGER.info("Elements: {}", list.getLength());
	});
    }

    private void processNode(Element element) {

	Map<String, String> settersMap = new HashMap<String, String>();

	String name = element.getAttribute("name");
	String type = element.getAttribute("type");
	String source = element.getAttribute("source");
	String id = element.getAttribute("id");

	List<Element> setters = DomUtils.getChildElementsByTagName(element, "setters");
	for (Element e : setters) {
	    List<Element> sets = DomUtils.getChildElementsByTagName(e, "set");
	    sets.stream().forEach(node -> {
		settersMap.put(node.getAttribute("name").toLowerCase(), node.getTextContent());
		String currency = node.getAttribute("currency").toLowerCase();
		if (currency != null && !currency.isEmpty()) {
		    settersMap.put("currency", currency);
		}
	    });
	}

	switch (type.toLowerCase()) {
	case "item":
	    Tools tools = new Tools();
	    tools.setCost(Integer.valueOf(settersMap.get("cost")));
	    tools.setName(name);
	    tools.setProficiency(settersMap.get("proficiency"));
	    tools.setSource(source);
	    tools.setWeight(settersMap.get("weight"));
	    tools.setCurrency(settersMap.get("currency"));
	    tools.setCategory(settersMap.get("category"));
	    tools.setType(type);
	    tools.tag(id);
	    this.compendium.getItems().put(id, tools);
	    break;
	case "armor":
	    Armour armour = new Armour();
	    armour.setCost(Integer.valueOf(settersMap.get("cost")));
	    armour.setName(name);
	    armour.setProficiency(settersMap.get("proficiency"));
	    armour.setSource(source);
	    armour.setWeight(settersMap.get("weight"));
	    armour.setCurrency(settersMap.get("currency"));
	    armour.setCategory(settersMap.get("category"));
	    armour.setType(type);
	    armour.tag(id);
	    this.compendium.getItems().put(id, armour);
	    break;
	case "weapon":
	    Weapon weapon = new Weapon();
	    weapon.setCost(Integer.valueOf(settersMap.get("cost")));
	    weapon.setName(name);
	    weapon.setProficiency(settersMap.get("proficiency"));
	    weapon.setSource(source);
	    weapon.setWeight(settersMap.get("weight"));
	    weapon.setCurrency(settersMap.get("currency"));
	    weapon.setCategory(settersMap.get("category"));
	    weapon.setType(type);
	    weapon.tag(id);
	    this.compendium.getItems().put(id, weapon);
	    break;
	case "spell":
	    Spell spell = new Spell();
	    spell.setName(name);
	    spell.setCastTime(settersMap.get("time"));
	    spell.setClasses("");
	    spell.setConcentration(Boolean.valueOf(settersMap.get("isConcentration")));
	    spell.setDuration(settersMap.get("duration"));
	    spell.setLevel(Integer.parseInt(settersMap.get("level")));
	    spell.setMaterialComponens(settersMap.get("materialComponent"));
	    spell.setMaterialComponent(Boolean.valueOf(settersMap.get("hasMaterialComponent")));
	    spell.setRange(settersMap.get("range"));
	    spell.setRitual(Boolean.valueOf(settersMap.get("isRitual")));
	    spell.setSchool(settersMap.get("school"));
	    spell.setSomaticComponent(Boolean.valueOf(settersMap.get("hasSomaticComponent")));
	    spell.setVerbalComponent(Boolean.valueOf("hasVerbalComponent"));
	    spell.tag(id);
	    this.compendium.getSpells().put(id, spell);

	default:
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
