package pro.boto.maven.plugin.pom.enforcer.rules;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;

import org.jdom2.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Immutable, priority-sorted collection of {@link PomRule} instances.
 * Thread-safe for parallel reactor builds.
 */
public final class RuleRegistry {

    private final List<PomRule> rules;

    public RuleRegistry(List<PomRule> rules) {
        List<PomRule> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparingInt(PomRule::getPriority));
        this.rules = Collections.unmodifiableList(sorted);
    }

    /**
     * Read-only pass: runs {@code analyze()} on every rule, collects all violations.
     */
    public List<RuleViolation> analyzeAll(Document document) {
        List<RuleViolation> violations = new ArrayList<>();
        for (PomRule rule : rules) {
            violations.addAll(rule.analyze(document));
        }
        return violations;
    }

    /**
     * Mutation pass: runs {@code apply()} on every rule in priority order.
     */
    public void applyAll(Document document) {
        for (PomRule rule : rules) {
            rule.apply(document);
        }
    }

    public List<PomRule> getRules() {
        return rules;
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }
}
