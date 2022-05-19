package model;

import controller.ClientUIController;
import controller.ImageExpandController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import main.Listener;
import model.message.Message;
import model.message.MessageContent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class ConversationRenderer implements Callback<ListView<Message>, ListCell<Message>> {

    private URL path;
    private URL pathFriendField;
    private ClientUIController clientUIController = Listener.clientUIController;


    @Override
    public ListCell<Message> call(ListView<Message> param) {
        ListCell<Message> cell = new ListCell<Message>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                setGraphic(null);
                setText(null);
                if (msg != null) {
                    if (msg.getContent() == MessageContent.SERVER && msg.getMsg() != null) {
                        FXMLLoader loader = new FXMLLoader();
                        path = getClass().getResource("/UI/MessageCellServer.fxml");
                        try {
                            loader.setLocation(path);
                            HBox hBox = loader.load();
                            Label tfMessageBox = (Label) hBox.getChildren().get(0);
                            tfMessageBox.setText(msg.getMsg());
                            tfMessageBox.setMaxWidth(400);
                            tfMessageBox.setWrapText(true);
                            hBox.setAlignment(Pos.CENTER);
                            hBox.getChildren().set(0, tfMessageBox);
                            setGraphic(hBox);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getContent() == MessageContent.TEXT && msg.getMsg() != null) {
                        FXMLLoader loader = new FXMLLoader();
                        if (msg.getSender() == ClientUIController.myUser.getId()) {
                            path = getClass().getResource("/UI/MessageCellMe.fxml");
                        } else {
                            path = getClass().getResource("/UI/MessageCellFriend.fxml");
                        }
                        try {
                            loader.setLocation(path);
                            HBox hBox = loader.load();
                            Label tfMessageBox = (Label) hBox.getChildren().get(0);
                            tfMessageBox.setText(msg.getMsg());
                            tfMessageBox.setMaxWidth(400);
                            tfMessageBox.setWrapText(true);
                            if (msg.getSender() == ClientUIController.myUser.getId()) {
                                hBox.setAlignment(Pos.CENTER_RIGHT);
                            } else {
                                tfMessageBox
                                        .setBackground(new Background(new BackgroundFill(Color.rgb(242, 242, 242), CornerRadii.EMPTY, Insets.EMPTY)));
                                hBox.setAlignment(Pos.CENTER_LEFT);
                            }
                            hBox.getChildren().set(0, tfMessageBox);
                            setGraphic(hBox);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getContent() == MessageContent.IMAGE && msg.getData() != null) {
                        FXMLLoader loader = new FXMLLoader();
                        URL path = getClass().getResource("/UI/MessageCell_Image.fxml");
                        URL pathExpand = getClass().getResource("/UI/ImageExpand.fxml");
                        try {
                            loader.setLocation(path);
                            HBox hBox = loader.load();
                            AnchorPane pane = (AnchorPane) hBox.getChildren().get(0);
                            Button btnExpand = (Button) pane.getChildren().get(1);
                            btnExpand.setVisible(false);
                            btnExpand.setOnMouseEntered(event -> {
                                btnExpand.setVisible(true);
                            });
                            btnExpand.setOnAction(event -> {
                                try {
                                    //load fxml file for UI
                                    FXMLLoader imgExpandLoader = new FXMLLoader();
                                    imgExpandLoader.setLocation(pathExpand);
                                    AnchorPane imageExpandPane = imgExpandLoader.load();
                                    //
                                    clientUIController.loadPane(imageExpandPane); //load expand image pane into current display pane to display the image expand
                                    ImageExpandController iec = imgExpandLoader.getController();
                                    iec.dataImageExpanded(msg);
                                    iec.updateImageView(); //lines 102-104 -> load msg data of image for controller of the pane above
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            pane.getChildren().set(1, btnExpand);
                            ImageView img = (ImageView) pane.getChildren().get(0);
                            ByteArrayInputStream bais = new ByteArrayInputStream(msg.getData());
                            BufferedImage imageBuffered = ImageIO.read(bais);
                            Image image = SwingFXUtils.toFXImage(imageBuffered, null);
                            img.setImage(image);
                            img.setOnMouseEntered(event -> {
                                btnExpand.setVisible(true);
                            });
                            img.setOnMouseExited(event -> {
                                btnExpand.setVisible(false);
                            });
                            pane.getChildren().set(0, img);
                            hBox.getChildren().set(0, pane);
                            if (msg.getSender() == ClientUIController.myUser.getId()) {
                                hBox.setAlignment(Pos.CENTER_RIGHT);
                            } else hBox.setAlignment(Pos.CENTER_LEFT);
                            setGraphic(hBox);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getContent() == MessageContent.FILE) {
                        FXMLLoader loader = new FXMLLoader();
                        URL path = getClass().getResource("/UI/MessageCell_File.fxml");
                        URL url = getClass().getResource("/images/file_icon/docx.png");
                        File f = new File(url.getFile());
                        File ff = f.getParentFile();
                        ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
                        String imageType = "";
                        for (String s : names) {
                            if (s.contains(msg.getFileExtension().substring(1))) {
                                imageType = s;
                                break;
                            }
                        }
                        try {
                            loader.setLocation(path);
                            HBox parent = loader.load();
                            HBox hBox = (HBox) parent.getChildren().get(0);
                            // load right image of file type
                            ImageView imgView = (ImageView) hBox.getChildren().get(0);
                            Image img = new Image("/images/file_icon/" + imageType);
                            imgView.setImage(img);
                            hBox.getChildren().set(0, imgView);
                            // load file name
                            VBox vBox = (VBox) hBox.getChildren().get(1); // load VBox from HBox
                            Label fileName = (Label) vBox.getChildren().get(0); // load label of file name
                            Label fileSize = (Label) vBox.getChildren().get(1); // load label of file size
                            fileName.setText(msg.getFileName());
                            fileSize.setText(msg.getFileSize());
                            vBox.getChildren().set(0, fileName);
                            vBox.getChildren().set(1, fileSize);
                            hBox.getChildren().set(1, vBox);
                            Button btnDownload = (Button) hBox.getChildren().get(2);
                            btnDownload.setOnAction(ev -> {
                                try {
                                    clientUIController.startDownload(msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            hBox.getChildren().set(2, btnDownload);
                            if (msg.getSender() == ClientUIController.myUser.getId()) {
                                parent.setAlignment(Pos.CENTER_RIGHT);
                            } else parent.setAlignment(Pos.CENTER_LEFT);
                            setGraphic(parent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        return cell;
    }
}
