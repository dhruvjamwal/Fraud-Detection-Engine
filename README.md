🔌 API Endpoints
Core Transaction Flow
sequenceDiagram
    participant Client
    participant API as Spring Boot API
    participant Engine as Fraud Engine
    participant DB as MySQL Database

    Client->>API: POST /api/v1/transactions
    API->>Engine: Validate & Score Transaction
    Engine->>DB: Fetch Transaction History
    DB-->>Engine: Historical Data
    Engine->>Engine: Apply Risk Rules (10+)
    Engine-->>API: Decision (BLOCK / ALLOW)
    API-->>Client: JSON Response (<50ms)
