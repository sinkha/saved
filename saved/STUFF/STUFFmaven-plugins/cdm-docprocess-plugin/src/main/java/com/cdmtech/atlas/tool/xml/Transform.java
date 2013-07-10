package com.cdmtech.atlas.tool.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * The Transform class is a static wrapper around the
 * XALAN XSL transform engine that provides a simple filter
 * interface allowing for piped command chains (standard input
 * may be used to pipe input from another process, standard
 * output can be used to pipe output to another process).
 *
 * @see Transformer
 */
public class Transform {
   public static void main(String[] args) throws Exception
   {
      InputStream xsl = null;
      StreamSource source = new StreamSource(System.in);
      StreamResult result = new StreamResult(System.out);
      String systemId = null;

      LongOpt[] longopts = new LongOpt[5];

      longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
      longopts[1] = new LongOpt("in", LongOpt.REQUIRED_ARGUMENT, null, 'i');
      longopts[2] = new LongOpt("out", LongOpt.REQUIRED_ARGUMENT, null, 'o');
      longopts[3] = new LongOpt("param", LongOpt.REQUIRED_ARGUMENT, null, 'p');
      longopts[4] = new LongOpt("id", LongOpt.REQUIRED_ARGUMENT, null, 's');

      Getopt g = new Getopt("Transform", args, "hi:o:p:s:", longopts);
      Hashtable<String,String> params = new Hashtable<String,String>();

      int c;

      while ((c = g.getopt()) != -1) {
         switch(c) {
             case 'h':
                 printUsage();
                 return;
             case 'i':
                 String inpath = g.getOptarg();
                 File infile = new File(inpath);

                 if (!infile.exists()) {
                     System.err.println("\n\nInput file does not exist: " +
                                        inpath + "\n");
                     return;
                 }

                 source = new StreamSource(infile);
                 break;
             case 'o':
                 String outpath = g.getOptarg();
                 File outfile = new File(outpath);
                 File parentfile = outfile.getParentFile();

                 if ((parentfile != null) && !parentfile.exists()) {
                     parentfile.mkdirs();
                 }

                 result = new StreamResult(outfile);
                 break;
             case 'p':
                 String param = g.getOptarg();
                 int n = param.indexOf('=');

                 if (n > 0) {
                     String name = param.substring(0, n);
                     String value = param.substring(n+1);

                     params.put(name, value);
                 } else {
                     System.err.println("\n\nError in parameter specification: '" +
                                        param + "'\n");
                     return;
                 }
                 break;
             case 's':
                 systemId = g.getOptarg();
                 break;
             default:
                 System.err.println("\n\nUnsupported option: " + (char)c + "\n");
                 printUsage();
                 return;
          }
      }

      if (g.getOptind() <= (args.length - 1)) {
          String path = args[g.getOptind()];
          File file = new File(path);
          InputStream input = null;

          if (!file.exists()) {
              input = Transform.class.getClassLoader().getResourceAsStream(path);
          }

          if (input == null) {
              input = new FileInputStream(file);
          }

          xsl = new BufferedInputStream(input);
      }
      if (xsl == null) {
         printUsage();
         return;
      }

      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer(new StreamSource(xsl));

      if (systemId != null) {
          source.setSystemId(systemId);
      }

      for (String name : params.keySet()) {
          transformer.setParameter(name, params.get(name));
      }

      transformer.transform(source, result);
   }

   /**
    * Prints the usage of this plugin and additional options
    * and their required parameters.
    */
   private static void printUsage() {
      System.err.println("usage: Transform (options) <XSL transform>\n");
      System.err.println("options:");
      System.err.println("  -i, --in <string>");
      System.err.println("         Input document (default: standard input stream)");
      System.err.println("  -o, --out <string>");
      System.err.println("         Output document (default: standard output stream)");
      System.err.println("  -p, --param <name=value>");
      System.err.println("         Named parameter passed to transform.");
      System.err.println("  -s, --id");
      System.err.println("         System identifier - used to resolve relative URIs (default: set to input document, if defined)");
      System.err.println("  -h, --help");
      System.err.println("         This help screen.");
   }
}
