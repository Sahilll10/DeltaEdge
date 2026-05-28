# DeltaEdge

> **Algorithmic Trading & Risk Contagion Engine**
> 
> Developed by Sahil Kumar (Roll: 3252)

DeltaEdge is a high-performance backend trading engine built to analyze cryptocurrency market telemetry, execute trades, and calculate systemic risk contagion. It leverages a distributed caching layer and circuit breakers to ensure fault tolerance against external API rate limits and network degradation.

## ⚙️ Core Architecture

The system is built on Java 21 and Spring Boot, utilizing a hybrid persistence layer (Redis for volatile market data, JPA/Hibernate for ACID-compliant ledger transactions).

```mermaid
graph TD
    %% Styling for a clean, monochrome, non-curved aesthetic
    classDef default fill:#fff,stroke:#000,stroke-width:1px,color:#000,shape:rect;
    classDef database fill:#eee,stroke:#000,stroke-width:1px,color:#000;
    
    Client[Client Request] --> API[Spring Boot REST API]
    API --> RateLimiter[Redis Rate Limiter]
    RateLimiter --> Service[Core Services]
    
    Service --> Cache{Redis Cache}
    Cache -- Cache Hit --> Service
    Cache -- Cache Miss --> CB[Resilience4j Circuit Breaker]
    
    CB --> ExternalAPI[External Market API]
    ExternalAPI -- Success --> DB[(Local Ledger)]
    CB -- Fallback --> DB
    
    Service --> WebSockets[Real-Time Price Feed]
    
    class Cache,DB database;
