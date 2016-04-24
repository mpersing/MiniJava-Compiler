package driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import parser.MiniJavaNodeJumper;
import parser.MiniJavaNodePrunerListener;
import parser.MiniJavaTypeCheckerListener;
import CodeGenerator.BasicCodeGenerator;
import antlr4.MiniJavaLexer;
import antlr4.MiniJavaParser;


public class GenericDriver {
	String in;
	public GenericDriver(String inFile){
		in = inFile;
	}
	
	public List<String> genBasicOutput(){
		File fileIn = new File(in);
		BufferedReader br;
		String inString = "";
		try {
			br = new BufferedReader(new FileReader(fileIn));
			while(true) {
				String nextLine = br.readLine();
				if(nextLine == null) {
					break;
				}
				inString += nextLine + "\n";
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		MiniJavaLexer lexer = new MiniJavaLexer(new ANTLRInputStream(inString));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniJavaParser parser = new MiniJavaParser(tokens);
		ParseTree tree = parser.program();
		
		tree.getChildCount();		
		ParseTreeWalker walker = new ParseTreeWalker();
		MiniJavaNodePrunerListener listener = new MiniJavaNodePrunerListener();
		walker.walk(listener, tree);
		
		MiniJavaNodeJumper listener2 = new MiniJavaNodeJumper();
		walker.walk(listener2, tree);
		JFrame frame = new JFrame("Antlr AST");
		JPanel panel = new JPanel();
		JScrollPane scrPane = new JScrollPane(panel);
		TreeViewer viewr = new TreeViewer(Arrays.asList(parser.getRuleNames()),tree);
		viewr.setScale(1.5);
		panel.add(viewr);
		frame.add(scrPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(200, 200);
		frame.setVisible(true);
		
		MiniJavaTypeCheckerListener listener3 = new MiniJavaTypeCheckerListener();
		walker.walk(listener3, tree);
		
		BasicCodeGenerator gen = new BasicCodeGenerator(listener3.getMap());
		
		gen.generate(); 
		return gen.getProgram();
	}
}