package ie.dnd4j.loader.aurora;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ie.dnd4j.JsonFileWriterTest;
import ie.dnd4j.configuration.CompendiumAutoConfiguration;
import ie.dnd4j.configuration.CompendiumSourcesConfiguration;

@SpringBootTest(classes = {CompendiumAutoConfiguration.class})
public class CompendiumBuilderTest extends JsonFileWriterTest {


    @Autowired
    private CompendiumSourceLoader webLoader;
    
    @Autowired
    private CompendiumBuilder processor;
    
    @Autowired
    private CompendiumSourcesConfiguration sources;
    
    @Test
    public void testConfiguration() {
	
	CompendiumSourcesConfiguration  source = webLoader.getCompendiumConfiguration();
	assertNotNull(source);

	assertNotNull(processor);
	assertNotNull(sources);
    }

    
    @Test
    public void testLoadCompendiums() {
	processor.buildCompendium(webLoader.loadSources());
	writeOutput(processor.getCompendium(), "src/test/resources/test-output/compendium.json");
	
    }
}
