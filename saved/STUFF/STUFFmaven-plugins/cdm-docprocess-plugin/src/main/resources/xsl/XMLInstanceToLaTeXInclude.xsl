<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:xs= "http://www.w3.org/2001/XMLSchema" 
                xmlns:str="http://exslt.org/strings"
                version="1.0" >
  <xsl:output method="text"/>
<!--
  <xsl:strip-space elements="*"/>
-->
  <xsl:preserve-space elements="*"/>

  <xsl:template name="replace-one">
    <xsl:param name="string"/>
    <xsl:param name="target"/>
    <xsl:param name="replace"/>
    <xsl:choose>
      <xsl:when test="contains($string,$target)">
        <xsl:value-of select="concat(substring-before($string,$target),$replace)"/>
        <xsl:call-template name="replace-one">
          <xsl:with-param name="string" select="substring-after($string,$target)"/>
          <xsl:with-param name="target" select="$target"/>
          <xsl:with-param name="replace" select="$replace"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="remove-one">
    <xsl:param name="string"/>
    <xsl:param name="target"/>
    <xsl:choose>
      <xsl:when test="contains($string,$target)">
        <xsl:value-of select="substring-before($string,$target)"/>
        <xsl:call-template name="remove-one">
          <xsl:with-param name="string" select="substring-after($string,$target)"/>
          <xsl:with-param name="target" select="$target"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="insert-one">
    <xsl:param name="string"/>
    <xsl:param name="target"/>
    <xsl:param name="insert"/>
    <xsl:variable name="replace">
      <xsl:call-template name="replace-one">
        <xsl:with-param name="string" select="$insert"/>
        <xsl:with-param name="target">.</xsl:with-param>
        <xsl:with-param name="replace" select="$target"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="replace-one">
      <xsl:with-param name="string" select="$string"/>
      <xsl:with-param name="target" select="$target"/>
      <xsl:with-param name="replace" select="$replace"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="insert">
    <xsl:param name="string"/>
    <xsl:param name="targets"/>
    <xsl:param name="insert"/>
    <xsl:choose>
      <xsl:when test="contains($targets,'|')">
        <xsl:variable name="target" select="substring-before($targets,'|')"/>
        <xsl:variable name="newstring">
          <xsl:call-template name="insert-one">
            <xsl:with-param name="string" select="$string"/>
            <xsl:with-param name="target" select="$target"/>
            <xsl:with-param name="insert" select="$insert"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:call-template name="insert">
          <xsl:with-param name="string" select="$newstring"/>
          <xsl:with-param name="targets" select="substring-after($targets,'|')"/>
          <xsl:with-param name="insert" select="$insert"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="insert-one">
          <xsl:with-param name="string" select="$string"/>
          <xsl:with-param name="target" select="$targets"/>
          <xsl:with-param name="insert" select="$insert"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="remove">
    <xsl:param name="string"/>
    <xsl:param name="targets"/>
    <xsl:choose>
      <xsl:when test="contains($targets,'|')">
        <xsl:variable name="target" select="substring-before($targets,'|')"/>
        <xsl:variable name="newstring">
          <xsl:call-template name="remove-one">
            <xsl:with-param name="string" select="$string"/>
            <xsl:with-param name="target" select="$target"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:call-template name="remove">
          <xsl:with-param name="string" select="$newstring"/>
          <xsl:with-param name="targets" select="substring-after($targets,'|')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="remove-one">
          <xsl:with-param name="string" select="$string"/>
          <xsl:with-param name="target" select="$targets"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="escape">
    <xsl:param name="string"/>
    <xsl:param name="targets">_|#|$|%|^|{|}|&amp;</xsl:param>
    <xsl:variable name="newstring">
      <xsl:call-template name="replace-one">
        <xsl:with-param name="string" select="$string"/>
        <xsl:with-param name="target">\</xsl:with-param>
        <xsl:with-param name="replace">\(\backslash\)</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="insert">
      <xsl:with-param name="string" select="$newstring"/>
      <xsl:with-param name="targets" select="$targets"/>
      <xsl:with-param name="insert">\.</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

<xsl:template match="/">\begin{alltt}<xsl:apply-templates/>\end{alltt}
</xsl:template>

<xsl:template match="text()[normalize-space(.)='']">
<xsl:variable name="newstring">
<xsl:call-template name="remove">
<xsl:with-param name="string" select="."/>
<xsl:with-param name="targets">&#xA;|&#xD;</xsl:with-param>
</xsl:call-template>
</xsl:variable>
<xsl:if test="not($newstring='')">
<xsl:text>
</xsl:text>
</xsl:if>
<xsl:value-of select="$newstring"/>
</xsl:template>

<!-- escape special characters in text and comment nodes -->
<xsl:template match="text()">
<xsl:variable name="newstring">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:call-template name="insert">
<xsl:with-param name="string" select="$newstring"/>
<xsl:with-param name="targets">/</xsl:with-param>
<xsl:with-param name="insert">.\-</xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template match="comment()">
<xsl:variable name="newstring">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="."/>
</xsl:call-template>
</xsl:variable>\end{alltt}
<xsl:call-template name="insert">
<xsl:with-param name="string" select="$newstring"/>
<xsl:with-param name="targets">/</xsl:with-param>
<xsl:with-param name="insert">.\-</xsl:with-param>
</xsl:call-template>
\begin{alltt}</xsl:template>

<xsl:template match="/*">
\textbf{&lt;<xsl:value-of select="name(.)"/><xsl:apply-templates select="@*"/>&gt;}<xsl:apply-templates/>
\textbf{&lt;/<xsl:value-of select="name(.)" />&gt;}
</xsl:template>

<xsl:template match="*">
<xsl:choose>
<xsl:when test="not(node())">\textbf{&lt;<xsl:value-of select="name(.)"/><xsl:apply-templates select="@*"/>/&gt;}</xsl:when>
<xsl:otherwise>\textbf{&lt;<xsl:value-of select="name(.)"/><xsl:apply-templates select="@*"/>&gt;}<xsl:apply-templates/>\textbf{&lt;/<xsl:value-of select="name(.)" />&gt;}</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="@*">
<xsl:variable name="value">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:text> </xsl:text><xsl:value-of select="name(.)" />="<xsl:value-of select="$value"/>"</xsl:template>

</xsl:stylesheet>

