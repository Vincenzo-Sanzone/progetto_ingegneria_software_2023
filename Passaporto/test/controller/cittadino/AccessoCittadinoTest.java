package controller.cittadino;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class AccessoCittadinoTest {

    @Start
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/cittadino/accesso-cittadino.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);;
        stage.setTitle("Accesso cittadino");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di campi vuoti.
     */
    void testCampiVuoti(FxRobot robot) {
        robot.clickOn("#bottoneAccessoUtente");
        String output = robot.lookup("#accessoErroreUtente").queryAs(Label.class).getText();
        assertEquals("Codice fiscale o password non inserita. Si prega di riprovare.", output);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di credenziali errate, e utente registrato.
     */
    void testCredenzialiErrateUtenteRegistrato(FxRobot robot) throws Exception {
        ModelCittadino model = ModelCittadino.getIstanza();
        String password = "";
        boolean elimina = false;
        if(model.èPresente("SNZVCN02L29C286R", "codiceFiscale")){
            ResultSet risultato = model.lanciaQuery("SELECT password FROM cittadini WHERE codiceFiscale='SNZVCN02L29C286R'");
            password = risultato.getString("password");
        }
        else{
            elimina = true;
            model.inserisciCittadino("Vincenzo", "Sanzone", LocalDate.of(2002, 7, 29)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "Castelvetrano",
                    "SNZVCN02L29C286R", "80380001209846723467","password");
            password = "password";
        }
        scriviCodiceFiscale(robot);
        robot.clickOn("#accessoPasswordUtente");
        robot.write(password.substring(0, password.length()-1));
        robot.clickOn("#bottoneAccessoUtente");
        if(elimina)
            model.elimina("SNZVCN02L29C286R", "codiceFiscale");
        String output = robot.lookup("#accessoErroreUtente").queryAs(Label.class).getText();
        assertEquals("La password inserita non è corretta. Contattare recupero.credenziali@gov.it", output);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di credenziali corrette, e utente registrato.
     */
    void testCredenzialiGiuste(FxRobot robot) throws Exception {
        ModelCittadino model = ModelCittadino.getIstanza();
        String password = "";
        boolean elimina = false;
        if(model.èPresente("SNZVCN02L29C286R", "codiceFiscale")){
            ResultSet risultato = model.lanciaQuery("SELECT password FROM cittadini WHERE codiceFiscale='SNZVCN02L29C286R'");
            password = risultato.getString("password");
        }
        else{
            elimina = true;
            model.inserisciCittadino("Vincenzo", "Sanzone", LocalDate.of(2002, 7, 29)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "Castelvetrano",
                    "SNZVCN02L29C286R", "12345678901234567890","password");
            password = "password";
        }
        scriviCodiceFiscale(robot);
        robot.clickOn("#accessoPasswordUtente");
        robot.write(password);
        robot.clickOn("#bottoneAccessoUtente");
        if(elimina)
            model.elimina("SNZVCN02L29C286R", "codiceFiscale");
        Button bottone = robot.lookup("#avviaRicerca").queryAs(Button.class);
        assertNotNull(bottone);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di utente non registrato.
     */
    void testUtenteNonRegistrato(FxRobot robot) throws Exception {
        ModelCittadino model = ModelCittadino.getIstanza();
        String nome, cognome, luogo, password, tessera;
        nome = cognome = luogo = password = tessera = "";
        String data = "";
        boolean ripristina = false;
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
        scriviCodiceFiscale(robot);
        scriviPassword(robot);
        robot.clickOn("#bottoneAccessoUtente");
        String output = robot.lookup("#accessoErroreUtente").queryAs(Label.class).getText();
        if(ripristina && !model.èPresente("SNZVCN02L29C286R", "codiceFiscale"))
            model.inserisciCittadino(nome, cognome, data, luogo, "SNZVCN02L29C286R", tessera, password);
        assertEquals("L'utente non è registrato. O il codice fiscale è errato.", output);
    }

    @Test
    /**
     * Funzione che testa l'accesso con un codice fiscale non valido.
     */
    void testCodiceFiscaleNonValido(FxRobot robot) {
        robot.clickOn("#accessoCodiceFiscaleUtente");
        robot.write("SNZVCN02829C286");
        scriviPassword(robot);
        robot.clickOn("#bottoneAccessoUtente");
        String output = robot.lookup("#accessoErroreUtente").queryAs(Label.class).getText();
        assertEquals("L'utente non è registrato. O il codice fiscale è errato.", output);
    }

    @Test
    /**
     * Funzione che testa il bottone registrati.
     */
    void testBottoneRegistrati(FxRobot robot) {
        robot.clickOn("#bottoneRegistrazioneUtente");
        Button bottone = robot.lookup("#sottomettiRegistrazione").queryAs(Button.class);
        assertNotNull(bottone);
    }

    @Test
    /**
     * Funzione che testa il bottone indietro.
     */
    void testBottoneIndietro(FxRobot robot) {
        robot.clickOn("#bottoneIndietroAccessoUtente");
        Button bottone = robot.lookup("#interfacciaUtente").queryAs(Button.class);
        assertNotNull(bottone);
    }

    /**
     * Funzione che scrive il codice fiscale.
     */
    private void scriviCodiceFiscale(FxRobot robot) {
        robot.clickOn("#accessoCodiceFiscaleUtente");
        robot.write("SNZVCN02L29C286R");
    }

    /**
     * Funzione che scrive la password.
     */
    private void scriviPassword(FxRobot robot) {
        robot.clickOn("#accessoPasswordUtente");
        robot.write("password");
    }
}