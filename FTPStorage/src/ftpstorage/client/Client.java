package ftpstorage.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import ftpstorage.client.data.DataConnection;
import ftpstorage.client.data.DataConnectionListener;
import ftpstorage.views.FileStatus;
import ftpstorage.views.MainFrameClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * @author HP Zbook 15
 *
 */
public class Client implements IClient, DataConnectionListener {

    private Socket socket;
    private DataConnection data;
    private DataInputStream input;
    private DataOutputStream output;

    private String username;

    private MainFrameClient frame;

    public Client(MainFrameClient frame) {
        this.frame = frame;
    }

    private void sendLine(String line) throws IOException {
        if (socket == null) {
            throw new IOException("FTP is not connected.");
        }
        try {
            output.writeUTF(line + "\r\n");
            output.flush();
            System.out.println("=> " + line);
        } catch (IOException e) {
            e.printStackTrace();
            socket = null;
            throw e;
        }
    }

    private String readLine() throws IOException {
        String line = input.readUTF();
        System.out.println("<= " + line);
        return line;
    }

    @Override
    public void connect(String host) throws IOException {
        connect(host, 21);
    }

    @Override
    public void connect(String host, int port) throws IOException {
        connect(host, port, "anonymous", "anonymous");
    }

    @Override
    public void connect(String host, int port, String username, String password) throws IOException {
        if (socket != null) {
            throw new IOException("FTP is already connected. Disconnect first.");
        }
        socket = new Socket(host, port);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

        String response = readLine();
        if (!response.startsWith("220 ")) {
            throw new IOException("FTP received an unknow reponse when connecting to the FTP server: " + response);
        }

        user(username);
        pass(password);

        connectData();

    }

    private void connectData() throws UnknownHostException, IOException {
        this.data = new DataConnection();
        this.data.setListener(this);
        this.data.setUsername(username);
        System.out.println("Data flow connect successed.");
    }

    @Override
    public String list() throws IOException {
        sendLine("LIST ");
        String response = readLine();
        if (response.startsWith("150 ")) {
            String dataResponse = data.list();
            return dataResponse;
        }
        return "error";

    }

    @Override
    public void disconnect() throws IOException {
        saveUnfinished();
        try {
            sendLine("QUIT");
            data.stop();
        } finally {
            socket = null;
        }
    }

    @Override
    public void user(String username) throws IOException {
        sendLine("USER " + username);
        String response = readLine();
        if (!response.startsWith("331 ")) {
            throw new IOException("FTP received an unknown response after sending the user: " + response);
        }
        this.username = username;
    }

    @Override
    public void pass(String password) throws IOException {
        sendLine("PASS " + password);

        String response = readLine();
        if (!response.startsWith("230 ")) {
            throw new IOException("FTP was unable to log in with the supplied password: " + response);
        }
    }

    @Override
    public void cd(String path) throws IOException {
        sendLine("CWD " + path);
        String response = readLine();
        if (!response.startsWith("250 ")) {
            throw new IOException("FTP unknow directory: " + path);
        }
    }

    @Override
    public void makedir(String dirName) throws IOException {
        sendLine("MKD " + dirName);
        String response = readLine();
        if (!response.startsWith("")) {
            throw new IOException("Make directory failed : " + dirName);
        }
    }

    @Override
    public void rmdir(String dirName) throws IOException {
        sendLine("RMD " + dirName);
        String response = readLine();
        if (!response.startsWith("")) {
            throw new IOException("Remove directory failed : " + dirName);
        }
    }

    @Override
    public void rename(String oldname, String newName) throws IOException {
        sendLine("RNFR " + oldname);
        String response = readLine();
        if (!response.startsWith("")) {
            throw new IOException("Rename from : Don't exists " + oldname);
        }
        sendLine("RNTO " + newName);
        response = readLine();
        if (!response.startsWith("")) {
            throw new IOException("Rename to: rename failed for " + newName);
        }
    }

    @Override
    public long get(String pathname, long[] offsets, File fileToSave) throws IOException {
        String remotePath = pwd();
        sendLine("SIZE " + pathname);
        String response = readLine();
        if (!response.startsWith("213 ")) {
            throw new IOException(pathname + ": No such file or directory");
        }
        long size = Long.parseLong(response.split(" ")[1].trim());

        StringBuilder sb = new StringBuilder("RETR ");
        sb.append(pathname);
        for (int i = 0; i < offsets.length; i++) {
            sb.append(" ");
            sb.append(offsets[i]);
        }

        sendLine(sb.toString());
        response = readLine();
        if (!response.startsWith("150 ")) {
            throw new IOException("Get file " + pathname + " is aborted.");
        }

        data.newFileSession(fileToSave, size, offsets, false, remotePath);
        return size;
    }

//    @Deprecated
//    public long get(String pathname, File fileToSave) throws IOException {
//        return 0;
//    }
    @Override
    public void put(String pathname) throws IOException {
        String remotePath = pwd();

        File f = new File(pathname);
        if (!f.exists()) {
            throw new IOException("File \"" + pathname + "\" not exists to put to server.");
        }

        allocate(f.length());

        String[] tmp = pathname.split("/");
        pathname = tmp[tmp.length - 1];

        sendLine("STOR " + pathname + " 1 4");
        String response = readLine();
        if (!response.startsWith("150 ")) {
            throw new IOException("Failed to put " + pathname + " to server.");
        }
        String[] parse = response.split(" ");
        long[] offsets = new long[parse.length - 1];
        for (int i = 1; i < parse.length; i++) {
            offsets[i - 1] = Long.parseLong(parse[i].trim());
        }

        data.newFileSession(f, f.length(), offsets, true, remotePath);
    }

    @Override
    public void put(File file) throws IOException {
        String remotePath = pwd();
        if (!file.exists()) {
            throw new IOException("File \"" + file.getName() + "\" not exists to put to server.");
        }

        allocate(file.length());

        sendLine("STOR " + file.getName() + " 1 4");
        
        String response = readLine();
        if(response.startsWith("500 ")){
            int choose = JOptionPane.showConfirmDialog(null, "File is exists. Do you want to continue?");
            if(choose == JOptionPane.YES_OPTION){
                choose = JOptionPane.showConfirmDialog(null, "Do you want Override or Keep all? (Yes to override, No to duplicate)");
                if(choose == JOptionPane.YES_OPTION){
                    sendLine("Override");
                    response = readLine();
                } else if(choose == JOptionPane.NO_OPTION){
                    sendLine("Duplicate");
                    response = readLine();
                }
                
            }
        }

        if (!response.startsWith("150 ")) {
            throw new IOException("Failed to put " + file.getName() + " to server.");
        }
        String[] parse = response.split(" ");
        long[] offsets = new long[parse.length - 1];
        for (int i = 1; i < parse.length; i++) {
            offsets[i - 1] = Long.parseLong(parse[i].trim());
        }

        data.newFileSession(file, file.length(), offsets, true, remotePath);
    }

    @Override
    public void append(String pathname) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(String pathname) throws IOException {
        sendLine("DELE " + pathname);
        String response = readLine();
        if (!response.startsWith("250 ")) {
            throw new IOException("Delete: failed to delete " + pathname);
        }
    }

    @Override
    public String pwd() throws IOException {
        sendLine("PWD");
        String response = readLine();
        if (!response.startsWith("257 ")) {
            throw new IOException("PWD: command is aborted.");
        }
        String currentDir = response.split("\"")[1].trim();
        return currentDir;
    }

    private void allocate(long size) throws IOException {
        sendLine("ALLO " + size);
        String response = readLine();
        if (!response.startsWith("200 ")) {
            throw new IOException("Allocate: failed with " + size);
        }
    }

    @Override
    public void pause(int index) throws IOException {
        sendLine("PAUSE " + index);
        data.pause(index);
    }

    @Override
    public void resume(int index) throws IOException {
        boolean resume = data.resume(index);
        if (resume) {
            sendLine("RESUME " + index);
        }
    }

    @Override
    public void cdup() throws IOException {
        cd((".."));
    }

    @Override
    public void notifyComplete(int index) {
        this.frame.notifyComplete(index);
    }

    @Override
    public void remove(int indexFile) {
        data.remove(indexFile);
    }

    @Override
    public JProgressBar[] getProgressBars() {
        return this.frame.getProgressBars();
    }

    public File[] checkUnfinished() {
        File f = new File("unfinished/");
        if (!f.exists()) {
            f.mkdir();
            return null;
        }
        File[] unfinisheds = f.listFiles();
        return unfinisheds;
    }

//    public void restorefinished(File[] unfinisheds) {
//        data.restoreUnfinish(unfinisheds);
//    }
    public static void main(String[] args) throws IOException {
        Client c = new Client(null);
        c.connect("localhost", 21, "tienthien", "tienthien");
        // long[] offsets = new long[4];
        //c.get("a.txt", offsets, new File("local/a.txt"));
        //c.makedir("put");
        c.cd("put");
        c.put("local/a.txt");
    }

    @Override
    public void saveUnfinished() {
        data.saveUnfinished(username);
    }

    @Override
    public void loadUnfinished() {
        data.loadUnfinished();
    }

    @Override
    public void addPanelFileStatus(String filename, long fileLength, boolean isDownload) {
        FileStatus fs = new FileStatus(filename, fileLength, isDownload);
//        fs.setClient(this);
        this.frame.getPnlFilesStatus().addFileStatus(fs);
    }

    @Override
    public void resumeUnfinished(boolean send, String commandResume) {
        try {
            sendLine(commandResume);
            String response = readLine();
            if (!response.startsWith("150 ")) {
                throw new IOException("Failed to resume with command:" + commandResume);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
