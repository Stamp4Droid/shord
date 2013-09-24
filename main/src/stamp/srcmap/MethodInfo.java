package stamp.srcmap;

import java.util.List;

/**
 * @author Saswat Anand 
 */
public interface MethodInfo {
	public abstract List<Marker> markers(int line, String markerType, String sig);
}	
