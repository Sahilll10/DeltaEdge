package com.DeltaEdge.repository;

import com.DeltaEdge.model.MarketEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarketEdgeRepository extends JpaRepository<MarketEdge, Long> {

    // Used for BFS: Find all outgoing edges from a specific coin
    List<MarketEdge> findBySourceCoinId(String sourceCoinId);

    // Used to check if an edge already exists so we can update it instead of creating duplicates
    MarketEdge findBySourceCoinIdAndTargetCoinId(String sourceCoinId, String targetCoinId);
}