package controller.cittadino;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import model.ModelCittadino;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class RegistrazioneCittadinoTest {

	@Start
	public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/cittadino/registrazione-cittadino.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);;
        stage.setTitle("Registrazione cittadino");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
	}

	@Test
    /**
     * Inseriamo tutti i campi dell'interfaccia, con il codice fiscale errato nel cognome
     */
	void codiceFiscaleErratoCognome(FxRobot robot) {
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNNVCN02L29C286R");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
	}

    @Test
    /**
     * Inseriamo tutti i campi dell'interfaccia, con il codice fiscale errato nel giorno
     */
    void codiceFiscaleErratoGiorno(FxRobot robot) {
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L39C286R");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }

    @Test
    /**
     * Inseriamo tutti i campi dell'interfaccia, con il codice fiscale errato nel nome
     */
    void codiceFiscaleErratoNome(FxRobot robot) {
        robot.clickOn("#registrazioneNomeUtente");
        robot.write("PL");
        scriviCognome(robot);
        scriviData(robot);
        scriviLuogo(robot);
        scriviCodice(robot);
        scriviTessera(robot);
        scriviPassword(robot);
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }

    @Test
    /**
     * Inseriamo tutti i campi con l'eccezione della data
     */
    void dataMancante(FxRobot robot) {
        scriviNome(robot);
        scriviCognome(robot);
        scriviLuogo(robot);
        scriviCodice(robot);
        scriviTessera(robot);
        scriviPassword(robot);
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Uno dei campi era vuoto, si prega di inserirli tutti.", output);
    }

    @Test
    /**
     * Inseriamo tutti i campi, ma la data è il giorno dopo di oggi
     */
    void dataDopoOggi(FxRobot robot) {
        scriviNome(robot);
        scriviCognome(robot);
        scriviLuogo(robot);
        DatePicker datePicker = robot.lookup("#registrazioneDataNascitaUtente").queryAs(DatePicker.class);
        assertNotNull(datePicker);
        datePicker.setValue(LocalDate.now().plusDays(1));
        scriviCodice(robot);
        scriviTessera(robot);
        scriviPassword(robot);
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il giorno inserito è dopo la data odierna.", output);
    }

    @Test
    /**
     * Inseriamo tutti i campi, ma la data è un giorno troppo vecchio
     */
    void dataVecchia(FxRobot robot) {
        scriviNome(robot);
        scriviCognome(robot);
        scriviLuogo(robot);
        DatePicker datePicker = robot.lookup("#registrazioneDataNascitaUtente").queryAs(DatePicker.class);
        assertNotNull(datePicker);
        datePicker.setValue(LocalDate.now().minusYears(120));
        scriviCodice(robot);
        scriviTessera(robot);
        scriviPassword(robot);
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il giorno inserito è troppo vecchio.", output);
    }

    @Test
    /**
     * Inseriamo tutti i campi in maniera corretta
     */
    void tuttoCorretto(FxRobot robot) throws Exception {
        scriviCodice(robot);
        scriviTuttoNoCodice(robot);
        boolean ripristina = false;
        String nome = "";
        String cognome = "";
        String luogo = "";
        String data = "";
        String tessera = "";
        String password = "";
        ModelCittadino model = ModelCittadino.getIstanza();
        if(model.èPresente("SNZVCN02L29C286R", "codiceFiscale")){
            ResultSet risultato = model.lanciaQuery("SELECT * FROM cittadini WHERE codiceFiscale='SNZVCN02L29C286R'");
            nome = risultato.getString("nome");
            cognome = risultato.getString("cognome");
            luogo = risultato.getString("luogoNascita");
            tessera = risultato.getString("tesseraSanitaria");
            password = risultato.getString("password");
            data = risultato.getString("dataNascita");
            ripristina = true;
            model.elimina("SNZVCN02L29C286R", "codiceFiscale");
        }
        robot.clickOn("#sottomettiRegistrazione");
        model.elimina("SNZVCN02L29C286R", "codiceFiscale");
        if(ripristina && !model.èPresente("SNZVCN02L29C286R", "codiceFiscale"))
            model.inserisciCittadino(nome, cognome, data, luogo, "SNZVCN02L29C286R", tessera, password);
        Button bottone = robot.lookup("#avviaRicerca").queryAs(Button.class);
        assertNotNull(bottone);
    }

    @Test
    /**
     * Metodo che testa il funzionamento in caso di nome troppo lungo.
     */
    void nomeTroppoLungo(FxRobot robot) {
        scriviCodice(robot);
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneNomeUtente");
        for(int i = 0; i < 20; i++)
            robot.write("CARoxpJFIWmco");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Nome/cognome/password troppo lunghi. " +
                "In caso di errore contattare passaporto@gov.it.", output);
    }

    @Test
    /**
     * Verifichiamo il funzionamento in caso fosse già presente nel database
     */

    void giàPresenteCodiceFiscale(FxRobot robot) throws Exception {
        scriviTuttoNoCodice(robot);
        scriviCodice(robot);
        ModelCittadino model = ModelCittadino.getIstanza();
        boolean cancella = false;
        if(!model.èPresente("SNZVCN02L29C286R", "codiceFiscale")) {
            model.inserisciCittadino("a", "a", LocalDate.of(2000, 9, 9)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "a",
                    "SNZVCN02L29C286R", "80380001000000000000","pass");
            cancella = true;
        }
        robot.clickOn("#sottomettiRegistrazione");
        Label label = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class);
        assertNotNull(label);
        if(cancella){
            model.elimina("SNZVCN02L29C286R", "codiceFiscale");
        }
        assertEquals("Codice fiscale già registrato. Contattare recupero.credenziali@gov.it per il recupero delle credenziali.",
                label.getText());
    }

    @Test
    /**
     * Tessera sanitaria già presente nel database.
     */
    void giàPresenteTesseraSanitaria(FxRobot robot) throws Exception {
        scriviTuttoNoCodice(robot);
        scriviCodice(robot);
        ModelCittadino model = ModelCittadino.getIstanza();
        boolean cancella = false;
        if(!model.èPresente("80380000109835471048", "tesseraSanitaria")) {
            model.inserisciCittadino("a", "a", LocalDate.of(2000, 9, 9)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "a",
                    "SNZVCN02L29C286E", "80380000109835471048","pass");
            cancella = true;
        }
        robot.clickOn("#sottomettiRegistrazione");
        Label label = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class);
        assertNotNull(label);
        if(cancella){
            model.elimina("SNZVCN02L29C286E", "codiceFiscale");
        }
        assertEquals("Tessera sanitaria già registrata. Contattare recupero.credenziali@gov.it per il recupero delle credenziali.",
                label.getText());
    }

    @Test
    /**
     * Codice fiscale errato nella lunghezza
     */
    void codiceErratoLunghezza(FxRobot robot){
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L29C286");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }

    @Test
    /**
     * Codice fiscale contiene caratteri non previsti
     */
    void codiceErratoLettere(FxRobot robot){
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L29C286%");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }
    
    @Test
    /**
     * Codice fiscale contenente lettera sbagliata del mese
     */
    void codiceErratoLetteraMese(FxRobot robot){
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02H29C286R");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }
    
    @Test
    /**
     * Codice fiscale contiene lettere invece del giorno
     */
    void codiceErratoGiorno(FxRobot robot){
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L2TC286R");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }
    
    @Test
    /**
     * Codice fiscale contiene un carattere di controllo non coerente.
     */
    void codiceControlloErrato(FxRobot robot){
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L29C286E");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }

    @Test
    /**
     * Codice fiscale contiene un codice catastale non coerente.
     */
    void codiceCatastaleErrato(FxRobot robot) {
        scriviTuttoNoCodice(robot);
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L29C276R");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il codice fiscale risulta errato. Contattare passaporto@gov.it per maggiori informazioni.", output);
    }

    @Test
    /**
     * Tessera sanitaria contiene un codice regionale non coerente.
     */
    void tesseraSanitariaErrataRegione(FxRobot robot) {
        scriviCodice(robot);
        scriviCognome(robot);
        scriviData(robot);
        scriviNome(robot);
        scriviPassword(robot);
        scriviLuogo(robot);
        robot.clickOn("#registrazioneTesseraSanitariaUtente");
        robot.write("80380000110000000001");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("La tessera sanitaria non è valida. Inserisci le cifre senza spazi.\n (possibile errore)", output);
    }

    @Test
    /**
     * Tessera sanitara contiene un carattere non valido.
     */
    void tesseraSanitariaErrataCarattere(FxRobot robot) {
        scriviCodice(robot);
        scriviCognome(robot);
        scriviData(robot);
        scriviNome(robot);
        scriviPassword(robot);
        scriviLuogo(robot);
        robot.clickOn("#registrazioneTesseraSanitariaUtente");
        robot.write("8038000010000000000a");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("La tessera sanitaria non è valida. Inserisci le cifre senza spazi.\n (possibile errore)", output);
    }

    @Test
    /**
     * Tessera sanitaria contiene una lunghezza errata.
     */
    void tesseraSanitariaErrataLunghezza(FxRobot robot) {
        scriviCodice(robot);
        scriviCognome(robot);
        scriviData(robot);
        scriviNome(robot);
        scriviPassword(robot);
        scriviLuogo(robot);
        robot.clickOn("#registrazioneTesseraSanitariaUtente");
        robot.write("8038000010000000000");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("La tessera sanitaria non è valida. Inserisci le cifre senza spazi.\n (possibile errore)", output);
    }

    @Test
    /**
     * Codice fiscale contiene un città non presente.
     */
    void cittàErrata(FxRobot robot) {
        scriviCodice(robot);
        scriviCognome(robot);
        scriviData(robot);
        scriviNome(robot);
        scriviPassword(robot);
        scriviTessera(robot);
        robot.clickOn("#registrazioneLuogoNascitaUtente");
        robot.write("Parigi");
        robot.clickOn("#sottomettiRegistrazione");
        String output = robot.lookup("#registrazioneErroreUtente").queryAs(Label.class).getText();
        assertEquals("Il luogo di nascita non è valido. Se nato all'estero inserire la nazione.", output);
    }

    @Test
    /**
     * Funzione che testa il bottone torna indietro.
     * Dopodiché ritorno alla schermata di registrazione e verifico che i campi siano vuoti
     */
    void tornaIndietro(FxRobot robot) {
        scriviTuttoNoCodice(robot);
        robot.clickOn("#bottoneIndietroRegistrazioneUtente");
        Button bottone = robot.lookup("#bottoneRegistrazioneUtente").queryAs(Button.class);
        assertNotNull(bottone);
        robot.clickOn("#bottoneRegistrazioneUtente");
        String output = robot.lookup("#registrazioneNomeUtente").queryAs(TextField.class).getText();
        assertEquals("", output);
    }

    /**
     * Funzione che scrive in ordine casuale i campi eccetto il codice fiscale
     */
    private void scriviTuttoNoCodice(FxRobot robot) {
        boolean nome, cognome, luogo, password, data, tessera;
        nome = cognome = luogo = password = data = tessera =true;
        while(nome || cognome || luogo || password || data || tessera){
            Random random = new Random();
            int x = random.nextInt(6);
            switch (x) {
                case 0:
                    if(nome)
                        scriviNome(robot);
                    nome = false;
                    break;
                case 1:
                    if(data)
                        scriviData(robot);
                    data = false;
                    break;
                case 2:
                    if(password)
                        scriviPassword(robot);
                    password = false;
                    break;
                case 3:
                    if(cognome)
                        scriviCognome(robot);
                    cognome = false;
                    break;
                case 4:
                    if(luogo)
                        scriviLuogo(robot);
                    luogo = false;
                    break;
                case 5:
                    if(tessera)
                        scriviTessera(robot);
                    tessera = false;
                    break;
            }
        }
    }

    /**
     * Funzione che scrive il nome corretto.
     */
    private void scriviNome(FxRobot robot){
        robot.clickOn("#registrazioneNomeUtente");
        robot.write("Vincenzo");
    }

    /**
     * Funzione che scrive il cognome corretto.
     */
    private void scriviCognome(FxRobot robot){
        robot.clickOn("#registrazioneCognomeUtente");
        robot.write("Sanzone");
    }

    /**
     * Funzione che scrive la data corretta.
     */
    private void scriviData(FxRobot robot){
        DatePicker data = robot.lookup("#registrazioneDataNascitaUtente").queryAs(DatePicker.class);
        assertNotNull(data);
        data.setValue(new LocalDateStringConverter().fromString("29/07/2002"));
    }

   /**
     * Funzione che scrive il luogo corretto.
     */
    private void scriviLuogo(FxRobot robot){
        robot.clickOn("#registrazioneLuogoNascitaUtente");
        robot.write("Castelvetrano");
    }

    /**
     * Funzione che scrive il codice fiscale corretto.
     */
    private void scriviCodice(FxRobot robot) {
        robot.clickOn("#registrazioneCodiceFiscale");
        robot.write("SNZVCN02L29C286R");
    }

    /**
     * Funzione che scrive la password corretta.
     */
    private void scriviPassword(FxRobot robot) {
        robot.clickOn("#registrazionePasswordUtente");
        robot.write("miaPassword");
    }

    private void scriviTessera(FxRobot robot) {
        robot.clickOn("#registrazioneTesseraSanitariaUtente");
        robot.write("80380000109835471048");
    }
}