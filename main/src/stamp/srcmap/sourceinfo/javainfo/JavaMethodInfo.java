package stamp.srcmap.sourceinfo.javainfo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.srcmap.Marker;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractMethodInfo;

/**
 * @author Saswat Anand 
 */
public class JavaMethodInfo extends AbstractMethodInfo {
	private Map<Integer,List<Marker>> lineToMarkers = new HashMap();

	JavaMethodInfo(String chordMethodSig, String className, File file) {
		super(chordMethodSig, className, file);
	}
}
