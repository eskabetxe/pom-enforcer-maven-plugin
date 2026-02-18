package pro.boto.maven.plugin.pom.enforcer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, aggregator = true)
public class CheckPomEnforcerMojo extends PomEnforcerMojo {
    @Override
    public void execute() throws MojoExecutionException {
        processProjects(false);
    }
}
