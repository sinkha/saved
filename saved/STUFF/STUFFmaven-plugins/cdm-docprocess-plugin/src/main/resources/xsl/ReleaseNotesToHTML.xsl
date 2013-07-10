<?xml version="1.0"?> 

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xslt" 
                version="1.0">
  <xsl:output method="html" indent="yes" xalan:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

  <!-- ignore all elements that are not explicitly matched on -->
  <xsl:template match="*"/>
  <xsl:template match="*" mode="short"/>
  <xsl:template match="*" mode="contents"/>
  <xsl:template match="*" mode="depend"/>
  <xsl:template match="*" mode="dependContents"/>

  <xsl:template match="releaseNotes">
    <html>
      <head>
        <title><xsl:value-of select="@title"/></title>
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
          <h1><xsl:value-of select="@title"/></h1>
          <h2>Version <xsl:value-of select="//component/release[position()=1]/@version"/></h2>
          <h2>Date <xsl:value-of select="//component/release[position()=1]/@date"/></h2>
        </center>

        <hr/>
        <xsl:apply-templates mode="short"/>

        <hr/>
        <ul>
          <xsl:apply-templates mode="contents"/>
        </ul>
        <hr/>

        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="description" mode="contents">
    <li><a href="#__description">Description</a></li>
  </xsl:template>
  <xsl:template match="requirements" mode="contents">
    <li><a href="#__requirements">Requirements</a></li>
  </xsl:template>
  <xsl:template match="distribution" mode="contents">
    <xsl:apply-templates select="//component" mode="dependContents"/>
    <li><a href="#__distribution">Distribution Contents</a></li>
  </xsl:template>
  <xsl:template match="installation" mode="contents">
    <li><a href="#__installation">Installation Instructions</a></li>
  </xsl:template>
  <xsl:template match="usage" mode="contents">
    <li><a href="#__usage">Usage Instructions</a></li>
  </xsl:template>
  <xsl:template match="test" mode="contents">
    <li><a href="#__test">Test Process</a></li>
  </xsl:template>
  <xsl:template match="component" mode="contents">
    <li><a href="#__releases">Component Releases</a></li>
    <xsl:apply-templates mode="contents"/>
  </xsl:template>
  <xsl:template match="faq" mode="contents">
    <li><a href="#__faq">Frequently Asked Questions</a></li>
  </xsl:template>
  <xsl:template match="contact" mode="contents">
    <li><a href="#__contact">Contact Information</a></li>
  </xsl:template>

  <xsl:template match="short" mode="short">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="component" mode="dependContents">
    <xsl:apply-templates select="release[child::depend][position()=1]" mode="dependContents"/>
  </xsl:template>

  <xsl:template match="release" mode="dependContents">
    <xsl:apply-templates mode="dependContents"/>
  </xsl:template>

  <xsl:template match="depend" mode="dependContents">
    <li><a href="#__depend">Dependencies</a></li>
  </xsl:template>

  <xsl:template match="component" mode="depend">
    <xsl:apply-templates select="release[child::depend][position()=1]" mode="depend"/>
  </xsl:template>

  <xsl:template match="release" mode="depend">
    <xsl:apply-templates mode="depend"/>
  </xsl:template>

  <xsl:template match="depend" mode="depend">
    <a name="__depend"/>
    <h2>Dependencies</h2>
    <ul>
      <xsl:apply-templates mode="depend"/>
    </ul>
  </xsl:template>

  <xsl:template match="internal" mode="depend">
    <xsl:variable name="xmldoc" select="concat(@name,'.xml')"/>
    <xsl:variable name="htmldoc" select="concat(@name,'.html')"/>
    <xsl:choose>
    <xsl:when test="@ref">
      <li>
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
          <xsl:value-of select="@name"/>
        </xsl:element>
        - <xsl:apply-templates mode="depend"/>
      </li>
    </xsl:when>
    <xsl:when test="document($xmldoc,.)">
      <li>
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="$xmldoc"/></xsl:attribute>
          <xsl:value-of select="document($xmldoc,.)/releaseNotes/@title"/>
        </xsl:element> - <xsl:apply-templates select="document($xmldoc,.)//short" mode="short"/>
      </li>
    </xsl:when>
    <xsl:otherwise>
      <li>
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="$htmldoc"/></xsl:attribute>
          <xsl:value-of select="@name"/>
        </xsl:element>
        - <xsl:apply-templates mode="depend"/>
      </li>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="copyright" mode="depend">
    <br/>
    <small>
    <xsl:choose>
    <xsl:when test="@ref">
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
        Copyright</xsl:element>
      <xsl:text> </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      Copyright
    </xsl:otherwise>
    </xsl:choose>
    &#xa9; <xsl:apply-templates mode="depend"/>
    </small>
  </xsl:template>

  <xsl:template match="license" mode="depend">
    <br/>
    <small>
    <xsl:choose>
    <xsl:when test="@ref">
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
        <xsl:apply-templates mode="depend"/></xsl:element>
      <xsl:text> </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates mode="depend"/>
    </xsl:otherwise>
    </xsl:choose>
    </small>
  </xsl:template>

  <xsl:template match="external" mode="depend">
    <xsl:choose>
    <xsl:when test="@ref">
      <li>
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
          <xsl:value-of select="@name"/>
        </xsl:element>
        - <xsl:apply-templates mode="depend"/>
      </li>
    </xsl:when>
    <xsl:otherwise>
      <li>
        <xsl:value-of select="@name"/>
        - <xsl:apply-templates mode="depend"/>
      </li>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="id">
    <xsl:if test="@id">
      <xsl:element name="a">
        <xsl:attribute name="name"><xsl:value-of select="@id"/></xsl:attribute>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="description">
    <a name="__description"/>
    <xsl:call-template name="id"/>
    <h2>Description</h2>
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="item">
    <xsl:call-template name="id"/>
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

  <xsl:template match="literal">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="requirements">
    <a name="__requirements"/>
    <xsl:call-template name="id"/>
    <h2>Requirements</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="distribution">
    <xsl:apply-templates select="//component" mode="depend"/>
    <a name="__distribution"/>
    <xsl:call-template name="id"/>
    <h2>Distribution Contents</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="installation">
    <a name="__installation"/>
    <xsl:call-template name="id"/>
    <h2>Installation Instructions</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="usage">
    <a name="__usage"/>
    <xsl:call-template name="id"/>
    <h2>Usage Instructions</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="test">
    <a name="__test"/>
    <xsl:call-template name="id"/>
    <h2>Test Process</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="component">
    <a name="__releases"/>
    <xsl:call-template name="id"/>
    <h2>Component Releases</h2>
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="release">
    <li>
      <xsl:value-of select="@version"/> - <xsl:value-of select="@date"/>
      <ul>
        <xsl:apply-templates select="log"/>
      </ul>
    </li>
  </xsl:template>

  <xsl:template match="log">
    <li>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template match="faq">
    <a name="__faq"/>
    <xsl:call-template name="id"/>
    <h2>Frequently Asked Questions</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="contact">
    <a name="__contact"/>
    <h3>Contact Information</h3>
    Principal contact:
    <xsl:apply-templates select="primary"/>
    <br/>
    Additional contact(s):
    <ul>
      <xsl:apply-templates select="additional"/>
    </ul>
  </xsl:template>

  <xsl:template match="primary">
    <xsl:call-template name="createLink">
      <xsl:with-param name="ref" select="@ref"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="additional">
    <li>
      <xsl:call-template name="createLink">
        <xsl:with-param name="ref" select="@ref"/>
      </xsl:call-template>
    </li>
  </xsl:template>

</xsl:stylesheet>
