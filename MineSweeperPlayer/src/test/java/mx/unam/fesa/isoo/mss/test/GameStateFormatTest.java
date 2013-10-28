package mx.unam.fesa.isoo.mss.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class GameStateFormatTest {
	
	/* */
	private final Pattern pattern = Pattern.compile("\\((GS) (\\d|[1-9]\\d*) (ON|TIE|WP1|WP2)\\)");
	/* */
	private final Matcher matcher = pattern.matcher("");
	
	@Test(description="Correct messages")
	public void test1() {
		matcher.reset("(GS 0 ON)");
		assert matcher.lookingAt();
		
		matcher.reset("(GS 1231 TIE)");
		assert matcher.lookingAt();
		
		matcher.reset("(GS 0 WP1)");
		assert matcher.lookingAt();
		
		matcher.reset("(GS 4353 WP2)");
		assert matcher.lookingAt();
	}
	
	@Test(description="Incorrect messages")
	public void test2() {
		matcher.reset("(GS 123 P)");
		assert !matcher.lookingAt();
		
		matcher.reset("(GS 123 OK ");
		assert !matcher.lookingAt();
		
		matcher.reset("(REG 12 WP1 )");
		assert !matcher.lookingAt();
		
		matcher.reset("(GS 0 ON)dasdas");
		assert matcher.lookingAt();
	}
	
	@Test(description="Extracting groups")
	public void test3() {
		matcher.reset("(GS 0 ON)");
		assert matcher.lookingAt();
		
		System.out.println("Group 1: " + matcher.group(1));
		System.out.println("Group 2: " + matcher.group(2));
		System.out.println("Group 2: " + matcher.group(3));
	}
}