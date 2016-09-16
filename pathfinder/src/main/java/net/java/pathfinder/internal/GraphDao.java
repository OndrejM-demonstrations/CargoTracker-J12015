package net.java.pathfinder.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GraphDao implements Serializable {

    private final Random random = new Random();

    /**
     * This method simulates an async DB call that completes the CompletinoStage in a new managed thread
     * @return
     */
    public CompletionStage<List<String>> listLocations() {
        return CompletableFuture.<List<String>>completedFuture(new ArrayList<>(Arrays.asList("CNHKG", "AUMEL", "SESTO",
                "FIHEL", "USCHI", "JNTKO", "DEHAM", "CNSHA", "NLRTM", "SEGOT",
                "CNHGH", "USNYC", "USDAL")));
    }

    public String getVoyageNumber(String from, String to) {
        int i = random.nextInt(5);

        switch (i) {
            case 0:
                return "0100S";
            case 1:
                return "0200T";
            case 2:
                return "0300A";
            case 3:
                return "0301S";
            default:
                return "0400S";
        }
    }
}
