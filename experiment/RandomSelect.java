
import java.util.*;
import java.io.*;

public class RandomSelect
{
	private static final int NUM = 100;

	public static void main(String[] args) throws IOException
	{
		String listFile = args[0];
		String outFile = args[1];

		List<String> apps = new LinkedList();
		int appCount = 0;

		BufferedReader reader = new BufferedReader(new FileReader(listFile));
		String line;
		while((line = reader.readLine()) != null){
			apps.add(line);
			appCount++;
		}
		reader.close();

		PrintWriter writer = new PrintWriter(new FileWriter(outFile));		
		Random random = new Random();
		for(int i = 0; i < NUM; i++){
			int index = random.nextInt(appCount);
			writer.println(apps.get(index));
		}
		writer.close();
	}
}