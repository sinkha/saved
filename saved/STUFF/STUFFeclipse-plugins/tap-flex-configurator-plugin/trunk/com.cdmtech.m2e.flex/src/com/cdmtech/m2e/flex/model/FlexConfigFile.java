package com.cdmtech.m2e.flex.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.eclipse.core.resources.IProject;


public class FlexConfigFile {

   private File fileLoc;
   private List<String> themes;

   public FlexConfigFile(IProject project) {
      fileLoc = project.getFile(".flexConfig.xml").getLocation().toFile();
   }
   
   public void setThemes(List<String> themes) {
      this.themes = themes;
   }

   public List<String> getThemes() {
      return themes;
   }
   
   public void writeFile() {
      PrintWriter printWriter = null;
      try {
         printWriter = new PrintWriter(fileLoc);
      }
      catch (FileNotFoundException ex) {
         ex.printStackTrace();
      }
      
      PrettyPrintXMLWriter w = new PrettyPrintXMLWriter(printWriter);
      w.startElement("flex-config");
      w.addAttribute("xmlns", "http://www.adobe.com/2006/flex-config");
      w.startElement("compiler");
      
      if (this.themes != null && this.themes.size() > 0) {
         w.startElement("theme");
         for (String theme : this.themes) {
            w.startElement("filename");
            w.writeText(theme);
            w.endElement();
         }
         w.endElement();
      }
      
      w.endElement();
      w.endElement();
      
      printWriter.close();
   }
   
}
