package org.ooxo;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.HashMap;

abstract class LPAlgorithm {
	protected class Edge {
		Edge(long src_, long dest_, double weight_) {
			src    = src_;
			dest   = dest_;
			weight = weight_;
		}
		private long src;
		private long dest;
		private double weight;
		
		public long getSrc() {
			return src;
		}
		public long getDest() {
			return dest;
		}
		public double getWeight() {
			return weight;
		}
	}
	
	private class ReadJSON {
		ReadJSON(String fileName) {
			fFile = new File(fileName);
		}
		
		void processLineByLine() throws FileNotFoundException {
			Scanner scanner = new Scanner(new FileReader(fFile));
			try {
				while (scanner.hasNextLine()) {
					processLine(scanner.nextLine());
				}
			} finally {
				scanner.close();
			}
		}
		void processLine(String line) {
			try {
				// [vertexId, vertexLabel, edges]
				// unlabeled vertex if vertexLabel == 0
				// i.e. [2, 1, [[1, 1.0], [3, 1.0]]]
				JSONArray json = new JSONArray(line);
				Long vertexId = json.getLong(0);
				Long vertexLabel = json.getLong(1);
				JSONArray edges = json.getJSONArray(2);
				ArrayList<Edge> edgeArray = new ArrayList<Edge>();
				vertexLabelMap.put(vertexId, vertexLabel);
				/*
				Double deg = new Double(0);
				if (vertexDegMap.containsKey(vertexId)) {
					deg = vertexDegMap.get(vertexId);
				} */
				for (int i = 0; i < edges.length(); ++i) {
					JSONArray edge = edges.getJSONArray(i);
					Long destVertexId = edge.getLong(0);
					Double edgeWeight = edge.getDouble(1);
					// deg += edgeWeight;
					edgeArray.add(new Edge(vertexId, destVertexId, edgeWeight));
					// System.out.println(vertexId + " -> " + edgeDest + " : " + edgeWeight);
				}
				vertexAdjMap.put(vertexId, edgeArray);
				
				// vertexDegMap.put(vertexId, deg);
			} catch (JSONException e) {
				throw new IllegalArgumentException(
						"Coundn't parse vertex from line: " + line, e);
			}
		}
		// private
		private File fFile;
	}
	
	LPAlgorithm() {
		vertexAdjMap   = new HashMap<Long, ArrayList<Edge>>();
		vertexInAdjMap = new HashMap<Long, ArrayList<Edge>>();
		vertexDegMap   = new HashMap<Long, Double>();
		vertexLabelMap = new HashMap<Long, Long>();
		labelIndexMap  = new HashMap<Long, Long>();
		vertexFMap     = new HashMap<Long, ArrayList<Double>>();
	}

	boolean loadJSON(String fileName) {
		ReadJSON reader = new ReadJSON(fileName);
		try {
			reader.processLineByLine();
		} catch (FileNotFoundException e) {
			System.err.println("Error: " + e.toString());
			return false;
		}
		// initialize vertexInAdjMap
		for (Long vertexId : vertexAdjMap.keySet()) {
			if (! vertexInAdjMap.containsKey(vertexId)) {
				vertexInAdjMap.put(vertexId, new ArrayList<Edge>());
			}
		}
		// and add edges
		for (Long vertexId : vertexAdjMap.keySet()) {
			for (Edge e : vertexAdjMap.get(vertexId)) {
				vertexInAdjMap.get(e.getDest()).add(e);
			}
		}
		// setup vertexDegMap
		for (Long vertexId : vertexAdjMap.keySet()) {
			double degree = 0;
			if (vertexDegMap.containsKey(vertexId)) {
				degree = vertexDegMap.get(vertexId);
			}
			for (Edge e : vertexAdjMap.get(vertexId)) {
				degree += e.getWeight();
			}
			vertexDegMap.put(vertexId, degree);
		}
		// setup vertexFMap
		Set<Long> vSet = vertexLabelMap.keySet();
		Iterator<Long> it = vSet.iterator();
		Set<Long> lSet = new TreeSet<Long>();
		while (it.hasNext()) {
			
			Long l = vertexLabelMap.get(it.next());
			lSet.add(l);
			vertexSize++;
		}
		Iterator<Long> lSetIter = lSet.iterator();
		int labelEnum = 0;
		while (lSetIter.hasNext()) {
			Long l = lSetIter.next();
			if (l.intValue() == 0) continue;
			System.out.println("label " + l + " is assigned to " + labelEnum);
			labelIndexMap.put(l, new Long(labelEnum));
			labelEnum++;
		}
		labelSize = labelEnum;
		it = vSet.iterator();
		labeledSize = 0;
		while (it.hasNext()) {
			Long v = it.next();
			ArrayList<Double> arr = new ArrayList<Double>(labelEnum);
			Long l = vertexLabelMap.get(v);
			if (l.intValue() == 0) {
				// unlabeled
				for (int i = 0; i < labelSize; ++i) {
					arr.add(0.0);
				}
			} else {
				// labeled
				labeledSize++;
				int ix = labelIndexMap.get(vertexLabelMap.get(v)).intValue();
				System.out.println("label " + v + " label = " + vertexLabelMap.get(v) + ", ix = " + ix);
				for (int i = 0; i < labelSize; ++i) {
					arr.add((i == ix) ? 1.0 : 0.0);
				}
			}
			vertexFMap.put(v, arr);
		}
		
		return true;
	}

	void showDetail() {
		System.out.println("Number of vertices:            " + vertexSize);
		System.out.println("Number of class labels:        " + labelSize);
		System.out.println("Number of unlabeled vertices:  " + (vertexSize - labeledSize));
		System.out.println("Numebr of labeled vertices:    " + labeledSize);
	}

	abstract void run();

	// private
	protected HashMap<Long,ArrayList<Edge>> vertexAdjMap; // out-edge
	protected HashMap<Long,ArrayList<Edge>> vertexInAdjMap;
	protected HashMap<Long, Double> vertexDegMap;
	protected HashMap<Long,Long> vertexLabelMap;
	protected HashMap<Long,Long> labelIndexMap; // todo: label as String, not Long
	protected HashMap<Long,ArrayList<Double>> vertexFMap;
	protected int vertexSize;
	protected int labelSize;
	protected int labeledSize;
}

class GRF extends LPAlgorithm {
	public GRF(int _steps) {
		steps = _steps;
	}
	public GRF() {
		steps = 10;
	}
	
	void debug() {
		System.out.println("> debug");
		
		Iterator<Long> it = vertexFMap.keySet().iterator();
		while (it.hasNext()) {
			ArrayList<Double> arr = vertexFMap.get(it.next());
			Iterator<Double> fMapIter = arr.iterator();
			while (fMapIter.hasNext()) {
				System.out.print(fMapIter.next().toString() + "  ");
			}
			System.out.println("");
		}
	}
	
	double iter() {
		System.out.println("> iter ");
		HashMap<Long,ArrayList<Double>> nextVertexFMap = new HashMap<Long,ArrayList<Double>>();
		// for all vertex
		double diff = 0.0;
		for (Long vertexId : vertexFMap.keySet()) {
			if (vertexLabelMap.get(vertexId) != 0) continue; // skip labeled
			// update F(vertexID) ... vetexFMap
			ArrayList<Double> nextFValue = new ArrayList<Double>();
			ArrayList<Double> fValues = vertexFMap.get(vertexId);
			for (int l = 0; l < labelSize; ++l) {
				// update f_l(vertexId)
				double fValue = 0.0;
				for (Edge e : vertexInAdjMap.get(vertexId)) {
					double w = e.getWeight();
					long src = e.getSrc();
					double deg = vertexDegMap.get(vertexId);
					fValue += vertexFMap.get(src).get(l) * (w / deg);
					System.out.println("(src,dst): " + src + "->" + vertexId + ", value = (" + fValue +"), deg = " + deg + ", label = " + l);
				}
				nextFValue.add(fValue);
				if (vertexLabelMap.get(vertexId) == 0) {
					diff += ((fValue > fValues.get(l)) ? fValue - fValues.get(l) : fValues.get(l) - fValue);
				}
			}
			System.out.println(nextFValue);
			nextVertexFMap.put(vertexId, nextFValue);
			System.out.println("----");
		}
		// fix labeled vertex
		for (Long vertexId : vertexLabelMap.keySet()) {
			if (vertexLabelMap.get(vertexId) == 0) continue; // 0 means unlabeled vertex
			System.out.println("fix: " + vertexId);
			nextVertexFMap.put(vertexId, vertexFMap.get(vertexId));
		}
		vertexFMap = nextVertexFMap;
		System.out.println(diff);
		
		return diff;
	}
	
	void run() {
		showDetail();
		debug();
		for (int i = 0; i < 50; ++i) {
			double diff = iter();
			if (diff < 10e-3) break;
			debug();
		}
	}
	
	// private
	final int steps;
}

public class LProp
{
	private static String algoName;
	private static String[] inputFileNames;

	private static void parseArgs(String[] args) {
		Options options = new Options();

		OptionBuilder.isRequired();
		OptionBuilder.hasArgs(1);
		options.addOption(OptionBuilder.create('a'));

		options.addOption(new Option( "help", "print this message" ));

		// TODO: add -s option: make given input symmetric
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Unexpected exception: " + exp.getMessage());
			return;
		}
		
		if (cmd.hasOption("help") || (!cmd.hasOption('a'))|| cmd.getArgs().length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			//formatter.printHelp(getClass().getName() + " -a ALG FILE1 ...", options);
			formatter.printHelp("LProp FILE", options);
			System.exit(0);
		}
		if (cmd.hasOption('a') ){
			algoName = cmd.getOptionValue("a");
		}

		inputFileNames = cmd.getArgs();
	}

	public static void main(String[] args) {
		parseArgs(args);

		LPAlgorithm lp = null;
		if (algoName.equals("GRF")) {
			lp = new GRF();
		} else {
			System.err.println("Specify algorithm with -a option.");
			System.exit(1);
		}
		lp.loadJSON(inputFileNames[0]);
		lp.run();
	}
}
