package controller.addetto;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class InserisciDisponibilita extends Controller implements Initializable {
	@FXML
	private Label erroreDisponibilità;
	@FXML
	private ComboBox sceltaRichiesta;
	@FXML
	private TextField inserimentoCittà;
	@FXML
	private DatePicker sceltaGiorno;
	@FXML
	private CheckComboBox sceltaOrario;
	@FXML
	private Label logAddetto;
	@FXML
	private ComboBox<String> disponibilitaNonPrenotate;
	@FXML
	private ComboBox<String> disponibilitaPrenotate;


	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		log("Inserisco le scelte nei menu a tendina...");
		sceltaRichiesta.getItems().addAll("Rilascio prima volta", "Rilascio rinnovo", "Rilascio deterioramento",
												"Rilascio smarrimento", "Rilascio furto", "Ritiro");
		sceltaOrario.getItems().addAll("08:00-09:00", "09:00-10:00",
				"10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
				"16:00-17:00", "17:00-18:00");
		log("Inserisco l'addetto loggato...");
		logAddetto.setText(UtenteLog.getUtente(null).getIdentificativo());
		try {
			aggiornaDisponibilita();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Funzione che aggiorna le combobox delle disponibilità prenotate e non prenotate.
	 * @throws SQLException
	 */
	private void aggiornaDisponibilita() throws SQLException {
		log("Pulisco le combo box delle disponibilità prenotate e non...");
		disponibilitaNonPrenotate.getItems().clear();
		disponibilitaPrenotate.getItems().clear();
		log("Aggiungo le stringhe di default...");
		disponibilitaNonPrenotate.getItems().add("Lista delle disponibilità non prenotate");
		disponibilitaPrenotate.getItems().add("Lista delle disponibilità prenotate");
		log("Aggiorno le combo box delle disponibilità prenotate e non...");
		ModelDisponibilita modelDisponibilita = null;
		try {
			log("Ottengo il modello disponibilità...");
			modelDisponibilita = ModelDisponibilita.getIstanza();
			log("Ottengo la lista delle disponibilità...");
			LinkedList<String> lista = modelDisponibilita.ottieniDisponibilitaAddetto(logAddetto.getText());
			log("Scorro la lista e aggiorno le combo box...");
			for(String disponibilita : lista){
				String[] disponibilitaSplit = disponibilita.split(" - "); //0 tipo, 1 sede, 2 giorno, 3 ora, 4 occorrenze
				//N.B. in occorrenze ho il nr° di occorrenze non prenotate
				int prenotata = ModelPrenotazione.getIstanza().ottieniNumeroPrenotazioni(modelDisponibilita
						.ottieniId(logAddetto.getText(),disponibilitaSplit[0], disponibilitaSplit[2], disponibilitaSplit[3], disponibilitaSplit[1]));
				if(prenotata == 0)
					disponibilitaNonPrenotate.getItems().add(disponibilita);
				else{
					if (Integer.parseInt(disponibilitaSplit[4]) > 0) //Se ci sono ancora disponibilità non prenotate le aggiorno
						disponibilitaNonPrenotate.getItems().add(disponibilita);
					//Inserisco le disponibilità prenotate
					disponibilitaSplit[4] = String.valueOf(prenotata); //Aggiorno il numero di occorrenze
					disponibilitaPrenotate.getItems().add(String.join(" - ", disponibilitaSplit));
				}
			}
			log("ComboBox aggiornate.\n");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if(modelDisponibilita != null)
				modelDisponibilita.chiudiConnessione();
		}
	}


	/**
	 * Funzione quando l'utente clicca sul bottone sottometti.
	 * Chiama la funzione inserisci.
	 */
	@FXML
	protected void inserisciDisponibilità(ActionEvent evento) throws SQLException {
		inserisci(evento);
	}

	/**
	 * Funzione quando l'addetto preme il tasto invio.
	 * Chiama la funzione inserisci.
	 */
	@FXML
	protected void invioDisponibilità(KeyEvent evento) throws SQLException {
		if(evento.getCode().equals(KeyCode.ENTER))
			inserisci(evento);
	}

	/**
	 * Funzione che inserisce la disponibilità nel database, dopo averne verificato la correttezza.
	 * @param evento evento che ha scatenato la funzione
	 * @throws SQLException
	 */
	private void inserisci(Event evento) throws SQLException {
		log("Bottone inserisci disponibilità cliccato...");
		log("Converto a stringa i campi...");
		String città = inserimentoCittà.getText().toUpperCase();
		LocalDate giorno = sceltaGiorno.getValue();
		ObservableList orario = sceltaOrario.getCheckModel().getCheckedItems();
		Object richiesta_no_null = sceltaRichiesta.getValue();
		log("Controllo che i campi non siano vuoti...");
		if (città.isEmpty() || giorno == null || orario.isEmpty() || richiesta_no_null == null) {
			log("Errore: uno o più campi sono vuoti.\n");
			erroreDisponibilità.setText("Inserire tutti i campi.");
			return;
		}

		log("Cambio il formato alla data...");
		String giornoStringa = giorno.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		String richiesta = richiesta_no_null.toString();
		log("Controllo che la data non sia prima di oggi...");
		if (giorno.isBefore(LocalDate.now().plusDays(1))) {
			log("Errore: la data è oggi o precedente.\n");
			pulisciCampi();
			erroreDisponibilità.setText("La data deve essere successiva a oggi.");
			return;
		}

		log("Controllo che la data non sia dopo due mese...");
		if (giorno.isAfter(LocalDate.now().plusMonths(2))) {
			log("Errore: la data è dopo due mesi.\n");
			pulisciCampi();
			erroreDisponibilità.setText("La data deve essere entro 2 mesi.");
			return;
		}

		log("Verifico che la data inserita non sia un festivo...");
		if(festivo(giorno)){
			log("Errore: la data inserita è un festivo.\n");
			erroreDisponibilità.setText("La data inserita è un festivo.");
			return;
		}

		log("Ottengo l'istanza del modello città...");
		try {
			ElencoCitta elencoCitta = ElencoCitta.getIstanza();
			log("Ottengo il codice della città...");
			String codice = elencoCitta.ottieniCodice(città);
			if (codice == null) {
				log("Errore: la città non esiste.\n");
				pulisciCampi();
				erroreDisponibilità.setText("La sede deve esistere.");
				return;
			} else if (codice.charAt(0) == 'Z') {
				log("Errore: impossibile inserire la disponibilità in uno stato.\n");
				pulisciCampi();
				erroreDisponibilità.setText("La sede deve essere una città.");
				return;
			}
		}
		catch (RuntimeException e){
			log(e.getMessage());
			pulisciCampi();
			erroreDisponibilità.setText("Si prega di riprovare o contattare sia@gov.it fornendo il codice 1.");
			return;
		}
		ModelDisponibilita modelDisponibilita = null;
		try {
			log("Ottengo l'istanza del modello disponibilità...");
			modelDisponibilita = ModelDisponibilita.getIstanza();

			log("Ottengo la città dell'addetto, in un determinato giorno...");
			String cittàAddetto = modelDisponibilita.ottieniCitta(logAddetto.getText(), giornoStringa);
			log("Controllo che l'addetto non abbia già inserito la disponibilità in quella data in una città differente...");
			if (cittàAddetto != null && !cittàAddetto.equals(città)) {
				log("Errore: l'addetto ha già inserito la disponibilità in un'altra città.\n");
				pulisciCampi();
				erroreDisponibilità.setText("Non puoi trovarti in due sede differenti nello stesso giorno.");
				return;
			}

			log("Inserisco le disponibilità nel database...");
			for (Object o : orario)
				modelDisponibilita.inserisciDisponibilità(logAddetto.getText(),richiesta, giornoStringa, o.toString(), città);
			log("Disponibilità inserite con successo.\n");
			aggiornaDisponibilita();
			pulisciCampi();
			erroreDisponibilità.setText("Disponibilità inserita con successo.");
		}
		catch (SQLException e) {
			log(e.getMessage());
			erroreDisponibilità.setText("Si prega di riprovare o contattare sia@gov.it fornendo il codice 2.");
			throw new SQLException(e.getMessage());
		}
		finally {
			if (modelDisponibilita != null)
				modelDisponibilita.chiudiConnessione();
		}
	}


	/**
	 * Metodo che verifica se la data inserita è un festivo.
	 * @param giorno data da verificare.
	 * @return true se la data è un festivo, false altrimenti.
	 */
	private boolean festivo(LocalDate giorno){
		log("Verifico che non sia sabato o domenica...");
		int giornoSettimana = giorno.getDayOfWeek().getValue(); //1 = lunedì, 7 = domenica
		if(giornoSettimana == 6 || giornoSettimana == 7)
			return true;
		log("Verifico che non sia un festivo...");
		int day = giorno.getDayOfMonth();
		int mese = giorno.getMonthValue();
		int valore = mese * 100 + day; //esempio: 1 gennaio = 101, 1 febbraio = 201, 1 marzo = 301, ecc...
		log("Creo una lista di festivi...");
		LinkedList<Integer> festivi = new LinkedList<>();
		festivi.add(101); //1 gennaio -> capodanno
		festivi.add(106); //6 gennaio -> epifania
		festivi.add(425); //25 aprile -> liberazione
		festivi.add(501); //1 maggio -> festa del lavoro
		festivi.add(602); //2 giugno -> festa della repubblica
		festivi.add(815); //15 agosto -> ferragosto
		festivi.add(1101); //1 novembre -> tutti i santi
		festivi.add(1225); //25 dicembre -> natale
		festivi.add(1226); //26 dicembre -> santo stefano
		int pasqua = pasqua(giorno.getYear());
		festivi.add(pasqua); //pasqua
		//calcolo pasquetta
		if(pasqua == 430) //Caso 30/04
			festivi.add(501);
		else if(pasqua % 100 == 31) //Caso 31/03 e simili
			festivi.add((pasqua / 100 + 1) * 100 + 1);
		else //Caso 01/04 e simili
			festivi.add(pasqua + 1);
		log("Controllo se il giorno è festivo...");
		return festivi.contains(valore);
	}

	/**
	 * Calcola la data della pasqua di un determinato anno.
	 * @param anno Anno di cui si vuole calcolare la pasqua.
	 * @return Data della pasqua. (mmgg)
	 */
	private int pasqua(int anno){
		//Algoritmo di Gauss per il calcolo della data della pasqua
		int a = anno % 19;
		int b = anno / 100;
		int c = anno % 100;
		int d = b / 4;
		int e = b % 4;
		int g = (8 * b + 13) / 25;
		int h = (19 * a + b - d - g + 15) % 30;
		int j = c / 4;
		int k = c % 4;
		int m = (a + 11 * h) / 319;
		int r = (2 * e + 2 * j - k - h + m + 32) % 7;
		int n = (h - m + r + 90) / 25;
		int p = (h - m + r + n + 19) % 32;
		return n*100+p;
	}

	/**
	 * Funzione che pulisce i campi
	 */
	private void pulisciCampi() {
		log("Pulisco i campi...");
		inserimentoCittà.setText("");
		sceltaGiorno.setValue(null);
		sceltaOrario.getCheckModel().clearChecks();
		sceltaRichiesta.setValue(null);
	}

	/**
	 * Funzione che torna alla schermata precedente
	 */
	@FXML
	protected void tornaIndietroDisponibilità(ActionEvent evento) {
		UtenteLog.getUtente(null).logOut();
		log("Bottone torna indietro cliccato...");
		try {
			super.changeView("addetto/accesso-addetto.fxml", evento, "Accesso addetto");
		} catch (IOException e) {
			log(e.getMessage());
			erroreDisponibilità.setText("Errore durante il cambio schermata");
		}
	}

	@Override
	public void log(String s){super.log("[InserisciDisponibilita]: " + s);}
}
