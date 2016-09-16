package net.java.cargotracker.infrastructure.routing;

import fish.payara.micro.cdi.Outbound;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import net.java.cargotracker.application.util.reactive.CompletionStream;
import net.java.cargotracker.application.util.reactive.DirectCompletionStream;

@Dependent
class GraphTraversalResource {

    @Inject
    @Outbound
    private Event<GraphTraversalResource> request;
    
    private ConcurrentHashMap<Long,DirectCompletionStream> completionMap = new ConcurrentHashMap<>();
            
    public CompletionStream<TransitPath> get(String origin, String destination) {
        DirectCompletionStream<TransitPath> completion = new DirectCompletionStream<>();
        request.fire(new GraphTraversalResource());
        return completion;
    }

}
