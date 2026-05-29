package com.DeltaEdge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_edges")
public class MarketEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceCoinId;
    private String targetCoinId;
    private Double correlationWeight;
    private LocalDateTime lastUpdated;

    public MarketEdge() {
    }

    public MarketEdge(String sourceCoinId, String targetCoinId, Double correlationWeight) {
        this.sourceCoinId = sourceCoinId;
        this.targetCoinId = targetCoinId;
        this.correlationWeight = correlationWeight;
        this.lastUpdated = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSourceCoinId() {
        return sourceCoinId;
    }
    public void setSourceCoinId(String sourceCoinId) {
        this.sourceCoinId = sourceCoinId;
    }
    public String getTargetCoinId() {
        return targetCoinId;
    }
    public void setTargetCoinId(String targetCoinId) {
        this.targetCoinId = targetCoinId;
    }
    public Double getCorrelationWeight() {
        return correlationWeight;
    }
    public void setCorrelationWeight(Double correlationWeight) {
        this.correlationWeight = correlationWeight;
    }
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}