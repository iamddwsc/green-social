package controller;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import main.Listener;
import model.message.Message;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class ImageExpandController implements Initializable {
    @FXML
    public Button btnClose;
    @FXML
    public MaterialDesignIconView btnClose_icon;
    @FXML
    public ImageView ivMainDisplay;
    public Button btnDownload;
    private final ClientUIController clientUIController = Listener.clientUIController;
    private Message msgExpandedImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void close(MouseEvent mouseEvent) {
        clientUIController.returnPreviousScene();
    }

    public void download(MouseEvent mouseEvent) throws IOException {
        System.out.println("OK DOWNLOAD Image");
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String fileNameTimeStamp = timeStamp + ".png";
        String home = System.getProperty("user.home");
        String pathname = home+"/Downloads/" + fileNameTimeStamp;
        FileUtils.writeByteArrayToFile(new File(pathname), msgExpandedImage.getData());
    }
    public void dataImageExpanded(Message msg) {
        this.msgExpandedImage = msg;
    }
    public void updateImageView() {
        ByteArrayInputStream bais = new ByteArrayInputStream(msgExpandedImage.getData());
        BufferedImage imageBuffered = null;
        try {
            imageBuffered = ImageIO.read(bais);
            Image image = SwingFXUtils.toFXImage(imageBuffered, null);
            ivMainDisplay.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
