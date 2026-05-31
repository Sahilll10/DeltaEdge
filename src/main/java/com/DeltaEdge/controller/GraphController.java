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

    @Autowired
    private com.DeltaEdge.service.GraphDataSyncService graphDataSyncService;

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

            return ResponseEntity.ok("Graph synchronization started in the background.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sync failed: " + e.getMessage());
        }
    }

    @GetMapping("/edges")
    public ResponseEntity<?> getGraphEdges() {
        Object edges = graphAnalysisService.getGraphEdges();
        return ResponseEntity.ok(edges);
    }
    @GetMapping("/{coinId}")
    public ResponseEntity<?> getSpecificCoinGraphData(@PathVariable String coinId) {
        return ResponseEntity.ok(Map.of("coin", coinId, "status", "synced"));
    }
}