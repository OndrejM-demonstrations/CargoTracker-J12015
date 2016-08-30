package net.java.cargotracker.application.util.reactive;

import javax.enterprise.concurrent.ContextService;

public interface ThreadAsyncContext {

    public static ThreadAsyncContext create(ContextService ctxService) {
        return ctxService.createContextualProxy(new ThreadAsyncContextWrapper(), ThreadAsyncContext.class);
    }

    public void run(Runnable r);
    
}
