package analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.G;
import soot.Unit;
import soot.ValueBox;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArrayFlowUniverse;
import soot.toolkits.scalar.ArrayPackedSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.BoundedFlowSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.FlowUniverse;


/**
 * 
 *A query execution statement q is anticipable at a program point u 
 *if every path from u to End contains an execution of q 
 *which is not preceded by any statement that modifies the parameters of q 
 *or affects the results of q.
 *
 */


public class QAAnalysis extends BackwardFlowAnalysis<Unit, ArrayPackedSet>  {

	public List<Unit> allQuery = new ArrayList<Unit>();
	FlowSet entryFlowSet;
	
	public QAAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		//it is required to identify which are sql query statements
		preprocessing(graph);
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		FlowUniverse queryUniverse = new ArrayFlowUniverse(allQuery.toArray());
		
		entryFlowSet = new ArrayPackedSet(queryUniverse);
		
		//perform Backward Dataflow Analysis
		doAnalysis();
	}
	
	@Override
	protected void flowThrough(ArrayPackedSet in, Unit d, ArrayPackedSet out) {
		FlowSet gen = getGen(d);
		FlowSet kill = getKill(d);
		in.intersection(kill, out);
		out.union(gen, out);
	}


/**
 * 
 * @param d       - unit for which we have to calculate Generate Set
 * @return genSet - Gen n is 1 at bit q if n is the query execution statement q
 */
	private FlowSet getGen(Unit d) {

		Tag t = d.getTag("sqlQuery");
		FlowSet genSet = entryFlowSet.clone();
		
		if(t != null) genSet.add(d);
		
		return genSet;
		
	}

/**
 * 
 * @param d         -- unit for which we have to calculate kill Set
 * @return killSet  -- Killn is 1 at bit q 
 * 						if either n contains an assignment to a parameter of q, 
 * 						or performs an update to the database that may affect 
 * 						the results of q
 */
	
	private FlowSet getKill(Unit d) {
		FlowSet killSet = newInitialFlow();
		for(Unit q : allQuery){
			for(ValueBox vb : d.getDefBoxes()){
				List<ValueBox> quse = q.getUseBoxes();
				for(ValueBox qvbox : quse){
					if(qvbox.getValue().equals(vb.getValue()))
						killSet.remove(q);
				}
			}
		}
		//actually Preserve Set.
		return killSet;
	}

	@Override
	protected ArrayPackedSet newInitialFlow() {
		BoundedFlowSet bflow = (BoundedFlowSet) entryFlowSet.clone();
		bflow.complement(bflow);
		return (ArrayPackedSet) bflow;
	}

	@Override
	protected ArrayPackedSet entryInitialFlow() {
		return (ArrayPackedSet) entryFlowSet.clone();
	}

	@Override
	protected void merge(ArrayPackedSet in1, ArrayPackedSet in2,
			ArrayPackedSet out) {
		in1.intersection(in2, out);
		//in1.union(in2, out);
	}

	@Override
	protected void copy(ArrayPackedSet source, ArrayPackedSet dest) {
		source.copy(dest);
	}

/**
 * 	
 * @param graph - for a given Directed Graph we are tagging sql statement whithin it.
 */
	
	private void preprocessing(DirectedGraph<Unit> graph) {
		// create tag having "sqlQuery" string as Tag Name. 
		Tag t = new Tag() {

			@Override
			public byte[] getValue() throws AttributeValueException {
				return null;
			}

			@Override
			public String getName() {
				return "sqlQuery";
			}
		};

		Iterator<Unit> unitIt = graph.iterator();
		while(unitIt.hasNext()){
			Unit unit = unitIt.next();
			for(ValueBox vb : unit.getUseBoxes()){
				
				if(vb.getValue().getType().toString().contains("java.sql") && 
						vb.getValue().getClass().getName().equals("soot.jimple.internal.JInterfaceInvokeExpr")){
					@SuppressWarnings("unchecked")
					List<ValueBox> v = vb.getValue().getUseBoxes();
					for(int i=0;i<v.size();i++){
						if(v.get(i).getClass().getName().equals("soot.jimple.internal.ImmediateBox")){
							unit.addTag(t);
							allQuery.add(unit);
							G.v().out.println("\t\t"+v.get(i).getValue());
						}
					}
					
				}
			}
		}
	}

	
}
