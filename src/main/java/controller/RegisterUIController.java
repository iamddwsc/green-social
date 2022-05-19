package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXRadioButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterUIController implements Initializable {
    @FXML
    public JFXButton btnRegister;
    public PasswordField pfPassword;
    public TextField tfLastName;
    public TextField tfFirstName;
    public JFXDatePicker dpDateOfBirth;
    public TextField tfUser;
    public ToggleGroup tgGender;
    public JFXRadioButton rbMale;
    public JFXRadioButton rbFemale;
    public ImageView ivAvatarPreview;
    public JFXButton btnAvatarPreview;

    private FileChooser fileChooser;




    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void register(MouseEvent mouseEvent) {
        boolean allow_register = false;
        String null_num = "not set";
        String null_email = "not set";
        String firstName = tfFirstName.getText();
        String lastName = tfLastName.getText();
        String user = tfUser.getText();
        String password = pfPassword.getText();
        LocalDate dateOfBirth = dpDateOfBirth.getValue();
        String gender_selected = "";
        int gender = -1;
        if (rbFemale.isSelected() || rbMale.isSelected()) {
            gender_selected = ((RadioButton) tgGender.getSelectedToggle()).getText();
            if (gender_selected.equals("Female")) {
                gender = 1; // female
            }
            if (gender_selected.equals("Male")) {
                gender = 0; // male
            }
        }
        if (user.equals("") || firstName.equals("") || lastName.equals("") || password.equals("") || dateOfBirth == null || gender == -1) {
            myAlert(Alert.AlertType.ERROR, "Input not valid"
                    , "Require fill all input to continue."
                    , "/images/cancel_32px.png"
                    , "/images/cancel_512px.png");
        } else {
            if (!checkUserType(user)) {
                //System.out.println("string");
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
                }
                else {
                    allow_register = true;
                }
            } else if (checkUserType(user)){
                if (!checkNum(user)) {
                    myAlert(Alert.AlertType.ERROR, "Input not valid"
                            , "The length of Vietnamese phone number must be 10 digits and start with number 0. Please try again."
                            , "/images/cancel_32px.png"
                            , "/images/cancel_512px.png");
                } else {
                    allow_register = true;
                }
                //System.out.println("num");
            }
            if (allow_register) { //do register
                if (!checkExist(user)) {
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
                        String query = "INSERT INTO user(email, phone, first_name, last_name, password, avatar, birthday, gender)" +
                                " values(?,?,?,?,?,?,?,?)";
                        PreparedStatement ps = connection.prepareStatement(query);
                        if (!checkUserType(user)) {
                            ps.setString(1, user);
                            ps.setString(2, null_num);
                        } else {
                            ps.setString(1, null_email);
                            ps.setString(2, user);
                        }
                        ps.setString(3, firstName);
                        ps.setString(4, lastName);
                        ps.setString(5, password);
                        ps.setBlob(6, (InputStream) null);
                        ps.setDate(7, Date.valueOf(dateOfBirth));
                        ps.setInt(8, gender);
                        int rs = ps.executeUpdate();
                        System.out.println(rs);
                        if (rs == 1) {
                            myAlert(Alert.AlertType.INFORMATION, "Welcome " + lastName + " " + firstName + ","
                                    , "Your account is successfully created."
                                    , "/images/done_32px.png"
                                    , "/images/done_512px.png");
                        }
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    myAlert(Alert.AlertType.INFORMATION, "User already exist."
                            , "Please choose other user"
                            , "/images/info_32px.png"
                            , "/images/info_512px.png");
                }
            }
        }
    }
    private boolean checkUserType(String user) {
        for (char c : user.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
    private boolean checkEmailFormat(String user) {
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return user.matches(regex);
    }
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
    private boolean checkNum(String user) {
        if (!user.startsWith("0")) return false;
        if (user.length() == 10) return true;
        return false;
    }
    private void myAlert(Alert.AlertType alertType, String header, String content, String graphic, String icon) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setGraphic(new ImageView(graphic));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(icon).toString()));
        stage.showAndWait();
    }
    private boolean checkExist(String user) {
        boolean check = false;
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
            PreparedStatement ps;
            if (!checkUserType(user)) {
                String query = "SELECT id FROM user WHERE email=?";
                ps = connection.prepareStatement(query);
                ps.setString(1, user);
            } else {
                String query = "SELECT id FROM user WHERE phone=?";
                ps = connection.prepareStatement(query);
                ps.setString(1, user);
            }
            ResultSet rs = ps.executeQuery();
            check = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }
}
