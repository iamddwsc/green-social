package controller;

import com.jfoenix.controls.JFXButton;
import com.mysql.cj.jdbc.Blob;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.InfoUserLogged;
import model.User;
import sun.rmi.runtime.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UploadAvatarUIController implements Initializable {
    @FXML
    public ImageView ivAvatar;
    @FXML
    public JFXButton btnUpload;
    @FXML
    public JFXButton btnSelect;

    private final InfoUserLogged myUser = LoginUIController.userLogged;

    File file;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ivAvatar.setImage(ClientUIController.loggedUserImage);
    }

    @FXML
    private void close(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        s.close();
    }

    public void chooseAvatarPreview(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        configuringFileChooser(fileChooser);
        Stage stage = new Stage();
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            ivAvatar.setImage(new Image(file.toURI().toString()));
            ivAvatar.setSmooth(true);
            ivAvatar.setPreserveRatio(true);
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

    //if upload the big image need run query "SET GLOBAL max_allowed_packet = 1024*1024*14;" on sql server
    public void upload(MouseEvent mouseEvent) {
        Connection connection = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        try {
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(file);
            String query = "UPDATE user set avatar=? WHERE id=?";
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
            ps = connection.prepareStatement(query);
            ps.setBinaryStream(1, inputStream, file.length());
            ps.setInt(2, myUser.getId());
            int rs = ps.executeUpdate();
            if (rs == 1) {
                ps2 = connection.prepareStatement("SELECT * FROM user WHERE id=?");
                ps2.setInt(1, myUser.getId());
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    InfoUserLogged newInfoUserLogged = new InfoUserLogged();
                    int id = rs2.getInt(1);
                    int gender = rs2.getInt(9);
                    String email = rs2.getString(2);
                    String phone = rs2.getString(3);
                    String firstName = rs2.getString(4);
                    String lastName = rs2.getString(5);
                    String full_name = rs2.getString("full_name");
                    //password already exist when login
                    Blob avatar = (Blob) rs2.getBlob(7);
                    Date birthDay = rs2.getDate(8);
                    newInfoUserLogged = new InfoUserLogged(id, email, phone, firstName, lastName, myUser.getPassword(), avatar, birthDay, gender);
                    LoginUIController.user_logged_toSend = new User(id, full_name, avatar, gender);
                    LoginUIController.userLogged = newInfoUserLogged;
                    ClientUIController.myUser = newInfoUserLogged;
                    LoginUIController.clientUIController.loadUserLoggedIn();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException: - " + e);
        } catch (SQLException e) {
            System.out.println("SQLException: - " + e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
                ps.close();
            } catch (SQLException e) {
                System.out.println("SQLException Finally: - " + e);
            }
        }
    }
}
