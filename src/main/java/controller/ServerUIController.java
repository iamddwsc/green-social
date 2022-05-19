package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jdk.nashorn.internal.ir.WhileNode;
import model.Conversation;
import model.InfoUserLogged;
import model.message.Message;
import model.User;
import model.message.MessageContent;
import model.message.MessageType;
import server.Notice;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ServerUIController implements Initializable {
    public TextField tfPort;
    public JFXButton btnStartServer;
    public JFXCheckBox cbPortEditable;
    public Label lbStatus;
    public JFXTextArea taServerNotice;

    //socket variable
    private static HashMap<String, ObjectOutputStream> writers = new HashMap<>();
    private static HashSet<Integer> checkConvo = new HashSet<>();
    private static ArrayList<User> listUserOnline = new ArrayList<>();
    private static int svPort;
    public static Notice notice;
    private Message msgNew;
    private Conversation newConversation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void updateNotice() {
        this.taServerNotice.appendText(notice.getMsg() + "\n");
    }
    public void startServer(MouseEvent mouseEvent) {
        String validPort = tfPort.getText();
        if (validPort(validPort) && !validPort.equals("")) {
            svPort = Integer.parseInt(validPort);
            Thread runServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    ServerSocket listener = null;
                    Connection connection = null;
                    try {
                        listener = new ServerSocket(svPort);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                lbStatus.setText("Running...");
                            }
                        });
                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO server_running(host, port) value (?,?) ON DUPLICATE KEY UPDATE port=?");
                        ps.setString(1, "localhost");
                        ps.setInt(2, Integer.parseInt(validPort));
                        ps.setInt(3, Integer.parseInt(validPort));
                        ps.executeUpdate();
                        while (true) {
                            new ClientHandler(listener.accept()).start();
                            System.out.println("new accepted");
                        }
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            listener.close();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    lbStatus.setText("Stopped");
                                }
                            });
                            PreparedStatement ps2 = connection.prepareStatement("DELETE FROM server_running WHERE host=?");
                            ps2.setString(1, "localhost");
                            ps2.executeUpdate();
                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            runServer.setDaemon(true);
            runServer.start();
        } else {
            myAlert(Alert.AlertType.ERROR, "Wrong port."
                    , "Please type number."
                    , "/images/cancel_32px.png"
                    , "/images/cancel_512px.png");
        }

    }
    private class ClientHandler extends Thread {
        private String name; //name of client connected
        private Socket socket;
        private User user;
        private ObjectInputStream ois;
        private InputStream is;
        private ObjectOutputStream oos;
        private OutputStream os;

        public ClientHandler(Socket socket) throws IOException{
            this.socket = socket;
        }

        public void run() {
            try {
                is = socket.getInputStream();
                ois = new ObjectInputStream(is);
                os = socket.getOutputStream();
                oos = new ObjectOutputStream(os);

                Message firstMessage = (Message) ois.readObject();
                writers.put(String.valueOf(firstMessage.getSender()), oos);
                notice = new Notice();
                notice.setMsg("User ID = " + firstMessage.getSender() + " " + firstMessage.getMsg());
                addToListOnline(firstMessage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateNotice();
                    }
                });
                while (socket.isConnected()) {
                    Message inputMsg = (Message) ois.readObject();
                    if (inputMsg != null) {
                        switch (inputMsg.getType()) {
                            case PRIVATE:
                                writePrivate(inputMsg);
                                saveData(inputMsg);
                                //System.out.println(Arrays.toString(inputMsg.getData()));
                                break;
                            case ONLINE:
                                addToListOnline(inputMsg);
                                break;
                            case NEW:
                                createNewConversation(inputMsg);
                                Message sendMsg = inputMsg;
                                sendMsg.setId_convo(msgNew.getId_convo());
                                writePrivate(sendMsg);
                                break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {

            }
        }
        private void writePrivate(Message msg) {
            ArrayList<User> person = msg.getReceiver();
            for (User p : person) {
                for (Map.Entry<String, ObjectOutputStream> writer : writers.entrySet()) {
                    if (writer.getKey().matches(String.valueOf(p.getId()))) {
                        try {
                            writer.getValue().writeObject(msg);
                            //save data to database here
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
//        private void writeNewConversation(Message msg) {
//            Message msgNew = msg;
//            msgNew.setId_convo();
//        }
        private void addToListOnline(Message msg) throws IOException {
            listUserOnline.add(msg.getUser_sender());
            Message onlineMessage = new Message();
            //onlineMessage.setReceiver(listUserOnline);
            onlineMessage.setType(MessageType.ONLINE);
            onlineMessage.setUser_sender(msg.getUser_sender());
            onlineMessage.setOnlineList(listUserOnline);
            for (User u : listUserOnline) {
                for (Map.Entry<String, ObjectOutputStream> writer : writers.entrySet()) {
                    if (writer.getKey().matches(String.valueOf(u.getId()))) {
                        writer.getValue().writeObject(onlineMessage);
                    }
                }
            }
            //oos.writeObject(onlineMessage);
        }
    }
    // save to data base
    private void saveData(Message msg) throws SQLException {
        String query = "INSERT INTO messages(id_convo, message, image, file_name_time_stamp, message_content, id_sender) values(?,?,?,?,?,?) ";
        String query2 = "INSERT INTO file_info(file_name_time_stamp, file_real_name, file_data, file_extension, file_size) values(?,?,?,?,?)";
        String query3 = "UPDATE list_convo SET last_message = ?, id_last_sender = ? WHERE id_convo = ?";
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
        PreparedStatement ps = connection.prepareStatement(query);
        PreparedStatement ps2 = connection.prepareStatement(query2);
        PreparedStatement ps3 = connection.prepareStatement(query3);
        ps.setInt(1, msg.getId_convo());
        ps.setString(2, msg.getMsg());
        if (msg.getData() != null) {
            if (msg.getContent() == MessageContent.IMAGE) {
                ByteArrayInputStream bais = new ByteArrayInputStream(msg.getData());
                ps.setBinaryStream(3, bais, msg.getData().length); // this is data of image
                ps.setString(4, null);
            } else {
                ps.setBinaryStream(3, null);
            }
            // name of file base on date time upload and file data
            if (msg.getContent() == MessageContent.FILE) {
                ps.setBinaryStream(3, null);
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String fileNameTimeStamp = timeStamp + msg.getFileExtension();
                System.out.println(fileNameTimeStamp);
                ps.setString(4, fileNameTimeStamp);
                ps2.setString(1, fileNameTimeStamp);
                ps2.setString(2, msg.getFileName());
                ByteArrayInputStream file_data = new ByteArrayInputStream(msg.getData());
                ps2.setBinaryStream(3, file_data, msg.getData().length);
                ps2.setString(4, msg.getFileExtension());
                ps2.setString(5, msg.getFileSize());

            } else {
                ps.setString(4, null);
            }
        } else {
            ps.setBinaryStream(3, null);
            ps.setString(4, null);
        }
        ps.setString(5, String.valueOf(msg.getContent()));
        ps.setInt(6, msg.getSender());
        int rs = ps.executeUpdate();
        if (rs == 1) {
            notice = new Notice();
            notice.setMsg("Data saved");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateNotice();
                }
            });
        }
        if (msg.getContent() == MessageContent.FILE) {
            int rs2 = ps2.executeUpdate();
            if (rs2 == 1) {
                notice = new Notice();
                notice.setMsg("Data saved to file_info table");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateNotice();
                    }
                });
            }
        }
        ps3.setString(1,msg.getMsg());
        ps3.setInt(2, msg.getSender());
        ps3.setInt(3, msg.getId_convo());
        int rs3 = ps3.executeUpdate();
        if (rs3 == 1) {
            notice = new Notice();
            notice.setMsg("Data updated to list_convo table");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateNotice();
                }
            });
        }
    }
    // save info of new conversation
    private void createNewConversation(Message msg) throws SQLException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String time_create = msg.getSender() + "_" + timeStamp;
        String query = "INSERT INTO list_convo(type_convo, list_user, last_message, id_last_sender, time_create, priority) VALUES(?,?,?,?,?,?)";
        String query2 = "SELECT id_convo FROM list_convo WHERE time_create = ?";
        String query3 = "UPDATE list_convo SET priority = ? WHERE id_convo = ?";
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
        PreparedStatement ps = connection.prepareStatement(query);
        PreparedStatement ps2 = connection.prepareStatement(query2);
        PreparedStatement ps3 = connection.prepareStatement(query3);
        if (msg.getReceiver().size() > 1) {
            ps.setInt(1,2);
        } else {
            ps.setInt(1,1);
        }
        String s = "";
        for (User u : msg.getReceiver()) {
            s+=u.getId() + ",";
        }
        s = s.substring(0,s.lastIndexOf(","));
        ps.setString(2,s);
        ps.setString(3, msg.getMsg());
        ps.setInt(4, msg.getSender());
        ps.setString(5, time_create);
        ps.setInt(6, 0);
        int rs = ps.executeUpdate();
        if (rs == 1) {
            notice = new Notice();
            notice.setMsg("New conversation created");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateNotice();
                }
            });
        }
        // get data of conversation was created
        ps2.setString(1, time_create);
        ResultSet rs2 = ps2.executeQuery();
        while (rs2.next()) {
            msgNew = new Message();
            msgNew.setId_convo(rs2.getInt("id_convo"));
        }
        // update data for priority column with id_convo data
        ps3.setString(1, String.valueOf(msgNew.getId_convo()));
        ps3.setInt(2, msgNew.getId_convo());
        int rs3 = ps3.executeUpdate();
        if (rs3 == 1) {
            notice = new Notice();
            notice.setMsg("Update priority to true value");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateNotice();
                }
            });
        }
        // create server message
        Message serverMsg = new Message();
        serverMsg.setId_convo(msgNew.getId_convo());
        serverMsg.setMsg(msg.getUser_sender().getFullName()+" vừa bắt đầu cuộc trò chuyện!");
        serverMsg.setContent(MessageContent.SERVER);
        serverMsg.setSender(msg.getSender());
        saveData(serverMsg);
        msg.setId_convo(msgNew.getId_convo());
        saveData(msg); // sql here
        // update data for sub_list_convo
        String query4 = "INSERT INTO sub_list_convo(id_convo, id_user) values(?,?)";
        PreparedStatement ps4 = connection.prepareStatement(query4);
        for (User u : msg.getReceiver()) {
            ps4.setInt(1, msgNew.getId_convo());
            ps4.setInt(2, u.getId());
            int i = ps4.executeUpdate();
            if (i == 1) {
                notice = new Notice();
                notice.setMsg("Update user " + u.getId() + " to sub_list_convo table!!");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateNotice();
                    }
                });
            }
        }
//        String swapPriority = "START TRANSACTION ;\n" +
//                "    UPDATE list_convo \n" +
//                "    SET priority = \n" +
//                "      CASE\n" +
//                "        WHEN priority = 1 THEN -7 \n" +
//                "        WHEN priority = 7 THEN -1 \n" +
//                "      END \n" +
//                "    WHERE priority IN (1,7) ;\n" +
//                "\n" +
//                "    UPDATE list_convo \n" +
//                "    SET priority = - priority\n" +
//                "    WHERE priority IN (-1,-7) ;\n" +
//                "COMMIT ;";

        //receive the first and the newly added row
        String getPriority ="(select *from list_convo order by priority ASC LIMIT 1) UNION (select *from list_convo WHERE id_convo = ?)";
        PreparedStatement psGetPriority = connection.prepareStatement(getPriority);
        psGetPriority.setInt(1, msgNew.getId_convo());
        ResultSet rsGetPriority = psGetPriority.executeQuery();
        ArrayList<Integer> priorityList = new ArrayList<>();
        while (rsGetPriority.next()) {
            priorityList.add(rsGetPriority.getInt("priority"));
        }
        // swap priority of conversation
        //String swapPriority = "(UPDATE list_convo SET priority = CASE WHEN priority = ? THEN ? WHEN priority = ? THEN ? END WHERE priority IN (?,?)) UNION (UPDATE list_convo SET priority = - priority WHERE priority IN (?,?)) UNION COMMIT";
        String swapPriority1 = "UPDATE list_convo SET priority = CASE WHEN priority = ? THEN ? WHEN priority = ? THEN ? END WHERE priority IN (?,?)";
        String swapPriority2 = "UPDATE list_convo SET priority = -priority WHERE priority IN (?,?)";
        PreparedStatement psSwapPriority1 = connection.prepareStatement(swapPriority1);
        PreparedStatement psSwapPriority2 = connection.prepareStatement(swapPriority2);
        psSwapPriority1.setInt(1, priorityList.get(0));
        psSwapPriority1.setInt(2, -priorityList.get(1));
        psSwapPriority1.setInt(3, priorityList.get(1));
        psSwapPriority1.setInt(4, -priorityList.get(0));
        psSwapPriority1.setInt(5, priorityList.get(0));
        psSwapPriority1.setInt(6, priorityList.get(1));
        psSwapPriority2.setInt(1, -priorityList.get(0));
        psSwapPriority2.setInt(2, -priorityList.get(1));
        int i = psSwapPriority1.executeUpdate();
        int j = psSwapPriority2.executeUpdate();
        if (i == 1 && j == 1) {
            notice = new Notice();
            notice.setMsg("Swapped between " + priorityList.get(0) + " and " + priorityList.get(1));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateNotice();
                }
            });
        }

    }
    // check port
    private boolean validPort(String port) {
        for (char c : port.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
    // custom alert
    private void myAlert(Alert.AlertType alertType, String header, String content, String graphic, String icon) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setGraphic(new ImageView(graphic));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(icon).toString()));
        stage.showAndWait();
    }
}