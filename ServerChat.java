import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

@SuppressWarnings("unused")
class ServerChatImpl extends UnicastRemoteObject implements IServerChat, Serializable {
    private ArrayList<String> roomList;
    private ServerChat serverGUI;

    public ServerChatImpl(ServerChat serverGUI_) throws RemoteException {
        super();
        serverGUI = serverGUI_;
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
            // System.out.println("Nova sala criada: " + roomName);
            serverGUI.displayMessage("Nova sala criada: " + roomName);
        } else {
            throw new RemoteException("Sala já existe.");
        }
    }

    public void endRoom(String roomName)
    {
        roomList.removeIf(r -> r.equals(roomName));
    }
}

public class ServerChat extends JFrame
{
    private ServerChatImpl server;
    private JComboBox<String> roomComboBox;
    private JTextArea chatTextArea;

    public ServerChat()
    {
        try {
            server = new ServerChatImpl(this);
            java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(2020);
            registry.rebind("Servidor", server);

            setupGUI();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void refreshRoomList()
    {
        try
        {
            List<String> rooms = server.getRooms();
            roomComboBox.setModel(new DefaultComboBoxModel<>(rooms.toArray(new String[0])));
        }
        catch (Exception e)
        {
            displayMessage("Erro ao obter a lista de salas: " + e.getMessage());
        }
    }

    private void setupRoomBox()
    {
        roomComboBox = new JComboBox<>();
        refreshRoomList();
    }

    private void closeRoom()
    {
        String roomName = (String) roomComboBox.getSelectedItem();;
        
        if (roomName == null) return;
        
        try {
            IRoomChat room = (IRoomChat) Naming.lookup("rmi://localhost:2020/" + roomName);
            server.endRoom(roomName);
            room.closeRoom();

            displayMessage("Sala fechada: " + roomName);
            refreshRoomList();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    // atualiza o texto da chatArea
    // esse método é chamado pelo método remoto deliverMessage()
    public void displayMessage(String message)
    {
        SwingUtilities.invokeLater(() -> {
            chatTextArea.append(message + "\n");
            chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
        });
    }

    private void setupGUI()
    {
        //close operation e tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 200);

        //painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        //painel para seleção de sala
        JPanel roomPanel = new JPanel();
        roomPanel.add(new JLabel("Escolha ou crie uma sala:"));

        //cria a caixa de seleção de sala e adiciona ao painel
        setupRoomBox();
        roomPanel.add(roomComboBox);

        // botão de atualizar a lista de salas 
        JButton refreshRoomButton = new JButton("Atualizar lista");
        refreshRoomButton.addActionListener(e -> {
            refreshRoomList();
        });
        
        // botão de criar nova sala
        JButton closeRoomButton = new JButton("Fechar sala");
        closeRoomButton.addActionListener(e -> {
            closeRoom();
            refreshRoomList();
        });
        
        // adiciona os botões ao painel
        roomPanel.add(refreshRoomButton);
        roomPanel.add(closeRoomButton);

        mainPanel.add(roomPanel, BorderLayout.NORTH);

        //area de mensagens não editável, scrollavel
        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    
                ServerChat serverChatGUI = new ServerChat();
                serverChatGUI.setVisible(true);
            
                serverChatGUI.displayMessage("Servidor pronto!");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
} 