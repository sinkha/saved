package com.cdmtech.m2e.flex.pom;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author svanhoose
 *
 */
public class Flexmojos3Configuration extends FlexmojosConfiguration {

   public Flexmojos3Configuration(Plugin flexmojos, ModelObjectInterpolator interpolator) {
      super(flexmojos, interpolator);
   }

   @Override
   public List<String> getLocalesCompiled() {
      return getStringList("compiledLocales");
   }
   
   @Override
   public String getLocalesSourcePath() {
      return getString("resourceBundlePath", "${basedir}/src/main/locales/{locale}");
   }
   
   @Override
   public List<CompilerDefine> getDefines() {
      Xpp3Dom d = configuration.getChild("definesDeclaration");
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
      Xpp3Dom p = configuration.getChild("mergeResourceBundle");
      if (p == null) {
         return ResourceBundleStyle.NO_GENERATION;
      }
      String value = p.getValue().trim().toLowerCase();
      return "true".equals(value) ? ResourceBundleStyle.MERGE : ResourceBundleStyle.SEPARATE;
   }
   
}
