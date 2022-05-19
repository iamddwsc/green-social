package controller;

import com.jfoenix.controls.JFXListView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import model.ReceiverSearchResult;
import model.ReceiverSearchResultRenderer;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiverSearchResultListViewController implements Initializable {
    @FXML
    public JFXListView searchResult;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void loadData() {
        searchResult.setItems(NewConversationDetailPaneController.observableList);
        searchResult.setCellFactory(new ReceiverSearchResultRenderer());
    }
}
