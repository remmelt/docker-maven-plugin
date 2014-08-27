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

package net.wouterdanes.docker.provider.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

/**
 * This class holds information about an image that was built so that it can be references in the start goal and
 * removed in the stop goal.
 */
public class BuiltImageInfo {

    private final String startId;
    private final String imageId;
    private final Optional<String> registry;
    private final boolean keepAfterStopping;
    private final boolean keepAfterPushing;
    private final List<String> names;

    public BuiltImageInfo(final String imageId, ImageBuildConfiguration imageConfig) {
        this.imageId = imageId;
        startId = imageConfig.getId();
        registry = Optional.fromNullable(imageConfig.getRegistry());
        names = new ArrayList<>();
        if (imageConfig.getNameAndTag() != null) {
            names.add(imageConfig.getNameAndTag());
        }
        keepAfterStopping = imageConfig.isKeep() || imageConfig.isPush();
        keepAfterPushing = imageConfig.isKeep();
    }

    public String getStartId() {
        return startId;
    }

    public String getImageId() {
        return imageId;
    }

    public Optional<String> getRegistry() {
        return registry;
    }

    public boolean shouldKeepAfterStopping() {
        return keepAfterStopping;
    }

    public boolean shouldKeepAfterPushing() {
        return keepAfterPushing;
    }

    public List<String> getNames() {
        return names;
    }

    public void addName(String name) {
        names.add(name);
    }
}
