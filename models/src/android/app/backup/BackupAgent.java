import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;
import java.io.IOException;

class BackupAgent
{
	public  BackupAgent() 
	{
		super((android.content.Context)null); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						BackupAgent.this.onCreate();
					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						BackupAgent.this.onDestroy();
					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						try{
							BackupAgent.this.onBackup(null, null, null);
						}catch(IOException e){
						}
					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						try{
							BackupAgent.this.onRestore(null, 0, null);
						}catch(IOException e){
						}
					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						try{
							BackupAgent.this.onFullBackup(null);
						}catch(IOException e){
						}

					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						try{
							BackupAgent.this.onRestoreFile(null, 0L, null, 0, 0L, 0L);
						}catch(IOException e){
						}
					}
				}); 
	}
}