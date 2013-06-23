package stamp.modelwiz;

import java.util.*;
import java.io.*;
import japa.parser.*;
import japa.parser.ast.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.*;
import japa.parser.ast.body.*;
import japa.parser.ast.visitor.*;
import japa.parser.ast.type.*;


public class Main
{
	private File stubsDir;
	private Scanner scanIn;
	private String className;
	private String methodName;

	public static void main(String[] args)
	{
		String stubsDirName = args[0];
		Main main = new Main(stubsDirName);
		
		if(args.length > 1)
			main.className = args[1];
		
		if(args.length > 2)
			main.methodName = args[2];
		
		try{
			main.perform();
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	Main(String stubsDirName)
	{
		this.stubsDir = new File(stubsDirName);
		this.scanIn = new Scanner(System.in);
	}

	void perform() throws Exception
	{
		if(className == null){
			System.out.println("\n*************************");
			System.out.println("* Step 1. Choose class *");
			System.out.println("*************************");
			//System.out.print("\nClass name: ");
			className = scanIn.nextLine();
		}
		//System.out.println(className);
		
		File stubFile = getStubFile(className);
		if(!stubFile.exists()){
			System.out.println("Class does not exist!");
			scanIn.close();            
			return;
		}

		CompilationUnit stubCU = getCU(stubFile);
		List<TypeDeclaration> stubTypes = stubCU.getTypes();
		
        for(TypeDeclaration stubType : stubTypes){
			handleType(stubType);
		}
	}

	void handleType(TypeDeclaration stubType)
	{
		if(methodName == null) {
			System.out.println("\n*************************");
			System.out.println("* Step 2. Choose method *");
			System.out.println("*************************");
			System.out.print("\nMethod name: ");
			methodName = scanIn.nextLine();
		}
		boolean approx = false;
		if(methodName.endsWith("*")){
			approx = true;
			methodName = methodName.substring(0, methodName.length()-1);
		}

		List<BodyDeclaration> matchedMethods = new ArrayList();
		List<BodyDeclaration> stubMembers = stubType.getMembers();
		if(stubMembers != null){
			for(BodyDeclaration stubMember : stubMembers){
				if(stubMember instanceof MethodDeclaration || 
				   stubMember instanceof ConstructorDeclaration){
					String name = methodName(stubMember);
					boolean match = approx ? name.startsWith(methodName) : name.equals(methodName);
					if(match){
						matchedMethods.add(stubMember);
					}
				} 
			}
		}
		
		BodyDeclaration chosenMethod;
		if(matchedMethods.isEmpty()){
			System.out.println("No methods with matching name found.\n");
		} else{
			if(matchedMethods.size() == 1)
				chosenMethod = matchedMethods.get(0);
			else{
				int count = 0;
				System.out.println("\nFollowing "+ matchedMethods.size() + " methods with matching names found.");
				for(BodyDeclaration stubMember : matchedMethods){
					String stubMethSig = signature(stubMember); 
					System.out.println(count+++". "+stubMethSig);
				}
				System.out.print("\nEnter the index to choose the method: ");
				chosenMethod = matchedMethods.get(getChoice(count-1));
			}
			System.out.println("Chosen method: "+signature(chosenMethod));
			handleMethod(chosenMethod);
		}
	}

	void handleMethod(BodyDeclaration method)
	{
		System.out.println("\n********************************");
		System.out.println("* Step 3. Choose annotations   *");
		System.out.println("********************************");
		
		StringBuilder newBody = new StringBuilder();
		List<String> annots = getAllAnnotations(method);
		if(annots.size() == 0){
			System.out.println("No possible annotations for this method");
		} else {
			int count = 0;
			System.out.println("Following are all possible annotations for the chosen method.");
			for(String an : annots){
				System.out.println(count+++". "+an);
			}
			System.out.print("\nEnter the white-space-separated indices of annotations (Press enter to not accept any): ");
			int[] choices = getChoices(count-1);
			if(choices == null){
				System.out.println("No annotations to be added.");
			} else {
				System.out.println("Following annotation to be added:");
				newBody.append("@STAMP(flows={");
				boolean first = true;
				for(int c : choices){
					System.out.println(c+". "+annots.get(c));
					String[] toks = annots.get(c).split(" => ");
					String an = "@Flow(from=\""+toks[0]+"\", to=\""+toks[1]+"\")";
					if(!first)
						newBody.append(", ");
					newBody.append(an);
					first = false;
				}
				newBody.append("})\n");
			}

			System.out.println("\n*********************************************************");
			System.out.println("* Step 4. Enter code. Type ^d at the end of your input. *");
			System.out.println("*********************************************************");
		
			StringBuilder code = new StringBuilder();
			boolean first = true;
			System.out.print("\t");
			while (scanIn.hasNext()) {
				if(!first)
					code.append("\n");
				System.out.print("\t");
				code.append(scanIn.nextLine());
				first = false;
			}
			newBody.append(method.toString().replace("throw new RuntimeException(\"Stub!\");", 
													 code.toString()));
			System.out.println("\nFollowing code will be added to the model of "+className);
			System.out.println(newBody);
		}
	}
	
	int[] getChoices(int maxChoice)
	{
		outer:
		do{
			try{
				String input = scanIn.nextLine();
				if(input.trim().length() == 0)
					return null;
				String[] tokens = input.split(" ");
				int[] choices = new int[tokens.length];
				int i = 0;
				for(String token : tokens){
					int choice = Integer.parseInt(token);
					if(choice >= 0 && choice <= maxChoice)
						choices[i++] = choice;
					else{
						System.out.println("Expecting a list of integers in the range of [0,"+maxChoice+"], separated by blank spaces.");
						continue outer;
					}
				}
				return choices;
			}catch(NumberFormatException e){
				System.out.println("Expecting a list of integers in the range of [0,"+maxChoice+"], separated by blank spaces.");
			}
		}while(true);
	}

	int getChoice(int maxChoice)
	{
		do{
			try{
				String input = scanIn.nextLine();
				String[] tokens = input.split(" ");
				if(tokens.length > 1){
					System.out.println("Expecting an integer input in the range [0,"+maxChoice+"].");
					continue;
				}
				int choice = Integer.parseInt(tokens[0]);
				if(choice >= 0 && choice <= maxChoice)
					return choice;
				System.out.println("Expecting an integer input in the range [0,"+maxChoice+"].");
			}catch(NumberFormatException e){
				System.out.println("Expecting an integer input in the range [0,"+maxChoice+"].");
			}
		}while(true);
	}

	File getStubFile(String className)
	{
		String filePath = className.replace('.', '/')+".java";
		File stubFile = new File(stubsDir, filePath);
		return stubFile;
	}

	CompilationUnit getCU(File file) throws Exception
	{
        FileInputStream in = new FileInputStream(file);

        CompilationUnit cu;
        try {
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
		return cu;
	}

	private String methodName(BodyDeclaration meth)
	{
		if(meth instanceof MethodDeclaration){
			MethodDeclaration method = (MethodDeclaration) meth;
			return method.getName();
		}
		else if(meth instanceof ConstructorDeclaration){
			ConstructorDeclaration method = (ConstructorDeclaration) meth;
			return "<init>";
		}
		return null;
	}

	private List<String> getAllAnnotations(BodyDeclaration meth)
	{
		List<Parameter> params;
		boolean isVoidReturnType = true;
		boolean isStatic = false;
		if(meth instanceof MethodDeclaration){
			MethodDeclaration method = (MethodDeclaration) meth;
			params = method.getParameters();
			isStatic = ModifierSet.isStatic(method.getModifiers());
			isVoidReturnType = method.getType().toString().equals("void");
		}
		else if(meth instanceof ConstructorDeclaration){
			ConstructorDeclaration method = (ConstructorDeclaration) meth;
			params = method.getParameters();
		}		
		else
			throw new RuntimeException("");
		return getAllAnnotations(params, isStatic, isVoidReturnType);
	}

	private List<String> getAllAnnotations(List<Parameter> params, boolean isStatic, boolean isVoidReturnType)
	{
		//System.out.println("isStatic " +isStatic);
		List<String> annots = new ArrayList();
		int paramCount = params == null ? 0 : params.size();
		for(int i = 0; i < paramCount; i++){
			for(int j = 0; j < paramCount; j++){
				if(i == j)
					continue;
				annots.add(params.get(i).getId().getName() + " => " + params.get(j).getId().getName());
			}
			if(!isStatic)
				annots.add(params.get(i).getId().getName() + " => this");
			if(!isVoidReturnType)
				annots.add(params.get(i).getId().getName() + " => return");
		}
		if(!isStatic){
			for(int i = 0; i < paramCount; i++){
				annots.add("this => "+params.get(i).getId().getName());
			}
			if(!isVoidReturnType)
				annots.add("this => return");
		}
		return annots;
	}

	private String signature(BodyDeclaration meth)
	{
		if(meth instanceof MethodDeclaration){
			MethodDeclaration method = (MethodDeclaration) meth;
			return signature(method.getName(), method.getType(), method.getParameters());
		}
		else if(meth instanceof ConstructorDeclaration){
			ConstructorDeclaration method = (ConstructorDeclaration) meth;
			return signature("<init>", new VoidType(), method.getParameters());
		}
		else if(meth instanceof InitializerDeclaration){
			return "<clinit>";
		}
		throw new RuntimeException(meth.toString());
	}

	private String signature(String name, Type retType, List<Parameter> params)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(retType.toString()+" ");
		builder.append(name);
		builder.append("(");
		
		if(params != null){
			int i = params.size();
			for(Parameter p : params){
				builder.append(p.getType().toString()+" "+p.getId().getName());
				if(i > 1)
					builder.append(", ");
				i--;
			}
		}
		builder.append(")");
		return builder.toString();
	}

}