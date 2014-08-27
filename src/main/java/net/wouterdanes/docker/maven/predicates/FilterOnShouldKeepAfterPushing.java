package net.wouterdanes.docker.maven.predicates;

import com.google.common.base.Predicate;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;

/**
 * Filters {@link net.wouterdanes.docker.provider.model.BuiltImageInfo}s on the field shouldKeepAfterStopping
 */
public final class FilterOnShouldKeepAfterPushing implements Predicate<BuiltImageInfo> {

    public static final FilterOnShouldKeepAfterPushing YES = new FilterOnShouldKeepAfterPushing(true);
    public static final FilterOnShouldKeepAfterPushing NO = new FilterOnShouldKeepAfterPushing(false);

    private final boolean shouldKeep;

    private FilterOnShouldKeepAfterPushing(final boolean shouldKeep) {
        this.shouldKeep = shouldKeep;
    }

    @Override
    public boolean apply(final BuiltImageInfo input) {
        return input.shouldKeepAfterPushing() == shouldKeep;
    }
}
