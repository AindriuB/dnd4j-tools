package ie.dnd4j.loader.aurora;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLReader.class);
    

    /**
     * Default encoding of UTF-8
     * 
     * @param xml
     * @return
     */
    protected Document parseXML(String xml) {
	return parseXML(xml, "UTF-8");
    }

    /***
     * Parse the XML body from the end point
     * 
     * @param xml
     * @param encoding
     * @return an XML Document
     */
    protected Document parseXML(String xml, String encoding) {
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
}
