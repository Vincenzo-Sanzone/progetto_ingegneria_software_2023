package controller.addetto;

import controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ModelAddetto;

import java.io.IOException;
import java.sql.SQLException;

public class PannelloAdmin extends Controller {
    @FXML
    private Label registrazioneErroreAddetto;
    @FXML
    private TextField registrazioneNomeAddetto;
    @FXML
    private TextField registrazionePasswordAddetto;

    /**
     * Funzione quando l'admin sottomette la registrazione.
     * Chiama inserisciAddetto.
     */
    @FXML
    protected void adminInserisciAddetto(ActionEvent evento){
        inserisciAddetto(evento);
    }


    /**
     * Funzione quando l'admin clicca invio.
     * Chiama inserisciAddetto.
     */
    @FXML
    protected void adminInserisciAddettoEnter(KeyEvent evento) {
        if(evento.getCode().equals(KeyCode.ENTER))
            inserisciAddetto(evento);
    }

    /**
     * Funzione che inserisce l'addetto nel database, dopo essersi assicurato della validità.
     * @param evento evento che ha causato l'invocazione della funzione.
     */
    private void inserisciAddetto(Event evento){
        log("Converto da testo a stringa...");
        String nome = registrazioneNomeAddetto.getText().toUpperCase();
        String password = registrazionePasswordAddetto.getText();
        log("Verifico se i campi non sono vuoti...");
        if(nome.isEmpty() || password.isEmpty()){
            log("Inserisco il messaggio d'errore...\n");
            registrazioneErroreAddetto.setText("Nome o password non inserita. Si prega di riprovare.");
            return;
        }
        log("Verifico la lunghezza del nome e della password...");
        if(nome.length() > 255 || password.length() > 255){
            log("Inserisco il messaggio d'errore...\n");
            registrazioneErroreAddetto.setText("Nome o password troppo lunga. Si prega di riprovare.");
            return;
        }
        log("Verifico se il nome è diverso da admin...");
        if(nome.equalsIgnoreCase("admin")){
            log("Inserisco il messaggio d'errore...\n");
            registrazioneErroreAddetto.setText("Impossibile registrare l'admin. Si prega di riprovare.");
            return;
        }
        try {
            log("Ottengo l'istanza del modello...");
            ModelAddetto model = ModelAddetto.getIstanza();
            log("Verifico se il nome è già presente...");
            if(model.èPresente(nome, "nome")){
                log("Inserisco il messaggio d'errore...\n");
                registrazioneErroreAddetto.setText("Nome già esistente. Si prega di riprovare.");
                return;
            }
            log("Inserisco l'addetto nel database...");
            model.inserisciAddetto(nome, password);
            pulisciCampi();
            log("Inserisco il messaggio di successo...\n");
            registrazioneErroreAddetto.setText("Registrazione avvenuta con successo.");
        }
        catch (SQLException e) {
            log(e.getMessage());
            pulisciCampi();
            registrazioneErroreAddetto.setText("Problemi durante l'accesso al database.");
        }
    }

    /**
     * Funzione che ripulisce i campi.
     */
    private void pulisciCampi() {
        log("Pulisco i campi...");
        registrazioneNomeAddetto.setText("");
        registrazionePasswordAddetto.setText("");
    }


    /**
     * Funzione quando l'admin clicca il bottone torna indietro.
     */
    @FXML
    protected void tornaIndietroAdmin(ActionEvent evento) {
        pulisciCampi();
        try {
            log("Cambio la scena...");
            super.changeView("addetto/accesso-addetto.fxml", evento, "Accesso addetto");
        } catch (IOException e) {
            log(e.getMessage());
            registrazioneErroreAddetto.setText("Problemi durante l'accesso alla pagina.");
        }
    }

    @Override
    public void log(String s){super.log("[PannelloAdmin]: " + s);}
}
