package compiler;

import static org.junit.Assert.*;

import org.junit.Test;

public class JavaLanguageTest {
	@Test
	public void main() {
		String code = "public class Main {\n" +
			"\tpublic static void main(String[] args) {\n" +
			"\t}\n" +
			"}";
		
		assertEquals("Main", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void mainWithSpace() {
		String code = "public class  Main2 {\n" +
			"\tpublic  static  void  main ( String[] args ) {\n" +
			"\t}\n" +
			"}";
		
		assertEquals("Main2", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void methodContainingMain() {
		String code = "public class Main2 {\n" +
			"\tpublic static void mainExtended(String[] args) {\n" +
			"\t}\n" +
			"}";
		
		assertEquals("Main", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void modifiedName() {
		String code = "public class Main2 {\n" +
			"\tpublic static void main(String[] args) {\n" +
			"\t}\n" +
			"}";
		
		assertEquals("Main2", new JavaLanguage().getFileName(code));
	}
	
	@Test
	public void multipleClasses() {
		String code = "class Main2 {\n" +
			"}\n" +
			"public class Main3 {\n" +
			"\tpublic static void main(String[] args) {\n" +
			"\t}\n" +
			"}";
		
		assertEquals("Main3", new JavaLanguage().getFileName(code));
	}
}
