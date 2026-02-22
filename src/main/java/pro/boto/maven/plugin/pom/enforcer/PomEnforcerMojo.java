package pro.boto.maven.plugin.pom.enforcer;

import static pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig.*;
import static pro.boto.maven.plugin.pom.enforcer.rules.DependencyOrderRule.DEFAULT_BOM_AT_BEGINNING;
import static pro.boto.maven.plugin.pom.enforcer.rules.DependencyOrderRule.DEFAULT_BOM_KEEP_ORDER;
import static pro.boto.maven.plugin.pom.enforcer.rules.DependencyOrderRule.DEFAULT_SORTING_ORDER;
import static pro.boto.maven.plugin.pom.enforcer.rules.TemplateOrderRule.DEFAULT_TEMPLATE_PATH;

import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.rules.DependencyOrderRule;
import pro.boto.maven.plugin.pom.enforcer.rules.PomRule;
import pro.boto.maven.plugin.pom.enforcer.rules.TemplateOrderRule;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class PomEnforcerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    protected List<MavenProject> reactorProjects;

    // Pom formatting options
    @Parameter(defaultValue = DEFAULT_ENCODING)
    protected String encoding;

    @Parameter(defaultValue = DEFAULT_FORMAT_SCHEMA_LOCATION + "")
    protected boolean formatSchemaLocation;

    @Parameter(defaultValue = DEFAULT_INDENT_SCHEMA_LOCATION + "")
    protected boolean indentSchemaLocation;

    @Parameter(defaultValue = DEFAULT_INDENT_SPACES_NUMBER + "")
    protected int indentSpacesNumber;

    @Parameter(defaultValue = DEFAULT_KEEP_BLANK_LINES + "")
    protected boolean keepBlankLines;

    @Parameter(defaultValue = DEFAULT_LINE_SEPARATOR)
    protected String lineSeparator;

    // Template order configuration
    @Parameter(defaultValue = DEFAULT_TEMPLATE_PATH)
    protected String templateOrder;
    // Dependency sort configuration
    @Parameter(defaultValue = DEFAULT_SORTING_ORDER)
    protected String dependencySort;

    @Parameter(defaultValue = DEFAULT_BOM_AT_BEGINNING + "")
    protected boolean bomAtBeginning;

    @Parameter(defaultValue = DEFAULT_BOM_KEEP_ORDER + "")
    protected boolean bomKeepOrder;

    protected void processProjects(boolean applyChanges) throws MojoExecutionException {
        List<File> violatedFiles = new ArrayList<>();

        List<PomRule> parsers = new LinkedList<>();
        parsers.add(new TemplateOrderRule().withTemplatePath(templateOrder));
        parsers.add(new DependencyOrderRule()
                .withBomAtBeginning(bomAtBeginning)
                .withBomKeepOrder(bomKeepOrder)
                .withSortingOrder(dependencySort));

        FormattingConfig formattingConfig = new FormattingConfig(
                this.encoding,
                this.formatSchemaLocation,
                this.indentSchemaLocation,
                this.indentSpacesNumber,
                this.keepBlankLines,
                this.lineSeparator);

        PomEnforcerProcessor processor = new PomEnforcerProcessor(formattingConfig, parsers);

        for (MavenProject project : reactorProjects) {
            File pomFile = project.getFile();
            if (pomFile == null || !pomFile.exists()) continue;

            try {
                List<RuleViolation> violations = processor.process(pomFile, applyChanges);
                if (!violations.isEmpty()) {
                    if (applyChanges) {
                        getLog().info("Applied changes to: " + pomFile.getName());
                    } else {
                        violations.forEach(
                                v -> getLog().error("Violation in " + pomFile.getName() + ": " + v.toString()));
                    }
                    violatedFiles.add(pomFile);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Error processing " + pomFile.getName(), e);
            }
        }

        if (!applyChanges && !violatedFiles.isEmpty()) {
            violatedFiles.forEach(f -> getLog().error("Violation found in: " + f.getAbsolutePath()));
            throw new MojoExecutionException("POM files are not valid. Run 'mvn pom-enforcer:apply' to fix them.");
        }
    }
}
