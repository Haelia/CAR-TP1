package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class FtpRequest extends Thread {

	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private String username;

	public FtpRequest(final Socket socket) throws IOException {
		super();
		this.socket = socket;
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.output = new PrintWriter(socket.getOutputStream(), true);
	}

	@Override
	public void run() {
		try {
			processRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processRequest() throws IOException {
		String cmd;
		
		do {
			String request = this.input.readLine();
			int space = request.indexOf(" ");

			if (space != -1) {
				cmd = request.substring(0, space);
			} else {
				cmd = request;
			}

			String param = request.substring(space + 1);

			System.out.println("Request: " + request);
			System.out.println("Cmd: " + cmd);

			switch (cmd) {
			case "USER":
				processUSER(param);
				break;

			case "PASS":
				processPASS(param);
				break;

			case "RETR":
				processRETR(param);
				break;

			case "STOR":
				processSTOR(param);
				break;

			case "LIST":
				processLIST();
				break;

			default:
				break;
			}
		} while (!"QUIT".equals(cmd));

		processQUIT();
	}

	protected void processUSER(final String user) throws IOException {
		this.username = user;
		this.output.println("331 En attente du mot de passe");
		this.output.flush();
	}

	public boolean processPASS(String param) {
		System.out.println("processPASS");
		return false;
	}

	public boolean processRETR(String param) {
		System.out.println("processRETR");
		return false;
	}

	public boolean processSTOR(String param) {
		System.out.println("processSTOR");
		return false;
	}

	public boolean processLIST() {
		System.out.println("processLIST");
		return false;
	}

	public String processQUIT() {
		System.out.println("processQUIT");
		try {
			this.socket.close();
			return "QUIT";
		} catch (IOException e) {
			e.printStackTrace();
			return "Erreur";
		}
	}
}
