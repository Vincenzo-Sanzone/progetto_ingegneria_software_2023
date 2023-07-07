package controller;

public class UtenteLog {
	private String identificativo = null;
	private static UtenteLog utente = null;

	/**
	 * Costruttore della classe controller.UtenteLog.
	 * @param identificativo identificativo dell'utente.
	 */
	public UtenteLog(String identificativo) {
		this.identificativo = identificativo;
	}

	/**
	 * Funzione che ritorna il codice fiscale dell'utente.
	 * @return identificativo dell'utente.
	 */
	public String getIdentificativo() {
		return identificativo;
	}

	/**
	 * Funzione che ritorna l'utente loggato.
	 * @param identificativo codice fiscale dell'utente.
	 * @return utente loggato.
	 */
	public static UtenteLog getUtente(String identificativo) {
		if(utente == null && identificativo != null)
			utente = new UtenteLog(identificativo);
		if(utente == null)
			throw new NullPointerException("Utente non loggato");
		return utente;
	}

	/**
	 * Funzione che effettua il logout dell'utente.
	 */
	public void logOut() {
		identificativo = null;
		utente = null;
	}
}
