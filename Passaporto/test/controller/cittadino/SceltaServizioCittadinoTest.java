package controller.cittadino;

import controller.UtenteLog;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ModelDisponibilita;
import model.ModelPrenotazione;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class SceltaServizioCittadinoTest {
	private final static String domani = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
	@Start
	private void start(Stage stage) throws IOException {
		UtenteLog.getUtente("TEST");
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/cittadino/scelta-servizio-cittadino.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 700, 700);;
		stage.setTitle("Scegli servizio");
		stage.setScene(scene);
		stage.show();
		stage.toFront();
	}

	@Test
	/**
	 * Metodo che testa la ricerca con filtri vuoti.
	 */
	void testFiltriVuoti(FxRobot robot) {
		//Avvio la ricerca senza filtri
		robot.clickOn("#avviaRicerca");
		//Controllo l'output
		String output = robot.lookup("#erroreFiltro").queryAs(Label.class).getText();
		assertEquals("Ricerca effettuata con successo.", output);
	}

	@Test
	/**
	 * Metodo che testa la ricerca con filtri non vuoti.
	 */
	void testFiltriNonVuoti(FxRobot robot) {
		//Inserisco come filtro un luogo, una data, un orario e una richiesta
		robot.lookup("#filtroLuogo").queryAs(TextField.class).setText("");
		robot.clickOn("#filtroLuogo").write("Roma");
		robot.lookup("#filtroData").queryAs(DatePicker.class).setValue(LocalDate.now().plusDays(2));
		robot.clickOn("#filtroOrario").clickOn("08:00-09:00").clickOn("12:00-13:00");
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		//Avvio la ricerca
		robot.clickOn("#avviaRicerca");
		//Controllo l'output
		String output = robot.lookup("#erroreFiltro").queryAs(Label.class).getText();
		assertEquals("Ricerca effettuata con successo.", output);
	}

	@Test
	/**
	 * Metodo che testa il filtro con data vecchia.
	 */
	void testDataVecchia(FxRobot robot) {
		//Inserisco come filtro una data già passata
		robot.lookup("#filtroData").queryAs(DatePicker.class).setValue(LocalDate.now().minusDays(2));
		robot.clickOn("#avviaRicerca");
		//Controllo l'output
		String output = robot.lookup("#erroreFiltro").queryAs(Label.class).getText();
		assertEquals("Data non valida. Inserire una data successiva a quella odierna.", output);
	}

	@Test
	/**
	 * Metodo che testa la prenotazione.
	 */
	void testPrenotazione(FxRobot robot) throws SQLException {
		//Ottengo l'istanza del modello e inserisco una disponibilità
		ModelDisponibilita modelDisponibilita = ModelDisponibilita.getIstanza();
		modelDisponibilita.inserisciDisponibilità("TEST","Rilascio prima volta", domani, "08:00-09:00", "VERONA");
		int idDisponibilita = modelDisponibilita.ottieniId("TEST","Rilascio prima volta", domani, "08:00-09:00", "VERONA");
		//Avvio una ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili").clickOn("Rilascio prima volta - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Elimino la prenotazione
		eliminaPrenotazione(idDisponibilita);
		//Elimino la disponibilità in caso fosse l'unica
		eliminaDisp(idDisponibilita, false);
		//Controllo l'output
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Prenotazione effettuata con successo. Controlla il tab 'Prenotazioni' per maggiori informazioni.", output);
	}

	@Test
	/**
	 * Metodo che testa la prenotazione già occupata.
	 */
	void testPrenotazioneOccupata(FxRobot robot) throws SQLException {
		//Inserisco una disponibilità
		int idDisponibilita = inserisciDisp("Rilascio prima volta", "VERONA");
		//Ottengo il numero di occorrenze della disponibilità appena inserita
		String query = "SELECT * FROM disponibilita WHERE id = '" + idDisponibilita + "'";
		ModelDisponibilita modelDisponibilita = ModelDisponibilita.getIstanza();
		ResultSet rs = modelDisponibilita.lanciaQuery(query);
		//In occorrenze avrò il numero di occorrenze della disponibilità appena inserita eccetto l'ultima
		int occorrenze = rs.getInt("occorrenze")-1;
		String addetto = rs.getString("addetto");
		//Elimino le disponibilità precedenti
		for(int i = 0; i < occorrenze; i++) {
			modelDisponibilita.elimina(Integer.toString(idDisponibilita), "id");
		}
		rs.close();
		modelDisponibilita.chiudiConnessione();
		//Avvio il mio test
		//Cerco la ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili").clickOn("Rilascio prima volta - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Effettuo nuovamente la ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		robot.clickOn("#avviaRicerca");
		//Mi assicuro che la disponibilità si trovi in quelle occupate
		robot.clickOn("#tabOccupati").clickOn("#elencoOccupati");
		//Elimino la prenotazione
		eliminaPrenotazione(idDisponibilita);
		//Ripristino le occorrenze della disponibilità
		for (int i = 0; i < occorrenze; i++) {
			modelDisponibilita.inserisciDisponibilità(addetto,"Rilascio prima volta", domani, "08:00-09:00", "VERONA");
		}
		modelDisponibilita.chiudiConnessione();
		//Elimino la disponibilità in caso fosse unica
		eliminaDisp(idDisponibilita, false);
		//Verifico che l'output sia quello atteso
		assertNotNull(robot.clickOn("Rilascio prima volta - VERONA - " + domani + " - 08:00-09:00"));
	}

	@Test
	/**
	 * Metodo che testa le prenotazioni da inserire, di un giorno dopo i 2 mesi.
	 */
	void testPrenotazioniDaInserireDopoMese(FxRobot robot){
		//Mi sposto nel tab da inserire
		robot.clickOn("#tabDaInserire");
		//Inserisco la data 2 mesi e 1 giorno dopo
		robot.lookup("#trovaGiorno").queryAs(DatePicker.class).setValue(LocalDate.now().plusMonths(2).plusDays(1));
		robot.clickOn("#sottomettiRichiestaInformazioni");
		String output = robot.lookup("#disponibileDa").queryAs(Label.class).getText();
		assertEquals(domani, output);
	}

	@Test
	/**
	 * Metodo che testa le prenotazioni da inserire, di un giorno passato.
	 */
	void testPrenotazioniDaInserirePassato(FxRobot robot){
		//Mi sposto nel tab da inserire
		robot.clickOn("#tabDaInserire");
		//Inserisco la data d'ieri e verifico che l'output sia quello atteso
		robot.lookup("#trovaGiorno").queryAs(DatePicker.class).setValue(LocalDate.now().minusDays(1));
		robot.clickOn("#sottomettiRichiestaInformazioni");
		String output = robot.lookup("#disponibileMessaggio").queryAs(Label.class).getText();
		assertEquals("Possiamo informarti solamente per domani e i giorni successivi.", output);
	}

	@Test
	/**
	 * Metodo che testa le prenotazioni da inserire vuota.
	 */
	void testPrenotazioniDaInserireVuota(FxRobot robot){
		//Mi sposto nel tab da inserire
		robot.clickOn("#tabDaInserire");
		//Sottometto la richiesta senza inserire giorni e verifico che l'output sia quello atteso
		robot.clickOn("#sottomettiRichiestaInformazioni");
		String output = robot.lookup("#disponibileMessaggio").queryAs(Label.class).getText();
		assertEquals("Inserire il giorno, da valutare.", output);
	}

	@Test
	/**
	 * Metodo che testa le prenotazioni da inserire, di un giorno prima i 2 mesi.
	 */
	void testPrenotazioniDaInserirePrimaMesi(FxRobot robot){
		//Mi sposto nel tab da inserire
		robot.clickOn("#tabDaInserire");
		//Inserisco la data di 13 giorni dopo
		robot.lookup("#trovaGiorno").queryAs(DatePicker.class).setValue(LocalDate.now().plusDays(13));
		robot.clickOn("#sottomettiRichiestaInformazioni");
		//Controllo che il messaggio sia quello aspettato
		String output = robot.lookup("#disponibileDa").queryAs(Label.class).getText();
		assertEquals("quando un addetto inserirà la sua disponibilità.", output);
	}


	@Test
	/**
	 * Metodo che testa il filtro or.
	 */
	void testFiltroOr(FxRobot robot) throws SQLException {
		//Inserisco le due disponibilità di test
		int idVerona = inserisciDisp("Ritiro", "VERONA");
		int idRoma = inserisciDisp("Rilascio prima volta", "ROMA");
		//Effettuo la ricerca con i filtri appositi
		robot.lookup("#filtroLuogo").queryAs(TextField.class).setText("");
		robot.clickOn("#filtroLuogo").write("Verona");
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		robot.clickOn("#avviaRicerca");
		//Elimino le disponibilità di test
		eliminaDisp(idVerona, true);
		eliminaDisp(idRoma, true);
		//Mi assicuro che la disponibilità si trovi in quelle disponibili
		robot.clickOn("#elencoDisponibili");
		assertNotNull(robot.clickOn("Rilascio prima volta - ROMA - " + domani + " - 08:00-09:00"));
		robot.clickOn("#elencoDisponibili");
		assertNotNull(robot.clickOn("Ritiro - VERONA - " + domani + " - 08:00-09:00"));
	}

	@Test
	/**
	 * Metodo che testa la richiesta di un ritiro senza aver fatto un rilascio.
	 */
	void ritiroSenzaRilascio(FxRobot robot) throws SQLException {
		//Inserisco la disponibilità di test
		int idDisponibilita = inserisciDisp("Ritiro", "VERONA");
		//Avvio la ricerca
		robot.clickOn("#filtroOrario").clickOn("08:00-09:00").clickOn("#logUtente");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Ritiro - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Cancello la prenotazione
		eliminaPrenotazione(idDisponibilita);
		//Se era l'unica disponibilità, la elimino
		eliminaDisp(idDisponibilita, false);
		//Verifico l'output.
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Non hai chiesto il rilascio, impossibile chiedere il ritiro.", output);
	}

	@Test
	/**
	 * Metodo che testa la richiesta di un rilascio senza aver fatto un ritiro.
	 */
	void rilascioSenzaRitiro(FxRobot robot) throws SQLException {
		robot.clickOn("#filtroAndOr");
		//Inserisco la disponibilità di test
		int idDisponibilita = inserisciDisp("Rilascio furto", "ROMA");
		//Avvio la ricerca
		robot.clickOn("#filtroOrario").clickOn("08:00-09:00").clickOn("#logUtente");
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio furto");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Rilascio furto - ROMA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Inserisco la disponibilità di test
		inserisciDisp("Rilascio furto", "Roma");
		//Avvio la ricerca
		robot.clickOn("#filtroOrario").clickOn("08:00-09:00").clickOn("#logUtente");
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio furto");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Rilascio furto - ROMA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Cancello le prenotazioni
		eliminaPrenotazione(idDisponibilita);
		//Se era l'unica disponibilità, la elimino
		eliminaDisp(idDisponibilita, false);
		//Verifico l'output.
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Non hai ancora fatto tutti i ritiri.", output);
	}

	@Test
	/**
	 * Metodo che testa la richiesta di un ritiro senza che sia passato un mese.
	 */
	void ritiroMeseNonPassato(FxRobot robot) throws SQLException {
		robot.clickOn("#filtroAndOr");
		//Inserisco la disponibilità di test
		int idRilascio = inserisciDisp("Rilascio prima volta", "VERONA");
		//Avvio la ricerca
		robot.clickOn("#filtroOrario").clickOn("08:00-09:00").clickOn("#logUtente");
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Rilascio prima volta - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Inserisco la disponibilità di test
		int idRitiro = inserisciDisp("Ritiro", "Verona");
		//Avvio la ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Ritiro");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Ritiro - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Cancello le prenotazioni
		eliminaPrenotazione(idRilascio);
		eliminaPrenotazione(idRitiro);
		//Se erano le uniche disponibilità le elimino
		eliminaDisp(idRilascio, false);
		eliminaDisp(idRitiro, false);
		//Verifico l'output.
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Non è passato un mese dall'ultimo rilascio.", output);
	}

	@Test
	/**
	 * Metodo che testa la dichiarazione troppo lunga.
	 */
	void dichiarazioneTroppoLunga(FxRobot robot){
		robot.clickOn("#dichiarazioniExtra");
		for(int i = 0; i < 26; i++)
			robot.write("PRovaOIIJm");
		robot.clickOn("#prenotazione");
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Dichiarazioni extra troppo lunghe.", output);
	}

	@Test
	/**
	 * Metodo che testa la richiesta di un ritiro dopo che sia passato un mese.
	 */
	void ritiroMesePassato(FxRobot robot) throws SQLException {
		robot.clickOn("#filtroAndOr");
		//Inserisco la disponibilità di test
		int idRilascio = inserisciDisp("Rilascio prima volta", "VERONA");
		//Avvio la ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio prima volta");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Rilascio prima volta - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Inserisco la disponibilità di test
		ModelDisponibilita modelDisponibilita = ModelDisponibilita.getIstanza();
		String secondaData = LocalDate.now().plusMonths(1).plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		modelDisponibilita.inserisciDisponibilità("TEST","Ritiro", secondaData, "08:00-09:00", "VERONA");
		//Ottengo l'id
		int idRitiro = modelDisponibilita.ottieniId("TEST","Ritiro", secondaData, "08:00-09:00", "VERONA");
		modelDisponibilita.chiudiConnessione();

		//Avvio la ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Ritiro");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Ritiro - VERONA - " + secondaData + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Cancello le prenotazioni
		eliminaPrenotazione(idRilascio);
		eliminaPrenotazione(idRitiro);
		//Se erano le uniche disponibilità le elimino
		eliminaDisp(idRilascio, false);
		eliminaDisp(idRitiro, false);
		//Verifico l'output.
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Prenotazione effettuata con successo. Controlla il tab 'Prenotazioni' per maggiori informazioni.", output);
	}

	@Test
	/**
	 * Metodo che testa il filtro con città Francia.
	 */
	void filtroCittaFrancia(FxRobot robot) throws SQLException {
		//Avvio la ricerca
		TextField luogo = robot.lookup("#filtroLuogo").queryAs(TextField.class);
		luogo.setText("");
		robot.clickOn("#filtroLuogo").write("Francia");
		robot.clickOn("#avviaRicerca");
		//Verifico l'output.
		String output = robot.lookup("#erroreFiltro").queryAs(Label.class).getText();
		assertEquals("Città non esistente. Visitare dait.interno.gov.it per maggiori informazioni.", output);
	}

	@Test
	/**
	 * Metodo che testa la prenotazione di una disponibilità non esistente.
	 */
	void prenotazioneDispNonEsistente(FxRobot robot){
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Non hai ancora fatto una ricerca.");
		robot.clickOn("#prenotazione");
		//Verifico l'output.
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("La sua scelta non è una disponibilità.", output);
	}

	@Test
	/**
	 * Metodo che testa la prenotazione di un rilascio prima del suo ultimo ritiro
	 */
	void prenotazioneRilascioPrimaDelRitiro(FxRobot robot) throws SQLException {
		robot.clickOn("#filtroAndOr");
		//Inserisco 2 disponibilità di test
		int idRilascio = inserisciDisp("Rilascio furto", "VERONA");
		inserisciDisp("Rilascio furto", "VERONA");
		//Avvio la ricerca
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio furto");
		robot.clickOn("#avviaRicerca");
		//Prenoto la disponibilità
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Rilascio furto - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Inserisco la disponibilità di test
		ModelDisponibilita modelDisponibilita = ModelDisponibilita.getIstanza();
		String secondaData = LocalDate.now().plusMonths(1).plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		modelDisponibilita.inserisciDisponibilità("TEST","Ritiro", secondaData, "08:00-09:00", "VERONA");
		//Ottengo l'id
		int idRitiro = modelDisponibilita.ottieniId("TEST","Ritiro", secondaData, "08:00-09:00", "VERONA");
		modelDisponibilita.chiudiConnessione();
		//Prenoto il ritiro
		robot.clickOn("#filtroRichiesta").clickOn("Ritiro");
		robot.clickOn("#avviaRicerca");
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Ritiro - VERONA - " + secondaData + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Prenoto il rilascio
		robot.clickOn("#filtroRichiesta").clickOn("Rilascio furto");
		robot.clickOn("#avviaRicerca");
		robot.clickOn("#elencoDisponibili");
		robot.clickOn("Rilascio furto - VERONA - " + domani + " - 08:00-09:00");
		robot.clickOn("#prenotazione");
		//Cancello le prenotazioni
		eliminaPrenotazione(idRilascio);
		eliminaPrenotazione(idRitiro);
		//Se erano le uniche disponibilità le elimino N.B. rimane una disponibilità di rilascio
		eliminaDisp(idRilascio, false);
		eliminaDisp(idRitiro, false);
		//Verifico l'output.
		String output = robot.lookup("#messaggioPrenotazione").queryAs(Label.class).getText();
		assertEquals("Stai richiedendo un rilascio prima di un ritiro. Cambiare data.", output);
	}

	/**
	 * Funzione che elimina una disponibilità se non ha più occorrenze.
	 * @param id id della disponibilità da eliminare.
	 * @param ignora true se non si vuole controllare le occorrenze.
	 * @throws SQLException
	 */
	private void eliminaDisp(int id, boolean ignora) throws SQLException {
		String query = "SELECT * FROM disponibilita WHERE id = " + id + ";";
		ResultSet rs = ModelDisponibilita.getIstanza().lanciaQuery(query);
		rs.next();
		int occorrenze = rs.getInt("occorrenze");
		rs.close();
		ModelDisponibilita.getIstanza().chiudiConnessione();
		if(occorrenze == 0 || ignora) {
			ModelDisponibilita modelDisponibilita = ModelDisponibilita.getIstanza();
			modelDisponibilita.elimina(Integer.toString(id), "id");
			modelDisponibilita.chiudiConnessione();
		}
	}

	/**
	 * Funzione che inserisce una disponibilità di test.
	 * @return id della disponibilità inserita.
	 * @throws SQLException
	 */
	private int inserisciDisp(String tipologia, String sede) throws SQLException{
		ModelDisponibilita modelDisponibilita = ModelDisponibilita.getIstanza();
		modelDisponibilita.inserisciDisponibilità("TEST",tipologia, domani, "08:00-09:00", sede.toUpperCase());
		//Ottengo l'id
		int idDisponibilita = modelDisponibilita.ottieniId("TEST",tipologia, domani, "08:00-09:00", sede.toUpperCase());
		modelDisponibilita.chiudiConnessione();
		return idDisponibilita;
	}

	/**
	 * Funzione che elimina una prenotazione di test.
	 * @param id id della disponibilità associata alla prenotazione
	 * @throws SQLException
	 */
	private void eliminaPrenotazione(int id) throws SQLException {
		ModelPrenotazione modelPrenotazione = ModelPrenotazione.getIstanza();
		modelPrenotazione.elimina(Integer.toString(modelPrenotazione.ottieniId("TEST", id)), "idPrenotazione");
		modelPrenotazione.chiudiConnessione();
	}
}