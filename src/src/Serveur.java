package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Serveur extends ServerSocket {

	public static final Map<String, String> utilisateurs = new HashMap<>();
	public String repertoire;

	public Serveur(int port, String repertoire) throws IOException {
		super(port);
		this.repertoire = repertoire;
	}

	/**
	 * Ajoute un nouvel utilisateur
	 */
	public static void ajouteUtilisateur(String utilisateur, String mdp) {
		Serveur.utilisateurs.put(utilisateur, mdp);
	}

	public void start() throws IOException {
		while (true) {
			Socket socket = this.accept();

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(Constantes.CODE_CONNEXION_REUSSIE + " "
					+ Constantes.MSG_CONNEXION_REUSSIE);
			out.flush();

			System.out.println(Constantes.MSG_CONNEXION_REUSSIE);

			FtpRequest ftpRequest = new FtpRequest(socket, repertoire);
			ftpRequest.start();
		}
	}

	public static void main(String[] args) {
		try {
			Serveur.ajouteUtilisateur("azerty", "azerty");
			Serveur server = new Serveur(1234, System.getProperty("user.dir"));
			server.start();
			server.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}