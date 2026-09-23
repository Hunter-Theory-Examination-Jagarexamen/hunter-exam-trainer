# System Architecture

```mermaid
flowchart LR

User --> Browser

Browser --> ReactPWA

ReactPWA --> SpringBoot

SpringBoot --> PostgreSQL
```