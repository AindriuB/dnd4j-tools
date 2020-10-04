package ie.dnd4j.loader.aurora;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

public class Book {
    
    private String name;
    
    private String url;
    
    private Map<String, Document> texts;
    
    public Book() {
	texts = new HashMap<String, Document>();
   }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Document> getTexts() {
        return texts;
    }

    public void setTexts(Map<String, Document> texts) {
        this.texts = texts;
    }

}
