package net.java.pathfinder.api;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import net.java.pathfinder.internal.GraphDao;

@Stateless
@Path("/graph-traversal")
public class GraphTraversalService {

    @Inject
    private GraphDao dao;
    private final Random random = new Random();
    private static final long ONE_MIN_MS = 1000 * 60;
    private static final long ONE_DAY_MS = ONE_MIN_MS * 60 * 24;

    Logger logger = Logger.getLogger(GraphTraversalService.class.getCanonicalName());

    @GET
    @Path("/shortest-path")
    @Produces({"application/json", "application/xml; qs=.75"})
    // TODO Add internationalized messages for constraints.
    public void findShortestPath(
            @Suspended AsyncResponse asyncResponse,
            @NotNull @Size(min = 5, max = 5) @QueryParam("origin") final String originUnLocode,
            @NotNull @Size(min = 5, max = 5) @QueryParam("destination") final String destinationUnLocode,
            @QueryParam("deadline") final String deadline) {

        dao.listLocations()
                .thenApply((List<String> allVertices) -> {
                    Date date = nextDate(new Date());
                    allVertices.remove(originUnLocode);
                    allVertices.remove(destinationUnLocode);

                    int candidateCount = getRandomNumberOfCandidates();
                    List<TransitPath> candidates = new ArrayList<>(
                            candidateCount);

                    FindShortestPathVars vars = new FindShortestPathVars();
                    vars.date = date;
                    vars.allVertices = allVertices;
                    CompletableFuture<Void> cf = new CompletableFuture<>();
                    cf.complete(null);
                    
                    for (int counter = 0; counter < candidateCount; counter++) {
                        cf = cf.thenCombine(CompletableFuture.completedFuture(
                            counter), 
                            (v, i) -> {    
                                vars.allVertices = getRandomChunkOfLocations(vars.allVertices);
                                List<TransitEdge> transitEdges = new ArrayList<>(
                                        allVertices.size() - 1);
                                String firstLegTo = allVertices.get(0);

                                vars.fromDate = nextDate(vars.date);
                                vars.toDate = nextDate(vars.fromDate);
                                vars.date = nextDate(vars.toDate);

                                dao.getVoyageNumber(originUnLocode, firstLegTo)
                                    .thenAccept(
                                        voyageNumber -> {
                                            transitEdges.add(new TransitEdge(
                                                voyageNumber, originUnLocode, firstLegTo,
                                                vars.fromDate, vars.toDate));
                                        })
                                    .toCompletableFuture().join();

                                for (int j = 0; j < allVertices.size() - 1; j++) {
                                    String current = allVertices.get(j);
                                    String next = allVertices.get(j + 1);
                                    vars.fromDate = nextDate(vars.date);
                                    vars.toDate = nextDate(vars.fromDate);
                                    vars.date = nextDate(vars.toDate);
                                    dao.getVoyageNumber(current, next)
                                        .thenAccept(
                                            voyageNumber -> {
                                                transitEdges.add(new TransitEdge(
                                                    voyageNumber, current, next, 
                                                        vars.fromDate, vars.toDate));
                                            })
                                        .toCompletableFuture().join();
                                }

                                String lastLegFrom = allVertices.get(allVertices.size() - 1);
                                vars.fromDate = nextDate(vars.date);
                                vars.toDate = nextDate(vars.fromDate);
                                dao.getVoyageNumber(lastLegFrom, destinationUnLocode)
                                    .thenAcceptBoth(CompletableFuture.completedFuture(vars),
                                    (voyageNumber, dates) -> {
                                        transitEdges.add(new TransitEdge(
                                            voyageNumber, lastLegFrom, destinationUnLocode, 
                                                dates.fromDate, dates.toDate));
                                    })
                                    .toCompletableFuture().join();

                                candidates.add(new TransitPath(transitEdges));
                                return null;
                            });
                    }

                    logger.info("Path Finder Service called for " + originUnLocode + " to " + destinationUnLocode);

                    return candidates;
                })
                .thenApply(response -> asyncResponse.resume(response))
                .exceptionally(exception -> asyncResponse.resume(exception));
    }

    private static class FindShortestPathVars {

        public Date fromDate;
        public Date toDate;
        public Date date;
        public List<String> allVertices;

        public FindShortestPathVars() {
        }

        public FindShortestPathVars(Date fromDate, Date toDate, Date date, List<String> allVertices) {
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.date = date;
            this.allVertices = allVertices;
        }

    }

    private Date nextDate(Date date) {
        return new Date(date.getTime() + ONE_DAY_MS
                + (random.nextInt(1000) - 500) * ONE_MIN_MS);
    }

    private int getRandomNumberOfCandidates() {
        return 3 + random.nextInt(3);
    }

    private List<String> getRandomChunkOfLocations(List<String> allLocations) {
        Collections.shuffle(allLocations);
        int total = allLocations.size();
        int chunk = total > 4 ? 1 + new Random().nextInt(5) : total;
        return allLocations.subList(0, chunk);
    }
}
