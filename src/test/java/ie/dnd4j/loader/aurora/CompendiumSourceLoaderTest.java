package ie.dnd4j.loader.aurora;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

import ie.dnd4j.configuration.CompendiumAutoConfiguration;

@SpringBootTest(classes = {CompendiumAutoConfiguration.class})
class CompendiumSourceLoaderTest {

    @Autowired
    private CompendiumSourceLoader loader;

    
    @Test
    void test() {
	 Map<String, Source> map = loader.loadSources();
	 
	 assertNotNull(map);
    }

}
