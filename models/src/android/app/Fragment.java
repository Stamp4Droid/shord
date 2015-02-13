class Fragment
{
	public  Fragment()
	{
		this.onHiddenChanged(false);
		this.onActivityResult(0, 0, null);
		this.onInflate(null, null);
		this.onInflate(null, null, null);
		this.onAttach(null);
		this.onCreate(null);
		this.onViewCreated(null, null);
		this.onCreateView(null, null, null);
		this.onActivityCreated(null);
		this.onStart();
		this.onResume();
		this.onSaveInstanceState(null);
		this.onConfigurationChanged(null);
		this.onPause();
		this.onStop();
		this.onLowMemory();
		this.onTrimMemory(0);
		this.onDestroyView();
		this.onDestroy();
		this.onDetach();
		this.onCreateOptionsMenu(null, null);
		this.onPrepareOptionsMenu(null);
		this.onDestroyOptionsMenu();
		this.onOptionsItemSelected(null);
		this.onOptionsMenuClosed(null);
		this.onCreateContextMenu(null, null, null);
		this.onContextItemSelected(null);
	}
}