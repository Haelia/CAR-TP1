package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

	public static void main(String[] args) {
		try {
			ServerSocket ss = new ServerSocket(1252);
			while(true) {
				Socket s = ss.accept();
				System.out.println("Connexion établie");
				new Thread(new FtpRequest(s)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
