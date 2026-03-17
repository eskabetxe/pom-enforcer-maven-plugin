package pro.boto.maven.plugin.pom.enforcer.rules;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.model.ViolationDetail;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateOrderRule implements PomRule {

    public static final String DEFAULT_TEMPLATE_PATH = "/default_formatter.xml";

    private final Map<String, List<String>> orderMap = new HashMap<>();

    public TemplateOrderRule() {
        withTemplate(DEFAULT_TEMPLATE_PATH);
    }

    public TemplateOrderRule withTemplate(String templatePath) {
        if (templatePath != null && !templatePath.isBlank()) {
            SAXBuilder builder = new SAXBuilder();
            try (InputStream is = getClass().getResourceAsStream(templatePath)) {
                Document doc = builder.build(is);
                orderMap.clear();
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
            parseTemplate(child);
        }
        orderMap.put(element.getName(), order);
    }

    @Override
    public String getName() {
        return "template-order";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    // ---- READ-ONLY ----

    @Override
    public List<RuleViolation> analyze(Document document) {
        List<ViolationDetail> details = new ArrayList<>();
        collectViolations(document.getRootElement(), details);
        if (details.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(
                new RuleViolation(getName(), "Elements are not in the expected order.", details));
    }

    private void collectViolations(Element parent, List<ViolationDetail> details) {
        List<String> order = orderMap.get(parent.getName());
        List<Element> children = parent.getChildren();

        if (order != null && children.size() > 1) {
            List<String> currentNames = children.stream().map(Element::getName).collect(Collectors.toList());

            List<String> sortedNames = new ArrayList<>(currentNames);
            sortedNames.sort(templateComparator(order));

            if (!currentNames.equals(sortedNames)) {
                details.add(new ViolationDetail(
                        parent.getName(), String.join(", ", sortedNames), String.join(", ", currentNames)));
            }
        }

        for (Element child : children) {
            collectViolations(child, details);
        }
    }

    // ---- MUTATION ----

    @Override
    public void apply(Document document) {
        sortElement(document.getRootElement());
    }

    private void sortElement(Element parent) {
        List<String> order = orderMap.get(parent.getName());
        List<Element> children = new ArrayList<>(parent.getChildren());

        if (order != null && children.size() > 1) {
            children.sort((left, right) -> templateComparator(order).compare(left.getName(), right.getName()));
            parent.setContent(children);
        }

        for (Element child : parent.getChildren()) {
            sortElement(child);
        }
    }

    // ---- SHARED ----

    private java.util.Comparator<String> templateComparator(List<String> order) {
        return (left, right) -> {
            int leftIndex = order.indexOf(left);
            int rightIndex = order.indexOf(right);
            if (leftIndex != -1 && rightIndex != -1) return Integer.compare(leftIndex, rightIndex);
            if (leftIndex != -1) return -1;
            if (rightIndex != -1) return 1;
            return left.compareTo(right);
        };
    }
}
