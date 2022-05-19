package controller;

import com.jfoenix.controls.JFXButton;
import com.mysql.cj.jdbc.Blob;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.Listener;
import model.InfoUserLogged;
import model.User;

import javax.imageio.ImageIO;
import javax.naming.Context;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class LoginUIController implements Initializable {

    public JFXButton btnLogin;
    public PasswordField pfPassword;
    public TextField tfUser;
    public JFXButton btnForgotPassword;
    public JFXButton btnCreate;

    public static InfoUserLogged userLogged;
    public static User user_logged_toSend;
    public static ClientUIController clientUIController;

    private static LoginUIController instance;

    public LoginUIController() {
        instance = this;
    }

    public static LoginUIController getInstance() {
        return instance;
    }
    private double x, y;

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        tfUser.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                login();
                ke.consume();
            }
        });
        pfPassword.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                login();
                ke.consume();
            }
        });
    }

    public void login() {
        boolean allow_login = false;
        String query = "";
        String user = tfUser.getText();
        String password = pfPassword.getText();
        if (user == "" || password == "") {
            myAlert(Alert.AlertType.ERROR, "Input not valid"
                    , "Require fill all input to continue."
                    , "/images/cancel_32px.png"
                    , "/images/cancel_512px.png");
        } else {
            if (!checkUserType(user)) { //false = email, true = phone
                if (!user.contains("@")) {
                    myAlert(Alert.AlertType.ERROR, "Input not valid"
                            , "An email should have it's correct structure, including '@'."
                            , "/images/cancel_32px.png"
                            , "/images/cancel_512px.png");
                } else if (user.contains("@") && !checkEmailFormat(user) || user.contains("@") && !checkDot(user)) {
                    myAlert(Alert.AlertType.ERROR, "Input not valid"
                            , "An email should have it's correct structure, example: greensocial@example.com"
                            , "/images/cancel_32px.png"
                            , "/images/cancel_512px.png");
                } else {
                    query = "SELECT * FROM user WHERE email=? AND password=?";
                    allow_login = true;
                }
            } else if (checkUserType(user)) {
                if (!validNum(user)) {
                    myAlert(Alert.AlertType.ERROR, "Input not valid"
                            , "The length of Vietnamese phone number must be 10 digits and start with number 0. Please try again."
                            , "/images/cancel_32px.png"
                            , "/images/cancel_512px.png");
                } else {
                    query = "SELECT * FROM user WHERE phone=? AND password=?";
                    allow_login = true;
                }
            }
            if (allow_login) {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setString(1, user);
                    ps.setString(2, password);
                    ResultSet rs = ps.executeQuery();
                    PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM server_running WHERE host IS NOT NULL");
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs.next()) {
                        //get info of logged in user and create new logged in user
                        int id = rs.getInt(1);
                        int gender = rs.getInt(10);
                        String email = rs.getString(2);
                        String phone = rs.getString(3);
                        String firstName = rs.getString(4);
                        String lastName = rs.getString(5);
                        String fullName = rs.getString("full_name");
                        //password already exist when login
                        Blob avatar = (Blob) rs.getBlob(8);
                        Date birthDay = rs.getDate(9);
                        //InputStream is = avatar.getBinaryStream();
                        //BufferedImage imageBuffered = ImageIO.read(is);
                        //Image image = SwingFXUtils.toFXImage(imageBuffered, null );
                        userLogged = new InfoUserLogged(id, email, phone, firstName, lastName, password, avatar, birthDay, gender);
                        user_logged_toSend = new User(id, fullName, avatar, gender);
                        System.out.println("Login success");
                        //display Chat Stage
                        btnLogin.getScene().getWindow().hide();
                        Stage stage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/ClientUI.fxml"));
                        AnchorPane root = loader.load();
                        //change here
                        AnchorPane pane_to_drag2_client = (AnchorPane) root.getChildren().get(2);
                        //end change
                        toDragWindow(stage, pane_to_drag2_client);
                        AnchorPane pane_to_drag_client = (AnchorPane) root.getChildren().get(0);
                        toDragWindow(stage, pane_to_drag_client);
                        clientUIController = loader.getController();
                        if (rs2.next()) {
                            String host = rs2.getString("host");
                            int port = rs2.getInt("port");
                            Listener listener = new Listener(host, port, clientUIController);
                            Thread x = new Thread(listener);
                            x.start();
                        }
                        Scene scene = new Scene(root);
                        scene.setFill(Color.TRANSPARENT);
                        scene.getStylesheets().add(Context.class.getResource("/css/MainStyle.css").toExternalForm());
                        stage.setScene(scene);
                        stage.initStyle(StageStyle.TRANSPARENT);
                        stage.getIcons().add(new Image("/images/logo.png"));
                        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                        stage.show();
                    } else {
                        myAlert(Alert.AlertType.ERROR, "Incorrect Password"
                                , "The password you entered is incorrect. Please try again."
                                , "/images/cancel_32px.png"
                                , "/images/cancel_512px.png");
                    }
                } catch (SQLException | IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void toDragWindow(Stage stage, AnchorPane pane_to_drag_client) {
        pane_to_drag_client.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                x = event.getSceneX();
                y = event.getSceneY();
            }
        });
        pane_to_drag_client.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);
            }
        });
    }

    //check user is num or char
    private boolean checkUserType(String user) {
        for (char c : user.toCharArray()) {
            if (!Character.isDigit(c)) return false; //khong phai la so -> return fasle
        }
        return true;
    }

    //check email format
    private boolean checkEmailFormat(String user) {
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return user.matches(regex);
    }

    //kiem tra so dau cham(dot) trong email typed
    private boolean checkDot(String user) {
        int dupeDot = 0;
        int to = user.indexOf("@");
        String sub_user = user.substring(0, to);
        for (int i = 0; i < sub_user.length(); i++) {
            if (i < sub_user.length()-1 && sub_user.charAt(i) == sub_user.charAt(i+1)) {
                dupeDot++;
            }
            char c = sub_user.charAt(i);
            if (c == '.') {
                if (sub_user.indexOf(c) - sub_user.length() == -1) return false;
            }
        }
        if (dupeDot > 0) return false;
        else return true;
    }

    //kiem tra xem so co dung voi sdt Vietnam hay khong
    private boolean validNum(String user) {
        if (!user.startsWith("0")) return false;
        if (user.length() == 10 || user.contains("1")) return true;
        return false;
    }

    //multiple alert
    private void myAlert(Alert.AlertType alertType, String header, String content, String graphic, String icon) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setGraphic(new ImageView(graphic));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(icon).toString()));
        stage.showAndWait();
    }

    //button go to register
    public void gotoRegister(MouseEvent mouseEvent) throws IOException {
        AnchorPane goto_register = FXMLLoader.load(getClass().getResource("/UI/RegisterUI.fxml"));
        AfterSplashUIController.swapper.setCurrent_page(2);
        AfterSplashUIController.swapper.setFlag_page_3(true);
        AfterSplashUIController.swapper.getLayout_swapping().getChildren().removeAll();
        AfterSplashUIController.swapper.getLayout_swapping().getChildren().setAll(goto_register);
        AfterSplashUIController.swapper.getBtnBack().setFill(Color.valueOf("000000"));
        AfterSplashUIController.swapper.getBtnGo().setFill(Color.valueOf("7e7e7e"));
    }
}
