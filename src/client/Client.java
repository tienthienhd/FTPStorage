package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import client.data.DataConnection;

/**
 * @author HP Zbook 15
 *
 */
public class Client implements IClient {

	private Socket socket;
	private DataConnection dataConnection;
	private DataInputStream input;
	private DataOutputStream output;

	public Client() {

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
		System.out.println("username ok");
		pass(password);
		System.out.println("password ok");

		connectData();

	}

	private void connectData() throws UnknownHostException, IOException {
		this.dataConnection = new DataConnection();
		System.out.println("Data flow connect successed.");
	}

	@Override
	public String list() throws IOException {
		sendLine("LIST ");
		String response = readLine();
		if (response.startsWith("150 ")) {
			String dataResponse = dataConnection.ls();
			return dataResponse;
		}
		return "error";

	}

//	public void getFile(String remote, String local) throws IOException {
//		sendLine("RETR " + remote);
//
//		String response = readLine();
//		System.out.println(response);
//		if (response.startsWith("150 ")) {
//
//			// this.dataConnection.receiveFile(file, 4, );
//
//			// File f = new File("./local/", local);
//			//
//			// System.out.println("create socket!");
//			// Socket s1 = new Socket("localhost", 20);
//			// Socket s2 = new Socket("localhost", 20);
//			// Socket s3 = new Socket("localhost", 20);
//			// Socket s4 = new Socket("localhost", 20);
//
//		}
//	}

	@Override
	public void disconnect() throws IOException {
		try {
			sendLine("QUIT");
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
		if(!response.startsWith("")) {
			throw new IOException("FTP unknow directory: " + path);
		}
	}

	@Override
	public void makedir(String dirName) throws IOException {
		sendLine("MKD " + dirName);
		String response = readLine();
		if(!response.startsWith("")) {
			throw new IOException("Make directory failed : " + dirName);
		}
	}

	@Override
	public void rmdir(String dirName) throws IOException {
		sendLine("RMD " + dirName);
		String response = readLine();
		if(!response.startsWith("")) {
			throw new IOException("Remove directory failed : " + dirName);
		}
	}

	@Override
	public void rename(String oldname, String newName) throws IOException {
		sendLine("RNFR " + oldname);
		String response = readLine();
		if(!response.startsWith("")) {
			throw new IOException("Rename from : Don't exists " + oldname);
		}
		sendLine("RNTO " + newName);
		response = readLine();
		if(!response.startsWith("")) {
			throw new IOException("Rename to: rename failed for " + newName);
		}
	}

	@Override
	public void get(String pathname) throws IOException {
		

	}

	@Override
	public void put(String pathname) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void append(String pathname) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String pathname) throws IOException {
		sendLine("DELE " + pathname);
		String response = readLine();
		if(!response.startsWith("")) {
			throw new IOException("Delete: failed to delete " + pathname);
		}
	}

	@Override
	public String pwd() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void allocate(long size) throws IOException {
		sendLine("ALLO " + size);
		String response = readLine();
		if(!response.startsWith("")) {
			throw new IOException("Allocate: failed with " + size);
		}
	}
}
