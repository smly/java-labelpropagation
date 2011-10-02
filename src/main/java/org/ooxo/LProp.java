package org.ooxo;

import org.apache.commons.cli.*;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;

public class LProp
{
	public static void main( String[] args ) {
		Options options = new Options();
		options.addOption("g", false, "graph file");
		options.addOption("t", false, "display current time");
		options.addOption(OptionBuilder.create('h'));
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("cmd parser error.");
			return;
		}
		
		if (cmd.hasOption('t')) {
			System.out.println("hogeee");
		}
        System.out.println( "Hello World!" );
    }
}
