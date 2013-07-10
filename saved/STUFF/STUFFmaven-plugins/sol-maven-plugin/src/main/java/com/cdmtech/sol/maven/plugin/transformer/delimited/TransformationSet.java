package com.cdmtech.sol.maven.plugin.transformer.delimited;

import java.io.File;
import java.util.Map;

public class TransformationSet {
   /**
    * A list of source files to include. These paths are relative to the
    * sourcesDirectory. Unlike the configs property, the runtime classpath will
    * not be searched for sources.
    * 
    * @parameter
    * @required
    */
   protected String[] sources;
   
   /**
    * Root directory of the source files.
    * 
    * @parameter
    * @required
    */
   protected File sourcesDirectory;
   
   /**
    * A list of configuration file paths used to dictate how to parse the
    * included files. These files can be either a file in the runtime
    * classpath or on the filesystem. The number of configPaths needs to
    * match the number of resulting included source files.
    * 
    * @parameter
    * @required
    */
   protected String[] configs;
   
   /**
    * The directory into which transformed files will be output.
    * 
    * @parameter
    * @required
    */
   protected File outputDirectory;
   
   /**
    * The filename for the resulting transformed output file.
    * 
    * @parameter
    * @optional
    */
   protected String outputFilename;
   
   /**
    * The root element name used to ensure the document is well-formed
    * in cases where multiple sources/configs are specified for a single
    * transformation set/output file. 
    * 
    * @parameter
    * @required
    */
   protected String rootElementName;
   
   /**
    * Configures the properties passed to the transformer.
    * 
    * @parameter
    * @optional
    */
   protected Map<String, String> properties;
}
