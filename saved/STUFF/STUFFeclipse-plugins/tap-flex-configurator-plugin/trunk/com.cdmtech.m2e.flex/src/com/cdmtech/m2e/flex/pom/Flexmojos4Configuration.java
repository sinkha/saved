package com.cdmtech.m2e.flex.pom;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author svanhoose
 *
 */
public class Flexmojos4Configuration extends FlexmojosConfiguration {
   
   public Flexmojos4Configuration(Plugin flexmojos, ModelObjectInterpolator interpolator) {
      super(flexmojos, interpolator);
   }

   @Override
   public List<String> getLocalesCompiled() {
      return getStringList("localesCompiled");
   }

   @Override
   public String getLocalesSourcePath() {
      return getString("localesSourcePath", "${basedir}/src/main/locales/{locale}");
   }

   @Override
   public List<CompilerDefine> getDefines() {
      Xpp3Dom d = configuration.getChild("defines");
      if (d == null) {
         return null;
      }
      
      List<CompilerDefine> defines = new ArrayList<CompilerDefine>();
      for (Xpp3Dom property : d.getChildren()) {
         defines.add(new CompilerDefine(property.getChild("name").getValue(),
               property.getChild("value").getValue()));
      }
      return defines;
   }

   @Override
   public ResourceBundleStyle getMergeResourceBundle() {
      if (configuration.getChild("localesCompiled") != null) {
         return ResourceBundleStyle.MERGE;
      }
      
      if (configuration.getChild("localesRuntime") != null) {
         return ResourceBundleStyle.SEPARATE;
      }
      
      return ResourceBundleStyle.NO_GENERATION;
   }

}
