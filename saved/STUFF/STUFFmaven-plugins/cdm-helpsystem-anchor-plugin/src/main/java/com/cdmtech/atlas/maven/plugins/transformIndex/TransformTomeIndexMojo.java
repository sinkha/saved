package com.cdmtech.atlas.maven.plugins.transformIndex;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/** 
 * Mojo for generating an index.hxk file from a Tome output HTML index.
 * 
 * The HTML index must be in a table, each column "<td>" element signifies a 
 * unique entry, and "&nbsp;" identifies the indentation/location of each 
 * element (parent-child relationship). Groups are separated based on a 
 * blank "<td>" element.
 * 
 * Example:
 * 
 * INPUT HTML							    INPUT STRUCTURE	  	 OUTPUT STRUCTURE
 *
 * <table>								   |   A				|   <A/>
 *	<tr><td>A</td></tr>					   |   About   			|	<About>
 *	<tr><td>About</td></tr>				   |  	 Page1	  		|	  <Page1>
 *	<tr><td>&nbsp;Page1</td></tr>		   |	   Page2		|		<Page2/>
 *	<tr><td>&nbsp;&nbsp;Page2</td></tr>    |   Contact      	|     </Page1>
 *	<tr><td>Contact</td></tr>			   |					|	</About>
 *	<tr><td></td></tr>					   |   B				|	<Contact/>
 *	<tr><td>B</td></tr>					   |   Boats			|	<B/>
 *	<tr><td>Boats</td></tr> 			   | 	   				|	<Boats/>
 * </table> 							   |      				|     
 *
 * @goal transform-index
 * @phase initialize
 */
public class TransformTomeIndexMojo extends AbstractMojo {
    /**
     * Location of input HTML file
     * 
     * @parameter expression="${project.build.directory}/directory/input.html"
     * @required
     */
    private String inputFile;
    
    /**
     * Location of output XML file
     * 
     * @parameter expression="${project.build.directory}/directory/output.xml"
     * @required
     */
    private String outputFile;    
	
	public void execute() throws MojoFailureException {
		TomeHTMLTreeGenerator htmlTree = null;
		
		try {
			htmlTree = new TomeHTMLTreeGenerator(getLog(), inputFile);
		} catch (IOException exception) {
			throw new MojoFailureException("Error creating HTML tree from " 
					+ inputFile, exception);
		}
		
		Document xmlTree = DocumentHelper.createDocument();
		xmlTree.addElement("HelpIndex")
			.addAttribute("Name", "K")
			.addAttribute("Visible", "Yes")
			.addAttribute("DTDVersion", "1.0")
			.addAttribute("FileVersion", "1.0")
			.addAttribute("LangId", "1033");
		
		buildXMLTree(xmlTree.getRootElement(), htmlTree.getRoot());
		
		try {
			saveFile(xmlTree);
		} catch (IOException exception) {
			throw new MojoFailureException("Error saving file " + outputFile, exception);
		}
		
		getLog().info("Output index created: " + outputFile);
	}
	
	private void buildXMLTree(Element xmlNode, DefaultMutableTreeNode htmlNode) {
		Element addedNode;
		
		if (!htmlNode.equals(htmlNode.getRoot())) {
			addedNode = xmlNode.addElement("Keyword")
				.addAttribute("Term", ((Keyword)htmlNode.getUserObject()).getTerm());
		} else {
			addedNode = xmlNode;
		}
		
		if (((Keyword)htmlNode.getUserObject()).getUrl() != null) {
			addedNode.addElement("Jump").addAttribute("Url", ((Keyword)htmlNode.getUserObject()).getUrl());
		}
		
		Enumeration<?> children = htmlNode.children();
		if (htmlNode.children() != null) {
			while (children.hasMoreElements()) {
				buildXMLTree(addedNode, (DefaultMutableTreeNode)children.nextElement());
			}
		}
	}
	
	private void saveFile(Document xmlTree) throws IOException {
		FileWriter fileWriter = new FileWriter(outputFile);
		fileWriter.write("<?xml version=\"1.0\"?>\n");
		fileWriter.write("<!DOCTYPE HelpIndex SYSTEM \"MS-Help://hx/resources/HelpIndex.DTD\">\n");

		OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(fileWriter, format);
        writer.write(xmlTree.getRootElement());
        
        writer.close();
        fileWriter.close();
	}
}
