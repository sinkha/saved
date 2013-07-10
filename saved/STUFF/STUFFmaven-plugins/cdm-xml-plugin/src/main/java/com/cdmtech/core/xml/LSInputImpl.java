package com.cdmtech.core.xml;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

public class LSInputImpl implements LSInput {

   protected String fPublicId;
   protected String fSystemId;
   protected String fBaseSystemId;

   protected InputStream fByteStream;
   protected Reader fCharStream;
   protected String fData;

   protected String fEncoding;

   protected boolean fCertifiedText;

   public LSInputImpl() {
   }

   public LSInputImpl(String publicId, String systemId, InputStream byteStream) {
      fPublicId = publicId;
      fSystemId = systemId;
      fByteStream = byteStream;
   }

   public InputStream getByteStream() {
      return fByteStream;
   }

   public void setByteStream(InputStream byteStream) {
      fByteStream = byteStream;
   }

   public Reader getCharacterStream() {
      return fCharStream;
   }

   public void setCharacterStream(Reader characterStream) {
      fCharStream = characterStream;
   }

   public String getStringData() {
      return fData;
   }

   public void setStringData(String stringData) {
      fData = stringData;
   }

   public String getEncoding() {
      return fEncoding;
   }

   public void setEncoding(String encoding) {
      fEncoding = encoding;
   }

   public String getPublicId() {
      return fPublicId;
   }

   public void setPublicId(String publicId) {
      fPublicId = publicId;
   }

   public String getSystemId() {
      return fSystemId;
   }

   public void setSystemId(String systemId) {
      fSystemId = systemId;
   }

   public String getBaseURI() {
      return fBaseSystemId;
   }

   public void setBaseURI(String baseURI) {
      fBaseSystemId = baseURI;
   }

   public boolean getCertifiedText() {
      return fCertifiedText;
   }

   public void setCertifiedText(boolean certifiedText) {
      fCertifiedText = certifiedText;
   }

}
