class AdapterView
{
	public  void setOnItemClickListener(final android.widget.AdapterView.OnItemClickListener listener) 
	{ 
		listener.onItemClick(AdapterView.this, null, 0, 0L);
	}
	
	public  void setOnItemLongClickListener(final android.widget.AdapterView.OnItemLongClickListener listener)
	{ 
		listener.onItemLongClick(AdapterView.this, null, 0, 0L);
	}

	public  void setOnItemSelectedListener(final android.widget.AdapterView.OnItemSelectedListener listener) 
	{ 
		listener.onItemSelected(AdapterView.this, null, 0, 0L);
		listener.onNothingSelected(AdapterView.this);
	}
}