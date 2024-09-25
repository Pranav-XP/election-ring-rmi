# Election Ring RMI with Peer Register

## Overview
This project implements a distributed election algorithm using the Ring topology and Java RMI (Remote Method Invocation). It simulates a group of nodes, each with a unique ID, that participate in an election to select a leader. When a leader node fails, an election process is initiated by the remaining nodes to elect a new leader. The election messages are passed through the ring of nodes until the highest ID is elected as the new leader.

A peer register is implemented to keep track of the registered nodes in the ring.

## Features
- **Ring Election Algorithm**: Each node is aware of its next node in the ring, forwarding messages related to the election.
- **Leader Failure Detection**: A node detects the failure of the leader and initiates an election to elect a new leader.
- **Peer Registration**: Nodes are dynamically registered with a `PeerRegister` for better management of nodes in the ring.
- **Remote Communication**: All interactions between nodes are done remotely using Java RMI.

## Project Structure
- `NodeImpl`: Implements the behavior of a node in the election ring. Nodes can send and receive election and leader messages.
- `PeerRegisterImpl`: Manages the registration and lookup of nodes in the RMI registry.
- `Node`: The interface that defines the remote methods (`recieveElection`, `recieveLeader`) each node must implement.

## Prerequisites
- Java Development Kit (JDK) 8 or later
- Basic understanding of Java RMI

## How It Works
1. **Election Process**:
    - A node detects the leader's failure and sends an election message.
    - Each node forwards the message with its ID if its ID is greater than the candidate's ID. Otherwise, it forwards the message as received.
    - Once the message completes the ring, the highest ID is declared as the new leader.

2. **Leader Announcement**:
    - Once a leader is elected, the elected leader sends a leader message, and each node updates its internal state to recognize the new leader.

3. **Peer Register**:
    - The `PeerRegister` allows nodes to dynamically register themselves in the RMI registry, ensuring the proper flow of the ring election.

## Setup Instructions

### Step 1: Compile the Code
1. Open your terminal and navigate to the directory containing the source files.
2. Compile the source files using the `javac` command:
   ```bash
   javac NodeImpl.java PeerRegisterImpl.java
   ```

### Step 2: Run the Peer Register
Start the Peer Register to manage node registration. Run the following command:
```bash
java PeerRegisterImpl
```
This will create a peer register that nodes will interact with for registering and managing node connections.

### Step 3: Run the Nodes
To start each node, use the following command:
```bash
java NodeImpl <nodeId>
```
For example, to start a node with ID 1:
```bash
java NodeImpl 1
```
Nodes will automatically register themselves with the peer register.

### Step 4: Interact with the Nodes
Once a node is started, you can interact with it by typing commands:
- `start`: Initiates an election.
- `exit`: Shuts down the node.

### Example Interaction
```
Type 'start' to initiate the election, or 'exit' to quit:
start
1: Detected Leader failure. Initiating election...
```

## Key Classes and Methods

- **`NodeImpl`**:
    - `recieveElection(int candidateId, int originId)`: Handles the election process by forwarding or updating the leader candidate.
    - `recieveLeader(int leaderId, int originId)`: Handles the reception of the leader announcement.
    - `initiateElection()`: Triggers the election process.

- **`PeerRegisterImpl`**:
    - `register(int nodeId)`: Registers a node in the peer register.
    - `recieveElection(int candidateId, int originId)`: Handles forwarding election messages to the next node.
    - `recieveLeader(int leaderId, int originId)`: Handles forwarding the leader message to the next node.

## Error Handling
- The program catches `RemoteException` and handles any potential issues with RMI communication.
- If a node cannot be bound to the RMI registry (due to an existing binding), it will rebind the node.

## Future Enhancements
- Implement recovery for failed nodes.
- Allow dynamic node removal and addition during runtime.

---

By following the instructions above, you should be able to set up and run the election ring with a peer registration system using Java RMI.