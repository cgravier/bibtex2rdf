package fr.tse.lt2c.satin.bibtex2rdf;
/*
 * Created on Mar 21, 2003
 * @author Christophe Gravier. based on template as provided by henkel@cs.colorado.edu in javabib LaTEX API
 */

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import fr.tse.lt2c.satin.bibtex2rdf.beanfier.empire.JpaEntries;

import bibtex.dom.BibtexFile;
import bibtex.expansions.CrossReferenceExpander;
import bibtex.expansions.ExpansionException;
import bibtex.expansions.MacroReferenceExpander;
import bibtex.expansions.PersonListExpander;
import bibtex.parser.BibtexParser;

/**
 * 
 * 
 * @author Christophe Gravier
 */
public final class Main {
 
	public static void usage() {
		System.err.println(
			"\nUsage: bibtex.Main [-expandStringDefinitions]\n"
				+ "         [-expandAndDropMacroDefinitions] [-expandCrossReferences]\n"
				+ "         [-expandPersonLists] [-noOutput] <file.bib>\n"
				+ "\nNote: Selecting -expandCrossReferences implies that we will\n"
				+ "      expand the string definitions as well (for consistency).\n"
				+ "\nNote: Selecting -expandPersonLists implies that we will expand\n"
				+ "      the string definitions as well (for consistency)."
				+ "\nThe output will be given on stdout, errors and messages will be printed to stderr.\n\n");
	}

	public static void main(String[] args) {
		//long startTime = System.currentTimeMillis();
		if (args.length < 1) {
			usage();
			return;
		}
		
		BibtexFile bibtexFile = new BibtexFile();
		BibtexParser parser = new BibtexParser(false);
		
		boolean expandMacros = false;
		boolean dropMacros = false;
		boolean expandCrossrefs = false;
		boolean expandPersonLists = false;
		boolean noOutput = false;
		for (int argsIndex = 0; argsIndex < args.length - 1; argsIndex++) {
			String argument = args[argsIndex];
			if (argument.equals("-expandStringDefinitions")) {
				expandMacros = true;
			} else if (argument.equals("-expandAndDropStringDefinitions")) {
				expandMacros = dropMacros = true;
			} else if (argument.equals("-expandCrossReferences")) {
				expandCrossrefs = expandMacros = true;
			} else if (argument.equals("-expandPersonLists")) {
				expandPersonLists = expandMacros = true;
			} else if(argument.equals("-noOutput")){
				noOutput = true;
			} else {
				System.err.println("Illegal argument: " + argument);
				usage();
			}
		}

		try {
			String filename = args[args.length - 1];
			System.err.println("Parsing \"" + filename + "\" ... ");
			parser.parse(bibtexFile, new FileReader(new File(args[args.length - 1])));
		} catch (Exception e) {
			System.err.println("Fatal exception: ");
			e.printStackTrace();
			return;
		} finally {
			printNonFatalExceptions(parser.getExceptions());
		}
		try {
			if (expandMacros) {
				System.err.println("\n\nExpanding macros ...");
				MacroReferenceExpander expander =
					new MacroReferenceExpander(true, true, dropMacros,false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
				
			}
			if (expandCrossrefs) {
				System.err.println("\n\nExpanding crossrefs ...");
				CrossReferenceExpander expander = new CrossReferenceExpander(false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
			}
			if (expandPersonLists) {
				System.err.println("\n\nExpanding person lists ...");
				PersonListExpander expander = new PersonListExpander(true, true, false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
			}
		} catch (ExpansionException e1) {
			e1.printStackTrace();
			return;
		}
		if(noOutput) return;
		System.err.println("\n\nGenerating output ...");
		//PrintWriter out = new PrintWriter(System.out);
		//bibtexFile.printBibtex(out);
		//out.flush();

		JpaEntries jEntries = new JpaEntries(bibtexFile);
	}

	 
	
	private static void printNonFatalExceptions(Exception[] exceptions) {
		if (exceptions.length > 0) {
			System.err.println("Non-fatal exceptions: ");
			for (int i = 0; i < exceptions.length; i++) {
				exceptions[i].printStackTrace();
				System.err.println("===================");
			}
		}
	}
}
