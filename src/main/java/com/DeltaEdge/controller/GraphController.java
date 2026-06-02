package com.DeltaEdge.controller;

import com.DeltaEdge.model.MarketEdge;
import com.DeltaEdge.service.GraphAnalysisService;
import com.DeltaEdge.service.GraphDataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*")
public class GraphController {

    @Autowired
    private GraphAnalysisService graphAnalysisService;

    @Autowired
    private GraphDataSyncService graphDataSyncService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncGraph() {
        // Runs in the background so the UI doesn't freeze and throw a 500 timeout!
        CompletableFuture.runAsync(() -> {
            try {
                graphDataSyncService.buildGraphFromCoinGecko();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ResponseEntity.ok(Collections.singletonMap("message", "Graph sync started in background"));
    }

    @GetMapping("/edges")
    public ResponseEntity<List<MarketEdge>> getEdges() {
        return ResponseEntity.ok(graphAnalysisService.getGraphEdges());
    }

    @GetMapping("/contagion/{coinId}")
    public ResponseEntity<Map<String, Object>> getContagion(@PathVariable String coinId) {
        Map<String, Double> impactMap = graphAnalysisService.calculateContagionRisk(coinId, 10.0);

        List<Map<String, Object>> affectedNodes = new ArrayList<>();
        for (Map.Entry<String, Double> entry : impactMap.entrySet()) {
            if (entry.getKey().equals(coinId)) continue; // Skip mapping the origin coin to itself

            Map<String, Object> node = new HashMap<>();
            node.put("coinId", entry.getKey());
            node.put("coinName", entry.getKey().toUpperCase());

            // Dynamic node risk (0-100) based on attenuation
            double impact = entry.getValue();
            int nodeRisk = (int) Math.min(100, Math.max(10, impact * 10));
            node.put("riskScore", nodeRisk);

            // Dynamic BFS Depth based on impact drop-off
            int level = impact >= 7.0 ? 1 : (impact >= 4.0 ? 2 : 3);
            node.put("level", level);

            affectedNodes.add(node);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("affected", affectedNodes);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/risk-score/{coinId}")
    public ResponseEntity<Map<String, Object>> getRiskScore(@PathVariable String coinId) {
        // Calculate global risk based on how many edges this coin influences
        Map<String, Double> impactMap = graphAnalysisService.calculateContagionRisk(coinId, 10.0);

        int baseScore = 30;
        int dynamicScore = Math.min(100, baseScore + (impactMap.size() * 12)); // Scaled by influence

        String desc = dynamicScore >= 80 ? "CRITICAL: High systemic contagion risk. Highly correlated with broad market." :
                dynamicScore >= 60 ? "HIGH: Significant market influence detected." :
                        dynamicScore >= 40 ? "MEDIUM: Moderate correlation to other assets." :
                                "LOW: Isolated asset. Low contagion risk.";

        Map<String, Object> response = new HashMap<>();
        response.put("score", dynamicScore);
        response.put("description", desc);
        return ResponseEntity.ok(response);
    }
}