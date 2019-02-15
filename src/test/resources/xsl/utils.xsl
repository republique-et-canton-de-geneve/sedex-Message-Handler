<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:exslt="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xalan"
                extension-element-prefixes="exslt">

<!--
  Dieses XSL Stylesheet enthält wiederverwendbare templates.
  
  $Id: utils.xsl 36 2008-08-07 16:19:05Z sasha $
  $Author: sasha $
  $Revision: 36 $
  $Date: 2008-08-07 18:19:05 +0200 (Do, 07 Aug 2008) $
  -->
    
  <!-- ========================================================= -->
  <!-- 
     Formattiert ein ISO 8601 Datum in eine lesbare Form. Ist das
     Datum = 1900-01-01*, so wird ein leerer Node zurückgegeben.
     Parameter:
     date - ISO 8601 Datum, also etwas wie 1961-10-12+01:00
     -->
  <xsl:template name="format-date">
    <xsl:param name="date" />
    <xsl:if test="substring($date,1,10) != '1900-01-01'">
	   <!-- Day -->
      <xsl:value-of select="substring($date, 9, 2)" />
	   <xsl:text>.</xsl:text>
      <!-- Month -->
      <xsl:value-of select="substring($date, 6, 2)" />
      <xsl:text>.</xsl:text>
      <!-- Year -->
      <xsl:value-of select="substring($date, 1, 4)" />
    </xsl:if>
  </xsl:template>



  <!-- ========================================================= -->
  <!-- 
    Formatiert eine Adresse als eine Postanschrift. Bsp:
    
    Fred Feuerstein, Geröllstrasse 7, Postfach 17, 3012 Bern
    
    Parameters:
    addr   - addresse node
    -->
  <xsl:template name="format-address-post">
    <xsl:param name="addr"/>

    <xsl:if test="$addr[./Land != 'Undefined']">
      <!-- Die Adresse ist definiert worden -->
    
      <xsl:value-of select="$addr/Empfaenger"/>
      <xsl:text>, </xsl:text>
      <xsl:value-of select="$addr/Strasse"/>
      <xsl:if test="string($addr/HausNummer)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="$addr/HausNummer"/>
      </xsl:if>

      <xsl:if test="string($addr/Postfach)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$addr/Postfach"/>
      </xsl:if>

      <xsl:text>, </xsl:text>
      <xsl:value-of select="$addr/PLZ"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="$addr/ort"/>

      <xsl:if test="string($addr[./Land != 'CHE'])">
        <xsl:text>, </xsl:text>
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="$addr/Land"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <!-- ========================================================= -->
  <!-- 
    Dieses Template liefert internationalisierte Strings zurück.
    Damit das funktioniert, muss das Hauptstylesheet zwei globale Variablen
    zur Verfügung stellen:
    
    - $lang
      Muss einen ISO 639 language code aus der Menge (de | fr | it) enthalten
      
    - $labels
      muss einen temporären Tree enthalten, welcher der folgenden DTD genügt: 
    
    <!DOCTYPE resources [
      <!ELEMENT resources (field+)>
      <!ELEMENT field (descriptor+, url)>
      <!ATTLIST field
      name #CDATA #REQUIRED
      >
      <!ELEMENT url (#PCDATA)>
      <!ELEMENT label (#PCDATA)>
      <!ELEMENT descriptor (label)>
      <!ATTLIST descriptor
      xml:lang (de | fr | it | en) #REQUIRED
      >
    ]>
			
			Das Stylesheet kann ein XML File mit der document Funktion einlesen: 
			
	    <xsl:variable name="labels" select="document('form_labels.xml')/resources"/>
	    
	  Das template kann dann wie folgt aufgerufen werden:
	  
			    <xsl:call-template name="getMsg">
			      <xsl:with-param name="key" select="'ja'"/>
			    </xsl:call-template>
  -->
  <xsl:template name="getMsg">
    <xsl:param name="key"/>
    
    <xsl:variable name="label" select="exslt:node-set($labels/field[@name = $key]/descriptor[@xml:lang = $lang]/label)"/>
    
    <xsl:choose>
      <xsl:when test="string($label)">
        <xsl:value-of select="normalize-space($label/text())"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="no">getMsg: key=<xsl:value-of select="$key"/> not found!</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
