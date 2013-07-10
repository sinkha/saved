package com.cdmtech.sol.maven.plugin;

import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.cdmtech.core.client.bean.AbstractView;
import com.cdmtech.core.client.bean.BeanManager;
import com.cdmtech.core.client.bean.View;

/**
 * Removes objects from the default view.
 * 
 * @goal clear-objects
 * @phase generate-resources
 * @author csleight
 */
@SuppressWarnings("unchecked")
public class ClearObjectsMojo extends AbstractMojo {
   /**
    * The view used by the exporter throughout.
    */
   protected static View view;

   static {
      try {
         view = AbstractView.getDefaultView();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   public void execute() throws MojoExecutionException {
      try {
         BeanManager beanManager = view.getBeanManager(null);
         List instances = beanManager.getAllInstances();
         for (Object instance : instances) {
            beanManager = view.getBeanManager(instance.getClass().getName());
            beanManager.removeInstance(instance);
         }
         view.commit();
      } catch (Exception e) {
         throw new MojoExecutionException("Clear objects failed.", e);
      }
   }
}
