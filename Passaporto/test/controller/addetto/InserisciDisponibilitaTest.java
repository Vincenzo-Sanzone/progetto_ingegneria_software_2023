package controller.addetto;

import controller.UtenteLog;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.ModelDisponibilita;
import org.controlsfx.control.CheckComboBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class InserisciDisponibilitaTest {
    @Start
    private void start(Stage stage) throws IOException {
        UtenteLog.getUtente("TEST");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/addetto/inserisci-disponibilita.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);;
        stage.setTitle("Inserisci disponibilità");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    /**
     * Testa che il bottone "Indietro" porti alla schermata precedente
     */
    void testIndietro(FxRobot robot) {
        robot.clickOn("#bottoneIndietroInserisciDisponibilità");
        Button bottone = robot.lookup("#bottoneAccessoAddetto").queryAs(Button.class);
        assertNotNull(bottone);
    }

    @Test
    /**
     * Funzione che testa l'inserimento corretto
     */
    void testCorretto(FxRobot robot) {
        scriviSede(robot);
        scriviData(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Disponibilità inserita con successo.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con un festivo.
     */
    void testFestivo(FxRobot robot) {
        //Cerco il sabato o domenica
        LocalDate giorno = LocalDate.now();
        while (giorno.getDayOfWeek().getValue() != 6 && giorno.getDayOfWeek().getValue() != 7)
            giorno = giorno.plusDays(1);
        robot.lookup("#sceltaGiorno").queryAs(DatePicker.class).setValue(giorno);
        scriviSede(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("La data inserita è un festivo.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con sede vuota
     */
    void testSedeVuota(FxRobot robot) {
        scriviData(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Inserire tutti i campi.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con data vuota
     */
    void testDataVuota(FxRobot robot) {
        scriviSede(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Inserire tutti i campi.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con orario vuoto
     */
    void testOrarioVuoto(FxRobot robot) {
        scriviSede(robot);
        scriviData(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Inserire tutti i campi.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con richiesta vuota
     */
    void testRichiestaVuota(FxRobot robot) throws SQLException {
        scriviOrario(robot);
        scriviSede(robot);
        scriviData(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        eliminaDisponibilita("VERONA");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Inserire tutti i campi.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con data passata
     */
    void testDataPassata(FxRobot robot) {
        scriviSede(robot);
        robot.lookup("#sceltaGiorno").queryAs(DatePicker.class).setValue(LocalDate.now().minusDays(1));
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("La data deve essere successiva a oggi.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento con data troppo lontana
     */
    void testDataTroppoLontana(FxRobot robot) {
        scriviSede(robot);
        robot.lookup("#sceltaGiorno").queryAs(DatePicker.class).setValue(LocalDate.now().plusMonths(2).plusDays(1));
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("La data deve essere entro 2 mesi.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento di una richiesta già inserita
     */
    void testRichiestaGiaInserita(FxRobot robot) throws SQLException {
        ModelDisponibilita model = ModelDisponibilita.getIstanza();
        int occorrenze = 0;

        for(int i = 0; i < 2; i++) {
            scriviSede(robot);
            scriviData(robot);
            scriviOrario(robot);
            scriviRichiesta(robot);
            robot.clickOn("#bottoneSottomettiDisponibilità");
        }
        LocalDate giorno = LocalDate.now().plusWeeks(2);
        if(giorno.getDayOfWeek().getValue() == 6 || giorno.getDayOfWeek().getValue() == 7)
            giorno = giorno.plusDays(2);
        int id = model.ottieniId(null,"Rilascio prima volta", giorno.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(),
                "10:00-11:00", "Verona");
        for(int i = 0; i < 2; i++){
            model.elimina(String.valueOf(id), "id");
        }
        model.chiudiConnessione();
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Disponibilità inserita con successo.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento di una città non esistente
     */
    void testCittàNonEsistente(FxRobot robot) {
        robot.clickOn("#inserimentoCittà");
        robot.write("Pippo");
        scriviData(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("La sede deve esistere.", output);
    }

    @Test
    /**
     * Funzione che testa l'inserimento di uno stato come sede
     */
    void testStatoComeSede(FxRobot robot) {
        robot.clickOn("#inserimentoCittà");
        robot.write("Francia");
        scriviData(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("La sede deve essere una città.", output);
    }

    /**
     * Funzione che testa l'inserimento di una sede mentre l'addetto dovrebbe trovarsi in un'altra sede
     */
    void testSedeDifferente(FxRobot robot) throws SQLException {
        //Inserisco la prima città
        robot.clickOn("#inserimentoCittà");
        robot.write("Roma");
        scriviData(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        //Inserisco la seconda città
        scriviSede(robot);
        scriviData(robot);
        scriviOrario(robot);
        scriviRichiesta(robot);
        robot.clickOn("#bottoneSottomettiDisponibilità");
        //Elimino le disponibilità
        eliminaDisponibilita("ROMA");
        String output = robot.lookup("#erroreDisponibilità").queryAs(Label.class).getText();
        assertEquals("Non puoi trovarti in due sede differenti nello stesso giorno.", output);
    }

    /**
     * Funzione che elimina le disponibilità inserite
     * @param sede sede in cui si trova l'addetto
     * @throws SQLException
     */
    private void eliminaDisponibilita(String sede) throws SQLException {
        ModelDisponibilita model = ModelDisponibilita.getIstanza();
        LocalDate giorno = LocalDate.now().plusWeeks(2);
        if(giorno.getDayOfWeek().getValue() == 6 || giorno.getDayOfWeek().getValue() == 7)
            giorno = giorno.plusDays(2);
        int id = model.ottieniId("TEST","Rilascio prima volta", giorno
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(), "10:00-11:00", sede);
        model.elimina(String.valueOf(id), "id");
        id = model.ottieniId("TEST","Rilascio prima volta", giorno
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(), "11:00-12:00", sede);
        model.elimina(String.valueOf(id), "id");
        id = model.ottieniId("TEST","Rilascio prima volta", giorno
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(), "17:00-18:00", sede);
        model.elimina(String.valueOf(id), "id");
        model.chiudiConnessione();
    }

    /**
     * Funzione che scrive la sede
     */
    private void scriviSede(FxRobot robot) {
        robot.clickOn("#inserimentoCittà");
        robot.write("Verona");
    }

    /**
     * Funzione che scrive la data
     */
    private void scriviData(FxRobot robot) {
        LocalDate giorno = LocalDate.now().plusWeeks(2);
        if(giorno.getDayOfWeek().getValue() == 6 || giorno.getDayOfWeek().getValue() == 7)
            giorno = giorno.plusDays(2);
        robot.lookup("#sceltaGiorno").queryAs(DatePicker.class).setValue(giorno);

    }

    /**
     * Funzione che scrive l'orario
     */
    private void scriviOrario(FxRobot robot) {
        robot.clickOn("#sceltaOrario").clickOn("10:00-11:00").clickOn("11:00-12:00").clickOn("17:00-18:00");
    }

    /**
     * Funzione che scegli la richiesta
     */
    private void scriviRichiesta(FxRobot robot) {
        robot.clickOn("#sceltaRichiesta").clickOn("Rilascio prima volta");
    }


}