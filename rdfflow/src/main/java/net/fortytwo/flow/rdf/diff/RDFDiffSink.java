/*
 * $URL: https://ripple.googlecode.com/svn/trunk/ripple-rdf/src/main/java/net/fortytwo/flow/rdf/diff/RDFDiffSink.java $
 * $Revision: 135 $
 * $Author: parcour $
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.flow.rdf.diff;

import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.flow.diff.DiffSink;
import org.openrdf.model.Statement;
import org.openrdf.model.Namespace;

public interface RDFDiffSink<E extends Exception>
{
	RDFSink<E> adderSink();
	RDFSink<E> subtractorSink();

    DiffSink<Statement, E> statementSink();
    DiffSink<Namespace, E> namespaceSink();
    DiffSink<String, E> commentSink();
}
