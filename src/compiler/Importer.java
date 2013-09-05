package compiler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Importer {
	private List<String> jars;

	public Importer(List<String> jars) {
		this.jars = jars;
	}

	public List<String> findImports(String className) {
		List<String> imports = new ArrayList<String>();

		for(String jar:jars) {
			for(String name:readClassesInJar(jar)) {
				if(name.contains("$")) continue;
				
				String localName = name.replaceAll(".*\\.", "");
				
				if(localName.toLowerCase().startsWith(className.toLowerCase())) {
					imports.add(name);
				}
			}
		}
		
		Collections.sort(imports, new Comparator<String>() {
			public int compare(String s1, String s2) {
				s1 = s1.replaceAll(".*\\.", "");
				s2 = s2.replaceAll(".*\\.", "");
				return s1.compareTo(s2);
			}
		});
		
		return imports;
	}

	private List<String> readClassesInJar(String jar) {
		List<String> classes = new ArrayList<String>();
		if(jar == null) return classes;
		
		try(ZipInputStream inputStream = new ZipInputStream(new FileInputStream(jar))) {
			ZipEntry entry;
			while((entry = inputStream.getNextEntry()) != null) {
				String name = entry.getName();
				if(name.endsWith(".class")) {
					name = name.replaceAll("\\.class$", "");
					name = name.replaceAll("[\\/]", ".");
					classes.add(name);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes;
	}
}
