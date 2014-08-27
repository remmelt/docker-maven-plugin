package net.wouterdanes.docker.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Special Test Docker Mojo that does nothing, used to test the AbstractDockerMojo
 */
public class NoOpDockerMojo extends AbstractDockerMojo {
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        getLog().info("NO OP Docker Mojo doing nothing..");
    }
}
