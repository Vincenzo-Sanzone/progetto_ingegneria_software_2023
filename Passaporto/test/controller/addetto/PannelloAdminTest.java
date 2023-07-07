package controller.addetto;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.ModelAddetto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class PannelloAdminTest {
    @Start
    private void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/addetto/pannello-admin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);;
        stage.setTitle("Pannello admin");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    /**
     * Metodo che testa l'inserimento in caso di campi vuoti.
     */
    void testCampiVuoti(FxRobot robot) {
        robot.clickOn("#bottoneInserisciAddetto");
        String output = robot.lookup("#registrazioneErroreAddetto").queryAs(Label.class).getText();
        assertEquals("Nome o password non inserita. Si prega di riprovare.", output);
    }
    
    @Test
    /**
     * Metodo che testa l'inserimento in caso di nome già esistente.
     */
    void testNomeEsistente(FxRobot robot) throws SQLException {
        ModelAddetto model = ModelAddetto.getIstanza();
        boolean elimina = false;
        if(!model.èPresente("UTENTE081", "nome")) {
            model.inserisciAddetto("UTENTE081", "miaPassword");
            elimina = true;
        }
        scriviNome(robot);
        scriviPassword(robot);
        robot.clickOn("#bottoneInserisciAddetto");
        String output = robot.lookup("#registrazioneErroreAddetto").queryAs(Label.class).getText();
        if(elimina)
            model.elimina("UTENTE081", "nome");
        assertEquals("Nome già esistente. Si prega di riprovare.", output);
    }

    @Test
    /**
     * Metodo che testa l'inserimento con nome admin.
     */
    void testNomeAdmin(FxRobot robot){
        robot.clickOn("#registrazioneNomeAddetto");
        robot.write("AdMiN");
        scriviPassword(robot);
        robot.clickOn("#bottoneInserisciAddetto");
        String output = robot.lookup("#registrazioneErroreAddetto").queryAs(Label.class).getText();
        assertEquals("Impossibile registrare l'admin. Si prega di riprovare.", output);
    }

    @Test
    /**
     * Metodo che testa un inserimento valido
     */
    void testInserimentoValido(FxRobot robot) throws SQLException {
        ModelAddetto model = ModelAddetto.getIstanza();
        boolean ripristina = false;
        String password = "";
        if(model.èPresente("UTENTE081", "nome")) {
            String query = "SELECT * FROM addetti WHERE nome = 'UTENTE081'";
            ResultSet rs = model.lanciaQuery(query);
            password = rs.getString("password");
            model.elimina("UTENTE081", "nome");
            ripristina = true;
        }
        scriviNome(robot);
        robot.clickOn("#registrazionePasswordAddetto");
        robot.write("password");
        robot.clickOn("#bottoneInserisciAddetto");
        String output = robot.lookup("#registrazioneErroreAddetto").queryAs(Label.class).getText();
        assertEquals("Registrazione avvenuta con successo.", output);
    }

    @Test
    /**
     * Metodo che testa il bottone indietro.
     */
    void testBottoneIndietro(FxRobot robot) {
        robot.clickOn("#bottoneIndietroPannelloAdmin");
        Button bottone = robot.lookup("#bottoneAccessoAddetto").queryAs(Button.class);
        assertNotNull(bottone);
    }

    /**
     * Funzione che scrive la password.
     */
    private void scriviPassword(FxRobot robot) {
        robot.clickOn("#registrazionePasswordAddetto");
        robot.write("miaPassword");
    }

    /**
     * Funzione che scrive il nome.
     */
    private void scriviNome(FxRobot robot) {
        robot.clickOn("#registrazioneNomeAddetto");
        robot.write("utente081");
    }

}