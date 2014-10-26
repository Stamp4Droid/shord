package edu.stanford.stamp.test.sootdextest;

import android.app.Activity;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends Activity
{

	public static byte[] decode(byte[] source) {

		byte[] a = new byte[1];

		byte b = (byte) (source[0] & 0x7f);
		a[0] = b;

		return a;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}

/* Jimple:

    public static byte[] decode(byte[])
    {
        byte[] $r0, $r1;
        byte $b0;

        $r0 := @parameter0: byte[];
        $r1 = newarray (byte)[1];
        $b0 = $r0[0];
        $b0 = $b0 & 127;
        $b0 = (byte) $b0;
        $r1[0] = $b0;
        return $r1;
    }
 
*/

/* DEX:

    #1              : (in Ledu/stanford/stamp/test/sootdextest/MainActivity;)
      name          : 'decode'
      type          : '([B)[B'
      access        : 0x0009 (PUBLIC STATIC)
      code          -
      registers     : 4
      ins           : 1
      outs          : 0
      insns size    : 12 16-bit code units
001824:                                        |[001824] edu.stanford.stamp.test.sootdextest.MainActivity.decode:([B)[B
001834: 1202                                   |0000: const/4 v2, #int 0 // #0
001836: 1210                                   |0001: const/4 v0, #int 1 // #1
001838: 2300 2b00                              |0002: new-array v0, v0, [B // type@002b
00183c: 4801 0302                              |0004: aget-byte v1, v3, v2
001840: dd01 017f                              |0006: and-int/lit8 v1, v1, #int 127 // #7f
001844: 0000                                   |0008: nop // spacer
001846: 4f01 0002                              |0009: aput-byte v1, v0, v2
00184a: 1100                                   |000b: return-object v0

*/
