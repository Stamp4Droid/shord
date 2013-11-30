package stamp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Scanner;

public class Launcher
{
	static String stampScript;
	static ConcurrentLinkedQueue<String> apkNames = new ConcurrentLinkedQueue();


	public static void main(String[] args) throws Exception
	{
		if(args.length <= 0){
			System.out.println("specify:\n"+ 
							   "(1) path to stamp script\n"+
							   "(2) max. number of stamp's to launch\n"+
							   "(3) path to the directory containing apk's\n");
			System.exit(1);
		}

		stampScript = args[0];
		int maxStamp = Integer.parseInt(args[1]);
		String apkDir = args[2];

		String[] apks = new File(apkDir).list(new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.endsWith("apk");
				}
			});
		
		for(String apk : apks){
			apkNames.add(apkDir + '/' +apk);
		}	
		
		for(int i = 0; i < maxStamp; i++){
			new Worker().start();
		}
	}

	static class Worker extends Thread 
	{
		public void run()
		{
			String apkName = null;
			while((apkName = apkNames.poll()) != null){
				String[] cmdArray = new String[]{stampScript, "analyze", apkName};
				try{
					Process proc = Runtime.getRuntime().exec(cmdArray, null, null); 
                    Scanner sc = new Scanner(proc.getInputStream());           
                    while (sc.hasNext()) System.out.println(sc.nextLine());
                    proc.waitFor();

				}catch(IOException e){
					System.out.println(e.getMessage());
					e.printStackTrace();
				}catch(InterruptedException e){
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}

    }
}
