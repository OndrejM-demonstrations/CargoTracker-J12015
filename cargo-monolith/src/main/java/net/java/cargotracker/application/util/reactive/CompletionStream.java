package net.java.cargotracker.application.util.reactive;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public interface CompletionStream<ITEM_TYPE> {

    /**
     * Add a callback to execute for each item once it is available. 
     * This method should be called only once. Subsequent callbacks are ignored.
     * Callback accepts a CompletionStage to handle both success and failure. 
     * In case of success, the CompletionStage is completed normally.
     * In case of a failure, the CompletionStage is completed exceptionally.
     * @param consumer
     * @return
     */
    public CompletionStream<ITEM_TYPE> acceptEach(Consumer<CompletionStage<ITEM_TYPE>> consumer);

}
