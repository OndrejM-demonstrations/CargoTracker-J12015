package net.java.cargotracker.application.util.reactive;

import java.util.concurrent.Callable;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
@Transactional
public class TransactionalCaller {

    public <V> V call(Callable<V> r) {
        try {
            return r.call();
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }
}
