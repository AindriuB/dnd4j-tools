package ie.dnd4j.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DOMUtils {

    public static List<Element> getChildrenWithName(Element parent, String name) {
	List<Element> elements = new ArrayList<Element>();
	for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		if (name.equals(element.getLocalName())) {
		    elements.add(element);
		}
	    }
	}
	return elements;
    }

}
