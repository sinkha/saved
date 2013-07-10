<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:str="http://exslt.org/strings"
                version="1.0" >
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

  <xsl:template name="replace-one-embedded">
    <xsl:param name="string"/>
    <xsl:param name="target"/>
    <xsl:param name="replace"/>
    <xsl:choose>
      <xsl:when test="contains($string,$target)">
        <xsl:variable name="after" 
          select="substring-after(normalize-space($string),$target)"/>
        <xsl:choose>
          <xsl:when test="starts-with($after,' ')">
            <xsl:value-of select="concat(substring-before($string,$target),$target)"/>
          </xsl:when>
          <xsl:when test="$after=''">
            <xsl:value-of select="concat(substring-before($string,$target),$target)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat(substring-before($string,$target),$replace)"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="replace-one-embedded">
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
    <xsl:param name="braces">[|]</xsl:param>
    <xsl:variable name="backslash">
      <xsl:call-template name="replace-one">
        <xsl:with-param name="string" select="$string"/>
        <xsl:with-param name="target">\</xsl:with-param>
        <xsl:with-param name="replace">\(\backslash\)</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="newstring">
      <xsl:call-template name="insert">
        <xsl:with-param name="string" select="$backslash"/>
        <xsl:with-param name="targets" select="$targets"/>
        <xsl:with-param name="insert">\.</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="insert">
      <xsl:with-param name="string" select="$newstring"/>
      <xsl:with-param name="targets" select="$braces"/>
      <xsl:with-param name="insert">{.}</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="hyphenate">
    <xsl:param name="string"/>
    <xsl:param name="targets">_|/</xsl:param>
    <xsl:variable name="newstring">
      <xsl:call-template name="replace-one-embedded">
        <xsl:with-param name="string" select="$string"/>
        <xsl:with-param name="target">.</xsl:with-param>
        <xsl:with-param name="replace">.\-</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="insert">
      <xsl:with-param name="string" select="$newstring"/>
      <xsl:with-param name="targets" select="$targets"/>
      <xsl:with-param name="insert">.\-</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- ignore all elements that are not explicitly matched on -->
  <xsl:template match="*"/>
  <xsl:template match="*" mode="title"/>
  <xsl:template match="*" mode="body"/>
  <xsl:template match="*" mode="description"/>
  <xsl:template match="*" mode="constraint"/>
  <xsl:template match="*" mode="choice"/>
  <xsl:template match="*" mode="key"/>

<!-- escape special characters in text and attribute nodes -->
<xsl:template match="text()">
<xsl:variable name="newstring">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template>
</xsl:variable>
<xsl:call-template name="hyphenate">
<xsl:with-param name="string"><xsl:value-of select="$newstring"/></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template match="propertyGroup">
\label{pg:<xsl:value-of select="@name"/>}
<xsl:apply-templates select="description" mode="description"/>

Properties denoted with an asterisk (*) are required.
\begin{itemize}
<xsl:apply-templates mode="body"/>
\end{itemize}
</xsl:template>

<xsl:template match="propertyGroup" mode="body">
\item <xsl:apply-templates select="key"/><xsl:apply-templates select="value"/>
<xsl:apply-templates select="description" mode="description"/>
<xsl:apply-templates select="key" mode="description"/>
<xsl:apply-templates select="value" mode="description"/>
\begin{itemize}
<xsl:apply-templates mode="body"/>
\end{itemize}
</xsl:template>

<xsl:template match="property" mode="body">
\item <xsl:if test="@use='required'"><xsl:text>*</xsl:text></xsl:if>
<xsl:apply-templates select="key"/><xsl:apply-templates select="value"/>
<xsl:apply-templates select="description" mode="description"/>
<xsl:apply-templates select="key" mode="description"/>
<xsl:apply-templates select="value" mode="description"/>
</xsl:template>

<xsl:template match="key">
<xsl:apply-templates mode="key"/></xsl:template>

<xsl:template match="value"> = <xsl:apply-templates/>\\
</xsl:template>

<xsl:template match="choice">
<xsl:apply-templates select="element[position()=1]"/>
<xsl:apply-templates select="element[position()>1]" mode="choice"/>
</xsl:template>

<xsl:template name="element">
<xsl:param name="ref"/>
<xsl:choose>
<xsl:when test="@occurs='unbounded'">{[&lt;<xsl:value-of select="$ref"/>&gt;,...]}</xsl:when>
<xsl:otherwise>&lt;<xsl:value-of select="$ref"/>&gt;</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="element[@ref]">
<xsl:call-template name="element">
<xsl:with-param name="ref" select="@ref"/>
</xsl:call-template>
<xsl:if test="text()">\textit{: <xsl:apply-templates select="text()"/>}</xsl:if>
</xsl:template>

<xsl:template match="element[@id]">
<xsl:call-template name="element">
<xsl:with-param name="ref" select="@id"/>
</xsl:call-template>
<xsl:if test="text()">\textit{: <xsl:apply-templates select="text()"/>}</xsl:if>
</xsl:template>

<xsl:template match="element">
<xsl:if test="text()">\textit{<xsl:apply-templates select="text()"/>}</xsl:if>
</xsl:template>

<xsl:template match="text()" mode="key">
<xsl:variable name="newstring">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template>
</xsl:variable>
<xsl:call-template name="hyphenate">
<xsl:with-param name="string"><xsl:value-of select="$newstring"/></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template match="element[@ref]" mode="key">
<xsl:call-template name="element">
<xsl:with-param name="ref" select="@ref"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="element[@id]" mode="key">
<xsl:call-template name="element">
<xsl:with-param name="ref" select="@id"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="element[@ref]" mode="choice">
 | <xsl:call-template name="element">
<xsl:with-param name="ref" select="@ref"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="element[@id]" mode="choice">
 | <xsl:call-template name="element">
<xsl:with-param name="ref" select="@id"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="element" mode="choice">
 | <xsl:if test="text()">\textit{<xsl:apply-templates select="text()"/>}</xsl:if>
</xsl:template>

<xsl:template match="text()" mode="description">
<xsl:variable name="newstring">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template>
</xsl:variable>
<xsl:call-template name="hyphenate">
<xsl:with-param name="string"><xsl:value-of select="$newstring"/></xsl:with-param>
</xsl:call-template><xsl:if test="position()=last()">\\</xsl:if>
</xsl:template>

<xsl:template match="description" mode="description">
<xsl:apply-templates mode="description"/>
</xsl:template>

<xsl:template match="key" mode="description">
<xsl:apply-templates select="element" mode="description"/>
</xsl:template>

<xsl:template match="value" mode="description">
<xsl:apply-templates mode="description"/>
</xsl:template>

<xsl:template match="choice" mode="description">
<xsl:apply-templates mode="description"/>
</xsl:template>

<xsl:template match="element[@id]" mode="description">
<xsl:variable name="type" select="@type"/>
&lt;<xsl:value-of select="@id"/>&gt; - <xsl:apply-templates select="description" mode="description"/>
<xsl:apply-templates select="//type[@id=$type]" mode="description"/><br/>
</xsl:template>

<xsl:template match="element[@ref]" mode="description">
<xsl:variable name="ref" select="@ref"/>
<xsl:if test="not(ancestor::property[position()=1]/parent::propertyGroup/child::node()/element[@ref=$ref])">
<xsl:apply-templates select="//element[@id=$ref]" mode="description"/>
</xsl:if>
</xsl:template>

<xsl:template match="type" mode="description">
<xsl:apply-templates mode="description"/>
</xsl:template>

<xsl:template match="constraint" mode="description">
\begin{itemize}
<xsl:apply-templates mode="constraint"/>
\end{itemize}
</xsl:template>

<xsl:template match="value" mode="constraint">
\item <xsl:apply-templates mode="constraint"/>
</xsl:template>

<xsl:template match="choice" mode="constraint">
<xsl:apply-templates select="element[position()=1]"/>
<xsl:apply-templates select="element[position()>1]" mode="choice"/>
</xsl:template>

<xsl:template match="list" mode="description">
\begin{itemize}
<xsl:apply-templates mode="description"/>
\end{itemize}
</xsl:template>

<xsl:template match="item" mode="description">
\item <xsl:apply-templates mode="description"/>
</xsl:template>

<xsl:template match="link" mode="description">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template><xsl:if test="position()=last()">\\</xsl:if>
</xsl:template>

<xsl:template match="emphasis" mode="description">
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
</xsl:stylesheet>

