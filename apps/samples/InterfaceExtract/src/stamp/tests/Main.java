package stamp.tests;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		final EditText textBox1 = (EditText) this.findViewById(R.id.textbox1);
		final EditText textBox2 = (EditText) this.findViewById(R.id.textbox2);

		Intent i = new Intent(this, Second.class);
		i.putExtra("key1", textBox1.getText().toString());
		i.putExtra("key2", textBox2.getText().toString());
		startActivity(i);
    }
}
