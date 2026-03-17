package pro.boto.maven.plugin.pom.enforcer.mojo;

import pro.boto.maven.plugin.pom.enforcer.PomEnforcer;
import pro.boto.maven.plugin.pom.enforcer.format.FormattingConfig;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;
import pro.boto.maven.plugin.pom.enforcer.rules.RuleRegistry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for all pom-enforcer goals.
 * Wiring only — no rule logic, no formatting logic.
 */
public abstract class EnforcerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    protected List<MavenProject> reactorProjects;

    @Parameter
    protected FormattingConfig formatting = FormattingConfig.defaultConfig();

    @Parameter
    protected RulesConfig rules = new RulesConfig();

    protected PomEnforcer buildEnforcer() {
        RuleRegistry registry = new RuleRegistry(rules.buildRules());
        return new PomEnforcer(formatting, registry);
    }

    protected void processProjects(boolean applyChanges) throws MojoExecutionException {
        PomEnforcer enforcer = buildEnforcer();
        List<File> violatedFiles = new ArrayList<>();

        for (MavenProject project : reactorProjects) {
            File pomFile = project.getFile();
            if (pomFile == null || !pomFile.exists()) continue;

            try {
                List<RuleViolation> violations = applyChanges ? enforcer.apply(pomFile) : enforcer.check(pomFile);

                if (!violations.isEmpty()) {
                    if (applyChanges) {
                        getLog().info("Applied changes to: " + pomFile.getName());
                    }
                    for (RuleViolation v : violations) {
                        getLog().error("Violation in " + pomFile.getName() + ": " + v);
                    }
                    violatedFiles.add(pomFile);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Error processing " + pomFile.getName(), e);
            }
        }

        if (!applyChanges && !violatedFiles.isEmpty()) {
            throw new MojoExecutionException(violatedFiles.size() + " POM file(s) have violations. "
                    + "Run 'mvn pom-enforcer:apply' to fix them.");
        }
    }
}
