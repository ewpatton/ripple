package net.fortytwo.ripple.util;

import net.fortytwo.ripple.RippleException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public final class FileUtils
{
	public static Collection<String> getLines( final InputStream is )
		throws RippleException
	{
		LinkedList<String> lines = new LinkedList<String>();

		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader( is ) );
	
			// Break out when end of stream is reached.
			while ( true )
			{
				String line = reader.readLine();
	
				if ( null == line )
				{
					break;
				}

				line = line.trim();
	
				if ( !line.startsWith( "#" ) && !line.equals( "" ) )
				{
					lines.add( line );
				}
			}
		}

		catch ( java.io.IOException e )
		{
			throw new RippleException( e );
		}

		return lines;
	}

	private FileUtils()
	{
	}
}

