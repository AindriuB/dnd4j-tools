package ie.dnd4j.loader.aurora;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ie.dnd4j.Compendium;
import ie.dnd4j.abilities.Ability;
import ie.dnd4j.character.Alignment;
import ie.dnd4j.character.background.Background;
import ie.dnd4j.feats.Feat;
import ie.dnd4j.items.Armour;
import ie.dnd4j.items.ArmourType;
import ie.dnd4j.items.Item;
import ie.dnd4j.items.Weapon;
import ie.dnd4j.race.Race;
import ie.dnd4j.religion.Deity;
import ie.dnd4j.rules.dependencies.AbilityDependency;
import ie.dnd4j.rules.stats.ArmourClassRule;
import ie.dnd4j.rules.stats.RacialAbilityModifierRule;
import ie.dnd4j.spells.Spell;

public class CompendiumBuilder extends XMLReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompendiumBuilder.class);

    private Compendium compendium;

    public CompendiumBuilder() {
	compendium = new Compendium();
	compendium.setSource("Aurora");
    }

    /**
     * Build the compendium from our XML documents
     * 
     * @param documents
     */
    public void buildCompendium(Map<String, Source> sources) {
	if (sources.isEmpty()) {
	    return;
	}

	sources.entrySet().forEach(entry -> {

	    LOGGER.info("Building source {}", entry.getKey());

	    Source source = entry.getValue();

	    source.getTexts().entrySet().stream().forEach(sourceEntrySet -> {
		LOGGER.info("Building compendium from document {}", sourceEntrySet.getKey());
		final Book book = sourceEntrySet.getValue();

		compendium.getSources().add(sourceEntrySet.getKey());
		book.getTexts().entrySet().stream().forEach(chapterEntrySet -> {

		    LOGGER.info("Building chapter {} from book {}", chapterEntrySet.getKey(),  sourceEntrySet.getKey());
		    Document document = chapterEntrySet.getValue();
		    NodeList list = document.getElementsByTagName("element");

		    for (int i = 0; i < list.getLength(); i++) {
			try {
			    processNode((Element) list.item(i));
			} catch (Exception e) {  
			    LOGGER.error("Error parsing Node", e);   
			}
		    }

		    LOGGER.info("Elements: {}", list.getLength());
		});
	    });
	});

    }

    /***
     * Process an element node
     * 
     * @param element
     */
    private void processNode(Element element) {

	Map<String, String> attributes = new HashMap<String, String>();
	Map<String, String> stats = new HashMap<String, String>();
	Map<String, List<String>> select = new HashMap<String, List<String>>();

	String name = element.getAttribute("name");
	String type = element.getAttribute("type");
	String source = element.getAttribute("source");
	String id = UUID.randomUUID().toString();
	String tag = element.getAttribute("id");

	Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
	String description = getDescription(descriptionElement);

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

	    List<Element> selects = DomUtils.getChildElementsByTagName(e, "select");
	    selects.stream().forEach(node -> {
		String selectName = node.getAttribute("name");
		select.put(selectName, new ArrayList<String>());
		List<Element> options = DomUtils.getChildElementsByTagName(node, "item");
		options.stream().forEach(childNode -> {
		    select.get(selectName).add(DomUtils.getTextValue(childNode));
		});
	    });

	}

	switch (type.toLowerCase()) {
	case "item":
	    Item tools = new Item();
	    tools.setCost(convertNumber(attributes.get("cost")));
	    tools.setName(name);
	    tools.setDescription(description);
	    tools.setProficiency(attributes.get("proficiency"));
	    tools.setSource(source);
	    tools.setWeight(attributes.get("weight"));
	    tools.setCurrency(attributes.get("currency"));
	    tools.setCategory(attributes.get("category"));
	    tools.setSlot(attributes.get("slot"));
	    tools.setStackable(Boolean.valueOf(attributes.get("stackable")));
	    tools.setType(type);
	    tools.tag(tag);
	    this.compendium.getItems().put(id, tools);
	    break;
	case "armor":
	    Armour armour = new Armour();
	    armour.setCost(convertNumber(attributes.get("cost")));
	    armour.setDescription(description);
	    armour.setName(name);
	    armour.setProficiency(attributes.get("proficiency"));
	    armour.setSource(source);
	    armour.setWeight(attributes.get("weight"));
	    armour.setCurrency(attributes.get("currency"));
	    armour.setCategory(attributes.get("category"));
	    armour.setSlot(attributes.get("slot"));
	    armour.setStackable(Boolean.valueOf(attributes.get("stackable")));
	    armour.setType(type);
	    armour.setArmour(attributes.get("armor"));
	    armour.tag(tag);

	    int ac = convertNumber(stats.get("ac:armored:armor"));

	    ArmourClassRule rule = new ArmourClassRule(ac, ArmourType.forString(armour.getArmour()));
	    String check = attributes.get("strength");
	    if (check != null) {
		int strengthCheck = convertNumber(attributes.get("strength"));
		rule.addDependency(new AbilityDependency(Ability.STRENGTH, strengthCheck));
	    }

	    armour.addRule(rule);
	    this.compendium.getItems().put(id, armour);
	    break;
	case "weapon":
	    Weapon weapon = new Weapon();
	    weapon.setDescription(description);
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
	    weapon.tag(tag);
	    this.compendium.getItems().put(id, weapon);
	    break;
	case "spell":
	    Spell spell = new Spell();
	    spell.setDescription(description);
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
	    spell.tag(tag);
	    this.compendium.getSpells().put(id, spell);
	    break;
	case "race":
	    Race race = new Race();
	    race.setDescription(description);
	    race.setName(name);
	    race.setSpeed(convertNumber(stats.get("innate speed")));
	    int strength = convertNumber(stats.get("strength"));
	    int dexterity = convertNumber(stats.get("dexterity"));
	    int constitution = convertNumber(stats.get("constitution"));
	    int intelligence = convertNumber(stats.get("intelligence"));
	    int wisdom = convertNumber(stats.get("wisdom"));
	    int charisma = convertNumber(stats.get("charisma"));
	    RacialAbilityModifierRule abilityModifier = new RacialAbilityModifierRule(strength, dexterity, constitution,
		    intelligence, wisdom, charisma);
	    race.setRacialAbilityModifier(abilityModifier);
	    race.tag(tag);
	    this.compendium.getRaces().put(id, race);
	    break;
	case "deity":
	    Deity deity = new Deity();
	    deity.setName(name);
	    deity.setSource(source);
	    deity.setType(type);
	    deity.setDescription(description);
	    deity.setAlignment(Alignment.forTag(attributes.get("alignment")));
	    deity.setDomains(listForString(attributes.get("domains")));
	    deity.setGender(attributes.get("gender"));
	    deity.setSymbol(attributes.get("symbol"));
	    deity.tag(tag);
	    this.compendium.getDeities().put(id, deity);
	    break;
	case "feat":
	    Feat feat = new Feat();
	    feat.setName(name);
	    feat.setDescription(description);
	    feat.setType(type);
	    feat.tag(tag);
	    this.compendium.getFeats().put(id, feat);
	    break;
	case "feat feature":
	    Feat feature = new Feat();
	    feature.setName(name);
	    feature.setDescription(description);
	    feature.setType(type);
	    this.compendium.getFeats().put(id, feature);
	    break;
	case "background":
	    Background background = new Background();
	    background.setName(name);
	    background.setDescription(description);
	    background.setPersonality(select.get("Personality Trait"));
	    background.setIdeal(select.get("Ideal"));
	    background.setBond(select.get("Bond"));
	    background.setFlaw(select.get("Flaw"));
	    background.tag(tag);
	    this.compendium.getBackgrounds().put(id, background);
	    break;
	default:
	    break;

	}

    }

    private List<String> listForString(String list) {
	return Arrays.asList(list.split(","));
    }

    private int convertNumber(String value) {
	if (value == null) {
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
	if (description == null) {
	    return "";
	}
	return description.getTextContent();
    }

    public Compendium getCompendium() {
	return compendium;
    }

}
