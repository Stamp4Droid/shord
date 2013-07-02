package stamp.reporting;

import java.util.*;
import java.io.*;

import javax.xml.*;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class FileManager
{
    // path data
    private String rootPath;
    private String outPath;

	private List<File> srcDirs = new ArrayList();
    //private StampVisitor srcVisitor;

    // set of files and corresponding info
    //private HashMap<String,SourceData> filePathToSourceData = new HashMap();
    private HashMap<String,String> filePathToAnnotatedSource = new HashMap();
    private HashMap<String,String> modelFilePathToAnnotatedSource = new HashMap();


    public FileManager(String rootPath, String outPath, String libPath, String srcPath) throws IOException 
	{
		this.rootPath = rootPath;
		this.outPath = outPath;
		
		List<String> srcpathEntries = new ArrayList();
		for(String sd : srcPath.split(":")){
			File d = new File(sd);
			if(d.exists()){
				srcDirs.add(d);
				srcpathEntries.add(d.getCanonicalPath());
			}
		}
		
		srcpathEntries.add(rootPath+"/models/api-16/gen");
		srcpathEntries.add(rootPath+"/models/src");

		List<String> classpathEntries = new ArrayList();
		for(String j : libPath.split(":")){
			File jar = new File(j);
			if(jar.exists())
				classpathEntries.add(j);
		}
					
		//this.srcVisitor = new StampVisitor(srcpathEntries, classpathEntries);
    }
	
	private void add(String filePath, boolean isModel) throws Exception 
	{	
		if(isModel){
			File file = new File(rootPath+"/models/src", filePath);
			if(file.exists()){
				SourceProcessor sp = new SourceProcessor(file);
				String annotatedSource = sp.getSourceWithAnnotations();
				modelFilePathToAnnotatedSource.put(filePath, annotatedSource);
			}
			return;
		} 

		String srcMapDirPath = null;
		File file = new File(rootPath+"/models/api-16/gen", filePath);
		if(file.exists())
			srcMapDirPath = rootPath+"/models/api-16/srcmap";
		else {
			for(File sd : srcDirs){
				file = new File(sd, filePath);
				if(file.exists()){
					srcMapDirPath = outPath+"/srcmap/";
					break;
				}
			}
		}

		System.out.println("DEBUG: " + srcMapDirPath + " " + file.getCanonicalPath());
		
		if(srcMapDirPath == null)
			return;
		
		//SourceData data = srcVisitor.process(file);
		File taintedInfoFile = new File(outPath+"/results/TaintedVar.xml");
		File allReachableFile = new File(outPath+"/results/AllReachable.xml");
		File reachedFile = new File(outPath+"/results/reachedmethods.xml");


		//replace .java with .xml
		String fname = filePath.substring(0, filePath.length()-4).concat("xml");
		File srcMapFile = new File(srcMapDirPath+"/"+fname);

		SourceProcessor sp = new SourceProcessor(file, srcMapFile, taintedInfoFile, allReachableFile, reachedFile, filePath);
		String annotatedSource = sp.getSourceWithAnnotations();
		//System.out.println("FILEPATH: "+filePath+"\n"+annotatedSource);
		//filePathToSourceData.put(filePath, data);
		filePathToAnnotatedSource.put(filePath, annotatedSource);
    }
    
	/*
	public SourceData getSourceData(String filePath, boolean isModel) throws Exception 
	{
		if(isModel)
			return null;
		if(!filePathToSourceData.containsKey(filePath)) {
			add(filePath, isModel);
		}
		return filePathToSourceData.get(filePath);
		}*/
	
    public String getAnnotatedSource(String filePath, boolean isModel) 
	{
		try{
			if(isModel){
				if(!modelFilePathToAnnotatedSource.containsKey(filePath)) {
					add(filePath, isModel);
				}
				return modelFilePathToAnnotatedSource.get(filePath);
			}
			else{
				if(!filePathToAnnotatedSource.containsKey(filePath)) {
					add(filePath, isModel);
				}
				return filePathToAnnotatedSource.get(filePath);
			}
		}catch(Exception e){
			throw new Error(e);
		}
    }

	public String getClassInfo(String chordSig)
	{
		try{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			File fileNamesFile = new File(outPath+"/results/FileNames.xml");
			Document document = builder.parse(fileNamesFile);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String query = "//tuple[@chordsig=\""+chordSig+"\"]";
			Element node = (Element) xpath.evaluate(query, document, XPathConstants.NODE);
			return node.getAttribute("srcFile")+","+node.getAttribute("lineNum");
		}catch(Exception e){
			throw new Error(e);
		}
	}

	public boolean isFrameworkFile(String filePath)
	{
		return new File(rootPath+"/models/api-16/gen", filePath).exists();
	}
	
	public static String readFile(File file) throws IOException 
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder fileBuilder = new StringBuilder();
		String line;
		while((line = br.readLine()) != null) {
			fileBuilder.append(line);
			fileBuilder.append("\n");
		}
		return fileBuilder.toString();
    }
	
    public static void writeFile(File file, String string) throws IOException 
	{
		file.getParentFile().mkdirs();
		PrintWriter pw = new PrintWriter(file);
		pw.print(string);
		pw.close();
    }

}
