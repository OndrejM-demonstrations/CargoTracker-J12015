package net.java.cargotracker.interfaces.booking.socket;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/routeCandidates")
public class RouteCandidatesResource {

    @OnMessage(encoders = {JSONEncoder.class})
    public String onMessage(String count) {
        return count;
    }

}
