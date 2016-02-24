package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class FtpRequest extends Thread {

	private static String cmd_sans_param[] = { Constantes.CMD_QUIT, Constantes.CMD_LIST, Constantes.CMD_SYST,
			Constantes.CMD_EPSV, Constantes.CMD_PWD, Constantes.CMD_CDUP, Constantes.CMD_PASV };

	private static String cmd_avec_param[] = { Constantes.CMD_USER, Constantes.CMD_PASS, Constantes.CMD_RETR,
			Constantes.CMD_STOR, Constantes.CMD_EPRT, Constantes.CMD_PORT, Constantes.CMD_CWD, };

	/**
	 * Le socket de communication
	 */
	private Socket socket;
	/**
	 * Le socket de données
	 */
	private Socket socketDonnees;
	/**
	 * Le serveur permettant le mode passif
	 */
	private ServerSocket serveur;
	/**
	 * Input de communication sur le socket
	 */
	private BufferedReader input;
	/**
	 * Output de communication sur le socket
	 */
	private PrintWriter output;
	/**
	 * Nom d'utilisateur
	 */
	private String identifiant;
	/**
	 * Valeur à true si l'utilisateur est authentifié, à false sinon
	 */
	private boolean authentifie;
	/**
	 * Répertoire courant du serveur
	 */
	private File repertoire;
	/**
	 * Adresse à laquelle le socket communique
	 */
	private InetAddress adresse;

	/**
	 * Constructeur de FtpRequest
	 * 
	 * @param socket
	 *            le socket de communication
	 * @param repertoire
	 *            le répertoire courant du serveur
	 * @throws IOException
	 */
	public FtpRequest(Socket socket, String repertoire) throws IOException {
		super();
		this.socket = socket;
		// Initilisation de l'input sur le socket
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// Initialisation de l'output sur le socket
		this.output = new PrintWriter(socket.getOutputStream(), true);
		this.identifiant = "";
		this.authentifie = false;
		this.repertoire = new File(repertoire);
		// Initialisation de l'adresse à celle du socket
		this.adresse = socket.getInetAddress();
	}

	/**
	 * Méthode utilisée par start() au lancement du thread de FtpRequest
	 */
	@Override
	public void run() {
		try {
			processRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode gérant les requêtes en les receptionnant et en lançant les
	 * commandes correspondantes. La méthode continue la reception de requêtes
	 * tant que la commande QUIT n'est pas appelée
	 * 
	 * @throws IOException
	 */
	public void processRequest() throws IOException {
		String cmd = null;

		do {
			// Reception d'une requête sur l'entrée du socket
			String request = this.input.readLine();

			System.out.println("Request: " + request);

			int index = request.indexOf(" ");
			cmd = index != -1 ? request.substring(0, index) : request;
			String param = index != -1 ? request.substring(index + 1) : null;

			// Vérification de la validité de la commande : Elle doit exister et
			// avoir un nombre de paramêtre correcte. L'utilisateur doit aussi
			// être authentifié si la commande le requiert
			if (estValideCommande(cmd, param != null)) {
				// Switch appelant la méthode correspondant à la commande
				switch (cmd) {
				case Constantes.CMD_USER:
					processUSER(param);
					break;

				case Constantes.CMD_PASS:
					processPASS(param);
					break;

				case Constantes.CMD_QUIT:
					processQUIT();
					break;

				case Constantes.CMD_SYST:
					processSYST();
					break;

				case Constantes.CMD_PORT:
					processPORT(param);
					break;

				case Constantes.CMD_PASV:
					processPASV();
					break;

				case Constantes.CMD_EPRT:
					processEPRT(param);
					break;

				case Constantes.CMD_EPSV:
					processEPSV();
					break;

				case Constantes.CMD_LIST:
					processLIST();
					break;

				case Constantes.CMD_RETR:
					processRETR(param);
					break;

				case Constantes.CMD_STOR:
					processSTOR(param);
					break;

				case Constantes.CMD_PWD:
					processPWD();
					break;

				case Constantes.CMD_CWD:
					processCWD(param);
					break;

				case Constantes.CMD_CDUP:
					processCDUP();
					break;
				}
			} else {
				cmd = null;
			}

		} while (!Constantes.CMD_QUIT.equals(cmd));
		// processRequest continue la reception de requêtes tant que la commande
		// QUIT n'est pas appelée
	}

	/**
	 * Méthode Vérifiant la validité d'une commande
	 * 
	 * @param cmd
	 *            la commande à vérifier
	 * @param param
	 *            true si la commande est parametrée, false sinon
	 * @return true si la commande est valide, false sinon
	 */
	public boolean estValideCommande(String cmd, boolean param) {
		if (!estValideParametre(cmd, param)) {
			return false;
		}
		if (requiertAuthenfication(cmd)) {
			if (!this.estAuthentifie()) {
				this.output.println(Constantes.CODE_NON_AUTH + " " + Constantes.MSG_NON_AUTH);
				this.output.flush();
				return false;
			}
		}
		return true;
	}

	/**
	 * Méthode vérifiant la validité des paramètres en fonction de leur nombre
	 * 
	 * @param cmd
	 *            la commande à vérifier
	 * @param param
	 *            true si la commande est parametrée, false sinon
	 * @return true si le nombre de paramètre est valide, false sinon
	 */
	public boolean estValideParametre(String cmd, boolean param) {
		if (param && Arrays.asList(FtpRequest.cmd_avec_param).contains(cmd)) {
			return true;

		} else if (!param && Arrays.asList(FtpRequest.cmd_sans_param).contains(cmd)) {
			return true;
		}
		this.output.println(Constantes.CODE_PARAM_INVALIDE + " " + Constantes.MSG_PARAM_INVALIDE);
		this.output.flush();
		return false;
	}

	/**
	 * Méthode vérifiant si une commande requiert une authentification pour être
	 * utilisée
	 * 
	 * @param cmd
	 *            la commande à vérifier
	 * @return true si la commande requiert une authentification, false sinon
	 */
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

	/**
	 * Accesseur à l'attribut authentifie
	 * 
	 * @return true si l'utilisateur est authentifié, false sinon
	 */
	public boolean estAuthentifie() {
		return this.authentifie;
	}

	/**
	 * Méthode éxécutant la commande USER
	 * 
	 * @param user
	 *            l'identifiant de l'utilisateur
	 * @throws IOException
	 */
	public void processUSER(String user) throws IOException {
		this.authentifie = false;
		if (Serveur.utilisateurs.containsKey(user)) {
			this.identifiant = user;
			this.output.println(Constantes.CODE_ATTENTE_MDP + " " + Constantes.MSG_ATTENTE_MDP);
			this.output.flush();
		} else {
			this.output.println(Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_AUTH_ECHOUE);
			this.output.flush();
		}

	}

	/**
	 * Méthode éxécutant la commande PASS
	 * 
	 * @param mdp
	 *            le mot de passe de l'utilisateur
	 * @return vrai si l'authenfication s'est déroulée correctement, false sinon
	 */
	public boolean processPASS(String mdp) {
		if (identifiant == "") {
			this.output.println(Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_SAISIR_IDENTIFIANT);
			this.output.flush();
		} else {
			if (Serveur.utilisateurs.get(this.identifiant).equals(mdp)) {
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

	/**
	 * Méthode éxécutant la commande QUIT
	 * 
	 * @throws IOException
	 */
	public void processQUIT() throws IOException {
		this.output.println(Constantes.CODE_DECONNEXION + " " + Constantes.MSG_DECONNEXION);
		this.output.flush();
		this.socket.close();
	}

	/**
	 * Méthode éxécutant la commande SYST
	 * 
	 * @throws IOException
	 */
	public void processSYST() throws IOException {
		this.output.println(Constantes.CODE_SYST + " " + Constantes.MSG_SYST);
		this.output.flush();
	}

	/**
	 * Méthode éxécutant la commande PORT
	 * 
	 * @param hote
	 *            représente l'adresse et le port
	 * @throws IOException
	 */
	private void processPORT(String hote) throws IOException {
		String[] s = hote.split(",");
		int port = Integer.parseInt(s[4]) * 256 + Integer.parseInt(s[5]);

		this.socketDonnees = new Socket(this.adresse, port);
		this.output.println(Constantes.CODE_SERVICE_OK);
		this.output.flush();
	}

	/**
	 * Méthode éxécutant la commande PASV
	 * 
	 * @throws IOException
	 */
	private void processPASV() throws IOException {
		serveur = new ServerSocket(0);
		byte[] host = this.adresse.getAddress();
		String adresse = "";
		for (byte b : host) {
			adresse += b + ",";
		}
		int port = serveur.getLocalPort();
		this.output.println("227 Entering Passive Mode " + "(" + adresse + port / 256 + "," + port % 256 + ")");
		this.output.flush();
		this.socketDonnees = serveur.accept();
	}

	/**
	 * Méthode éxécutant la commande EPRT
	 * 
	 * @param hote
	 *            représente l'adresse et le port
	 * @throws IOException
	 */
	public void processEPRT(String hote) throws IOException {
		String[] s = hote.split("[|]");
		int port = Integer.parseInt(s[3]);

		this.socketDonnees = new Socket(this.adresse, port);
		this.output.println(Constantes.CODE_SERVICE_OK);
		this.output.flush();
	}

	/**
	 * Méthode éxécutant la commande EPSV
	 * 
	 * @throws IOException
	 */
	private void processEPSV() throws IOException {
		serveur = new ServerSocket(0);
		int port = serveur.getLocalPort();

		this.output.println("229 Entering Extended Passive Mode (|||" + port + "|)");
		this.output.flush();
		this.socketDonnees = serveur.accept();
	}

	/**
	 * Méthode éxécutant la commande LIST
	 * 
	 * @throws IOException
	 */
	public boolean processLIST() throws IOException {
		if (socketDonnees == null) {
			this.output.println(Constantes.CODE_ERREUR_DONNEES + " " + Constantes.MSG_AUCUN_SOCKET_DONNEES);
			this.output.flush();
			return false;
		}

		String liste = "";
		File[] fichiers = repertoire.listFiles();

		for (File fichier : fichiers) {
			liste += fichier.getName() + "\n";
		}

		this.output.println(Constantes.CODE_LIST);
		this.output.flush();

		OutputStreamWriter outputWriterData = new OutputStreamWriter(socketDonnees.getOutputStream());
		outputWriterData.write(liste);
		outputWriterData.flush();

		this.socketDonnees.close();

		if (this.serveur != null) {
			this.serveur.close();
			this.serveur = null;
		}

		this.output.println(Constantes.CODE_226_LIST);
		this.output.flush();
		return true;
	}

	/**
	 * Méthode éxécutant la commande RETR
	 * 
	 * @param filename
	 *            le nom du fichier envoyé au client
	 * @return
	 */
	public boolean processRETR(String filename) {
		if (socketDonnees == null) {
			this.output.println(Constantes.CODE_ERREUR_DONNEES + " " + Constantes.MSG_AUCUN_SOCKET_DONNEES);
			this.output.flush();
			return false;
		}

		try {
			this.output.println(Constantes.CODE_LIST);
			this.output.flush();
			String path = this.repertoire.toPath().toAbsolutePath().toString() + "/" + filename;
			Path target = Paths.get(path);

			OutputStream out = this.socketDonnees.getOutputStream();

			Files.copy(target, out);

			socketDonnees.close();

			if (this.serveur != null) {
				this.serveur.close();
				this.serveur = null;
			}
		} catch (Exception e) {
			this.output.println(Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_TRANSFERT_ECHOUE);
			this.output.flush();
		}

		this.output.println(Constantes.CODE_TRANSFERT_REUSSI + " " + Constantes.MSG_TRANSFERT_REUSSI);
		this.output.flush();
		return true;
	}

	/**
	 * Méthode éxécutant la commande STOR
	 * 
	 * @param filename
	 *            le nom du fichier copié depuis le cient
	 * @return
	 */
	public boolean processSTOR(String filename) {
		if (socketDonnees == null) {
			this.output.println(Constantes.CODE_ERREUR_DONNEES + " " + Constantes.MSG_AUCUN_SOCKET_DONNEES);
			this.output.flush();
			return false;
		}

		try {
			this.output.println(Constantes.CODE_LIST);
			this.output.flush();
			InputStream in = this.socketDonnees.getInputStream();

			String path = this.repertoire.toPath().toAbsolutePath().toString() + "/" + filename;
			Path target = Paths.get(path);

			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);

			socketDonnees.close();

			if (this.serveur != null) {
				this.serveur.close();
				this.serveur = null;
			}
		} catch (IOException e) {
			this.output.println(Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_TRANSFERT_ECHOUE);
			this.output.flush();
		}

		this.output.println(Constantes.CODE_TRANSFERT_REUSSI + " " + Constantes.MSG_TRANSFERT_REUSSI);
		this.output.flush();
		return true;
	}

	/**
	 * Méthode éxécutant la commande PWD
	 */
	public void processPWD() {
		this.output.println(Constantes.CODE_257_PWD + " " + this.repertoire.getAbsolutePath());
		this.output.flush();
	}

	/**
	 * Méthode éxécutant la commande CWD
	 * 
	 * @param chemin
	 *            le chemin du dossier vers lequel on souhaite naviguer
	 * @throws IOException
	 */
	public void processCWD(String chemin) throws IOException {
		Path path = Paths.get(this.repertoire.getPath() + "/" + chemin);

		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			this.repertoire = new File(path.toString());
			this.output.println(Constantes.CODE_FILEOP_COMPLETED + " " + this.repertoire.getAbsolutePath());
			this.output.flush();
		} else {
			this.output.println(Constantes.CODE_REQUEST_NO_EXECUTED + " " + Constantes.MSG_NO_SUCH_FOLDER);
			this.output.flush();
		}
	}

	/**
	 * Méthode éxécutant la commande CDUP
	 * 
	 * @throws IOException
	 */
	public void processCDUP() throws IOException {
		Path path;
		if (this.repertoire.getParent() != null) {
			path = Paths.get(this.repertoire.getParent());
		} else {
			path = Paths.get(this.repertoire.getPath());
		}

		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			this.repertoire = new File(path.toString());
			this.output.println(Constantes.CODE_FILEOP_COMPLETED + " " + this.repertoire.getAbsolutePath());
			this.output.flush();
		} else {
			this.output.println(Constantes.CODE_REQUEST_NO_EXECUTED + " " + Constantes.MSG_NO_SUCH_FOLDER);
			this.output.flush();
		}
	}
}
