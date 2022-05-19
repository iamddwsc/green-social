package model.message;

import com.mysql.cj.jdbc.Blob;
import model.Status;
import model.User;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private String msg;
    private int sender;
    private ArrayList<User> receiver;
    private User user_sender;
    private MessageType type;
    private Status status;
    transient Blob image;
    private int id_convo;
    private MessageContent content;
    private byte[] data;
    private ArrayList<User> onlineList;
    private String fileExtension;
    private String fileName;
    private String fileSize;
    private String time_create;

    public ArrayList<User> getOnlineList() {
        return onlineList;
    }

    public void setOnlineList(ArrayList<User> onlineList) {
        this.onlineList = onlineList;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public ArrayList<User> getReceiver() {
        return receiver;
    }

    public void setReceiver(ArrayList<User> receiver) {
        this.receiver = receiver;
    }

    public User getUser_sender() {
        return user_sender;
    }

    public void setUser_sender(User user_sender) {
        this.user_sender = user_sender;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public int getId_convo() {
        return id_convo;
    }

    public void setId_convo(int id_convo) {
        this.id_convo = id_convo;
    }

    public MessageContent getContent() {
        return content;
    }

    public void setContent(MessageContent content) {
        this.content = content;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getTime_create() {
        return time_create;
    }

    public void setTime_create(String time_create) {
        this.time_create = time_create;
    }
}
