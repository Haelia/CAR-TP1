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
import java.util.HashMap;
import java.util.Map;

public class FtpRequest extends Thread {

	public static final Map<String, Boolean> commandes;

	static {
		commandes = new HashMap<>();
		commandes.put(Constantes.CMD_USER, true);
		commandes.put(Constantes.CMD_PASS, true);
		commandes.put(Constantes.CMD_QUIT, false);
		commandes.put(Constantes.CMD_SYST, false);
		commandes.put(Constantes.CMD_PORT, true);
		commandes.put(Constantes.CMD_PASV, false);
		commandes.put(Constantes.CMD_EPRT, true);
		commandes.put(Constantes.CMD_EPSV, false);
		commandes.put(Constantes.CMD_LIST, false);
		commandes.put(Constantes.CMD_RETR, true);
		commandes.put(Constantes.CMD_STOR, true);
		commandes.put(Constantes.CMD_PWD, false);
		commandes.put(Constantes.CMD_CWD, true);
		commandes.put(Constantes.CMD_CDUP, false);
	}

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
		String request = null;
		String cmd = null;
		String param = null;

		do {
			request = null;
			// Reception d'une requête sur l'entrée du socket
			// On boucle pour éviter la reception de requête null qui peut arriver
			while (request == null) {
				request = this.input.readLine();
			}

			System.out.println("Request: " + request);

			int index = request.indexOf(" ");
			cmd = index != -1 ? request.substring(0, index) : request;
			param = index != -1 ? request.substring(index + 1) : null;

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
				cmd = null; // évite de sortir de la boucle quand la commande
							// non valide est QUIT
			}

		} while (!Constantes.CMD_QUIT.equals(cmd));
		// processRequest continue la reception de requêtes tant que la commande
		// QUIT n'est pas appelée
	}

	/**
	 * Méthode envoyant un message au client sur l'ouput du socket
	 * 
	 * @param msg
	 *            le message à envoyer
	 */
	public void sendMessage(String msg) {
		this.output.println(msg);
		this.output.flush();
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
				String msg = Constantes.CODE_NON_AUTH + " " + Constantes.MSG_NON_AUTH;
				this.sendMessage(msg);
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
		if (!commandes.containsKey(cmd)) {
			String msg = Constantes.CODE_CMD_INVALIDE + " " + Constantes.MSG_CMD_INVALIDE;
			this.sendMessage(msg);
			return false;
		}

		if (!commandes.get(cmd).equals(param)) {
			String msg = Constantes.CODE_PARAM_INVALIDE + " " + Constantes.MSG_PARAM_INVALIDE;
			this.sendMessage(msg);
			return false;
		}

		return true;
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
	public int processUSER(String user) throws IOException {
		this.authentifie = false;
		if (Serveur.utilisateurs.containsKey(user)) {
			this.identifiant = user;
			String msg = Constantes.CODE_ATTENTE_MDP + " " + Constantes.MSG_ATTENTE_MDP;
			this.sendMessage(msg);
			return Constantes.CODE_ATTENTE_MDP;
		} else {
			String msg = Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_AUTH_ECHOUE;
			this.sendMessage(msg);
			return Constantes.CODE_AUTH_ECHOUE;
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
			String msg = Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_SAISIR_IDENTIFIANT;
			this.sendMessage(msg);
		} else {
			if (Serveur.utilisateurs.get(this.identifiant).equals(mdp)) {
				this.authentifie = true;
				String msg = Constantes.CODE_AUTH_REUSSIE + " " + Constantes.MSG_AUTH_REUSSIE;
				this.sendMessage(msg);
				return true;
			} else {
				String msg = Constantes.CODE_AUTH_ECHOUE + " " + Constantes.MSG_AUTH_ECHOUE;
				this.sendMessage(msg);
			}
		}
		return false;
	}

	/**
	 * Méthode éxécutant la commande QUIT
	 * 
	 * @throws IOException
	 */
	public int processQUIT() throws IOException {
		String msg = Constantes.CODE_DECONNEXION + " " + Constantes.MSG_DECONNEXION;
		this.sendMessage(msg);
		this.socket.close();
		return Constantes.CODE_DECONNEXION;
	}

	/**
	 * Méthode éxécutant la commande SYST
	 * 
	 * @throws IOException
	 */
	public int processSYST() throws IOException {
		String msg = Constantes.CODE_SYST + " " + Constantes.MSG_SYST;
		this.sendMessage(msg);
		return Constantes.CODE_SYST;
	}

	/**
	 * Méthode éxécutant la commande PORT
	 * 
	 * @param hote
	 *            représente l'adresse et le port
	 * @throws IOException
	 */
	public int processPORT(String hote) throws IOException {
		String[] s = hote.split(",");
		int port = Integer.parseInt(s[4]) * 256 + Integer.parseInt(s[5]);

		this.socketDonnees = new Socket(this.adresse, port);
		String msg = String.valueOf(Constantes.CODE_SERVICE_OK);
		this.sendMessage(msg);
		return Constantes.CODE_SERVICE_OK;
	}

	/**
	 * Méthode éxécutant la commande PASV
	 * 
	 * @throws IOException
	 */
	public void processPASV() throws IOException {
		serveur = new ServerSocket(0);
		byte[] host = this.adresse.getAddress();
		String adresse = "";
		for (byte b : host) {
			adresse += b + ",";
		}
		int port = serveur.getLocalPort();
		String msg = "227 Entering Passive Mode " + "(" + adresse + port / 256 + "," + port % 256 + ")";
		this.sendMessage(msg);
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
		String msg = String.valueOf(Constantes.CODE_SERVICE_OK);
		this.sendMessage(msg);
	}

	/**
	 * Méthode éxécutant la commande EPSV
	 * 
	 * @throws IOException
	 */
	public void processEPSV() throws IOException {
		serveur = new ServerSocket(0);
		int port = serveur.getLocalPort();

		String msg = "229 Entering Extended Passive Mode (|||" + port + "|)";
		this.sendMessage(msg);

		this.socketDonnees = serveur.accept();
	}

	/**
	 * Méthode éxécutant la commande LIST
	 * 
	 * @throws IOException
	 */
	public boolean processLIST() throws IOException {
		if (this.socketDonnees == null) {
			String msg = Constantes.CODE_ERREUR_DONNEES + " " + Constantes.MSG_AUCUN_SOCKET_DONNEES;
			this.sendMessage(msg);
			return false;
		}

		String liste = "";
		File[] fichiers = repertoire.listFiles();

		for (File fichier : fichiers) {
			liste += fichier.getName() + "\n";
		}

		String msg = String.valueOf(Constantes.CODE_LIST);
		this.sendMessage(msg);

		OutputStreamWriter outputWriterData = new OutputStreamWriter(socketDonnees.getOutputStream());
		outputWriterData.write(liste);
		outputWriterData.flush();

		this.socketDonnees.close();

		if (this.serveur != null) {
			this.serveur.close();
			this.serveur = null;
		}

		msg = String.valueOf(Constantes.CODE_226_LIST);
		this.sendMessage(msg);
		return true;
	}

	/**
	 * Méthode éxécutant la commande RETR
	 * 
	 * @param filename
	 *            le nom du fichier envoyé au client
	 * @return vrai si la commande se déroule bien, faux sinon
	 */
	public boolean processRETR(String filename) {
		if (socketDonnees == null) {
			String msg = Constantes.CODE_ERREUR_DONNEES + " " + Constantes.MSG_AUCUN_SOCKET_DONNEES;
			this.sendMessage(msg);
			return false;
		}

		try {
			String path = this.repertoire.toPath().toAbsolutePath().toString() + "/" + filename;
			
			File f = new File(path);
			if(f.exists()) { 
				String msg = String.valueOf(Constantes.CODE_LIST);
				this.sendMessage(msg);
				
				Path target = Paths.get(path);
				OutputStream out = this.socketDonnees.getOutputStream();
				
				Files.copy(target, out);
				
				socketDonnees.close();
				
				if (this.serveur != null) {
					this.serveur.close();
					this.serveur = null;
				}
			} else {
				String msg = Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_FICHIER_INEXISTANT;
				this.sendMessage(msg);
				return false;
			}

		} catch (Exception e) {
			String msg = Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_TRANSFERT_ECHOUE;
			this.sendMessage(msg);
		}

		String msg = Constantes.CODE_TRANSFERT_REUSSI + " " + Constantes.MSG_TRANSFERT_REUSSI;
		this.sendMessage(msg);
		return true;
	}

	/**
	 * Méthode éxécutant la commande STOR
	 * 
	 * @param filename
	 *            le nom du fichier copié depuis le cient
	 * @return vrai si la commande se déroule bien, faux sinon
	 */
	public boolean processSTOR(String filename) {
		if (socketDonnees == null) {
			String msg = Constantes.CODE_ERREUR_DONNEES + " " + Constantes.MSG_AUCUN_SOCKET_DONNEES;
			this.sendMessage(msg);
			return false;
		}

		try {
			String msg = String.valueOf(Constantes.CODE_LIST);
			this.sendMessage(msg);
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
			String msg = Constantes.CODE_TRANSFERT_ECHOUE + " " + Constantes.MSG_TRANSFERT_ECHOUE;
			this.sendMessage(msg);
		}

		String msg = Constantes.CODE_TRANSFERT_REUSSI + " " + Constantes.MSG_TRANSFERT_REUSSI;
		this.sendMessage(msg);
		return true;
	}

	/**
	 * Méthode éxécutant la commande PWD
	 */
	public int processPWD() {
		String msg = Constantes.CODE_257_PWD + " " + this.repertoire.getAbsolutePath();
		this.sendMessage(msg);
		return Constantes.CODE_257_PWD;
	}

	/**
	 * Méthode éxécutant la commande CWD
	 * 
	 * @param chemin
	 *            le chemin du dossier vers lequel on souhaite naviguer
	 * @throws IOException
	 * @return le code de retour de la commande
	 */
	public int processCWD(String chemin) throws IOException {
		Path path = Paths.get(this.repertoire.getPath() + "/" + chemin);

		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			this.repertoire = new File(path.toString());
			String msg = Constantes.CODE_FILEOP_COMPLETED + " " + this.repertoire.getAbsolutePath();
			this.sendMessage(msg);
			return Constantes.CODE_FILEOP_COMPLETED;
		} else {
			String msg = Constantes.CODE_REQUEST_NO_EXECUTED + " " + Constantes.MSG_NO_SUCH_FOLDER;
			this.sendMessage(msg);
			return Constantes.CODE_REQUEST_NO_EXECUTED;
		}
	}

	/**
	 * Méthode éxécutant la commande CDUP
	 * 
	 * @throws IOException
	 * @return le code de retour de la commande
	 */
	public int processCDUP() throws IOException {
		Path path;
		if (this.repertoire.getParent() != null) {
			path = Paths.get(this.repertoire.getParent());
		} else {
			path = Paths.get(this.repertoire.getPath());
		}

		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			this.repertoire = new File(path.toString());
			String msg = Constantes.CODE_FILEOP_COMPLETED + " " + this.repertoire.getAbsolutePath();
			this.sendMessage(msg);
			return Constantes.CODE_FILEOP_COMPLETED;
		} else {
			String msg = Constantes.CODE_REQUEST_NO_EXECUTED + " " + Constantes.MSG_NO_SUCH_FOLDER;
			this.sendMessage(msg);
			return Constantes.CODE_REQUEST_NO_EXECUTED;
		}
	}
}
