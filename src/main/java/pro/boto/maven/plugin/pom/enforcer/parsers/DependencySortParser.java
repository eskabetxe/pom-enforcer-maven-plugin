package pro.boto.maven.plugin.pom.enforcer.parsers;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DependencySortParser implements Parser {
    public static final String DEFAULT_SORTING_ORDER = "groupId,artifactId,scope,classifier";
    public static final boolean DEFAULT_BOM_AT_BEGINNING = true;
    public static final boolean DEFAULT_BOM_KEEP_ORDER = true;

    private List<String> sortFields;
    private boolean bomAtBeginning;
    private boolean bomKeepOrder;

    public DependencySortParser() {
        withSortingOrder(DEFAULT_SORTING_ORDER);
        withBomAtBeginning(DEFAULT_BOM_AT_BEGINNING);
        withBomKeepOrder(DEFAULT_BOM_KEEP_ORDER);
    }

    public DependencySortParser withSortingOrder(String sortingOrder) {
        if (sortingOrder != null && !sortingOrder.isBlank()) {
            this.sortFields = Arrays.stream(sortingOrder.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return this;
    }

    public DependencySortParser withBomAtBeginning(boolean bomAtBeginning) {
        this.bomAtBeginning = bomAtBeginning;
        return this;
    }

    public DependencySortParser withBomKeepOrder(boolean bomKeepOrder) {
        this.bomKeepOrder = bomKeepOrder;
        return this;
    }

    @Override
    public void accept(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();

        sortDependenciesIn(root.getChild("dependencies", ns), ns);

        Element depMgmt = root.getChild("dependencyManagement", ns);
        if (depMgmt != null) {
            sortDependenciesIn(depMgmt.getChild("dependencies", ns), ns);
        }
    }

    private void sortDependenciesIn(Element parent, Namespace ns) {
        if (parent == null) return;

        List<Element> deps = new ArrayList<>(parent.getChildren());
        if (deps.size() < 2) return;

        deps.sort((left, right) -> {
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
        });

        parent.setContent(deps);
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
