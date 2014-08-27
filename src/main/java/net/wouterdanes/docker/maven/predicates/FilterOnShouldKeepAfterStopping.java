package net.wouterdanes.docker.maven.predicates;

import com.google.common.base.Predicate;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;

/**
 * Filters {@link net.wouterdanes.docker.provider.model.BuiltImageInfo}s on the field shouldKeepAfterStopping
 */
public final class FilterOnShouldKeepAfterStopping implements Predicate<BuiltImageInfo> {

    public static final FilterOnShouldKeepAfterStopping YES = new FilterOnShouldKeepAfterStopping(true);
    public static final FilterOnShouldKeepAfterStopping NO = new FilterOnShouldKeepAfterStopping(false);

    private final boolean shouldKeep;

    private FilterOnShouldKeepAfterStopping(final boolean shouldKeep) {
        this.shouldKeep = shouldKeep;
    }

    @Override
    public boolean apply(final BuiltImageInfo input) {
        return input.shouldKeepAfterStopping() == shouldKeep;
    }
}
