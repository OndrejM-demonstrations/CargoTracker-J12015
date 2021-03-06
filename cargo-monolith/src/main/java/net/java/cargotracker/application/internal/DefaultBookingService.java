package net.java.cargotracker.application.internal;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import net.java.cargotracker.application.BookingService;
import net.java.cargotracker.application.util.demo.Slow;
import net.java.cargotracker.application.util.reactive.CompletionStream;
import net.java.cargotracker.application.util.reactive.DirectCompletionStream;
import net.java.cargotracker.domain.model.cargo.Cargo;
import net.java.cargotracker.domain.model.cargo.CargoRepository;
import net.java.cargotracker.domain.model.cargo.Itinerary;
import net.java.cargotracker.domain.model.cargo.RouteSpecification;
import net.java.cargotracker.domain.model.cargo.TrackingId;
import net.java.cargotracker.domain.model.location.Location;
import net.java.cargotracker.domain.model.location.LocationRepository;
import net.java.cargotracker.domain.model.location.UnLocode;
import net.java.cargotracker.domain.service.RoutingService;

@Stateless
public class DefaultBookingService implements BookingService {

    @Inject
    @Slow
    private CargoRepository cargoRepository;
    @Inject
    private LocationRepository locationRepository;
    @Inject
    private RoutingService routingService;
    @Resource
    private ManagedExecutorService executor;
    // TODO See if the logger can be injected.
    private static final Logger logger = Logger.getLogger(
            DefaultBookingService.class.getName());

    @Override
    public TrackingId bookNewCargo(UnLocode originUnLocode,
            UnLocode destinationUnLocode,
            Date arrivalDeadline) {
        TrackingId trackingId = cargoRepository.nextTrackingId();
        Location origin = locationRepository.find(originUnLocode);
        Location destination = locationRepository.find(destinationUnLocode);
        RouteSpecification routeSpecification = new RouteSpecification(origin,
                destination, arrivalDeadline);

        Cargo cargo = new Cargo(trackingId, routeSpecification);

        cargoRepository.store(cargo);
        logger.log(Level.INFO, "Booked new cargo with tracking id {0}",
                cargo.getTrackingId().getIdString());

        return cargo.getTrackingId();
    }

    @Override
    public CompletionStream<Itinerary> requestPossibleRoutesForCargo(TrackingId trackingId) {
        DirectCompletionStream<Itinerary> completion = new DirectCompletionStream<>();
        CompletableFuture
        .supplyAsync(() -> {
            return cargoRepository.find(trackingId);
        }, executor)
        .thenAccept(cargo -> {
            if (cargo == null) {
                completion.processingFinished();
            } else {
                routingService.fetchRoutesForSpecification(cargo.getRouteSpecification())
                    .acceptEach(stage -> {
                        stage.thenAccept(completion::itemProcessed);
                    })
                    .whenFinished()
                        .thenRun(completion::processingFinished);
            }
        });
        return completion;
    }

    @Override
    public void assignCargoToRoute(Itinerary itinerary, TrackingId trackingId) {
        Cargo cargo = cargoRepository.find(trackingId);

        cargo.assignToRoute(itinerary);
        cargoRepository.store(cargo);

        logger.log(Level.INFO, "Assigned cargo {0} to new route", trackingId);
    }

    @Override
    public void changeDestination(TrackingId trackingId, UnLocode unLocode) {
        Cargo cargo = cargoRepository.find(trackingId);
        Location newDestination = locationRepository.find(unLocode);

        RouteSpecification routeSpecification = new RouteSpecification(
                cargo.getOrigin(), newDestination,
                cargo.getRouteSpecification().getArrivalDeadline());
        cargo.specifyNewRoute(routeSpecification);

        cargoRepository.store(cargo);

        logger.log(Level.INFO, "Changed destination for cargo {0} to {1}",
                new Object[]{trackingId, routeSpecification.getDestination()});
    }
}
