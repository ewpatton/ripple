/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.query.commands;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Arrays;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.io.RipplePrintStream;
import net.fortytwo.ripple.query.Command;
import net.fortytwo.ripple.query.QueryEngine;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.flow.Collector;
import net.fortytwo.ripple.flow.Sink;

import org.openrdf.model.Namespace;

public class ShowNamespacesCmd extends Command
{
	public void execute( final QueryEngine qe, final ModelConnection mc )
		throws RippleException
	{
		final RipplePrintStream ps = qe.getPrintStream();

        // Create a map of prefixes to names and find the longest prefix.
        Map<String, String> prefixesToNames = new HashMap<String, String>();
        Collector<Namespace, RippleException> coll = new Collector<Namespace, RippleException>();
		mc.putNamespaces( coll );
		int max = 0;
        int j = 0;
		Iterator<Namespace> iter = coll.iterator();
		while ( iter.hasNext() )
		{
            Namespace ns = iter.next();
            prefixesToNames.put(ns.getPrefix(), ns.getName());

            int len = ( ns.getPrefix() + j ).length();
			if ( len > max )
			{
				max = len;
			}
			j++;
		}
		final int maxlen = max + 4;

        // Alphabetize the prefixes.
        String[] array = new String[prefixesToNames.size()];
        prefixesToNames.keySet().toArray( array );
        Arrays.sort(array);

        ps.println( "" );

        // Print the namespaces, aligning the name portions with one another.
        int i = 0;
        for ( String prefix : array )
        {
            String s = "[" + i++ + "] " + prefix + ":";
            int len = s.length();
            ps.print( s );

            for ( int l = 0; l < maxlen - len + 2; l++ )
            {
                ps.print( ' ' );
            }

            ps.print( prefixesToNames.get( prefix ) );
            ps.print( '\n' );
        }

		ps.println( "" );
	}

	protected void abort()
	{
	}
}

