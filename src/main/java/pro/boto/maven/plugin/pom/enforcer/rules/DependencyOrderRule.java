package pro.boto.maven.plugin.pom.enforcer.rules;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.model.ViolationDetail;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyOrderRule implements PomRule {

    public static final String DEFAULT_SORT_BY = "groupId,artifactId,scope,classifier";
    public static final boolean DEFAULT_BOM_FIRST = true;
    public static final boolean DEFAULT_BOM_PRESERVE_ORDER = true;

    private List<String> sortFields;
    private boolean bomFirst;
    private boolean bomPreserveOrder;

    public DependencyOrderRule() {
        withSortBy(DEFAULT_SORT_BY);
        withBomFirst(DEFAULT_BOM_FIRST);
        withBomPreserveOrder(DEFAULT_BOM_PRESERVE_ORDER);
    }

    public DependencyOrderRule withSortBy(String sortBy) {
        if (sortBy != null && !sortBy.isBlank()) {
            this.sortFields = Arrays.stream(sortBy.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return this;
    }

    public DependencyOrderRule withBomFirst(boolean bomFirst) {
        this.bomFirst = bomFirst;
        return this;
    }

    public DependencyOrderRule withBomPreserveOrder(boolean bomPreserveOrder) {
        this.bomPreserveOrder = bomPreserveOrder;
        return this;
    }

    @Override
    public String getName() {
        return "dependency-order";
    }

    @Override
    public int getPriority() {
        return 200;
    }

    // ---- READ-ONLY ----

    @Override
    public List<RuleViolation> analyze(Document document) {
        List<RuleViolation> violations = new ArrayList<>();
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        analyzeSection(root.getChild("dependencies", ns), ns, violations, "dependencies", false);

        Element mgmt = root.getChild("dependencyManagement", ns);
        if (mgmt != null) {
            analyzeSection(
                    mgmt.getChild("dependencies", ns), ns, violations, "dependencyManagement/dependencies", true);
        }

        return violations;
    }

    private void analyzeSection(
            Element parent, Namespace ns, List<RuleViolation> violations, String path, boolean isManagedSection) {
        if (parent == null || parent.getChildren().size() < 2) return;

        List<Element> original = parent.getChildren();
        List<Element> sorted = new ArrayList<>(original);
        sorted.sort(dependencyComparator(ns, isManagedSection));

        if (!elementsMatchOrder(original, sorted, ns)) {
            List<String> currentOrder =
                    original.stream().map(e -> gavLabel(e, ns)).collect(Collectors.toList());
            List<String> expectedOrder =
                    sorted.stream().map(e -> gavLabel(e, ns)).collect(Collectors.toList());

            ViolationDetail detail =
                    new ViolationDetail(path, String.join(", ", expectedOrder), String.join(", ", currentOrder));

            violations.add(new RuleViolation(
                    getName(), "Dependencies in <" + path + "> are not sorted.", Collections.singletonList(detail)));
        }
    }

    // ---- MUTATION ----

    @Override
    public void apply(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        sortSection(root.getChild("dependencies", ns), ns, false);

        Element mgmt = root.getChild("dependencyManagement", ns);
        if (mgmt != null) {
            sortSection(mgmt.getChild("dependencies", ns), ns, true);
        }
    }

    private void sortSection(Element parent, Namespace ns, boolean isManagedSection) {
        if (parent == null || parent.getChildren().size() < 2) return;

        List<Element> sorted = new ArrayList<>(parent.getChildren());
        sorted.sort(dependencyComparator(ns, isManagedSection));
        parent.setContent(sorted);
    }

    // ---- SHARED ----

    private Comparator<Element> dependencyComparator(Namespace ns, boolean isManagedSection) {
        return (left, right) -> compareDependencies(left, right, ns, isManagedSection);
    }

    private int compareDependencies(Element left, Element right, Namespace ns, boolean isManagedSection) {
        // BOM handling only applies to dependencyManagement
        if (bomFirst && isManagedSection) {
            boolean leftIsBom = isBom(left, ns);
            boolean rightIsBom = isBom(right, ns);

            if (leftIsBom && !rightIsBom) return -1;
            if (!leftIsBom && rightIsBom) return 1;
            if (leftIsBom && rightIsBom && bomPreserveOrder) return 0;
        }

        for (String field : sortFields) {
            String leftVal = getChildText(left, field, ns);
            String rightVal = getChildText(right, field, ns);
            int comparison = leftVal.compareTo(rightVal);
            if (comparison != 0) return comparison;
        }
        return 0;
    }

    private boolean isBom(Element element, Namespace ns) {
        String type = getChildText(element, "type", ns);
        String scope = getChildText(element, "scope", ns);
        return "pom".equalsIgnoreCase(type) && "import".equalsIgnoreCase(scope);
    }

    private String getChildText(Element element, String name, Namespace ns) {
        Element child = element.getChild(name, ns);
        return (child != null) ? child.getTextTrim() : "";
    }

    private String gavLabel(Element dep, Namespace ns) {
        return getChildText(dep, "groupId", ns) + ":" + getChildText(dep, "artifactId", ns);
    }

    private boolean elementsMatchOrder(List<Element> original, List<Element> sorted, Namespace ns) {
        if (original.size() != sorted.size()) return false;
        for (int i = 0; i < original.size(); i++) {
            if (!gavLabel(original.get(i), ns).equals(gavLabel(sorted.get(i), ns))) {
                return false;
            }
        }
        return true;
    }
}
