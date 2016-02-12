package src;

public class Constantes {

	//Commandes
	public static final String CMD_USER = "USER";
	public static final String CMD_PASS = "PASS";
	public static final String CMD_RETR = "RETR";
	public static final String CMD_STOR = "STOR";
	public static final String CMD_LIST = "LIST";
	public static final String CMD_QUIT = "QUIT";
	public static final String CMD_SYST = "SYST";

	//Codes de retour
	public static final int CODE_INFO_SYST = 215;
	public static final int CODE_CONNEXION_REUSSIE = 220;
	public static final int CODE_TRANSFERT_REUSSI = 226;
	public static final int CODE_DECONNEXION = 221;
	public static final int CODE_AUTH_REUSSIE = 230;
	//public static final int CODE_FILEOP_COMPLETED = 250;
	public static final int CODE_ATTENTE_MDP = 331;
	public static final int CODE_AUTH_ECHOUE = 430;
	public static final int CODE_CMD_INVALIDE = 500;
	public static final int CODE_PARAM_INVALIDE = 501;
	public static final int CODE_NON_AUTH = 530;
	
	//Messages
	
	public static final String MSG_ATTENTE_MDP = "En attente du mot de passe";
	public static final String MSG_SAISIR_IDENTIFIANT = "Veuillez saisir votre identifiant d'abord";
	public static final String MSG_NON_AUTH = "Vous devez etre authentifie pour effectuer cette action";
	public static final String MSG_AUTH_ECHOUE = "L'authentification a echoue";
	public static final String MSG_CONNEXION_REUSSIE = "Connexion etablie";
	public static final String MSG_AUTH_REUSSIE = "Authentification reussie";
}
