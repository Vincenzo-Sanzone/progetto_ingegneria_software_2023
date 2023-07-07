package model;

import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ModelCittadino extends Model {
    private static ModelCittadino istanzaUnica = null;

    /**
     * Costruttore di model
     */
    private ModelCittadino() throws Exception {
        super();
    }

    /**
     * Metodo che permette di ottenere l'istanza della classe
     * @return l'istanza della classe.
     * @throws Exception se si verifica un errore.
     */
    public static ModelCittadino getIstanza() throws Exception {
        if(istanzaUnica == null)
            istanzaUnica = new ModelCittadino();
        return istanzaUnica;
    }

    @Override
    /**
     * {@inheritDoc}
     * La tabella in cui viene ricercato l'elemento è cittadini.
     */
    public boolean èPresente(String elemento, String primaryKey) throws SQLException {
        log("Preparo la query...");
        String query = "SELECT * FROM cittadini WHERE " + primaryKey + " = '" + elemento + "'";
        try {
            ResultSet risultato = lanciaQuery(query);
            return risultato.next();
        }
        catch (SQLiteException e){
            if(e.getResultCode() == SQLiteErrorCode.SQLITE_NOTFOUND)
                return false;
            else
                throw new SQLiteException(e.getMessage(), e.getResultCode());
        }
        catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    /**
     * {@inheritDoc}
     * La tabella in cui viene ricercato l'elemento è cittadini.
     */
    public boolean elimina(String elemento, String key) throws SQLException {
        log("Preparo l'istruzione...");
        String istruzione = "DELETE FROM cittadini WHERE " + key + " = '" + elemento + "'";
        try {
            eseguiIstruzione(istruzione);
            return true;
        }
        catch (SQLiteException e) {
            if(e.getResultCode() == SQLiteErrorCode.SQLITE_NOTFOUND)
                return false;
            else
                throw new SQLiteException(e.getMessage(), e.getResultCode());
        }
        catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Funzione che verifica se la password inserita è corretta.
     * ASSUME: il cittadino è presente nel database
     * @param codiceFiscale il codice fiscale del cittadino
     * @param password la password inserita dal cittadino
     * @return true se la password è corretta, false altrimenti
     */
    public boolean verificaPassword(String codiceFiscale, String password) throws SQLException {
        log("Preparo la query...");
        String query = "SELECT password FROM cittadini WHERE codiceFiscale = '" + codiceFiscale + "'";
        try {
            ResultSet risultato = lanciaQuery(query);
            risultato.next();
            return risultato.getString("password").equals(password);
        }
        catch (SQLiteException e) {
            if(e.getResultCode() == SQLiteErrorCode.SQLITE_NOTFOUND)
                return false;
            else
                throw new SQLiteException(e.getMessage(), e.getResultCode());
        }
        catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Funzione che inserisce un cittadino nel suo database.
     * @param nome il nome del cittadino
     * @param cognome il cognome del cittadino
     * @param dataNascita il giorno in cui è nato il cittadino
     * @param luogoNascita la città in cui è nato il cittadino
     * @param codiceFiscale il codice fiscale del cittadino
     * @param password la password che il cittadino userà per accedere
	 */
    public void inserisciCittadino(String nome, String cognome, String dataNascita, String luogoNascita,
                                   String codiceFiscale, String tesseraSanitaria,String password) throws SQLException {
        String istruzione = "INSERT INTO cittadini(nome, cognome, dataNascita, luogoNascita, codiceFiscale, tesseraSanitaria, password)\n" +
                "VALUES ('" + nome + "', '" + cognome + "', " + dataNascita + " , '" +
                luogoNascita + "', '" + codiceFiscale + "', '" + tesseraSanitaria + "', '" + password + "')";
        eseguiIstruzione(istruzione);
    }

    @Override
    public void log(String s){super.log("[ModelCittadino]: " + s);}
}
