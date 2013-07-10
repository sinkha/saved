package com.cdmtech.m2e.flex.pom;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author svanhoose
 *
 */
public abstract class FlexmojosConfiguration {
   
   public static enum ResourceBundleStyle {
      MERGE,
      SEPARATE,
      NO_GENERATION
   }
   
   private ModelObjectInterpolator interpolator;
   protected Xpp3Dom configuration;
   
   public FlexmojosConfiguration(Plugin flexmojos, ModelObjectInterpolator interpolator) {
      this.interpolator = interpolator;
      this.configuration = (Xpp3Dom)flexmojos.getConfiguration();
   }
   
   protected boolean getBool(String name, boolean defaultValue) {
      Xpp3Dom b = configuration.getChild(name);
      if (b == null) {
         return defaultValue;
      }
      
      String value = b.getValue();
      return value == null ? defaultValue :
         "true".equals(value.trim().toLowerCase());
   }
   
   protected String getString(String name) {
      Xpp3Dom s = configuration.getChild(name);
      if (s == null) {
         return null;
      }
      return s.getValue();
   }
   
   protected String getString(String name, String defaultValue) {
      Xpp3Dom s = configuration.getChild(name);
      if (s == null) {
         return interpolator.interpolateString(defaultValue);
      }
      return s.getValue();
   }
   
   protected String[] getStringArray(String name) {
      return getStringArray(configuration.getChild(name));
   }
   
   protected String[] getStringArray(Xpp3Dom parent) {
      if (parent == null) {
         return null;
      }
      
      Xpp3Dom[] children = parent.getChildren();
      String[] values = new String[children.length];
      for (int i = 0; i < children.length; i++) {
         values[i] = children[i].getValue();
      }
      return values;
   }
   
   protected List<String> getStringList(String name) {
      return getStringList(configuration.getChild(name));
   }
   
   protected List<String> getStringList(Xpp3Dom parent) {
      if (parent == null) {
         return null;
      }
      
      Xpp3Dom[] children = parent.getChildren();
      List<String> values = new ArrayList<String>(children.length);
      for (int i = 0; i < children.length; i++) {
         values.add(children[i].getValue());
      }
      return values;
   }
   
   public boolean getAccessible() {
      return getBool("accessible", false);
   }
   
   public boolean getStrict() {
      return getBool("strict", true);
   }
   
   public boolean getVerifyDigests() {
      return getBool("verifyDigests", true);
   }
   
   public boolean getShowWarnings() {
      return getBool("showWarnings", true);
   }
   
   public boolean getGenerateHtmlWrapper() {
      return getBool("generateHtmlWrapper", false);
   }
   
   /**
    * Get the target player version. Null if not specified, in which
    * case the configurator should default to 0.0.0 (use SDK default.)
    */
   public String getTargetPlayer() {
      return getString("targetPlayer");
   }
   
   public String[] getBuildCssFiles() {
      //TODO: Is this equivalent to includeStylesheets in FM4-compile-swc?
      return getStringArray("buildCssFiles");
   }
   
   public String[] getAdditionalApplications() {
      return getStringArray("additionalApplication");
   }
   
   public String getIdeOutputFolderPath() {
      return getString("ideOutputFolderPath", "bin-debug");
   }
   
   public String getIdeOutputFolderLocation() {
      return getString("ideOutputFolderLocation");
   }
   
   public String getRootUrl() {
      return getString("rootURL");
   }
   
   public abstract String getLocalesSourcePath();
   
   public abstract List<String> getLocalesCompiled();
   
   public String getDefaultLocale() {
      return getString("defaultLocale", "en_US");
   }
   
   public String[] getIncludeClasses() {
      //TODO: Support fm4 includeclasses syntax
      return getStringArray("includeClasses");
   }
   
   /**
    * compile-swc only
    */
   public String[] getIncludeSources() {
      return getStringArray("includeSources");
   }
   
   public String[] getIncludeFiles() {
      return getStringArray("includeFiles");
   }
   
   public abstract ResourceBundleStyle getMergeResourceBundle();
   
   public List<FlexNamespace> getNamespaces() {
      Xpp3Dom p = configuration.getChild("namespaces");
      if (p == null) {
         return null;
      }
      
      List<FlexNamespace> list = new ArrayList<FlexNamespace>();
      Xpp3Dom[] properties = p.getChildren();
      for (Xpp3Dom prop : properties) {
         Xpp3Dom uri = prop.getChild("uri");
         Xpp3Dom manifest = prop.getChild("manifest");
         
         if (uri != null && manifest != null) {
            list.add(new FlexNamespace(uri.getValue(), manifest.getValue()));
         }
      }
      return list;
   }
   
   public abstract List<CompilerDefine> getDefines();
   
   public String getContextRoot() {
      return getString("contextRoot");
   }
   
   /**
    * "The file to be compiled. The path must be relative to the source folder."
    */
   public String getSourceFile() {
      return getString("sourceFile");
   }
   
   public List<String> getConfigFiles() {
      return getStringList("configFiles");
   }
   
   public List<String> getThemes() {
      return getStringList("themes");
   }
   
   public String getServices() {
      return getString("services");
   }
}
