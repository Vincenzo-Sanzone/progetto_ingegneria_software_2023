package model;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;

public class ElencoCitta {
	private static ElencoCitta istanza_unica = null;
	private boolean corretto = false;
	private HashMap<String, String> mappa;

	private void log(String s){System.out.println("LOG: [ElencoCitta] " + s);}
	/**
	 * Costruttore di model
	 */
	private ElencoCitta() throws RuntimeException {
		try{
			inizializza();
		}
		catch (RuntimeException e){
			corretto = false;
			log(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}

	}

	/**
	 * Funzione che inizializza la tabella
	 */
	private void inizializza() throws RuntimeException {
		mappa = new HashMap<>();
		log("Procedo con l'inizializzazione...");
		log("Apro il file di configurazione");
		try(BufferedReader file = new BufferedReader(
				new FileReader("src/resources/configurazione/lista_citta.txt"))) {
			log("Inizio a leggere il file di configurazione...");
			log("Ottengo l'hashcode da verificare...");
			long hashcodeDaVerificare = parseLong(file.readLine());
			long hashcode = 0;
			log("Inizio lettura delle città...");
			String riga = file.readLine();
			while (riga != null){
				String[] riga_divisa = riga.split(Pattern.quote("|")); //Divido la riga in base al carattere |
				long attuale = calcolaHashcode(riga_divisa[0], 1) + calcolaHashcode(riga_divisa[1], 1);
				hashcode = Math.abs(attuale-hashcode); //Calcolo l'hashcode
				mappa.put(riga_divisa[0], riga_divisa[1]);
				riga = file.readLine(); //Leggo la riga successiva per assicurarmi che non sia null
			}
			log("Chiudo il file di configurazione...");
			log("Verifico l'hashcode...");
			if(hashcode != hashcodeDaVerificare) {
				log("L'hashcode trovato è " + hashcode + " mentre quello atteso è " + hashcodeDaVerificare);
				log("Elimino la tabella.");
				corretto = false;
				throw new RuntimeException("L'hashcode non corrisponde");
			}
			log("Chiudo il file...");
			file.close();
			log("L'hashcode corrisponde, gli elementi sono stati inseriti.");
			corretto = true;

		}
		catch (FileNotFoundException e) {
			log(e.getMessage());
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			log(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Funzione che calcola l'hashcode di una stringa
	 * @param stringa stringa di cui calcolare l'hashcode
	 * @return hashcode della stringa
	 */
	private long calcolaHashcode(String stringa, int eleva) {
		//Caso base
		if(stringa == null || stringa.length() == 0)
			return 1;
		//Caso ricorsivo (se la stringa inizia con un numero, allora lo considero come valore)
		if(stringa.charAt(0) >= '0' && stringa.charAt(0) <= '9')
			return Integer.valueOf(stringa);
		//Caso ricorsivo (se la stringa inizia con uno spazio, allora lo ignoro)
		if(stringa.charAt(0) < 'A' || stringa.charAt(0) > 'Z')
			return calcolaHashcode(stringa.substring(1), eleva);
		//Caso ricorsivo (se la stringa inizia con una lettera, allora calcolo l'hashcode)
		long valore = Character.getNumericValue(stringa.charAt(0)) - Character.getNumericValue('A') + 1;
		valore = (long) Math.pow(valore, eleva);
		return (valore * calcolaHashcode(stringa.substring(1), eleva+1)) % 1000000007;
	}

	/**
	 * Funzione che ritorna l'istanza dell'elenco
	 */
	public static ElencoCitta getIstanza(){
		if(istanza_unica == null)
			istanza_unica = new ElencoCitta();
		return istanza_unica;
	}

	/**
	 * Funzione che ottiene il codice di una citta/nazione
	 * @param citta nome della città di cui si vuole ottenere il codice
	 * @return codice della citta
	 */
	public String ottieniCodice(String citta) throws RuntimeException {
		if (!corretto)
			throw new RuntimeException("La tabella non è stata inizializzata correttamente.");
		if(!cittaEsiste(citta))
			return null;
		log("Ottengo il codice di " + citta + "...");
		return mappa.get(citta);
	}


	/**
	 * Funzione che verifica la presenza di "elemento"
	 * @param elemento elemento da verificare
	 * @return true se è presente, false altrimenti
	 */
	public boolean cittaEsiste(String elemento) throws RuntimeException {
		if(!corretto)
			throw new RuntimeException("La tabella non è stata inizializzata correttamente.");
		return mappa.containsKey(elemento);
	}
}
