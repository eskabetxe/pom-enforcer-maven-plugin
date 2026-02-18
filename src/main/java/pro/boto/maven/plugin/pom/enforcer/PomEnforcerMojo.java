package pro.boto.maven.plugin.pom.enforcer;

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
    @Parameter
    protected boolean keepBlankLines;

    @Parameter
    protected int indentSpacesNumber;

    @Parameter
    protected String lineSeparator;

    @Parameter
    protected boolean formatSchemaLocation;

    @Parameter
    protected String encoding;
    // Template order configuration
    @Parameter
    protected String templateOrder;
    // Dependency sort configuration
    @Parameter(defaultValue = "groupId,artifactId,scope,classifier")
    protected String dependencySort;

    @Parameter(defaultValue = "true")
    protected boolean bomAtBeginning;

    @Parameter(defaultValue = "true")
    protected boolean bomKeepOrder;

    protected void processProjects(boolean applyChanges) throws MojoExecutionException {
        List<File> violatedFiles = new ArrayList<>();

        List<Parser> parsers = new LinkedList<>();
        parsers.add(new TemplateOrderParser(templateOrder));
        parsers.add(new DependencySortParser(dependencySort, bomAtBeginning, bomKeepOrder));

        PomSerde pomSerde = PomSerde.builder()
                .withIndentSpacesNumber(indentSpacesNumber)
                .withKeepBlankLines(keepBlankLines)
                .withLineSeparator(lineSeparator)
                .withFormatSchemaLocation(formatSchemaLocation)
                .withEncoding(encoding)
                .build();

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
