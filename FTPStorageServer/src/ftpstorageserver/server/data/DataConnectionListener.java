/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server.data;

import java.io.File;

/**
 *
 * @author HP Zbook 15
 */
public interface DataConnectionListener {
    public void notifyFileTransferComplete(File file);
}
