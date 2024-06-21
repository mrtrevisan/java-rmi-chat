import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    public RoomChat(String roomName) throws RemoteException {
        super();
        this.roomName = roomName;
        userList = new HashMap<>();
    }

    @Override
    public void sendMsg(String usrName, String msg) throws RemoteException {
        String message = usrName + ": " + msg;

        for (Map.Entry<String, IUserChat> user : userList.entrySet()) {
            user.getValue().deliverMsg(roomName, message);
        }
    }

    @Override
    public void joinRoom(String usrName, IUserChat user) throws RemoteException {
        userList.put(usrName, user);

        sendMsg("SYSTEM", usrName + " entrou na sala.");
        // System.out.println(usrName + " entrou na sala: " + roomName);
    }

    @Override
    public void leaveRoom(String usrName) throws RemoteException {
        userList.entrySet().removeIf(user -> user.getKey().equals(usrName));

        sendMsg("SYSTEM", usrName + " saiu da sala.");
        // System.out.println(usrName + " saiu da sala: " + roomName);
    }

    @Override
    public void closeRoom() throws RemoteException {
        for (Map.Entry<String, IUserChat> user : userList.entrySet()) {
            user.getValue().deliverMsg(roomName, "Sala fechada pelo servidor.");
        }

        userList.clear();
        // System.out.println("Sala " + roomName + " fechada pelo servidor.");

        java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(2020);
            
        try {
            registry.unbind(roomName);
        } catch (NotBoundException e) {
            System.out.println("Sala " + roomName + " já está fechada.");
        }
    }

    @Override
    public String getRoomName() throws RemoteException {
        return roomName;
    }
}
