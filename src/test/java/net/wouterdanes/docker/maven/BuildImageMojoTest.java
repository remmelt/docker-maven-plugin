/*
    Copyright 2014 Wouter Danes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

*/

package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.provider.AbstractFakeDockerProvider;
import net.wouterdanes.docker.provider.DockerExceptionThrowingDockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.provider.model.PushableImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

public class BuildImageMojoTest {

    private static final String FAKE_PROVIDER_KEY = UUID.randomUUID().toString();
    private static final String IMAGEID = UUID.randomUUID().toString();
    private static final String STARTID = UUID.randomUUID().toString();
    private static final String NAMEANDTAG = UUID.randomUUID().toString();
    private static final String REGISTRY = UUID.randomUUID().toString();
    private static final String REGISTRYANDNAMEANDTAG = REGISTRY + "/" + NAMEANDTAG;

    private BuildImageMojo mojo = new BuildImageMojo();

    private ImageBuildConfiguration mockImage;

    @Before
    public void setUp() throws Exception {
        mojo.setPluginContext(new HashMap());

        FakeDockerProvider.instance = Mockito.mock(FakeDockerProvider.class);
        Mockito.when(FakeDockerProvider.instance.buildImage(any(ImageBuildConfiguration.class))).thenReturn(IMAGEID);

        DockerProviderSupplier.registerProvider(FAKE_PROVIDER_KEY, FakeDockerProvider.class);
        DockerExceptionThrowingDockerProvider.class.newInstance();

        // valid by default
        mockImage = Mockito.mock(ImageBuildConfiguration.class);
        Mockito.when(mockImage.getId()).thenReturn(STARTID);
        Mockito.when(mockImage.getNameAndTag()).thenReturn(NAMEANDTAG);

        List<ImageBuildConfiguration> images = Collections.singletonList(mockImage);

        mojo.setImages(images);
    }

    @After
    public void tearDown() throws Exception {
        DockerProviderSupplier.removeProvider(FAKE_PROVIDER_KEY);
    }

    @Test
    public void testThatTheMojoBuildsAtLeastOneImageWhenAllImagesAreValid() throws Exception {
        executeMojo(FAKE_PROVIDER_KEY);

        Mockito.verify(FakeDockerProvider.instance, Mockito.atLeastOnce()).buildImage(any(ImageBuildConfiguration.class));

        assertTrue(mojo.getPluginErrors().isEmpty());
        assertImageNotEnqueuedForPush();
    }

    @Test
    public void testThatTheMojoLogsAnErrorWhenBuildingAnImageFails() throws Exception {
        executeMojo(DockerExceptionThrowingDockerProvider.PROVIDER_KEY);

        assertFalse(mojo.getPluginErrors().isEmpty());
        assertImageNotEnqueuedForPush();
    }

    @Test
    public void testThatTheMojoEnqueuesImageWithoutRegistryForPush() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());
        assertImageEnqueuedForPush(NAMEANDTAG);
    }

    @Test
    public void testThatTheMojoEnqueuesImageWithRegistryForPush() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);
        Mockito.when(mockImage.getRegistry()).thenReturn(REGISTRY);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());
        assertImageEnqueuedForPush(REGISTRYANDNAMEANDTAG);
    }

    @Test
    public void testThatTheMojoEnqueuesImageWithoutNameForPush() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);
        Mockito.when(mockImage.getNameAndTag()).thenReturn(null);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());
        assertImageEnqueuedForPush(null);
    }

    @Test(expected = MojoExecutionException.class)
    public void testThatTheMojoThrowsAnExceptionWhenDuplicateImageIdsExist() throws Exception {
        List<ImageBuildConfiguration> images = new ArrayList<>(2);
        images.add(mockImage);
        images.add(mockImage);

        mojo.setImages(images);

        mojo.execute();
        assertImageEnqueuedForPush(null);
    }

    private void executeMojo(String provider) throws MojoExecutionException, MojoFailureException {
        mojo.setProviderName(provider);
        mojo.execute();
    }

    private void assertImageNotEnqueuedForPush() {
        assertTrue(mojo.getImagesToPush().isEmpty());
    }

    private void assertImageEnqueuedForPush(String expectedNameAndTag) {
        assertEquals(1, mojo.getImagesToPush().size());

        PushableImage actual = mojo.getImagesToPush().get(0);
        assertEquals(BuildImageMojoTest.IMAGEID, actual.getImageId());
        assertEquals(expectedNameAndTag, actual.getNameAndTag().orNull());
    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {

        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }

}
