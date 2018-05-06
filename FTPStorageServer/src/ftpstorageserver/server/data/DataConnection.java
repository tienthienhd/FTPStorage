/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server.data;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HP Zbook 15
 */
public class DataConnection implements Runnable, FileSessionListener {

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private ArrayList<FileSession> files = new ArrayList<>();

    private DataConnectionListener listener;
    private String username;

    private Thread thread;

    /**
     * Create ServerSocket to listening client
     *
     * @throws IOException
     */
    public DataConnection() throws IOException {
        ServerSocket serv = new ServerSocket(20);
        serv.setSoTimeout(10 * 1000);
        this.socket = serv.accept();
        serv.close();
        serv = null;

        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() throws IOException {
        if (files != null) {
            files.removeAll(files);
        }

        if (socket != null) {
            socket.close();
            socket = null;
        }

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void run() {
    }

    public void newFileSession(File file, long lengthFile, long[] offsets, boolean isSend, String remotePath) throws IOException {
        remotePath = remotePath + "/" + file.getName();
        FileSession fs = new FileSession();
        fs.setListener(this);
        fs.setUsername(username);
        fs.configFile(file, lengthFile, offsets, isSend, remotePath);
        this.files.add(fs);
        fs.start();
    }

    public void sendString(String msg) throws IOException {
        this.output.writeUTF(msg);
        this.output.flush();
    }

    @Override
    public void transferComplete(FileSession file) {
        this.listener.notifyFileTransferComplete(file.getFile());
        System.out.println("DataConnection Transferred file " + file.getName());
//        this.files.remove(file);
    }

    public void setListener(DataConnectionListener l) {
        this.listener = l;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void pause(int index) {
        FileSession fs;
        try {
            fs = this.files.get(index);
            fs.pause();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("File session index invalid");
        }
    }

    public void resume(int index) {
        FileSession fs;
        try {
            fs = this.files.get(index);
            fs.resume();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("File session index invalid");
        }
    }

    public void remove(int indexFile) {
        this.files.remove(indexFile);
    }

    public void saveUnfinished(String username) {
        System.out.println("Saving unfinished....");
        for (FileSession fs : files) {
            try {
                fs.saveUnfinished();
            } catch (IOException ex) {
                System.out.println("Save information unfinished failure.");
                Logger.getLogger(DataConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Deprecated
    public void loadUnfinished() {
        File unfinished = new File("unfinished/" + username);
        if (!unfinished.exists()) {
            System.out.println("No file unfinished.");
            return;
        }
        File[] files = unfinished.listFiles();

        for (File f : files) {
            System.out.println("Load unfinished " + f.getName());
            try {
                loadUnfinished(f);

            } catch (IOException ex) {
                Logger.getLogger(DataConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Deprecated
    private void loadUnfinished(File fileInfo) throws IOException {
        if (!fileInfo.exists()) {
            throw new IOException(fileInfo.getName() + " don't exists.");
        }
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
        File file = new File(parsed[1]);
        long lengthOfFile = Long.parseLong(parsed[2]);
        boolean isSend = parsed[3].equals("1") ? true : false;
        int numPieces = Integer.parseInt(parsed[4]);

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

        String remotePath = parsed[parsed.length - 1];

//        this.listener.addPanelFileStatus(parsed[0], lengthOfFile, !isSend);
        FileSession fs = new FileSession();
//        fs.setRestore(true);
        fs.setListener(this);
        fs.setUsername(username);
        fs.configFileWithoutConnect(file, lengthOfFile, offsets, isSend, remotePath);

        String commandResume;
        if (isSend) {
            commandResume = "STOR " + remotePath;
            for (long offset : offsets) {
                commandResume += " " + offset;
            }
        } else {
            commandResume = "RETR " + remotePath;
            for (long offset : offsets) {
                commandResume += " " + offset;
            }
        }

//        fs.setCommandResume(commandResume);
        this.files.add(fs);
        fs.start();
    }

}
