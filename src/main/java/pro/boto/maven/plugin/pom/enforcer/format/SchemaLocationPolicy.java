package pro.boto.maven.plugin.pom.enforcer.format;

/**
 * Controls how the {@code <project>} tag's namespace declarations
 * and {@code xsi:schemaLocation} attribute are rendered.
 */
public enum SchemaLocationPolicy {

    /**
     * All attributes on one line:
     * {@code <project xmlns="..." xmlns:xsi="..." xsi:schemaLocation="...">}
     */
    INLINE,

    /**
     * Each attribute on its own line, aligned:
     * <pre>{@code
     * <project xmlns="..."
     *          xmlns:xsi="..."
     *          xsi:schemaLocation="...">
     * }</pre>
     */
    INDENTED,

    /**
     * Preserve attributes as-is, no reformatting.
     */
    KEEP
}
