package com.cdmtech.atlas.maven.plugins.transformIndex;

import org.htmlcleaner.TagNode;

/** 
 * Represents a single Keyword as listed in the index.hxk.
 */
public final class Keyword 
{
	private String term;
	private String url;
	private int depth;
	
	public Keyword(TagNode node) {
		this.term = cleanTerm(node);
		this.url = findUrl(node);
		this.depth = calculateDepth(node);
	}
	
	public Keyword(String term, String url, int depth) {
		this.term = term;
		this.url = url;
		this.depth = depth;
	}
	
	public String getTerm() {
		return term;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getDepth() {
		return depth;
	}
	
	/**
	 * Contains FarHTML URL fix, replaces "." with "content".
	 * 
	 * @return null if no URL is present for the keyword.
	 */
	private String findUrl(TagNode node) {
		for (TagNode child : node.getChildTags()) {
			if (child.getName().equals("a")) {
				return child.getAttributeByName("href")
					.replaceFirst(".", "content");
			}
		}
		
		return null;
	}
	
	/**
	 * The depth, or indentation is found by the number of
	 * leading "&nbsp;" strings in the term.
	 */
	private int calculateDepth(TagNode node) {
		String term = node.getText().toString().trim();
		int counter = 0;
		
		while (term.indexOf("&nbsp;") == 0) {
			term = term.substring(6);
			counter++;
		}
		
		return counter;
	}
	
	private String cleanTerm(TagNode node) {
		String term = node.getText().toString();	
		return term.replaceAll("&nbsp;", "").trim();
	}	
}
