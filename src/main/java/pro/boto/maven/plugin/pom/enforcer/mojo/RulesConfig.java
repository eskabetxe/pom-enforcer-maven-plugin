package pro.boto.maven.plugin.pom.enforcer.mojo;

import pro.boto.maven.plugin.pom.enforcer.rules.DependencyOrderRule;
import pro.boto.maven.plugin.pom.enforcer.rules.PomRule;
import pro.boto.maven.plugin.pom.enforcer.rules.TemplateOrderRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Maven-injectable configuration that maps {@code <rules>} XML to rule instances.
 *
 * <pre>{@code
 * <rules>
 *     <templateOrder>
 *         <template>/default_formatter.xml</template>
 *     </templateOrder>
 *     <dependencyOrder>
 *         <sortBy>groupId,artifactId</sortBy>
 *         <bomFirst>true</bomFirst>
 *         <bomPreserveOrder>true</bomPreserveOrder>
 *     </dependencyOrder>
 * </rules>
 * }</pre>
 */
public class RulesConfig {

    // ---- Nested config classes ----

    public static class TemplateOrder {
        private String template = TemplateOrderRule.DEFAULT_TEMPLATE_PATH;

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }
    }

    public static class DependencyOrder {
        private String sortBy = DependencyOrderRule.DEFAULT_SORT_BY;
        private boolean bomFirst = DependencyOrderRule.DEFAULT_BOM_FIRST;
        private boolean bomPreserveOrder = DependencyOrderRule.DEFAULT_BOM_PRESERVE_ORDER;

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }

        public boolean isBomFirst() {
            return bomFirst;
        }

        public void setBomFirst(boolean bomFirst) {
            this.bomFirst = bomFirst;
        }

        public boolean isBomPreserveOrder() {
            return bomPreserveOrder;
        }

        public void setBomPreserveOrder(boolean bomPreserveOrder) {
            this.bomPreserveOrder = bomPreserveOrder;
        }
    }

    // ---- Config fields (all enabled by default) ----

    private TemplateOrder templateOrder = new TemplateOrder();
    private DependencyOrder dependencyOrder = new DependencyOrder();

    public TemplateOrder getTemplateOrder() {
        return templateOrder;
    }

    public void setTemplateOrder(TemplateOrder templateOrder) {
        this.templateOrder = templateOrder;
    }

    public DependencyOrder getDependencyOrder() {
        return dependencyOrder;
    }

    public void setDependencyOrder(DependencyOrder dependencyOrder) {
        this.dependencyOrder = dependencyOrder;
    }

    /**
     * Converts this Maven configuration into concrete rule instances.
     */
    public List<PomRule> buildRules() {
        List<PomRule> rules = new ArrayList<>();

        if (templateOrder != null) {
            rules.add(new TemplateOrderRule().withTemplate(templateOrder.getTemplate()));
        }

        if (dependencyOrder != null) {
            rules.add(new DependencyOrderRule()
                    .withSortBy(dependencyOrder.getSortBy())
                    .withBomFirst(dependencyOrder.isBomFirst())
                    .withBomPreserveOrder(dependencyOrder.isBomPreserveOrder()));
        }

        return rules;
    }
}
