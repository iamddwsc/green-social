package controller;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeUIController implements Initializable {
    public JFXButton btnSignIn;
    public JFXButton btnSignUp;
    @FXML
    private AnchorPane layout_swapping;
    @FXML
    private JFXButton btnLogin;
    @FXML
    private JFXButton btnRegister;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void gotoLogin(MouseEvent mouseEvent) throws IOException {
        AnchorPane goto_login = FXMLLoader.load(getClass().getResource("/UI/LoginUI.fxml"));
        AfterSplashUIController.swapper.setCurrent_page(1);
        AfterSplashUIController.swapper.setFlag_page_2(true);
        AfterSplashUIController.swapper.setFlag_page_3(false);
        AfterSplashUIController.swapper.getLayout_swapping().getChildren().removeAll();
        AfterSplashUIController.swapper.getLayout_swapping().getChildren().setAll(goto_login);
        AfterSplashUIController.swapper.getBtnBack().setFill(Color.valueOf("000000"));
        AfterSplashUIController.swapper.getBtnGo().setFill(Color.valueOf("7e7e7e"));
    }

    public void gotoRegister(MouseEvent mouseEvent) throws IOException {
        AnchorPane goto_register = FXMLLoader.load(getClass().getResource("/UI/RegisterUI.fxml"));
        AfterSplashUIController.swapper.setCurrent_page(1);
        AfterSplashUIController.swapper.setFlag_page_2(false);
        AfterSplashUIController.swapper.getLayout_swapping().getChildren().removeAll();
        AfterSplashUIController.swapper.getLayout_swapping().getChildren().setAll(goto_register);
        AfterSplashUIController.swapper.getBtnBack().setFill(Color.valueOf("000000"));
        AfterSplashUIController.swapper.getBtnGo().setFill(Color.valueOf("7e7e7e"));
    }
}
