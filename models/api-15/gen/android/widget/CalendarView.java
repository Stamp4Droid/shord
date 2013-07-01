package android.widget;
public class CalendarView
  extends android.widget.FrameLayout
{
public static interface OnDateChangeListener
{
public abstract  void onSelectedDayChange(android.widget.CalendarView view, int year, int month, int dayOfMonth);
}
public  CalendarView(android.content.Context context) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  CalendarView(android.content.Context context, android.util.AttributeSet attrs) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  CalendarView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  void setEnabled(boolean enabled) { throw new RuntimeException("Stub!"); }
public  boolean isEnabled() { throw new RuntimeException("Stub!"); }
protected  void onConfigurationChanged(android.content.res.Configuration newConfig) { throw new RuntimeException("Stub!"); }
public  long getMinDate() { throw new RuntimeException("Stub!"); }
public  void setMinDate(long minDate) { throw new RuntimeException("Stub!"); }
public  long getMaxDate() { throw new RuntimeException("Stub!"); }
public  void setMaxDate(long maxDate) { throw new RuntimeException("Stub!"); }
public  void setShowWeekNumber(boolean showWeekNumber) { throw new RuntimeException("Stub!"); }
public  boolean getShowWeekNumber() { throw new RuntimeException("Stub!"); }
public  int getFirstDayOfWeek() { throw new RuntimeException("Stub!"); }
public  void setFirstDayOfWeek(int firstDayOfWeek) { throw new RuntimeException("Stub!"); }
public  void setOnDateChangeListener(android.widget.CalendarView.OnDateChangeListener listener) { throw new RuntimeException("Stub!"); }
public  long getDate() { throw new RuntimeException("Stub!"); }
public  void setDate(long date) { throw new RuntimeException("Stub!"); }
public  void setDate(long date, boolean animate, boolean center) { throw new RuntimeException("Stub!"); }
}
