<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:str="http://exslt.org/strings"
                version="1.0">
  <xsl:output method="text"/>
  <xsl:strip-space elements="*"/>

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

  <xsl:template name="escape">
    <xsl:param name="string"/>
    <xsl:param name="targets">_|#|$|%|^|&amp;</xsl:param>
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

<!-- ignore all elements that are not explicitly matched on -->
<xsl:template match="*"/>
<xsl:template match="*" mode="abstract"/>
<xsl:template match="*" mode="short"/>
<xsl:template match="*" mode="depend"/>

<!-- escape special characters in text and attribute nodes -->
<xsl:template match="text()">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template match="text()" mode="depend">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template match="literal//text()">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="releaseNotes">
<xsl:variable name="title">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="@title"/>
</xsl:call-template>
</xsl:variable>
\documentclass[letterpaper,twoside]{article}
\usepackage{core}
 
\geometry{verbose,letterpaper,tmargin=0.5in,bmargin=0.5in,lmargin=1in,rmargin=1in}
 
\raggedbottom
 
\parindent 0in

\begin{document}
\pagestyle{cdm}

\title{<xsl:value-of select="$title"/>\\Version <xsl:value-of select="//component/release[position()=1]/@version"/>}
\date{<xsl:value-of select="//component/release[position()=1]/@date"/>}
\maketitle

<xsl:apply-templates mode="abstract"/>
\tableofcontents

<xsl:apply-templates/>
\end{document}</xsl:template>

<xsl:template match="short" mode="abstract">
\begin{abstract}
<xsl:apply-templates/>
\end{abstract}
</xsl:template>

<xsl:template match="short" mode="short">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="description">
\section{Description}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="list">
\begin{itemize}
<xsl:apply-templates/>
\end{itemize}
</xsl:template>

<xsl:template match="item">
\item <xsl:apply-templates/>
</xsl:template>

<xsl:template name="createLink">
<xsl:param name="ref"/>
\href{<xsl:value-of select="$ref"/>}{<xsl:apply-templates/>}
</xsl:template>

<xsl:template match="link">
<xsl:call-template name="createLink">
<xsl:with-param name="ref" select="@ref"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="emphasis">
<xsl:choose>
<xsl:when test="@style='bold'">
\textbf{<xsl:apply-templates/>}
</xsl:when>
<xsl:when test="@style='italic'">
\textit{<xsl:apply-templates/>}
</xsl:when>
<xsl:when test="@style='underline'">
\underline{<xsl:apply-templates/>}
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="literal">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="requirements">
\section{Requirements}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="distribution">
<xsl:apply-templates select="//component" mode="depend"/>
\section{Distribution Contents}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="installation">
\section{Installation Instructions}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="usage">
\section{Usage Instructions}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="test">
\section{Test Process}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="component">
\section{Component Releases}
\begin{itemize}
<xsl:apply-templates select="release"/>
\end{itemize}
<xsl:apply-templates select="contact"/>
</xsl:template>

<xsl:template match="release">
\item <xsl:value-of select="@version"/> - <xsl:value-of select="@date"/>
\begin{itemize}
<xsl:apply-templates select="log"/>
\end{itemize}
</xsl:template>

<xsl:template match="log">
\item <xsl:apply-templates/>
</xsl:template>

<xsl:template match="component" mode="depend">
<xsl:apply-templates select="release[child::depend][position()=1]" mode="depend"/>
</xsl:template>

<xsl:template match="release" mode="depend">
<xsl:apply-templates mode="depend"/>
</xsl:template>

<xsl:template match="depend" mode="depend">
\section{Dependencies}
\begin{itemize}
<xsl:apply-templates mode="depend"/>
\end{itemize}
</xsl:template>

<xsl:template match="internal" mode="depend">
<xsl:variable name="name">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="@name"/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="xmldoc" select="concat(@name,'.xml')"/>
<xsl:variable name="pdfdoc" select="concat(@name,'.pdf')"/>
<xsl:choose>
<xsl:when test="@ref">
\item \href{<xsl:value-of select="@ref"/>}{<xsl:value-of select="$name"/>} - <xsl:apply-templates mode="depend"/>
</xsl:when>
<xsl:when test="document($xmldoc,.)">
\item \href{<xsl:value-of select="$pdfdoc"/>}{<xsl:value-of select="document($xmldoc,.)/releaseNotes/@title"/>} - <xsl:apply-templates select="document($xmldoc,.)//short" mode="short"/>
</xsl:when>
<xsl:otherwise>
\item \href{<xsl:value-of select="$pdfdoc"/>}{<xsl:value-of select="$name"/>}
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="external" mode="depend">
<xsl:variable name="name">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="@name"/>
</xsl:call-template>
</xsl:variable>
<xsl:choose>
<xsl:when test="@ref">
\item \href{<xsl:value-of select="@ref"/>}{<xsl:value-of select="$name"/>} - <xsl:apply-templates mode="depend"/>
</xsl:when>
<xsl:otherwise>
\item <xsl:value-of select="$name"/> - <xsl:apply-templates mode="depend"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="faq">
\section{Frequently Asked Questions}
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="contact">
\subsection{Contact Information}
Principal contact: <xsl:apply-templates select="primary"/>
Additional contact(s):
\begin{itemize}
<xsl:apply-templates select="additional"/>
\end{itemize}
</xsl:template>

<xsl:template match="primary">
<xsl:call-template name="createLink">
<xsl:with-param name="ref" select="@ref"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="additional">
\item <xsl:call-template name="createLink">
<xsl:with-param name="ref" select="@ref"/>
</xsl:call-template>
</xsl:template>

</xsl:stylesheet>
