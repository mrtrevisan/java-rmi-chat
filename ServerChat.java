
// ServerChat.java
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;

    public ServerChat() throws RemoteException {
        super();
        roomList = new ArrayList<>();
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        if (!roomList.contains(roomName)) {
            java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(2020);
            
            try {
                registry.bind(roomName, new RoomChat(roomName));
            } catch (AlreadyBoundException e) {
                throw new RemoteException("Sala já existe.");
            }

            roomList.add(roomName);
            System.out.println("Nova sala criada: " + roomName);
        } else {
            throw new RemoteException("Sala já existe.");
        }
    }

    public static void main(String[] args) {
        try {
            ServerChat server = new ServerChat();

            java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(2020);
            registry.rebind("Servidor", server);

            System.out.println("Servidor pronto.");
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
