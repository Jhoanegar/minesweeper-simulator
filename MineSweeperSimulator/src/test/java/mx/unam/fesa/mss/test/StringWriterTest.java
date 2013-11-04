package mx.unam.fesa.mss.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.annotations.Test;

public class StringWriterTest {
	/* */
	private StringWriter stringWriter = new StringWriter(14);
	/* */
	private PrintWriter printWriter = new PrintWriter(stringWriter, true);
	
	@Test
	public void test1() {
		printWriter.println("pancho");
		System.out.println(stringWriter.toString());
		
		stringWriter.flush();
		printWriter.println("panchito");
		System.out.println(stringWriter.toString());
		
		printWriter.println("merolico");
		System.out.println(stringWriter.toString());
	}
}
