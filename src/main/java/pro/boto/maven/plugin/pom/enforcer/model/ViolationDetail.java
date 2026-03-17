package pro.boto.maven.plugin.pom.enforcer.model;

import java.util.Objects;

/**
 * Pinpoints a single violation location inside a POM document.
 */
public final class ViolationDetail {

    private final String path;
    private final String expected;
    private final String actual;

    public ViolationDetail(String path, String expected, String actual) {
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.expected = expected;
        this.actual = actual;
    }

    public String path() {
        return path;
    }

    public String expected() {
        return expected;
    }

    public String actual() {
        return actual;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("at <").append(path).append(">");
        if (expected != null) sb.append(" expected=[").append(expected).append(']');
        if (actual != null) sb.append(" actual=[").append(actual).append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViolationDetail)) return false;
        ViolationDetail that = (ViolationDetail) o;
        return path.equals(that.path) && Objects.equals(expected, that.expected) && Objects.equals(actual, that.actual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, expected, actual);
    }
}
