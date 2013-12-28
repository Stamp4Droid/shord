package shord.analyses;

import stamp.app.Component;
import stamp.app.IntentFilter;

import java.util.*;

public class SystemComponents
{
	//add android's components
	static void add(List<Component> comps)
	{
		Component gInstallAPK = new Component("INSTALL_APK");
		IntentFilter filter = new IntentFilter();
		filter.addDataType("application/vnd.android.package-archive");
		gInstallAPK.addIntentFilter(filter);
		comps.add(gInstallAPK);
	}

}