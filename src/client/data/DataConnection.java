package client.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

public class DataConnection implements Runnable {
	
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	private Thread thread;
	
	
	/** Create ServerSocket to listening client
	 * @throws IOException
	 */
	public DataConnection() throws IOException {
		this.socket = new Socket("localhost", 20);
		this.input = new DataInputStream(socket.getInputStream());
		this.output = new DataOutputStream(socket.getOutputStream());
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
	
	public String ls() throws IOException {
		return receiveString();
	}

	public String receiveString() throws IOException {
		String msg = this.input.readUTF();
		return msg;
	}


}
