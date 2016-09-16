package net.java.cargotracker.application.util.reactive;

import java.util.function.Supplier;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
@Transactional
public class TransactionalCaller {

    public <V> V call(Supplier<V> r) {
        return r.get();
    }

    public void call(Runnable r) {
        r.run();
    }
}
