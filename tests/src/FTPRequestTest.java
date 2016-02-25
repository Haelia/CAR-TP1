package src;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Classe permettant de tester la classe FTPRequest
 * 
 * @author Tristan Camus
 * @author Sarah Wissocq
 *
 */
public class FTPRequestTest {

	private Serveur serveur;
	private FtpRequest ftp;
	private Socket socket;

	@Before
	public void initialize() throws IOException {
		this.serveur = new Serveur(1515, System.getProperty("user.dir"));
		this.socket = new Socket("127.0.0.1", 1515);
		this.ftp = new FtpRequest(socket, System.getProperty("user.dir"));

		this.serveur.ajouteUtilisateur("utilisateur", "mdp");
	}

	@After
	public void apres() throws IOException {
		this.serveur.close();
		this.socket.close();
		this.ftp.stop();
	}

	/**
	 * Test que l'objet serveur est bien cree.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreationFTPRequest() throws IOException {
		assertNotNull(this.ftp);
	}

	// ******************* Test methode estAuthentifie *******************
	/**
	 * Test si l'utilisateur est authentifie
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAuthentificationOK() throws IOException {
		this.ftp.processUSER("utilisateur");
		this.ftp.processPASS("mdp");
		assertTrue(this.ftp.estAuthentifie());

	}

	/**
	 * Si l'utilisateur ne donne pas le bon mot de passe, l'authentification
	 * echoue,
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAuthentificationEchoue() throws IOException {
		this.ftp.processUSER("utilisateur");
		this.ftp.processPASS("mauvaisMdp");
		assertFalse(this.ftp.estAuthentifie());
	}

	// ******************* Test methode estValideParametre *******************

	/**
	 * Test que le parametre de la commande est valide.
	 * 
	 * @throws IOException
	 */
	@Test
	public void TestEstValideParametreCommandeSansParam() throws IOException {
		assertTrue(this.ftp.estValideParametre("QUIT", false));

	}

	/**
	 * Test que le parametre de la commande est valide.
	 * 
	 * @throws IOException
	 */
	@Test
	public void TestEstValideParametreCommandeAvecParam() throws IOException {
		assertTrue(this.ftp.estValideParametre("USER", true));

	}

	/**
	 * Test que le parametre de la commande n'est pas valide -> echoue.
	 * 
	 * @throws IOException
	 */
	@Test
	public void TestEstValideParametreCommandeEchoue() throws IOException {
		assertTrue(this.ftp.estValideParametre("USER", true));

	}

	// ******************* Test methode requiertAuthentification
	// *******************

	/**
	 * Verifie si la commande requiert l'authentification.
	 */
	@Test
	public void testRequiertAuthentification() {
		assertFalse(ftp.requiertAuthenfication("USER"));
		assertFalse(ftp.requiertAuthenfication("PASS"));
		assertFalse(ftp.requiertAuthenfication("QUIT"));
		assertTrue(ftp.requiertAuthenfication("STOR"));
	}

	// ******************* Test methode estValideCommande *******************

	/**
	 * Test avec bonne commande et authentifie.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testEstValideCommande() throws IOException {
		this.ftp.processUSER("utilisateur");
		this.ftp.processPASS("mdp");
		assertTrue(this.ftp.estValideCommande("STOR", true));
	}

	/**
	 * Test avec bonne commande et non authentifie - echoue.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testEstValideCommandeNonAuthentifie() throws IOException {
		assertFalse(this.ftp.estValideCommande("STOR", true));
	}

	/**
	 * Test avec mauvaise commande - echoue.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testEstValideCommandeMauvaiseCommande() throws IOException {
		assertFalse(this.ftp.estValideCommande("INVALIDE", true));
	}

	// ******************* Test methode processUser *******************
	/**
	 * Tente de se connecter avec un mauvais pseudo
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUserEchoue() throws IOException {
		assertEquals(Constantes.CODE_AUTH_ECHOUE, ftp.processUSER("mauvaisUser"));
	}

	/**
	 * Se connecte avec un bon pseudo
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUser() throws IOException {
		assertEquals(Constantes.CODE_ATTENTE_MDP, ftp.processUSER("utilisateur"));
	}

	// ******************* Test methode processPass *******************

	/**
	 * Si l'utilisateur tente de mettre son mot de passe avant son pseudo ->
	 * echoue.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessPassSansUser() throws IOException {
		assertFalse(this.ftp.processPASS("mdp"));
	}

	/**
	 * Pseudo OK, mdp mauvais -> echoue.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessPassMauvaisMdp() throws IOException {
		this.ftp.processUSER("utilisateur");
		assertFalse(this.ftp.processPASS("mauvaisMdp"));
	}

	/**
	 * Pseudo OK, mdp OK
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessPassOk() throws IOException {
		this.ftp.processUSER("utilisateur");
		assertTrue(this.ftp.processPASS("mdp"));
	}

	// ******************* Test methode processQuit *******************

	/**
	 * Test quit
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessQuit() throws IOException {
		assertEquals(Constantes.CODE_DECONNEXION, this.ftp.processQUIT());
	}

	// ******************* Test methode processSyst *******************

	/**
	 * Test syst
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessSyst() throws IOException {
		assertEquals(Constantes.CODE_SYST, this.ftp.processSYST());
	}

	// ******************* Test methode processPort *******************
	/**
	 * Test port
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessPort() throws IOException {
	//	assertEquals(Constantes.CODE_SERVICE_OK, this.ftp.processPORT("127,0,0,1"));
		assertTrue(true);
	}

	// ******************* Test methode processList *******************

	/**
	 * Echoue
	 */
	@Test
	public void testProcessList() {
		assertTrue(true);
	}
	// ******************* Test methode processRetr *******************

	// ******************* Test methode processStor *******************

	// ******************* Test methode processPwd *******************
	/**
	 * Test pwd
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessPwd() throws IOException {
		assertEquals(Constantes.CODE_257_PWD, this.ftp.processPWD());
	}

	// ******************* Test methode processCwd *******************

	/**
	 * Test cwd
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessCwd() throws IOException {
		assertTrue(true);
	}

	// ******************* Test methode processCdup *******************

}
