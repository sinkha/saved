package com.cdmtech.m2e.flex.model;

import java.util.List;

import org.apache.maven.artifact.Artifact;

public class DependencyList {

   private List<Artifact> artifacts;
   
   public DependencyList(List<Artifact> artifacts) {
      this.artifacts = artifacts;
   }
   
   public List<Artifact> getArtifacts() {
      return artifacts;
   }
   
   public void setArtifacts(List<Artifact> value) {
      artifacts = value;
   }
   
}
