package org.ooxo;


import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

class GRF extends LPAlgorithm {
	public GRF(int _steps) {
		steps = _steps;
	}
	public GRF() {
		steps = 10;
	}
	
	void debug() {
		Iterator<Long> it = vertexFMap.keySet().iterator();
		while (it.hasNext()) {
			ArrayList<Double> arr = vertexFMap.get(it.next());
			Iterator<Double> fMapIter = arr.iterator();
			while (fMapIter.hasNext()) {
				System.out.printf("%.04f", fMapIter.next());
				System.out.print(fMapIter.hasNext() ? "   " : "\n");
			}
		}
	}
	
	double iter() {
		//System.out.println("> iter ");
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
					//System.out.println("(src,dst): " + src + "->" + vertexId + ", value = (" + fValue +"), deg = " + deg + ", label = " + l);
				}
				nextFValue.add(fValue);
				if (vertexLabelMap.get(vertexId) == 0) {
					diff += ((fValue > fValues.get(l)) ? fValue - fValues.get(l) : fValues.get(l) - fValue);
				}
			}
			//System.out.println(nextFValue);
			nextVertexFMap.put(vertexId, nextFValue);
			//System.out.println("----");
		}
		// fix labeled vertex
		for (Long vertexId : vertexLabelMap.keySet()) {
			if (vertexLabelMap.get(vertexId) == 0) continue; // 0 means unlabeled vertex
			nextVertexFMap.put(vertexId, vertexFMap.get(vertexId));
		}
		vertexFMap = nextVertexFMap;
		
		return diff;
	}
	
	void run() {
		showDetail();
		double diff = 0;
		for (int i = 0; i < 100; ++i) {
			System.out.print(".");
			System.out.flush();
			diff = iter();
			if (diff < 10e-5) break;
			if (i % 50 == 49) {
				System.out.println("");
			}
		}
		System.out.println("\neps = " + diff);
		debug();
	}
	
	// private
	final int steps;
}