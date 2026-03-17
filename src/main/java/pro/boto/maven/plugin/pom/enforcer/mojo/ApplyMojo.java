package pro.boto.maven.plugin.pom.enforcer.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "apply", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, aggregator = true)
public class ApplyMojo extends EnforcerMojo {
    @Override
    public void execute() throws MojoExecutionException {
        processProjects(true);
    }
}
