package pro.boto.maven.plugin.pom.enforcer.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, aggregator = true)
public class CheckMojo extends EnforcerMojo {
    @Override
    public void execute() throws MojoExecutionException {
        processProjects(false);
    }
}
