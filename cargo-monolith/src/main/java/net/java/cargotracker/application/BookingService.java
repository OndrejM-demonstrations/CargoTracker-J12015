package net.java.cargotracker.application;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;
import net.java.cargotracker.application.util.reactive.CompletionStream;
import net.java.cargotracker.domain.model.cargo.Itinerary;
import net.java.cargotracker.domain.model.cargo.TrackingId;
import net.java.cargotracker.domain.model.location.UnLocode;

/**
 * Cargo booking service.
 */
public interface BookingService {

    /**
     * Registers a new cargo in the tracking system, not yet routed.
     */
    TrackingId bookNewCargo(UnLocode origin, UnLocode destination, Date arrivalDeadline);

    /**
     * Requests a list of itineraries describing possible routes for this cargo.
     *
     * @param trackingId cargo tracking id
     * @return A list of possible itineraries for this cargo
     */
    CompletionStream<Itinerary> requestPossibleRoutesForCargo(TrackingId trackingId);

    void assignCargoToRoute(Itinerary itinerary, TrackingId trackingId);

    void changeDestination(TrackingId trackingId, UnLocode unLocode);
}
