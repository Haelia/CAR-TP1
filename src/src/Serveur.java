package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur extends ServerSocket {

	public Serveur(int port) throws IOException {
		super(port);
	}

	public void start() throws IOException {
		while (true) {
			Socket socket = this.accept();

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("220 OK");
			out.flush();

			System.out.println("Connexion établie");

			FtpRequest ftpRequest = new FtpRequest(socket);
			ftpRequest.start();
		}
	}

	public static void main(String[] args) {
		try {
			Serveur server = new Serveur(1515);
			server.start();
			server.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}