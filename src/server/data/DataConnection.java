package server.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author HP Zbook 15
 * DataConnection is create data connection to transfer data client-server
 * Implements Runnable to process parallel with Command Connection
 */
/**
 * @author HP Zbook 15
 *
 */
public class DataConnection implements Runnable {
	
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	private Thread thread;
	
	
	/** Create ServerSocket to listening client
	 * @throws IOException
	 */
	public DataConnection() throws IOException {
		ServerSocket serv = new ServerSocket(20);
		serv.setSoTimeout(10 *1000);
		this.socket = serv.accept();
		serv.close();
		serv = null;
		this.input = new DataInputStream(this.socket.getInputStream());
		this.output = new DataOutputStream(this.socket.getOutputStream());
	}
	
	public void start() {
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void stop() throws IOException {
		if(socket != null) {
			socket.close();
			socket = null;
		}
		
		if(thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	@Override
	public void run() {}
	
	public void sendFile(File file, int numPieces) throws IOException {
		FileSession fileSession = new FileSession(file);
		fileSession.sendFile(numPieces);
		fileSession.start();
	}
	
	public void receiveFile(File file, int numPieces, long lengthFile) throws IOException {
		FileSession fileSession = new FileSession(file);
		fileSession.receiveFile(numPieces, lengthFile);
		fileSession.start();
	}

	public void sendString(String msg) throws IOException {
		this.output.writeUTF(msg);
	}


}
