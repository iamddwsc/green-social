package controller;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AfterSplashUIController implements Initializable {
    public HBox hBox_move;
    public Button btnMin;
    public MaterialDesignIconView btnMin_icon;
    public Button btnMax;
    public MaterialDesignIconView btnMax_icon;
    public Button btnClose;
    public MaterialDesignIconView btnClose_icon;
    @FXML
    private AnchorPane main_pane;
    @FXML
    private AnchorPane layout_swapping;
    @FXML
    private MaterialDesignIconView btnGo;
    @FXML
    private MaterialDesignIconView btnBack;

    public static SwapperModel swapper = null;

    private final String[] LIST_PANE = {"/UI/WelcomeUI.fxml","/UI/LoginUI.fxml","/UI/RegisterUI.fxml"};

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        try {
            swapper = new SwapperModel();
            swapper.setLayout_swapping(layout_swapping);
            swapper.setBtnBack(btnBack);
            swapper.setBtnGo(btnGo);
            AnchorPane goto_welcome = FXMLLoader.load(getClass().getResource("/UI/WelcomeUI.fxml"));
            layout_swapping.getChildren().removeAll();
            layout_swapping.getChildren().setAll(goto_welcome);
            btnMax.setDisable(true);
            btnMax_icon.setFill(Color.valueOf("7e7e7e"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnMin_icon.styleProperty().bind(Bindings.when(btnMin.hoverProperty())
                .then("-fx-fill: #e5e5e5").otherwise("#000000"));
        btnMax_icon.styleProperty().bind(Bindings.when(btnMax.hoverProperty())
                .then("-fx-fill: #e5e5e5").otherwise("#000000"));
        btnClose_icon.styleProperty().bind(Bindings.when(btnClose.hoverProperty())
                .then("-fx-fill: #e5e5e5").otherwise("#000000"));
    }

    @FXML
    private void close(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.close();
    }

    public void max(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.setFullScreen(true);
    }

    public void min(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.setIconified(true);
    }

    public void go(MouseEvent mouseEvent) throws IOException {
        if (btnGo.getFill().equals(Color.valueOf("000000")) && swapper.isFlag_page_2()) {
            AnchorPane goto_login = FXMLLoader.load(getClass().getResource(LIST_PANE[swapper.getCurrent_page()+1]));
            swapper.inc_current_pane(1);
            layout_swapping.getChildren().removeAll();
            layout_swapping.getChildren().setAll(goto_login);

            if (swapper.getCurrent_page() < 1) {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("7e7e7e"));
            } else if (swapper.getCurrent_page() > 1) {
                btnGo.setFill(Color.valueOf("7e7e7e"));
                btnBack.setFill(Color.valueOf("000000"));
            } else if (swapper.getCurrent_page() == 1 && swapper.isFlag_page_3()){
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("000000"));
            } else {
                btnGo.setFill(Color.valueOf("7e7e7e"));
                btnBack.setFill(Color.valueOf("000000"));
            }
        } else if (btnGo.getFill().equals(Color.valueOf("000000")) && !swapper.isFlag_page_2()){
            AnchorPane goto_login = FXMLLoader.load(getClass().getResource(LIST_PANE[2]));
            swapper.setCurrent_page(2);
            layout_swapping.getChildren().removeAll();
            layout_swapping.getChildren().setAll(goto_login);

            if (swapper.getCurrent_page() < 1) {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("7e7e7e"));
            } else if (swapper.getCurrent_page() > 1) {
                btnGo.setFill(Color.valueOf("7e7e7e"));
                btnBack.setFill(Color.valueOf("000000"));
            } else {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("000000"));
            }
        }

    }

    public void back(MouseEvent mouseEvent) throws IOException {
        if (btnBack.getFill().equals(Color.valueOf("000000")) && swapper.isFlag_page_2()) {
            AnchorPane goto_login = FXMLLoader.load(getClass().getResource(LIST_PANE[swapper.getCurrent_page()-1]));
            swapper.dec_current_pane(1);
            layout_swapping.getChildren().removeAll();
            layout_swapping.getChildren().setAll(goto_login);

            if (swapper.getCurrent_page() < 1) {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("7e7e7e"));
            } else if (swapper.getCurrent_page() > 1) {
                btnGo.setFill(Color.valueOf("7e7e7e"));
                btnBack.setFill(Color.valueOf("000000"));
            } else {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("000000"));
            }
        } else if (btnBack.getFill().equals(Color.valueOf("000000")) && !swapper.isFlag_page_2()) {
            AnchorPane goto_login = FXMLLoader.load(getClass().getResource(LIST_PANE[0]));
            swapper.setCurrent_page(0);
            layout_swapping.getChildren().removeAll();
            layout_swapping.getChildren().setAll(goto_login);

            if (swapper.getCurrent_page() < 1) {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("7e7e7e"));
            } else if (swapper.getCurrent_page() > 1) {
                btnGo.setFill(Color.valueOf("7e7e7e"));
                btnBack.setFill(Color.valueOf("000000"));
            } else {
                btnGo.setFill(Color.valueOf("000000"));
                btnBack.setFill(Color.valueOf("000000"));
            }
        }
    }
}
