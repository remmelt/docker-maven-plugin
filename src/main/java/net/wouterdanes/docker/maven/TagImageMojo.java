/*
    Copyright 2014 Lachlan Coote

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

import java.util.List;

import com.google.common.base.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ImageTagConfiguration;

/**
 * This class is responsible for tagging docking images in the install phase of the maven build. The goal is called
 * "tag-images"
 */
@Mojo(defaultPhase = LifecyclePhase.INSTALL, name = "tag-images", threadSafe = true,
        instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class TagImageMojo extends AbstractDockerMojo {

    @Parameter(required = true)
    private List<ImageTagConfiguration> images;

    public void setImages(final List<ImageTagConfiguration> images) {
        this.images = images;
    }

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        for (ImageTagConfiguration config : images) {
            if (config.getTags().isEmpty()) {
                getLog().warn(String.format("Image '%s' doesn't specify any additional tags, ignoring it.", config.getId()));
                continue;
            }
            applyTagsToImage(config);
        }
    }

    private void applyTagsToImage(ImageTagConfiguration config) throws MojoFailureException {
        final String imageId = config.getId();
        boolean push = config.isPush();
        Optional<String> registry = Optional.fromNullable(config.getRegistry());

        Optional<BuiltImageInfo> builtInfo = getBuiltImageForStartId(imageId);

        final String idToTag;
        if (builtInfo.isPresent()) {
            idToTag = builtInfo.get().getImageId();
            registry = registry.or(builtInfo.get().getRegistry());
        } else {
            idToTag = imageId;
        }

        for (String nameAndTag : config.getTags()) {
            attachTag(idToTag, nameAndTag);
            if (builtInfo.isPresent()) {
                builtInfo.get().addName(nameAndTag);
            }
            if (push) {
                enqueueForPushing(idToTag, Optional.fromNullable(nameAndTag), registry);
            }
        }
    }

}