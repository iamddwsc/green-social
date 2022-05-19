package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import main.Listener;
import model.Conversation;
import model.ConversationListRenderer;

import java.net.URL;
import java.util.ResourceBundle;

public class NewConversationController implements Initializable {
    @FXML
    public Button btnCancel;
    ClientUIController clientUIController = Listener.clientUIController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void cancel(MouseEvent mouseEvent) {
        clientUIController.returnPreviousParentChatPane();
        clientUIController.convoList.getItems().remove(0);
        clientUIController.convoList.getSelectionModel().select(0);
        clientUIController.displayFirstAfterCancel(0);
        ConversationListRenderer.convo_selected = 0;
    }
}
