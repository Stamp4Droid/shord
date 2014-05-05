package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.bddbddb.Dom;
import chord.project.Chord;

@Chord(name = "relprinter")
public class RelationPrinter extends JavaAnalysis {
	public static final Set<Relation> relations = new HashSet<Relation>();
	static {
		relations.add(new Relation("Alloc"));
		relations.add(new Relation("Assign"));
		relations.add(new Relation("Store"));
		relations.add(new Relation("Load"));
		//relations.add(new Relation("StoreStat"));
		//relations.add(new Relation("LoadStat"));
		relations.add(new Relation("param"));
		relations.add(new Relation("return"));
		//relations.add(new Relation("typeFilter"));

		relations.add(new Relation("CH"));

		//relations.add(new Relation("pt"));
		//relations.add(new Relation("fpt"));
		//relations.add(new Relation("fptStat"));
		
		relations.add(new Relation("ptdf"));
	}
	
	public static class Relation {
		private final String relationName;
		
		public Relation(String relationName) {
			this.relationName = relationName;
		}
		
		public String getFileName() {
			return this.relationName + ".txt";
		}
		
		private static String toString(int[] tuple) {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<tuple.length-1; i++) {
				sb.append(tuple[i]).append(" ");
			}
			sb.append(tuple[tuple.length-1]);
			return sb.toString();
		}
		
		private static String toString(Dom<?>[] domains) {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<domains.length-1; i++) {
				sb.append(domains[i].getName()).append(" ");
			}
			sb.append(domains[domains.length-1].getName());
			return sb.toString();
		}

		public void print(PrintWriter pw) {
			final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(this.relationName);
			rel.load();
			
			pw.println(toString(rel.getDoms()));			
			
			Iterable<int[]> res = rel.getAryNIntTuples();
			List<String> tuples = new ArrayList<String>();
			for(int[] tuple : res) {
				tuples.add(toString(tuple));
			}
			Collections.sort(tuples);
			for(String tuple : tuples) {
				pw.println(tuple);
			}
			
			rel.close();
		}
	}

	@Override
	public void run() {
		String stampDirectory = System.getProperty("stamp.out.dir");
		File outputDir = new File(stampDirectory + File.separator + "cfl");
		outputDir.mkdirs();
		for(Relation relation : relations) {
			try {
				System.out.println("Printing: " + relation.relationName);
				PrintWriter pw = new PrintWriter(stampDirectory + File.separator + "cfl" + File.separator + relation.getFileName());
				relation.print(pw);
				pw.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
