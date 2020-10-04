package ie.dnd4j.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import ie.dnd4j.loader.aurora.CompendiumSourceLoader;
import ie.dnd4j.loader.aurora.CompendiumBuilder;

@EnableConfigurationProperties
@Configuration
@ComponentScan(basePackages = {"ie.dnd4j", "ie.dnd4j.configuration"})
public class CompendiumAutoConfiguration {
    
    @Autowired
    private CompendiumSourcesConfiguration compendiumConfiguration;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Bean
    public CompendiumBuilder compendiumLoader() {
	CompendiumBuilder loader = new CompendiumBuilder();
	return loader;
    }
    
    @Bean
    public CompendiumSourceLoader sourceLoader() {
	CompendiumSourceLoader loader = new CompendiumSourceLoader();
	loader.setRestTemplate(restTemplate);
	loader.setCompendiumConfiguration(compendiumConfiguration);
	return loader;
    }

}
