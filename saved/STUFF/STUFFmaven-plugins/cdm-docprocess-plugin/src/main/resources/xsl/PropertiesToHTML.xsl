<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:xalan="http://xml.apache.org/xslt" 
                version="1.0" >
  <xsl:output method="html" indent="yes" xalan:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

  <!-- ignore all elements that are not explicitly matched on -->
  <xsl:template match="*"/>
  <xsl:template match="*" mode="title"/>
  <xsl:template match="*" mode="body"/>
  <xsl:template match="*" mode="description"/>
  <xsl:template match="*" mode="constraint"/>
  <xsl:template match="*" mode="choice"/>
  <xsl:template match="*" mode="key"/>

  <xsl:template match="propertyGroup">
    <html>
      <head>
        <title><xsl:value-of select="@name"/></title>
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
          <h1><xsl:value-of select="@name"/></h1>
          <xsl:apply-templates select="description"/>
        </center>
        <hr/>
        <ul>
          <xsl:apply-templates mode="body"/>
        </ul>
        <hr/>
        <p>* - Required properties</p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="description">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="propertyGroup" mode="body">
    <li>
    <xsl:apply-templates select="key"/>
    <xsl:apply-templates select="value"/>
    <p><xsl:apply-templates select="description" mode="description"/></p>
    <xsl:apply-templates select="key" mode="description"/>
    <xsl:apply-templates select="value" mode="description"/><p/>
    <ul>
      <xsl:apply-templates mode="body"/>
    </ul>
    </li>
  </xsl:template>

  <xsl:template match="property" mode="body">
    <li>
    <xsl:if test="@use='required'">
      <xsl:text>*</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="key"/>
    <xsl:apply-templates select="value"/>
    <p><xsl:apply-templates select="description" mode="description"/></p>
    <xsl:apply-templates select="key" mode="description"/>
    <xsl:apply-templates select="value" mode="description"/><p/>
    </li>
  </xsl:template>

  <xsl:template match="key">
    <xsl:apply-templates mode="key"/>
  </xsl:template>

  <xsl:template match="value">
    = <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="choice">
    <xsl:apply-templates select="element[position()=1]"/>
    <xsl:apply-templates select="element[position()>1]" mode="choice"/>
  </xsl:template>

  <xsl:template name="element">
    <xsl:param name="ref"/>
    <xsl:choose>
      <xsl:when test="@occurs='unbounded'">[&lt;<xsl:value-of select="$ref"/>&gt;,...]</xsl:when>
      <xsl:otherwise>&lt;<xsl:value-of select="$ref"/>&gt;</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="element[@ref]">
    <xsl:call-template name="element">
      <xsl:with-param name="ref" select="@ref"/>
    </xsl:call-template>
    <xsl:if test="text()">
      <em>: <xsl:value-of select="text()"/></em>
    </xsl:if>
  </xsl:template>

  <xsl:template match="element[@id]">
    <xsl:call-template name="element">
      <xsl:with-param name="ref" select="@id"/>
    </xsl:call-template>
    <xsl:if test="text()">
      <em>: <xsl:value-of select="text()"/></em>
    </xsl:if>
  </xsl:template>

  <xsl:template match="element">
    <xsl:if test="text()">
      <em><xsl:value-of select="text()"/></em>
    </xsl:if>
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
    | <xsl:if test="text()">
      <em><xsl:value-of select="text()"/></em>
    </xsl:if>
  </xsl:template>

  <xsl:template match="description" mode="description">
    <xsl:apply-templates/>
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
    <ul>
      <xsl:apply-templates mode="constraint"/>
    </ul>
  </xsl:template>

  <xsl:template match="value" mode="constraint">
    <li><xsl:apply-templates mode="constraint"/></li>
  </xsl:template>

  <xsl:template match="choice" mode="constraint">
    <xsl:apply-templates select="element[position()=1]"/>
    <xsl:apply-templates select="element[position()>1]" mode="choice"/>
  </xsl:template>

  <xsl:template match="list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="item">
    <li>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template name="createLink">
    <xsl:param name="ref"/>
    <xsl:element name="a">
      <xsl:attribute name="href"><xsl:value-of select="$ref"/></xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="emphasis">
    <xsl:choose>
      <xsl:when test="@style='bold'">
        <xsl:element name="b">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <xsl:when test="@style='italic'">
        <xsl:element name="i">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <xsl:when test="@style='underline'">
        <xsl:element name="u">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="link">
    <xsl:call-template name="createLink">
      <xsl:with-param name="ref" select="@ref"/>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>

