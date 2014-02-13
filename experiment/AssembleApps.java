import java.io.*;

public class AssembleApps
{
	public static void main(String[] args) throws IOException
	{
		String listFile = args[0];
		String outDir = args[1];

		String[] dirs = args[3].split(File.pathSeparator);

		BufferedReader reader = new BufferedReader(new FileReader(listFile));
		String line;
		while((line = reader.readLine()) != null){
			int index = line.lastIndexOf(' ');
			String apkFileName = line.substring(0, index);
			for(String d : dirs){
				File f = new File(d, apkFileName);
				if(!f.exists())
					continue;

				copy(f, new File(outDir, apkFileName));
				break;
			}
		}
		reader.close();
	}

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	public static void copy(File srcFile, File dstFile) throws IOException
	{
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new BufferedInputStream(new FileInputStream(srcFile), DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(new FileOutputStream(dstFile), DEFAULT_BUFFER_SIZE);
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			for (int length = 0; ((length = input.read(buffer)) > 0);) {
                    output.write(buffer, 0, length);
			}
		} finally {
			if (output != null) try { output.close(); } catch (IOException e) { throw e; }
			if (input != null) try { input.close(); } catch (IOException e) { throw e; }
		}
	}
}