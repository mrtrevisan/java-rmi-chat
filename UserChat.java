
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.awt.*;
import javax.swing.*;

@SuppressWarnings("unused")
class UserChatImpl extends UnicastRemoteObject implements IUserChat, Serializable {
    private String userName;
    private UserChat userChat;

    public UserChatImpl(String userName, UserChat userChat) throws RemoteException {
        this.userName = userName;
        this.userChat = userChat;
    }

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException {
        userChat.displayMessage(senderName + ": " + msg);
    }

    public String getUserName() {
        return userName;
    }
}

public class UserChat extends JFrame
{
    //mantem referências para o 
    // nome de usuário
    // servidor conectado
    // classe serializavel do userchat
    // sala de chat selecionada
    private String userName;
    private IServerChat server;
    private UserChatImpl userChat;
    private String selectedRoom = null;

    // mantem referencias para alguns 
    // artefatos da GUI
    private JComboBox<String> roomComboBox;
    private JTextArea chatTextArea;
    private JTextField messageTextField;

    public UserChat(String userName)
    {
        super("Chat do Usuário: " + userName);
        this.userName = userName;
        connectToServer();
        setupGUI();
    }

    //conecta ao servidor remoto
    private void connectToServer() 
    {
        try 
        {
            server = (IServerChat) Naming.lookup("rmi://localhost:2020/Servidor");
            userChat = new UserChatImpl(userName, this);
        } 
        catch (Exception e) 
        {
            displayMessage("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    // cria nova sala de chat
    private void createNewRoom(String name)
    {
        try 
        {
            server.createRoom(name);
        } 
        catch (Exception e) 
        {
            displayMessage("Erro ao criar sala: " + e.getMessage());
        }
    }

    // atualiza lista de salas para ser selecionada pelo usuario
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

    // envia uma mensagem para a sala selecionada
    private void sendMessage(String message)
    {
        try
        {
            String roomName = selectedRoom;
            if (roomName == null) return;
    
            IRoomChat room = (IRoomChat) Naming.lookup("rmi://localhost:2020/" + roomName);
            room.sendMsg(userName, message);
        }
        catch (Exception e)
        {
            displayMessage("Erro ao enviar mensagem: " + e.getMessage());
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

    // sai da sala selecionada
    private void exitRoom()
    {
        try
        {
            String roomName = selectedRoom;
            if (roomName == null) return;

            IRoomChat room = (IRoomChat) Naming.lookup("rmi://localhost:2020/" + roomName);
            room.leaveRoom(userName);

            selectedRoom = null;
            chatTextArea.setText("");
        }
        catch (Exception e)
        {
            displayMessage("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    // setup da caixa de seleção de salas
    private void setupRoomBox()
    {
        roomComboBox = new JComboBox<>();
        refreshRoomList();
    }

    // setup da GUI
    private void setupGUI()
    {
        //close operation e tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);

        //painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        //painel para seleção de sala
        JPanel roomPanel = new JPanel();
        roomPanel.add(new JLabel("Escolha ou crie uma sala:"));

        //cria a caixa de seleção de sala e adiciona ao painel
        setupRoomBox();
        roomPanel.add(roomComboBox);

        //cria o botão de selecionar sala 
        // e adiciona o evento de clique
        JButton joinRoomButton = new JButton("Entrar na Sala");
        joinRoomButton.addActionListener(e -> {
            String roomName = (String) roomComboBox.getSelectedItem();
            if (roomName != null)
            {
                try 
                {
                    if (selectedRoom != null) exitRoom();

                    IRoomChat room = (IRoomChat) Naming.lookup("rmi://localhost:2020/" + roomName);
                    room.joinRoom(userName, (IUserChat) userChat);

                    displayMessage("Você entrou na sala: " + roomName);
                    selectedRoom = roomName;
                } 
                catch (Exception ex) 
                {
                    displayMessage("Erro ao entrar na sala: " + ex.getMessage());
                }
            } 
        });

        // botão de atualizar a lista de salas 
        JButton refreshRoomButton = new JButton("Atualizar lista");
        refreshRoomButton.addActionListener(e -> {
            refreshRoomList();
        });
        
        // botão de criar nova sala
        JButton newRoomButton = new JButton("Criar sala");
        newRoomButton.addActionListener(e -> {
            String newRoomName = JOptionPane.showInputDialog(null, "Digite o nome da sala:");
            createNewRoom(newRoomName);
            refreshRoomList();
        });
        
        // adiciona os botões ao painel
        roomPanel.add(joinRoomButton);
        roomPanel.add(refreshRoomButton);
        roomPanel.add(newRoomButton);

        mainPanel.add(roomPanel, BorderLayout.NORTH);

        //area de mensagens não editável, scrollavel
        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        //paniel para enviar mensagem
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        messageTextField = new JTextField();
        messagePanel.add(messageTextField, BorderLayout.CENTER);

        //botão para enviar a mensagem
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> {
            String message = messageTextField.getText().trim();

            if (!message.isEmpty() && selectedRoom != null) {
                //mensagens de ação 
                if (message.equals("/sair"))
                {
                    exitRoom();
                }
                else 
                {
                    //mensagens de conteúdo
                    sendMessage(message);
                    messageTextField.setText("");
                }
            }
        });

        messagePanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(messagePanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                String userName = JOptionPane.showInputDialog(null, "Digite seu nome de usuário:");
    
                if (userName != null && !userName.trim().isEmpty())
                {
                    UserChat userChatGUI = new UserChat(userName);
                    userChatGUI.setVisible(true);
                } 
                else
                {
                    JOptionPane.showMessageDialog(null, "Nome de usuário inválido!");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }

        });
    }
}
