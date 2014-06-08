package net.wouterdanes.docker.maven;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class is responsible for starting docking containers in the pre-integration phase of the maven build. The goal
 * is called "start-containers"
 */
@Mojo(defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, name = "start-containers",
        threadSafe = true, instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class StartContainerMojo extends AbstractDockerMojo {

    @Parameter(required = true)
    private List<ContainerStartConfiguration> containers;

    @Inject
    public StartContainerMojo(final List<ContainerStartConfiguration> containers) {
        this.containers = containers;
    }

    @Component
    private MavenProject project;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        DockerProvider provider = getDockerProvider();
        for (ContainerStartConfiguration configuration : containers) {
            replaceImageWithBuiltImageIdIfInternalId(configuration);
            try {
                getLog().info(String.format("Starting container '%s'..", configuration.getId()));
                String containerId = provider.startContainer(configuration);
                List<ExposedPort> exposedPorts = provider.getExposedPorts(containerId);
                for (ExposedPort exposedPort : exposedPorts) {
                    String prefix = String.format("docker.containers.%s.ports.%s.",
                            configuration.getId(), exposedPort.getContainerPort());
                    addPropertyToProject(prefix + "host", exposedPort.getHost());
                    addPropertyToProject(prefix + "port", String.valueOf(exposedPort.getExternalPort()));
                }
                getLog().info(String.format("Started container with id '%s'", containerId));
                registerStartedContainer(containerId);
            } catch (DockerException e) {
                getLog().error("Failed to start container", e);
            }
        }
        getLog().debug("Properties after exposing ports: " + project.getProperties());
    }

    private void replaceImageWithBuiltImageIdIfInternalId(final ContainerStartConfiguration configuration) {
        Optional<BuiltImageInfo> builtImage = getBuiltImageForStartId(configuration.getImage());
        if (builtImage.isPresent()) {
            configuration.fromImage(builtImage.get().getImageId());
        }
    }

    public void setProject(final MavenProject project) {
        this.project = project;
    }

    private void addPropertyToProject(final String key, final String value) {
        getLog().info(String.format("Setting property '%s' to '%s'", key, value));
        project.getProperties().setProperty(key, value);
    }
}