import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;


public class NodeImpl extends UnicastRemoteObject implements Node {
    private final int id;
    private int leaderId;
    private Node nextNode;
    private boolean isLeader;
    private boolean hasVoted;
    private boolean isAlive;

    public NodeImpl(int nodeId) throws RemoteException {
        this.id = nodeId;
        this.isAlive = true;
        this.isLeader = false;
        this.hasVoted = false;
    }

    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    public void setAlive(boolean status) {
        isAlive = status;
    }

    @Override
    public void recieveElection(int candidateId, int originId) throws RemoteException {

        //Simulate network delays
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(!isAlive){
            //The node is dead. Assume recovery mechanism and pass to the next node.
            nextNode.recieveElection(candidateId, originId);
            return;
        }

        if(this.id == originId) {
            leaderId = candidateId;
            hasVoted = true;
            System.out.println(id + ": Sending coordinator message.");
            nextNode.recieveLeader(leaderId, this.id);
            handleUserInput();
        }


        if (this.id > candidateId) {
            System.out.println(id + ": Recieving election message. Forwarding my ID.");
            hasVoted = true;
            //If my ID is greater than recieved ID, forward message with my ID
            nextNode.recieveElection(id, originId);
        } else if (this.id < candidateId) {
            System.out.println(id + ": Recieving election message. Forwarding message.");
            hasVoted = true;
            nextNode.recieveElection(candidateId, originId);
        }
    }

    @Override
    public void recieveLeader(int leaderId, int originId) throws RemoteException {

        //Simulate network delays
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (this.id == originId) {
            hasVoted = false;
            System.out.println(id + ": New leader is " + leaderId);
            System.out.println(id + ": Election is complete.");
            return;
        }

        if (!isAlive) {
            //Node is dead. Pass to the next node.
            nextNode.recieveLeader(leaderId, originId);
        } else {
            hasVoted = false;
            System.out.println(id + ": New leader is " + leaderId);
            nextNode.recieveLeader(leaderId, originId);
            this.leaderId = leaderId;
            if (this.id == leaderId) {
                //Leader node becomes aware of results
                isLeader = true;
            }
        }

        handleUserInput();
    }

    @Override
    public int getNodeId() throws RemoteException {
        return this.id;
    }

    public void initiateElection() {
        System.out.println(id + ": Detected Leader failure. Initiating election . . .");
        try {
            nextNode.recieveElection(id, id);
        } catch (RemoteException e) {
            System.out.println(id + ": Failed to initiate election");
        }
    }

    // Method to handle user input
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Type 'start' to initiate the election, or 'exit' to quit:");
            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("start")) {
                initiateElection();  // Start the election process
            } else if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                System.exit(0);
            } else {
                System.out.println("Invalid command.");
            }
        }
    }

    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Usage: java NodeImpl <nodeId>");
            return;
        }

        try{
            // Parsing arguments
            int nodeId = Integer.parseInt(args[0]);

            // Create the node and bind it to the RMI Registry
            NodeImpl node = new NodeImpl(nodeId);

            // Connect to the PeerRegister to register itself
            Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            //Node0 is reserved for Peer Register
            PeerRegister peerRegister = (PeerRegister) registry.lookup("Node0");
            node.setNextNode(peerRegister);

            //Add registered node to registry
            try {
                registry.bind("Node"+nodeId,node);
                peerRegister.register(nodeId);

            }catch (AlreadyBoundException e){
                registry.rebind("Node"+nodeId,node);
                peerRegister.register(nodeId);
            }

            System.out.println(nodeId + ": Registered with PeerRegister.");

/*
            // Retry Mechanism for looking up the next node
            int retries = 10;
            int delay = 3000;

            while (retries > 0) {
                try{
                    int nextNodeID = peerRegister.getNextNode(nodeId).getNodeId();
                    node.setNextNode(peerRegister.getNextNode(nodeId));
                    System.out.println(nodeId + ": Connected to " + nextNodeID);
                    break;
                } catch (Exception e){
                    retries--;
                    System.err.println("Failed to lookup next node");
                    if (retries > 0) {
                        System.out.println(nodeId + ": Retrying . . .");
                        try {
                            Thread.sleep(delay); // Wait before retrying
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }else{
                        System.err.println("Max tries reached. Exiting . . .");
                        System.exit(1); // Exit if max retries reached
                    }
                }
            }

            // Request the next node from the PeerRegister
            Node nextPeer = peerRegister.getNextNode(nodeId);
            if (nextPeer != null) {
                node.setNextNode(nextPeer);
                System.out.println(nodeId + ": Next node set to " + nextPeer.getNodeId());
            } else {
                System.out.println(nodeId + ": No next node available.");
            }
*/
            node.handleUserInput();

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}

