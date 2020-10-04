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

    public Map<String, Source> loadSources() {

	Map<String, Source> documentMap = new HashMap<String, Source>();

	if (compendiumConfiguration != null) {
	    Map<String, CompendiumConfiguration> configurations = compendiumConfiguration.getCompendiums();

	    configurations.entrySet().stream().forEach(element -> {
		LOGGER.info("Loading compendium {}", element.getKey());
		Source source = new Source();
		source.setName(element.getKey().toString());
		source.setUrl(element.getValue().toString());

		ResponseEntity<String> sourceResponse = restTemplate.getForEntity(element.getValue().getSourceUrl(),
			String.class);

		Document document = this.parseXML(sourceResponse.getBody());
		NodeList bookUrls = document.getElementsByTagName("file");
		for (int i = 0; i < bookUrls.getLength(); i++) {
		    Node bookUrl = bookUrls.item(i);
		    String url = bookUrl.getAttributes().getNamedItem("url").getNodeValue();
		    String name = bookUrl.getAttributes().getNamedItem("name").getNodeValue();

		    if (!url.equals(source.getUrl())) {
			LOGGER.info("Book: {} - {}", name, url);

			ResponseEntity<String> bookResponse = restTemplate.getForEntity(url, String.class);

			Document bookSource = this.parseXML(bookResponse.getBody());
			NodeList chapterUrls = bookSource.getElementsByTagName("file");
			for (int j = 0; j < chapterUrls.getLength(); j++) {

			    Node chapterNode = chapterUrls.item(j);
			    String chapterUrl = chapterNode.getAttributes().getNamedItem("url").getNodeValue();
			    String chapterName = chapterNode.getAttributes().getNamedItem("name").getNodeValue();
			    if (!url.equals(chapterUrl)) {
				Book book = new Book();
				book.setName(chapterName);
				book.setUrl(chapterUrl);

				LOGGER.info("Conetnts - {}: {}", chapterName, chapterUrl);
				ResponseEntity<String> response = restTemplate.getForEntity(chapterUrl, String.class);
				Document contents = this.parseXML(response.getBody());

				book.getTexts().put(chapterName, contents);
				source.getTexts().put(name, book);
			    } else {
				LOGGER.info("Discarding loopback url");
			    }
			}
		    } else {
			LOGGER.info("Discarding loopback url");
		    }
		}

		documentMap.put(source.getName(), source);
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
