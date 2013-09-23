package analysis;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.graph.CriticalEdgeRemover;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.FlowSet;

public class QATransformation extends BodyTransformer{

	private static QATransformation instance = new QATransformation();
	public QATransformation() {}
	public static QATransformation v() { return instance; }

	public void InsertPrefetchRequests(Body body, String phase, @SuppressWarnings("rawtypes") Map options){

		/**
		 * remove all critical edges by edge splitting
		 */
		CriticalEdgeRemover.v().transform(body, phase, options);
		/**
		 * @param Q   - contains all Query.
		 * @param qaa - QueryAnticipability Analysis object.  
		 */
		DirectedGraph<Unit> graph = new ExceptionalUnitGraph(body);
		QAAnalysis qaa = new QAAnalysis(graph);
		List<Unit> Q = qaa.allQuery;
		PatchingChain<Unit> units = body.getUnits();

		for(Unit q : Q){

			Iterator<Unit> stmtIt = units.snapshotIterator();
			while(stmtIt.hasNext()){

				Stmt stmt = (Stmt) stmtIt.next();

				FlowSet outn = qaa.getFlowAfter(stmt);
				FlowSet inn  = qaa.getFlowBefore(stmt);
				FlowSet outPred = qaa.entryFlowSet;

				List<Unit> preds = graph.getPredsOf(stmt);
				//union of predecessor of given unit which contain given query.
				for(Unit pred : preds){
					FlowSet outm = qaa.getFlowAfter(pred);
					outPred.union(outm,outPred);
				}
				if(outn.contains(q) && !inn.contains(q)){
					//TODO appendPrefetchRequest(unit,q);
					//units.insertAfter(toAdd, stmt);
					//G.v().out.println("Insert " + q.toString() + " after " + stmt.toString());
					Tag t = new MyTag(q.toString(), false);
					stmt.addTag(t);
					break;
				}
				else if(inn.contains(q) && !outPred.contains(q)){
					//TODO PrependPrefetchRequest(unit,q);
					//units.insertBefore(toAdd, stmt);
					Tag t = new MyTag(q.toString(), true);
					stmt.addTag(t);
					break;
					//G.v().out.println("Insert " + q.toString() + " before " + stmt.toString());
				}
			}
		}

	}

	@Override
	protected void internalTransform(Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
		// TODO Auto-generated method stub
		InsertPrefetchRequests(body,phaseName,options);
		PrintBody(body);
		
	}
	private void PrintBody(Body body) {
		PatchingChain<Unit> units = body.getUnits();
		Iterator<Unit> stmtIt = units.snapshotIterator();
		while(stmtIt.hasNext()){
			Stmt stmt = (Stmt) stmtIt.next();
			
			List<Tag> tags = stmt.getTags();
			G.v().out.println("\n");
			for(Tag t : tags) {
				if(t instanceof MyTag) {
					if(((MyTag) t).isbefore == true){
						G.v().out.println(t.getName());
					}
				}
			}
			G.v().out.println(stmt.toString());
			for(Tag t : tags) {
				if(t instanceof MyTag) {
					if(((MyTag) t).isbefore == false){
						G.v().out.println(t.getName());
					}
				}
			}
		}
	}
}

class MyTag implements Tag{

	String query;
	public Boolean isbefore;
	public MyTag(String q, Boolean flag)
	{
		query = q;
		isbefore = flag;
	}
	@Override
	public String getName() {
		return "submit (" + query +" )";
		//return isbefore.toString() + query;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		return null;
	}
	
}
