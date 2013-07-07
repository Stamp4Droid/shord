package stamp.util;

/**
 * Collection of useful functions for manipulating system properties.
 */
public class PropertyHelper {
	public static String getProperty(String propName) {
		String propValue = System.getProperty(propName);
		if (propValue == null) {
			String msg = "Required system property " + propName + " not set";
			throw new IllegalStateException(msg);
		}
		return propValue;
	}
}
