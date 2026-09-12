package com.z1z1y2.tfcfasttriphammer.api;

import com.z1z1y2.tfcfasttriphammer.TFCFastTripHammer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Thread-safe registry for external trip-hammer automation controllers. */
public final class TripHammerAutomationRegistry {
    private static final CopyOnWriteArrayList<Registration> CONTROLLERS = new CopyOnWriteArrayList<>();
    private static final Comparator<Registration> ORDER = Comparator.comparingInt(Registration::priority)
        .reversed().thenComparing(Registration::controllerId);

    private TripHammerAutomationRegistry() {}

    /** Registers or replaces a controller with the same stable id. */
    public static synchronized void register(String controllerId, int priority, boolean exclusive,
                                             TripHammerAutomation controller) {
        if (controllerId == null || controllerId.isBlank())
            throw new IllegalArgumentException("controller id cannot be blank");
        if (controller == null) throw new IllegalArgumentException("controller cannot be null");
        CONTROLLERS.removeIf(existing -> existing.controllerId().equals(controllerId));
        CONTROLLERS.add(new Registration(controllerId, priority, exclusive, controller));
        CONTROLLERS.sort(ORDER);
    }

    /** Dispatches to the first controller that claims the hit. */
    public static Optional<Boolean> tryWork(TripHammerContext context) {
        for (Registration registration : CONTROLLERS) {
            try {
                Optional<Boolean> result = registration.controller().tryWork(context);
                if (result != null && result.isPresent()) return result;
            } catch (RuntimeException exception) {
                TFCFastTripHammer.LOGGER.warn(
                    "Trip-hammer automation controller '{}' failed", registration.controllerId(), exception);
            }
        }
        return Optional.empty();
    }

    /** True when an external controller has exclusive ownership of automation. */
    public static boolean hasExclusiveController() {
        return CONTROLLERS.stream().anyMatch(Registration::exclusive);
    }

    public static List<Registration> registrations() {
        return List.copyOf(new ArrayList<>(CONTROLLERS));
    }

    public record Registration(String controllerId, int priority, boolean exclusive,
                               TripHammerAutomation controller) {}
}
