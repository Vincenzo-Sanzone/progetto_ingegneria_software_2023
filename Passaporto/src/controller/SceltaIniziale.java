package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class SceltaIniziale extends Controller{
    @FXML
    private Label erroreSceltaIniziale;


    /**
     Metodo che permette di accedere alla schermata di login per l'addetto
     */
    @FXML
    protected void visioneAddetto(ActionEvent evento){
        log("Bottone addetto cliccato");
        log("Cambio visuale...");
        try{
            super.changeView("addetto/accesso-addetto.fxml", evento, "Accesso addetto");
        }
        catch(IOException e){
            log(e.toString());
            erroreSceltaIniziale.setText("Errore interno. Si prega di riprovare.");
        }
    }

    /**
     Metodo che permette di accedere alla schermata di login per il cittadino
     */
    @FXML
    protected void visioneCittadino(ActionEvent evento){
        log("Bottone cittadino cliccato");
        log("Cambio visuale...");
        try{
            super.changeView("cittadino/accesso-cittadino.fxml", evento, "Accesso cittadino");
        }
        catch(IOException e){
            log(e.toString());
            erroreSceltaIniziale.setText("Errore interno. Si prega di riprovare.");
        }
    }
}
