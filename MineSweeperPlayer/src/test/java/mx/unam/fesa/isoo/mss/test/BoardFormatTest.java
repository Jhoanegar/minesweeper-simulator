package mx.unam.fesa.isoo.mss.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class BoardFormatTest {

	/* */
	private final Pattern pattern = Pattern.compile("\\(B (\\d|[1-9]\\d*) ([1-9]\\d?) ([1-9]\\d?) ((?:C|(?:P1|P2)[FEM[1-8]])(?: (?:C|(?:P1|P2)[FEM[1-8]]))*)\\)");
	/* */
	private final Matcher matcher = pattern.matcher("");

	@Test(description="Correct messages")
	public void test1() {
		matcher.reset("(B 0 23 10 C P1F P2E P1M P25 P1F)");
		assert matcher.lookingAt();
		
		matcher.reset("(B 3453 23 34 C C P2F)");
		assert matcher.lookingAt();
	}
	
	@Test(description="Failed messages")
	public void test2() {
		// missing player
		//
		matcher.reset("(B 234 2 10 C F P1E P2M P18 P2F)");
		assert !matcher.lookingAt();
		
		// wrong player
		//
		matcher.reset("(B 2 50 3 C P2F P1E P2M P38 P2F)");
		assert !matcher.lookingAt();
		
		// wrong mine count
		//
		matcher.reset("(B 23 2 10 C P2F P1E P2M P110 P2F)");
		assert !matcher.lookingAt();
		
		// incomplete command
		//
		matcher.reset("(B 2 10 C P2F");
		assert !matcher.lookingAt();
		
		// correct prefix
		//
		matcher.reset("(B 0 2 10 C P2F)laskdjalksdj");
		assert matcher.lookingAt();
		
		// zero in dimensions
		//
		matcher.reset("(B 21312 0 10 C P2F)");
		assert !matcher.lookingAt();
	}
	
	@Test(description="Extracting groups")
	public void test3() {
		matcher.reset("(B 0 23 2 C P1F P2E P1M P22 P1F)");
		assert matcher.lookingAt();
		
		System.out.println("Group 1 = '" + matcher.group(1) + "'");
		System.out.println("Group 2 = '" + matcher.group(2) + "'");
		System.out.println("Group 3 = '" + matcher.group(3) + "'");
		System.out.println("Group 3 = '" + matcher.group(4) + "'");
	}
}