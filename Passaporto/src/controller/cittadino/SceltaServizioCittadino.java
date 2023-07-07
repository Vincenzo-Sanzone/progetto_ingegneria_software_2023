package controller.cittadino;

import controller.Controller;
import controller.UtenteLog;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ElencoCitta;
import model.ModelDisponibilita;
import model.ModelPrenotazione;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class SceltaServizioCittadino extends Controller implements Initializable {
	//Questi campi riguardano il filtro
	@FXML
	private Label logUtente;
	@FXML
	private Label erroreFiltro;
	@FXML
	private TextField filtroLuogo;
	@FXML
	private DatePicker filtroData;
	@FXML
	private CheckComboBox filtroOrario;
	@FXML
	private ComboBox<String> filtroRichiesta;
	@FXML
	private CheckBox filtroAndOr;

	//Questi campi riguardano la disponibilità
	@FXML
	private Label messaggioPrenotazione;
	@FXML
	private ComboBox<String> elencoDisponibili;
	@FXML
	private TextField dichiarazioniExtra;

	@FXML
	private ComboBox<String> elencoOccupati; //Questo campo riguardo gli occupati.
	@FXML
	private DatePicker trovaGiorno; //Questo campo riguarda il giorno che si vuole scoprire quando verrà inserito.
	@FXML
	private Label disponibileMessaggio; //Questo campo riguarda il messaggio delle disponibilità da inserire.
	@FXML
	private Label disponibileDa; //Questo campo riguarda il messaggio delle disponibilità da inserire.

	@FXML
	private ComboBox<String> elencoPrenotazioni; //Questo campo riguarda le prenotazioni effettuate.

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		filtroRichiesta.getItems().addAll("","Rilascio prima volta", "Rilascio rinnovo", "Rilascio deterioramento",
												"Rilascio smarrimento", "Rilascio furto", "Ritiro");
		filtroOrario.getItems().addAll("08:00-09:00", "09:00-10:00",
				"10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
				"16:00-17:00", "17:00-18:00");
		elencoDisponibili.getItems().addAll("Non hai ancora fatto una ricerca.");
		elencoOccupati.getItems().addAll("Non hai ancora fatto una ricerca.");
		logUtente.setText("Benvenuto: " + UtenteLog.getUtente(null).getIdentificativo());
		aggiornaPrenotazioni();
	}

	/**
	 * Funzione che aggiorna le prenotazioni effettuate dall'utente.
	 */
	private void aggiornaPrenotazioni() {
		log("Ripulisco la combo box delle prenotazioni...");
		elencoPrenotazioni.getItems().clear();
		log("Verifico eventuali prenotazioni precedenti...");
		try {
			log("Ottengo la lista delle prenotazioni...");
			LinkedList<String> prenotazioni = ModelPrenotazione.getIstanza().ottieniPrenotazioniCittadino(UtenteLog.getUtente(null).getIdentificativo());
			boolean entrato = false;
			while (!prenotazioni.isEmpty()){
				String prenotazione = prenotazioni.removeFirst();
				entrato = true;
				elencoPrenotazioni.getItems().add(prenotazione);
			}
			if(!entrato)
				elencoPrenotazioni.getItems().add("Nessuna prenotazione trovata.");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Funzione quando l'utente clicca invio nella ricerca.
	 * Chiama avviaRicerca.
	 */
	@FXML
	protected void avviaRicercaInvio(KeyEvent evento) {
		if(evento.getCode().equals(KeyCode.ENTER))
			avviaRicerca(evento);
	}

	/**
	 * Funzione quando l'utente clicca sul bottone ricerca.
	 * Chiama avviaRicerca.
	 */
	@FXML
	protected void avviaRicercaBottone(ActionEvent evento){
		avviaRicerca(evento);
	}

	/**
	 * Funzione che avvia la ricerca, e riempe la tabella con i risultati.
	 * La ricerca deve essere valida.
	 */
	private void avviaRicerca(Event evento) {
		log("Elimino eventuali messaggi precedenti...");
		disponibileDa.setText("");
		disponibileMessaggio.setText("");
		messaggioPrenotazione.setText("");
		log("Verifico il filtro...");
		boolean andOr = filtroAndOr.isSelected(); //true = and, false = or
		log("Filtro impostato a: " + ((andOr)? "AND" : "OR") + ".");
		boolean cercaLuogo, cercaData, cercaOrario, cercaRichiesta;
		cercaLuogo = cercaRichiesta = cercaData = cercaOrario = false;
		log("Estrapolo i filtri...");

		String luogo = filtroLuogo.getText().toUpperCase();
		log("Luogo selezionato: " + luogo + ".");

		LocalDate data = filtroData.getValue();
		log("Data selezionata: " + ((data != null)? data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Nessuna data selezionata."));

		ObservableList orario = filtroOrario.getCheckModel().getCheckedItems();
		log("Orario selezionato: " + ((orario.size() != 0)? orario.toString() : "Nessun orario selezionato."));

		String richiesta = filtroRichiesta.getValue();
		log("Richiesta selezionata: " + ((richiesta != null)? richiesta : "Nessuna richiesta selezionata."));

		log("Verifico il filtro luogo...");
		//La città deve esistere, e non deve essere uno stato.
		if(ElencoCitta.getIstanza().cittaEsiste(luogo) && !(ElencoCitta.getIstanza().ottieniCodice(luogo).startsWith("Z"))) {
			log("Filtro città inserito correttamente.");
			cercaLuogo = true;
		}
		else if(!(luogo.equals("") || luogo.equalsIgnoreCase("Cerca luogo"))){
			log("Filtro città non inserito correttamente.\n");
			erroreFiltro.setText("Città non esistente. Visitare dait.interno.gov.it per maggiori informazioni.");
			return;
		}

		String dataString = "";
		log("Verifico il filtro data...");
		if(data != null && data.isAfter(LocalDate.now())) {
			log("Filtro data inserito correttamente.");
			log("Converto la data a stringa...");
			dataString = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			cercaData = true;
		}
		else if(data != null){
			log("Filtro data non inserito correttamente.\n");
			erroreFiltro.setText("Data non valida. Inserire una data successiva a quella odierna.");
			return;
		}

		log("Verifico il filtro orario...");
		if(!orario.isEmpty()){
			log("Filtro orario inserito correttamente.");
			cercaOrario = true;
		}

		log("Verifico il filtro richiesta...");
		if(!(richiesta == null || richiesta.equals("Cerca richiesta"))){
			log("Filtro richiesta inserito correttamente.");
			cercaRichiesta = true;
		}

		log("Preparo la query...");
		String query = "SELECT * FROM disponibilita";

		boolean inserito = false;
		if(cercaLuogo){
			query += " WHERE sede = '" + luogo + "'";
			inserito = true;
		}
		if(cercaData){
			if(inserito)
				query += ((andOr)? " AND " : " OR ") + "giorno = '" + dataString + "'";
			else{
				query += " WHERE giorno = '" + dataString + "'";
				inserito = true;
			}
		}
		if(cercaOrario){
			if(inserito)
				query += ((andOr)? " AND " : " OR ") + "orario IN (";
			else{
				query += " WHERE orario IN (";
				inserito = true;
			}
			for(int i = 0; i < orario.size(); i++){
				if(i == orario.size() - 1)
					query += "'" + orario.get(i) + "')";
				else
					query += "'" + orario.get(i) + "', ";
			}
		}
		if (cercaRichiesta) {
			if(inserito)
				query += ((andOr)? " AND " : " OR ") + "tipologia = '" + richiesta + "'";
			else{
				query += " WHERE tipologia = '" + richiesta + "'";
			}
		}
		log("La query finale è: " + query + ".");
		ModelDisponibilita modelDisponibilita = null;
		try {
			modelDisponibilita = ModelDisponibilita.getIstanza();
			ResultSet rs = modelDisponibilita.lanciaQuery(query);
			log("Query eseguita correttamente.");
			log("Ripulisco i campi...");
			elencoDisponibili.getItems().clear();
			elencoOccupati.getItems().clear();
			boolean disponibile = false;
			log("Riempio le comboBox...");
			while (rs.next()) {
				disponibile = true;
				String luogoDB = rs.getString("sede");
				String giornoDB = rs.getString("giorno");
				String orarioDB = rs.getString("orario");
				String richiestaDB = rs.getString("tipologia");
				int occorrenze = rs.getInt("occorrenze");
				if(occorrenze > 0)
					elencoDisponibili.getItems().add(richiestaDB + " - " + luogoDB + " - " + giornoDB + " - " + orarioDB);
				else
					elencoOccupati.getItems().add(richiestaDB + " - " + luogoDB + " - " + giornoDB + " - " + orarioDB);
			}
			if(!disponibile) {
				log("Nessun risultato trovato.");
				elencoDisponibili.getItems().add("Nessun risultato trovato.");
				elencoOccupati.getItems().add("Nessun risultato trovato.");

			}
			else
				log("Risultati trovati.");
			log("Ricerca effettuata con successo.");
			erroreFiltro.setText("Ricerca effettuata con successo.");
		}
		catch (SQLException e) {
			log(e.getMessage());
			erroreFiltro.setText("Errore nella ricerca.");
			throw new RuntimeException(e);
		}
		finally {
			log("Chiudo la connessione...\n");
			if(modelDisponibilita != null) {
				try {
					modelDisponibilita.chiudiConnessione();
				} catch (SQLException e) {
					log(e.getMessage());
					throw new RuntimeException(e);
				}
			}
		}

	}

	/**
	 * Funzione quando l'utente clicca invio nella prenotazione.
	 * Chiama la funzione avviaPrenotazione.
	 */
	@FXML
	protected void prenotaInvio(KeyEvent evento) {
		if(evento.getCode().equals(KeyCode.ENTER))
			avviaPrenotazione(evento);
	}

	/**
	 * Funzione quando l'utente clicca sul bottone prenota.
	 * Chiama la funzione avviaPrenotazione.
	 */
	@FXML
	protected void prenotaBottone(ActionEvent evento) {
		avviaPrenotazione(evento);
	}

	/**
	 * Funzione che avvia la prenotazione, assicurandosi della validità dei dati inseriti.
	 */
	private void avviaPrenotazione(Event evento) {
		log("Elimino eventuali messaggi precedenti...");
		disponibileDa.setText("");
		disponibileMessaggio.setText("");
		erroreFiltro.setText("");
		log("Ottengo la scelta dell'utente...");
		String prenotazione = elencoDisponibili.getValue();
		if(dichiarazioniExtra.getText().length() > 255){
			log("Dichiarazioni extra troppo lunghe.");
			messaggioPrenotazione.setText("Dichiarazioni extra troppo lunghe.");
			return;
		}
		if(prenotazione == null || prenotazione.equals("Nessun risultato trovato.") || prenotazione.equals("Non hai ancora fatto una ricerca.")) {
			log("Nessuna prenotazione selezionata.\n");
			messaggioPrenotazione.setText("La sua scelta non è una disponibilità.");
			return;
		}
		log("Verifico la coerenza della prenotazione, con le precedenti...");
		String[] prenotazioneSplit = verificaCoerenza(prenotazione);
		if (prenotazioneSplit == null) return;

		log("Ottengo l'id della prenotazione...");
		try {
			int idDisponibilita = ModelDisponibilita.getIstanza()
					.ottieniId(null, prenotazioneSplit[0], prenotazioneSplit[2], prenotazioneSplit[3], prenotazioneSplit[1]);
			log("Ottengo l'istanza del modello prenotazione...");
			ModelPrenotazione modelPrenotazione = ModelPrenotazione.getIstanza();
			log("Inserisco la prenotazione...");
			modelPrenotazione.inserisciPrenotazione(UtenteLog.getUtente(null).getIdentificativo(),
					idDisponibilita, dichiarazioniExtra.getText());
			modelPrenotazione.chiudiConnessione();
			aggiornaPrenotazioni();
			log("Prenotazione inserita correttamente.");
			messaggioPrenotazione.setText("Prenotazione effettuata con successo. Controlla il tab 'Prenotazioni' per maggiori informazioni.");
			log("Ripulisco i campi...");
			elencoDisponibili.getItems().clear();
			elencoOccupati.getItems().clear();
			log("Inserisco il campo 'Non hai ancora fatto una ricerca.'...");
			elencoDisponibili.getItems().add("Non hai ancora fatto una ricerca.");
			elencoOccupati.getItems().add("Non hai ancora fatto una ricerca.");
		}
		catch (RuntimeException e){
			log(e.getMessage());
			messaggioPrenotazione.setText("Sembra che qualcuno abbia ottenuto la disponibilità prima di te.");
			throw new RuntimeException(e.getMessage());
		}
		catch (SQLException e) {
			log(e.getMessage());
			messaggioPrenotazione.setText("Errore nella prenotazione.");
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Funzione che verifica la coerenza della prenotazione.
	 * @param prenotazione La prenotazione selezionata.
	 * @return Un array di stringhe contenente i dati della prenotazione. Null se la prenotazione non è coerente.
	 * Il metodo aggiorna i log e gli errori in caso di prenotazione non coerente.
	 */
	private String[] verificaCoerenza(String prenotazione) {
		log("Prenotazione selezionata: " + prenotazione + ".");
		log("Ottengo i dati della prenotazione...");
		String[] prenotazioneSplit = prenotazione.split(" - "); //0 = tipologia richiesta, 1 = luogo, 2 = giorno, 3 = orario
		boolean ritiro = prenotazioneSplit[0].equalsIgnoreCase("ritiro"); //Ho chiesto un ritiro (true) o un rilascio (false)?
		boolean rilascioPrimaVolta = prenotazioneSplit[0].equalsIgnoreCase("rilascio prima volta"); //Ho chiesto un rilascio prima volta?
		log("Verifico la compatibilità con le prenotazioni precedenti...");

		log("Ottengo tutte le prenotazioni precedenti...");

		ObservableList<String> prenotazioniPrecedenti = elencoPrenotazioni.getItems();
		log("Inizio a scorrere le prenotazioni...");
		int numeroRitiri = 0; //Variabili che mi servono per verificare il numero di ritiri e rilasci che trovo.
		int numeroRilasci = 0;
		LocalDate dataRilascio = null; //Variabile che avrà l'ultimo rilascio richiesto
		LocalDate dataRitiro = null; //Variabile che avrà l'ultimo ritiro richiesto
		for(String unaPrenotazione : prenotazioniPrecedenti){
			log("Prenotazione: " + unaPrenotazione + ".");
			String[] unaPrenotazioneSplit = unaPrenotazione.split(" - "); //0 = tipologia richiesta, 1 = luogo, 2 = giorno, 3 = orario

			//Caso in cui non abbiamo ancora prenotazioni
			if(unaPrenotazioneSplit[0] == null || unaPrenotazione.equals("Nessuna prenotazione trovata."))
				break;

			if(unaPrenotazioneSplit[0].equalsIgnoreCase("ritiro")){
				//Salvo la data più recente
				if(dataRitiro == null || dataRitiro.isBefore(LocalDate.parse(unaPrenotazioneSplit[2]))){
					dataRitiro = LocalDate.parse(unaPrenotazioneSplit[2], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
				}
				numeroRitiri++;
			}
			else{
				numeroRilasci++;
				//Salvo la data più recente
				if(dataRilascio == null || dataRilascio.isBefore(LocalDate.parse(unaPrenotazioneSplit[2]))){
					dataRilascio = LocalDate.parse(unaPrenotazioneSplit[2], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
				}
			}
		}

		log("Verifico i numeri di ritiri e rilasci...");
		if(dataRitiro != null &&
				LocalDate.parse(prenotazioneSplit[2], DateTimeFormatter.ofPattern("dd/MM/yyyy")).isBefore(dataRitiro)){
			//Caso in cui chiedo un rilascio prima di un ritiro.
			log("L'utente ha chiesto: " + prenotazioneSplit[0] + " il giorno: " + prenotazioneSplit[2] +
					", ma l'ultimo ritiro deve essere fatto il giorno: " + dataRitiro + ".\n");
			messaggioPrenotazione.setText("Stai richiedendo un rilascio prima di un ritiro. Cambiare data.");
			return null;
		}
		if(rilascioPrimaVolta && numeroRilasci > 0){
			//Caso in cui ho chiesto un rilascio prima volta ma ho già fatto un rilascio.
			log("L'utente ha chiesto: " + prenotazioneSplit[0] + ", ma il numero rilasci corrisponde a: "
					+ numeroRilasci + ".\n");
			messaggioPrenotazione.setText("Hai già fatto un rilascio. Cambiare motivazione del rilascio.");
			return null;
		}
		if(!ritiro && numeroRilasci != numeroRitiri){
			//Caso in cui ho chiesto un rilascio ma non ho fatto tutti i ritiri.
			log("L'utente ha chiesto: " + prenotazioneSplit[0] + ", ma il numero rilasci corrisponde a: "
					+ numeroRilasci + " e il numero ritiri corrisponde a: " + numeroRitiri + ".\n");
			messaggioPrenotazione.setText("Non hai ancora fatto tutti i ritiri.");
			return null;
		}
		else if(ritiro && numeroRilasci == numeroRitiri){
			//Caso in cui ho chiesto un ritiro, ma non avevo chiesto il rilascio.
			log("L'utente ha chiesto: " + prenotazioneSplit[0] + ", ma il numero rilasci corrisponde a: "
					+ numeroRilasci + " e il numero ritiri corrisponde a: " + numeroRitiri + ".\n");
			messaggioPrenotazione.setText("Non hai chiesto il rilascio, impossibile chiedere il ritiro.");
			return null;
		}
		else if(ritiro && dataRilascio.plusMonths(1).isAfter
				(LocalDate.parse(prenotazioneSplit[2], DateTimeFormatter.ofPattern("dd/MM/yyyy")))){
			//Caso in cui ho chiesto un ritiro, ma non è passato 1 mese dall'ultimo rilascio
			log("L'utente ha chiesto: " + prenotazioneSplit[0] + ", ma l'ultimo rilascio è stato il: "
					+ dataRilascio + ".\n");
			messaggioPrenotazione.setText("Non è passato un mese dall'ultimo rilascio.");
			return null;
		}
		return prenotazioneSplit;
	}

	/**
	 * Funzione quando l'utente clicca invio nella richiesta informazioni.
	 * Chiama la funzione richiestaInformazioni.
	 */
	@FXML
	protected void richiestaInformazioniInvio(KeyEvent evento){
		if(evento.getCode().equals(KeyCode.ENTER))
			richiestaInformazioni(evento);
	}

	/**
	 * Funzione quando l'utente clicca sul bottone richiesta informazioni.
	 * Chiama la funzione richiestaInformazioni.
	 */
	@FXML
	protected void richiestaInformazioniBottone(ActionEvent evento){
		richiestaInformazioni(evento);
	}

	/**
	 * Funzione che risponde alla richiesta dell'utente, se questa è valida.
	 */
	private void richiestaInformazioni(Event evento) {
		LocalDate giorno = trovaGiorno.getValue();
		if(giorno == null){
			log("Giorno non inserito.\n");
			disponibileMessaggio.setText("Inserire il giorno, da valutare.");
			disponibileDa.setText("");
		}
		else if (giorno.isBefore(LocalDate.now().plusDays(1))){
			log("Giorno inserito non valido.\n");
			disponibileMessaggio.setText("Possiamo informarti solamente per domani e i giorni successivi.");
			disponibileDa.setText("");
		}
		else if(giorno.isAfter(LocalDate.now().plusMonths(2))){
			log("Inserito giorno dopo 2 mesi.");
			log("Inserisco il messaggio.\n");
			disponibileMessaggio.setText("Il giorno: " + giorno.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " potrebbe essere disponibile da:");
			disponibileDa.setText(giorno.minusMonths(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		}
		else{
			log("Inserito giorno entro 2 mese.");
			log("Inserisco il messaggio.\n");
			disponibileMessaggio.setText("Il giorno: " + giorno.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " potrebbe essere disponibile");
			disponibileDa.setText("quando un addetto inserirà la sua disponibilità.");
		}
	}

	/**
	 * Funzione quando l'utente clicca torna indietro.
	 */
	@FXML
	protected void tornaIndietro(ActionEvent evento) {
		log("Torno indietro...");
		try {
			log("Effettuo il log out...");
			UtenteLog.getUtente(null).logOut();
			changeView("cittadino/accesso-cittadino.fxml", evento, "Accesso utente");
		} catch (IOException e) {
			log(e.getMessage());
			erroreFiltro.setText("Errore interno. Impossibile cambiare schemata");
		}
	}

	@Override
	public void log(String s){super.log("[SceltaServizioCittadino]: " + s);}
}
