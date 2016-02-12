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
	private boolean authentifie;

	public FtpRequest(final Socket socket) throws IOException {
		super();
		this.socket = socket;
		this.input = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		this.output = new PrintWriter(socket.getOutputStream(), true);
		this.username = "";
		this.authentifie = false;
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
		String[] cmd;

		do {
			String request = this.input.readLine();
			cmd = request.split(" ");

			System.out.println("Request: " + request);

			switch (cmd[0]) {
			case Constantes.CMD_USER:
				processUSER(cmd[1]);
				break;

			case Constantes.CMD_PASS:
				processPASS(cmd[1]);
				break;

			case Constantes.CMD_RETR:
				processRETR(cmd[1]);
				break;

			case Constantes.CMD_STOR:
				processSTOR(cmd[1]);
				break;

			case Constantes.CMD_LIST:
				processLIST();
				break;

			default:
				break;
			}
		} while (!Constantes.CMD_QUIT.equals(cmd[0]));

		processQUIT();
	}

	protected void processUSER(final String user) throws IOException {
		if (Serveur.utilisateurs.containsKey(user)) {
			this.username = user;
			this.output.println(Constantes.CODE_ATTENTE_MDP + " "
					+ Constantes.MSG_ATTENTE_MDP);
			this.output.flush();
		} else {
			this.output.println(Constantes.CODE_AUTH_ECHOUE + " "
					+ Constantes.MSG_AUTH_ECHOUE);
			this.output.flush();
		}

	}

	public boolean processPASS(String param) {
		if (username == "") {
			this.output.println(Constantes.CODE_AUTH_ECHOUE + " "
					+ Constantes.MSG_SAISIR_IDENTIFIANT);
			this.output.flush();
		} else {
			if (Serveur.utilisateurs.get(this.username).equals(param)) {
				this.authentifie = true;
				this.output.println(Constantes.CODE_AUTH_REUSSIE + " "
						+ Constantes.MSG_AUTH_REUSSIE);
				this.output.flush();
			} else {
				this.output.println(Constantes.CODE_AUTH_ECHOUE + " "
						+ Constantes.MSG_AUTH_ECHOUE);
				this.output.flush();
			}
		}
		return false;
	}

	public boolean processRETR(String param) {
		if (this.authentifie == false) {
			this.output.println(Constantes.CODE_NON_AUTH + " "
					+ Constantes.MSG_NON_AUTH);
			this.output.flush();
		}
		return false;
	}

	public boolean processSTOR(String param) {
		if (this.authentifie == false) {
			this.output.println(Constantes.CODE_NON_AUTH + " "
					+ Constantes.MSG_NON_AUTH);
			this.output.flush();
		}
		return false;
	}

	public boolean processLIST() {
		if (this.authentifie == false) {
			this.output.println(Constantes.CODE_NON_AUTH + " "
					+ Constantes.MSG_NON_AUTH);
			this.output.flush();
		}
		return false;
	}

	public String processQUIT() throws IOException {
		System.out.println("processQUIT");
		this.socket.close();
		return Constantes.CMD_QUIT;
	}
}
