# ⚡ DeltaEdge: Algorithmic Trading Engine

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

DeltaEdge is a full-stack, cloud-deployed cryptographic trading simulator and market analysis engine. Built with a resilient microservice-oriented architecture, it provides real-time market data, secure dual-entry wallet management, and robust order execution capabilities.

### 🔗 Live Links
* **Live Dashboard (Frontend):** [Insert your Vercel Link here]
* **API Engine (Backend):** [Insert your Render Link here]

---

## 🏗️ System Architecture

DeltaEdge is built on a distributed, resilient architecture designed to handle rate limits and maintain data integrity.

* **Frontend Layer:** React.js Single Page Application (SPA) utilizing Vite for rapid bundling, styled with custom CSS and Lucide React icons.
* **API Gateway & Core:** Spring Boot 3.x REST API handling authentication, business logic, and routing.
* **Authentication:** Stateless JWT (JSON Web Token) implementation utilizing 256-bit HMAC-SHA secure keys via Spring Security.
* **Primary Persistence:** PostgreSQL database handling User state, dual-entry Wallet transactions, and Order histories.
* **Market Data & Caching:** Real-time data ingestion via the CoinGecko Pro API. Implements graceful degradation—falling back to local database persistence when the API is rate-limited or the Redis cache is unavailable.
* **Resilience:** Circuit Breaker pattern (Resilience4j) implemented on external API calls to prevent cascading failures.

---

## ✨ Core Features

* **Real-Time Market Tracking:** Live INR pricing, 24h highs/lows, and market capitalization across the top cryptocurrencies.
* **Secure JWT Authentication:** Full user lifecycle management with encrypted credential storage and token-based session validation.
* **ACID-Compliant Wallet:** A dual-entry ledger system for depositing, withdrawing, and transferring simulated funds, protected by idempotency keys to prevent duplicate transactions.
* **Order Execution:** Place simulated Buy/Sell orders that instantly calculate against live market rates and adjust wallet balances accordingly.
* **Watchlist Management:** Users can curate custom watchlists that persist across sessions via the PostgreSQL database.
* **Graceful Degradation:** The engine intelligently bypasses offline Redis clusters and rate-limited APIs to serve the last known good state from the database.

---

## 🚀 Getting Started (Local Development)

To run DeltaEdge locally, you will need Java 17+, Node.js, and a PostgreSQL instance.

### 1. Backend Setup (Spring Boot)
```bash
# Clone the repository
git clone [https://github.com/yourusername/deltaedge.git](https://github.com/yourusername/deltaedge.git)
cd deltaedge-backend

# Configure your environment variables in application.properties
# DB_URL, DB_USER, DB_PASSWORD, COINGECKO_API_KEY, JWT_SECRET_KEY

# Build and run the engine
./mvnw spring-boot:run
