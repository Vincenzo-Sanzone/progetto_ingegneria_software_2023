package model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ModelAddetto extends Model{
    private static ModelAddetto istanza_unica = null;

    /**
     * Costruttore di model
     */
    private ModelAddetto() throws SQLException {
        super();
    }

    /**
     * Metodo che permette di ottenere l'istanza della classe
     * @return l'istanza della classe.
     * @throws SQLException se si verifica un errore.
     */
    public static ModelAddetto getIstanza() throws SQLException {
        if (istanza_unica == null)
            istanza_unica = new ModelAddetto();
        return istanza_unica;
    }

    @Override
    /**
     * {@inheritDoc}
     * La tabella in cui viene ricercato l'elemento è addetti.
     */
    public boolean èPresente(String elemento, String primaryKey) throws SQLException {
        log("Preparo la query...");
        String query = "SELECT * FROM addetti WHERE " + primaryKey + " = '" + elemento + "'";
        try {
            ResultSet rs = lanciaQuery(query);
            boolean return_value = rs.next();
            rs.close();
            chiudiConnessione();
            return return_value;
        }
        catch (SQLException e) {
            log(e.getMessage());
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    /**
     * {@inheritDoc}
     * La tabella in cui viene ricercato l'elemento è addetti.
     */
    public boolean elimina(String elemento, String key) throws SQLException {
        log("Preparo l'istruzione...");
        String istruzione = "DELETE FROM addetti WHERE " + key + " = '" + elemento + "'";
        try {
            eseguiIstruzione(istruzione);
            return true;
        }
        catch (SQLException e) {
            log(e.getMessage());
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Metodo che permette di aggiungere un addetto al database.
     * @param nome il nome dell'addetto.
     * @param password la password dell'addetto.
     */
    public void inserisciAddetto(String nome, String password) throws SQLException {
        if(nome.equalsIgnoreCase("admin"))
            throw new SQLException("Impossibile aggiungere un addetto con nome admin");
        log("Preparo l'istruzione...");
        String istruzione = "INSERT INTO addetti (nome, password) VALUES ('" + nome + "', '" + password + "')";
        try {
            eseguiIstruzione(istruzione);
        } catch (SQLException e) {
            log(e.getMessage());
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Metodo che verifica se la password è corretta.
     * @param nome il nome dell'addetto.
     * @param password la password dell'addetto.
     */
    public boolean verificaPassword(String nome, String password) throws SQLException {
        log("Preparo la query...");
        String query = "SELECT password FROM addetti WHERE nome = '" + nome + "'";
        try {
            ResultSet rs = lanciaQuery(query);
            log("Verifico la password...");
            boolean return_value = rs.getString("password").equals(password);
            rs.close();
            chiudiConnessione();
            return return_value;
        } catch (SQLException e) {
            log(e.getMessage());
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public void log(String s){super.log("[ModelAddetto]: " + s);}
}
