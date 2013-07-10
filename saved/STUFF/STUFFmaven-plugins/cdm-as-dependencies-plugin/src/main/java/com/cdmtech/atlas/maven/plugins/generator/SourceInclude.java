package com.cdmtech.atlas.maven.plugins.generator;

import java.io.File;

public class SourceInclude {

    /**
     * Root directory files to be included.
     * 
     * @parameter expression="${basedir}/src/main/flex"
     */
    File filesDirectory;
    
    /**
     * A list of files to include. Can contain ant-style wildcards and double wildcards.
     * By default, everything in the imagesDirectory is included.
     * <code>**</code>
     *
     * @parameter
     */
    String[] includes;

    /**
     * A list of files to exclude. Can contain ant-style wildcards and double wildcards.
     *
     * @parameter
     */
    String[] excludes;
    
}
