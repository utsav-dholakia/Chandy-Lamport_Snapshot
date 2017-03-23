# Chandy-Lamport Global Consistent Snapshot Protocol for Distributed System

This is an implementation of Chandy-Lamport's protocol for taking consistent global snapshot of a distributed system. 
All the nodes in the system are independent and communicates in a certain topology. Also, to simulate an application running in the system, there is a custom protocol designed, called - Map protocol.
According to Map protocol's design, nodes will send each other application messages which contain vector clock (Fidge-Mattern's vector clock) that represents timestamp of each node as perceived by each node globally.
On top of this application messages, the chandy-lamport protocol's marker messages will be present, asking nodes to record local state and then keep monitoring incoming channels for the in-transit messages for that snapshot.
All the snapshot requests will be originated by one node and that node will get snapshot data from all nodes in the end. It will consolidate data from all nodes and then save it in output files seperately for each node.
