package stamp.injectannot;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.AbstractJasminClass;
import shord.program.Program;

public class NativeMethodAnnotation extends AnnotationInjector.Visitor
{
	protected void postVisit()
	{
	}

	protected void visit(SootClass klass)
    {
        Program prog = Program.g();
		for(SootMethod method : klass.getMethods()){
			if(!method.isNative() || prog.exclude(method))
				continue;
			String methSig = getChordMethodSigFor(method);

			String label = methSig;

			//each param is a sink
			int i = 0;
			if(!method.isStatic()){
				writeAnnotation(methSig, "0", "!"+label);
				i++;
			}
			for(int j = 0; i < method.getParameterCount(); j++,i++)
				writeAnnotation(methSig, String.valueOf(i), "!"+label);
			
			//return value if any is sensitive
			//but return value does not point to anything unless we write a model
			//if(!(method.getReturnType() instanceof VoidType)){
			//	writeAnnotation(methSig, "$"+label, "-1");
			//}
				
		}
    }
	
	public static String getChordMethodSigFor(SootMethod method)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(method.getName()).append(":(");
		for(Type ptype : method.getParameterTypes())
			builder.append(AbstractJasminClass.jasminDescriptorOf(ptype));
		builder.append(')');
		builder.append(AbstractJasminClass.jasminDescriptorOf(method.getReturnType()));
		builder.append('@');
		builder.append(method.getDeclaringClass().getName());
		return builder.toString();
	}
}
