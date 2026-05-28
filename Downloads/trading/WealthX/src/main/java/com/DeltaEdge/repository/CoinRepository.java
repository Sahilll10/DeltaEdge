package com.DeltaEdge.repository;

import com.DeltaEdge.model.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinRepository extends JpaRepository<Coin, String> {

}
