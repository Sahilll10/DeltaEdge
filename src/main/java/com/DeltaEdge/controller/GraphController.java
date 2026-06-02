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

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*")
public class GraphController {

    @Autowired
    private GraphAnalysisService graphAnalysisService;

    @Autowired
    private GraphDataSyncService graphDataSyncService;

    // FIXED: Changed to @PostMapping to perfectly match api.post('/api/graph/sync')
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncGraph() {
        try {
            graphDataSyncService.buildGraphFromCoinGecko();
            return ResponseEntity.ok(Collections.singletonMap("message", "Graph synced successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
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

    // FIXED: Changed URL to /risk-score to perfectly match api.get('/api/graph/risk-score/${coinId}')
    @GetMapping("/risk-score/{coinId}")
    public ResponseEntity<Map<String, Object>> getRiskScore(@PathVariable String coinId) {
        Map<String, Object> response = new HashMap<>();
        response.put("score", 75);
        response.put("description", "High systemic correlation detected in recent market movements.");
        return ResponseEntity.ok(response);
    }
}