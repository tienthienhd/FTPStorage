package client;

import java.io.IOException;

public interface IClient {
	
	public void user(String username) throws IOException;
	
	public void pass(String password) throws IOException;
	
	public void connect(String host) throws IOException;
	
	public void connect(String host, int port) throws IOException;
	
	public void connect(String host, int port, String user, String pass) throws IOException;
	
	public void cd(String path) throws IOException;
	
	public void makedir(String dirName) throws IOException;
	
	public void rmdir(String dirName) throws IOException;
	
	public void rename(String oldname, String newName) throws IOException;
	
	public void get(String pathname) throws IOException;
	
	public void put(String pathname) throws IOException;
	
	public void append(String pathname) throws IOException;
	
	public void delete(String pathname) throws IOException;
	
	public void disconnect() throws IOException;
	
	/**Display files in current directory
	 * @return
	 * @throws IOException
	 */
	public String list() throws IOException ;
	
	/**Display current directory
	 * @return
	 */
	public String pwd();
	
	/**Rename file
	 * @param file
	 * @return true file if success
	 */
	
}
