/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

import java.io.IOException;

/**
 *
 * @author HP Zbook 15
 */
public interface ISession {

    public void processUser(String param) throws IOException;

    public void processPassword(String param) throws Exception;

    public void processChangeWorkingDirectory(String param) throws IOException;

    public void processChangeDirectoryToParent() throws IOException;

    public void processList() throws IOException;

    public void processNameList() throws IOException;

    public void processAllocate(String param) throws IOException;

    public void processFileSize(String param) throws IOException;

    public void processRetrieve(String param) throws IOException;

    public void processStore(String param) throws IOException;

    public void processAppend(String param) throws IOException;

    public void processRenameFrom(String param) throws IOException;

    public void processRenameTo(String param) throws IOException;

    public void processDelete(String cmd, String param) throws IOException;

    public void processMakeDirectory(String param) throws IOException;

    public void processRemoveDirectory(String cmd, String param) throws IOException;

    public void processPrintWorkingDirectory() throws IOException;

    public void processQuit() throws IOException;

    public void processResume(String param) throws IOException;

    public void processPause(String param) throws IOException;
}
