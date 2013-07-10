<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:xs= "http://www.w3.org/2001/XMLSchema" 
                xmlns:xalan="http://xml.apache.org/xslt" 
                version="1.0" >
  <xsl:output method="html" indent="yes" xalan:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

  <!-- ignore all elements that are not explicitly matched on -->
  <xsl:template match="*"/>
  <xsl:template match="*" mode="title"/>
  <xsl:template match="*" mode="attr"/>
  <xsl:template match="*" mode="type"/>
  <xsl:template match="*" mode="choice"/>
  <xsl:template match="*" mode="any-attr"/>
  <xsl:template match="*" mode="any-elem"/>

  <xsl:template match="xs:schema">
    <html>
      <head>
        <title>XML Schema Documentation</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
        <meta http-equiv="Content-Style-Type" content="text/css"/>

        <style type="text/css">
          BODY { background-color: #CCCCCC }
          A:link { color: #333399 }
          A:visited { color: #333399 }
          A:active { color: black }
        </style>
      </head>
      <body>
        <hr/>
        <center>
          <h1>XML Schema Documentation</h1>
        </center>
        <xsl:apply-templates mode="title"/>
        <hr/>

        <xsl:apply-templates select="xs:element"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xs:documentation" mode="title">
    <center>
      <h2><xsl:apply-templates mode="title"/></h2>
    </center>
  </xsl:template>

  <xsl:template match="xs:annotation" mode="title">
    <xsl:apply-templates mode="title"/>
  </xsl:template>

  <xsl:template match="xs:element">
    <xsl:variable name="type" select="@type"/>
    <xsl:variable name="any-attrs">
      <xsl:apply-templates select="//xs:complexType[@name=$type]"
                           mode="any-attr"/>
    </xsl:variable>
    <xsl:variable name="any-reqds">
      <xsl:apply-templates select="//xs:complexType[@name=$type]"
                           mode="any-reqd"/>
    </xsl:variable>
    <xsl:variable name="any-elems">
      <xsl:apply-templates select="//xs:complexType[@name=$type]"
                           mode="any-elem"/>
    </xsl:variable>
    <xsl:element name="a">
      <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
    </xsl:element>
    <h2>Element: <xsl:value-of select="@name"/></h2>
    <xsl:apply-templates/>
    <xsl:if test="normalize-space($any-attrs)">
    Attributes<xsl:if test="normalize-space($any-reqds)"> (* required)</xsl:if>:
    <ul>
      <xsl:apply-templates select="//xs:complexType[@name=$type]" mode="attr"/>
    </ul>
    </xsl:if>
    <xsl:if test="normalize-space($any-elems)">
    Elements:
    <p>
      <xsl:apply-templates select="//xs:complexType[@name=$type]" mode="type"/>
    </p>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xs:complexType" mode="any-attr">
    <xsl:apply-templates mode="any-attr"/>
  </xsl:template>

  <xsl:template match="xs:complexContent" mode="any-attr">
    <xsl:apply-templates mode="any-attr"/>
  </xsl:template>

  <xsl:template match="xs:extension" mode="any-attr">
    <xsl:variable name="base" select="@base"/>
    <xsl:apply-templates select="//xs:complexType[@name=$base]"
                         mode="any-attr"/>
    <xsl:apply-templates mode="any-attr"/>
  </xsl:template>

  <xsl:template match="xs:attribute" mode="any-attr">
    <xsl:value-of select="@name"/>
  </xsl:template>

  <xsl:template match="xs:complexType" mode="any-reqd">
    <xsl:apply-templates mode="any-reqd"/>
  </xsl:template>

  <xsl:template match="xs:complexContent" mode="any-reqd">
    <xsl:apply-templates mode="any-reqd"/>
  </xsl:template>

  <xsl:template match="xs:extension" mode="any-reqd">
    <xsl:variable name="base" select="@base"/>
    <xsl:apply-templates select="//xs:complexType[@name=$base]"
                         mode="any-reqd"/>
    <xsl:apply-templates mode="any-reqd"/>
  </xsl:template>

  <xsl:template match="xs:attribute" mode="any-reqd">
    <xsl:if test="@use='required'">
      <xsl:value-of select="@name"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xs:complexType" mode="any-elem">
    <xsl:apply-templates mode="any-elem"/>
  </xsl:template>

  <xsl:template match="xs:complexContent" mode="any-elem">
    <xsl:apply-templates mode="any-elem"/>
  </xsl:template>

  <xsl:template match="xs:extension" mode="any-elem">
    <xsl:variable name="base" select="@base"/>
    <xsl:apply-templates select="//xs:complexType[@name=$base]"
                         mode="any-elem"/>
    <xsl:apply-templates mode="any-elem"/>
  </xsl:template>

  <xsl:template match="xs:sequence" mode="any-elem">
    <xsl:apply-templates mode="any-elem"/>
  </xsl:template>

  <xsl:template match="xs:choice" mode="any-elem">
    <xsl:apply-templates mode="any-elem"/>
  </xsl:template>

  <xsl:template match="xs:element[@ref]" mode="any-elem">
    <xsl:value-of select="@ref"/>
  </xsl:template>

  <xsl:template match="xs:complexType" mode="attr">
    <xsl:apply-templates mode="attr"/>
  </xsl:template>

  <xsl:template match="xs:complexContent" mode="attr">
    <xsl:apply-templates mode="attr"/>
  </xsl:template>

  <xsl:template match="xs:extension" mode="attr">
    <xsl:variable name="base" select="@base"/>
    <xsl:apply-templates select="//xs:complexType[@name=$base]" mode="attr"/>
    <xsl:apply-templates mode="attr"/>
  </xsl:template>

  <xsl:template match="xs:simpleType" mode="attr">
    <xsl:apply-templates mode="attr"/>
  </xsl:template>

  <xsl:template match="xs:restriction" mode="attr">
    <ul>
      <xsl:apply-templates mode="attr"/>
    </ul>
  </xsl:template>

  <xsl:template match="xs:enumeration" mode="attr">
    <li>
      <b><xsl:value-of select="@value"/></b>
      <xsl:text> - </xsl:text>
      <xsl:apply-templates mode="attr"/>
    </li>
  </xsl:template>

  <xsl:template match="xs:pattern" mode="attr">
    <li>
      <b><xsl:value-of select="@value"/></b>
      <xsl:text> - </xsl:text>
      <xsl:apply-templates mode="attr"/>
    </li>
  </xsl:template>

  <xsl:template match="xs:attribute" mode="attr">
    <xsl:variable name="type" select="@type"/>
    <li>
      <xsl:if test="@use='required'">
        <xsl:text>*</xsl:text>
      </xsl:if>
      <xsl:choose>
      <xsl:when test="//xs:simpleType[@name=$type]">
      <b><xsl:value-of select="@name"/></b>
      <xsl:text> - </xsl:text>
      <xsl:apply-templates mode="attr"/>
      <xsl:apply-templates select="//xs:simpleType[@name=$type]" mode="attr"/>
      </xsl:when>
      <xsl:otherwise>
      <b><xsl:value-of select="@name"/></b>[<xsl:value-of select="substring-after($type,':')"/>]
      <xsl:text> - </xsl:text>
      <xsl:apply-templates mode="attr"/>
      </xsl:otherwise>
      </xsl:choose>
    </li>
  </xsl:template>

  <xsl:template match="xs:documentation" mode="attr">
    <xsl:apply-templates mode="attr"/>
  </xsl:template>

  <xsl:template match="xs:annotation" mode="attr">
    <xsl:apply-templates mode="attr"/>
  </xsl:template>

  <xsl:template match="xs:complexType" mode="type">
    <xsl:apply-templates mode="type"/>
  </xsl:template>

  <xsl:template match="xs:complexContent" mode="type">
    <xsl:apply-templates mode="type"/>
  </xsl:template>

  <xsl:template match="xs:extension" mode="type">
    <xsl:variable name="base" select="@base"/>
    <xsl:apply-templates select="//xs:complexType[@name=$base]" mode="type"/>
    <xsl:apply-templates mode="type"/>
  </xsl:template>

  <xsl:template match="xs:sequence" mode="type">
    <ul>
    <xsl:apply-templates mode="type"/>
    </ul>
  </xsl:template>

  <xsl:template match="xs:choice" mode="type">
    <li>
      (<xsl:apply-templates mode="choice"/> )
      [<xsl:choose>
        <xsl:when test="@minOccurs"><xsl:value-of select="@minOccurs"/></xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>...
      <xsl:choose>
        <xsl:when test="@maxOccurs='unbounded'">*</xsl:when>
        <xsl:when test="@maxOccurs"><xsl:value-of select="@maxOccurs"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>]
    </li>
  </xsl:template>

  <xsl:template match="xs:element[@ref]" mode="type">
    <li>
    <xsl:element name="a">
      <xsl:attribute name="href">#<xsl:value-of select="@ref"/></xsl:attribute>
      <xsl:value-of select="@ref"/>
    </xsl:element>
    [<xsl:choose>
      <xsl:when test="@minOccurs"><xsl:value-of select="@minOccurs"/></xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>...
    <xsl:choose>
      <xsl:when test="@maxOccurs='unbounded'">*</xsl:when>
      <xsl:when test="@maxOccurs"><xsl:value-of select="@maxOccurs"/></xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>]
    </li>
  </xsl:template>

  <xsl:template match="xs:element[@ref]" mode="choice">
    <xsl:text> </xsl:text>
    <xsl:element name="a">
      <xsl:attribute name="href">#<xsl:value-of select="@ref"/></xsl:attribute>
      <xsl:value-of select="@ref"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="xs:documentation">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="xs:annotation">
    <xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>

