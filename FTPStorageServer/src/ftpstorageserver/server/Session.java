/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

import ftpstorageserver.server.conflicts.RequestStore;
import ftpstorageserver.server.data.DataConnection;
import ftpstorageserver.server.data.DataConnectionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author HP Zbook 15
 */
public class Session implements Runnable, ISession, DataConnectionListener {

    private JTextArea txtCommand;

    private Socket socket;
    private Thread thread;
    private boolean isAlive;

    private SessionListener listener;
    private ConflictListener conflict;

    private DataInputStream input;
    private DataOutputStream output;

    private DataConnection data;

    private String username;
    private boolean isAuth;
    private File userRoot;
    private File userCurrent;

    private SimpleDateFormat fmtDate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private SimpleDateFormat fmtStamp = new SimpleDateFormat("yyyyMMddHHmmss");

    private File targetFile;
    private long length;

    public Session(Socket s) throws IOException {
        this.socket = s;
        this.input = new DataInputStream(s.getInputStream());
        this.output = new DataOutputStream(s.getOutputStream());

    }

    public void setTxtCommand(JTextArea txtCommand) {
        this.txtCommand = txtCommand;
    }

    public void setListener(SessionListener l) {
        this.listener = l;
    }

    public void setConflictListener(ConflictListener l) {
        this.conflict = l;
    }

    public String getUsername() {
        return this.username;
    }

    public void start() {
        this.isAlive = true;
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public synchronized void stop() {
        data.saveUnfinished(username);
        try {
            if (data != null) {
                data.stop();
                data = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }

            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void println(String msg) throws IOException {
        System.out.println("<=" + msg + "\n");
        txtCommand.append("\t\t\t\t<=" + msg + "\n");
        msg = msg + "\r\n";
        output.writeUTF(msg);
        output.flush();
    }

    private String getUserPath(File f) throws IOException {
        String root = userRoot.getCanonicalPath();
        String path = f.getCanonicalPath();

        path = path.substring(root.length()).replace('\\', '/');
        if (path.charAt(0) != '/') {
            path = '/' + path;
        }
        return path;
    }

    @Override
    public void run() {
        String line = null;

        try {
            onConnect();
            while (isAlive) {
                line = input.readUTF();
                if (line == null) {
                    break;
                }

                System.out.println("=>" + line.trim());
                txtCommand.append("=>" + line.trim() + "\n");

                String cmd = null;
                String param = null;
                int i = line.indexOf(' ');
                if (i != -1) {
                    cmd = line.substring(0, i);
                    param = line.substring(i).trim();
                } else {
                    cmd = line;
                }

                processCommand(cmd, param);
            }
        } catch (SocketException s) {
            // s.printStackTrace();
//            System.out.println(
//                    "Client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " disconnected.");
        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.listener.notifyDisconnected(this);
            this.stop();
        }
    }

    private void processCommand(String cmd, String param) throws Exception {
        cmd = cmd.toUpperCase();
        cmd = cmd.trim();
        if (cmd.equals("USER")) {
            processUser(param);
        } else if (cmd.equals("PASS")) {
            processPassword(param);
        } else if (cmd.equals("CWD") && checkAuth()) {
            processChangeWorkingDirectory(param);
        } else if (cmd.equals("LIST") && checkAuth()) {
            processList();
        } else if (cmd.equals("NLST") && checkAuth()) {
            processNameList();
        } else if (cmd.equals("ALLO")) {
            processAllocate(param);
        } else if (cmd.equals("SIZE")) {
            processFileSize(param);
        } else if (cmd.equals("RETR") && checkAuth()) {
            processRetrieve(param);
        } else if (cmd.equals("STOR") && checkAuth()) {
            processStore(param);
        } else if (cmd.equals("APPE") && checkAuth()) {
            processAppend(param);
        } else if (cmd.equals("RNFR") && checkAuth()) {
            processRenameFrom(param);
        } else if (cmd.equals("RNTO") && checkAuth()) {
            processRenameTo(param);
        } else if (cmd.equals("DELE") && checkAuth()) {
            processDelete(cmd, param);
        } else if (cmd.equals("MKD") && checkAuth()) {
            processMakeDirectory(param);
        } else if (cmd.equals("RMD") && checkAuth()) {
            processRemoveDirectory(cmd, param);
        } else if (cmd.equals("PWD") && checkAuth()) {
            processPrintWorkingDirectory();
        } else if (cmd.equals("CDUP") && checkAuth()) {
            processChangeDirectoryToParent();
        } else if (cmd.equals("QUIT") && checkAuth()) {
            processQuit();
        } else if (cmd.equals("RESUME") && checkAuth()) {
            processResume(param);
        } else if (cmd.equals("PAUSE") && checkAuth()) {
            processPause(param);
        } else {
            println("500 Syntax error, command unrecognized ");
        }

    }

    private void onConnect() throws IOException {
        println("220 Welcome to FTP Daemon.");
    }

    private boolean checkAuth() throws IOException {
        if (!isAuth) {
            println("530 Not logged in.");
            return false;
        }
        return true;
    }

    @Override
    public void processUser(String param) throws IOException {
        this.username = param;
        println("331 User name okay, need password.");
    }

    @Override
    public void processPassword(String param) throws Exception {
        if (username == null) {
            println("503 Bad sequence of commands. Send USER first.");
            return;
        }

        this.isAuth = new Authenticator().isValidUser(username, param);
        if (!isAuth) {
            if (!username.equals("anonymous")) {
                Thread.sleep(3000L);
            }
            println("530 Login incorrect.");
        } else {
            userRoot = new File(System.getProperty("ftp.home"), username);
            userRoot = userRoot.getCanonicalFile();
            String privateRoot = System.getProperty("ftp.home." + username);
            if (privateRoot != null) {
                this.userRoot = new File(privateRoot);
                this.userRoot = userRoot.getCanonicalFile();
            }
            if (!userRoot.exists()) {
                userRoot.mkdirs();
            }
            userCurrent = userRoot;
            // System.out.println(userCurrent.getAbsolutePath());
            println("230 User " + username + " logged in.");
            data = new DataConnection();
            data.setUsername(username);
            data.setListener(this);
            // data.addDataConnectionListener(this);
//			println("225 Data connection open.");
            this.listener.notifyLogin(this);
        }

    }

    @Override
    public void processList() throws IOException {
        File[] files = userCurrent.listFiles();
        StringBuilder sb = new StringBuilder();

        Calendar cal = Calendar.getInstance();

        List<File> list = new ArrayList<File>(files.length);
        for (File f : files) {
            list.add(f);
        }

        Collections.sort(list, new Comparator<File>() {
            public int compare(File f0, File f1) {
                return f0.getName().compareTo(f1.getName());
            }
        });

        for (File f : list) {
            if (f.isDirectory()) {
                sb.append("drwxr-xr-x");
            } else if (f.isFile()) {
                sb.append("-rw-r--r--");
            } else {
                continue;
            }
            sb.append(' ');
            sb.append(String.format("%3d", 1)); // Number of link (linux)
            sb.append(' ');
            sb.append(String.format("%-8s", this.username)); // owner name
            sb.append(' ');
            sb.append(String.format("%-8s", this.username)); // owner group name
            sb.append(' ');
            long len = f.length();
            if (f.isDirectory()) {
                len = 4096;
            }
            sb.append(String.format("%8d", len));
            sb.append(' ');

            cal.setTimeInMillis(f.lastModified());
            sb.append(fmtDate.format(cal.getTime()));
            sb.append(' ');

            sb.append(f.getName());
            sb.append("\r\n");
        }

        if (data != null) {
            println("150 Opening ASCII mode data connection for file list");
            data.sendString(sb.toString());
        } else {
            println("552 Requested file list action aborted.");
        }
    }

    @Override
    public void processStore(String param) throws IOException {
        String[] parse = param.split(" ");
        parse[0] = parse[0].contains("/") ? parse[0].substring(parse[0].lastIndexOf("/")) : parse[0];
        String filename = parse[0];
        int numPieces = Integer.parseInt(parse[2]);

        File f = new File(userCurrent, parse[0]);

        if (this.conflict.isFileProcessing(f)) {
            println("500 file is exists.");
            String response = input.readUTF();
            System.out.println(response);
            if (response.startsWith("Override")) {
                processQueueRequestStore(param);
                return;

            } else if (response.startsWith("Duplicate")) {
                filename = "copy_" + filename;
                f = new File(userCurrent, filename);
                System.out.println(f.getCanonicalPath());
            }
        }
        this.conflict.addFileProcessing(f);

        long[] offsets = null;
        if (parse[1].equals("1")) {
            File unfinished = new File("unfinished/" + username, filename + ".unf");
            //System.err.println(unfinished.getCanonicalPath());
            if (!unfinished.exists()) {
//                println("550 No file unfinished on server");
//                return;
                offsets = new long[numPieces];
            } else {
                Object[] loaded = loadInfo(unfinished);
                f = (File) loaded[0];
                length = (long) loaded[1];
                offsets = (long[]) loaded[4];
            }
        } else if (parse[1].equals("0")) {
            offsets = new long[numPieces];
        }

        if (data != null) {

            String response = "150";
            for (long offset : offsets) {
                response += " " + offset;
            }
            println(response);

            String root = userRoot.getCanonicalPath();
            String remotePath = f.getCanonicalPath().substring(root.length());
            data.newFileSession(f, length, offsets, false, remotePath);
//            println("150 Opening BINARY mode data connection for " + param);
//            data.receiveFile(f, numPieces != 0 ? numPieces : 4, length);
//            this.numPieces = 0;
        } else {
            println("552 Requested file action aborted.");
        }
    }

    @Override
    public void processFileSize(String param) throws IOException {
        File f = null;
        if (param.charAt(0) == '/') {
            f = new File(userRoot, param);
        } else {
            f = new File(userCurrent, param);
        }

        if (f.exists()) {
            println("213 " + f.length());
        } else {
            println("550 " + param + ": No such file or directory");
        }
    }

    @Override
    public void processRetrieve(String param) throws IOException {
        String[] parse = param.split(" ");
        long[] offsets = new long[parse.length - 1];
        for (int i = 1; i < parse.length; i++) {
//            System.out.println(parse[i]);
            offsets[i - 1] = Long.parseLong(parse[i]);
        }
        File f = null;
        if (parse[0].charAt(0) == '/') {
            f = new File(userRoot, parse[0]);
        } else {
            f = new File(userCurrent, parse[0]);
        }

        String root = userRoot.getCanonicalPath();
        String remotePath = f.getCanonicalPath().substring(root.length());

        if (!f.exists()) {
            println("550 " + f.getCanonicalPath() + ": No such file or directory.");
            return;
        }

        if (data != null) {
            println("150 send file " + parse[0] + " (" + f.length() + " bytes)");
//            data.sendFile(f, offsets);
            data.newFileSession(f, f.length(), offsets, true, remotePath);
        }
    }

    public void processQuit() throws IOException {
        println("221 Goodbye.");
        isAlive = false;
    }

    @Override
    public void processChangeWorkingDirectory(String param) throws IOException {
        File toChange = null;
        if (param.length() > 0 && param.charAt(0) == '/') {
            toChange = new File(userRoot, param.substring(1));
        } else {
            toChange = new File(userCurrent, param);
        }

        if (!toChange.exists() || !toChange.isDirectory()) {
            println("550 " + param + ": No such file or directory");
            return;
        }

        String root = userRoot.getCanonicalPath();
        String willChange = toChange.getCanonicalPath();
        if (!willChange.startsWith(root)) {
            println("553 Requested action not taken.");
            return;
        }

        this.userCurrent = new File(willChange);
        println("250 CWD command successful.");
    }

    @Override
    public void processChangeDirectoryToParent() throws IOException {
        File toChange = userCurrent.getParentFile();
        String root = userRoot.getCanonicalPath();
        String willChange = toChange.getCanonicalPath();
        if (!willChange.startsWith(root)) {
            println("553 Requested action not taken.");
            return;
        }

        this.userCurrent = new File(willChange);
        println("200 CDUP command successfull.");
    }

    @Override
    public void processAllocate(String param) throws IOException {
        try {
            this.length = Long.parseLong(param);
        } catch (NumberFormatException e) {
            println("501 Syntax error in parameters must is number");
        }
        println("200 allocate command successful.");
    }

    @Override
    public void processAppend(String param) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void processRenameFrom(String param) throws IOException {
        this.targetFile = new File(userCurrent, param);
        if (!targetFile.exists()) {
            targetFile = null;
            println("550 File not found.");
        } else {
            println("250 File had setted to target.");
        }
    }

    @Override
    public void processRenameTo(String param) throws IOException {
        if (targetFile == null) {
            println("503 Bad sequence of commands. Command REFR first.");
        } else {
            if (targetFile.renameTo(new File(userCurrent, param))) {
                println("250 File is renamed to " + param);
            } else {
                println("553 Requested action not taken. File name not allowed.");
            }
        }

    }

    @Override
    public void processDelete(String cmd, String param) throws IOException {
        File f = null;
        if (param.charAt(0) == '/') {
            f = new File(userRoot, param);
        } else {
            f = new File(userCurrent, param);
        }

        if (!f.exists()) {
            println("550 " + param + ": not found.");
            return;
        }

        if (f.isFile() && f.delete()) {
            println("250 " + cmd + " command successful.");
        } else {
            println("450 Removing file was failed.");
        }
    }

    @Override
    public void processMakeDirectory(String param) throws IOException {
        File f = null;
        if (param.charAt(0) == '/') {
            f = new File(userRoot, param);
        } else {
            f = new File(userCurrent, param);
        }

        if (f.exists()) {
            println("521 Directory already exists.");
            return;
        }

        if (f.mkdir()) {
            println("257 \"" + getUserPath(f) + "\" - Directory successfully created.");
        } else {
            println("521 Making directory was failed.");
        }
    }

    @Override
    public void processRemoveDirectory(String cmd, String param) throws IOException {
        File f = null;
        if (param.charAt(0) == '/') {
            f = new File(userRoot, param);
        } else {
            f = new File(userCurrent, param);
        }

        if (!f.exists()) {
            println("521 " + param + ": No such directory.");
            return;
        }

        if (f.isDirectory() && f.delete()) {
            println("250 " + cmd + " command successful.");
        } else {
            println("521 Removing directory was failed.");
        }
    }

    @Override
    public void processPrintWorkingDirectory() throws IOException {
        String root = userRoot.getCanonicalPath();
        String curr = userCurrent.getCanonicalPath();

        curr = curr.substring(root.length());
        if (curr.length() == 0) {
            curr = "/";
        }
        curr = curr.replace('\\', '/');

        println("257 \"" + curr + "\" is current directory.");
    }

    @Override
    public void processResume(String param) throws IOException {
        int index = Integer.parseInt(param);
        data.resume(index);

    }

    @Override
    public void processPause(String param) throws IOException {
        int index = Integer.parseInt(param);
        data.pause(index);

    }

    @Override
    public void processNameList() throws IOException {
        File[] files = userCurrent.listFiles();
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append(f.getName()).append("\r\n");
        }

        if (data != null) {
            println("150 Opening ASCII mode data connection for file list");
            data.sendString(sb.toString());
        } else {
            println("552 Requested file list action aborted.");
        }

    }

    @Override
    public void notifyFileTransferComplete(File file) {
        conflict.removeFileProccessing(file);
        RequestStore rs = conflict.popRequest();
        if (rs == null) {
            return;
        }
        try {
            rs.resumeProcess();
        } catch (IOException ex) {
            System.err.println("Resume process queue");
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Object[] loadInfo(File fileInfo) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileReader fr = new FileReader(fileInfo);
        BufferedReader br = new BufferedReader(fr);
        while (br.ready()) {
            String s = br.readLine();
            sb.append(s);
            sb.append("\n");
        }
        br.close();
        fr.close();

        String[] parsed = sb.toString().split("\n");
        Object[] result = new Object[5];

        File file = new File(parsed[1]);
        result[0] = file;
        long lengthOfFile = Long.parseLong(parsed[2]);
        result[1] = lengthOfFile;
        boolean isSend = parsed[3].equals("1") ? true : false;
        result[2] = isSend;
        int numPieces = Integer.parseInt(parsed[4]);
        result[3] = numPieces;

        long[] offsets = new long[numPieces];
        for (int i = 0; i < numPieces; i++) {
            String tmp = parsed[5 + i];
            String[] ptmp = tmp.split(" ");

            int pIndex = Integer.parseInt(ptmp[0]);
            long pStartOffset = Long.parseLong(ptmp[1]);
            long pLength = Long.parseLong(ptmp[2]);
            long pOffset = Long.parseLong(ptmp[3]);

            offsets[i] = pOffset;
        }
        result[4] = offsets;
        return result;
    }

    private void processQueueRequestStore(String param) {
        conflict.addRequest(this, param);
    }
}
