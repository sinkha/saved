<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:str="http://exslt.org/strings"
                version="1.0">
  <xsl:output method="text"/>
  <xsl:strip-space elements="*"/>

<!-- ignore all elements that are not explicitly matched on -->
<xsl:template match="*"/>

<xsl:template match="releaseNotes">
#========================================================
# <xsl:value-of select="@title"/>
# Version <xsl:value-of select="//component/release[position()=1]/@version"/>
# Date <xsl:value-of select="//component/release[position()=1]/@date"/>
#========================================================

<xsl:apply-templates/>
</xsl:template>

<xsl:template match="description">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="list">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="item">
* <xsl:apply-templates/>
</xsl:template>

<xsl:template match="link">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="emphasis">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="literal">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="requirements">

Requirements:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="distribution">

Distribution Contents:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="installation">

Installation Instructions:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="usage">

Usage Instructions:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="test">

Test Process:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="component">

Component Releases:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="release">
<xsl:text>

</xsl:text>
<xsl:value-of select="@version"/> - <xsl:value-of select="@date"/>
<xsl:text>
</xsl:text>
<xsl:apply-templates select="log"/>
</xsl:template>

<xsl:template match="log">
* <xsl:apply-templates/>
</xsl:template>

<xsl:template match="faq">
Frequently Asked Questions:

<xsl:apply-templates/>
</xsl:template>

<xsl:template match="contact">

Contact Information:

Principal contact: <xsl:apply-templates select="primary"/>
Additional contact(s): <xsl:apply-templates select="additional"/>
</xsl:template>

<xsl:template match="primary">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="additional">
<xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
