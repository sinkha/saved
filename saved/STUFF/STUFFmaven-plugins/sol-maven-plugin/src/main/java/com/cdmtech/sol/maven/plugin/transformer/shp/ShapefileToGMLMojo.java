package com.cdmtech.sol.maven.plugin.transformer.shp;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.cdmtech.sol.maven.plugin.FileProcessingUtils;

/**
 * Transforms shapfiles into GML using Geotools.
 * 
 * @goal shp-to-gml
 * @phase generate-resources
 * @author csleight
 */
public class ShapefileToGMLMojo extends AbstractMojo {
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
   
   public void execute() throws MojoExecutionException {
      boolean error = false;
      
      List<String> includedFilePaths = null;
      try {
         includedFilePaths = FileProcessingUtils.getIncludedFilePaths(includesDirectory, includes, excludes);
      } catch (Exception e) {
         throw new MojoExecutionException("Could not determine included files.", e);
      }
      
      if (includedFilePaths == null || includedFilePaths.size() == 0) {
         getLog().error("No includes specified.");
         error = true;
      }
      
      if (!error) {
         try {
            for (String includedFilePath : includedFilePaths) {
               File f = new File(includesDirectory, includedFilePath);
               shpToGml(f, outputDirectory);
            }
         } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed processing include: " + e.getMessage(), e);
         }
      }
      else {
         throw new MojoExecutionException("Import Failed");
      }
   }
   
   /**
    * Converts an ESRI shapefile into a GML file on the filesystem.
    * 
    * @param shapefile
    *           the <code>File</code>
    * @param outputDir
    *           directory to place the resulting gml. If null, the gml
    *           <code>File</code> will be created in the current working
    *           directory.
    * @throws Exception
    */
   public void shpToGml(File shapefile, File outputDir) throws Exception {
      URL shapefileURL = shapefile.toURI().toURL();
      long startTime = System.currentTimeMillis();
      
      getLog().info("Parsing shapefile: '" + shapefileURL + "' ...");
      
      String outFilename;
      String shapefileName = shapefile.getName();
      int index = shapefileName.toLowerCase().lastIndexOf(".shp");
      if (index > 0)
         outFilename = shapefileName.substring(0, index) + ".xml";
      else
         outFilename = shapefileName + ".xml";

      File outFile;
      if (outputDir != null)
         outFile = new File(outputDir, outFilename);
      else
         outFile = new File(outFilename);

      File parentDir = outFile.getParentFile();
      parentDir.mkdirs();
      
      final FileOutputStream outStream = new FileOutputStream(outFile);

      FeatureCollection<SimpleFeatureType, SimpleFeature> features = null;
      FeatureIterator<SimpleFeature> itr = null;
      try {
         DataStore dataStore = new ShapefileDataStore(shapefileURL);
         String typeName = dataStore.getTypeNames()[0];

         FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
               .getFeatureSource(typeName);

         features = source.getFeatures();
         
         FeatureTransformer transformer = new FeatureTransformer();
         transformer.setNumDecimals(18); // covers most data precision cases
         transformer.setIndentation(1);

         getLog().info("Writing GML to: " + outFile.getAbsolutePath());
         
         // transform
         transformer.transform(features, outStream);
         outStream.close();
         
         long endTime = System.currentTimeMillis();
         getLog().info("Finished writing GML to: " + outFile.getAbsolutePath() + " in " + ((endTime-startTime) / 1000) + " seconds.");
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (features != null && itr != null) {
            features.close(itr);
         }
      }
   }
}
