package src;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

public class serveurTest {

	
	@Test
	public void testAjouteUtilisateur() throws IOException {
		Serveur s = new Serveur(1515);
		s.ajouteUtilisateur("utilisateur", "mdp");
		assertTrue(s.utilisateurs.containsKey("utilisateur"));		
	}

}
