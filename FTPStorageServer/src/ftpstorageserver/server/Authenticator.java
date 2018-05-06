/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

/**
 *
 * @author HP Zbook 15
 */
public class Authenticator {

    public Authenticator() {

    }

    public boolean isValidUser(String user, String pass) throws Exception {
        String fileAuth = System.getProperty("ftp.user." + user);
        if (fileAuth != null && pass.equals(fileAuth)) {
            return true;
        }
        return false;
    }
}
