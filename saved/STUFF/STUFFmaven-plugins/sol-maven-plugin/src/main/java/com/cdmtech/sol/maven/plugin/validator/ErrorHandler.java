package com.cdmtech.sol.maven.plugin.validator;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ErrorHandler extends DefaultHandler {
   private Log log;
   private List<SAXParseException> errors = new ArrayList<SAXParseException>();
   private List<SAXParseException> fatalErrors = new ArrayList<SAXParseException>();
   
   public ErrorHandler(Log log) {
      this.log = log;
   }
   
   public void warning(SAXParseException e) throws SAXException {
      log.warn(e);
      log.warn("Public ID:     "+e.getPublicId());
      log.warn("System ID:     "+e.getSystemId());
      log.warn("Line number:   "+e.getLineNumber());
      log.warn("Column number: "+e.getColumnNumber());
      log.warn("Message:       "+e.getMessage());
   }
   
   public void error(SAXParseException e) throws SAXException {
      errors.add(e);
      log.error(e);
      log.error("Public ID:     "+e.getPublicId());
      log.error("System ID:     "+e.getSystemId());
      log.error("Line number:   "+e.getLineNumber());
      log.error("Column number: "+e.getColumnNumber());
      log.error("Message:       "+e.getMessage());
   }
   
   public void fatalError(SAXParseException e) throws SAXException {
      fatalErrors.add(e);
      log.error("Fatal Error", e);
      log.error("Public ID:     "+e.getPublicId());
      log.error("System ID:     "+e.getSystemId());
      log.error("Line number:   "+e.getLineNumber());
      log.error("Column number: "+e.getColumnNumber());
      log.error("Message:       "+e.getMessage());
   }
   
   public List<SAXParseException> getErrors() {
      return errors;
   }
   
   public List<SAXParseException> getFatalErrors() {
      return fatalErrors;
   }
}
