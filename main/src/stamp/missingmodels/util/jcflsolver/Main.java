package stamp.missingmodels.util.jcflsolver;

import java.io.*;

import stamp.missingmodels.grammars.*;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		if(args.length < 1)
			throw new RuntimeException("First argument must be the path to the directory containing .dat files.");
		File inputDir = new File(args[0]);
		if(!inputDir.exists())
			throw new RuntimeException("Input directory "+inputDir.getAbsolutePath()+" does not exist.");
		
		File outputDir;
		if(args.length < 2)
			outputDir = new File("jcflsolver_output");
		else
			outputDir = new File(args[1]);
		if(outputDir.exists())
			outputDir.delete();			
		
		try{
			outputDir.mkdirs();
		}catch(SecurityException e){
			throw new Error(e);
		}

		System.out.println("Outputing to "+outputDir.getAbsolutePath());
		long time = System.currentTimeMillis();

		Graph g = new C12();
		FactsReader.read(g, inputDir, outputDir);
		g.algo.process();
		FactsWriter.write(g, outputDir);
		
		System.out.println(System.currentTimeMillis()-time);
	}
}