package stamp.submitting;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.HashMap;

public class StampRunner extends Thread
{
	private final String stampDir;
	private final BlockingQueue<File> workList = new LinkedBlockingQueue();
	private UpdateServlet.Updater updater;

	public StampRunner(String stampDir)
	{
		this.stampDir = stampDir;
	}

	public File targetFile(String apkName)
	{
		String outDirName = stampDir+"/stamp_output/"+apkName;
		File outDir = new File(outDirName);
		outDir.mkdirs();
		File apkFile = new File(outDir, apkName);
		return apkFile;
	}

	public void addToWorkList(File apkFile)
	{
		try {
			workList.put(apkFile);
		} catch (InterruptedException ex) { 
			throw new Error(ex);
		}
	}
	
	public void run()
	{
		try {
			while (true)
				process(workList.take());
		} catch (InterruptedException ex) { 
			throw new Error(ex);
		}
	}

    
    /* Get map to classify warnings
       @param stampDir Path to stamp 
       @return A map from method names to warning labels
     */
    private HashMap<String,String> getWarningClassMap(String stampDir) {
	HashMap<String,String> warnClassMap = new HashMap<String, String>();

	try {
	    String wClass = new String();
	    File cWarnFile = new File(stampDir+"/scanner/src/stamp/scanner/warnM.xml");
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(cWarnFile);

	    doc.getDocumentElement().normalize();
 
	    NodeList warnList = doc.getElementsByTagName("warn");
	
	    for (int c = 0; c < warnList.getLength(); c++) {
		Node warnNode = warnList.item(c);
		if (warnNode.getNodeType() == Node.ELEMENT_NODE) {
		    Element warnElement = (Element) warnNode;
		    System.out.println("Warn Method:" + warnElement.getAttribute("method") + " Text:" + warnElement.getTextContent());
		    warnClassMap.put(warnElement.getAttribute("method").replaceAll("\\s",""), warnElement.getTextContent());
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return warnClassMap;
    }

    /* 
       Push flows and warnings to client

       @param apkFile APK file being processed
     */
    private void pushResults(File apkFile) {

	System.out.println("Begin pushResults");

	String apkName = apkFile.getName();
	String apkId = apkFile.getName()+apkFile.length();

	if(updater != null) {
	    String apkDir = stampDir+"/stamp_output/"+apkName;
	    String resultsPath = apkDir + "/results/";
	    File cFlowsFile = new File(resultsPath + "classifiedFlows.xml");
	    File logFile = new File(apkDir+"/chord_output/log.txt");

	    HashMap<String,String> warnClassMap = getWarningClassMap(stampDir);

	    /* Send warnings */
	    if (logFile.exists()){
		try {
		    StringBuilder logContents = new StringBuilder((int)logFile.length());
		
		    Scanner scanner = new Scanner(logFile);
		    String lineSeparator = System.getProperty("line.separator");
		
		    try {
			while(scanner.hasNextLine()) {
			    String l = scanner.nextLine();
			    if (l.matches("DANGER_METHOD:(.*)")) {
				String[] parts = l.split(" ");
				String wClass = warnClassMap.get(parts[1]);
				if (wClass != null) {
				    logContents.append("DANGER_METHOD:" + parts[1] 
						       + ";;" + warnClassMap.get(parts[1]) 
						       + ";;" + parts[2]
						       + lineSeparator);
				} else {
				    logContents.append("DANGER_METHOD:" + parts[1] 
						       + ";;Other" 
						       + ";;" + parts[2]
						       + lineSeparator);
				}
			    }
			}
		    } finally {
			scanner.close();
		    }

		    try{
			updater.update("WARN::"+apkId+"::"+logContents.toString());
		    }catch(IOException ioe){
			throw new Error(ioe);
		    }

		}catch(FileNotFoundException e) {
		    throw new Error(e);
		}
		
	    } else {
		try{
		    updater.update("NoWarnings::"+apkId);
		}catch(IOException ioe){
		    throw new Error(ioe);
		}
	    }
	    
	    /* Send classified flow data */
	    if (cFlowsFile.exists()){
		try {
		    StringBuilder fileContents = new StringBuilder((int)cFlowsFile.length());
		    
		    Scanner scanner = new Scanner(cFlowsFile);
		    String lineSeparator = System.getProperty("line.separator");
		
		    try {
			while(scanner.hasNextLine()) {
			    fileContents.append(scanner.nextLine() + lineSeparator);
			}
		    } finally {
			scanner.close();
		    }
		    
		    try{
			updater.update("Flow::"+apkId+"::"+fileContents.toString());
		    }catch(IOException ioe){
			throw new Error(ioe);
		    }
		}catch(FileNotFoundException e) {
		    throw new Error(e);
		}
	    } else {
		try{
		    updater.update("NoFlows::"+apkId);
		}catch(IOException ioe){
		    throw new Error(ioe);
		}
	    }
	}
	return;
    }
	
	private void process(File apkFile)
	{
		String apkId = apkFile.getName()+apkFile.length();
		try{
			if(updater != null)
				updater.update("BEGIN::"+apkId);
			//ShellProcessRunner.run(cmdLine, outDir, false, log);
			runStamp(apkFile);
			pushResults(apkFile);
			if(updater != null)
				updater.update("END::"+apkId);
		}catch(ShellProcessRunner.InvocationFailureException/*BuildException*/ e){
			e.printStackTrace();
			try{
				updater.update("ERROR::"+apkId);
			}catch(IOException ioe){
				throw new Error(ioe);
			}
		}catch(Exception e){
			throw new Error(e);
		}
	}

	private void runStamp(File apkFile) throws Exception
	{
		File outDir = apkFile.getParentFile();
		File logFile = new File(outDir, "stamplog.txt");
		logFile.delete();
		

		//String[] cmdLine = {stampDir+"/stamp", "analyze", "\""+apkFile.getAbsolutePath()+"\"", "-Dstamp.out.dir=\""+outDir.getAbsolutePath()+"\""};
		//String[] cmdLine = {"bash", stampDir+"/stamp", "analyze", "\""+apkFile.getAbsolutePath()+"\"", "-Dstamp.out.dir=\""+outDir.getAbsolutePath()+"\""};
		String[] cmdLine = {stampDir+"/stamp", "analyze", apkFile.getAbsolutePath(), "-Dstamp.out.dir="+outDir.getAbsolutePath()};
		//String[] cmdLine = {"/bin/bash", "/home/saswat/software/apache-ant-1.8.4/bin/ant", "-f", stampDir+"/ant/stamp.xml", "analyze", "\""+apkFile.getAbsolutePath()+"\"", "-Dstamp.out.dir=\""+outDir.getAbsolutePath()+"\""};
		//String[] cmdLine = {"bash", "-c", stampDir+"/stamp", "analyze \""+apkFile.getAbsolutePath()+"\" -Dstamp.out.dir=\""+outDir.getAbsolutePath()+"\""};
		System.out.print("Executing: ");
		for(String tok : cmdLine)
			System.out.println(tok +" ");
		ShellProcessRunner.run(cmdLine, outDir, false, logFile);

		
		/*
		CommandLine cmd = new CommandLine(stampDir+"/stamp");
		cmd.addArgument("analyze");
		cmd.addArgument(apkFile.getAbsolutePath());
		cmd.addArgument("-Dstamp.out.dir='"+outDir.getAbsolutePath()+"'");
		System.out.println("executing: "+cmd.toString());
		Executor exec = new DefaultExecutor();
		exec.setWorkingDirectory(outDir);
		exec.execute(cmd);
		*/
		/*
		File buildFile = new File(stampDir, "ant/stamp.xml");
		Project p = new Project();
		p.init();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		p.setUserProperty("app", apkFile.getAbsolutePath());
		p.setUserProperty("stamp.out.dir", outDir.getAbsolutePath());

		BuildLogger logger = new DefaultLogger();
		PrintStream out = new PrintStream(logFile);
		logger.setOutputPrintStream(out);
		logger.setErrorPrintStream(out);
		logger.setMessageOutputLevel(Project.MSG_DEBUG);
		p.addBuildListener(logger);

		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);
		p.executeTarget("analyze");
		*/
	}

	void setUpdater(UpdateServlet.Updater updater)
	{
		this.updater = updater;
	}

	public static void main(String[] args) throws Exception
	{
		String stampDir = args[0];
		String apkPath = args[1];
		File apkFile = new File(apkPath);
		StampRunner runner = new StampRunner(stampDir);
		runner.runStamp(apkFile);
	}
}