package tango.rEditor;

import java.io.File;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public interface EditorWorker {

	public void readFile(JTextComponent tComp, File file);
	public void writeFile(JTextComponent tComp, File file);
	
	public void readFile(Document doc, File file);
	public void writeFile(Document doc, File file);

	public void readFile(DefaultStyledDocument doc, File file);
	public void writeFile(DefaultStyledDocument doc, File file);
}
