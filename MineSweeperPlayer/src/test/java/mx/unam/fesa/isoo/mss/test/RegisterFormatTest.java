package mx.unam.fesa.isoo.mss.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class RegisterFormatTest {
	
	/* */
	private final Pattern pattern = Pattern.compile("\\(REG (?:(OK) (P1|P2)|(NO) \"((?:\\w| ){0,30})\")\\)");
	/* */
	private final Matcher matcher = pattern.matcher("");
	
	@Test(description="Correct messages")
	public void test1() {
		matcher.reset("(REG OK P1)");
		assert matcher.lookingAt();
		
		matcher.reset("(REG OK P2)");
		assert matcher.lookingAt();
		
		matcher.reset("(REG NO \"\")");
		assert matcher.lookingAt();
		
		matcher.reset("(REG NO \"jsldote sdfe jl8s 8sj jd72 sl8\")");
		assert matcher.lookingAt();
	}
	
	@Test(description="Incorrect messages")
	public void test2() {
		matcher.reset("(REG OK P3)");
		assert !matcher.lookingAt();
		
		matcher.reset("(REG OK ");
		assert !matcher.lookingAt();
		
		matcher.reset("(REG NO )");
		assert !matcher.lookingAt();
		
		matcher.reset("(REG NO \"jsldote sdfe jl8s 8sj jd72 sl8sdfsdfsdfsdfsfd\")");
		assert !matcher.lookingAt();
	}
	
	@Test(description="Extracting groups")
	public void test3() {
		matcher.reset("(REG OK P1)");
		assert matcher.lookingAt();
		
		System.out.println("Group 1: " + matcher.group(1));
		System.out.println("Group 2: " + matcher.group(2));
		
		matcher.reset("(REG NO \"panchito gomez\")");
		assert matcher.lookingAt();
		
		System.out.println("Group 3: " + matcher.group(3));
		System.out.println("Group 4: " + matcher.group(4));
	}
}