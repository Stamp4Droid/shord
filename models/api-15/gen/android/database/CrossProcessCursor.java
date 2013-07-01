package android.database;
public interface CrossProcessCursor
  extends android.database.Cursor
{
public abstract  android.database.CursorWindow getWindow();
public abstract  void fillWindow(int position, android.database.CursorWindow window);
public abstract  boolean onMove(int oldPosition, int newPosition);
}
