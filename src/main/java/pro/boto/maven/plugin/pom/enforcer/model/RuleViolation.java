package pro.boto.maven.plugin.pom.enforcer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Outcome of a single rule violation. Carries the rule name, a human-readable
 * message, and zero or more {@link ViolationDetail} entries for precise reporting.
 */
public final class RuleViolation {

    private final String ruleName;
    private final String message;
    private final List<ViolationDetail> details;

    public RuleViolation(String ruleName, String message) {
        this(ruleName, message, Collections.emptyList());
    }

    public RuleViolation(String ruleName, String message, List<ViolationDetail> details) {
        this.ruleName = Objects.requireNonNull(ruleName, "ruleName must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.details = Collections.unmodifiableList(new ArrayList<>(details));
    }

    public String ruleName() {
        return ruleName;
    }

    public String message() {
        return message;
    }

    public List<ViolationDetail> details() {
        return details;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(ruleName).append("] ").append(message);
        for (ViolationDetail detail : details) {
            sb.append("\n    - ").append(detail);
        }
        return sb.toString();
    }
}
