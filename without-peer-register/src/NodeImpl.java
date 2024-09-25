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

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    @Override
    public int recieveElection(int candidateId, int originId) throws RemoteException {

        //Simulate network delays
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(!isAlive){
            //The node is dead. Assume recovery mechanism and pass to the next node.
            nextNode.recieveElection(candidateId, originId);
        }

        if(this.id == originId) {
            leaderId = candidateId;
            hasVoted = true;
            System.out.println(id + ": Sending coordinator message.");
            nextNode.recieveLeader(leaderId, this.id);
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
        return 1;
    }

    @Override
    public int recieveLeader(int leaderId, int originId) throws RemoteException {

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
            handleUserInput();
            return 1;
        }
        if (!isAlive) {
            //Node is dead. Pass to the next node.
            nextNode.recieveLeader(leaderId, originId);
        } else {
            hasVoted = false;
            System.out.println(id + ": New leader is " + leaderId);
            this.leaderId = leaderId;
            if (this.id == leaderId) {
                //Leader node becomes aware of results
                isLeader = true;
            }
            nextNode.recieveLeader(leaderId, originId);
        }
        handleUserInput();
        return 1;
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
        if(args.length != 4){
            System.out.println("Usage: java NodeImpl <nodeId> <nextNode> <registryPort> <nextPort>");
            return;
        }

        try{
            // Parsing arguments
            int nodeId = Integer.parseInt(args[0]);
            String nextNode = args[1];
            int registryPort = Integer.parseInt(args[2]);
            int nextPort = Integer.parseInt(args[3]);

            // Create the node and bind it to the RMI Registry
            NodeImpl node = new NodeImpl(nodeId);

            Registry registry = LocateRegistry.createRegistry(registryPort);
            registry.bind("Node" + nodeId, node);
            System.out.println(nodeId +": Registered");

            // Retry Mechanism for looking up the next node
            int retries = 10;
            int delay = 2000;

            while (retries > 0) {
                try{
                    Registry registry2 = LocateRegistry.getRegistry("localhost", nextPort);
                    node.setNextNode((Node) registry2.lookup(nextNode));
                    System.out.println(nodeId + ": Connected to " + nextNode);
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

            while(true){
                node.handleUserInput();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}

