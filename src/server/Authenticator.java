package server;

public class Authenticator 
{
	public Authenticator()
	{

	}

	public boolean isValidUser( String user, String pass ) 
		throws Exception
	{
		String fileAuth = System.getProperty("ftp.user." + user);
		if( fileAuth!=null && pass.equals(fileAuth) )
			return true;

		return false;
	}
}
