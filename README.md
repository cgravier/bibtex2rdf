bibtex2rdf
=========

A Java bibtex parser that serialises bibtex entries into a triplestore using the Sesame framework.

The software is licence under the Apache 2.0 licence http://www.apache.org/licenses/LICENSE-2.0.html

Configure the triplestore that the software will write to using the satin.properties file.

The vocabularies used in the triplestore are the same than the Semantic Web Dog Food server: http://data.semanticweb.org/

To run the software you will need :
- a working copy of the Croquette library in your local maven repository. Build instruction in : https://github.com/cgravier/Croquette/
- a running instance of a triplestore using the Sesame framework. You can for instance follow these isntructions : http://owlim.ontotext.com/display/OWLIMv43/OWLIM-Lite+Installation
