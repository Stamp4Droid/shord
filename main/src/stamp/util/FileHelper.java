package stamp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of file manipulation functions.
 */
public class FileHelper {
	public static boolean hasExtension(String fname, String ext) {
		return fname.endsWith("." + ext);
	}

	public static void checkExtension(String fname, String ext) {
		if (!hasExtension(fname, ext)) {
			String msg = "Wrong extension on input file " + fname +
				": ." + ext + " required";
			throw new IllegalArgumentException(msg);
		}
	}

	public static String stripExtension(String fname) {
		int ext_index = fname.lastIndexOf('.');
		if (ext_index < 0) {
			return fname;
		}
		return fname.substring(0, ext_index);
	}

	public static String basename(String fname) {
		return new File(fname).getName();
	}

	public static String changeDir(String fname, String todir) {
		return new File(todir, new File(fname).getName()).toString();
	}

	public static Set<String> splitPath(String path) {
		List<String> parts = StringHelper.split(path, File.pathSeparator);
		Set<String> nonEmptyParts = new HashSet<String>();
		for (String p : parts) {
			if (!p.equals("")) {
				nonEmptyParts.add(p);
			}
		}
		return nonEmptyParts;
	}

	public static File[] listContents(File dir) {
		if (!dir.isDirectory()) {
			String msg = "" + dir + " is not a directory";
			throw new IllegalArgumentException(msg);
		}
		return dir.listFiles();
	}

	public static Set<String> listRegularFiles(File dir, String reqdExt) {
		Set<String> contents = new HashSet<String>();
		for (File f : listContents(dir)) {
			String fName = f.getAbsolutePath();
			if (f.isFile() && hasExtension(fName, reqdExt)) {
				contents.add(fName);
			}
		}
		return contents;
	}

	public static Set<String> listRegularFiles(String dirName,
											   String reqdExt) {
		return listRegularFiles(new File(dirName), reqdExt);
	}

	public static List<File> listSubDirs(File dir) {
		List<File> subdirs = new ArrayList<File>();
		for (File f : listContents(dir)) {
			if (f.isDirectory()) {
				subdirs.add(f);
			}
		}
		return subdirs;
	}

	public static List<File> listSubDirs(String dirName) {
		return listSubDirs(new File(dirName));
	}

	public static Set<Pair<String,String>> matchBasenames(Set<String> set1,
														  Set<String> set2) {
		Set<Pair<String,String>> matchingFiles =
			new HashSet<Pair<String,String>>();
		Map<String,String> set1baseToFile = new HashMap<String,String>();
		for (String f1 : set1) {
			set1baseToFile.put(basename(stripExtension(f1)), f1);
		}
		for (String f2 : set2) {
			String f1 = set1baseToFile.get(basename(stripExtension(f2)));
			if (f1 != null) {
				matchingFiles.add(new Pair<String,String>(f1, f2));
			}
		}
		return matchingFiles;
	}
}
