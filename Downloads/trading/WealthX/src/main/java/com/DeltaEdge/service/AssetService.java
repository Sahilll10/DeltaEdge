
package com.DeltaEdge.service;

import com.DeltaEdge.model.Asset;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.User;
import java.util.List;

public interface AssetService {
    Asset createAsset(User user, Coin coin, double quantity);
    Asset getAssetById(Long assetId);
    Asset getAssetByUserIdAndId(Long userId, Long assetId);
    List<Asset> getUsersAssets(Long userId);
    Asset updateAsset(Long assetId, double quantity);
    Asset findAssetByUserIdAndCoinId(Long userId, String coinId);
    void deleteAsset(Long assetId);
}