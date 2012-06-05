package fr.tse.lt2c.satin.bibtex2rdf.beanfier.empire;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import fr.tse.lt2c.satin.gomasio.beans.Author;
import fr.tse.lt2c.satin.gomasio.beans.Paper;
import fr.tse.lt2c.satin.gomasio.beans.ConferenceEvent;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import fr.tse.lt2c.satin.gomasio.utils.Gomasio;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clarkparsia.empire.SupportsRdfId.URIKey;

/*
 * Test it :
 * 
 * 1)
 * PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dc:<http://purl.org/dc/elements/1.1/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
 
SELECT DISTINCT ?property ?hasValue ?isValueOf WHERE { { ?made ?property
 ?hasValue } UNION { ?isValueOf ?property ?made } } ORDER BY
 (!BOUND(?hasValue)) ?property ?hasValue ?isValueOf
 * 
 * 2) PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX
 * owl:<http://www.w3.org/2002/07/owl#> PREFIX
 * xsd:<http://www.w3.org/2001/XMLSchema#> PREFIX
 * rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX
 * dc:<http://purl.org/dc/elements/1.1/> PREFIX foaf:
 * <http://xmlns.com/foaf/0.1/>
 * 
 * SELECT DISTINCT ?maker ?made WHERE { ?made foaf:maker ?maker } ORDER BY
 * ?maker
 */

public class JpaEntries {

	private Log log = LogFactory.getLog(getClass());

	public JpaEntries(BibtexFile bibtexFile) {

		EntityManager aManager = Gomasio
				.getSesameEntityManager("satin.properties");

		for (@SuppressWarnings("rawtypes")
		Iterator iter = bibtexFile.getEntries().iterator(); iter.hasNext();) {
			BibtexEntry anEntry = (BibtexEntry) iter.next();

			String titre = "";
			String pages = "";
			String resume = "";
			String annee = "";

			if (anEntry.getFieldValue("title") != null) {
				titre = (new Gomasio()).removeUnexpectedChars(anEntry
						.getFieldValue("title").toString());
				titre = titre.replaceAll("\\r", "");
				titre = titre.replaceAll("\\n", "");
				titre = titre.replaceAll("\\t", " ");
			}
			if (anEntry.getFieldValue("pages") != null)
				pages = (new Gomasio()).removeUnexpectedChars(anEntry
						.getFieldValue("pages").toString());

			if (anEntry.getFieldValue("abstract") != null)
				resume = (new Gomasio()).removeUnexpectedChars(anEntry
						.getFieldValue("abstract").toString());

			if (anEntry.getFieldValue("year") != null)
				annee = (new Gomasio()).removeUnexpectedChars(anEntry
						.getFieldValue("year").toString());

			Paper paper = new Paper(titre, resume, annee, pages);

			if (anEntry.getFieldValue("author") != null) {
				String authors = anEntry.getFieldValue("author").toString();
				String str[] = authors.split(" and ");

				for (int i = 0; i < str.length; i++) {

					// create a new author and persist it.
					String _buff = str[i];
					_buff = _buff.replaceAll(",", "@@@");
					_buff = (new Gomasio()).removeUnexpectedChars(_buff);
					_buff = _buff.replaceAll("@@@", ",");
					Author a = getAuthor(_buff, aManager);

					if (a != null) {
						// Linking author to the paper.
						try {
							if (a.getRdfId() != null) {
								paper.getmAuthors().add(
										new URI(a.getRdfId().toString()));
							}
							// Linking paper to the author.
							// Linking paper to the author.
							if (a.getmPapers() == null) {
								a.setmPaper(new ArrayList<URI>());
							}

							if (paper.getRdfId() == null) {
								URIKey cle;
								try {
									int hash = paper.getmTitle().hashCode();
									if (hash < 0)
										hash = -1 * hash;

									cle = new URIKey(new URI(
											"http://data-satin.telecom-st-etienne.fr/paper/"
													+ hash));
									paper.setRdfId(cle);
								} catch (URISyntaxException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}

							a.getmPapers().add(
									new URI(paper.getRdfId().toString()));
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			String event = new String();
			String eventType = new String();
			if (anEntry.getFieldValue("booktitle") != null) {
				event = (new Gomasio()).removeUnexpectedChars(anEntry
						.getFieldValue("booktitle").toString());
				eventType = "conference";
				// paper.addEvent(event,"conference");
			} else if (anEntry.getFieldValue("journal") != null) {
				event = (new Gomasio()).removeUnexpectedChars(anEntry
						.getFieldValue("journal").toString());
				eventType = "journal";
				// paper.addEvent(event,"journal");
			} else
				event = null;

			if (event != null) {
				// create a new event and persist it.
				ConferenceEvent e = getEvent(event, eventType, aManager);
				try {
					paper.getmEvent().add(new URI(e.getRdfId().toString()));
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// paper.addEvent(event);
			}

			persistPaper(paper, aManager);
		}
	}

	private ConferenceEvent getEvent(String event, String eventType,
			EntityManager aManager) {
		String event_uri = "http://data-satin.telecom-st-etienne.fr/"
				+ eventType + "/" + event;
		event_uri = (new Gomasio()).removeUnexpectedChars(event_uri);
		event_uri = event_uri.replaceAll(" ", "");
		event_uri = event_uri.replaceAll("\\r", "");
		event_uri = event_uri.replaceAll("\\n", "");
		event_uri = event_uri.replaceAll("\\t", "");
		event_uri = event_uri.replaceAll("`", "");
		event_uri = event_uri.replaceAll("\"", "");
		log.info("looking up event : " + event_uri);

		ConferenceEvent e = aManager.find(ConferenceEvent.class, event_uri);
		if (e == null) {
			// author does not exist, create it and persist it
			e = new ConferenceEvent(event);

			// satin-compliant URI generation for authors
			URIKey cle;
			try {
				cle = new URIKey(new URI(event_uri));
				e.setRdfId(cle);
				aManager.persist(e);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return e;
	}

	private Author getAuthor(String string, EntityManager aManager) {
		// possibilities : "First von Last" "von Last, First"
		// "von Last, Jr, First"
		string = string.trim();
		string = string.replaceAll("\\r", "");
		string = string.replaceAll("\\n", "");
		string = string.replaceAll("\\t", " ");
		String familyName = "";
		String surName = "";
		if (!string.contains(",")) {
			// "First von Last" or "First Last"
			if (string.contains(" ")
					&& StringUtils.countMatches(string, " ") == 1) {
				// First Last
				String str[] = string.split(" ");
				surName = str[0];
				familyName = str[1];
			} else {
				// "First von Last"
				String str[] = string.split(" ");
				String startLast = "";
				for (int i = 0; i < str.length; i++) {
					log.info("examinating " + str[i]);
					if (str[i].length() > 0 && i > 0
							&& Character.isLowerCase(str[i].charAt(0))) {
						startLast = str[i];
					}
				}
				log.info("looking surname *" + string
						+ "* and with startLast *" + startLast + "*");
				surName = string.substring(0, string.indexOf(startLast));
				surName = surName.replaceAll(" ", "");
				log.info("found surname *" + surName + "*");

				log.info("looking familyame *" + string
						+ "* and with startLast *" + startLast + "*");
				familyName = string.substring(string.indexOf(startLast),
						string.length());
				familyName = familyName.replaceAll(" ", "");
				log.info("found familyname *" + familyName + "*");
			}
		} else if (StringUtils.countMatches(string, ",") == 1) {
			// "von Last, First"
			String str[] = string.split(",");
			familyName = str[0];
			surName = str[1];
		} else {
			// "von Last, Jr, First" : got to hell Jr ...
			String str[] = string.split(",");
			familyName = str[0];
			surName = str[str.length - 1];
		}

		String _pn = surName.replaceAll(" ", "");
		_pn = surName.replaceAll("\"", "");
		_pn = surName.replaceAll("\'", "");
		surName.replaceAll(" ", "");

		String _n = familyName.replaceAll(" ", "");
		_n = familyName.replaceAll("\"", "");
		_n = familyName.replaceAll("\'", "");
		_n = familyName.replaceAll(" ", "");

		String aut_uri = "http://data-satin.telecom-st-etienne.fr/person/"
				+ (new Gomasio()).removeUnexpectedChars(_n).trim() + "-"
				+ (new Gomasio()).removeUnexpectedChars(_pn).trim();

		aut_uri = aut_uri.replaceAll("\"", "");
		aut_uri = aut_uri.replaceAll(" ", "");
		aut_uri = aut_uri.replaceAll("\\`", "");
		aut_uri = aut_uri.replaceAll("\\\\`", "");
		aut_uri = aut_uri.replaceAll("\\\\\\`", "");
		aut_uri = aut_uri.replaceAll("`", "");

		try {
			Author a = aManager.find(Author.class, aut_uri);

			if (a == null) {
				// author does not exist, create it and persist it
				a = new Author(surName, familyName);
				

				// satin-compliant URI generation for authors
				URIKey cle;
				try {
					cle = new URIKey(new URI(aut_uri));

					a.setRdfId(cle);
					aManager.persist(a);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return a;
		} catch (Exception e) {
			for (char c : aut_uri.toCharArray()) {
				return null;
			}
		}
		return null;
	}

	private void persistPaper(Paper paper, EntityManager aManager) {

		log.info("Persisting paper \"" + paper.getmTitle() + "\" ("
				+ paper.getmPublishYear() + ")");

		if (aManager.contains(paper)) {
			log.info("Paper exists. Update Uri *" + paper.getRdfId()
					+ "* (update = remove/insert in the semantic web ...)");
			aManager.remove(paper);
			aManager.flush();
			log.info("Remove Done.\nReinserting ...");

			if (aManager.contains(paper)) {
				log.error("Remove for preparing update for paper "
						+ paper.getRdfId() + " failed ! (title = *"
						+ paper.getmTitle() + "*)");
			} else {
				aManager.persist(paper);
				log.info("(re)Insertion Done.");
			}
		} else {
			aManager.persist(paper);
			log.info("Insertion Done.");
		}
	}
}
