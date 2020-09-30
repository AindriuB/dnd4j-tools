package ie.dnd4j;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ie.dnd4j.configuration.CompendiumAutoConfiguration;
import ie.dnd4j.configuration.CompendiumSourcesConfiguration;
import ie.dnd4j.loader.aurora.WebCompendiumLoader;

@SpringBootTest(classes = {CompendiumAutoConfiguration.class})
public class WebCompendiumLoaderTest {

    @Autowired
    private WebCompendiumLoader loader;
    
    @Autowired
    private CompendiumSourcesConfiguration sources;
    
    @Test
    public void testConfiguration() {
	
	CompendiumSourcesConfiguration  source = loader.getCompendiumConfiguration();
	assertNotNull(source);

	assertNotNull(loader);
	assertNotNull(sources);
    }

    
    @Test
    public void testLoadCompendiums() {
	loader.loadCompendiums();
	
    }
}
