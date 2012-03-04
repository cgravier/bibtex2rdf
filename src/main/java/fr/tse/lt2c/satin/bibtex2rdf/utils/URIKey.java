package fr.tse.lt2c.satin.bibtex2rdf.utils;

import java.net.URI;

import com.clarkparsia.empire.SupportsRdfId.RdfKey;

public class URIKey implements RdfKey<java.net.URI> {
	private final java.net.URI mURI;

	public URIKey(final URI theURI) {
		mURI = theURI;
	}

	/**
	 * @inheritDoc
	 */
	public java.net.URI value() { return mURI; }

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return mURI.hashCode();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return mURI.toASCIIString();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object theObj) {
		if (theObj == null) {
			return false;
		}
		else if (this == theObj) {
			return true;
		}
		else {
			return theObj instanceof URIKey && mURI.equals(((URIKey) theObj).value());
		}
	}
}