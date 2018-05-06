/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

import ftpstorageserver.server.conflicts.RequestStore;
import java.io.File;

/**
 *
 * @author HP Zbook 15
 */
public interface ConflictListener {

    public void addRequest(Session s, String request);

    public RequestStore popRequest();

    public void addFileProcessing(File file);

    public boolean removeFileProccessing(File file);

    public boolean isFileProcessing(File file);
}
