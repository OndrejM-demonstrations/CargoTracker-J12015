package net.java.cargotracker.application.util.reactive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class DirectCompletionStream<ITEM_TYPE> implements CompletionStream<ITEM_TYPE> {
    private final CompletableFuture<Consumer<CompletionStage<ITEM_TYPE>>> initialItemCF = new CompletableFuture<>();
    private CompletionStage<Consumer<CompletionStage<ITEM_TYPE>>> lastItemCF = initialItemCF;
    private final CompletableFuture<Void> finishedCF = new CompletableFuture<>();
    
    @Override
    public CompletionStream<ITEM_TYPE> acceptEach(Consumer<CompletionStage<ITEM_TYPE>> consumer) {
        initialItemCF.complete(consumer);
        return this;
    }

    @Override
    public CompletionStage<Void> whenFinished() {
        return finishedCF;
    }
    
    public void itemProcessed(ITEM_TYPE item) {
        lastItemCF = lastItemCF.thenApply(consumer -> {
           consumer.accept(CompletableFuture.completedFuture(item));
           return consumer;
        });
    }
    
    public void processingFinished() {
        finishedCF.complete(null);
    }

}
