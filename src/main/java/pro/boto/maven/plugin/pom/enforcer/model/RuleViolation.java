package pro.boto.maven.plugin.pom.enforcer.model;

public final class RuleViolation {
    private final String ruleName;
    private final String message;

    public RuleViolation(String ruleName, String message) {
        this.ruleName = ruleName;
        this.message = message;
    }

    public String ruleName() {
        return ruleName;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", ruleName, message);
    }
}
