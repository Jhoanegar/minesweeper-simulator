package mx.unam.fesa.mss.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;


public class RequestFormatTest {
	
	private final String COMMAND_PATTERN = "\\((?:(RF|SF|UN) (0|[1-9]\\d?) (0|[1-9]\\d?)|(REG) (\\w{1,8}))\\)";
	private final Matcher matcher = Pattern.compile(COMMAND_PATTERN).matcher("");
	
	@Test(description="RF - correct messages")
	public void correctRF() {
		matcher.reset("(RF 0 0)");
		assert matcher.lookingAt();
		
		matcher.reset("(RF 90 99)");
		assert matcher.lookingAt();
		
		matcher.reset("(RF 45 80)");
		assert matcher.lookingAt();
	}
	@Test(description="RF - incorrect messages")
	public void incorrectRF() {
		// leading zero in first number
		//
		matcher.reset("(RF 09 0)");
		assert !matcher.lookingAt();
		
		// leading zero in second number
		//
		matcher.reset("(RF 3 00)");
		assert !matcher.lookingAt();
		
		// using } instead of )
		//
		matcher.reset("(RF 45 80}");
		assert !matcher.lookingAt();
		
		// correct prefix
		//
		matcher.reset("(RF 45 80) sdlkfjsdlkfj");
		assert matcher.lookingAt();
		
		// incomplete command
		//
		matcher.reset("(RF 45");
		assert !matcher.lookingAt();
		
		// long first number
		//
		matcher.reset("(RF 4543 80");
		assert !matcher.lookingAt();
		
		// long second number
		//
		matcher.reset("(RF 45 80009");
		assert !matcher.lookingAt();
		
		// no number in first position
		//
		matcher.reset("(RF 4e 80");
		assert !matcher.lookingAt();
		
		// no number in second position
		//
		matcher.reset("(RF 45 6g");
		assert !matcher.lookingAt();
	}
	
	@Test(description="SF - correct messages")
	public void correctSF() {
		matcher.reset("(SF 0 0)");
		assert matcher.lookingAt();
		
		matcher.reset("(SF 30 67)");
		assert matcher.lookingAt();
		
		matcher.reset("(SF 99 90)");
		assert matcher.lookingAt();
	}
	@Test(description="SF - incorrect messages")
	public void incorrectSF() {
		matcher.reset("(SF 09 0)");
		assert !matcher.lookingAt();
		
		matcher.reset("(SF 3 00)");
		assert !matcher.lookingAt();
		
		matcher.reset("(SF 45 80}");
		assert !matcher.lookingAt();
		
		matcher.reset("(SF 45 80) sdlkfjsdlkfj");
		assert matcher.lookingAt();
		
		matcher.reset("(SF 45");
		assert !matcher.lookingAt();
		
		matcher.reset("(SF 4543 80");
		assert !matcher.lookingAt();
				
		matcher.reset("(SF 45 80009");
		assert !matcher.lookingAt();
				
		matcher.reset("(SF 4e 80");
		assert !matcher.lookingAt();
				
		matcher.reset("(SF 45 6g");
		assert !matcher.lookingAt();
	}
	
	@Test(description="UN - correct messages")
	public void correctUN() {
		matcher.reset("(UN 0 0)");
		assert matcher.lookingAt();
		
		matcher.reset("(UN 20 12)");
		assert matcher.lookingAt();
		
		matcher.reset("(UN 99 90)");
		assert matcher.lookingAt();
	}
	@Test(description="UN - incorrect messages")
	public void incorrectUN() {
		matcher.reset("(UN 09 0)");
		assert !matcher.lookingAt();
		
		matcher.reset("(UN 3 00)");
		assert !matcher.lookingAt();
		
		matcher.reset("(UN 45 80}");
		assert !matcher.lookingAt();
		
		matcher.reset("(UN 45 80) sdlkfjsdlkfj");
		assert matcher.lookingAt();
		
		matcher.reset("(UN 45");
		assert !matcher.lookingAt();
		
		matcher.reset("(UN 4543 80");
		assert !matcher.lookingAt();
				
		matcher.reset("(UN 45 80009");
		assert !matcher.lookingAt();
				
		matcher.reset("(UN 4e 80");
		assert !matcher.lookingAt();
				
		matcher.reset("(UN 45 6g");
		assert !matcher.lookingAt();
	}
	
	@Test(description="REG - correct messages")
	public void correctREG() {
		matcher.reset("(REG 123_gsD8)");
		assert matcher.lookingAt();
		
		matcher.reset("(REG a_5F)");
		assert matcher.lookingAt();
		
		matcher.reset("(REG d)");
		assert matcher.lookingAt();
	}
	@Test(description="REG - incorrect messages")
	public void incorrectREG() {
		// using } instead of )
		//
		matcher.reset("(REG 123_gsD8}");
		assert !matcher.lookingAt();
		
		// long command
		//
		matcher.reset("(REG a_5Fg6Gjus9)");
		assert !matcher.lookingAt();
		
		// non word
		//
		matcher.reset("(REG d:Ñ**)");
		assert !matcher.lookingAt();
		
		// incomplete command
		//
		matcher.reset("(REG");
		assert !matcher.lookingAt();
		
		// correct prefix
		//
		matcher.reset("(REG sdf_D)aslkdjaslkdj");
		assert matcher.lookingAt();
	}
	
	@Test(description="RF - group verification", dependsOnMethods={"correctRF", "incorrectRF"})
	public void groupsRF() {
		matcher.reset("(RF 90 0)");
		assert matcher.lookingAt();
		assert matcher.group(1).equals("RF");
		assert matcher.group(2).equals("90");
		assert matcher.group(3).equals("0");
	}
	
	@Test(description="SF - group verification", dependsOnMethods={"correctSF", "incorrectSF"})
	public void groupsSF() {
		matcher.reset("(SF 0 45)");
		assert matcher.lookingAt();
		assert matcher.group(1).equals("SF");
		assert matcher.group(2).equals("0");
		assert matcher.group(3).equals("45");
	}
	
	@Test(description="UN - group verification", dependsOnMethods={"correctUN", "incorrectUN"})
	public void groupsUN() {
		matcher.reset("(UN 89 32)");
		assert matcher.lookingAt();
		assert matcher.group(1).equals("UN");
		assert matcher.group(2).equals("89");
		assert matcher.group(3).equals("32");
	}
	
	@Test(description="REG - group verification", dependsOnMethods={"correctREG", "incorrectREG"})
	public void groupsREG() {
		matcher.reset("(REG sGb_D9)");
		assert matcher.lookingAt();
		assert matcher.group(4).equals("REG");
		assert matcher.group(5).equals("sGb_D9");
	}
	
	public void lookingAt_4() {
		Pattern pattern = Pattern.compile(COMMAND_PATTERN);
		
		Matcher matcherTest1 = pattern.matcher("(RF 2342 234) asdasdasd");
		assert matcherTest1.lookingAt();
		System.out.println("matcherTest1 ------------------------");
		System.out.println("requiredEnd = " + matcherTest1.requireEnd());
		System.out.println("start = " + matcherTest1.start());
		System.out.println("end = " + matcherTest1.end());
		System.out.println("start 1 = " + matcherTest1.start(1));
		System.out.println("end 1 = " + matcherTest1.end(1));
		System.out.println("start 2 = " + matcherTest1.start(2));
		System.out.println("end 2 = " + matcherTest1.end(2));
		System.out.println("start 3 = " + matcherTest1.start(3));
		System.out.println("end 3 = " + matcherTest1.end(3));
		
		StringBuffer buffer = new StringBuffer("(RF 2342");
		Matcher matcherTest2 = pattern.matcher(buffer);
		System.out.println("matcherTest2 ------------------------");
		
		assert !matcherTest2.lookingAt();
		System.out.println("first ------");
		System.out.println("requiredEnd = " + matcherTest2.requireEnd());
		System.out.println("regionStart = " + matcherTest2.regionStart());
		System.out.println("regionEnd = " + matcherTest2.regionEnd());
//		System.out.println("start = " + matcherTest2.start());
//		System.out.println("end = " + matcherTest2.end());
//		System.out.println("start 1 = " + matcherTest2.start(1));
//		System.out.println("end 1 = " + matcherTest2.end(1));
//		System.out.println("start 2 = " + matcherTest2.start(2));
//		System.out.println("end 2 = " + matcherTest2.end(2));
//		System.out.println("start 3 = " + matcherTest2.start(3));
//		System.out.println("end 3 = " + matcherTest2.end(3));
		
		buffer.append(" 23234)");
		matcherTest2.region(0, buffer.length());
		assert matcherTest2.lookingAt();
		System.out.println("second ------");
		System.out.println("requiredEnd = " + matcherTest2.requireEnd());
		System.out.println("regionStart = " + matcherTest2.regionStart());
		System.out.println("regionEnd = " + matcherTest2.regionEnd());
		
//		System.out.println("start = " + matcherTest2.start());
//		System.out.println("end = " + matcherTest2.end());
//		System.out.println("start 1 = " + matcherTest2.start(1));
//		System.out.println("end 1 = " + matcherTest2.end(1));
//		System.out.println("start 2 = " + matcherTest2.start(2));
//		System.out.println("end 2 = " + matcherTest2.end(2));
//		System.out.println("start 3 = " + matcherTest2.start(3));
//		System.out.println("end 3 = " + matcherTest2.end(3));
	}
}