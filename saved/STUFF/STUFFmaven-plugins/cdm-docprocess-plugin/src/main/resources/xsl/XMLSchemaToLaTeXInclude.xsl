<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:xs= "http://www.w3.org/2001/XMLSchema" 
                xmlns:str="http://exslt.org/strings"
                version="1.0" >
  <xsl:output method="text"/>
  <xsl:strip-space elements="*"/>

  <xsl:param name="rootname" select="xs:schema/xs:element[1]/@name"/>

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
<xsl:template match="*" mode="attr"/>
<xsl:template match="*" mode="addattr"/>
<xsl:template match="*" mode="elem"/>
<xsl:template match="*" mode="addelem"/>
<xsl:template match="*" mode="choice"/>

<!-- escape special characters in text and attribute nodes -->
<xsl:template match="text()">
<xsl:call-template name="escape">
<xsl:with-param name="string"><xsl:value-of select="."/></xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template match="xs:schema">
<xsl:apply-templates select="xs:element"/>
</xsl:template>

<xsl:template match="xs:element">
<xsl:variable name="type" select="@type"/>
\subsection{<xsl:value-of select="@name"/>}
\label{sub:<xsl:value-of select="$rootname"/>_<xsl:value-of select="@name"/>}
<xsl:apply-templates select="xs:annotation"/>
<xsl:choose>
<xsl:when test="//xs:complexType[@name=$type]">
<xsl:apply-templates select="//xs:complexType[@name=$type]" mode="attr"/>
<xsl:apply-templates select="//xs:complexType[@name=$type]" mode="elem"/>
</xsl:when>
<xsl:when test="xs:complexType">
<xsl:apply-templates select="xs:complexType" mode="attr"/>
<xsl:apply-templates select="xs:complexType" mode="elem"/>
</xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="xs:complexType" mode="attr">
<xsl:choose>
<xsl:when test="xs:attribute">
\subsubsection*{Attributes}
\begin{itemize}
<xsl:apply-templates select="xs:complexContent" mode="addattr"/>
<xsl:apply-templates select="xs:attribute" mode="attr"/>
\end{itemize}
* indicates required attribute.
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates mode="attr"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="xs:complexContent" mode="attr">
<xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:extension" mode="attr">
<xsl:variable name="base" select="@base"/>
<xsl:choose>
<xsl:when test="xs:attribute">
\subsubsection*{Attributes}
\begin{itemize}
<xsl:apply-templates select="//xs:complexType[@name=$base]" mode="addattr"/>
<xsl:apply-templates select="xs:attribute" mode="attr"/>
\end{itemize}
* indicates required attribute.
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates select="//xs:complexType[@name=$base]" mode="attr"/>
<xsl:apply-templates mode="attr"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="xs:simpleType" mode="attr">
<xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:restriction" mode="attr">
\begin{itemize}
<xsl:apply-templates mode="attr"/>
\end{itemize}
</xsl:template>

<xsl:template match="xs:enumeration" mode="attr">
\item \textbf{<xsl:value-of select="@value"/>} - <xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:pattern" mode="attr">
<xsl:variable name="value">
<xsl:call-template name="escape">
<xsl:with-param name="string" select="@value"/>
</xsl:call-template>
</xsl:variable>
\item \textbf{<xsl:value-of select="$value"/>} - <xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:attribute" mode="attr">
<xsl:variable name="type" select="@type"/>
\item <xsl:if test="@use='required'">*</xsl:if><xsl:choose>
<xsl:when test="//xs:simpleType[@name=$type]">
\textbf{<xsl:value-of select="@name"/>} - <xsl:apply-templates mode="attr"/>
<xsl:apply-templates select="//xs:simpleType[@name=$type]" mode="attr"/>
</xsl:when>
<xsl:otherwise>
\textbf{<xsl:value-of select="@name"/>}[<xsl:value-of select="substring-after($type,':')"/>] - <xsl:apply-templates mode="attr"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="xs:documentation" mode="attr">
<xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:annotation" mode="attr">
<xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:complexType" mode="addattr">
<xsl:apply-templates select="xs:complexContent" mode="addattr"/>
<xsl:apply-templates select="xs:attribute" mode="attr"/>
</xsl:template>

<xsl:template match="xs:complexContent" mode="addattr">
<xsl:apply-templates mode="addattr"/>
</xsl:template>

<xsl:template match="xs:extension" mode="addattr">
<xsl:variable name="base" select="@base"/>
<xsl:apply-templates select="//xs:complexType[@name=$base]" mode="addattr"/>
<xsl:apply-templates mode="attr"/>
</xsl:template>

<xsl:template match="xs:complexType" mode="elem">
<xsl:choose>
<xsl:when test="xs:sequence or xs:choice">
\subsubsection*{Elements}
\begin{itemize}
<xsl:apply-templates select="xs:complexContent" mode="addelem"/>
<xsl:apply-templates select="xs:sequence" mode="elem"/>
<xsl:apply-templates select="xs:choice" mode="elem"/>
\end{itemize}
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates mode="elem"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="xs:complexContent" mode="elem">
<xsl:apply-templates mode="elem"/>
</xsl:template>

<xsl:template match="xs:extension" mode="elem">
<xsl:variable name="base" select="@base"/>
<xsl:choose>
<xsl:when test="xs:sequence or xs:choice">
\subsubsection*{Elements}
\begin{itemize}
<xsl:apply-templates select="//xs:complexType[@name=$base]" mode="addelem"/>
<xsl:apply-templates select="xs:sequence" mode="elem"/>
<xsl:apply-templates select="xs:choice" mode="elem"/>
\end{itemize}
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates select="//xs:complexType[@name=$base]" mode="elem"/>
<xsl:apply-templates mode="elem"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="xs:sequence" mode="elem">
<xsl:apply-templates mode="elem"/>
</xsl:template>

<xsl:template match="xs:choice" mode="elem">
\item (<xsl:apply-templates mode="choice"/> ) [<xsl:choose>
<xsl:when test="@minOccurs"><xsl:value-of select="@minOccurs"/></xsl:when>
<xsl:otherwise>0</xsl:otherwise>
</xsl:choose>...<xsl:choose>
<xsl:when test="@maxOccurs='unbounded'">*</xsl:when>
<xsl:when test="@maxOccurs"><xsl:value-of select="@maxOccurs"/></xsl:when>
<xsl:otherwise>1</xsl:otherwise>
</xsl:choose>]
</xsl:template>

<xsl:template match="xs:element[@ref]" mode="elem">
\item <xsl:value-of select="@ref"/> \ref{sub:<xsl:value-of select="$rootname"/>_<xsl:value-of select="@ref"/>} [<xsl:choose>
<xsl:when test="@minOccurs"><xsl:value-of select="@minOccurs"/></xsl:when>
<xsl:otherwise>0</xsl:otherwise>
</xsl:choose>...<xsl:choose>
<xsl:when test="@maxOccurs='unbounded'">*</xsl:when>
<xsl:when test="@maxOccurs"><xsl:value-of select="@maxOccurs"/></xsl:when>
<xsl:otherwise>1</xsl:otherwise>
</xsl:choose>]
</xsl:template>

<xsl:template match="xs:element[@ref]" mode="choice">
 <xsl:value-of select="@ref"/> \ref{sub:<xsl:value-of select="$rootname"/>_<xsl:value-of select="@ref"/>}
</xsl:template>

<xsl:template match="xs:sequence" mode="choice">
 (<xsl:apply-templates mode="choice"/> )
</xsl:template>

<xsl:template match="xs:complexType" mode="addelem">
<xsl:apply-templates select="xs:complexContent" mode="addelem"/>
<xsl:apply-templates select="xs:sequence" mode="elem"/>
</xsl:template>

<xsl:template match="xs:complexContent" mode="addelem">
<xsl:apply-templates mode="addelem"/>
</xsl:template>

<xsl:template match="xs:extension" mode="addelem">
<xsl:variable name="base" select="@base"/>
<xsl:apply-templates select="//xs:complexType[@name=$base]" mode="addelem"/>
<xsl:apply-templates mode="elem"/>
</xsl:template>

<xsl:template match="xs:documentation">
\item <xsl:apply-templates/>
</xsl:template>

<xsl:template match="xs:annotation">
\subsubsection*{Description}
\begin{itemize}
<xsl:apply-templates/>
\end{itemize}
</xsl:template>
</xsl:stylesheet>

