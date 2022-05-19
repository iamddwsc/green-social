package model;

import controller.ClientUIController;
import controller.LoginUIController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import main.Listener;
import model.message.Message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

public class ConversationListRenderer implements Callback<ListView<Conversation>, ListCell<Conversation>> {
    @FXML
    public Circle cirFriendAvatar;
    public Label lbFriendName;
    public Label lbTime;
    public Label lbLastChat;

    private URL path;
    private AnchorPane anchorPane;
    private HBox hBox;

    public static int convo_selected = 0;

    @Override
    public ListCell<Conversation> call(ListView<Conversation> param) {

        ListCell<Conversation> cell = new ListCell<Conversation>() {
            @Override
            protected void updateItem(Conversation convo, boolean empty) {
                super.updateItem(convo, empty);
                setGraphic(null);
                setText(null);
                //load multiple cell.fxml file - use for each conversation
                if (convo != null) {
                    if (convo.getNewConversation() == ConvoType.NEW) {
                        FXMLLoader loader = new FXMLLoader();
                        path = getClass().getResource("/UI/NewConversation.fxml");
                        loader.setLocation(path);
                        try {
                            HBox newMessage = loader.load();
                            setGraphic(newMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        FXMLLoader loader = new FXMLLoader();
                        path = getClass().getResource("/UI/Cell.fxml");
                        try {
                            loader.setLocation(path);
                            anchorPane = loader.load();
                            //get(0) -> circle(image) | get(1) -> conversation display name | get(2) -> hBox contain last message and time
                            hBox = (HBox) anchorPane.getChildren().get(2);
                            Circle status = (Circle) anchorPane.getChildren().get(4);
                            ArrayList<User> users = convo.getUsers();
                            User uAva = new User();
                            int onlineCount = 0;
                            for (User s : users) {
                                //System.out.println(s.getId());
                                if (s.getId() != ClientUIController.myUser.getId()) {
                                    uAva = s;
                                }
                                if (s.getId() != ClientUIController.myUser.getId()) {
                                    if (s.getStatus() == Status.ONLINE) {
                                        onlineCount += 1;
                                    }

                                }
                            }
                            if (onlineCount > 0) {
                                status.setFill(Color.rgb(49,204,70));
                            } else {
                                status.setFill(Color.rgb(179,179,179));
                            }
                            anchorPane.getChildren().set(4, status);
                            // friend avatar
                            Circle cir = (Circle) anchorPane.getChildren().get(0);
                            String defaultAvatar = "";
                            if (uAva.getAvatar() != null) {
                                InputStream is = uAva.getAvatar().getBinaryStream();
                                BufferedImage imageBuffered = ImageIO.read(is);
                                Image image = SwingFXUtils.toFXImage(imageBuffered, null );
                                cir.setFill(new ImagePattern(image));
                            } else {
                                if (uAva.getGender() == 0) {
                                    defaultAvatar = "default_male";
                                } else if (uAva.getGender() == 1) {
                                    defaultAvatar = "default_female";
                                }
                                cir.setStroke(Color.TRANSPARENT);
                                Image im = new Image("/images/" + defaultAvatar + ".png");
                                cir.setFill(new ImagePattern(im));
                            }
                            anchorPane.getChildren().set(0, cir);
                            //friend name
                            Label convoName = (Label) anchorPane.getChildren().get(1);
                            if (users.size() == 1) {
                                convoName.setText(users.get(0).getName());
                                anchorPane.getChildren().set(1, convoName);
                            } else {
                                String convoGroupName = "";
                                for (User u : users) {
                                    if (u.getId() != ClientUIController.myUser.getId()) {
                                        if (convoGroupName.equals("")) {
                                            convoGroupName += u.getName();
                                        } else {
                                            convoGroupName += ", " + u.getName();
                                        }
                                    }
                                    convoName.setText(convoGroupName);
                                    anchorPane.getChildren().set(1, convoName);
                                }
                            }
                            // last message
                            Label last_message = (Label) hBox.getChildren().get(0);
                            last_message.setText(convo.getLastMessage());
                            hBox.getChildren().set(0, last_message);
                            setGraphic(anchorPane);
                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && (!cell.isEmpty())) {
                Conversation convo = cell.getItem();
                if (convo.getNewConversation() != ConvoType.NEW) {
                    if (convo_selected != convo.getId_convo()) {
                        convo_selected = convo.getId_convo();
                        ConvoType ct;
                        if (convo.getUsers().size() == 1) {
                            ct = ConvoType.PRIVATE;
                        } else {
                            ct = ConvoType.GROUP;
                        }
                        ClientUIController.convo_type = ct;
                        loadSelected(convo);
                        if (cell.getListView().getItems().get(0).getNewConversation() == ConvoType.NEW) {
                            Listener.clientUIController.cancelNewConversation();
                            Listener.clientUIController.returnPreviousParentChatPane();
                        }
                    }
                }
            }
        });
        return cell;
    }
    private void loadSelected(Conversation convo) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Listener.clientUIController.loadSelectedConvo(convo);
                Listener.clientUIController.displaySelectedConvo(convo);
            }
        });
    }
}
