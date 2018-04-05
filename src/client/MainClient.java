package client;

import java.io.IOException;

public class MainClient {

	public static void main(String[] args) throws IOException {
		Client c = new Client();
		c.connect("localhost", 21, "tienthien", "tienthien");
		String ls = c.list();
		System.out.println(ls);
		
		c.cd("sdd");
		
		ls = c.list();
		System.out.println(ls);
		
		c.makedir("new directory");
		
		ls = c.list();
		System.out.println(ls);
		
		c.rmdir("new directory");
		
		ls = c.list();
		System.out.println(ls);
		
		
		c.delete("a.txt");
		
		ls = c.list();
		System.out.println(ls);
		
//		c.getFile("a.txt", "a.txt");
	}
}
