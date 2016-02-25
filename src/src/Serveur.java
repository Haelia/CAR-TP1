package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Serveur extends ServerSocket {

	private static final int PORT = 1234;
	private static final String REPERTOIRE = System.getProperty("user.dir");
	private static final String USER = "azerty";
	private static final String PASS = "azerty";

	public static final Map<String, String> utilisateurs = new HashMap<>();

	public String repertoire;

	public Serveur(int port, String repertoire) throws IOException {
		super(port);
		this.repertoire = repertoire;
	}

	/**
	 * Ajoute un nouvel utilisateur au serveur
	 */
	public static void ajouteUtilisateur(String utilisateur, String mdp) {
		Serveur.utilisateurs.put(utilisateur, mdp);
	}

	/**
	 * Démarre le serveur qui attend alors des connexions de clients
	 */
	public void start() throws IOException {
		System.out.println(Constantes.MSG_DEMARRAGE_REUSSIE);
		while (true) {
			Socket socket = this.accept();

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(Constantes.CODE_CONNEXION_REUSSIE + " " + Constantes.MSG_CONNEXION_REUSSIE);
			out.flush();

			System.out.println(Constantes.MSG_CONNEXION_REUSSIE);

			FtpRequest ftpRequest = new FtpRequest(socket, repertoire);
			ftpRequest.start();
		}
	}

	public static void main(String[] args) {
		try {
			Serveur.ajouteUtilisateur(USER, PASS);
			Serveur server = new Serveur(PORT, REPERTOIRE);
			server.start();
			server.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}