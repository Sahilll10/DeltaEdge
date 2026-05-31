package com.DeltaEdge.service;

import com.DeltaEdge.model.MarketEdge;
import com.DeltaEdge.repository.MarketEdgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphAnalysisService {

    @Autowired
    private MarketEdgeRepository marketEdgeRepository;
    private Map<String, List<MarketEdge>> buildAdjacencyList() {
        List<MarketEdge> allEdges = marketEdgeRepository.findAll();
        Map<String, List<MarketEdge>> adjList = new HashMap<>();

        for (MarketEdge edge : allEdges) {
            adjList.computeIfAbsent(edge.getSourceCoinId(), k -> new ArrayList<>()).add(edge);
            MarketEdge reverseEdge = new MarketEdge(edge.getTargetCoinId(), edge.getSourceCoinId(), edge.getCorrelationWeight());
            adjList.computeIfAbsent(edge.getTargetCoinId(), k -> new ArrayList<>()).add(reverseEdge);
        }
        return adjList;
    }

    /**
     * ALGORITHM 1: Weighted Breadth-First Search (BFS) for Risk Contagion.
     * Predicts how a price drop in one coin propagates through the market.
     * Time Complexity: O(V + E)
     */
    public Map<String, Double> calculateContagionRisk(String sourceCoinId, double initialDropPercentage) {
        Map<String, List<MarketEdge>> adjList = buildAdjacencyList();
        Map<String, Double> impactMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        // Start BFS
        queue.add(sourceCoinId);
        impactMap.put(sourceCoinId, initialDropPercentage);

        while (!queue.isEmpty()) {
            String currentCoin = queue.poll();
            double currentDrop = impactMap.get(currentCoin);

            List<MarketEdge> neighbors = adjList.getOrDefault(currentCoin, new ArrayList<>());

            for (MarketEdge edge : neighbors) {
                String neighborCoin = edge.getTargetCoinId();
                double weight = edge.getCorrelationWeight();

                // Only propagate if they are positively correlated
                if (weight > 0.5) {
                    double propagatedDrop = currentDrop * weight;

                    // If the neighbor hasn't been hit yet, OR this path causes a worse drop
                    if (!impactMap.containsKey(neighborCoin) || impactMap.get(neighborCoin) < propagatedDrop) {
                        // Only care about drops greater than 1% to avoid infinite micro-ripples
                        if (propagatedDrop > 1.0) {
                            impactMap.put(neighborCoin, propagatedDrop);
                            queue.add(neighborCoin);
                        }
                    }
                }
            }
        }
        return impactMap;
    }

    /**
     * ALGORITHM 2: Depth-First Search (DFS) for Connected Components.
     * Finds "clusters" of coins that move together.
     * Time Complexity: O(V + E)
     */
    public List<List<String>> findMarketClusters() {
        Map<String, List<MarketEdge>> adjList = buildAdjacencyList();
        Set<String> visited = new HashSet<>();
        List<List<String>> clusters = new ArrayList<>();

        for (String coin : adjList.keySet()) {
            if (!visited.contains(coin)) {
                List<String> currentCluster = new ArrayList<>();
                exploreClusterDFS(coin, adjList, visited, currentCluster);

                // Only consider it a cluster if it has more than 1 coin
                if (currentCluster.size() > 1) {
                    clusters.add(currentCluster);
                }
            }
        }
        return clusters;
    }

    private void exploreClusterDFS(String coin, Map<String, List<MarketEdge>> adjList, Set<String> visited, List<String> currentCluster) {
        visited.add(coin);
        currentCluster.add(coin);

        for (MarketEdge edge : adjList.getOrDefault(coin, new ArrayList<>())) {
            if (edge.getCorrelationWeight() >= 0.8 && !visited.contains(edge.getTargetCoinId())) {
                exploreClusterDFS(edge.getTargetCoinId(), adjList, visited, currentCluster);
            }
        }
    }

    public List<MarketEdge> getGraphEdges() {
        return marketEdgeRepository.findAll();
    }
}