package ftpstorage.client.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JProgressBar;

public class FileSession implements Runnable, PieceListener {

    private FileSessionListener listener;
    private String username;
    
    private String remotePath;
    private boolean restored = false;

    private File file;
    private ArrayList<Piece> pieces;
    private boolean isSend;
    private int numPieces = 0;
    private long lengthOfFile;

    private Thread thread;
    private boolean finished = false;

    /**
     * Use for resume transfer
     */
    public FileSession(){
        this.pieces = new ArrayList<>();
    }
    
    /**
     * Use for create new transfer
     * @param file 
     */
    public FileSession(File file) {
        this.file = file;
        this.pieces = new ArrayList<>();
    }
    
    public void setListener(FileSessionListener l) {
        this.listener = l;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getName() {
        return this.file.getName();
    }
    
    public void setRestore(boolean restored){
        this.restored = restored;
    }
    
    public boolean isRestored(){
        return this.restored;
    }
    
    public boolean isSend(){
        return this.isSend;
    }
    
    
    public void configFileWithoutConnect(File file, long lengthOfFile, long[] offsets, boolean isSend, String remotePath) throws IOException{
        this.file = file;
        this.isSend = isSend;
        this.lengthOfFile = lengthOfFile;
        this.numPieces = offsets.length;
        this.remotePath = remotePath;
        long lengthPiece = lengthOfFile / offsets.length;
        
        createPiecesWithoutStart(offsets, lengthPiece);
        
    }
    
    public void configFile(File file, long lengthOfFile, long[] offsets, boolean isSend, String remotePath) throws IOException{
        this.file = file;
        this.isSend = isSend;
        this.lengthOfFile = lengthOfFile;
        this.numPieces = offsets.length;
        this.remotePath = remotePath;
        long lengthPiece = lengthOfFile / offsets.length;
        
        createPieces(offsets, lengthPiece);
        connectPieces();
        setProgressBars();
    }
    
    private void createPiecesWithoutStart(long[] offsets, long lengthOfPiece){
        for(int i = 0; i < this.numPieces; i++){
            long startOffset = i * lengthOfPiece;
            long length = i != numPieces - 1 ? lengthOfPiece : lengthOfFile - lengthOfPiece * (numPieces - 1);
            Piece p = new Piece(file, i, length, startOffset, offsets[i], isSend);
            p.setListener(this);
            p.setNotified(false);
            this.pieces.add(p);
        }
        setProgressBars();
    }
    
    
    public void createPieces(long[] offsets, long lengthOfPiece){
        for(int i = 0; i < this.numPieces; i++){
            long startOffset = i * lengthOfPiece;
            long length = i != numPieces - 1 ? lengthOfPiece : lengthOfFile - lengthOfPiece * (numPieces - 1);
            Piece p = new Piece(file, i, length, startOffset, offsets[i], isSend);
            p.setListener(this);
            p.setNotified(true);
            this.pieces.add(p);
        }
    }
    
    public void connectPieces() throws IOException{
        for(int i = 0; i < numPieces; i++){
            Socket s = new Socket("localhost", 20);
            this.pieces.get(i).setSocket(s);
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void run() {
        for (Piece p : pieces) {
            p.start();
        }

        try {
            for (Piece p : pieces) {
                synchronized (p) {
                    if (!p.isAlive()) {
                        continue;
                    }
                    p.wait();
                }
            }
            System.out.println("Transfer complete");
            if (!isSend) {
                craftFile();
            }
            notifyComplete();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            stop();
        }
    }

    private void craftFile() {
        System.out.println("Crafting...");
        byte[] buffer = new byte[524288];
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
            for (int i = 0; i < numPieces; i++) {
                File f = new File(file.getAbsolutePath() + ".part" + i);
                DataInputStream dis = new DataInputStream(new FileInputStream(f));
                long remaining = f.length();
                while (remaining > 0) {
                    int bytes = dis.read(buffer); // don't need limited because read full file
                    if (bytes < 0) {
                        break;
                    }
                    remaining -= bytes;
                    dos.write(buffer, 0, bytes);
                }
                dis.close();
                f.delete();
            }
            dos.flush();
            dos.close();
            System.out.println("Craft complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void pause() {
        for (Piece p : pieces) {
            p.pause();
        }
    }

    void resume() {
        for (Piece p : pieces) {
            p.resume();
        }
    }

    public void notifyComplete() {
        this.finished = true;
        File f = new File("unfinished/" + username, this.file.getName() + ".unf");
        if (f.exists()) {
            f.delete();
        }
        this.listener.transferComplete(this);
    }

    

    @Override
    public synchronized void transferPieceComplete(Piece p) {
        System.out.println("Piece " + p.getPieceIndex() + " : transfer complete!");
        synchronized (p) {
            p.notify();
        }
//        this.pieces.remove(p);
    }

    public void setProgressBars() {
        JProgressBar[] bars = this.listener.getProgressBars();
        for (int i = 0; i < bars.length; i++) {
            this.pieces.get(i).setProgressBar(bars[i]);
        }
    }

    @Override
    public void notifyInteruptedPiece(Piece p) {
        System.out.println("Piece interupted....");
    }

    void saveUnfinished() throws IOException {
        File folder = new File("unfinished/" + username);
        if(!folder.exists()){
            folder.mkdir();
        }
        if(!this.finished){
            File f = new File(folder, this.file.getName() + ".unf");
        if (!f.exists()) {
            f.createNewFile();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());//filename
        sb.append("\n");
        sb.append(file.getCanonicalPath());//filepath
        sb.append("\n");
        sb.append(lengthOfFile);//file size
        sb.append("\n");
        sb.append(isSend ? 1 : 0);// is Send : 1-send 0-receive
        sb.append("\n");
        sb.append(this.numPieces); // number of pieces
        for (int i = 0; i < numPieces; i++) {
            Piece p = pieces.get(i);
            sb.append("\n");
            sb.append(p.getPieceIndex());// pieceindex 
            sb.append(" ");
            sb.append(p.getStartOffset());// startOffset
            sb.append(" ");
            sb.append(p.getLength());//Piece length
            sb.append(" ");
            sb.append(p.getOffset());// offset
        }
        sb.append("\n");
        sb.append(remotePath);
        FileWriter fw = new FileWriter(f);
        fw.write(sb.toString());
        fw.close();
        }
    }
    
    
    private String commandResume;
    public String getCommandResume(){
        return this.commandResume;
    }
    public void setCommandResume(String commandResume){
        this.commandResume = commandResume;
    }

}
