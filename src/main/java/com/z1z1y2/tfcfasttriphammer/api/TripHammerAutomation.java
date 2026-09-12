package com.z1z1y2.tfcfasttriphammer.api;

import java.util.Optional;

/** Provides optional automation for a trip-hammer hit. */
@FunctionalInterface
public interface TripHammerAutomation {
    /** Empty means not claimed; present means claimed, including a failed work attempt. */
    Optional<Boolean> tryWork(TripHammerContext context);
}
