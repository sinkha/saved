package com.cdmtech.atlas.maven.plugins.transformIndex;

import java.io.File;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.maven.plugin.logging.Log;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 * Takes the output HTML file from Tome and builds
 * a tree of Keywords.
 */
public class TomeHTMLTreeGenerator {
	private DefaultMutableTreeNode indexRoot;
	private DefaultMutableTreeNode currentNode;
	private TagNode readerHTML;
	private String fileName;
	private Log log;
	
	public TomeHTMLTreeGenerator(Log log, String fileName) throws IOException {
		indexRoot = new DefaultMutableTreeNode(new Keyword
				("Index Root", null, -1));
		currentNode = indexRoot;
		this.log = log;
		this.fileName = fileName;
		
		openFile();
		buildTree();
	}
	
	public DefaultMutableTreeNode getRoot() {
		return indexRoot;
	}
	
	private void openFile() throws IOException {
		File fileRef = new File(fileName);
		if (!fileRef.exists()) {
			throw new IOException("Input file not found");
		}
		
		readerHTML = new HtmlCleaner().clean(fileRef);
	}
	
	private void buildTree() {
		TagNode[] tdElements = readerHTML.getElementsByName("td", true);
		
		if (tdElements == null || tdElements.length == 0) {
			log.warn("No <td> column elements were found in " + fileName);
		} else {
			log.info("Parsing " + tdElements.length + " column elements");
		}
		
		for (TagNode node : tdElements) {
			Keyword currentKey = new Keyword(node);
			
			int previousDepth;
			do {
				previousDepth = ((Keyword)currentNode.getUserObject()).getDepth();
				if (currentKey.getDepth() > previousDepth) {
					DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(currentKey);
					currentNode.add(tempNode);
					currentNode = tempNode;
				} else {
					currentNode = (DefaultMutableTreeNode)currentNode.getParent();
				}
			} while (currentKey.getDepth() <= previousDepth);
		}
	}
}
