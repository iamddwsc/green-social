package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import model.User;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CellController implements Initializable {
    @FXML
    public Label lbFriendName;
    @FXML
    public Label lbFriendChat;
    @FXML
    public Label lbTime;

    public static ArrayList<User> allUsers_friend = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //getAllConversation();
    }
    public void getAllConversation() {
        try {
            String query = "SELECT o.list_user, b.message " +
                    "FROM list_convo AS a JOIN messages AS b " +
                    "ON list_convo.id_convo = messages.id_convo " +
                    "WHERE list_user LIKE ?";
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial", "root", "");
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, String.valueOf(LoginUIController.userLogged.getId()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
