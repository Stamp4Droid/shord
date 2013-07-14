package stamp.readannot;

import java.io.*;
import java.util.*;
 
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

/*
  @author Saswat Anand
*/
public class AnnotationReader extends ASTVisitor
{
	public static char[] toCharArray(String filePath) throws IOException 
	{
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		StringBuilder content = new StringBuilder();
		while((line = reader.readLine()) != null) {
			content.append(line+"\n");
		}
		return content.toString().toCharArray();
    }

    private String[] classpathEntries;
    private String[] srcpathEntries;
	private PrintWriter writer;
	private static String modelsPath;
	private static String stubsPath;

	public AnnotationReader(String[] srcpathEntries, String[] classpathEntries) throws IOException
	{
		this.srcpathEntries = srcpathEntries;
		this.classpathEntries = classpathEntries;
	}

	private void process(String androidDir) throws Exception
	{
		openWriter(androidDir);
		for(String srcDirPath : srcpathEntries){
			File srcDir = new File(srcDirPath);
			if(!srcDir.isDirectory())
				continue;
			processDir(srcDir, androidDir != null);
		}
		writer.close();
	}
	
	private void openWriter(String dirName) throws IOException
	{
		this.writer = new PrintWriter(new FileWriter("stamp_annotations.txt"));
		if(dirName == null)
			return;
		BufferedReader reader = new BufferedReader(new FileReader(new File(dirName, "stamp_annotations.txt")));
		String line;
		while((line = reader.readLine()) != null){
			writer.println(line);
		}
		reader.close();
	}

	private void processDir(File dir, boolean processingApp) throws Exception
	{
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) 
				processDir(file, processingApp);
			else{
				String fname = file.getName();	
				
				if(fname.endsWith(".java") && fname.indexOf('#') < 0) {
					try{
						//add by yu. this method is also shared by app.
						if (!processingApp){
							File modelsDir = new File(modelsPath);	
							File stubDir = new File(stubsPath);	
							String stubPath = stubDir.getAbsolutePath() + File.separator;
							String modelFilePath = file.getAbsolutePath().substring(stubPath.length());
							File modelFile = new File(modelsDir, modelFilePath);
							
							if (modelFile.lastModified() < file.lastModified()) continue;
						}		
						
						System.out.println("processing "+fname);
						processFile(file);
						//record last modifed time.
						file.setLastModified(System.currentTimeMillis());
					}catch(Exception e){
						System.out.println("\nException occurred while reading annotations of "+fname);
						throw new Error(e);
					}
				}
			}
		}
	}

    private void processFile(File javaFile) throws Exception 
	{
		// set up the parser
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		parser.setCompilerOptions(options);
		
		parser.setEnvironment(this.classpathEntries, this.srcpathEntries, null, true);
		
		String canonicalPath = javaFile.getCanonicalPath();
		parser.setUnitName(canonicalPath);
		parser.setSource(toCharArray(canonicalPath));
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(this);		
    }

	public boolean visit(MethodDeclaration node) 
	{		
		// get annotation position
		List<IExtendedModifier> modifiers = (List<IExtendedModifier>) node.modifiers();
		for(IExtendedModifier modifier : modifiers) {
			if(!modifier.isAnnotation())
				continue;
			Annotation annotation = (Annotation)modifier;
			if(!(annotation.getTypeName().getFullyQualifiedName().equals("STAMP")))
				continue;
			Map<String,Integer> nameToIndex = new HashMap();
			String chordSig = process(node, nameToIndex);				
			MemberValuePair mvp = (MemberValuePair) ((NormalAnnotation) annotation).values().get(0);
			assert mvp.getName().getIdentifier().equals("flows");
			ArrayInitializer arrayInitializer = (ArrayInitializer) mvp.getValue();
			
			List<Expression> expressions = arrayInitializer.expressions();
			for(Expression expression : expressions) {
				NormalAnnotation flowAnnotation = (NormalAnnotation) expression;
				assert flowAnnotation.getTypeName().getFullyQualifiedName().equals("Flow");
				
				List<MemberValuePair> flow = (List<MemberValuePair>) flowAnnotation.values();
				String from = null; String to = null;
				for(MemberValuePair mvp1 : flow){
					String name = mvp1.getName().getIdentifier();
					if(name.equals("from"))
						from = ((StringLiteral) mvp1.getValue()).getLiteralValue();
					else if(name.equals("to"))
						to = ((StringLiteral) mvp1.getValue()).getLiteralValue();
				}
				writer.println(chordSig + " " + 
							   processEndPoint(from, nameToIndex) + " " + 
							   processEndPoint(to, nameToIndex));
			}
		}		
		return true;
    }

	private String processEndPoint(String endPoint, Map<String,Integer> nameToIndex)
	{
		char c = endPoint.charAt(0);
		if(c == '$'){
			return endPoint; //e.g., $getDeviceId
		} else if(c == '!'){
			String t = endPoint.substring(1);
			checkSanity(t);
			Integer paramIndex = nameToIndex.get(t);
			if(paramIndex != null)
				return '?'+paramIndex.toString(); //! followed by param name
			else
				return endPoint; // e.g., !Internet
		} else {
			Integer paramIndex = nameToIndex.get(endPoint); //endPoint must be a param name
			return paramIndex.toString();
		}
	}

	private void checkSanity(String s) 
	{
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return; 
		}
		throw new Error("labels are expected to be non-integers: "+s);
	}

	private String process(MethodDeclaration node, Map<String,Integer> nameToIndex)
	{
		int offset;
		if(Modifier.isStatic(node.getModifiers())){
			offset = 0;
		} else {
			offset = 1;
			nameToIndex.put("this", 0);
		}
		String rtype;
		String mname;
		if(node.isConstructor()){
			rtype = "V";
			mname = "<init>";
		} else {
			ITypeBinding retType = node.getReturnType2().resolveBinding().getErasure();
			rtype = toChordType(retType);
			mname = node.getName().getIdentifier();
		}
		if(!rtype.equals("V"))
			nameToIndex.put("@return", -1);

		//build the chord signature and fill the nameToIndex map
		StringBuilder builder = new StringBuilder();		
		builder.append(mname);
		builder.append(":(");
		List params = node.parameters();
		for(int i = 0; i < params.size(); i++){
			SingleVariableDeclaration p = (SingleVariableDeclaration) params.get(i);
			
			//parameter name
			String pname = p.getName().getIdentifier();
			nameToIndex.put(pname, offset+i);
			
			//System.out.println("** " + p.getType() + " " + ((p.getType().resolveBinding()==null) ? "null" : ""));
			ITypeBinding type = p.getType().resolveBinding().getErasure();
			String ptype = toChordType(type);
			if(p.isVarargs())
				ptype = "["+ptype;
			//System.out.println(pname + " " + ptype + " " + type.isArray() + " " + p.getType().resolveBinding().getBinaryName() + " " + p.isVarargs());
			builder.append(ptype);
		}
		builder.append(")");
		builder.append(rtype);
		
		builder.append("@");
		ASTNode parent = node.getParent();
		ITypeBinding containerType = null;
		if(parent instanceof AbstractTypeDeclaration){
			AbstractTypeDeclaration td = (AbstractTypeDeclaration) parent;
			containerType = td.resolveBinding();
		}else if(parent instanceof AnonymousClassDeclaration){
			containerType = ((AnonymousClassDeclaration) parent).resolveBinding();
		}
		else
			throw new RuntimeException(parent.getClass().toString());
		builder.append(containerType.getErasure().getBinaryName());
		String chordSig = builder.toString();
		return chordSig;
	}


	private static String toChordType(ITypeBinding type)
	{
		if(type.isPrimitive()){
			return type.getBinaryName();
		}
		
		if(type.isArray()){
			ITypeBinding elemType = type.getElementType();
			int dim = type.getDimensions();
			String typeStr;
			if(elemType.isPrimitive())
				typeStr = elemType.getBinaryName();
			else
				typeStr = "L".concat(toChordRefType(elemType)).concat(";");
			for(int i = 0; i < dim; i++)
				typeStr = "[".concat(typeStr);
			return typeStr;
		}
			
		return "L".concat(toChordRefType(type)).concat(";");
	}
	
	private static String toChordRefType(ITypeBinding type)
	{
		ITypeBinding declKlass = type.getDeclaringClass();
		if(declKlass != null){
			String cname;
			if(type.isAnonymous())
				cname = "anonymous";
			else
				cname = type.getName();
			return toChordRefType(declKlass).concat("$").concat(cname);
		}
		return type.getBinaryName().replace('.', '/');
	}
	
	public static void main(String[] args) throws Exception
    {
		stubsPath = args[0];
		modelsPath = args[2];
		
		for(String arg : args)
			System.out.println(arg);

		String androidJar = null;
		if(args.length == 4) 
			androidJar = args[3];
		
		new AnnotationReader(args[0].split(":"), args[1].split(":")).process(androidJar);
    }

}
