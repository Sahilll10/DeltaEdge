package com.DeltaEdge.service;

import com.DeltaEdge.exception.ResourceNotFoundException;
import com.DeltaEdge.model.Asset;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.User;
import com.DeltaEdge.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Override
    public Asset createAsset(User user, Coin coin, double quantity) {
        Asset asset = new Asset();
        asset.setUser(user);
        asset.setCoin(coin);
        asset.setQuantity(quantity);
        asset.setBuyPrice(coin.getCurrentPrice());
        return assetRepository.save(asset);
    }

    @Override
    public Asset getAssetById(Long assetId) {
        // Fintech Fix: Use custom RuntimeException for automatic transaction rollback
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));
    }

    @Override
    public Asset getAssetByUserIdAndId(Long userId, Long assetId) {
        // You can implement a custom query in repository for this if needed for security checks
        return assetRepository.findById(assetId)
                .filter(asset -> asset.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for this user"));
    }

    @Override
    public List<Asset> getUsersAssets(Long userId) {
        return assetRepository.findByUserId(userId);
    }

    @Override
    public Asset updateAsset(Long assetId, double quantity) {
        Asset oldAsset = getAssetById(assetId);
        // The quantity passed can be positive (for BUY) or negative (for SELL)
        oldAsset.setQuantity(oldAsset.getQuantity() + quantity);
        return assetRepository.save(oldAsset);
    }

    @Override
    public Asset findAssetByUserIdAndCoinId(Long userId, String coinId) {
        return assetRepository.findByUserIdAndCoinId(userId, coinId);
    }

    @Override
    public void deleteAsset(Long assetId) {
        assetRepository.deleteById(assetId);
    }
}