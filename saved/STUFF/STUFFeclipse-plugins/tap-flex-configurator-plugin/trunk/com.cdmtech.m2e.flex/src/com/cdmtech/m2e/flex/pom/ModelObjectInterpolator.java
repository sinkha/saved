package com.cdmtech.m2e.flex.pom;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.ValueSource;

/**
 * @author svanhoose
 *
 */
public class ModelObjectInterpolator extends StringSearchModelInterpolator {
   
   private ModelBuildingRequest request;
   private List<? extends ValueSource> valueSources;
   private List<? extends InterpolationPostProcessor> postProcessors;
   private ModelProblemCollector problems;
   
   private Model model;
   private File projectDir;
   
   public ModelObjectInterpolator(Model model, File projectDir, ModelBuildingRequest request) {
      this.request = request;
      this.model = model;
      this.projectDir = projectDir;
      
      valueSources = createValueSources(model, projectDir, request, problems);
      postProcessors = createPostProcessors(model, projectDir, request);
   }
   
   public void interpolate(Object obj) {
      interpolateObject(obj, model, projectDir, request, problems);
   }
   
   public String interpolateString(String value) {
      return interpolateInternal(value, valueSources, postProcessors, problems);
   }
   
}
