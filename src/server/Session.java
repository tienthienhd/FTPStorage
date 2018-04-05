package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import server.data.DataConnection;

public class Session implements Runnable, ISession {

	private Socket socket;
	private Thread thread;
	private boolean isAlive;

	private DataInputStream input;
	private DataOutputStream output;

	private DataConnection data;

	private String username;
	private boolean isAuth;
	private File userRoot;
	private File userCurrent;

	private SimpleDateFormat fmtDate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	private SimpleDateFormat fmtStamp = new SimpleDateFormat("yyyyMMddHHmmss");
	private int numPieces = 0;

	public Session(Socket s) throws IOException {
		this.socket = s;
		this.input = new DataInputStream(s.getInputStream());
		this.output = new DataOutputStream(s.getOutputStream());

		System.out.println("New session was created. (" + s.getInetAddress() + ")");
		onConnect();
	}

	public void start() {
		this.isAlive = true;
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	private void println(String msg) throws IOException {
		System.out.println("<=" + msg);
		msg = msg + "\r\n";
		output.writeUTF(msg);
		output.flush();
	}
	
	private String getUserPath(File f) throws IOException {
		String root = userRoot.getCanonicalPath();
		String path = f.getCanonicalPath();

		path = path.substring(root.length()).replace('\\', '/');
		if (path.charAt(0) != '/')
			path = '/' + path;
		return path;
	}

	@Override
	public void run() {
		String line = null;

		try {
			while (isAlive) {
				line = input.readUTF();
				if (line == null) {
					break;
				}

				System.out.println("=>" + line);

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
			System.out.println(
					"Client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " disconnected.");
			if (socket != null) {
				if (!socket.isClosed()) {
					try {
						this.socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				this.socket = null;
				this.thread.interrupt();
			}
		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processCommand(String cmd, String param) throws Exception {
		cmd = cmd.toUpperCase();
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
			processResume();
		} else if (cmd.equals("PAUSE") && checkAuth()) {
			processPause();
		} else {
			println("500 " + cmd + " not understood.");
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
		println("331 Password required for " + username);
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
			String privateRoot = System.getProperty("ftp.home." + username);
			if (privateRoot != null) {
				this.userRoot = new File(privateRoot);
			}
			if (!userRoot.exists()) {
				userRoot.mkdirs();
			}
			userCurrent = userRoot;
			// System.out.println(userCurrent.getAbsolutePath());
			println("230 User " + username + " logged in.");
			data = new DataConnection();
			// data.addDataConnectionListener(this);
		}

	}

	@Override
	public void processList() throws IOException {
		File[] files = userCurrent.listFiles();
		StringBuilder sb = new StringBuilder();

		Calendar cal = Calendar.getInstance();

		List<File> list = new ArrayList<File>(files.length);
		for (File f : files)
			list.add(f);

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
			} else
				continue;
			sb.append(' ');
			sb.append(String.format("%3d", 1)); // Number of link (linux)
			sb.append(' ');
			sb.append(String.format("%-8s", this.username)); // owner name
			sb.append(' ');
			sb.append(String.format("%-8s", this.username)); // owner group name
			sb.append(' ');
			long len = f.length();
			if (f.isDirectory())
				len = 4096;
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
		String filename = param.split(" ")[0];
		long length = Long.parseLong(param.split(" ")[1]);

		File f = new File(userCurrent, filename);

		if (data != null) {
			println("150 Opening BINARY mode data connection for " + filename);
			data.receiveFile(f, numPieces != 0 ? numPieces : 4, length);
		} else {
			println("552 Requested file action aborted.");
		}
	}

	@Override
	public void processRetrieve(String param) throws IOException {
		File f = null;
		if (param.charAt(0) == '/') {
			f = new File(userRoot, param);
		} else {
			f = new File(userCurrent, param);
		}

		if (!f.exists()) {
			println("550 " + param + ": No such file or directory.");
			// if(data != null) {
			// data.stop();
			// }
			return;
		}

		if (data != null) {
			println("150 Opening BINAY mode data connection for " + param + " (" + f.length() + " bytes)");
			data.sendFile(f, numPieces != 0 ? numPieces : 4);
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
		println("250 CWD command successful");
	}

	@Override
	public void processAllocate(String param) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processAppend(String param) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRenameFrom(String param) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRenameTo(String param) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processDelete(String cmd, String param) throws IOException {
		File f = null;
		if (param.charAt(0) == '/')
			f = new File(userRoot, param);
		else
			f = new File(userCurrent, param);

		if (!f.exists()) {
			println("521 " + param + ": No such directory.");
			return;
		}

		if (f.isFile() && f.delete()) {
			println("250 " + cmd + " command successful.");
		} else {
			println("521 Removing file was failed.");
		}
	}

	@Override
	public void processMakeDirectory(String param) throws IOException {
		File f = null;
		if (param.charAt(0) == '/')
			f = new File(userRoot, param);
		else
			f = new File(userCurrent, param);

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
		if (param.charAt(0) == '/')
			f = new File(userRoot, param);
		else
			f = new File(userCurrent, param);

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
		String root = userRoot.getAbsolutePath();
		String curr = userCurrent.getAbsolutePath();

		curr = curr.substring(root.length());
		if (curr.length() == 0)
			curr = "/";
		curr = curr.replace('\\', '/');

		println("257 \"" + curr + "\" is current directory.");
	}

	@Override
	public void processResume() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processPause() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processNameList() throws IOException {
		File[] files = userCurrent.listFiles();
		StringBuilder sb = new StringBuilder();
		for (File f : files)
			sb.append(f.getName()).append("\r\n");

		if (data != null) {
			println("150 Opening ASCII mode data connection for file list");
			data.sendString(sb.toString());
		} else {
			println("552 Requested file list action aborted.");
		}

	}

	@Override
	public void processChangeDirectoryToParent() throws IOException {
		processChangeWorkingDirectory("..");
	}
}
