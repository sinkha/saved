package com.cdmtech.sol.maven.plugin.metadata;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.Application;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.Artifact;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.BuildInfo;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.Domain;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.Metadata;
import com.cdmtech.proj.sol.meta.cr.v1.crmetadata.ObjectFactory;

/**
 * Creates an Archive Metadata XML file. Adheres to the sol-meta component's
 * component-2.0.xsd schema.
 * 
 * @goal create-cr-metadata
 * @phase generate-resources
 * @author jundesser
 */
public class CreateCRMetadataMojo extends AbstractMojo {

	public static final String METADATA_VERSION = "v1";
	public static final String METADATA_PACKAGE = "com.cdmtech.proj.sol.meta.cr.v1.crmetadata";


	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	protected MavenProject project;

	/**
	 * The directory into which the component metadata XML will be output.
	 * 
	 * @parameter default-value="${project.build.outputDirectory}/doc"
	 *            expression="${project.build.outputDirectory}/doc"
	 */
	protected File outputDirectory;

	/**
	 * The file name for the component metadata XML artifact.
	 * 
	 * @parameter default-value="cr.metadata.xml"
	 *            expression="cr.metadata.xml"
	 */
	protected String outputFilename;

	/**
	 * The prefix to strip off the artifact to get at the shortName which is the objectKey
	 * ex:  if artifactId = "icodesv6_0_3-ship-abby" 
	 *      and artifactIdPrefix = "icodesv6_0_3-ship"
	 *      then the resulting artifactId that will be used later would be "abby"
	 * @parameter
	 */
	protected String artifactIdPrefix;

	/**
	 * The artifact version
	 * 
	 * @parameter
	 */
	protected String artifactVersion;
	
	/**
	 * The application name that this artifact belongs to
	 * 
	 * @parameter
	 */
	protected String applicationName;

	/**
	 * The application unique key that this artifact belongs to
	 * 
	 * @parameter
	 */
	protected String applicationKey;

	/**
	 * The application domain name that this artifact belongs to
	 * 
	 * @parameter
	 */
	protected String domainName;

	/**
	 * The application domain unique key that this artifact belongs to
	 * 
	 * @parameter
	 */
	protected String domainKey;

	/**
	 * The version of the parent pom used to build this component.
	 * 
	 * @parameter
	 */
	protected String parentBuildVerison;

	/**
	 * The version of sol used to build this component.
	 * 
	 * @parameter
	 */
	protected String solVersion;

	/**
	 * The version of the sol-maven-plugin used to build this component.
	 * 
	 * @parameter
	 */
	protected String solMavenPluginVersion;

	public void execute() throws MojoExecutionException {
		try {
			checkRequiredPropertyValues();
			Metadata crMetadata = createCRMetadataObject();
			
			File outFile = new File(outputDirectory, outputFilename);
			File parentDir = outFile.getParentFile();
			if (parentDir != null)
				parentDir.mkdirs();

			outFile.createNewFile();

			JAXBContext jaxbContext = JAXBContext.newInstance(METADATA_PACKAGE);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(crMetadata, outFile);

			if (project.getExecutionProject() != null) {
				project.getExecutionProject().addCompileSourceRoot(outputDirectory.getAbsolutePath());
			}

			project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
			
		}
		catch (Exception e) {
			throw new MojoExecutionException("Failed to create component metadata XML"
			        + (e.getMessage() != null ? ": " + e.getMessage() : "") + ".", e);
		}
	}

	private Metadata createCRMetadataObject() throws MojoExecutionException {
		ObjectFactory objectFactory = new ObjectFactory();

		
		String shortName = project.getArtifactId().replace(artifactIdPrefix, "");
		
		Metadata crMetadata = objectFactory.createMetadata();
		crMetadata.setVersion(METADATA_VERSION);

		BuildInfo buildInfo = objectFactory.createBuildInfo();
		buildInfo.setParentBuildVersion(this.parentBuildVerison);
		buildInfo.setSolMavenPluginVersion(this.solMavenPluginVersion);
		buildInfo.setSolVersion(this.solVersion);
		crMetadata.setBuildInfo(buildInfo);
		
		Artifact artifact = objectFactory.createArtifact();
		String artifactKey = this.domainKey + " " + shortName;
		artifact.setObjectKey(artifactKey);
		artifact.setVersion(this.artifactVersion);
		crMetadata.setArtifact(artifact);

		Application application = objectFactory.createApplication();
		application.setObjectKey( this.applicationKey);
		application.setDisplayName(this.applicationName);
		artifact.setApplication(application);
		
		Domain domain = objectFactory.createDomain();
		domain.setObjectKey(this.domainKey);
		domain.setDisplayName(this.domainName);
		application.setDomain(domain);
		
		return crMetadata;
		
	}
	
	private void checkRequiredPropertyValues() throws MojoExecutionException {
		checkRequiredPropertyValue("artifactIdPrefix", this.artifactIdPrefix);
		checkRequiredPropertyValue("artifactVersion", this.artifactVersion);
		checkRequiredPropertyValue("applicationKey", this.applicationKey);
		checkRequiredPropertyValue("applicationName", this.applicationName);
		checkRequiredPropertyValue("domainKey", this.domainKey);
		checkRequiredPropertyValue("domainName", this.domainName);
		checkRequiredPropertyValue("solVersion", this.solVersion);
		checkRequiredPropertyValue("solMavenPluginVersion", this.solMavenPluginVersion);
		checkRequiredPropertyValue("parentBuildVerison", this.parentBuildVerison);
	}
	private void checkRequiredPropertyValue(String propertyName, String propertyValue) throws MojoExecutionException {
//		System.out.println("Property: " + propertyName);
//		System.out.println("Value   : " + propertyValue);

		if (propertyValue == null) {
			throw new MojoExecutionException("Property <" + propertyName + "> has not been set in the POM");
		}
		else if (propertyValue.trim().length() == 0) {
			throw new MojoExecutionException("Property <" + propertyName + "> has not been set in the POM");
		}
	}
}
