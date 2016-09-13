package net.java.cargotracker.application.util.reactive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class DirectCompletionStream<ITEM_TYPE> implements CompletionStream<ITEM_TYPE> {
    private final CompletableFuture<Consumer<CompletionStage<ITEM_TYPE>>> initialCF = new CompletableFuture<>();
    private CompletionStage<Consumer<CompletionStage<ITEM_TYPE>>> lastCF = initialCF;
    
    @Override
    public CompletionStream<ITEM_TYPE> acceptEach(Consumer<CompletionStage<ITEM_TYPE>> consumer) {
        initialCF.complete(consumer);
        return this;
    }

    public void itemProcessed(ITEM_TYPE item) {
        lastCF = lastCF.thenApply(consumer -> {
           consumer.accept(CompletableFuture.completedFuture(item));
           return consumer;
        });
    }
    
}
