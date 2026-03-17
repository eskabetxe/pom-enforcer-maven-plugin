package pro.boto.maven.plugin.pom.enforcer.mojo;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.rules.PomRule;

import org.junit.jupiter.api.Test;

import java.util.List;

class RulesConfigTest {

    @Test
    void defaultConfigShouldBuildAllRules() {
        RulesConfig config = new RulesConfig();

        List<PomRule> rules = config.buildRules();

        assertThat(rules).extracting(PomRule::getName).containsExactly("template-order", "dependency-order");
    }

    @Test
    void nullTemplateOrderShouldExcludeRule() {
        RulesConfig config = new RulesConfig();
        config.setTemplateOrder(null);

        List<PomRule> rules = config.buildRules();

        assertThat(rules).extracting(PomRule::getName).containsExactly("dependency-order");
    }

    @Test
    void nullDependencyOrderShouldExcludeRule() {
        RulesConfig config = new RulesConfig();
        config.setDependencyOrder(null);

        List<PomRule> rules = config.buildRules();

        assertThat(rules).extracting(PomRule::getName).containsExactly("template-order");
    }

    @Test
    void customSortByShouldBePassedToRule() {
        RulesConfig config = new RulesConfig();
        config.getDependencyOrder().setSortBy("groupId,artifactId");
        config.getDependencyOrder().setBomFirst(false);

        List<PomRule> rules = config.buildRules();

        assertThat(rules).hasSize(2);
        assertThat(rules).extracting(PomRule::getName).contains("dependency-order");
    }
}
