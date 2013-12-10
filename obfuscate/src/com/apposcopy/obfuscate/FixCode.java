package com.apposcopy.obfuscate;

import soot.*;
import java.util.*;

public class FixCode
{
	public static void main(String[] args)
	{
		FixManifest.main(args);

		String mappingFile = args[0];

		String[] newArgs = new String[args.length-2];
		System.arraycopy(args, 2, newArgs, 0, args.length-2);

		PackManager.v().getPack("jtp").add(new Transform("jtp.sct", new StringConstantsTransformer(mappingFile)));
		PackManager.v().getPack("jtp").add(new Transform("jtp.cit", new CallIndirectionTransformer()));	
		
		soot.Main.main(newArgs);
	}

	
}