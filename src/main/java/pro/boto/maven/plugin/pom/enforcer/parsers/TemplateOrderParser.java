package pro.boto.maven.plugin.pom.enforcer.parsers;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateOrderParser implements Parser {

    public static final String DEFAULT_TEMPLATE_PATH = "/default_formatter.xml";

    private final Map<String, List<String>> orderMap = new HashMap<>();

    public TemplateOrderParser() {
        withTemplatePath(DEFAULT_TEMPLATE_PATH);
    }

    public TemplateOrderParser withTemplatePath(String templatePath) {
        if (templatePath != null && !templatePath.isBlank()) {
            SAXBuilder builder = new SAXBuilder();
            // Always load from resources (classpath)
            try (InputStream is = getClass().getResourceAsStream(templatePath)) {
                Document doc = builder.build(is);
                parseTemplate(doc.getRootElement());
            } catch (IOException | JDOMException e) {
                throw new IllegalStateException(
                        String.format("Template resource %s not found in classpath", templatePath), e);
            }
        }
        return this;
    }

    private void parseTemplate(Element element) {
        List<Element> children = element.getChildren();
        if (children.isEmpty()) return;

        List<String> order = new ArrayList<>();
        for (Element child : children) {
            order.add(child.getName());
            parseTemplate(child); // Recursive to map inner levels
        }
        orderMap.put(element.getName(), order);
    }

    @Override
    public void accept(Document parent) {
        sortElement(parent.getRootElement());
    }

    public void sortElement(Element parent) {

        List<String> order = orderMap.get(parent.getName());
        List<Element> children = new ArrayList<>(parent.getChildren());

        if (order != null && children.size() > 1) {
            children.sort((left, right) -> {
                int leftIndex = order.indexOf(left.getName());
                int rightIndex = order.indexOf(right.getName());

                // Both elements exist in the template order
                if (leftIndex != -1 && rightIndex != -1) {
                    return Integer.compare(leftIndex, rightIndex);
                }

                // Only left exists: left comes first
                if (leftIndex != -1) return -1;

                // Only right exists: right comes first
                if (rightIndex != -1) return 1;

                // Neither in template: fallback to alphabetical order
                return left.getName().compareTo(right.getName());
            });

            // Replace all content with the newly sorted list
            parent.setContent(children);
        }

        // Recurse into children to apply sorting at all levels
        for (Element child : parent.getChildren()) {
            sortElement(child);
        }
    }
}
