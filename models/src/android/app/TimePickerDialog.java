class TimePickerDialog
{
	public  TimePickerDialog(android.content.Context context, android.app.TimePickerDialog.OnTimeSetListener setListener, int hourOfDay, int minute, boolean is24HourView) 
	{ 
		super((android.content.Context)null,false,(android.content.DialogInterface.OnCancelListener)null); 
		setListener.onTimeSet(null, hourOfDay, minute);
		
		this.onClick(this, 0);
		this.onRestoreInstanceState(null);
		this.onSaveInstanceState();
		this.onTimeChanged(null, 0, 0);
		this.onStop();
	}

	public  TimePickerDialog(android.content.Context context, int theme, android.app.TimePickerDialog.OnTimeSetListener setListener, int hourOfDay, int minute, boolean is24HourView) 
	{ 
		super((android.content.Context)null,false,(android.content.DialogInterface.OnCancelListener)null); 
		setListener.onTimeSet(null, hourOfDay, minute);

		this.onClick(this, 0);
		this.onRestoreInstanceState(null);
		this.onSaveInstanceState();
		this.onTimeChanged(null, 0, 0);
		this.onStop();
	}
	


}