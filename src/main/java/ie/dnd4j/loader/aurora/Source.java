package ie.dnd4j.loader.aurora;

import java.util.HashMap;
import java.util.Map;

public class Source {
    
    private String name;
    
    private String url;
    
    private Map<String, Book> texts;
    
    public Source() {
	texts = new HashMap<String, Book>();
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

    public Map<String, Book> getTexts() {
        return texts;
    }

    public void setTexts(Map<String, Book> texts) {
        this.texts = texts;
    }
    
    
}
