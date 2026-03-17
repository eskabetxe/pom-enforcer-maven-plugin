package pro.boto.maven.plugin.pom.enforcer.rules;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;

import org.jdom2.Document;

import java.util.List;

/**
 * Contract for every POM rule.
 *
 * <p>Two operations for two goals:
 * <ul>
 *   <li>{@link #analyze(Document)} — read-only inspection, used by {@code check}</li>
 *   <li>{@link #apply(Document)}   — in-place mutation, used by {@code apply}</li>
 * </ul>
 *
 * <p>Design contracts:
 * <ul>
 *   <li>{@code analyze()} MUST NOT mutate the document.</li>
 *   <li>{@code apply()} MUST be idempotent: apply(apply(doc)) == apply(doc).</li>
 *   <li>After {@code apply()}, a subsequent {@code analyze()} MUST return empty.</li>
 * </ul>
 */
public interface PomRule {

    /**
     * Stable, kebab-case identifier used in configuration and reports.
     */
    String getName();

    /**
     * Execution priority. Lower values run first.
     * Bands: 100=structural, 200=element-level, 300=validation-only.
     */
    default int getPriority() {
        return 200;
    }

    /**
     * Read-only analysis. Returns violations without mutating the document.
     *
     * @param document the POM document (must not be mutated)
     * @return list of violations (empty if compliant)
     */
    List<RuleViolation> analyze(Document document);

    /**
     * Mutates the document in-place to make it compliant with this rule.
     *
     * @param document the POM document (mutated in place)
     */
    void apply(Document document);
}
