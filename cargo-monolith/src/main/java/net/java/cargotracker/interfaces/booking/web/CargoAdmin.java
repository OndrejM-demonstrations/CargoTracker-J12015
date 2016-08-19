package net.java.cargotracker.interfaces.booking.web;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import net.java.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import net.java.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import net.java.cargotracker.interfaces.booking.facade.dto.Leg;
import net.java.cargotracker.interfaces.booking.facade.dto.Location;
import net.java.cargotracker.interfaces.booking.facade.dto.RouteCandidate;

/**
 * Handles cargo booking and routing. Operates against a dedicated service
 * facade, and could easily be rewritten as a thick Swing client. Completely
 * separated from the domain layer, unlike the tracking user interface.
 * <p/>
 * In order to successfully keep the domain model shielded from user interface
 * considerations, this approach is generally preferred to the one taken in the
 * tracking controller. However, there is never any one perfect solution for all
 * situations, so we've chosen to demonstrate two polarized ways to build user
 * interfaces.
 *
 * @see net.java.cargotracker.interfaces.tracking.CargoTrackingController
 */
@Named
@RequestScoped
public class CargoAdmin {

    private List<Location> locations;
    private List<String> unlocodes;
    private List<CargoRoute> cargos;
    private Date arrivalDeadline;
    private String originUnlocode;
    private String destinationUnlocode;
    private String trackingId;
    private List<Leg> legs;
    @Inject
    private BookingServiceFacade bookingServiceFacade;

    public List<Location> getLocations() {
        return locations;
    }

    public List<String> getUnlocodes() {
        return unlocodes;
    }

    public List<CargoRoute> getCargos() {
        return cargos;
    }

    public Date getArrivalDeadline() {
        return arrivalDeadline;
    }

    public void setArrivalDeadline(Date arrivalDeadline) {
        this.arrivalDeadline = arrivalDeadline;
    }

    public String getOriginUnlocode() {
        return originUnlocode;
    }

    public void setOriginUnlocode(String originUnlocode) {
        this.originUnlocode = originUnlocode;
    }

    public String getDestinationUnlocode() {
        return destinationUnlocode;
    }

    public void setDestinationUnlocode(String destinationUnlocode) {
        this.destinationUnlocode = destinationUnlocode;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    @PostConstruct
    public void init() {
        locations = bookingServiceFacade.listShippingLocations();
        unlocodes = new ArrayList<>();

        for (Location location : locations) {
            unlocodes.add(location.getUnLocode());
        }

        cargos = bookingServiceFacade.listAllCargos();
    }

    public String register() {
        String trackingId = bookingServiceFacade.bookNewCargo(
                originUnlocode, destinationUnlocode, arrivalDeadline);
        return "show.html?trackingId=" + trackingId;
    }

    public CargoRoute getCargo() {
        return bookingServiceFacade.loadCargoForRouting(trackingId);
    }

    private static class CompletionSequence<T> {
        public final T result;
        public final CompletionStage<CompletionSequence<?>> next;

        public CompletionSequence(T result, CompletionStage<CompletionSequence<?>> next) {
            this.result = result;
            this.next = next;
        }
        
    }
    
    public List<RouteCandidate> getRouteCanditates() {
        List<RouteCandidate> result = new ArrayList<>();
        getRouteCanditates(v -> result.add(v), () -> {});
        
        
        new CompletableFuture<CompletionSequence<RouteCandidate>>()
                .thenComposeAsync(cSeq -> {
                    result.add(cSeq.result);
                    return cSeq.next;
                    }, executor
                );
        return result;
    }

    @Inject
    private ExecutorService executor;
    
    /**
     * A non-blocking API - consumer should update client in asynchronous way, e.g. using a web socket.
     * Everything is called in the same thread now, we may expect to execute consumers in separate threads in the future.
     * @param consumer Called for each route candidate
     * @param finihed Called after last rout candidate to indicate the end of list
     */
    public void getRouteCanditates(Consumer<RouteCandidate> consumer, Runnable finihed) {
        bookingServiceFacade.requestPossibleRoutesForCargo(trackingId).stream()
                .forEach(consumer);
        finihed.run();
    }

    public String assignItinerary() {
        RouteCandidate selectedRoute = new RouteCandidate(legs);
        bookingServiceFacade.assignCargoToRoute(trackingId, selectedRoute);

        return "show.html?trackingId=" + trackingId;
    }

    public String changeDestination() {
        bookingServiceFacade.changeDestination(trackingId, destinationUnlocode);

        return "show.html?trackingId=" + trackingId;
    }
}