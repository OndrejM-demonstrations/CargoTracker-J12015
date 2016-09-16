package net.java.cargotracker.application.util.demo;

import fish.payara.micro.cdi.ClusteredCDIEventBus;
import javax.enterprise.context.*;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Dependent
public class CDIBusInitializer {
    
    @Inject
    private ClusteredCDIEventBus eventBus;
    
    public void initialize(@Observes @Initialized(ApplicationScoped.class) Object event) {
        eventBus.initialize();
    }
}
