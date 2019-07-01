# axon-distributed-demo
Distributed Query Bus for Axonframework

Provides facilities for QueryBus and QueryUpdateEmitter to work with multiple cluster-instances, 
i.e. DistributedQueryBus and DistributedQueryEventEmitter.

# Challenge

QueryHandlers are typically feeded by events.
The processing instance for the event is determined by the segment allocation.

When using SubcriptionQuery.updates() then QueryUpdateEmitter 
can provide updates based on events retrieved after the initial query-result.

In Axon Framework V4.1 this works on a single instance or with axon-server.

# Solution Proposal

Given this challenge, inspired by the JpaEventStorageEngine, 
a shared storage can be used to communicate SubscriptionQueryUpdateMessages.

The JpaQueryUpdateStore manages subscription and their updates.

The QueryUpdatePollingServices is polling on above store and is feeding results into the update-Flux.

## About the ideal storage
JpaQueryUpdateStorage will handle volatile data, which main characteristics is to be short-living.

The data amount will scale alongside the subscriptions and the updates linearly.
Given example of DemoApp is subscribing on a per-request basis.


An ideal storage would therefore be some in-memory database.
Eviction strategies might be applied when capacity becomes tight.

## Usage
This package supports SpringBoot AutoConfiguration.

Build the jar and submit it to your local maven repository or equivalent.

## Benchmark results
Results taken on random hard-ware:

| Benchmark                         | Mode   |  Cnt  |  Score     | Units |
| --------------------------------- | ------ | ----- | ---------: | ----- |
| SimpleQueryBusBenchmark           | thrpt  |  2    | 173445,474 | ops/s |
| DistributedQueryBusJpaBenchmark   | thrpt  |  2    |     31,248 | ops/s |
| DistributedQueryBusRedisBenchmark | thrpt  |  2    |     12,688 | ops/s |

## Todo
1. ~~There should be cleansing handling stale subscriptions and their updates.~~
2. ~~Performance should be benchmark, and scalability assured.~~
3. make use of redis ttl
