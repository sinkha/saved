package com.cdmtech.m2e.flex.sdk;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;

public class PlaceholderArtifact implements Artifact {

   @Override
   public int compareTo(Artifact o) {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void addMetadata(ArtifactMetadata arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public ArtifactHandler getArtifactHandler() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getArtifactId() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<ArtifactVersion> getAvailableVersions() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getBaseVersion() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getClassifier() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getDependencyConflictId() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ArtifactFilter getDependencyFilter() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<String> getDependencyTrail() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getDownloadUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public File getFile() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getGroupId() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getId() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Collection<ArtifactMetadata> getMetadataList() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ArtifactRepository getRepository() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getScope() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ArtifactVersion getSelectedVersion() throws OverConstrainedVersionException {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getType() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getVersion() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public VersionRange getVersionRange() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean hasClassifier() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isOptional() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isRelease() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isResolved() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isSelectedVersionKnown() throws OverConstrainedVersionException {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isSnapshot() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void selectVersion(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setArtifactHandler(ArtifactHandler arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setArtifactId(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setAvailableVersions(List<ArtifactVersion> arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setBaseVersion(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setDependencyFilter(ArtifactFilter arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setDependencyTrail(List<String> arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setDownloadUrl(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setFile(File arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setGroupId(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setOptional(boolean arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setRelease(boolean arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setRepository(ArtifactRepository arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setResolved(boolean arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setResolvedVersion(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setScope(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setVersion(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setVersionRange(VersionRange arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void updateVersion(String arg0, ArtifactRepository arg1) {
      // TODO Auto-generated method stub

   }

}
