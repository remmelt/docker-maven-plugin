package net.wouterdanes.docker;

import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.codehaus.plexus.util.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;

/**
 * This Integration Test checks the registry to see if all images are pushed by the IT POMs.
 */
public class VerifyPushedImagesIT {

    private WebTarget repositories;
    private DockerProvider dockerProvider;

    @Before
    public void setUp() throws Exception {

        String registryUri = System.getProperty("docker.registry");
        ClientConfig config = new ClientConfig(new JacksonFeature());
        repositories = ClientBuilder.newClient(config).target(registryUri).path("v1/repositories");

        String dockerProviderName = System.getProperty("docker.provider");
        if (StringUtils.isBlank(dockerProviderName)) {
            dockerProviderName = "remote";
        }
        dockerProvider = new DockerProviderSupplier(dockerProviderName).get();

    }

    @Test
    public void testThatPushWithCredsItImagesGotPushed() throws Exception {

        assertThatImageExistsInRepository("drek", "latest", "push-with-creds-it");
        assertThatImageExistsInRepository("nginxier", "latest", "push-with-creds-it");

    }

    @Test
    public void testThatPushWithExplicitRegistryItImagesGotPushed() throws Exception {

        assertThatImageExistsInRepository("corgis", "latest", "push-with-explicit-registry-it");

    }

    @Test
    public void testThatTagAndPushItImagesGotPushed() throws Exception {

        assertThatImageExistsInRepository("dross", "snapshot", "tag-and-push-it");
        assertThatImageExistsInRepository("dross", "release", "tag-and-push-it");
        assertThatImageExistsInRepository("dross", "4.1", "tag-and-push-it");

    }

    @Test
    public void testThatDrossImagesWereTagged() throws Exception {

        dockerProvider.removeImage("dross:snapshot");
        dockerProvider.removeImage("dross:release");
        dockerProvider.removeImage("dross:4.1");

    }

    @Test
    public void testThatCorgisImageWasTagged() throws Exception {

        dockerProvider.removeImage("corgis:latest");

    }

    @Test
    public void testThatDrekImageWasTagged() throws Exception {

        dockerProvider.removeImage("drek:latest");

    }

    @Test
    public void testThatSimpleItImageWasTagged() throws Exception {

        dockerProvider.removeImage("nginx-name:tahahag");

    }

    private void assertThatImageExistsInRepository(String name, String tag, String itName) {

        Map<String,String> tags = repositories.path(name).path("tags")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, String>>() {
                });

        Assert.assertTrue(String.format("Integration test '%s' should push image '%s:%s'.", itName, name, tag),
                tags.containsKey(tag));

    }
}
