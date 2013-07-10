package com.cdmtech.core.tool.exec;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Goal which processes an execution suite
 *
 * @goal process
 */
public class SuiteProcessorMojo extends AbstractMojo {
    /**
     * The Maven project object
     *
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * Suite execution result output directory.
     * @parameter expression="${process.outputDirectory}" default-value="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * This folder is added to the list of those folders containing source
     * to be compiled. Use this if the suite generates source code.
     *
     * @parameter expression="${sourceRoot}"
     */
    private File sourceRoot; 

    /**
     * This folder is added to the list of those folders containing source
     * to be compiled. Use this if the suite generates source code.
     *
     * @parameter expression="${testSourceRoot}"
     */
    private File testSourceRoot; 

    /**
     * Suite execution file. Used to specify single external suite to execute.
     * @parameter
     */
    private File suiteFile;

    /**
     * Suite system properties.
     * @parameter
     */
    private Map<String,String> properties;

    /**
     * @parameter expression="${project.compileClasspathElements}"
     * @readonly
     */
    private List<String> artifacts;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     */
    private List<String> pluginArtifacts;

    /**
     * The XML for the embedded suite. You can add anything you can add
     * between &lt;suite&gt; and &lt;/suite&gt; in a suite file.
     * @parameter
     */
	private PlexusConfiguration suite;
	
    private SuiteProcessor processor;

    public void execute() throws MojoExecutionException {
        SuiteProcessor.resultdir = this.outputDirectory.getPath();

        try {
            this.processor = new SuiteProcessor();
        } catch (Exception e) {
            throw 
                new MojoExecutionException("Error in suite processor", e);
        }

        StringBuffer javaclasspath = new StringBuffer();

        for (Iterator<String> i = this.artifacts.iterator(); i.hasNext();) {
            String path = i.next();

            javaclasspath.append(path);
            if (i.hasNext()) javaclasspath.append(File.pathSeparator);
        }
        if (this.pluginArtifacts.size() > 0) {
            javaclasspath.append(File.pathSeparator);
            for (Iterator i = this.pluginArtifacts.iterator(); i.hasNext();) {
                Artifact a = (Artifact) i.next();
                File file = a.getFile();

                if (file != null) {
                    javaclasspath.append(file.getPath());
                    if (i.hasNext()) javaclasspath.append(File.pathSeparator);
                }
            }
        }
        //getLog().info("Execute suite with classpath "+javaclasspath);

        System.setProperty("java.class.path",
                           javaclasspath.toString()); 

        if (this.properties != null) {
            for (String key: this.properties.keySet()) {
                System.setProperty(key, this.properties.get(key));
            }
        }

        if (this.suiteFile != null) {
            processSuite(this.suiteFile);
        } else {
            try {
                processSuite(this.writeSuiteToProjectFile());
            } catch ( Exception e ) {
                throw new MojoExecutionException("Error processing suite: "
                                                 + e.getMessage(), e);
            }
        }

        if (this.sourceRoot != null) {
            getLog().info("Adding source root " + this.sourceRoot);
            this.project.addCompileSourceRoot(this.sourceRoot.toString());
        }

        if (this.testSourceRoot != null) {
            getLog().info("Adding test source root " + this.testSourceRoot);
            this.project.addTestCompileSourceRoot(this.testSourceRoot.toString());
        }
    }

    private void processSuite(File suiteFile)
        throws MojoExecutionException {

        String suiteName = suiteFile.getPath();

        getLog().info("Begin suite processing for: " + suiteName);

        try {
            Suite suite = (Suite) this.processor.parse(suiteName);

            new StatusPrinter(suite);

            if (suite.run() < 0) {
                throw new MojoExecutionException("Execution failed for suite"
                                                 + suiteName);
            }
        } catch ( Exception e ) {
            throw new MojoExecutionException("Error processing file "
                                             + suiteName, e);
        }
        getLog().info("Completed suite processing for: " + suiteName);
    }

    /**
     * Write the suite and surrounding tags to a temporary file
     * 
     * @throws PlexusConfigurationException
     */
    private File writeSuiteToProjectFile()
        throws IOException, PlexusConfigurationException
    {
        String suiteId = this.suite.getAttribute("id");
        String fileName;

        if (suiteId == null) {
            fileName = "suite.xxs";
        } else {
            fileName = suiteId + ".xxs";
        }

        File suiteFile = new File(this.outputDirectory, fileName);

        suiteFile.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(suiteFile);
        XmlPlexusConfigurationWriter xmlWriter =
            new XmlPlexusConfigurationWriter();

        xmlWriter.write(this.suite, writer);
        writer.close();
        return suiteFile;
    }
}
