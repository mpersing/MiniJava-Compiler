package lex;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import driver.GenericDriver;
import junit.framework.Assert;

@RunWith(Parameterized.class)
public class FullCodeGeneratorTest {
	
	@Parameters
	public static Collection<Object[]> getFiles() {
		Collection<Object[]> params = new HashSet<Object[]>();
		for(File f : new File("./input_output/FullCodeGeneratorFullTests").listFiles()) {
			if(f.getName().contains(".java")) {
				Object[] arr = new Object[] { f.getName().substring(0, f.getName().indexOf('.')) };
				params.add(arr);
			}
		}
		return params;
	}
	
	private String filePrefix;
	
	public FullCodeGeneratorTest(String filePrefix) {
		this.filePrefix = filePrefix;
	}
	
	private void runSimulatorForOutput(String inASM, String outRunIO) {
		Process p;
		
		try {
			System.out.println("Launching");
			//ProcessBuilder pb = new ProcessBuilder("java -jar mars.jar nc " + inASM);
			//pb.redirectOutput(Redirect.INHERIT);
			//p = pb.start();
			p = Runtime.getRuntime().exec("java -jar mars.jar nc " + inASM);
			InputStream in = p.getInputStream();
			int counter = 0;
			while(p.isAlive()) {
				System.out.println("waiting for termination..");
				if(++counter > 100) {
					Assert.fail(this.filePrefix);
				}
				Thread.sleep(100);
			}
			File outFile = new File(outRunIO);
			Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private List<String> readFile(String filePath) {
		File fileIn = new File(filePath);
		BufferedReader br;
		List<String> expected = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(fileIn));
			while(true) {
				String nextLine = br.readLine();
				if(nextLine == null || nextLine.equals("")) {
					break;
				}
				expected.add(nextLine);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return expected;
	}

	@Test
	public void testAgainstExpectedOutput() {
		GenericDriver driver = new GenericDriver("./input_output/FullCodeGeneratorFullTests/" + this.filePrefix + ".java");
		System.out.println(this.filePrefix);
		List<String> generatedASM = driver.genBasicOutput();
//		generatedASM = new ArrayList<String>();
//		generatedASM.add("li $v0, 1");
//		generatedASM.add("li $a0, 2");
//		generatedASM.add("syscall");
		String asmFileName = "./input_output/temp/testAsmOut" + this.filePrefix + ".asm";
		String asmOutputName = "./input_output/temp/asmFileOutput" + this.filePrefix + ".out_gen";
		File fileOut = new File(asmFileName);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));
			for(String s : generatedASM) {
				bw.write(s);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runSimulatorForOutput(asmFileName, asmOutputName);
		List<String> result = readFile(asmOutputName);
		List<String> expected = readFile("./input_output/FullCodeGeneratorFullTests/" + this.filePrefix + ".out");
		System.out.println(expected.size());
		assertTrue(this.filePrefix, result.size() == expected.size());
		for(int i = 0 ; i < result.size(); ++i) {
			assertTrue(this.filePrefix, result.get(i).equals(expected.get(i)));
		}
	}

}
