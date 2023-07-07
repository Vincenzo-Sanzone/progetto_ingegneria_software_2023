package controller.cittadino;

import controller.Controller;
import controller.UtenteLog;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ElencoCitta;
import model.ModelCittadino;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


public class RegistrazioneCittadino extends Controller {
	@FXML
	private Label registrazioneErroreUtente;
	@FXML
	private TextField registrazioneNomeUtente;
	@FXML
	private TextField registrazioneCognomeUtente;
	@FXML
	private DatePicker registrazioneDataNascitaUtente;
	@FXML
	private TextField registrazioneLuogoNascitaUtente;
	@FXML
	private TextField registrazioneCodiceFiscale;
	@FXML
	private TextField registrazioneTesseraSanitariaUtente;
	@FXML
	private TextField registrazionePasswordUtente;


	/**
	 * Funzione usata quando clicchiamo il bottone sottometti, durante la registrazione
	 * Chiama registra.
	 */
	@FXML
	protected void sottomettiRegistrazione(ActionEvent evento){
		registra(evento);
	}


	/**
	 * Funzione usata quando clicchiamo il bottone invio
	 * Chiama registra.
	 */
	@FXML
	protected void invioRegistrazione(KeyEvent evento){
		if(evento.getCode().equals(KeyCode.ENTER))
			registra(evento);
	}

	/**
	 * Funzione che registra il cittadino, se le credenziali sono corrette.
	 * @param evento evento che ha portato alla chiamata della funzione.
	 */
	private void registra(Event evento){
		log("Converto da testo a stringa...");
		String nome = registrazioneNomeUtente.getText();
		String cognome = registrazioneCognomeUtente.getText();
		LocalDate dataNascita = registrazioneDataNascitaUtente.getValue();
		String luogoNascita = registrazioneLuogoNascitaUtente.getText().toUpperCase();
		String codiceFiscale = registrazioneCodiceFiscale.getText().toUpperCase();
		String tesseraSanitaria = registrazioneTesseraSanitariaUtente.getText();
		String password = registrazionePasswordUtente.getText();

		log("Verifico se i campi non sono vuoti...");
		if(nome.isEmpty() || cognome.isEmpty() || dataNascita == null || luogoNascita.isEmpty() ||
				codiceFiscale.isEmpty() || tesseraSanitaria.isEmpty() || password.isEmpty()){
			log("Inserisco il messaggio d'errore...\n");
			registrazioneErroreUtente.setText("Uno dei campi era vuoto, si prega di inserirli tutti.");
			return;
		}

		log("Verifico se i campi non sono troppo lunghi...");
		if(nome.length() > 255 || cognome.length() > 255 || password.length() > 255){
			log("Lunghezza nome: " + nome.length() + " Lunghezza cognome: " + cognome.length() + " Lunghezza password: " + password.length());
			log("Inserisco il messaggio d'errore...\n");
			registrazioneErroreUtente.setText("Nome/cognome/password troppo lunghi. In caso di errore contattare passaporto@gov.it.");
			return;
		}

		log("Cambio il formato alla data");
		dataNascita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		log("Verifico se il luogo di nascita è valido...");
		ElencoCitta elencoCitta = ElencoCitta.getIstanza();
		if(!elencoCitta.cittaEsiste(luogoNascita)){
			log("Inserisco il messaggio d'errore...\n");
			registrazioneLuogoNascitaUtente.clear();
			registrazioneErroreUtente.setText("Il luogo di nascita non è valido. Se nato all'estero inserire la nazione.");
			return;
		}

		log("Verifico se la data inserita non viene dopo oggi...");
		if(dataNascita.isAfter(LocalDate.now())){
			log("L'utente ha inserito: " + dataNascita + " ma oggi è: " + LocalDate.now() +"\n");
			registrazioneDataNascitaUtente.setValue(null);
			registrazioneErroreUtente.setText("Il giorno inserito è dopo la data odierna.");
			return;
		}

		log("Verifico se la data non è troppo vecchia...");
		if(dataNascita.isBefore(LocalDate.now().minusYears(116).plusDays(1))){
			log("La data inserita è: " + dataNascita + " ma non può essere prima di: "
					+ LocalDate.now().minusYears(116).plusDays(1) + "\n");
			registrazioneDataNascitaUtente.setValue(null);
			registrazioneErroreUtente.setText("Il giorno inserito è troppo vecchio.");
			return;
		}

		log("Verifico se la tessera sanitaria è valida...");
		if(!tesseraSanitariaValida(tesseraSanitaria)){
			log("Inserisco il messaggio d'errore...\n");
			registrazioneTesseraSanitariaUtente.clear();
			registrazioneErroreUtente.setText("La tessera sanitaria non è valida. Inserisci le cifre senza spazi.\n (possibile errore)");
			return;
		}

		if(!codiceFiscaleValido(nome, cognome, dataNascita, codiceFiscale, luogoNascita)){
			log("Inserisco il messaggio d'errore... \n");
			registrazioneErroreUtente.setText("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.");
			return;
		}

		try {
			log("Ottengo l'istanza del modello...");
			ModelCittadino model = ModelCittadino.getIstanza();
			log("Verifico l'esistenza del codice fiscale...");
			if(model.èPresente(codiceFiscale, "codiceFiscale")){
				log("Il codice fiscale: " + codiceFiscale + " è già presente.\n");
				ripulisci();
				registrazioneErroreUtente.setText("Codice fiscale già registrato. Contattare recupero.credenziali@gov.it " +
						"per il recupero delle credenziali.");
				return;
			}

			if(model.èPresente(tesseraSanitaria, "tesseraSanitaria")){
				log("La tessera sanitaria: " + tesseraSanitaria + " è già presente.\n");
				ripulisci();
				registrazioneErroreUtente.setText("Tessera sanitaria già registrata. Contattare recupero.credenziali@gov.it " +
						"per il recupero delle credenziali.");
				return;
			}

			log("Inserisco il cittadino...");
			model.inserisciCittadino(nome, cognome, dataNascita.toString(), luogoNascita, codiceFiscale, tesseraSanitaria,password);
			log("Registrazione effettuata.\n");
			UtenteLog.getUtente(codiceFiscale);
			model.chiudiConnessione();
			super.changeView("cittadino/scelta-servizio-cittadino.fxml", evento, "Scegli il servizio");
		}
		catch (SQLException e){
			log(e.toString());
			ripulisci();
			registrazioneErroreUtente.setText("Il database non è raggiungibile, si prega di riprovare, o chiudere il programma.");
		}
		catch (IOException e){
			log(e.toString());
			ripulisci();
			registrazioneErroreUtente.setText("Problemi durante l'accesso alla pagina.");
		}
		catch (Exception e){
			log(e.toString());
			ripulisci();
			registrazioneErroreUtente.setText("Errore sconosciuto, si prega di riprovare, o chiudere il programma.");
		}
	}

	/**
	 * Metodo che verifica se la tessera sanitaria è valida.
	 * @param tesseraSanitaria tessera sanitaria da verificare.
	 * @return true se la tessera sanitaria è valida, false altrimenti.
	 */
	private boolean tesseraSanitariaValida(String tesseraSanitaria) {
		log("Veifico lunghezza tessera sanitaria...");
		if(tesseraSanitaria.length() != 20){
			log("La lunghezza della tessera sanitaria è: " + tesseraSanitaria.length() + " ma deve essere 20.\n");
			return false;
		}

		log("Verifico che la tessera sanitaria sia composta solo da numeri...");
		for(int i = 0; i < tesseraSanitaria.length(); i++){
			if(!Character.isDigit(tesseraSanitaria.charAt(i))){
				log("La tessera sanitaria contiene caratteri non numerici.\n");
				return false;
			}
		}

		log("Verifico i primi 5 caratteri della tessera sanitaria...");
		if(!tesseraSanitaria.substring(0, 5).equals("80380")){
			log("I primi 5 caratteri della tessera sanitaria sono: " + tesseraSanitaria.substring(0, 5) + " ma devono essere 80380.\n");
			return false;
		}

		log("Verifico che i caratteri 6 e 7 siano 00...");
		if(!tesseraSanitaria.substring(5, 7).equals("00")){
			log("I caratteri 6 e 7 della tessera sanitaria sono: " + tesseraSanitaria.substring(5, 7) + " ma devono essere 00.\n");
			return false;
		}

		final String codiciRegionali[] = {"010", "020", "030", "041", "042", "050", "060", "070", "080", "090", "100",
											"110", "120", "130", "140", "150", "160", "170", "180", "190", "200"};
		log("Verifico che i caratteri 8, 9 e 10 siano un codice regionale...");
		boolean codiceRegionaleValido = false;
		for(String codice : codiciRegionali){
			if(tesseraSanitaria.substring(7, 10).equals(codice)){
				codiceRegionaleValido = true;
				break;
			}
		}
		//Gli ultimi 10 caratteri sono univoci per ogni cittadino, e non possiamo sapere se sono corretti o meno.
		return codiceRegionaleValido;
	}

	/**
	 * Funzione usata quando clicchiamo il bottone torna indietro, durante la registrazione.
	 */
	@FXML
	void tornaIndietroRegistrazione(ActionEvent evento){
		log("Bottone torna indietro cliccato");
		log("Cambio visuale...");
		try {
			super.changeView("cittadino/accesso-cittadino.fxml", evento, "Accesso cittadino");
		}
		catch (IOException e){
			log(e.toString());
			registrazioneErroreUtente.setText("Problemi durante l'accesso alla pagina.");
		}
	}

	/**
	 * La funzione ripulisce tutti i campi ad eccezione del campo errore.
	 */
	private void ripulisci(){
		log("Ripulisco i campi...");
		registrazioneNomeUtente.setText("");
		registrazioneCognomeUtente.setText("");
		registrazioneDataNascitaUtente.setValue(null);
		registrazioneLuogoNascitaUtente.setText("");
		registrazioneCodiceFiscale.setText("");
		registrazioneTesseraSanitariaUtente.setText("");
		registrazionePasswordUtente.setText("");
	}

	/**
	 * Funzione che controlla la validità del codice fiscale.
	 * @param nome Nome inserito dall'utente.
	 * @param cognome Cognome inserito dall'utente.
	 * @param dataNascita Data di nascita inserita dall'utente.
	 * @param codiceFiscale Codice fiscale inserito dall'utente.
	 * @return true se il codice fiscale è valido, false altrimenti.
	 */
	private boolean codiceFiscaleValido(String nome, String cognome, LocalDate dataNascita, String codiceFiscale, String luogoNascita){
		log("Inizio controllo validità del codice fiscale...");
		log("Controllo lunghezza...");
		if(codiceFiscale.length() != 16){
			log("Lunghezza errata: " + codiceFiscale.length() + " ma dovrebbe essere 16.");
			return false;
		}
		log("Controllo inserimento solo di lettere...");
		if(!soloLettere(nome)){
			log("Il nome contiene un carattere non lettera.");
			return false;
		}
		if(!soloLettere(cognome)){
			log("Il cognome contiene un carattere non lettera.");
			return false;
		}

		String consonanti_estrapolate = ottieniConsonanti(cognome, false);
		log("Verifico validità delle consonanti del cognome...");
		if(!consonanti_estrapolate.equalsIgnoreCase(codiceFiscale.substring(0, 3))){
			log("Le consonanti del cognome sono: " + codiceFiscale.substring(0,3) + ", ma dovrebbero essere: " + consonanti_estrapolate);
			return false;
		}

		consonanti_estrapolate = ottieniConsonanti(nome, true);
		log("Verifico validità delle consonanti del nome...");
		if(!consonanti_estrapolate.equalsIgnoreCase(codiceFiscale.substring(3, 6))){
			log("Le consonanti del nome sono: " + codiceFiscale.substring(3,6) + ", ma dovrebbero essere: " + consonanti_estrapolate);
			return false;
		}

		log("Verifico validità dell'anno...");
		int anno_nascita = dataNascita.getYear();
		anno_nascita = anno_nascita % 100; //Mi servono solamente le ultime due cifre
		try {
			int anno_inserito = Integer.parseInt(codiceFiscale.substring(6, 8));
			if (anno_nascita != anno_inserito) {
				log("Anno di nascita ricavato dalla data corrisponderebbe a: " + anno_nascita +
						", ma l'anno di nascita inserito nel codice fiscale corrisponde a: " + anno_inserito);
				return false;
			}
		}
		catch (Exception e){
			log("Presenti caratteri non numeri nell'anno.");
			return false;
		}
		char lettereMese[] = {'A', 'B', 'C', 'D', 'E', 'H', 'L', 'M', 'P', 'R', 'S', 'T'};
		int mese_nascita = dataNascita.getMonthValue();
		log("Verifico validità del mese...");
		if(lettereMese[mese_nascita-1] != Character.toUpperCase(codiceFiscale.charAt(8))){
			log("Lettera inserita nel codice fiscale: " + codiceFiscale.charAt(8) + ", ma era prevista: " + lettereMese[mese_nascita-1]);
			return false;
		}

		int giorniMese[] = {31, (dataNascita.isLeapYear()) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		log("Verifica validità dei giorni...");
		try {
			int giorno_inserito = Integer.parseInt(codiceFiscale.substring(9, 11));
			if ((giorno_inserito > giorniMese[mese_nascita - 1] && giorno_inserito < 41) || giorno_inserito > giorniMese[mese_nascita - 1] + 40) {
				log("Il giorno: " + giorno_inserito + " non è valido per il: " + mese_nascita + " mese.");
				return false;
			}
		}
		catch (Exception e){
			log("Presenti caratteri non numeri nei giorni.");
			return false;
		}

		log("Verifico validità codice catastale...");
		try {
			ElencoCitta elencoCitta = ElencoCitta.getIstanza();
			String codiceCatastale = elencoCitta.ottieniCodice(luogoNascita);
			if(!codiceFiscale.substring(11, 15).equals(codiceCatastale)) {
				log("Il codice catastale inserito: " + codiceFiscale.substring(11, 15) + " non corrisponde a quello ricavato: " + codiceCatastale);
				return false;
			}
		}
		catch (RuntimeException e){
			log("Errore all'interno dell'elenco città: " + e.getMessage());
			throw e;
		}

		log("Verifico validità del carattere di controllo...");
		HashMap<Character, Integer> pari = new HashMap<>();
		log("Inserisco i valori delle lettere pari...");
		pari.put('0', 0); pari.put('1', 1); pari.put('2', 2); pari.put('3', 3); pari.put('4', 4); pari.put('5', 5);
		pari.put('6', 6); pari.put('7', 7); pari.put('8', 8); pari.put('9', 9); pari.put('A', 0); pari.put('B', 1);
		pari.put('C', 2); pari.put('D', 3); pari.put('E', 4); pari.put('F', 5); pari.put('G', 6); pari.put('H', 7);
		pari.put('I', 8); pari.put('J', 9); pari.put('K', 10); pari.put('L', 11); pari.put('M', 12); pari.put('N', 13);
		pari.put('O', 14); pari.put('P', 15); pari.put('Q', 16); pari.put('R', 17); pari.put('S', 18); pari.put('T', 19);
		pari.put('U', 20); pari.put('V', 21); pari.put('W', 22); pari.put('X', 23); pari.put('Y', 24); pari.put('Z', 25);
		HashMap<Character, Integer> dispari = new HashMap<>();
		log("Inserisco i valori delle lettere dispari...");
		dispari.put('0', 1); dispari.put('1', 0); dispari.put('2', 5); dispari.put('3', 7); dispari.put('4', 9); dispari.put('5', 13);
		dispari.put('6', 15); dispari.put('7', 17); dispari.put('8', 19); dispari.put('9', 21); dispari.put('A', 1); dispari.put('B', 0);
		dispari.put('C', 5); dispari.put('D', 7); dispari.put('E', 9); dispari.put('F', 13); dispari.put('G', 15); dispari.put('H', 17);
		dispari.put('I', 19); dispari.put('J', 21); dispari.put('K', 2); dispari.put('L', 4); dispari.put('M', 18); dispari.put('N', 20);
		dispari.put('O', 11); dispari.put('P', 3); dispari.put('Q', 6); dispari.put('R', 8); dispari.put('S', 12); dispari.put('T', 14);
		dispari.put('U', 16); dispari.put('V', 10); dispari.put('W', 22); dispari.put('X', 25); dispari.put('Y', 24); dispari.put('Z', 23);
		int somma = 0;
		log("Calcolo la somma...");
		for(int i = 1; i < 16; i++){
			if(i % 2 == 0)
				somma += pari.get(codiceFiscale.charAt(i-1));
			else
				somma += dispari.get(codiceFiscale.charAt(i-1));
		}
		log("Calcolo il carattere di controllo...");
		char carattere_controllo = (char) ((int)'A' + (somma % 26));
		if(codiceFiscale.charAt(15) != carattere_controllo) {
			log("Il carattere di controllo inserito: " + codiceFiscale.charAt(15) + " non corrisponde a quello ricavato: " + carattere_controllo);
			return false;
		}

		log("Il codice fiscale è valido.");
		return true;
	}

	/**
	 * Funzione che verifica la presenza di solo lettere.
	 * @param testo: testo che si vuole verificare.
	 * @return true se ha solo lettere, false altrimenti.
	*/
	private boolean soloLettere(String testo){
		for(int i = 0; i < testo.length(); i++){
			if(!Character.isAlphabetic(testo.charAt(i)) && testo.charAt(i) != ' ')
				return false;
		}
		return true;
	}

	/**
	 * Funzione che restituisce le lettere del codice fiscale.
	 * @param testo il testo da cui estrapolare le lettere.
	 * @param salta indica se saltare o meno la seconda consonante.
	 * @return una stringa contenente le 3 lettere.
	 */
	private String ottieniConsonanti(String testo, boolean salta){
		log("Estrapolando le consonanti da: " + testo + "...");
		String risultato = "";
		String vocali = "";
		short consonante_trovate = 0;
		for (int i = 0; i < testo.length(); i++) {
			if(consonante(testo.charAt(i))){
				risultato += Character.toUpperCase(testo.charAt(i));
				consonante_trovate++;
				if(consonante_trovate == 3 && !salta)
					break;
				else if (consonante_trovate == 4) {
					break;
				}
			}
			else
				vocali += Character.toUpperCase(testo.charAt(i));
		}
		//Caso cognome
		if((consonante_trovate != 3 && !salta) || (consonante_trovate < 3 && salta)){
			short lunghezza_risultato = consonante_trovate;
			for(int i = 0; i < testo.length() - consonante_trovate; i++){
				risultato += Character.toUpperCase(vocali.charAt(i));
				lunghezza_risultato++;
				if(lunghezza_risultato == 3)
					break;
			}
			if(lunghezza_risultato != 3){
				for(int i = lunghezza_risultato; i < 3; i++){
					risultato += 'X';
				}
			}
		} else if (salta && consonante_trovate == 4)
			risultato = risultato.substring(0,1) + risultato.substring(2);
		return risultato;
	}

	/**
	 * Funzione che dice se una lettera è consonante o meno.
	 * @param carattere il carattere da verificare.
	 * @return true se carattere è una consonante, false altrimenti.
	 */
	private boolean consonante(char carattere){
		carattere = Character.toLowerCase(carattere);
		return Character.isAlphabetic(carattere) && carattere != 'a' && carattere != 'e' && carattere != 'i' &&
				carattere != 'o' && carattere != 'u';
	}

	@Override
	public void log(String s){super.log("[RegistrazioneCittadino]: " + s);}
}
