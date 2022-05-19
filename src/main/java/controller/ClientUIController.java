package controller;

import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.Listener;
import model.*;
import model.message.Message;
import model.message.MessageContent;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientUIController implements Initializable {
    @FXML
    public Button btnClose;
    public MaterialDesignIconView btnClose_icon;
    public Button btnMax;
    public MaterialDesignIconView btnMax_icon;
    public Button btnMin;
    public MaterialDesignIconView btnMin_icon;
    public Button btnSearch;
    public ImageView ivMore;
    public Circle cLoggedInUser;
    public TextArea messageBox;
    public JFXListView chatPane;
    public JFXListView convoList;
    public Button btnSend;
    public Label lbSelectedConvo;
    public Label lbStatusSelectedConvo;
    public Circle circleAvataSelectedConvo;
    public Button btnOption;
    public Circle circleOption;
    public AnchorPane pane_to_drag_client;
    public AnchorPane pane_to_drag2_client;
    public AnchorPane paneImageExpand;
    public AnchorPane parentAnchorPane;
    public static Image loggedUserImage = null;
    public static InfoUserLogged myUser = LoginUIController.userLogged;
    public static ArrayList<User> listReceiver;
    public ArrayList<Conversation> loggedUserListConvo = new ArrayList<>();
    public static ConvoType convo_type;
    static ArrayList<Message> messageListSelectedConvo;
    public static Conversation convo_selected;

    public Button btnNewConversation;
    public AnchorPane parentChatPane;
    private AnchorPane previousParentChatPane;

    File file;
    SimpleIntegerProperty count;
    Double originChatPane = 730.00;
    DoubleProperty dynamicChatPane = new SimpleDoubleProperty(originChatPane);
    ObservableList<Message> observableList;
    ObservableList<Conversation> observableConvoList;
    boolean max = false;
    public static AnchorPane previousRootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // load image for add file/image button
        Image im = new Image("/images/add_512px.png");
        circleOption.setFill(new ImagePattern(im));
        // set text area auto resize it self based on text
        count = new SimpleIntegerProperty(40);
        int rowHeight = 10;
        messageBox.prefHeightProperty().bindBidirectional(count);
        messageBox.minHeightProperty().bindBidirectional(count);
        messageBox.scrollTopProperty().addListener((ov, oldVal, newVal) -> {
            if(newVal.intValue() > rowHeight){
                count.setValue(count.get() + newVal.intValue());
                double abc = chatPane.getHeight() - newVal.doubleValue();
                DoubleProperty z = new SimpleDoubleProperty(abc);
                chatPane.prefHeightProperty().bindBidirectional(z);
                if (count.get() > 180) {
                    count.setValue(180);
                    chatPane.prefHeightProperty().bindBidirectional(new SimpleDoubleProperty(originChatPane-count.get()));
                    dynamicChatPane = new SimpleDoubleProperty(originChatPane-count.get());
                }
            }
        });
        messageBox.addEventHandler(ActionEvent.ACTION, event -> {
            if (messageBox.getText().equals("") || messageBox.getText() == null) {
                count.setValue(40);
                chatPane.prefHeightProperty().bindBidirectional(new SimpleDoubleProperty(originChatPane));
            }
        });
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });
        try {
            loadUserLoggedIn();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAllConversation();
        try {
            displayListConversation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            loadFirstConversation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadPane(AnchorPane pane) {
        this.paneImageExpand = pane;
        Scene s = pane_to_drag2_client.getScene();
        previousRootPane = parentAnchorPane;
        s.setRoot(paneImageExpand);
    }
    public void returnPreviousScene() {
        Scene s = paneImageExpand.getScene();
        s.setRoot(previousRootPane);
    }
    public void loadUserLoggedIn() throws SQLException, IOException {
        String defaultAvatar = "";
        if (myUser.getGender() == 0) {
            defaultAvatar = "default_male";
        } else if (myUser.getGender() == 1) {
            defaultAvatar = "default_female";
        }
        if (myUser.getAvatar() == null) {
            cLoggedInUser.setStroke(Color.TRANSPARENT);
            Image im = new Image("/images/" + defaultAvatar + ".png");
            cLoggedInUser.setFill(new ImagePattern(im));
            loggedUserImage = im;
        } else {
            cLoggedInUser.setStroke(Color.TRANSPARENT);
            InputStream is = myUser.getAvatar().getBinaryStream();
            BufferedImage imageBuffered = ImageIO.read(is);
            Image image = SwingFXUtils.toFXImage(imageBuffered, null );
            loggedUserImage = image;
            cLoggedInUser.setFill(new ImagePattern(image));
        }
    }
    //get all conversation of logged in user
    public void getAllConversation() {
        try {
            // query get list conversation
            String query1 = "SELECT * FROM list_convo WHERE list_convo.list_user LIKE ? ORDER BY list_convo.priority";
            // query get user of each conversation
            String query2 = "SELECT DISTINCT u.* FROM user as u LEFT JOIN sub_list_convo as s  ON u.id = s.id_user WHERE s.id_convo = ?";
            // query get all message of each conversation
            String query3 = "SELECT message, id_sender FROM messages WHERE id_convo=?";
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial", "root", "");
            //lấy danh sách conversation của logged in user
            PreparedStatement ps = connection.prepareStatement(query1);
            ps.setString(1, "%" + myUser.getId() + "%");
            ResultSet rs = ps.executeQuery();
            //với mỗi một conversation id
            while (rs.next()) {
                // get user of each conversation
                PreparedStatement ps2 = connection.prepareStatement(query2);
                ps2.setInt(1, rs.getInt("id_convo"));
                ResultSet rs2 = ps2.executeQuery();
                Conversation conversation = new Conversation();
                listReceiver = new ArrayList<>();
                while (rs2.next()) {
                    User user = new User();
                    user.setId(rs2.getInt("id"));
//                    String first_name = rs2.getString("first_name");
//                    String last_name = rs2.getString("last_name");
                    String full_name = rs2.getString("full_name");
                    user.setName(full_name);
                    user.setAvatar((com.mysql.cj.jdbc.Blob) rs2.getBlob("avatar"));
                    user.setGender(rs2.getInt("gender"));
                    listReceiver.add(user);
                }
                conversation.setUsers(listReceiver);
                // get all message of each conversation
                PreparedStatement ps3 = connection.prepareStatement(query3);
                ps3.setInt(1, rs.getInt("id_convo"));
                ResultSet rs3 = ps3.executeQuery();
                ArrayList<Message> list_msg = new ArrayList<>();
                while (rs3.next()) {
                    Message msg = new Message();
                    msg.setMsg(rs3.getString("message"));
                    msg.setSender(rs3.getInt("id_sender"));
                    list_msg.add(msg);
                }
                conversation.setMessages(list_msg);
                conversation.setId_convo(rs.getInt("id_convo"));
                conversation.setType_convo(rs.getInt("type_convo"));
                conversation.setListUser(rs.getString("list_user"));
                conversation.setLastMessage(rs.getString("last_message"));
                conversation.setIdLastSender(rs.getInt("id_last_sender"));
                loggedUserListConvo.add(conversation);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //display list conversation of logged in user
    public void displayListConversation() {
        Platform.runLater(() -> {
            observableConvoList = FXCollections.observableList(loggedUserListConvo);
            convoList.setItems(observableConvoList);
            convoList.setCellFactory(new ConversationListRenderer());
        });
    }
    public void loadFirstConversation() {
        if (convoList.getItems() != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    convoList.getSelectionModel().select(0);
                    if (loggedUserListConvo.size() > 0) {
                        loadSelectedConvo(loggedUserListConvo.get(0));
                        displaySelectedConvo(observableConvoList.get(0));
                    }
                }
            });
        }
    }
    private void autoScrollMessageList() {
        if (chatPane.getItems().size() > 10/*where size equals possible items to display*/) {
            chatPane.scrollTo(chatPane.getItems().size() - 1);
        }
    }
    public void loadSelectedConvo(Conversation convo) {
        convo_selected = convo;
        String query = "SELECT message, image, message_content, id_sender FROM messages WHERE id_convo=?";
        String query2 = "(SELECT msg.MID, msg.message, msg.image, msg.message_content, msg.id_sender, fi.file_real_name, fi.file_data, fi.file_extension, fi.file_size FROM messages as msg LEFT JOIN file_info as fi ON msg.file_name_time_stamp=fi.file_name_time_stamp WHERE id_convo=? ORDER BY MID DESC LIMIT 15) ORDER BY MID";
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
            PreparedStatement ps = connection.prepareStatement(query2);
            ps.setInt(1, convo.getId_convo());
            ResultSet rs = ps.executeQuery();
            messageListSelectedConvo = new ArrayList<>();
            while (rs.next()) {
                Message msg = new Message();
                msg.setMsg(rs.getString("message"));
                Blob blob = rs.getBlob("image");
                if (blob != null) {
                    byte[] bytes = blob.getBytes(1, (int) blob.length());
                    msg.setData(bytes);
                    blob.free();
                }
                String fileRealName = rs.getString("file_real_name");
                Blob fileDataBlob = rs.getBlob("file_data");
                if (fileDataBlob != null) {
                    byte[] bytes = fileDataBlob.getBytes(1, (int) fileDataBlob.length());
                    msg.setData(bytes);
                    fileDataBlob.free();
                }
                String fileExtension = rs.getString("file_extension");
                msg.setFileName(fileRealName);
                msg.setFileExtension(fileExtension);
                msg.setFileSize(rs.getString("file_size"));
                msg.setContent(MessageContent.valueOf(rs.getString("message_content")));
                msg.setSender(rs.getInt("id_sender"));
                messageListSelectedConvo.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void displaySelectedConvo(Conversation convo) {
        Blob avt = null;
        String defaultAvatar = "";
        String friendName = "";
        ArrayList<User> users = convo.getUsers();
        User uAva = new User();
        if (convo.getUsers().size() == 1) {
            friendName = convo.getUsers().get(0).getName();
        } else {
            String convoGroupName = "";
            for (User u : users) {
                if (u.getId() != myUser.getId()) {
                    if (convoGroupName.equals("")) {
                        convoGroupName += u.getName();
                    } else {
                        convoGroupName += ", " + u.getName();
                    }
                }
                friendName = convoGroupName;
            }
        }
        for (User s: users) {
            if (s.getId() != ClientUIController.myUser.getId()) {
                uAva = s;
                avt = uAva.getAvatar();
                if (s.getGender() == 0) {
                    defaultAvatar = "default_male";
                } else if (s.getGender() == 1) {
                    defaultAvatar = "default_female";
                }
            }
        }
        Blob finalAvt = avt;
        String finalDefaultAvatar = defaultAvatar;
        String finalFriendName = friendName;
        Platform.runLater(() -> {
            lbSelectedConvo.setText(finalFriendName);
            if (finalAvt == null) {
                //circleAvataSelectedConvo.setStroke(Color.TRANSPARENT);
                Image im = new Image("/images/" + finalDefaultAvatar + ".png");
                circleAvataSelectedConvo.setFill(new ImagePattern(im));
                //loggedUserImage = im;
            } else {
                cLoggedInUser.setStroke(Color.TRANSPARENT);
                // create image from blob data
                InputStream is;
                BufferedImage imageBuffered = null;
                try {
                    is = finalAvt.getBinaryStream();
                    imageBuffered = ImageIO.read(is);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
                Image image = SwingFXUtils.toFXImage(imageBuffered, null );
                circleAvataSelectedConvo.setFill(new ImagePattern(image));
            }
            observableList = FXCollections.observableList(messageListSelectedConvo);
            chatPane.setItems(observableList);
            chatPane.setCellFactory(new ConversationRenderer());
            autoScrollMessageList();
        });
    }
    public synchronized void addToChatView(Message msg) {
        if (msg.getId_convo() == convo_selected.getId_convo()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    observableList.add(msg);
                }
            });
        }
    }
    public void startDownload(Message msg) throws IOException {
        System.out.println("OK DOWNLOAD");
        System.out.println(msg.getFileName());
        String home = System.getProperty("user.home");
        String pathname = home+"/Downloads/" + msg.getFileName();
        FileUtils.writeByteArrayToFile(new File(pathname), msg.getData());
    }
    public void setFriendOnline(Message msg) {
        for (Conversation c : loggedUserListConvo) {
            for (User u : c.getUsers()) {
                if (u.getId() == msg.getUser_sender().getId()) {
                    u.setStatus(Status.ONLINE);
                }
                for (User uu : msg.getOnlineList()) {
                    if (u.getId() == uu.getId()) {
                        u.setStatus(Status.ONLINE);
                    }
                    //System.out.println(uu.getName());
                }
            }
        }
        displayListConversation();
    }
    @FXML
    private void close(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        s.close();
        Platform.exit();
        System.exit(0);
    }

    public void max(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        if (!max) {
            chatPane.setPrefHeight(chatPane.getPrefHeight() + 200);
            s.setFullScreen(true);
            max = true;
        } else {
            if (messageBox.getText() != null || messageBox.getText() != "") {
                chatPane.setPrefHeight(chatPane.getPrefHeight() - 200);
                chatPane.prefHeightProperty().bindBidirectional(dynamicChatPane);
            } else {
                chatPane.setPrefHeight(chatPane.getPrefHeight() - 200);
                chatPane.setPrefHeight(originChatPane);
            }
            s.setFullScreen(false);
            max = false;
        }
    }

    public void min(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        s.setIconified(true);
    }

    public void search(MouseEvent mouseEvent) {
    }

    public void more(MouseEvent mouseEvent) {
    }

    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (!messageBox.getText().isEmpty()) {
            Listener.sendPrivate(msg);
            messageBox.clear();
            count.setValue(40);
            chatPane.prefHeightProperty().bindBidirectional(new SimpleDoubleProperty(originChatPane));
        }
    }
    public void OptionLoggedUser(MouseEvent mouseEvent) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Upload your avatar");
        item1.setOnAction(event -> {
            Stage stage = new Stage();
            try {
                AnchorPane root = FXMLLoader.load(getClass().getResource("/UI/UploadAvatarUI.fxml"));
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                stage.setScene(scene);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.getIcons().add(new Image("/images/logo.png"));
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MenuItem item2 = new MenuItem("Log out");
        item2.setOnAction(event -> {
            try {
                AnchorPane root = FXMLLoader.load(getClass().getResource("/UI/AfterSplashUI.fxml"));
                Stage newStage = new Stage();
                Scene scene = new Scene(root);
                newStage.setScene(scene);

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        contextMenu.getItems().addAll(item1,item2);
        contextMenu.show(cLoggedInUser, mouseEvent.getScreenX(), mouseEvent.getScreenY());
    }

    public void showOptions(MouseEvent mouseEvent) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem voice = new MenuItem("Record a Voice Clip");
        MenuItem attachment = new MenuItem("Add Attachment(s)");
        attachment.setOnAction(event -> {
            try {
                chooseFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        contextMenu.getItems().addAll(voice,attachment);
        contextMenu.show(btnOption, mouseEvent.getScreenX()+5, mouseEvent.getScreenY()+10);
    }
    public void chooseFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        configuringFileChooser(fileChooser);
        Stage stage = new Stage();
        file = fileChooser.showOpenDialog(stage);
        if(file != null) {
            String fileName = file.getName();
            int lastIndexOf = fileName.lastIndexOf(".");
            String fileExtension = fileName.substring(lastIndexOf);
            String fileType = Files.probeContentType(file.toPath());
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            // Get length of file in bytes
            String fileSizeInBytes = df.format(FileUtils.sizeOf(file));
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            String fileSizeInKB = df.format(FileUtils.sizeOf(file)/1024);
            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            String fileSizeInMB = df.format(FileUtils.sizeOf(file)/1024/1024);
            if (file != null && Double.parseDouble(fileSizeInMB) <= 25) {
                Optional<ButtonType> result = myAlert(Alert.AlertType.CONFIRMATION, "Do you want to send this attachment?"
                        , ""
                        , "/images/info_32px.png"
                        , "/images/info_512px.png");
                if (result.get() == ButtonType.OK) {
                    String senderName = myUser.getFirstName() + " " + myUser.getLastName();
                    int id_cv = convo_selected.getId_convo();
                    if (fileType != null && fileType.split("/")[0].equals("image")) {
                        Listener.sendAttachment(senderName, id_cv, FileUtils.readFileToByteArray(file));
                    } else {
                        if (Double.parseDouble(fileSizeInMB) >= 1) {
                            Listener.sendFile(senderName, id_cv, FileUtils.readFileToByteArray(file), fileExtension, fileName, fileSizeInMB + " MB");
                        } else if (Double.parseDouble(fileSizeInKB) < 1){
                            Listener.sendFile(senderName, id_cv, FileUtils.readFileToByteArray(file), fileExtension, fileName, fileSizeInBytes + " B");
                        } else {
                            Listener.sendFile(senderName, id_cv, FileUtils.readFileToByteArray(file), fileExtension, fileName, fileSizeInKB + " KB");
                        }
                    }
                }
            } else {
                myAlert(Alert.AlertType.INFORMATION, "The file you have selected is too large"
                        , "The file you have selected is too large. The maximum size is 25MB."
                        , "/images/info_32px.png"
                        , "/images/info_512px.png");
            }
        }
    }
    private void configuringFileChooser(FileChooser fileChooser) {
        // Set tiêu đề cho FileChooser
        fileChooser.setTitle("Select Pictures");
        // Sét thư mục bắt đầu nhìn thấy khi mở FileChooser
        fileChooser.setInitialDirectory(new File("C:/Users"));
        // Thêm các bộ lọc file vào
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));
    }
    private Optional<ButtonType> myAlert(Alert.AlertType alertType, String header, String content, String graphic, String icon) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setGraphic(new ImageView(graphic));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(icon).toString()));
        return alert.showAndWait();
    }
    public void closeStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void showEmoji(MouseEvent mouseEvent) {

    }

    public void showSticker(MouseEvent mouseEvent) {

    }

    public void createNewConversation(MouseEvent mouseEvent) throws IOException {
        if (loggedUserListConvo.get(0).getNewConversation() != ConvoType.NEW || observableConvoList.size() == 0) {
            Conversation newConversation = new Conversation();
            newConversation.setNewConversation(ConvoType.NEW);
            convoList.getItems().add(0, newConversation);
            convoList.getSelectionModel().select(0);

            FXMLLoader newConversationLoader = new FXMLLoader();
            URL path = getClass().getResource("/UI/NewConversationDetailPane.fxml");
            newConversationLoader.setLocation(path);
            AnchorPane newConversationDetailPane = newConversationLoader.load();
            ReceiverSearchResultRenderer.detailPaneController = newConversationLoader.getController();
            loadPaneForCreateNewConversation(newConversationDetailPane);
        }
    }
    public void cancelNewConversation() {
        convoList.getItems().remove(0);
    }
    public void displayFirstAfterCancel(int i) {
        loadSelectedConvo(observableConvoList.get(i));
        displaySelectedConvo(observableConvoList.get(i));
    }
    public void loadPaneForCreateNewConversation(AnchorPane pane) {
        previousParentChatPane = new AnchorPane();
        previousParentChatPane.getChildren().setAll(parentChatPane.getChildren());
        parentChatPane.getChildren().setAll(pane);
    }
    public void returnPreviousParentChatPane() {
        parentChatPane.getChildren().setAll(previousParentChatPane);
    }
    public synchronized void updateListConversation(Message msg) {
        getSpecificConversation(msg);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                convoList.getItems().remove(0);
                observableConvoList.add(0, loggedUserListConvo.get(loggedUserListConvo.size()-1));
                convoList.getSelectionModel().select(0);
                returnPreviousParentChatPane();
                displayFirstAfterCancel(0);
            }
        });
        ConversationListRenderer.convo_selected = 0;
    }
    public void getSpecificConversation(Message msg) {
        try {
            // query get list conversation
            String query1 = "SELECT * FROM list_convo WHERE id_convo = ?";
            // query get user of each conversation
            String query2 = "SELECT DISTINCT u.* FROM user as u LEFT JOIN sub_list_convo as s  ON u.id = s.id_user WHERE s.id_convo = ?";
            // query get all message of each conversation
            String query3 = "SELECT message, id_sender FROM messages WHERE id_convo=?";
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial", "root", "");
            //lấy danh sách conversation của logged in user
            PreparedStatement ps = connection.prepareStatement(query1);
            ps.setInt(1, msg.getId_convo());
            ResultSet rs = ps.executeQuery();
            //với mỗi một conversation id
            while (rs.next()) {
                // get user of each conversation
                PreparedStatement ps2 = connection.prepareStatement(query2);
                ps2.setInt(1, rs.getInt("id_convo"));
                ResultSet rs2 = ps2.executeQuery();
                Conversation conversation = new Conversation();
                listReceiver = new ArrayList<>();
                while (rs2.next()) {
                    User user = new User();
                    user.setId(rs2.getInt("id"));
//                    String first_name = rs2.getString("first_name");
//                    String last_name = rs2.getString("last_name");
                    String full_name = rs2.getString("full_name");
                    user.setName(full_name);
                    user.setAvatar((com.mysql.cj.jdbc.Blob) rs2.getBlob("avatar"));
                    user.setGender(rs2.getInt("gender"));
                    listReceiver.add(user);
                }
                conversation.setUsers(listReceiver);
                // get all message of each conversation
                PreparedStatement ps3 = connection.prepareStatement(query3);
                ps3.setInt(1, rs.getInt("id_convo"));
                ResultSet rs3 = ps3.executeQuery();
                ArrayList<Message> list_msg = new ArrayList<>();
                while (rs3.next()) {
                    Message msgg = new Message();
                    msgg.setMsg(rs3.getString("message"));
                    msgg.setSender(rs3.getInt("id_sender"));
                    list_msg.add(msgg);
                }
                conversation.setMessages(list_msg);
                conversation.setId_convo(rs.getInt("id_convo"));
                conversation.setType_convo(rs.getInt("type_convo"));
                conversation.setListUser(rs.getString("list_user"));
                conversation.setLastMessage(rs.getString("last_message"));
                conversation.setIdLastSender(rs.getInt("id_last_sender"));
                loggedUserListConvo.add(conversation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

