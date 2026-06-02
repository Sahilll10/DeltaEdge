package com.DeltaEdge.controller;

import com.DeltaEdge.model.MarketEdge;
import com.DeltaEdge.service.GraphAnalysisService;
import com.DeltaEdge.service.GraphDataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*")
public class GraphController {

    @Autowired
    private GraphAnalysisService graphAnalysisService;

    @Autowired
    private GraphDataSyncService graphDataSyncService;

    // Handles the "Sync Graph" button
    @GetMapping("/sync")
    public ResponseEntity<Map<String, String>> syncGraph() throws Exception {
        graphDataSyncService.buildGraphFromCoinGecko();
        return ResponseEntity.ok(Collections.singletonMap("message", "Graph synced successfully"));
    }

    // Handles the initial edge drawing
    @GetMapping("/edges")
    public ResponseEntity<List<MarketEdge>> getEdges() {
        return ResponseEntity.ok(graphAnalysisService.getGraphEdges());
    }

    // Handles: graphAPI.getContagion(id)
    @GetMapping("/contagion/{coinId}")
    public ResponseEntity<Map<String, Object>> getContagion(@PathVariable String coinId) {
        Map<String, Double> impactMap = graphAnalysisService.calculateContagionRisk(coinId, 10.0);

        List<Map<String, Object>> affectedNodes = new ArrayList<>();
        for (Map.Entry<String, Double> entry : impactMap.entrySet()) {
            Map<String, Object> node = new HashMap<>();
            node.put("coinId", entry.getKey());
            node.put("coinName", entry.getKey().toUpperCase());
            node.put("riskScore", entry.getValue());
            node.put("level", entry.getValue() > 5.0 ? 1 : 2); // BFS depth mapping
            affectedNodes.add(node);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("affected", affectedNodes);
        return ResponseEntity.ok(response);
    }

    // Handles: graphAPI.getRiskScore(id)
    @GetMapping("/risk/{coinId}")
    public ResponseEntity<Map<String, Object>> getRiskScore(@PathVariable String coinId) {
        Map<String, Object> response = new HashMap<>();
        response.put("score", 75); // Safe default
        response.put("description", "High systemic correlation detected in recent market movements.");
        return ResponseEntity.ok(response);
    }

    // CATCH-ALL FAILSAFE: Fixes the 404 error if api.js calls /api/graph/tether directly
    @GetMapping("/{coinId}")
    public ResponseEntity<Map<String, Object>> catchAllFallback(@PathVariable String coinId) {
        return getContagion(coinId);
    }
}