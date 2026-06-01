package com.caspian.pichak.utility;

import com.caspian.pichak.PichakServiceApplication;
import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadOverrideProperties {
    public void loadOverrideProperties() {
//        System.out.println(getParentDirectoryFromJar());
        ApplicationHome home = new ApplicationHome(PichakServiceApplication.class);
        System.out.println(home.getDir());
        System.out.println(home.getSource());
//        String sourcePath = (new File(System.getProperty("java.class.path"))).getParentFile().getAbsolutePath();
        String sourcePath = home.getDir().toString();

        File file = new File(sourcePath + "/override.properties");
        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(inputStream);
                for (Object o : properties.keySet()) {
                    System.setProperty(String.valueOf(o), properties.getProperty(String.valueOf(o)));
                    System.out.println("override property '" + o + "' with value : " + properties.getProperty(String.valueOf(o)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
