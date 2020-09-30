package ie.dnd4j.configuration;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class CompendiumSourcesConfiguration {
    
    Map<String, CompendiumConfiguration> compendiums;

    public Map<String, CompendiumConfiguration> getCompendiums() {
        return compendiums;
    }

    public void setCompendiums(Map<String, CompendiumConfiguration> compendiums) {
        this.compendiums = compendiums;
    }


}
