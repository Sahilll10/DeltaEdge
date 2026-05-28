package com.DeltaEdge.service;

import com.DeltaEdge.model.MarketEdge;
import com.DeltaEdge.repository.MarketEdgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorrelationService {

    @Autowired
    private MarketEdgeRepository marketEdgeRepository;
    public double calculatePearsonCorrelation(List<Double> pricesA, List<Double> pricesB) {
        if (pricesA == null || pricesB == null || pricesA.size() != pricesB.size() || pricesA.isEmpty()) {
            throw new IllegalArgumentException("Price lists must be of equal, non-zero length.");
        }
        int n=pricesA.size();
        double sumA=0.0, sumB=0.0,sumSqA=0.0,sumSqB=0.0,sumProduct=0.0;

        for (int i=0;i<n;i++) {
            double a=pricesA.get(i);
            double b=pricesB.get(i);
            sumA+=a;
            sumB+=b;
            sumSqA+= a * a;
            sumSqB+= b * b;
            sumProduct+= a * b;
        }
        double numerator=(n*sumProduct)-(sumA * sumB);
        double denominator=Math.sqrt(((n*sumSqA)-(sumA*sumA))*((n*sumSqB)-(sumB * sumB)));

        if(denominator==0) return 0.0;
        return numerator/denominator;
    }
    public void updateEdge(String sourceId, String targetId, List<Double> pricesA, List<Double> pricesB) {
        double correlation = calculatePearsonCorrelation(pricesA, pricesB);
        if (Math.abs(correlation)>=0.6) {
            MarketEdge edge=marketEdgeRepository.findBySourceCoinIdAndTargetCoinId(sourceId, targetId);
            if (edge==null) {
                edge=new MarketEdge(sourceId, targetId, correlation);
            } else{
                edge.setCorrelationWeight(correlation);
                edge.setLastUpdated(java.time.LocalDateTime.now());
            }
            marketEdgeRepository.save(edge);
        }
    }
}