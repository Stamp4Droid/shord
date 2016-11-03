package shord.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import shord.util.Utils;

/**
 * Entry point of Chord before JVM settings are resolved.
 *
 * Resolves JVM settings and spawns Chord in a fresh JVM process with those settings.
 *
 * The system properties in the current JVM are altered as follows (in order):
 *
 * 1. Property chord.main.dir is set to the directory containing file chord.jar
 *    from which this class is loaded.
 *
 * 2. All properties from file "[chord.main.dir]/chord.properties" are loaded, if the
 *    file exists.
 *
 * 3. Property chord.work.dir is set to "[user.dir]" unless the user has defined it;
 *    in either case, its value is canonicalized, and Chord exits if it is not a
 *    valid existing directory.
 *
 * 4. All properties from file "[chord.work.dir]/chord.properties" are loaded, if the
 *    file exists, unless the user has defined property chord.props.file, in which
 *    case all properties from the file specified by that property are loaded; in the
 *    latter case, Chord exits if the file cannot be read.
 *
 * 5. The following properties are set to the following values unless the user has
 *    already defined them:
 *
 *    Property name    Default value
 *
 *    chord.max.heap  "2048m"
 *    chord.max.stack "32m"
 *    chord.jvmargs   "-ea -Xmx[chord.max.heap] -Xss[chord.max.stack]"
 *    chord.classic   "true"
 *
 *    chord.std.java.analysis.path "[chord.main.dir]/chord.jar"
 *    chord.ext.java.analysis.path ""
 *    chord.java.analysis.path     "[chord.std.java.analysis.path]:[chord.ext.java.analysis.path]"
 *
 *    chord.std.dlog.analysis.path "[chord.main.dir]/chord.jar"
 *    chord.ext.dlog.analysis.path ""
 *    chord.dlog.analysis.path     "[chord.std.dlog.analysis.path]:[chord.ext.dlog.analysis.path]"
 *   
 *    chord.class.path ""
 *
 * 6. Property user.dir is set to "[chord.work.dir]".
 *
 * 7. Property java.class.path is set to
 *    "[chord.main.dir]/chord.jar:[chord.java.analysis.path]:[chord.dlog.analysis.path]:[chord.class.path]".
 *
 * The above altered properties plus all other system properties in the current JVM
 * are passed on to the new JVM.
 *
 * Note: Do not refer to any properties defined in class chord.project.Config here.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Boot {
    private static final String WARN_DUPLICATE_SYSPROP =
        "WARN: Property '%s' defined multiple times; assuming value '%s' instead of '%s'.";
    private static final String CHORD_JAR_NOT_FOUND =
        "ERROR: Boot: Expected Chord to be loaded from chord.jar instead of from '%s'.";
    private static final String USER_DIR_AS_CHORD_WORK_DIR =
        "WARN: Boot: Property chord.work.dir not set; using value of user.dir '%s' instead.";
    private static final String CHORD_MAIN_DIR_UNDEFINED =
        "ERROR: Boot: Property chord.main.dir must be set to location of directory named 'main' in your Chord installation.";
    private static final String CHORD_MAIN_DIR_NOT_FOUND =
        "ERROR: Boot: Directory '%s' specified by property chord.main.dir not found.";
    private static final String CHORD_WORK_DIR_UNDEFINED =
        "ERROR: Boot: Property chord.work.dir must be set to location of working directory desired during Chord's execution.";
    private static final String CHORD_WORK_DIR_NOT_FOUND =
        "ERROR: Boot: Directory '%s' specified by property chord.work.dir not found.";

    static String mainDirName;

    public static void main(String[] args) throws Throwable {
        String chordJarFile = getChordJarFile();

        // resolve Chord's main dir

        mainDirName = (new File(chordJarFile)).getParent();
        if (mainDirName == null)
            Messages.fatal(CHORD_MAIN_DIR_UNDEFINED);
        System.setProperty("chord.main.dir", mainDirName);
        
        // resolve Chord's work dir

        String workDirName = System.getProperty("chord.work.dir");
        try {
            workDirName = (new File(workDirName)).getCanonicalPath();
        } catch (IOException ex) {
            Messages.fatal(ex);
        }
        if (!(new File(workDirName)).isDirectory()) {
            Messages.fatal(CHORD_WORK_DIR_NOT_FOUND, workDirName);
        }
        System.setProperty("chord.work.dir", workDirName);

        // load program-specific Chord properties, if any

        String propsFileName = System.getProperty("chord.props.file");
        if (propsFileName != null) {
            try {
                readProps(propsFileName);
            } catch (IOException ex) {
                Messages.fatal(ex);
            }
        } else {
            try {
                propsFileName = workDirName + File.separator + "chord.properties";
                readProps(propsFileName);
            } catch (IOException ex) {}
        }

        // load system-wide Chord properties, if any
        try {
            readProps(mainDirName + File.separator + "chord.properties");
        } catch (IOException ex) {}
        
        String stdJavaAnalysisPath = getOrSetProperty("chord.std.java.analysis.path", chordJarFile);
        String extJavaAnalysisPath = getOrSetProperty("chord.ext.java.analysis.path", "");
        String javaAnalysisPath = getOrSetProperty("chord.java.analysis.path", Utils.concat(stdJavaAnalysisPath, File.pathSeparator, extJavaAnalysisPath));
        String stdDlogAnalysisPath = getOrSetProperty("chord.std.dlog.analysis.path", chordJarFile);
        String extDlogAnalysisPath = getOrSetProperty("chord.ext.dlog.analysis.path", "");
        String dlogAnalysisPath = getOrSetProperty("chord.dlog.analysis.path", Utils.concat(stdDlogAnalysisPath, File.pathSeparator, extDlogAnalysisPath));
        String userClassPath = getOrSetProperty("chord.class.path", "");
        
        System.setProperty("user.dir", workDirName);
        
        List<String> cpList = new ArrayList<String>(10);
        cpList.add(chordJarFile);
        if (!javaAnalysisPath.equals("")) {
            String[] a = javaAnalysisPath.split(Utils.PATH_SEPARATOR);
            for (String s : a) {
                if (!cpList.contains(s))
                    cpList.add(s);
            }
        }
        if (!dlogAnalysisPath.equals("")) {
            String[] a = dlogAnalysisPath.split(Utils.PATH_SEPARATOR);
            for (String s : a) {
                if (!cpList.contains(s))
                    cpList.add(s);
            }
        }
        if (!userClassPath.equals("")) {
            userClassPath= userClassPath.replace(';', File.pathSeparatorChar);//normalize
            System.setProperty("chord.class.path", userClassPath);//write back new value

            String[] a = userClassPath.split(Utils.PATH_SEPARATOR);
            for (String s : a) {
                if (!cpList.contains(s))
                    cpList.add(s);
            }
        }
        String cp = cpList.get(0);
        for (int i = 1; i < cpList.size(); i++)
            cp += File.pathSeparator + cpList.get(i);
        System.setProperty("java.class.path", cp);

        // build command line arguments of fresh JVM process to run Chord

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("java");
        for (Map.Entry e : System.getProperties().entrySet()) {
            String k = (String) e.getKey();
            String v = (String) e.getValue();
            // no need to pass standard params
            if (k.startsWith("sun") || k.startsWith("jikes"))
                continue; 
            cmdList.add("-D" + k + "=" + v);
        }
        cmdList.add("chord.project.Main");
        String[] cmdAry = new String[cmdList.size()];
        cmdList.toArray(cmdAry);
        
        int result = ProcessExecutor.execute(cmdAry, null, new File(workDirName), -1);
        System.exit(result);
    }
    
    private static String getChordJarFile() {
        String cname = Boot.class.getName().replace('.', '/') + ".class";
        URL url = Boot.class.getClassLoader().getResource(cname);
        if (!url.getProtocol().equals("jar"))
            Messages.fatal(CHORD_JAR_NOT_FOUND, url.toString());
        String file = url.getFile();
        return file.substring(file.indexOf(':') + 1, file.indexOf('!'));
    }

    private static String getOrSetProperty(String key, String defVal) {
        String val = System.getProperty(key);
        if (val != null)
            return val;
        System.setProperty(key, defVal);
        return defVal;
    }

    private static void readProps(String fileName) throws IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(fileName);
        props.load(in);
        in.close();
        
        Properties sysprops = System.getProperties();
        for (Map.Entry e : props.entrySet()) {
            String key = (String) e.getKey();
            String val = (String) e.getValue();
            String oldVal = (String)sysprops.get(key);
            if (oldVal == null)
                sysprops.setProperty(key, val);
            else if (!oldVal.equals(val))
                Messages.log(WARN_DUPLICATE_SYSPROP, key, oldVal, val);
        }
    }
}
