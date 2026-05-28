# DeltaEdge

> **High-Frequency Trading & Graph-Based Risk Contagion Engine**
> 
> Developed by: Sahil Kumar (Roll: 3252)

DeltaEdge is a high-performance backend infrastructure designed for real-time cryptocurrency telemetry and systemic risk analysis. Unlike standard trading platforms, DeltaEdge treats the market as a **directed graph**, allowing for complex analysis of how a price drop in one asset propagates through correlated markets using non-linear search algorithms.

## 🧠 Engineered Features

### 1. Graph-Based Risk Contagion Engine
The core differentiator of DeltaEdge. The system maps coin correlations as a graph where:
* **Nodes:** Crypto assets.
* **Edges:** Weighted market correlations.
* **Algorithm:** Implements **Breadth-First Search (BFS)** to simulate market "contagion." When a source asset (e.g., Bitcoin) experiences a simulated crash, the engine calculates the cascading impact on secondary and tertiary assets based on their edge weights.

### 2. Resilience-First Market Ingestion
Built to withstand external API failures and network degradation:
* **Resilience4j Integration:** Protects the system from CoinGecko rate limits.
* **Autonomous Fallback:** If the external API fails, the **Circuit Breaker** triggers a fallback state, immediately serving the most recent valid market snapshot from the local H2/MySQL persistence layer.

### 3. Distributed Caching & Performance
* **Redis Layer:** Reduces latency by caching high-traffic paginated data and top-ranking market results with a 5-minute TTL.
* **Zero-Block Initialization:** A dedicated `DataInitializer` (CommandLineRunner) handles asynchronous data seeding on startup to ensure the engine is "warm" before the first user request.

### 4. Real-Time Telemetry & WebSockets
* **STOMP/WebSocket Protocol:** Pushes real-time price updates to connected clients without polling.
* **Atomic Order Execution:** Ensures idempotent trading operations to prevent race conditions during high-volatility events.

## 🏗️ System Architecture

```mermaid
graph TD
    %% Monochrome Technical Aesthetic
    classDef default fill:#fff,stroke:#000,stroke-width:1px,color:#000,shape:rect;
    classDef database fill:#eee,stroke:#000,stroke-width:1px,color:#000;

    User[Client] -- WebSocket/REST --> API[DeltaEdge Gateway]
    API --> RateLimiter[Redis Rate Limiter]
    
    subgraph Engine [Core Processing Engine]
        API --> GraphSvc[Graph Risk Analysis BFS]
        API --> MarketSvc[Market Telemetry]
        MarketSvc --> CB[Resilience4j Circuit Breaker]
    end

    CB -- Fetch --> External[CoinGecko API]
    CB -- Fallback --> H2[(Local Ledger)]
    
    MarketSvc --> RedisCache[(Redis Distributed Cache)]
    GraphSvc --> Logic[Contagion Calculation]
    
    class RedisCache,H2 database;
