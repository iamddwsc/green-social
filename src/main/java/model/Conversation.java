package model;

import model.message.Message;

import java.io.Serializable;
import java.util.ArrayList;

public class Conversation implements Serializable {
    private ArrayList<Message> messages;
    private ArrayList<User> users;
    private int id_convo;
    private int type_convo; //1 = 1vs1, 2 = 1vsN
    private String listUser;
    private String lastMessage;
    private int idLastSender;
    private ConvoType newConversation;

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public int getId_convo() {
        return id_convo;
    }

    public void setId_convo(int id_convo) {
        this.id_convo = id_convo;
    }

    public int getType_convo() {
        return type_convo;
    }

    public void setType_convo(int type_convo) {
        this.type_convo = type_convo;
    }

    public String getListUser() {
        return listUser;
    }

    public void setListUser(String listUser) {
        this.listUser = listUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getIdLastSender() {
        return idLastSender;
    }

    public void setIdLastSender(int idLastSender) {
        this.idLastSender = idLastSender;
    }

    public ConvoType getNewConversation() {
        return newConversation;
    }

    public void setNewConversation(ConvoType newConversation) {
        this.newConversation = newConversation;
    }
}
