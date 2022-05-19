package model;

import controller.ClientUIController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.message.Message;
import model.message.MessageContent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

public class ConvoRenderer implements Callback<ListView<Message>, ListCell<Message>> {

    private URL path;

    @Override
    public ListCell<Message> call(ListView<Message> param) {
        ListCell<Message> cell = new ListCell<Message>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                setGraphic(null);
                setText(null);
                if (msg != null) {
                    if (msg.getContent() == MessageContent.TEXT && msg.getMsg() != null) {
                        FXMLLoader loader = new FXMLLoader();
                        path = getClass().getResource("/UI/MessageCell.fxml");
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
                        try {
                            loader.setLocation(path);
                            HBox hBox = loader.load();
                            ImageView img = (ImageView) hBox.getChildren().get(0);
                            ByteArrayInputStream bais = new ByteArrayInputStream(msg.getData());
                            BufferedImage imageBuffered = ImageIO.read(bais);
                            Image image = SwingFXUtils.toFXImage(imageBuffered, null);
                            img.setImage(image);
                            hBox.getChildren().set(0, img);
                            if (msg.getSender() == ClientUIController.myUser.getId()) {
                                hBox.setAlignment(Pos.CENTER_RIGHT);
                            } else hBox.setAlignment(Pos.CENTER_LEFT);
                            setGraphic(hBox);
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
