package model;

import controller.NewConversationDetailPaneController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ReceiverSearchResultRenderer implements Callback<ListView<User>, ListCell<User>> {

    public static NewConversationDetailPaneController detailPaneController;

    @Override
    public ListCell<User> call(ListView<User> param) {
        ListCell<User> cell = new ListCell<User>() {
            @Override
            protected void updateItem(User result, boolean empty) {
                super.updateItem(result, empty);
                setGraphic(null);
                setText(null);
                if (result != null) {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/UI/ReceiverSearchResultCell.fxml"));
                    try {
                        HBox hBox = loader.load();
                        Circle avatar = (Circle) hBox.getChildren().get(0);
                        String defaultAvatar = "";
                        if (result.getAvatar() != null) {
                            InputStream is = result.getAvatar().getBinaryStream();
                            BufferedImage imageBuffered = ImageIO.read(is);
                            Image image = SwingFXUtils.toFXImage(imageBuffered, null );
                            avatar.setFill(new ImagePattern(image));
                        } else {
                            if (result.getGender() == 0) {
                                defaultAvatar = "default_male";
                            } else if (result.getGender() == 1) {
                                defaultAvatar = "default_female";
                            }
                            avatar.setStroke(Color.TRANSPARENT);
                            Image im = new Image("/images/" + defaultAvatar + ".png");
                            avatar.setFill(new ImagePattern(im));
                        }
                        hBox.getChildren().set(0, avatar);
                        Label name = (Label) hBox.getChildren().get(1);
                        name.setText(result.getFullName());
                        hBox.getChildren().set(1, name);

                        setGraphic(hBox);
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && (!cell.isEmpty())) {
                User result = cell.getItem();
                detailPaneController.pickResult(result);
            }
        });
        return cell;
    }
}
