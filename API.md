# TFC Fast Trip Hammer API

Version 1.2.0 adds `TripHammerAutomationRegistry` for addons that need to
control the work performed by a trip hammer without replacing the TFC tick
method. Register a controller with a stable ID, priority, and `exclusive`
flag. Controllers return `Optional.empty()` when they do not claim a hit, or
`Optional.of(worked)` after claiming it. An exclusive controller suppresses
FastTripHammer's own auto-forging fallback while it is registered.

## Upgrade notes

The `1.2.0` change is additive and keeps the NeoForge, Minecraft, and TFC
dependency ranges unchanged. Without a registered exclusive controller,
`autoForge` behaves as in `1.1.0`. A controller receives a defensive copy of
the input stack and must not mutate trip-hammer rotation or cooldown state.
