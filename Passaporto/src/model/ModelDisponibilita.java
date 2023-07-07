package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ModelDisponibilita extends Model {
	private static ModelDisponibilita istanza_unica = null;
	/**
	 * Costruttore di model
	 */
	private ModelDisponibilita() throws SQLException {
		super();
	}

	/**
	 * Funzione che restituisce l'istanza unica del model
	 * @return Oggetto ModelDisponibilita
	 */
	public static ModelDisponibilita getIstanza() throws SQLException {
		if (istanza_unica == null)
			istanza_unica = new ModelDisponibilita();
		return istanza_unica;
	}

	/**
	 * Funzione che permette d'inserire una disponibilità nel database
	 * @param addetto l'addetto della disponibilità
	 * @param tipologia la tipologia di disponibilità
	 * @param giorno il giorno della disponibilità
	 * @param orario l'orario della disponibilità
	 * @param sede la sede della disponibilità
	 * @throws SQLException
	 */
	public void inserisciDisponibilità(String addetto, String tipologia, String giorno, String orario, String sede) throws SQLException {
		int occorrenze = ottieniOccorrenze(addetto,tipologia, giorno, orario, sede);
		String istruzione;
		if(occorrenze == -1) {
			log("La disponibilità non esiste, preparo l'istruzione di inserimento...");
			istruzione = "INSERT INTO disponibilita (addetto, tipologia, giorno, orario, sede) VALUES ('"
					+ addetto +"', '" + tipologia + "', '" + giorno + "', '" + orario + "', '" + sede + "')";
		}
		else{
			log("La disponibilità esiste, preparo l'istruzione di aggiornamento...");
			occorrenze++;
			istruzione = "UPDATE disponibilita SET occorrenze = " + occorrenze + " WHERE " + "addetto = '" + addetto + "' AND " +
					"tipologia = '" + tipologia + "' AND giorno = '" + giorno +
					"' AND orario = '" + orario + "' AND sede = '" + sede + "'";
		}
		try {
			eseguiIstruzione(istruzione);
		} catch (SQLException e) {
			log(e.getMessage());
			throw new SQLException(e.getMessage());
		}
		finally {
			chiudiConnessione();
		}
	}

	/**
	 * Funzione che permette di ottenere l'occorrenza di una disponibilità
	 * @param addetto l'addetto della disponibilità
	 * @param tipologia la tipologia di disponibilità
	 * @param giorno il giorno della disponibilità
	 * @param orario l'orario della disponibilità
	 * @param sede la sede della disponibilità
	 * @return l'occorrenza della disponibilità
	 * @throws SQLException
	 */
	private int ottieniOccorrenze(String addetto, String tipologia, String giorno, String orario, String sede) throws SQLException {
		log("Preparo query per verificare l'esistenza della disponibilità...");
		String query = "SELECT * FROM disponibilita WHERE " + "addetto = '" + addetto + "' AND " +
				"tipologia = '" + tipologia + "' AND giorno = '" + giorno +
				"' AND orario = '" + orario + "' AND sede = '" + sede + "'";
		log("Eseguo query...");
		try {
			ResultSet rs = lanciaQuery(query);
			if(rs.next()) {
				int occorrenze = rs.getInt("occorrenze");
				rs.close();
				chiudiConnessione();
				return occorrenze;
			}
			else{
				rs.close();
				chiudiConnessione();
				return -1;
			}
		} catch (SQLException e) {
			log(e.getMessage());
			throw new SQLException(e);
		}
	}

	/**
	 * Funzione che permette di ottenere l'id di una disponibilità
	 * @param addetto l'addetto della disponibilità
	 * @param tipologia la tipologia di disponibilità
	 * @param giorno il giorno della disponibilità
	 * @param orario l'orario della disponibilità
	 * @param sede la sede della disponibilità
	 * @return l'id della disponibilità
	 * @throws SQLException
	 */
	public int ottieniId(String addetto, String tipologia, String giorno, String orario, String sede) throws SQLException {
		log("Preparo query per ottenere l'id della disponibilità...");
		String query;
		if(addetto != null)
			query = "SELECT * FROM disponibilita WHERE " + "addetto = '" + addetto + "' AND " +
				"tipologia = '" + tipologia + "' AND giorno = '" + giorno +
				"' AND orario = '" + orario + "' AND sede = '" + sede + "'";
		else
			query = "SELECT * FROM disponibilita WHERE " + "tipologia = '" + tipologia + "' AND giorno = '" + giorno +
			"' AND orario = '" + orario + "' AND sede = '" + sede + "'";

		log("Eseguo query...");
		ResultSet rs = lanciaQuery(query);
		if (rs.next()) {
			log("Ottengo l'id...");
			int id = rs.getInt("id");
			rs.close();
			chiudiConnessione();
			return id;
		}
		else {
			log("Non presente");
			rs.close();
			chiudiConnessione();
			return -1;
		}
	}

	/**
	 * Funzione che permette di ottenere la lista di tutte le disponibilità.
	 * @param addetto l'addetto della disponibilità
	 * @return le informazioni delle disponibilità
	 * @throws SQLException
	 */
	public LinkedList<String> ottieniDisponibilitaAddetto(String addetto) throws SQLException {
		log("Preparo query per ottenere le informazioni della disponibilita...");
		String query = "SELECT * FROM disponibilita WHERE " + "addetto = '" + addetto + "'";
		LinkedList<String> lista = new LinkedList<String>();
		log("Eseguo query...");
		try {
			ResultSet rs = lanciaQuery(query);
			while (rs.next()) {
				log("Ottengo l'id...");
				String tipologia = rs.getString("tipologia");
				String sede = rs.getString("sede");
				String giorno = rs.getString("giorno");
				String orario = rs.getString("orario");
				String occorrenze = rs.getString("occorrenze");
				lista.add(tipologia + " - " + sede + " - " + giorno + " - " + orario + " - " + occorrenze);
			}
		} catch (SQLException e) {
			log(e.getMessage());
			throw new SQLException(e);
		}
		finally {
			chiudiConnessione();
		}
		return lista;
	}

	/**
	 * Metodo che permette di ottenere la città in cui si trova l'addetto in un determinato giorno.
	 * @param addetto l'addetto di cui si vuole sapere la città.
	 * @param giorno il giorno in cui si vuole sapere la città.
	 * @return la città in cui si trova l'addetto. O null in caso non esista.
	 */
	public String ottieniCitta(String addetto, String giorno) throws SQLException {
		log("Ottengo le disponibilità dell'addetto...");
		LinkedList<String> lista = ottieniDisponibilitaAddetto(addetto);
		log("Cerco la disponibilità del giorno...");
		for(String disponibilita : lista) {
			String[] info = disponibilita.split(" - "); //Divico gli elementi della disponibilità
			if(info[2].equals(giorno)) //Se il giorno è quello cercato
				return info[1]; //Ritorno la città
		}
		return null;
	}

	@Override
	/**
	 * {@inheritDoc}
	 * La tabella in cui viene ricercato l'elemento è disponibilità.
	 * NON USARE.
	 */
	public boolean èPresente(String elemento, String primaryKey) throws SQLException {
		log("Preparo la query...");
		String query = "SELECT * FROM disponibilita WHERE " + primaryKey + " = '" + elemento + "'";
		log("Eseguo la query...");
		ResultSet rs = lanciaQuery(query);
		log("Verifico l'esistenza dell'elemento e il suo numero di occorrenze...");
		if (rs.next()) {
			log("Presente");
			rs.close();
			chiudiConnessione();
			return true;
		}
		log("Non presente");
		rs.close();
		chiudiConnessione();
		return false;
	}

	@Override
	/**
	 * {@inheritDoc}
	 * La tabella in cui viene ricercato l'elemento è disponibilità.
	 * La key è l'id, l'elemento è il nr° dell'id.
	 * La disponibilità viene eliminata esclusivamente se il nr° di occorrenze è 0.
	 * Altrimenti viene diminuito di 1 il nr° di occorrenze.
	 */
	public boolean elimina(String elemento, String key) throws SQLException {
		log("Preparo la query...");
		String query = "SELECT * FROM disponibilita WHERE " + key + " = '" + elemento + "'";
		log("Eseguo la query...");
		ResultSet rs = lanciaQuery(query);
		log("Verifico l'esistenza dell'elemento e il suo numero di occorrenze...");
		boolean presente = rs.next();
		int occorrenze = rs.getInt("occorrenze");
		String istruzione = "";
		if (presente && occorrenze > 0) {
			log("Diminuisco di 1 l'occorrenza");
			occorrenze--;
			log("Preparo l'istruzione di aggiornamento...");
			istruzione = "UPDATE disponibilita SET occorrenze = " + occorrenze + " WHERE " + key + " = '" + elemento + "'";
		}
		else if(presente && occorrenze == 0){
			istruzione = "DELETE FROM disponibilita WHERE " + key + " = '" + elemento + "'";
		}
		else {
			log("Non presente");
			return false;
		}
		try {
			rs.close();
			chiudiConnessione();
			eseguiIstruzione(istruzione);
		}
		catch (SQLException e) {
			log(e.getMessage());
			throw new SQLException(e.getMessage());
		}
		return true;

	}

	@Override
	public void log(String s){super.log("[ModelDisponibilita]: " + s);}
}
