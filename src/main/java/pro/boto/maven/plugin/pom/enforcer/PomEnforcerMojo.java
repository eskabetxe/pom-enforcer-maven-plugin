package pro.boto.maven.plugin.pom.enforcer;

import static pro.boto.maven.plugin.pom.enforcer.parsers.DependencySortParser.DEFAULT_BOM_AT_BEGINNING;
import static pro.boto.maven.plugin.pom.enforcer.parsers.DependencySortParser.DEFAULT_BOM_KEEP_ORDER;
import static pro.boto.maven.plugin.pom.enforcer.parsers.DependencySortParser.DEFAULT_SORTING_ORDER;
import static pro.boto.maven.plugin.pom.enforcer.parsers.TemplateOrderParser.DEFAULT_TEMPLATE_PATH;
import static pro.boto.maven.plugin.pom.enforcer.xml.PomSerde.*;

import pro.boto.maven.plugin.pom.enforcer.parsers.DependencySortParser;
import pro.boto.maven.plugin.pom.enforcer.parsers.Parser;
import pro.boto.maven.plugin.pom.enforcer.parsers.TemplateOrderParser;
import pro.boto.maven.plugin.pom.enforcer.xml.PomSerde;

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

        List<Parser> parsers = new LinkedList<>();
        parsers.add(new TemplateOrderParser().withTemplatePath(templateOrder));
        parsers.add(new DependencySortParser()
                .withBomAtBeginning(bomAtBeginning)
                .withBomKeepOrder(bomKeepOrder)
                .withSortingOrder(dependencySort));

        PomSerde pomSerde = new PomSerde()
                .withEncoding(encoding)
                .withFormatSchemaLocation(formatSchemaLocation)
                .withIndentSchemaLocation(indentSchemaLocation)
                .withIndentSpacesNumber(indentSpacesNumber)
                .withKeepBlankLines(keepBlankLines)
                .withLineSeparator(lineSeparator);

        PomEnforcerProcessor processor = new PomEnforcerProcessor(pomSerde, parsers);

        for (MavenProject project : reactorProjects) {
            File pomFile = project.getFile();
            if (pomFile == null || !pomFile.exists()) continue;

            try {
                if (processor.process(pomFile, applyChanges)) {
                    if (applyChanges) {
                        getLog().info("Applied changes to: " + pomFile.getName());
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
