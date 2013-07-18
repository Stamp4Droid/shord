package stamp.missingmodels.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

/*
 * Manages scratch and output directories.
 * Currently primarily for JCFLSolverAnalysis.
 * 
 * @author Osbert Bastani
 */
public class FileManager implements Serializable {
	private static final long serialVersionUID = -3693317914605735230L;
	
	/*
	 * An enum of possible file types, currently
	 * either scratch files or output files.
	 */
	public static enum FileType {
		SCRATCH, OUTPUT;		
	}
	
	/*
	 * A basic data structure for generating file content,
	 * to be used as input to the file manager.
	 */
	public static interface StampFile {
		/*
		 * Returns the file name (not including the scratch
		 * or output directory portion of the path).
		 * 
		 * @return: The file name.
		 */
		public abstract String getName();
		
		/*
		 * Returns the file type.
		 * 
		 * @return: The file type.
		 */
		public abstract FileType getType();
		
		/*
		 * Returns the content to be written to the file.
		 * 
		 * @return: The content to be written to the file.
		 */
		public abstract String getContent();
	}

	private final File scratchDirectory;
	private final File outputDirectory;
	
	/*
	 * The generic constructor. If useScratch is false, then it
	 * clears the scratch directory.
	 * 
	 * @param outputDirectory: The directory to put output files.
	 * @param scratchDirectory: The directory to put input files.
	 * @param useScratch: Whether or not to use the existing
	 * scratch directory.
	 */
	public FileManager(File outputDirectory, File scratchDirectory, boolean useScratch) throws IOException {
		// STEP 1: delete the scratch directory if needed, and
		// ensure that the directories exist.
		if(!useScratch) {
			scratchDirectory.delete();
		}
		
		scratchDirectory.mkdirs();
		outputDirectory.mkdirs();
		
		// STEP 2: set the fields.
		this.outputDirectory = outputDirectory;
		this.scratchDirectory = scratchDirectory;
	}
	
	/*
	 * Returns the directory associated with the file type.
	 * 
	 * @param type: The file type which for we want to get the
	 * directory
	 * @return: The file representing the directory corresponding
	 * to the given file type. 
	 */
	public File getDirectory(FileType type) {
		switch(type) {
		case SCRATCH:
			return this.scratchDirectory;
		case OUTPUT:
			return this.outputDirectory;
		default:
			return null;					
		}
	}
	
	/*
	 * Returns a buffered reader corresponding to the
	 * given file.
	 * 
	 * @param file: The file to be read.
	 * @param type: The file type.
	 */
	public BufferedReader read(StampFile stampFile) throws IOException {
		File file = new File(this.getDirectory(stampFile.getType()), stampFile.getName());
		return new BufferedReader(new FileReader(file));
	}
	
	/*
	 * Writes the contents of the stamp file to the
	 * appropriate directory.
	 * 
	 * @param file: The content to be written.
	 * @param type: The file type. 
	 */
	public void write(StampFile stampFile) throws IOException {
		File file = new File(this.getDirectory(stampFile.getType()), stampFile.getName());
		PrintWriter printWriter = new PrintWriter(file);
		printWriter.println(stampFile.getContent());
		printWriter.close();
	}
}
