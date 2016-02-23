package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FtpRequest extends Thread {

	private static String commandes[] = { Constantes.CMD_USER, Constantes.CMD_PASS, Constantes.CMD_QUIT,
			Constantes.CMD_LIST, Constantes.CMD_RETR, Constantes.CMD_STOR, Constantes.CMD_SYST };

	private static int params[] = { 1, 1, 0, 0, 1, 1, 0 };

	private Socket socket;
	private Socket socketData;
	private BufferedReader input;
	private PrintWriter output;
	private String username;
	private boolean authentifie;
	private File repertoire;

	public FtpRequest(final Socket socket, String repertoire) throws IOException {
		super();
		this.socket = socket;
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.output = new PrintWriter(socket.getOutputStream(), true);
		this.username = "";
		this.authentifie = false;
		this.repertoire = new File(repertoire);
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

			if (estValideCommande(cmd[0], cmd.length - 1)) {
				switch (cmd[0]) {
				case Constantes.CMD_USER:
					processUSER(cmd[1]);
					break;

				case Constantes.CMD_PASS:
					processPASS(cmd[1]);
					break;

				case Constantes.CMD_LIST:
					processLIST();
					break;

				case Constantes.CMD_RETR:
					processRETR(cmd[1]);
					break;

				case Constantes.CMD_STOR:
					processSTOR(cmd[1]);
					break;

				case Constantes.CMD_SYST:
					processSYST();
					break;

				case Constantes.CMD_QUIT:
					processQUIT();
					break;
				}
			}

		} while (!Constantes.CMD_QUIT.equals(cmd[0]));
	}

	public boolean estValideCommande(String cmd, int nbParams) {
		if (!Arrays.asList(FtpRequest.commandes).contains(cmd)) {
			this.output.println(Constantes.CODE_CMD_INVALIDE + " " + Constantes.MSG_CMD_INVALIDE);
			this.output.flush();
			return false;
		}
		if (!estValideParametre(cmd, nbParams))
			return false;
		if (requiertAuthenfication(cmd)) {
			if (!this.estAuthentifie()) {
				this.output.println(Constantes.CODE_NON_AUTH + " " + Constantes.MSG_NON_AUTH);
				this.output.flush();
				return false;
			}
		}
		return true;
	}

	public boolean estValideParametre(String cmd, int nbParams) {
		int index = Arrays.asList(FtpRequest.commandes).indexOf(cmd);
		if (FtpRequest.params[index] == nbParams)
			return true;
		this.output.println(Constantes.CODE_PARAM_INVALIDE + " " + Constantes.MSG_PARAM_INVALIDE);
		this.output.flush();
		return false;
	}

	public boolean requiertAuthenfication(String cmd) {
		switch (cmd) {
		case Constantes.CMD_USER:
		case Constantes.CMD_PASS:
		case Constantes.CMD_QUIT:
			return false;
		default:
			return true;
		}
	}

	public boolean estAuthentifie() {
		return this.authentifie;
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

	public void processLIST() {
		String liste = "";
		File[] fichiers = repertoire.listFiles();

		for (File fichier : fichiers) {
			liste += fichier.getName() + "\n";
		}

		this.output.println(liste);
		this.output.flush();
	}

	public void processQUIT() throws IOException {
		this.output.println(Constantes.CODE_DECONNEXION + " " + Constantes.MSG_DECONNEXION);
		this.output.flush();
		this.socket.close();
	}

	public void processRETR(final String filename) throws IOException {
		try {
			InputStream flux = new FileInputStream(this.repertoire.getPath() + "/" + filename);
			InputStreamReader lecture = new InputStreamReader(flux);
			BufferedReader buff = new BufferedReader(lecture);
			String tmp = "", line;
			while ((line = buff.readLine()) != null) {
				tmp += line + "\n";
			}
			buff.close();

			OutputStreamWriter outputWriterData = new OutputStreamWriter(socketData.getOutputStream());
			outputWriterData.write(tmp);
			outputWriterData.flush();

			socketData.close();

			this.output.println(Constantes.CODE_TRANSFERT_REUSSI + " " + Constantes.MSG_TRANSFERT_REUSSI);
			this.output.flush();
		} catch (Exception e) {
			this.output.println(Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_TRANSFERT_ECHOUE);
			this.output.flush();
		}

	}

	public void processSTOR(final String filename) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socketData.getInputStream()));
			String res = "", tmp;
			while ((tmp = br.readLine()) != null) {
				res += tmp + "\n";
			}

			socketData.close();

			File fichier = new File(this.repertoire.getPath() + "/" + filename);
			fichier.createNewFile();
			FileOutputStream output = new FileOutputStream(fichier);
			output.write(res.getBytes());
			output.close();
		} catch (IOException e) {
			this.output.println(Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_TRANSFERT_ECHOUE);
			this.output.flush();
		}

		this.output.println(Constantes.CODE_TRANSFERT_REUSSI + " " + Constantes.MSG_TRANSFERT_REUSSI);
		this.output.flush();
	}

	public void processSYST() throws IOException {
		this.output.println(Constantes.CODE_SYST + " " + Constantes.MSG_SYST);
		this.output.flush();
	}
	
	public void processPORT(final String cmd) {
		System.out.println("CMD PORT : " + cmd);
		String[] tmpPort = cmd.split(",");
		String tmp = "";
		int port = Integer.parseInt(tmpPort[4]) * 256
				+ Integer.parseInt(tmpPort[5]);
		for (int i = 0; i < 4; i++)
			tmp += tmpPort[i] + ".";
		tmp = tmp.substring(0, tmp.length() - 1);
		try {
			socketData = new Socket(tmp, port);
			this.output.println(Constantes.CODE_SERVICE_OK);
			this.output.flush();
		} catch (Exception e) {
			this.output.println(Constantes.CODE_PARAM_INVALIDE);
			this.output.flush();
		}

	}

	public void processPWD() {
		this.output.println(Constantes.CODE_FILEOP_COMPLETED + " " + this.repertoire.getAbsolutePath());
		this.output.flush();
	}

	public void processCWD(final String chemin) throws IOException {
		Path tmpPath = Paths.get(this.repertoire.getPath() + "/" + chemin);

		if (Files.exists(tmpPath, LinkOption.NOFOLLOW_LINKS)) {
			this.repertoire = new File(tmpPath.toString());
			this.output.println(Constantes.CODE_FILEOP_COMPLETED + " " + this.repertoire.getAbsolutePath());
			this.output.flush();
		} else {
			this.output.println(Constantes.CODE_REQUEST_NO_EXECUTED + " " + Constantes.MSG_NO_SUCH_FOLDER);
			this.output.flush();
		}
	}

	public void processCDUP() throws IOException {
		processCWD("..");
	}
}
