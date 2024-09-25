import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    int recieveElection(int candidateId, int originId) throws RemoteException;
    int recieveLeader(int leaderId,int originId) throws RemoteException;
}
