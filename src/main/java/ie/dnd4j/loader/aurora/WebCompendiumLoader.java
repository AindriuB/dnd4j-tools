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
import ie.dnd4j.items.Item;
import ie.dnd4j.items.Weapon;
import ie.dnd4j.loader.CompendiumLoader;
import ie.dnd4j.race.Race;
import ie.dnd4j.rules.RacialAbilityModifier;
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

	Map<String, String> attributes = new HashMap<String, String>();
	Map<String, String> stats = new HashMap<String, String>();

	String name = element.getAttribute("name");
	String type = element.getAttribute("type");
	String source = element.getAttribute("source");
	String id = element.getAttribute("id");
	
	
	Element description = DomUtils.getChildElementByTagName(element, "description");
	String descriptionText = getDescription(description);

	List<Element> setters = DomUtils.getChildElementsByTagName(element, "setters");
	for (Element e : setters) {
	    List<Element> sets = DomUtils.getChildElementsByTagName(e, "set");
	    sets.stream().forEach(node -> {
		attributes.put(node.getAttribute("name").toLowerCase(), node.getTextContent());
		String currency = node.getAttribute("currency").toLowerCase();
		if (currency != null && !currency.isEmpty()) {
		    attributes.put("currency", currency);
		}
		String attributeType = node.getAttribute("type").toLowerCase();
		if (attributeType != null && !attributeType.isEmpty()) {
		    attributes.put("type", attributeType);
		}
	    });
	}
	
	List<Element> rules = DomUtils.getChildElementsByTagName(element, "rules");
	for (Element e : rules) {
	    List<Element> stat = DomUtils.getChildElementsByTagName(e, "stat");
	    stat.stream().forEach(node -> {
		stats.put(node.getAttribute("name").toLowerCase(), node.getAttribute("value"));
	    });
	}

	switch (type.toLowerCase()) {
	case "item":
	    Item tools = new Item();
	    tools.setCost(Integer.valueOf(attributes.get("cost")));
	    tools.setName(name);
	    tools.setDescription(descriptionText);
	    tools.setProficiency(attributes.get("proficiency"));
	    tools.setSource(source);
	    tools.setWeight(attributes.get("weight"));
	    tools.setCurrency(attributes.get("currency"));
	    tools.setCategory(attributes.get("category"));
	    tools.setSlot(attributes.get("slot"));
	    tools.setStackable(Boolean.valueOf(attributes.get("stackable")));
	    tools.setType(type);
	    tools.tag(id);
	    this.compendium.getItems().put(id, tools);
	    break;
	case "armor":
	    Armour armour = new Armour();
	    armour.setCost(Integer.valueOf(attributes.get("cost")));
	    armour.setDescription(descriptionText);
	    armour.setName(name);
	    armour.setProficiency(attributes.get("proficiency"));
	    armour.setSource(source);
	    armour.setWeight(attributes.get("weight"));
	    armour.setCurrency(attributes.get("currency"));
	    armour.setCategory(attributes.get("category"));
	    armour.setSlot(attributes.get("slot"));
	    armour.setStackable(Boolean.valueOf(attributes.get("stackable")));
	    armour.setType(type);
	    armour.tag(id);
	    this.compendium.getItems().put(id, armour);
	    break;
	case "weapon":
	    Weapon weapon = new Weapon();
	    weapon.setDescription(descriptionText);
	    weapon.setCost(Integer.valueOf(attributes.get("cost")));
	    weapon.setName(name);
	    weapon.setProficiency(attributes.get("proficiency"));
	    weapon.setSource(source);
	    weapon.setWeight(attributes.get("weight"));
	    weapon.setCurrency(attributes.get("currency"));
	    weapon.setCategory(attributes.get("category"));
	    weapon.setType(type);
	    weapon.setDamageType(attributes.get("type"));
	    weapon.setSlot(attributes.get("slot"));
	    weapon.setStackable(Boolean.valueOf(attributes.get("stackable")));
	    weapon.setRange(attributes.get("range"));
	    weapon.setDamage(attributes.get("damage"));
	    weapon.setVersatile(attributes.get("versatile"));
	    weapon.tag(id);
	    this.compendium.getItems().put(id, weapon);
	    break;
	case "spell":
	    Spell spell = new Spell();
	    spell.setDescription(descriptionText);
	    spell.setName(name);
	    spell.setCastTime(attributes.get("time"));
	    spell.setClasses("");
	    spell.setConcentration(Boolean.valueOf(attributes.get("isConcentration")));
	    spell.setDuration(attributes.get("duration"));
	    spell.setLevel(Integer.parseInt(attributes.get("level")));
	    spell.setMaterialComponens(attributes.get("materialComponent"));
	    spell.setMaterialComponent(Boolean.valueOf(attributes.get("hasMaterialComponent")));
	    spell.setRange(attributes.get("range"));
	    spell.setRitual(Boolean.valueOf(attributes.get("isRitual")));
	    spell.setSchool(attributes.get("school"));
	    spell.setSomaticComponent(Boolean.valueOf(attributes.get("hasSomaticComponent")));
	    spell.setVerbalComponent(Boolean.valueOf("hasVerbalComponent"));
	    spell.tag(id);
	    this.compendium.getSpells().put(id, spell);
	    break;
	case "race":
	    Race race = new Race();
	    race.setDescription(descriptionText);
	    race.setName(name);
	    race.setSpeed(convertNumber(stats.get("innate speed")));
	    int strength = convertNumber(stats.get("strength"));
	    int dexterity = convertNumber(stats.get("dexterity"));
	    int constitution = convertNumber(stats.get("constitution"));
	    int intelligence = convertNumber(stats.get("intelligence"));
	    int wisdom = convertNumber(stats.get("wisdom"));
	    int charisma = convertNumber(stats.get("charisma"));
	    RacialAbilityModifier abilityModifier = new RacialAbilityModifier(strength, dexterity, constitution, intelligence, wisdom, charisma);
	    race.setRacialAbilityModifier(abilityModifier);
	    this.compendium.getRaces().put(id, race);
	    break;
	default:
	    break;
	
	}

    }
    
    private int convertNumber(String value) {
	if(value == null) {
	    return 0;
	} else {
	    try {
		return Integer.valueOf(value).intValue();
	    } catch (NumberFormatException e) {
		return 0;
	    }
	}
	
    }
    
    private String getDescription(Element description) {
	if(description == null) {
	    return "";
	}
	return description.getTextContent();
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
