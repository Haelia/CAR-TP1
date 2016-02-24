package src;

public class Constantes {

	// Commandes
	public static final String CMD_USER = "USER";
	public static final String CMD_PASS = "PASS";
	public static final String CMD_QUIT = "QUIT";
	public static final String CMD_SYST = "SYST";
	public static final String CMD_PORT = "PORT";
	public static final String CMD_PASV = "PASV";
	public static final String CMD_EPRT = "EPRT";
	public static final String CMD_EPSV = "EPSV";
	public static final String CMD_LIST = "LIST";
	public static final String CMD_RETR = "RETR";
	public static final String CMD_STOR = "STOR";
	public static final String CMD_PWD = "PWD";
	public static final String CMD_CWD = "CWD";
	public static final String CMD_CDUP = "CDUP";

	// Codes de retour
	public static final int CODE_DECONNEXION = 221;
	public static final int CODE_INFO_SYST = 215;
	public static final int CODE_NON_AUTH = 530;
	public static final int CODE_CONNEXION_REUSSIE = 220;
	public static final int CODE_AUTH_REUSSIE = 230;
	public static final int CODE_ATTENTE_MDP = 331;
	public static final int CODE_AUTH_ECHOUE = 430;
	public static final int CODE_CMD_INVALIDE = 500;
	public static final int CODE_PARAM_INVALIDE = 501;
	public static final int CODE_TRANSFERT_REUSSI = 226;
	public static final int CODE_TRANSFERT_ECHOUE = 425;
	public static final int CODE_SYST = 215;
	public static final int CODE_257_PWD = 257;
	public static final int CODE_REQUEST_NO_EXECUTED = 530;
	public static final int CODE_SERVICE_OK = 200;
	public static final int CODE_LIST = 150;
	public static final int CODE_226_LIST = 226;
	public static final int CODE_FILEOP_COMPLETED = 250;

	// Messages

	public static final String MSG_ATTENTE_MDP = "En attente du mot de passe";
	public static final String MSG_SAISIR_IDENTIFIANT = "Veuillez saisir votre identifiant d'abord";
	public static final String MSG_NON_AUTH = "Vous devez etre authentifie pour effectuer cette action";
	public static final String MSG_AUTH_ECHOUE = "L'authentification a echoue";
	public static final String MSG_CONNEXION_REUSSIE = "Connexion etablie";
	public static final String MSG_DECONNEXION = "Deconnexion";
	public static final String MSG_AUTH_REUSSIE = "Authentification reussie";
	public static final String MSG_CMD_INVALIDE = "Commande non reconnue";
	public static final String MSG_PARAM_INVALIDE = "Parametres invalides";
	public static final String MSG_TRANSFERT_REUSSI = "Transfert reussi";
	public static final String MSG_TRANSFERT_ECHOUE = "Transfert echoue";
	public static final String MSG_SYST = "Serveur FTP Java";
	public static final String MSG_NO_SUCH_FOLDER = "Repertoire inexistant";
}
