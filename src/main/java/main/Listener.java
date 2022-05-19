package main;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import controller.ClientUIController;
import controller.LoginUIController;
import model.InfoUserLogged;
import model.ReceiverSearchResult;
import model.Status;
import model.User;
import model.message.Message;
import model.message.MessageContent;
import model.message.MessageType;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Listener implements Runnable{

    private static final String HASCONNECTED = "has connected";

    private Socket socket;
    private String hostname;
    public int port;
    public static ClientUIController clientUIController;
    private ObjectInputStream ois;
    private InputStream is;
    private static ObjectOutputStream oos;
    private OutputStream os;
    //private static User userLogged;

    public static InfoUserLogged userLogged_listener = ClientUIController.myUser;
    Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, ClientUIController clientUIController) {
        this.hostname = hostname;
        this.port = port;
        Listener.clientUIController = clientUIController;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(hostname, port);
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        }
        try {
            connect();
            logger.debug("Sockets in and out ready!");
            while (socket.isConnected()) {
                Message message = null;
                message = (Message) ois.readObject();
                if (message != null) {
                    switch (message.getType()) {
                        case PRIVATE:
                            clientUIController.addToChatView(message);
                            break;
                        case ONLINE:
                            clientUIController.setFriendOnline(message);
                            break;
                        case NEW:
                            clientUIController.updateListConversation(message);
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* This method is used to send a connecting message */ //first message
    public static void connect() throws IOException {
        Message createMessage = new Message();
        createMessage.setSender(userLogged_listener.getId());
        createMessage.setType(MessageType.CONNECTED);
        createMessage.setUser_sender(LoginUIController.user_logged_toSend);
        createMessage.setStatus(Status.ONLINE);
        createMessage.setMsg(HASCONNECTED);
        oos.writeObject(createMessage);
    }
    // send message text
    public static void sendPrivate(String msg) throws IOException {
        Message message = new Message();
        message.setSender(userLogged_listener.getId());
        message.setReceiver(clientUIController.convo_selected.getUsers());
        message.setType(MessageType.PRIVATE);
        message.setContent(MessageContent.TEXT);
        message.setStatus(Status.ONLINE);
        message.setMsg(msg);
        message.setImage(userLogged_listener.getAvatar());
        message.setId_convo(clientUIController.convo_selected.getId_convo());
        oos.writeObject(message);
        oos.flush();
    }
    // send image
    public static void sendAttachment(String senderName, int id_convo, byte[] data) throws IOException {
        Message message = new Message();
        message.setSender(userLogged_listener.getId());
        message.setReceiver(clientUIController.convo_selected.getUsers());
        message.setMsg(senderName + " has sent an attachment!");
        message.setId_convo(id_convo);
        message.setType(MessageType.PRIVATE);
        message.setContent(MessageContent.IMAGE);
        message.setStatus(Status.ONLINE);
        message.setData(data);
        message.setImage(userLogged_listener.getAvatar());
        message.setId_convo(clientUIController.convo_selected.getId_convo());
        oos.writeObject(message);
        oos.flush();
    }
    // send file
    public static void sendFile(String senderName, int id_convo, byte[] data, String fileExtension, String fileName, String fileSize) throws IOException {
        Message message = new Message();
        message.setSender(userLogged_listener.getId());
        message.setReceiver(clientUIController.convo_selected.getUsers());
        message.setMsg(senderName + " has sent a file!");
        message.setId_convo(id_convo);
        message.setType(MessageType.PRIVATE);
        message.setContent(MessageContent.FILE);
        message.setFileName(fileName);
        message.setFileExtension(fileExtension);
        message.setStatus(Status.ONLINE);
        message.setData(data);
        message.setFileSize(fileSize);
        message.setImage(userLogged_listener.getAvatar());
        message.setId_convo(clientUIController.convo_selected.getId_convo());
        oos.writeObject(message);
        oos.flush();
    }
    // send create new Conversation by Text
    public static void sendNewConversationByText(String msg, ArrayList<User> users) throws IOException {
        Message message = new Message();
        message.setSender(userLogged_listener.getId());
        message.setUser_sender(users.get(0));
        message.setReceiver(users);
        message.setType(MessageType.NEW);
        message.setContent(MessageContent.TEXT);
        message.setStatus(Status.ONLINE);
        message.setMsg(msg);
        message.setImage(userLogged_listener.getAvatar());
        oos.writeObject(message);
        oos.flush();
    }
}
