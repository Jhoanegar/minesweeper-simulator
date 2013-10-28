package mx.unam.fesa.isoo.mss.test;

import java.nio.charset.Charset;

import org.testng.annotations.Test;

public class CharsetTest {
  @Test
  public void bytePerChar() {
	  Charset charset = Charset.forName("UTF-8");
	  System.out.println("AvBytesPerChar: " + charset.newEncoder().averageBytesPerChar());
	  System.out.println("MaxBytesPerChar: " + charset.newEncoder().maxBytesPerChar());
  }
}