# Oracle Coherence + API Presentation

> Topic: Extended Client, Proxy, Cluster, POF, Deployment, Scaling, and Failover

---

# 1. Objective

The objective of this presentation is to explain:

- The **Oracle Coherence architecture**
- How the **Extended Client** connects through the **Extended Client Proxy**
- How the **cluster** is organized into **members** and **2 nodes per member**
- Why **POF (Portable Object Format)** is used
- How **POF models** and **POF files** are managed on both **client** and **server** sides
- How to **deploy the Oracle Coherence configuration**
- How to **deploy the API** that uses Oracle Coherence as a cache
- How **scaling** and **fallback/failover** work

---

# 2. High-Level Architecture

## Main components

- **API / Extended Client**
- **Extended Client Proxy**
- **Oracle Coherence Cluster**
- **Cluster Members**
- **Nodes inside each member**

## Schematic view

```text
+----------------------+
|        API           |
|  Extended Client     |
+----------+-----------+
           |
           | TCP / Extend protocol
           v
+----------------------+
|  Extended Client     |
|      Proxy           |
+----------+-----------+
           |
           v
+---------------------------------------------------+
|                Oracle Coherence Cluster           |
|                                                   |
|  +----------------+    +----------------+        |
|  | Member 1       |    | Member 2       |        |
|  |                |    |                |        |
|  | Node 1         |    | Node 3         |        |
|  | Node 2         |    | Node 4         |        |
|  +----------------+    +----------------+        |
|                                                   |
+---------------------------------------------------+
```

## What this means

- The **API** is not directly storing data in a local cache only.
- The API connects to Coherence using the **Extend client** pattern.
- The **proxy** is the server-side entry point for remote clients.
- The cluster is split into **members**, and each member hosts **2 nodes**.
- The nodes work together to provide **distribution, redundancy, and availability**.

---

# 3. Roles of Each Component

## Extended Client

The Extended Client is the API application that:
- sends requests to Coherence remotely
- reads and writes cache entries
- serializes objects using POF

### Example use case
A REST endpoint receives an order request, then stores the order in Coherence cache.

## Extended Client Proxy

The proxy is the server-side gateway that:
- receives Extend connections from clients
- forwards requests to the Coherence cluster
- keeps the client decoupled from the internal cluster topology

### Why it matters
The API does not need to know which node actually stores the data.

## Cluster

The cluster:
- stores and distributes cache entries
- balances load across members
- provides failover when a node fails

## Member

A member is one Coherence JVM participating in the cluster.

## Node

In this presentation, a node is a process or runtime instance inside a member setup.
With **2 nodes per member**, the goal is to:
- increase redundancy
- improve availability
- support better fallback behavior

---

# 4. Why POF Is Used

## What POF is

POF means **Portable Object Format**.

It is used to serialize objects in a compact and efficient way.

## Why POF is a good fit

- **High performance**
- **Smaller payloads** than default Java serialization
- **Stable schema evolution**
- **Better compatibility** across versions
- **Designed for distributed data grids**

## Business value

Using POF reduces:
- serialization overhead
- network cost
- memory footprint
- upgrade risk

---

# 5. POF Usage in Client and Server

## Important rule

The POF structure must be consistent on both sides:

- **Client side**
- **Server side**

If the client writes data in one format and the server expects another, deserialization failures can happen.

## What is shared

Both sides need:
- the **POF model classes**
- the **POF configuration file**
- stable **type IDs**
- compatible field definitions

## Schematic flow

```text
API / Extended Client
    |
    |  serialize using POF
    v
Extended Client Proxy
    |
    |  deserialize / process using same POF schema
    v
Coherence Cluster
```

## Example model

```java
public class Order {
    private String id;
    private double amount;
    private String currency;
}
```

## POF evolution example

### Safe change
Add a new field at the end.

```java
public class Order {
    private String id;
    private double amount;
    private String currency;
    private String status; // new field
}
```

### Unsafe change
- changing field indexes
- changing type IDs
- reusing a type ID for another class

---

# 6. POF Configuration Files

## Main idea

POF configuration usually defines:
- the mapping between classes and type IDs
- the serialization layout
- the known object types for the cluster

## Example POF config

```xml
<pof-config>
  <user-type-list>
    <user-type>
      <type-id>1001</type-id>
      <class-name>com.example.model.Order</class-name>
    </user-type>
    <user-type>
      <type-id>1002</type-id>
      <class-name>com.example.model.Customer</class-name>
    </user-type>
  </user-type-list>
</pof-config>
```

## Best practice

- Keep **type IDs stable**
- Version your POF changes carefully
- Keep client and server config aligned
- Test serialization before deployment

---

# 7. Coherence Configuration

## Main configuration files

Typical Coherence-related files include:

- `cache-config.xml`
- `pof-config.xml`
- `coherence-override.xml`
- environment-specific deployment files

## Cache configuration example

```xml
<cache-config>
  <caching-schemes>
    <distributed-scheme>
      <scheme-name>distributed-cache</scheme-name>
      <service-name>DistributedCache</service-name>
      <backup-count>1</backup-count>
    </distributed-scheme>
  </caching-schemes>
</cache-config>
```

## Why configuration matters

The configuration controls:
- cache behavior
- backup strategy
- clustering
- proxy access
- serialization support

---

# 8. Deployment of Oracle Coherence Configuration

## Deployment objective

Deploy a repeatable and predictable Coherence setup that includes:
- cluster configuration
- proxy setup
- POF configuration
- cache configuration

## Deployment flow

```text
Prepare configuration
        |
Package Coherence artifacts
        |
Deploy member/node instances
        |
Start proxy service
        |
Join cluster
        |
Validate cache and POF behavior
```

## Suggested deployment steps

1. Package the Coherence configuration with the deployment artifact.
2. Ensure the correct `pof-config.xml` is present.
3. Ensure cache definitions are available.
4. Start cluster members.
5. Start the proxy service.
6. Confirm cluster membership.
7. Validate cache operations.
8. Validate serialization compatibility.

## Example startup idea

```bash
java \
  -Dtangosol.coherence.cluster=prod-cluster \
  -Dtangosol.coherence.wka=host1,host2 \
  -Dtangosol.coherence.pof.config=pof-config.xml \
  -jar coherence-node.jar
```

---

# 9. Deployment of the API That Uses Coherence as a Cache

## API responsibilities

The API:
- exposes endpoints to clients
- uses Coherence as a distributed cache
- sends requests through the Extend client
- serializes models using POF

## API deployment flow

```text
Build API
   |
Run unit tests
   |
Run serialization tests
   |
Package artifact
   |
Deploy to runtime
   |
Connect to Coherence proxy
   |
Validate cache operations
```

## Example cache usage

```java
NamedCache cache = CacheFactory.getCache("orders");
cache.put("123", new Order("123", 250.0, "EUR"));
```

## Deployment best practices

- Keep API and POF model versions compatible
- Deploy cluster changes first when needed
- Test cache read/write before opening traffic
- Use health checks for proxy connectivity

---

# 10. Relationship Between API and Coherence

## Simple flow

```text
Client request
   |
API receives request
   |
API stores or reads from Coherence cache
   |
Coherence proxy forwards to cluster
   |
Cluster returns result
   |
API responds to caller
```

## Example use cases

- read-through cache
- write-through cache
- session storage
- shared distributed state
- computed results cache

---

# 11. Scaling Strategy

## Horizontal scaling

Scaling means adding more capacity by adding more nodes or members.

### Example

```text
Before:
2 members -> load shared across 2 runtime units

After:
4 members -> load shared across 4 runtime units
```

## What improves when scaling

- throughput
- storage capacity
- availability
- resilience during peak traffic

## How Coherence scales

- partitions are redistributed
- cache data is balanced
- members join or leave the cluster
- the cluster rebalances automatically

## Suggested operational rule

Scale first by adding **members/nodes** before increasing application complexity.

---

# 12. Fallback / Failover Strategy

## Goal

Keep the system available even if a node or member fails.

## Failover mechanism

```text
Primary node fails
        |
Backup partition becomes active
        |
Cluster rebalances
        |
Traffic continues
```

## Example with 2 nodes per member

```text
Member 1
- Node 1
- Node 2

Member 2
- Node 3
- Node 4
```

If one node goes down:
- the backup node can continue serving the data
- the cluster can recover without full service interruption

## Recommended practices

- Use backup partitions
- Distribute nodes across different hosts
- Monitor node health
- Automate restarts
- Test node failure scenarios

---

# 13. Change Management

## Types of changes

### API changes
- endpoint changes
- business logic changes
- validation changes

### Coherence changes
- cache configuration changes
- proxy settings
- cluster topology changes

### POF changes
- adding fields
- modifying models
- introducing new types

## Safe change rule

Prefer:
- additive changes
- backward-compatible changes
- rolling deployments

Avoid:
- changing type IDs
- breaking POF schema
- deploying incompatible client and server versions together

## Deployment sequence suggestion

1. Deploy compatible Coherence configuration
2. Deploy API with compatible POF models
3. Validate cache access
4. Shift traffic gradually
5. Monitor behavior

---

# 14. Monitoring and Operations

## What to monitor

- proxy availability
- cluster membership
- node health
- cache hit rate
- serialization errors
- partition distribution
- response latency

## Operational view

```text
Monitoring
   |
   +--> API health
   +--> Proxy health
   +--> Cluster health
   +--> Serialization health
```

## Example operational checks

- Is the proxy reachable?
- Are all members joined?
- Are all caches available?
- Are POF files aligned?
- Are there any deserialization errors?

---

# 15. Recommended Deployment Checklist

## Before deployment

- [ ] POF model classes are updated on both sides
- [ ] `pof-config.xml` is synchronized
- [ ] `cache-config.xml` is valid
- [ ] proxy configuration is available
- [ ] cluster startup parameters are correct
- [ ] tests pass
- [ ] rollback plan exists

## During deployment

- [ ] Deploy Coherence configuration
- [ ] Start cluster members
- [ ] Start proxy
- [ ] Deploy API
- [ ] Validate cache read/write
- [ ] Check logs and health metrics

## After deployment

- [ ] Confirm cluster is stable
- [ ] Confirm serialization works
- [ ] Confirm scaling behavior
- [ ] Confirm fallback behavior
- [ ] Confirm latency is acceptable

---

# 16. Key Takeaways

- The **API** acts as an **Extended Client**
- The **Extended Client Proxy** is the gateway into the Coherence cluster
- The cluster is made of **members**, with **2 nodes per member**
- **POF** is used for fast, compact, and stable serialization
- POF files and models must be aligned on **client** and **server**
- Oracle Coherence configuration must be deployed consistently
- The API and Coherence must be deployed in the right order
- Scaling is horizontal and failover should be tested explicitly

---

# 17. Final Message

The target architecture provides:

- a distributed cache layer
- a predictable deployment model
- efficient serialization
- safe evolution of the API and data models
- horizontal scaling
- fallback and resilience

This gives the team a clear path to operate, extend, and maintain the platform safely.

---

# Appendix: Short Version for a Slide Summary

```text
API (Extended Client)
   -> Extended Client Proxy
   -> Coherence Cluster
      -> Members
      -> 2 Nodes per Member

POF is used on client and server for fast serialization and compatibility.

Deploy:
1. Coherence config
2. Cluster members
3. Proxy
4. API

Scale:
- Add members/nodes

Fallback:
- Backup partitions
- Automatic failover
```
