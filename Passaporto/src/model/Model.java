package model;

import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.time.LocalDate;

public abstract class Model {
	private Connection connessione;

	/**
	 * Metodo che permette di stampare un messaggio di log
	 * @param s messaggio da stampare
	 */
	protected void log(String s){
		System.out.println("LOG: " + s);
	}

	/**
	 * Costruttore di model
	 */
	protected Model() throws SQLException {
		connetti();
		if(!tabellaEsiste("cittadini"))
			resettaTabellaCittadini();
		if(!tabellaEsiste("disponibilita"))
			resettaTabellaDisponibilita();
		if(!tabellaEsiste("addetti"))
			resettaTabellaAddetti();
		if(!tabellaEsiste("prenotazioni"))
			resettaTabellaPrenotazioni();
	}

	/**
	 * Funzione che ricrea la tabella addetti.
	 */
	protected void resettaTabellaAddetti() throws SQLException {
		log("Inizio reset tabella addetti...");
		String istruzione = "DROP TABLE IF EXISTS addetti;" +
				"CREATE TABLE addetti( " +
				"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"nome VARCHAR(255) unique, " +
				"password VARCHAR(255) );";
		eseguiIstruzione(istruzione);
	}

	/**
	 * Funzione che ricrea la tabella disponibilita.
	 */
	protected void resettaTabellaDisponibilita() throws SQLException {
		log("Inizio reset tabella disponibilità...");
		String istruzione = "DROP TABLE IF EXISTS disponibilita;" +
				"CREATE TABLE disponibilita( " +
				"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"addetto VARCHAR(255), " +
				"tipologia VARCHAR(255), " +
				"giorno VARCHAR(10), " +
				"orario VARCHAR(11), "+
				"sede VARCHAR(255), " +
				"occorrenze INTEGER DEFAULT 1 );";
		eseguiIstruzione(istruzione);
	}

	/**
	 * Funzione che ricrea la tabella cittadini.
	 */
	protected void resettaTabellaCittadini() throws SQLException {
		log("Inizio reset tabella cittadini...");
		String istruzione = "DROP TABLE IF EXISTS cittadini;" +
							"CREATE TABLE cittadini( " +
							"nome VARCHAR(255), " +
							"cognome VARCHAR(255), " +
							"dataNascita VARCHAR(10), " +
							"luogoNascita VARCHAR(255), " +
							"codiceFiscale VARCHAR(16) PRIMARY KEY UNIQUE, " +
							"tesseraSanitaria VARCHAR(20) UNIQUE, " +
                            "password VARCHAR(255) );";
		eseguiIstruzione(istruzione);
	}

	/**
	 * Funzione che ricrea la tabella prenotazioni.
	 */
	protected void resettaTabellaPrenotazioni() throws SQLException {
		log("Inizio reset tabella prenotazioni...");
		String istruzione = "DROP TABLE IF EXISTS prenotazioni;"+
							"CREATE TABLE prenotazioni( " +
							"idPrenotazione INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"codiceFiscale VARCHAR(16), " +
							"idDisponibilita INTEGER, " +
							"dichiarazione VARCHAR(255), " +
							"FOREIGN KEY(codiceFiscale) REFERENCES cittadini(codiceFiscale), " +
							"FOREIGN KEY(idDisponibilita) REFERENCES disponibilita(id) );";
		eseguiIstruzione(istruzione);
	}

	/**
	 * Funzione che esegue un'istruzione.
	 * @param istruzione l'istruzione da eseguire
	 */
	public void eseguiIstruzione(String istruzione) throws SQLException {
		connetti();
		log("Creo un istruzione SQL...");
		Statement is = connessione.createStatement();
		log("Eseguo l'istruzione: " + istruzione + " ...");
		is.executeUpdate(istruzione);
		log("Istruzione eseguita.");
		is.close();
		chiudiConnessione();
	}

	public void chiudiConnessione() throws SQLException {
		try {
			if(connessione != null) {
				log("Chiudo la connessione al database...");
				connessione.close();
				connessione = null;
			}
		} catch (SQLException e) {
			log(e.getMessage());
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Funzione per connettersi al database
	 */
	protected void connetti() throws SQLException {
		if(connessione == null) {
			log("Mi connetto al database...");
			connessione = DriverManager.getConnection("jdbc:sqlite:passaporto.db");
			log("Connessione riuscita.");
		}
	}

	/**
	 * Funzione che esegue una query.
	 * @param query la query da eseguire.
	 * @return ResultSet il risultato della query.
	 */
	public ResultSet lanciaQuery(String query) throws SQLException {
		connetti();
		ResultSet risultato = null;
		log("Creo una istruzione SQL...");
		Statement istruzione = connessione.createStatement();
		log("Eseguo la query: " + query + "...");
		risultato = istruzione.executeQuery(query);
		return risultato;
	}

	/**
	 * Funzione che verifica l'esistenza di una tabella
	 * @param nomeTabella il nome della tabella di cui si vuole verificare l'esistenza.
	 * @return true se la tabella esiste, false altrimenti
	 */
	public boolean tabellaEsiste(String nomeTabella) throws SQLException {
		log("Verifico l'esistenza della tabella " + nomeTabella + "...");
		String query = "SELECT * FROM sqlite_master WHERE tbl_name = '" + nomeTabella + "'";
		ResultSet risultato = lanciaQuery(query);
		boolean esiste = risultato.next();
		log("La tabella " + nomeTabella + (esiste ? "" : " non") + " esiste");
		return esiste;
	}



	/**
	 * Funzione per verificare la presenza di un elemento in una tabella.
	 * ASSUME: la tabella esiste.
	 * @param elemento l'elemento da cercare.
	 * @param primaryKey la chiave primaria della tabella.
	 * @return true se è presente, false altrimenti.
	 */
	public abstract boolean èPresente(String elemento, String primaryKey) throws SQLException;

	/**
	 * Funzione che elimina un elemento da una tabella.
	 * ASSUME: l'elemento è presente nella tabella.
	 * @param elemento l'elemento da eliminare.
	 * @param key la chiave della tabella.
	 * @return true se è stato eliminato, false altrimenti.
	 */
	public abstract boolean elimina(String elemento, String key) throws SQLException;
}
