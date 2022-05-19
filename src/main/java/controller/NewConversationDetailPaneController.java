package controller;

import com.jfoenix.controls.JFXListView;
import com.mysql.cj.jdbc.Blob;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import main.Listener;
import model.User;
import model.ReceiverSearchResultRenderer;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class NewConversationDetailPaneController implements Initializable {
    @FXML
    public JFXListView chatPane;
    @FXML
    public TextArea messageBox;
    @FXML
    public Button btnOption;
    @FXML
    public Circle circleOption;
    @FXML
    public Button btnSend;
    @FXML
    public Button btnEmoji;
    @FXML
    public Button btnSticker;
    public TextField receiverBox;
    public AnchorPane paneResult;

    private static User userCreateNewConvo = LoginUIController.user_logged_toSend;
    private ArrayList<User> receiversList;
    public static ObservableList<User> observableList;
    private ArrayList<User> currentReceivers = new ArrayList<>();
    private AnchorPane defaultResultPane;
    SimpleIntegerProperty count;
    Double originChatPane = 730.00;
    DoubleProperty dynamicChatPane = new SimpleDoubleProperty(originChatPane);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentReceivers.add(userCreateNewConvo);
        // load image for add file/image button
        Image im = new Image("/images/add_512px.png");
        circleOption.setFill(new ImagePattern(im));
        // set text area auto resize it self based on text
        count = new SimpleIntegerProperty(40);
        int rowHeight = 10;
        messageBox.prefHeightProperty().bindBidirectional(count);
        messageBox.minHeightProperty().bindBidirectional(count);
        messageBox.scrollTopProperty().addListener((ov, oldVal, newVal) -> {
            if(newVal.intValue() > rowHeight){
                count.setValue(count.get() + newVal.intValue());
                double abc = chatPane.getHeight() - newVal.doubleValue();
                DoubleProperty z = new SimpleDoubleProperty(abc);
                chatPane.prefHeightProperty().bindBidirectional(z);
                if (count.get() > 180) {
                    count.setValue(180);
                    chatPane.prefHeightProperty().bindBidirectional(new SimpleDoubleProperty(originChatPane-count.get()));
                    dynamicChatPane = new SimpleDoubleProperty(originChatPane-count.get());
                }
            }
        });
        messageBox.addEventHandler(ActionEvent.ACTION, event -> {
            if (messageBox.getText().equals("") || messageBox.getText() == null) {
                count.setValue(40);
                chatPane.prefHeightProperty().bindBidirectional(new SimpleDoubleProperty(originChatPane));
            }
        });
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });
    }

    private String getLastReceiverToQuery(String text) {
        ArrayList<String> arrays = new ArrayList();
        String sReturn = null;
        String[] words = text.split(";");
        for (String s:words) {
            arrays.add(s);
        }
        String temp = arrays.get(arrays.size()-1);
        if (temp.startsWith(" ")) {
            int index = 0;
            for (int i = 0; i<temp.length(); i++) {
                if (temp.charAt(i) != ' ') {
                    index = i;
                    break;
                }
            }
            sReturn = temp.substring(index);
        } else {
            sReturn = arrays.get(arrays.size()-1).replaceAll("\\s","");
        }
        return sReturn;
    }

    public void onEnter(ActionEvent event) {
        //to do
        receiversList = new ArrayList<>();
        String text = receiverBox.getText();
        int type = filter(text);
        String textQuery = getLastReceiverToQuery(text);
        String query1 = "SELECT id, email, phone, full_name, avatar, gender FROM user WHERE email = ?";
        String query3 = "SELECT id, email, phone, full_name, avatar, gender FROM user WHERE phone = ?";
        String query2 = "SELECT id, email, phone, full_name, avatar, gender FROM user WHERE full_name LIKE ?";
        PreparedStatement ps = null;
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javagreensocial","root","");
            if (type == 1) {
                ps = connection.prepareStatement(query1);
                ps.setString(1, textQuery);
            }
            if (type == 2) {
                ps = connection.prepareStatement(query2);
                ps.setString(1,"%" + textQuery + "%");
            }
            if (type == 3) {
                ps = connection.prepareStatement(query3);
                ps.setInt(1, Integer.parseInt(textQuery));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User rsr = new User();
                rsr.setId(rs.getInt("id"));
                rsr.setAvatar((Blob) rs.getBlob("avatar"));
                rsr.setEmail(rs.getString("email"));
                rsr.setPhone(rs.getString("phone"));
                rsr.setFullName(rs.getString("full_name"));
                rsr.setGender(rs.getInt("gender"));
                if (checkResult(rsr)) {
                    receiversList.add(rsr);
                }
            }
            defaultResultPane = paneResult;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/UI/ReceiverSearchResultListView.fxml"));
            AnchorPane pane = loader.load();
            observableList = FXCollections.observableList(receiversList);
            //controller.loadData();
            //paneResult.getChildren().setAll(pane.getChildren());
            //System.out.println(paneResult.getChildren());

            ScrollPane sp = (ScrollPane) pane.getChildren().get(0);
            JFXListView listView = (JFXListView) sp.getContent();
            listView.setItems(observableList);
            listView.setCellFactory(new ReceiverSearchResultRenderer());
            sp.setContent(listView);
            paneResult.getChildren().setAll(sp);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    private boolean checkResult(User r) {
        for (User rsr : currentReceivers) {
            // neu r dua vao trung ten voi bat ky thanh phan nao trong currentReceivers thi return false, con k return true
            if (rsr.getFullName().equals(r.getFullName())) return false;
        }
        return true;
    }
    private String getExceptLastForPickResult(String text) {
        String s = text;
        int index = s.lastIndexOf(";");
        s = s.substring(0, index+1);
        return s;
    }
    public void pickResult(User result) {
        if (receiverBox.getText() != null) {
            if (!receiverBox.getText().contains(";")) {
                receiverBox.setText(receiverBox.getText().replaceAll(getLastReceiverToQuery(receiverBox.getText()),""));
            } else {
                receiverBox.setText(getExceptLastForPickResult(receiverBox.getText()));
            }
            //receiverBox.setText(receiverBox.getText().replaceAll(getLastReceiverToQuery(receiverBox.getText()),""));
            receiverBox.setText(receiverBox.getText() + "'" + result.getFullName() + "'" + ";");
            receiverBox.positionCaret(receiverBox.getText().length());
        } else {
            receiverBox.setText("'" + result.getFullName() + "'" + "; ");
            receiverBox.positionCaret(receiverBox.getText().length());
        }
        currentReceivers.add(result);
        paneResult.getChildren().removeAll(paneResult.getChildren());
    }
    private ArrayList<String> getAllExceptLastForDelete(String text) {
        ArrayList<String> arrays = new ArrayList<>();
        ArrayList<String> sReturn = new ArrayList<>();
        String[] words = text.split(";");
        for (String s:words) {
            arrays.add(s);
        }
        for (int i = 0; i < arrays.size()-1; i++) {
            String s = arrays.get(i) + ";";
            sReturn.add(s);
        }
        return sReturn;
    }
    public void isDelete(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
            String s = getLastReceiverToQuery(receiverBox.getText());
            if (s.endsWith("'")) {
                ArrayList<String> temp = getAllExceptLastForDelete(receiverBox.getText());
                receiverBox.clear();
                for (int i = 0; i<temp.size(); i++) {
                    receiverBox.setText(receiverBox.getText().concat(temp.get(i)));
                }
                receiverBox.setText(receiverBox.getText() + ";");
                currentReceivers.remove(currentReceivers.size()-1);
                receiverBox.positionCaret(receiverBox.getText().length());
            }
        }
    }
    private int filter(String text) {
        if (!checkUserType(text)) { // neu khong phai hoan toan la number
            if (checkEmailFormat(text)) { // neu co @
                System.out.println("day la email");
                return 1;
            } else {
                System.out.println("day la ten nguoi dung");
                return 2;
            }
        } else { // neu hoan toan la so
            System.out.println("day la number");
            return 3;
        }
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

    public void showOptions(MouseEvent mouseEvent) {
    }

    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (!messageBox.getText().isEmpty()) {
            Listener.sendNewConversationByText(msg,currentReceivers);
            messageBox.clear();
            count.setValue(40);
            chatPane.prefHeightProperty().bindBidirectional(new SimpleDoubleProperty(originChatPane));
        }
    }

    public void showEmoji(MouseEvent mouseEvent) {
    }

    public void showSticker(MouseEvent mouseEvent) {
    }

    private ArrayList<String> getCurrentReceiverInTextField(String text) {
        ArrayList<String> arrays = new ArrayList();
        String[] words = text.split(";");
        for (String s:words) {
            arrays.add(s);
        }
        return arrays;
    }
    private void checkReceiver() {
        String rcv = receiverBox.getText();
    }

}
