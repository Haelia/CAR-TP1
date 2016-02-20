package src;

import java.io.BufferedReader;
import java.io.File;
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
	private File repertoire;

	public FtpRequest(final Socket socket) throws IOException {
		super();
		this.socket = socket;
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.output = new PrintWriter(socket.getOutputStream(), true);
		this.username = "";
		this.authentifie = false;
		this.repertoire = new File(System.getProperty("user.dir"));
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

			if (estParametrable(cmd[0])) {
				if (cmd.length < 2) {
					this.output.println(Constantes.CODE_PARAM_INVALIDE + " " + Constantes.MSG_PARAM_INVALIDE);
					this.output.flush();
				} else {
					if (authentificationEstValide(cmd[0])) {
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
						}
					}
				}
			} else {
				if (authentificationEstValide(cmd[0])) {
					switch (cmd[0]) {
					case Constantes.CMD_LIST:
						processLIST();
						break;

					case Constantes.CMD_QUIT:
						processQUIT();
						break;

					default:
						this.output.println(Constantes.CODE_CMD_INVALIDE + " " + Constantes.MSG_CMD_INVALIDE);
						this.output.flush();
						break;
					}
				}
			}

		} while (!Constantes.CMD_QUIT.equals(cmd[0]));
	}

	private boolean estParametrable(String cmd) {
		switch (cmd) {
		case Constantes.CMD_USER:
		case Constantes.CMD_PASS:
			return true;
		default:
			return false;
		}
	}

	private boolean requiertAuthenfication(String cmd) {
		switch (cmd) {
		case Constantes.CMD_LIST:
			return true;
		default:
			return false;
		}
	}

	public void processUSER(final String user) throws IOException {
		this.authentifie = false;
		if (Serveur.utilisateurs.containsKey(user)) {
			this.username = user;
			this.output.println(Constantes.CODE_ATTENTE_MDP + " " + Constantes.MSG_ATTENTE_MDP);
			this.output.flush();
		} else {
			this.output.println(Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_AUTH_ECHOUE);
			this.output.flush();
		}

	}

	public boolean processPASS(String param) {
		if (username == "") {
			this.output.println(Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_SAISIR_IDENTIFIANT);
			this.output.flush();
		} else {
			if (Serveur.utilisateurs.get(this.username).equals(param)) {
				this.authentifie = true;
				this.output.println(Constantes.CODE_AUTH_REUSSIE + " " + Constantes.MSG_AUTH_REUSSIE);
				this.output.flush();
				return true;
			} else {
				this.output.println(Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_AUTH_ECHOUE);
				this.output.flush();
			}
		}
		return false;
	}

	public boolean processRETR(String param) {
		return false;
	}

	public boolean processSTOR(String param) {
		return false;
	}

	public boolean processLIST() {
		String liste = "";
		File[] fichiers = repertoire.listFiles();
		for (File fichier : fichiers) {
			liste += fichier.getName() + "\n";
		}

		this.output.println(liste);
		this.output.flush();
		return true;
	}

	public String processQUIT() throws IOException {
		System.out.println("processQUIT");
		this.socket.close();
		return Constantes.CMD_QUIT;
	}

	public boolean authentificationEstValide(String cmd) {
		if (requiertAuthenfication(cmd)) {
			if (!this.estAuthentifie()) {
				this.output.println(Constantes.CODE_NON_AUTH + " " + Constantes.MSG_NON_AUTH);
				this.output.flush();
				return false;
			}
		}
		return true;
	}

	public boolean estAuthentifie() {
		return this.authentifie;
	}
}
