package src;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class serveurTest {
	
	/**
	 * Test que l'objet serveur est bien cree.
	 * @throws IOException
	 */
	@Test
	public void testCreerServeur() throws IOException{
		Serveur s = new Serveur(1516,System.getProperty("user.dir"));
		assertNotNull(s);
	}

	/**
	 * Verifie que le nouvel utilisateur (utilisateur + mot de passe) est bien integre a la hashmap.
	 * @throws IOException
	 */
	@Test
	public void testAjouteUtilisateur() throws IOException {
		Serveur s = new Serveur(1515,System.getProperty("user.dir"));
		s.ajouteUtilisateur("utilisateur", "mdp");
		assertTrue(s.utilisateurs.containsKey("utilisateur"));
	}
}
