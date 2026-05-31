package com.DeltaEdge.controller;

import com.DeltaEdge.service.GraphAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    @Autowired
    private GraphAnalysisService graphAnalysisService;
    @GetMapping("/risk")
    public ResponseEntity<Map<String, Double>> getContagionRisk(
            @RequestParam String source,
            @RequestParam double drop) {

        Map<String, Double> impact = graphAnalysisService.calculateContagionRisk(source, drop);
        return ResponseEntity.ok(impact);
    }
    @GetMapping("/clusters")
    public ResponseEntity<List<List<String>>> getMarketClusters() {
        List<List<String>> clusters = graphAnalysisService.findMarketClusters();
        return ResponseEntity.ok(clusters);
    }
    @Autowired
    private com.DeltaEdge.service.GraphDataSyncService graphDataSyncService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncGraphData() {
        try {
            new Thread(() -> {
                try {
                    graphDataSyncService.buildGraphFromCoinGecko();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            return ResponseEntity.ok("Graph synchronization started in the background." +
                                           " Check IntelliJ console for logs.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sync failed: " + e.getMessage());
        }
    }

    // FIX: Added the missing /edges endpoint requested by the frontend
    @GetMapping("/edges")
    public ResponseEntity<?> getGraphEdges() {
        // Assuming your GraphAnalysisService has a method to fetch the graph edges.
        // If the method is named differently in your service, just update this line!
        Object edges = graphAnalysisService.getGraphEdges();
        return ResponseEntity.ok(edges);
    }
}