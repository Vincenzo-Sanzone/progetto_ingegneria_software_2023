package controller.addetto;

import controller.Controller;
import controller.UtenteLog;
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

public class    AccessoAddetto extends Controller {
    @FXML
    private Label accessoErroreAddetto;
    @FXML
    private TextField accessoNomeAddetto;
    @FXML
    private TextField accessoPasswordAddetto;


    /**
     * Funzione quando l'addetto sottomette l'accesso.
     * Chiama accediAddetto.
     */
    @FXML
    protected void sottomettiAccessoAddetto(ActionEvent evento) {
        accediAddetto(evento);
    }


    /**
     * Funzione quando l'addetto preme invio.
     * Chiama accediAddetto.
     */
    @FXML
    protected void invioAccessoAddetto(KeyEvent evento) {
        if (evento.getCode().equals(KeyCode.ENTER))
            accediAddetto(evento);
    }

    /**
     * Funzione che permette all'addetto di accedere al sistema, dopo aver verificato la validità dei dati.
     * @param evento evento che ha portato alla chiamata della funzione
     */
    private void accediAddetto(Event evento){
        log("Converto da testo a stringa...");
        String nome = accessoNomeAddetto.getText().toUpperCase();
        String password = accessoPasswordAddetto.getText();

        log("Verifico se i campi non sono vuoti...");
        if (nome.isEmpty() || password.isEmpty()) {
            log("Imposto testo...");
            accessoErroreAddetto.setText("Nome o password non inserita. Si prega di riprovare.");
            log("Ripulisco il nome...");
            accessoNomeAddetto.setText("");
            log("Ripulisco la password...\n");
            accessoPasswordAddetto.setText("");
            return;
        }

        log("Verifico se l'accesso è da parte del admin...");
        if (nome.equals("ADMIN") && password.equals("admin")) {
            log("L'accesso è da parte del admin.");
            log("Accesso in corso.\n");
            try {
                super.changeView("addetto/pannello-admin.fxml", evento, "Pannello admin");
            } catch (IOException e) {
                log(e.getMessage());
                accessoErroreAddetto.setText("Problemi durante l'accesso alla pagina.");
            }
            return;
        }

        log("Verifico credenziali...");
        try {
            ModelAddetto model = ModelAddetto.getIstanza();
            log("Verifico se l'addetto è presente nel database...");
            if (!model.èPresente(nome, "nome")) {
                log("L'addetto non è presente nel database.");
                log("Inserisco il messaggio d'errore...\n");
                accessoErroreAddetto.setText("L'addetto non è registrato.");
                return;

            }
            log("Verifico se la password è corretta...");
            if (!model.verificaPassword(nome, password)) {
                log("La password non è corretta.");
                log("Inserisco il messaggio d'errore...\n");
                accessoErroreAddetto.setText("La password inserita non è corretta. Contattare recupero.credenziali@gov.it");
                return;
            }
            log("La password è corretta.");
            log("Accesso in corso.\n");
            model.chiudiConnessione();
            log("Recupero l'utente...");
            UtenteLog.getUtente(nome);
            super.changeView("addetto/inserisci-disponibilita.fxml", evento, "Inserisci disponibilità");

        } catch (SQLException e) {
            log(e.getMessage());
            accessoErroreAddetto.setText("Problemi durante l'accesso al database.");
        } catch (IOException e) {
            log(e.getMessage());
            accessoErroreAddetto.setText("Problemi durante l'accesso alla pagina.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Funzione quando l'addetto clicca sul pulsante per tornare indietro.
     */
    @FXML
    protected void tornaIndietroAccessoAddetto(ActionEvent evento) {
        log("Bottone 'Torna indietro' premuto.");
        try {
            super.changeView("scelta-iniziale.fxml", evento, "Scelta iniziale");
        } catch (IOException e) {
            log(e.getMessage());
            accessoErroreAddetto.setText("Problemi durante l'accesso alla pagina.");
        }
    }

    @Override
    public void log(String s){super.log("[AccessoAddetto]: " + s);}
}