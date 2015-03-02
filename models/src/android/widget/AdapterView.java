class AdapterView
{
    public void setOnClickListener(android.view.View.OnClickListener l) {
		l.onClick(this);
    }

	public  void setOnItemClickListener(final android.widget.AdapterView.OnItemClickListener listener) 
	{ 
		listener.onItemClick(this, null, 0, 0L);
	}
	
	public  void setOnItemLongClickListener(final android.widget.AdapterView.OnItemLongClickListener listener)
	{ 
		listener.onItemLongClick(this, null, 0, 0L);
	}

	public  void setOnItemSelectedListener(final android.widget.AdapterView.OnItemSelectedListener listener) 
	{ 
		listener.onItemSelected(this, null, 0, 0L);
		listener.onNothingSelected(this);
	}
}