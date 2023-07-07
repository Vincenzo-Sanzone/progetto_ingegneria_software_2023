package controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {

    /**
     * Metodo che permette di stampare un messaggio di log
     * @param s messaggio da stampare
     */
    protected void log(String s){
        System.out.println("LOG: " + s);
    }

    /**
     * Metodo che permette di cambiare la view
     * @param nomeFile nome del file fxml
     * @param evento evento che ha scatenato il cambio
     * @param titolo titolo della nuova view
     * @throws IOException se il file non viene trovato
     */
    protected void changeView(String nomeFile, Event evento, String titolo) throws IOException {
        log("Ricevuto file: " + nomeFile + " ed evento " + evento);
        log("Creo l'FXMLLoader...");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("resources/" + nomeFile));
        log("Creo lo stage...");
        Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        log("Prendo la dimensione della scena...");
        double dim_x = ((Node) evento.getSource()).getScene().getWidth();
        double dim_y = ((Node) evento.getSource()).getScene().getHeight();
        log("Creo la scena...");
        Scene scene = new Scene(fxmlLoader.load(), dim_x, dim_y);
        log("Inserisco il titolo...");
        stage.setTitle(titolo);
        log("Inserisco la scena nello stage...");
        stage.setScene(scene);
        log("Mostro la scena...");
        stage.show();
        log("Cambio effettuato.");
    }
}
