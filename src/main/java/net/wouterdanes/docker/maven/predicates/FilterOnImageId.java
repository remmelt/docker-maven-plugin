package net.wouterdanes.docker.maven.predicates;

import com.google.common.base.Predicate;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;

/**
 * A Predicate to use to filter a collection of {@link net.wouterdanes.docker.provider.model.BuiltImageInfo}s on Image
 * Id.
 */
public final class FilterOnImageId implements Predicate<BuiltImageInfo> {

    private final String imageId;

    private FilterOnImageId(final String imageId) {
        this.imageId = imageId;
    }

    @Override
    public boolean apply(final BuiltImageInfo input) {
        return imageId.equals(input.getImageId());
    }

    public static Predicate<BuiltImageInfo> of(String imageId) {
        return new FilterOnImageId(imageId);
    }
}
