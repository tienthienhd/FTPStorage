package server;

import java.io.IOException;

public interface ISession {
	public void processUser(String param) throws IOException;

	public void processPassword(String param) throws Exception;

	public void processChangeWorkingDirectory(String param) throws IOException;
	
	public void processChangeDirectoryToParent() throws IOException;

	public void processList() throws IOException;

	public void processNameList() throws IOException;
	
	public void processAllocate(String param) throws IOException;

	public void processRetrieve(String param) throws IOException;

	public void processStore(String param) throws IOException;

	public void processAppend(String param) throws IOException;

	public void processRenameFrom(String param) throws IOException;

	public void processRenameTo(String param) throws IOException;

	public void processDelete(String cmd, String param) throws IOException;

	public void processMakeDirectory(String param) throws IOException;

	public void processRemoveDirectory(String cmd, String param) throws IOException;

	public void processPrintWorkingDirectory() throws IOException;

	public void processQuit() throws IOException;

	public void processResume() throws IOException;

	public void processPause() throws IOException;
}
