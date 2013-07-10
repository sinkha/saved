package com.cdmtech.core.xml;

import java.io.File;
import java.util.Map;

public class TransformationSet {
   /**
    * A list of source file patterns to include. Can contain ant-style wildcards
    * and double wildcards.
    * 
    * @parameter
    */
   protected String[] includes;
   
   /**
    * A list of source file patterns to exclude. Can contain ant-style wildcards
    * and double wildcards.
    * 
    * @parameter
    */
   protected String[] excludes;
   
   /**
    * Root directory of the included source files to be used during the
    * transformation.
    * 
    * @parameter expression="${basedir}"
    */
   protected File includesDirectory;
   
   /**
    * The directory into which transformed files will be output.
    * 
    * @parameter expression="${project.build.directory}"
    */
   protected File outputDirectory;
   
   /**
    * The directory into which transformed files will be output.
    * 
    * @parameter
    */
   protected String outputFilename;
   
   /**
    * A directory into which temporary files can be created (i.e. when both
    * usingSAX and batchIncludes are true, a temporary file must be created
    * for the combined xml prior to reading it in as a stream).
    * 
    * @parameter expression="${project.build.directory}/tmp"
    */
   protected File tempDirectory;
   
   /**
    * Path to XSLT file used to perform the transformation.
    * 
    * @parameter
    */
   protected String stylesheet;
   
   /**
    * Configures the properties passed to the transformer.
    * 
    * @parameter
    */
   protected Map<String, String> properties;
   
   /**
    * Configures the transformer's output format/method.
    * 
    * @parameter default-value="xml" expression="xml"
    */
   protected String outputMethod = "xml";
   
   /**
    * Configures the transformer's output indentation amount.
    * 
    * @parameter default-value=2 expression=2
    */
   protected int indentAmount = 2;
   
   /**
    * Configures the transformation to use a SAX parser for the input XML.
    * 
    * @parameter default-value=false expression=false
    */
   protected boolean useSAX;
   
   /**
    * If true, all includes for this transformation set will be processed as
    * though they were a single source document.
    */
   protected boolean batchIncludes;
}
