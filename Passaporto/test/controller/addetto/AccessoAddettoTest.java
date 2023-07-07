package controller.addetto;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
class AccessoAddettoTest {
    @Start
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/addetto/accesso-addetto.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);;
        stage.setTitle("Accesso addetto");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di campi vuoti.
     */
    void testCampiVuoti(FxRobot robot) {
        robot.clickOn("#bottoneAccessoAddetto");
        String output = robot.lookup("#accessoErroreAddetto").queryAs(Label.class).getText();
        assertEquals("Nome o password non inserita. Si prega di riprovare.", output);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di credenziali errate, e addetto registrato.
     */
    void testCredenzialiErrateAddettoRegistrato(FxRobot robot) throws Exception {
        ModelAddetto model = ModelAddetto.getIstanza();
        String password = "";
        boolean elimina = false;
        if (model.èPresente("UTENTE090", "nome")) {
            ResultSet risultato = model.lanciaQuery("SELECT * FROM addetti WHERE nome='UTENTE090'");
            password = risultato.getString("password");
        } else {
            elimina = true;
            model.inserisciAddetto("UTENTE090", "password");
            password = "password";
        }
        scriviNome(robot);
        robot.clickOn("#accessoPasswordAddetto");
        robot.write(password.substring(0, password.length() - 1));
        robot.clickOn("#bottoneAccessoAddetto");
        String output = robot.lookup("#accessoErroreAddetto").queryAs(Label.class).getText();
        if (elimina) {
            model.elimina("UTENTE090", "id");
        }
        assertEquals("La password inserita non è corretta. Contattare recupero.credenziali@gov.it", output);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di addetto non registrato.
     */
    void testAddettoNonRegistrato(FxRobot robot) throws SQLException {
        ModelAddetto model = ModelAddetto.getIstanza();
        boolean ripristina = false;
        String password = "";
        if(model.èPresente("UTENTE090", "nome")){
            ResultSet risultato = model.lanciaQuery("SELECT * FROM addetti WHERE nome='utente090'");
            password = risultato.getString("password");
            risultato.close();
            model.chiudiConnessione();
            model.elimina("UTENTE090", "nome");
            ripristina = true;
        }
        scriviNome(robot);
        scriviPassword(robot);
        robot.clickOn("#bottoneAccessoAddetto");
        String output = robot.lookup("#accessoErroreAddetto").queryAs(Label.class).getText();
        if(ripristina) {
            model.inserisciAddetto("UTENTE090", password);
        }
        assertEquals("L'addetto non è registrato.", output);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di credenziali corrette.
     */
    void testCredenzialiCorrette(FxRobot robot) throws SQLException {
        ModelAddetto model = ModelAddetto.getIstanza();
        boolean elimina = false;
        String password = "";
        if(!model.èPresente("UTENTE090", "nome")){
            model.inserisciAddetto("UTENTE090", "password");
            elimina = true;
            password = "password";
        }
        else {
            ResultSet risultato = model.lanciaQuery("SELECT * FROM addetti WHERE nome='UTENTE090'");
            password = risultato.getString("password");
        }
        scriviNome(robot);
        robot.clickOn("#accessoPasswordAddetto");
        robot.write(password);
        robot.clickOn("#bottoneAccessoAddetto");
        Button bottone = robot.lookup("#bottoneSottomettiDisponibilità").queryAs(Button.class);
        if(elimina) {
            model.elimina("UTENTE090", "nome");
        }
        assertNotNull(bottone);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di credenziali admin corrette.
     */
    void testAdminCorretto(FxRobot robot){
        robot.clickOn("#accessoNomeAddetto");
        robot.write("adMiN");
        robot.clickOn("#accessoPasswordAddetto");
        robot.write("admin");
        robot.clickOn("#bottoneAccessoAddetto");
        Button bottone = robot.lookup("#bottoneInserisciAddetto").queryAs(Button.class);
        assertNotNull(bottone);
    }

    @Test
    /**
     * Funzione che testa l'accesso in caso di credenziali admin errate.
     */
    void testAdminErrato(FxRobot robot) {
        robot.clickOn("#accessoNomeAddetto");
        robot.write("ADmin");
        robot.clickOn("#accessoPasswordAddetto");
        robot.write("ADMIN");
        robot.clickOn("#bottoneAccessoAddetto");
        String output = robot.lookup("#accessoErroreAddetto").queryAs(Label.class).getText();
        assertEquals("L'addetto non è registrato.", output);
    }

    @Test
    /**
     * Funzione che testa il bottone per tornare indietro.
     */
    void testBottoneIndietro(FxRobot robot) {
        robot.clickOn("#bottoneIndietroAccessoAddetto");
        Button bottone = robot.lookup("#interfacciaAddetto").queryAs(Button.class);
        assertNotNull(bottone);
    }

    /**
     * Funzione che scrive il nome dell'addetto.
     */
    private void scriviNome(FxRobot robot) {
        robot.clickOn("#accessoNomeAddetto");
        robot.write("utente090");
    }

    /**
     * Funzione che scrive la password dell'addetto.
     */
    private void scriviPassword(FxRobot robot) {
        robot.clickOn("#accessoPasswordAddetto");
        robot.write("password");
    }
}