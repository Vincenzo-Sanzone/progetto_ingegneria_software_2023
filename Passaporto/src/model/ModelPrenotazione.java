package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ModelPrenotazione extends Model {
	private static ModelPrenotazione istanza_unica = null;

	/**
	 * Costruttore di model
	 */
	private ModelPrenotazione() throws SQLException {
		super();
	}

	/**
	 * Funzione che restituisce l'istanza unica del model
	 */
	public static ModelPrenotazione getIstanza() throws SQLException {
		if(istanza_unica == null)
			istanza_unica = new ModelPrenotazione();
		return istanza_unica;
	}

	/**
	 * Funzionce che inserisce una prenotazione nel database.
	 * @param codiceFiscale codice fiscale dell'utente che ha prenotato.
	 * @param idDisponibilita id della disponibilità prenotata.
	 * @throws SQLException
	 * @throws RuntimeException se la disponibilità è 0.
	 */
	public void inserisciPrenotazione(String codiceFiscale, int idDisponibilita, String dichiarazione) throws SQLException {
		String query =  "SELECT * FROM disponibilita WHERE id = " + idDisponibilita + ";";
		ResultSet rs = lanciaQuery(query);
		rs.next();
		int occorrenze = rs.getInt("occorrenze");
		if(occorrenze == 0){
			rs.close();
			chiudiConnessione();
			log("Errore: disponibilità non presente.");
			throw new RuntimeException("Disponibilità non presente.");
		}
		log("Elimino l'occorrenza della disponibilità...");
		rs.close();
		chiudiConnessione();
		ModelDisponibilita.getIstanza().elimina(String.valueOf(idDisponibilita), "id");
		log("Preparo l'istruzione di inserimento...");
		String istruzione = "INSERT INTO prenotazioni (codiceFiscale, idDisponibilita, dichiarazione) VALUES ('"
				+ codiceFiscale + "', " + idDisponibilita + ", '" + dichiarazione + "');";
		eseguiIstruzione(istruzione);
	}

	/**
	 * Funzione che restituisce l'id di una prenotazione
	 * @param codiceFiscale codice fiscale dell'utente che ha prenotato.
	 * @param idDisponibilita id della disponibilità prenotata.
	 * @return id della prenotazione.
	 * @throws SQLException
	 */
	public int ottieniId(String codiceFiscale, int idDisponibilita) throws SQLException {
		String query = "SELECT * FROM prenotazioni WHERE codiceFiscale = '" + codiceFiscale + "' AND idDisponibilita = " + idDisponibilita + ";";
		ResultSet rs = lanciaQuery(query);
		if(rs.next()){
			log("Prenotazione trovata.");
			log("Ottengo l'id...");
			int id = rs.getInt("idPrenotazione");
			rs.close();
			chiudiConnessione();
			return id;
		}
		log("Prenotazione non trovata.");
		return -1;
	}

	/**
	 * Funzione che restituisce il numero di prenotazioni per una disponibilità.
	 * @param idDisponibilita id della disponibilità.
	 * @return
	 */
	public int ottieniNumeroPrenotazioni(int idDisponibilita){
		log("Ottengo il numero di prenotazioni per la disponibilità " + idDisponibilita + "...");
		String query = "SELECT * FROM prenotazioni WHERE idDisponibilita = " + idDisponibilita + ";";
		try {
			ResultSet rs = lanciaQuery(query);
			int prenotate = 0;
			while(rs.next())
				prenotate++;
			rs.close();
			chiudiConnessione();
			return prenotate;
		} catch (SQLException e) {
			log(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Funzione che restituisce le prenotazioni di un cittadino.
	 * @param codiceFiscale codice fiscale dell'utente.
	 * @return lista delle prenotazioni.
	 */
	public LinkedList<String> ottieniPrenotazioniCittadino(String codiceFiscale){
		LinkedList<String> disponibilita = new LinkedList<String>();
		String query = "SELECT * FROM prenotazioni WHERE codiceFiscale = '" + codiceFiscale + "';";
		try {
			ResultSet rs = lanciaQuery(query);
			while(rs.next()){
				int idDisponibilita = rs.getInt("idDisponibilita");
				String query2 = "SELECT * FROM disponibilita WHERE id = " + idDisponibilita + ";";
				ResultSet rs2 = lanciaQuery(query2);
				rs2.next();
				String tipologia = rs2.getString("tipologia");
				String sede = rs2.getString("sede");
				String data = rs2.getString("giorno");
				String ora = rs2.getString("orario");
				disponibilita.add(tipologia + " - " + sede + " - " + data + " - " + ora);
				rs2.close();
			}
			rs.close();
			chiudiConnessione();
		} catch (SQLException e) {
			log(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		return disponibilita;
	}

	@Override
	/**
	 * {@inheritDoc}
	 * La tabella in cui viene fatta la ricerca è prenotazione.
	 */
	public boolean èPresente(String elemento, String primaryKey) throws SQLException {
		String query = "SELECT * FROM prenotazioni WHERE " + primaryKey + " = '" + elemento + "';";
		ResultSet risultato = lanciaQuery(query);
		boolean presente = risultato.next();
		risultato.close();
		chiudiConnessione();
		return presente;
	}

	@Override
	/**
	 * {@inheritDoc}
	 * La tabella in cui viene fatta la ricerca è prenotazione.
	 */
	public boolean elimina(String elemento, String key) throws SQLException {
		if(!èPresente(elemento, key))
			return false;
		String istruzione = "DELETE FROM prenotazioni WHERE " + key + " = '" + elemento + "';";
		eseguiIstruzione(istruzione);
		return true;
	}

	@Override
	public void log(String s){super.log("[ModelPrenotazione]: " + s);}
}
