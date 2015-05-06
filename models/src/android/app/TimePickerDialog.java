import edu.stanford.stamp.annotation.Inline;

class TimePickerDialog
{
        @Inline
	public  TimePickerDialog(android.content.Context context, android.app.TimePickerDialog.OnTimeSetListener setListener, int hourOfDay, int minute, boolean is24HourView) 
	{ 
		super((android.content.Context)null,false,(android.content.DialogInterface.OnCancelListener)null); 
		setListener.onTimeSet(null, hourOfDay, minute);
		this.onClick(this, 0);
		this.onTimeChanged(null, 0, 0);
	}

        @Inline
	public  TimePickerDialog(android.content.Context context, int theme, android.app.TimePickerDialog.OnTimeSetListener setListener, int hourOfDay, int minute, boolean is24HourView) 
	{ 
		super((android.content.Context)null,false,(android.content.DialogInterface.OnCancelListener)null); 
		setListener.onTimeSet(null, hourOfDay, minute);
		this.onClick(this, 0);
		this.onTimeChanged(null, 0, 0);
	}

}