package ftpstorage.client;

import java.io.File;
import java.io.IOException;

public interface IClient {

    public void user(String username) throws IOException;

    public void pass(String password) throws IOException;

    public void connect(String host) throws IOException;

    public void connect(String host, int port) throws IOException;

    public void connect(String host, int port, String user, String pass) throws IOException;

    public void cd(String path) throws IOException;
    
    public void cdup() throws IOException;

    public void makedir(String dirName) throws IOException;

    public void rmdir(String dirName) throws IOException;

    public void rename(String oldname, String newName) throws IOException;

    
    //public long get(String pathname, File fileToSave) throws IOException;
    
    public long get(String pathname, long[] offsets, File fileToSave) throws IOException;

    public void put(String pathname) throws IOException;
    
    public void put(File file) throws IOException;
    
//    public void continueDownload(String file, int numberPieces, int[] offsetOfPieces) throws IOException;

    public void append(String pathname) throws IOException;

    public void delete(String pathname) throws IOException;

    public void disconnect() throws IOException;

    /**
     * Display files in current directory
     *
     * @return
     * @throws IOException
     */
    public String list() throws IOException;

    /**
     * Display current directory
     *
     * @return
     */
    public String pwd() throws IOException;

    /**
     * Rename file
     *
     * @param file
     * @return true file if success
     */
    public void pause(int index) throws IOException;

    public void resume(int index) throws IOException;

//    public void setProgressBars(JProgressBar[] progressBars);

    public void remove(int indexFile);
    
    public File[] checkUnfinished();
    
//    public void restorefinished(File[] unfinisheds);

    public void saveUnfinished();

    public void loadUnfinished();
}
