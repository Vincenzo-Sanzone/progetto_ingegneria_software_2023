package controller.cittadino;

import controller.Controller;
import controller.UtenteLog;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ModelCittadino;

import java.io.IOException;

public class AccessoCittadino extends Controller {
    @FXML
    private TextField accessoCodiceFiscaleUtente;
    @FXML
    private TextField accessoPasswordUtente;
    @FXML
    private Label accessoErroreUtente;
    @FXML
    private Label logUtente;

    @FXML
    /**
     * Funzione quando l'utente sottomette l'accesso, tramite il bottone.
     * Chiama accediUtente.
     */
    protected void sottomettiAccessoUtente(ActionEvent evento){
        accediUtente(evento);
    }



    /**
     * Funzione quando l'utente preme invio.
     * Chiama accediUtente.
     */
    @FXML
    protected void invioAccessoUtente(KeyEvent evento) {
        if(evento.getCode().equals(KeyCode.ENTER))
            accediUtente(evento);
    }

    /**
     * Funzione che permette all'utente di accedere al sistema, dopo aver verificato la correttezza dei dati inseriti.
     */
    private void accediUtente(Event evento){
        log("Converto da testo a stringa...");
        String codiceFiscale = accessoCodiceFiscaleUtente.getText().toUpperCase();
        String password = accessoPasswordUtente.getText();

        log("Verifico se i campi non sono vuoti...");
        if(codiceFiscale.isEmpty() || password.isEmpty()){
            log("Imposto testo...\n");
            accessoErroreUtente.setText("Codice fiscale o password non inserita. Si prega di riprovare.");
            return;
        }

        log("Verifico credenziali...");
        try{
            ModelCittadino modelCittadino = ModelCittadino.getIstanza();
            log("Verifico se l'utente è presente nel database...");
            if(!modelCittadino.èPresente(codiceFiscale, "codiceFiscale")){
                log("L'utente non è presente nel database.");
                log("Inserisco il messaggio d'errore...\n");
                accessoErroreUtente.setText("L'utente non è registrato. O il codice fiscale è errato.");
                accessoCodiceFiscaleUtente.clear();
                return;

            }
            log("Verifico se la password è corretta...");
            if(!modelCittadino.verificaPassword(codiceFiscale, password)){
                log("La password non è corretta.");
                log("Inserisco il messaggio d'errore...\n");
                accessoErroreUtente.setText("La password inserita non è corretta. Contattare recupero.credenziali@gov.it");
                accessoPasswordUtente.clear();
                return;
            }
            log("La password è corretta.");
            log("Accesso in corso.\n");
            UtenteLog.getUtente(codiceFiscale);
            modelCittadino.chiudiConnessione();
            super.changeView("cittadino/scelta-servizio-cittadino.fxml", evento, "Scegli il servizio");
        }
        catch (IOException e){
            log(e.getMessage());
            accessoErroreUtente.setText("Problemi durante l'accesso alla pagina.");
        }
        catch (Exception e){
            log(e.toString());
            accessoErroreUtente.setText("Problemi durante l'accesso al database.");
        }
    }


    /**
     * Funzione quando l'utente clicca su registrati.
     */
    @FXML
    protected void registraUtente(ActionEvent evento){
        log("Registra utente cliccato.");
        log("Cambio scena...");
        try {
            super.changeView("cittadino/registrazione-cittadino.fxml", evento, "Registra cittadino");
        }
        catch(Exception e){
            log(e.toString());
            accessoErroreUtente.setText("Problemi durante il cambio scena.");
        }
    }

    /**
     * Funzione quando l'utente clicca su torna indietro.
     */
    @FXML
    protected void tornaIndietroAccessoUtente(ActionEvent evento) {
        log("Torna indietro cliccato.");
        log("Cambio scena...");

        try {
            super.changeView("scelta-iniziale.fxml", evento, "Scelta iniziale");
        } catch (IOException e) {
            log(e.toString());
            accessoErroreUtente.setText("Problemi durante l'accesso alla pagina.");
        }
    }

    @Override
    public void log(String s){super.log("[AccessoCittadino]: " + s);}
}
