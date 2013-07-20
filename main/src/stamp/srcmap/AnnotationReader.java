package stamp.srcmap;

import java.io.*;
import java.util.*;
 
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

/*
  @author Saswat Anand
*/
public class AnnotationReader extends ASTVisitor
{
	private String file;

	public AnnotationReader(String file)
	{
		this.file = file;
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
				try{
					Main.writeAnnot(chordSig + " " + 
									processEndPoint(from, nameToIndex) + " " + 
									processEndPoint(to, nameToIndex));
				}catch(InvalidEndPointException e){
					throw new Error("Error occurred while processing "+file+"\n. Invalid Annotation on method " + node + ". "+e.getMessage());
				}
			}
		}		
		return true;
    }

	private String processEndPoint(String endPoint, Map<String,Integer> nameToIndex) throws InvalidEndPointException
	{
		char c = endPoint.charAt(0);
		if(c == '$'){
			return endPoint; //e.g., $getDeviceId
		} else if(c == '!'){
			String t = endPoint.substring(1);
			if(isInteger(t))
				throw new InvalidEndPointException("labels are expected to be non-integers", endPoint);
			Integer paramIndex = nameToIndex.get(t);
			if(paramIndex != null)
				return '?'+paramIndex.toString(); //! followed by param name
			else
				return endPoint; // e.g., !Internet
		} else {
			Integer paramIndex = nameToIndex.get(endPoint); //endPoint must be a param name
			if(paramIndex == null)
				throw new InvalidEndPointException("endPoint isnt a param name", endPoint);
			else
				return paramIndex.toString();
		}
	}

	private boolean isInteger(String s) 
	{
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return false;
		}
		return true;
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
	
	static class InvalidEndPointException extends Exception
	{
		InvalidEndPointException(String message, String endPoint){
			super(message + ". Endpoint = "+endPoint);
		}
	}
}
