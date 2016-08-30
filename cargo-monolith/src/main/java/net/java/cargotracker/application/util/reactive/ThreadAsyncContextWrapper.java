package net.java.cargotracker.application.util.reactive;

class ThreadAsyncContextWrapper implements ThreadAsyncContext {

    @Override
    public void run(Runnable r) {
        r.run();
    }

}
