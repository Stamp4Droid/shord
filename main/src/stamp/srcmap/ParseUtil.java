package stamp.srcmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public abstract class ParseUtil {

    private static final Pattern chordMethodPattern = Pattern.compile("^([^:]+):\\(([^\\)]*)\\)([^@]*)@(.*)$");
    private static final Pattern sootMethodPattern = Pattern.compile("^<([^:]+): ([\\w\\.\\$]+(?:\\[\\])*) ('[\\w<>\\$]+'|[\\w<>\\$]+)\\((.*)\\)>$");
    
    private static final Map<String, String> typeToBCTypeDict;
    private static final Map<String, String> bcTypeToTypeDict;
    
    public static List<String> parseList(String s) {
        assert s.charAt(0) == '[';
        int len = s.length();
        assert s.charAt(len-1) == ']';
        s = s.substring(1,len-1);
        List<String> list = new ArrayList<String>();
        for(String s1 : s.split(",")) {
            s1 = s1.trim();
            if(!s1.equals("")) list.add(s1);
        }
        return list;
    }
    
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("byte","B");
        m.put("char","C");
        m.put("double","D");
        m.put("float","F");
        m.put("int","I");
        m.put("long","J");
        m.put("short","S");
        m.put("void","V");
        m.put("boolean","Z");
        Map<String, String> revM = new HashMap<String, String>();
        for(Map.Entry<String, String> pair : m.entrySet()) {
            revM.put(pair.getValue(), pair.getKey());
        }
        typeToBCTypeDict = Collections.unmodifiableMap(m);
        bcTypeToTypeDict = Collections.unmodifiableMap(revM);
    }
    
    public static boolean isJavaPrimitiveType(String typeStr) {
        return typeToBCTypeDict.containsKey(typeStr);
    }

    public static String typeToChordSignature(String typeStr) {
        if(typeToBCTypeDict.containsKey(typeStr)) {
            return typeToBCTypeDict.get(typeStr);
        } else if(typeStr.endsWith("[]")) {
            return "[" + typeToChordSignature(typeStr.substring(0, typeStr.length() - 2));
        } else {
            return "L" + typeStr.replace('.', '/') + ";";
        }
    }
    
    public static List<String> chordTypeSignatureToSootTypes(String typeStr) {
        List<String> l = new ArrayList<String>();
        int pos = 0;
        int arrayCount = 0;
        while(pos < typeStr.length()) {
            char c = typeStr.charAt(pos);
            String s;
            if(c == 'L') {
                s = typeStr.substring(pos + 1).split(";")[0].replace('/', '.');
                pos += s.length() + 2;
            } else if(c == '[') {
                arrayCount++;
                pos++;
                continue;
            } else {
                s = bcTypeToTypeDict.get(Character.toString(c));
                assert s != null;
                pos++;
            }
            while(arrayCount > 0) {
                s += "[]";
                arrayCount--;
            }
            l.add(s);
        }
        return l; 
    }
    
    private static String chordToSootMethodSignature(String s, boolean isSubsignature) {
        Matcher m = chordMethodPattern.matcher(s);
        if(!m.matches()) {
            throw new Error(
                String.format("Invalid chord method signature: \'%s\' (doesn't match regular expression \"%s\"",
                        s, chordMethodPattern.toString()));
        }
        String name = m.group(1);
        String paramChordStr = m.group(2);
        List<String> params = chordTypeSignatureToSootTypes(paramChordStr);
        String paramsStr = "";
        for(int i = 0; i < params.size()-1; i++) {
            paramsStr += params.get(i) + ",";
        }
        if(params.size() > 0) {
            paramsStr += params.get(params.size()-1);
        }
        String returnTypeChordStr = m.group(3);
        List<String> returnTypeList = chordTypeSignatureToSootTypes(returnTypeChordStr);
        assert returnTypeList.size() == 1;
        String returnType = returnTypeList.get(0);
        String klass = m.group(4);
        String signature;
        if(isSubsignature) {
            signature = String.format(": %s %s(%s)", returnType, name, paramsStr);
        } else {
            signature = String.format("<%s: %s %s(%s)>", klass, returnType, name, paramsStr);
        }
        return signature;
    }     
    
    public static String chordToSootMethodSignature(String s) {
        return chordToSootMethodSignature(s, false);
    }
    
    public static String chordToSootMethodSubSignature(String s) {
        return chordToSootMethodSignature(s, true);
    }
    
    public static String sootToChordMethodSignature(String s) {
    	Matcher m = sootMethodPattern.matcher(s);
        if(!m.matches()) {
            throw new Error(
                String.format("Invalid soot method signature: \'%s\' (doesn't match regular expression \"%s\"",
                        s, sootMethodPattern.toString()));
        }
        String klass = m.group(1);
        String returnType = typeToChordSignature(m.group(2));
        String name = m.group(3);
        String[] argList = m.group(4).split(",");
        String chordArgs = "(";
        for(String arg: argList) {
        	if(!arg.equals("")) {
        		chordArgs += typeToChordSignature(arg);
        	}
        }
        chordArgs += ")";
        return name + ":" + chordArgs + returnType + "@" + klass;
    }
    
}
