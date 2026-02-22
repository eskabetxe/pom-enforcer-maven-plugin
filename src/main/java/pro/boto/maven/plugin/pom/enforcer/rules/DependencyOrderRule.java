package pro.boto.maven.plugin.pom.enforcer.rules;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyOrderRule implements PomRule {
    public static final String DEFAULT_SORTING_ORDER = "groupId,artifactId,scope,classifier";
    public static final boolean DEFAULT_BOM_AT_BEGINNING = true;
    public static final boolean DEFAULT_BOM_KEEP_ORDER = true;

    private List<String> sortFields;
    private boolean bomAtBeginning;
    private boolean bomKeepOrder;

    public DependencyOrderRule() {
        withSortingOrder(DEFAULT_SORTING_ORDER);
        withBomAtBeginning(DEFAULT_BOM_AT_BEGINNING);
        withBomKeepOrder(DEFAULT_BOM_KEEP_ORDER);
    }

    public DependencyOrderRule withSortingOrder(String sortingOrder) {
        if (sortingOrder != null && !sortingOrder.isBlank()) {
            this.sortFields = Arrays.stream(sortingOrder.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return this;
    }

    public DependencyOrderRule withBomAtBeginning(boolean bomAtBeginning) {
        this.bomAtBeginning = bomAtBeginning;
        return this;
    }

    public DependencyOrderRule withBomKeepOrder(boolean bomKeepOrder) {
        this.bomKeepOrder = bomKeepOrder;
        return this;
    }

    @Override
    public String getName() {
        return "dependency-order";
    }

    @Override
    public List<RuleViolation> apply(Document document) {
        List<RuleViolation> violations = new ArrayList<>();
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        checkSection(root.getChild("dependencies", ns), ns, violations, "dependencies");

        Element mgmt = root.getChild("dependencyManagement", ns);
        if (mgmt != null) {
            checkSection(mgmt.getChild("dependencies", ns), ns, violations, "dependencyManagement/dependencies");
        }

        return violations;
    }

    private void checkSection(Element parent, Namespace ns, List<RuleViolation> violations, String path) {
        if (parent == null || parent.getChildren().size() < 2) return;

        List<Element> original = new ArrayList<>(parent.getChildren());
        // sort logic using compareDependencies
        List<Element> sorted = new ArrayList<>(original);
        sorted.sort((left, right) -> compareDependencies(left, right, ns));

        if (!original.equals(sorted)) {
            violations.add(new RuleViolation(getName(), "Dependencies in <" + path + "> are not sorted."));
            parent.setContent(sorted);
        }
    }

    private int compareDependencies(Element left, Element right, Namespace ns) {
        if (bomAtBeginning) {
            boolean leftIsBom = isBom(left, ns);
            boolean rightIsBom = isBom(right, ns);

            if (leftIsBom && !rightIsBom) return -1;
            if (!leftIsBom && rightIsBom) return 1;

            // If both are BOMs and we want to preserve their original order
            if (leftIsBom && rightIsBom && bomKeepOrder) return 0;
        }

        // Standard sorting logic for non-BOMs (or all if not using bomAtBeginning)
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
}
