class Cipher
{
	public final  int doFinal(byte[] output, int outputOffset) throws javax.crypto.IllegalBlockSizeException, javax.crypto.ShortBufferException, javax.crypto.BadPaddingException  {
	return 0; 
    }

	public final  byte[] doFinal(byte[] input) throws javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException { 
	return new byte[1] ;
    }

    public final byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException {
	return new byte[1] ;
    }

	public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output) throws javax.crypto.ShortBufferException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException { 
	return 0;  
    }

	public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws javax.crypto.ShortBufferException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException { 
	return 0;  
    }

	public final int doFinal(java.nio.ByteBuffer input, java.nio.ByteBuffer output) throws javax.crypto.ShortBufferException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException { 
	return 0;  
    }


}
