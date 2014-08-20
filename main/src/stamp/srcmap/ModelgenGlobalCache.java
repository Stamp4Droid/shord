package stamp.srcmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelgenGlobalCache {

	private static class MethodModelEntry {
        private final String from;
        public String getFrom() { return from; }
        private final String to;
        public String getTo() { return to; }
        
        public MethodModelEntry(String from, String to) {
            this.from = from;
            this.to = to;
        }
        
        public boolean equals(Object o) {
            if(!(o instanceof MethodModelEntry)) return false;
            MethodModelEntry other = (MethodModelEntry)o;
            return this.from.equals(other.from) && this.to.equals(other.to);
        }
        
        public String toString() {
        	return from + "->" + to;
        }
    }
    
    public static class MethodModel implements Iterable<MethodModelEntry> {
        private final List<MethodModelEntry> entries;
        private final String method;
        public String getMethod() { return method; }
        private final boolean missing;
        public boolean isMissingModel() { return missing; }
        
        private MethodModel(String method, boolean missing) {
            this.entries = new ArrayList<MethodModelEntry>();
            this.method = method;
            this.missing = missing;
        }
        
        public MethodModel(String method) {
            this(method, false);
        }
        
        private static MethodModel missingMethodModel = null;
        public static synchronized MethodModel getMissingMethodModel() {
        	if(missingMethodModel == null) {
        		missingMethodModel = new MethodModel(null,true);
        	}
        	return missingMethodModel;
        }
        
        // Preserves order and uniqueness
        public void add(MethodModelEntry entry) {
            for(int i = 0; i < entries.size(); i++) {
                MethodModelEntry e = entries.get(i);
                int compare = entry.getFrom().compareTo(e.getFrom());
                if(compare < 0) continue;
                else if(compare > 0) {
                    entries.add(i, entry);
                    return;
                } 
                assert compare == 0;
                
                compare = entry.getTo().compareTo(e.getTo());
                if(compare < 0) continue;
                else if(compare > 0) {
                    entries.add(i, entry);
                    return;
                }
                assert compare == 0;
                
                return; // duplicated entry
            }
            // Add last
            entries.add(entry);
        }
        
        public void add(String from, String to) {
            add(new MethodModelEntry(from, to));
        }
        
        public void parseAndAddEntry(String entry) {
        	Pattern pattern = Pattern.compile("^\t*([a-zA-Z0-9#]+)->([a-zA-Z0-9#]+)$");
        	Matcher matcher = pattern.matcher(entry);
	        if(!matcher.find()){
	        	throw new Error("Invalid modelgen entry.");
	        }
	        this.add(matcher.group(1),matcher.group(2));
        }
        
        public void add(MethodModel model) {
            for(MethodModelEntry entry : model)
                this.add(entry);
        }
        
        public Iterator<MethodModelEntry> iterator() {
            return Collections.unmodifiableList(entries).iterator();
        }
        
        public boolean equals(Object o) {
            if(!(o instanceof MethodModel)) return false;
            MethodModel other = (MethodModel)o;
            if(this.entries.size() != other.entries.size()) return false;
            for(int i = 0; i < entries.size(); i++) {
                if(!entries.get(i).equals(other.entries.get(i))) return false;
            }
            return true;
        }
    }
	
	private static Map<String,MethodModel> modelgenModels = new HashMap<String,MethodModel>();
	
	public static void load(String filename) {
		File f = new File(filename);
		BufferedReader reader = null;
		try{
			FileReader freader = new FileReader(f);
			reader = new BufferedReader(freader);
			String line = reader.readLine();
			String currentMethod = null;
			MethodModel currentModel = null;
			while(line != null){
				if (!line.trim().isEmpty() && !line.startsWith("#")){
					if(line.startsWith("\t")) {
						// Model entry
						if(currentMethod == null || currentModel == null) {
							throw new Error("Invalid models file.");
						}
						currentModel.parseAndAddEntry(line);
					} else {
						// Method name
						if(currentMethod != null) {
							// Save previous method and model
							modelgenModels.put(currentMethod, currentModel);
						}
						currentMethod = line;
						currentModel = new MethodModel(currentMethod);
						
					}
				}
				line = reader.readLine();
			}
			// Save last method and model
			if(currentMethod != null) modelgenModels.put(currentMethod, currentModel);
			reader.close();
		} catch(IOException e) {
			throw new Error(e);
		}
	}
	
	public static boolean hasEntryForMethod(String sootSig) {
		return modelgenModels.containsKey(sootSig);
	}
	
	public static Iterable<String> listMethods() {
		return modelgenModels.keySet();
	}
	
	public static Iterable<String> linesForMethod(String sootSig) {
		List<String> lines = new ArrayList<String>();
		MethodModel model = modelgenModels.get(sootSig);
		String chordSig = ParseUtil.sootToChordMethodSignature(sootSig);
		if(model != null) {
			for(MethodModelEntry entry: model) {
				String from = entry.getFrom();
				String to = entry.getTo();
				int fromNum = -2;
				int toNum = -2;
				if(from.equals("this")) {
					fromNum = 0;
				} else {
					if(from.startsWith("arg#")) {
						try{
						fromNum = Integer.parseInt(from.substring(4));
						} catch(NumberFormatException e) {
							throw new Error("Unrecognized argument specifier: " + from);
						}
					} else {
						throw new Error("Unrecognized argument specifier: " + from);
					}
				}
				if(to.equals("this")) {
					toNum = 0;
				} else if(to.equals("return")) {
					toNum = -1;
				} else {
					if(to.startsWith("arg#")) {
						try{
						toNum = Integer.parseInt(to.substring(4));
						} catch(NumberFormatException e) {
							throw new Error("Unrecognized argument specifier: " + to);
						}
					} else {
						throw new Error("Unrecognized argument specifier: " + to);
					}
				}
				lines.add(chordSig + " " + fromNum + " " + toNum);
			}
		}
		return lines;
	}
}
