/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HP Zbook 15
 */
public class Piece implements Runnable {

    private Thread thread;
    private PieceListener listener;

    private File file;
    private RandomAccessFile raf;
    private long startOffset;
    private long length;
    private int pieceIndex;

    private long offset; // offset on piece

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private boolean isSend; // true: is send piece, false is receive piece
    private Object lock = new Object();
    private boolean notified; // if true : this thread is running else wait

    public Piece() {

    }
    
    public Piece(File file, int pieceIndex){
        setFile(file);
        setPieceIndex(pieceIndex);
    }
    
    public Piece(File file, int pieceIndex, long length, long startOffset){
        this(file, pieceIndex);
        setLength(length);
        setStartOffset(startOffset);
        setOffset(0);
    }
    
    public Piece(File file, int pieceIndex, long length, long startOffset, long offset){
        this(file, pieceIndex, length, startOffset);
        setOffset(offset);
    }
    
    public Piece(File file, int pieceIndex, long length, long startOffset, long offset, boolean isSend){
        this(file, pieceIndex, length, startOffset, offset);
        setIsSend(isSend);
    }


    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public void setSocket(Socket socket) {
        try {
            this.socket = socket;
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Piece.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
        setRaf();
    }

    public File getFile() {
        return file;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getLength() {
        return length;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public long getOffset() {
        return offset;
    }

    public boolean isIsSend() {
        return isSend;
    }

    public void setRaf() {
        try {
            if (isSend) {
                this.raf = new RandomAccessFile(file, "rw");
                this.raf.seek(startOffset + offset);
            } else {
                String part = new String(file.getName() + ".part" + pieceIndex);
                //System.out.println("debug:" + part);
                File f = new File(file.getParentFile(), part);
                
//                // hidden part
//                Path p = Paths.get(f.getCanonicalPath());
//                Files.setAttribute(p, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                
                this.raf = new RandomAccessFile(f, "rw");
                this.raf.seek(f.length());
            }
        } catch (IOException ex) {
            System.out.println("Can't create raf");
            Logger.getLogger(Piece.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        this.notified = true;
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                socket = null;
            }
            socket = null;
        }

        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            raf = null;
        }

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

        synchronized (this) {
            notifyAll();

        }
    }

    public synchronized boolean isAlive() {
        if (thread != null) {
            return thread.isAlive();
        }
        return false;
    }

    @Override
    public void run() {
        try {
            synchronized (lock) {
                if (!notified) {
//                    System.out.println("DEBUG: lock.wait()");
                    lock.wait();
                }
            }

            if (isSend) {
                send();
            } else {
                receive();
            }
            notifyComplete();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            this.listener.notifyInteruptedPiece(this);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            this.listener.notifyInteruptedPiece(this);
        } finally {
            stop();
        }
    }

    public synchronized void pause() {
        synchronized (lock) {
            lock.notify();
        }
        this.notified = false;
    }

    public synchronized void resume() {
        synchronized (lock) {
            lock.notify();
        }
        this.notified = true;
    }

    private void receive() throws InterruptedException, IOException {
        byte[] buffer = new byte[16384];
        long remaining = length;
        while (remaining > 0) {
            synchronized (lock) {
                if (!notified) {
                    lock.wait();
                }
            }

            int bytes = input.read(buffer, 0, remaining > 16384 ? 16384 : (int) remaining);
            if (bytes < 0) {
                return;
            }
            remaining -= bytes;
            this.offset += bytes;
            raf.write(buffer, 0, bytes);

//            int percent = (int) ((offset / (float) length) * 100);
//            this.progressBar.setValue(percent);
        }
    }

    private void send() throws InterruptedException, IOException {
        byte[] buffer = new byte[16384];
        long remaining = length;
        while (remaining > 0) {
            synchronized (lock) {
                if (!notified) {
                    lock.wait();
                }
            }

            int bytes = raf.read(buffer, 0, remaining > 16384 ? 16384 : (int) remaining);
            if (bytes < 0) {
                return;
            }
            remaining -= bytes;
            this.offset += bytes;
            output.write(buffer, 0, bytes);

//            int percent = (int) ((offset / (float) length) * 100);
//            this.progressBar.setValue(percent);
        }
    }

    public void notifyComplete() {
        this.listener.transferPieceComplete(this);
    }

    public void setListener(PieceListener l) {
        this.listener = l;
    }

//    private JProgressBar progressBar = null;
//
//    public void setProgressBar(JProgressBar progressBar) {
//        this.progressBar = progressBar;
//    }
}
