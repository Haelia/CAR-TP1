package src;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

public class FTPRequestTest {

	private Serveur s;
	private FtpRequest ftp;

	/**
	 * Test si l'utilisateur est authentifie
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAuthentificationOK() throws IOException {
		s = new Serveur(1515,System.getProperty("user.dir"));
		Socket sock = new Socket("127.0.0.1", 1515);
		FtpRequest ftp = new FtpRequest(sock,System.getProperty("user.dir"));
		Serveur.ajouteUtilisateur("utilisateur", "mdp");
		ftp.processUSER("utilisateur");
		ftp.processPASS("mdp");
		assertTrue(ftp.estAuthentifie());

	}

	/**
	 * Si l'utilisateur ne donne pas le bon mot de passe, l'authentification
	 * echoue,
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAuthentificationEchoue() throws IOException {
		s = new Serveur(1516,System.getProperty("user.dir"));
		Socket sock = new Socket("127.0.0.1", 1516);
		FtpRequest ftp = new FtpRequest(sock,System.getProperty("user.dir"));
		Serveur.ajouteUtilisateur("utilisateur", "mdp");
		ftp.processUSER("utilisateur");
		ftp.processPASS("mauvaisMdp");
		assertFalse(ftp.estAuthentifie());
	}

	/**
	 * Si l'utilisateur tente de mettre son mot de passe avant son pseudo,
	 * echoue
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcessPassSansUser() throws IOException {
		s = new Serveur(1517,System.getProperty("user.dir"));
		Socket sock = new Socket("127.0.0.1", 1517);
		FtpRequest ftp = new FtpRequest(sock,System.getProperty("user.dir"));
		Serveur.ajouteUtilisateur("utilisateur", "mdp");
		assertFalse(ftp.processPASS("mdp"));
	}

}
