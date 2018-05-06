/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 * @author HP Zbook 15
 */
public class MainServer {

    private void loadProperties() throws IOException {
        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("ftp.properties");
            prop.load(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        for (Enumeration e = prop.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            System.setProperty(key, prop.getProperty(key));
        }
    }

    public static void main(String[] args) throws IOException {
        MainServer ms = new MainServer();
        System.out.println("Load all configuration properties...");
        ms.loadProperties();

        Server s = new Server();
        s.startListening(21, 5);
    }
}
