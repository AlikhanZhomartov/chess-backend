# Modern Chess Backend: 100k+ Connections with Virtual Threads

This project demonstrates a high-concurrency architecture for an online chess service built with **Java 25** and **Project Loom**. It moves away from the complexity of reactive programming (WebFlux/Reactor) in favor of a clean, imperative style using **Virtual Threads** without sacrificing performance.



## 🚀 Key Features

* **Netty Gateway:** Handles 100,000+ simultaneous WebSocket connections with minimal memory footprint.
* **Virtual Actor Pattern:** Ensures strict message ordering (Move A -> Move B) for each game session using non-blocking queues and Virtual Threads.
* **Zero-Parsing Pipeline:** Efficiently broadcasts game updates via NATS by passing raw bytes directly to WebSockets.
* **Hybrid Threading Model:** * **Virtual Threads** for I/O-bound tasks (gRPC, NATS, Networking).
    * **Platform Threads** for CPU-bound tasks (Chess engine calculations) to avoid carrier thread pinning.

## 🛠 Tech Stack

* **Runtime:** Java 25 (with Virtual Threads enabled)
* **Networking:** Netty (Custom Pipeline)
* **Service Communication:** gRPC (Blocking calls over VT)
* **Messaging:** NATS (Pub/Sub for game updates)
* **Concurrency:** JCTools (MpscUnboundedArrayQueue)

## 🏗 Architecture

The system is split into two main components:

1.  **Gateway Service:** The entry point. Manages sessions, JWT authentication, and routes commands.
2.  **Game Brain Service:** The "intelligence" layer. Handles move validation and chess engine logic. Currently designed as a high-performance gRPC server.



### The Virtual Actor Pattern
To maintain message ordering while using the power of Virtual Threads, we implement a lightweight actor per connection:
- Incoming messages are pushed to a `MpscUnboundedArrayQueue`.
- An `AtomicBoolean` ensures only one Virtual Thread at a time processes a specific user's queue.
- This eliminates the need for `synchronized` blocks or complex locking in business logic.

## 🚦 Getting Started

### Prerequisites
* JDK 25
* Docker (for NATS)