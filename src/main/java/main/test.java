package main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class test {
    public static void main(String args[]) throws URISyntaxException, IOException {

//        String file = "file_icon";
//        Path path = Paths.get("src/main/resources/images/", file);
//        File f = new File(path.get);
//        System.out.println();

//        ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
//        for (String s : names) {
//            System.out.println(s);

//        }
//        File starting = new File(System.getProperty("user.dir"));
//        File fileToBeRead = new File(starting,"my_file.txt");
//        System.out.println(resource.getAbsolutePath());

        test t = new test();
        t.getFile();
    }
    public void getFile() {
        URL url = getClass().getResource("/images/file_icon/docx.png");
        File f = new File(url.getFile());
        File ff = f.getParentFile();
        System.out.println(ff.getAbsolutePath());
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
        String imageType = "";
        for (String s : names) {
            System.out.println(s);
        }

    }
}
