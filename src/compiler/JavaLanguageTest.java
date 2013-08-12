package compiler;

import static org.junit.Assert.*;

import org.junit.Test;

public class JavaLanguageTest {
	@Test
	public void main() {
		String code = "public class Main {\r" +
			"\tpublic static void main(String[] args) {\r" +
			"\t}\r" +
			"}";
		
		assertEquals("Main", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void mainWithSpace() {
		String code = "public class  Main2 {\r" +
			"\tpublic  static  void  main ( String[] args ) {\r" +
			"\t}\r" +
			"}";
		
		assertEquals("Main2", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void methodContainingMain() {
		String code = "public class Main2 {\r" +
			"\tpublic static void mainExtended(String[] args) {\r" +
			"\t}\r" +
			"}";
		
		assertEquals("Main", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void modifiedName() {
		String code = "public class Main2 {\r" +
			"\tpublic static void main(String[] args) {\r" +
			"\t}\r" +
			"}";
		
		assertEquals("Main2", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void multipleClasses() {
		String code = "class Main2 {\r" +
			"}\r" +
			"public class Main3 {\r" +
			"\tpublic static void main(String[] args) {\r" +
			"\t}\r" +
			"}";
		
		assertEquals("Main3", new JavaLanguage().getFileName(code));
	}
}
