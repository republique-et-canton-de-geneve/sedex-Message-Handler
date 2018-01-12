<?xml version="1.0" encoding='iso-8859-1'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pd="http://eschkg.bj.admin.ch/formular/1_1"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:exslt="http://exslt.org/common"
  xmlns:java="http://xml.apache.org/xalan/java" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:str="http://www.ora.com/XSLTCookbook/namespaces/strings"
  exclude-result-prefixes="java xalan" extension-element-prefixes="str exslt java">
  <!--
  Dieses XSL Stylesheet erstellt ein Betreibungsbegehren.

  $Id: formular.xsl 36 2008-08-07 16:19:05Z sasha $
  $Author: sasha $
  $Revision: 36 $
  $Date: 2008-08-07 18:19:05 +0200 (Do, 07 Aug 2008) $

  Von Hand kann das ausgeführt werden wie folgt:

    cd /cygdrive/c/workspace/suis-eschkg/src/xsl
    cp ../metadata/sample-prosectiondemand-1.1.xml .
    fop -xsl formular.xsl -xml sample-prosectiondemand-1.1.xml -pdf gaga.pdf

    Die Labels für dieses Formular müssen wie folgt bereitgestellt werden:

    wget http://calvin.glue.ch:25087/eschkg/form_labels/formular/getAllFormResources -O form_labels.xml
  -->

  <xsl:output method="xml"/>

  <!-- Top level parameters, which need to be passed by the application -->
  <xsl:param name="lang">de</xsl:param>

  <xsl:include href="utils.xsl"/>

  <!-- Internationalisierte Labels einlesen und zur Verfügung stellen -->
  <xsl:variable name="labels" select="document('form_labels.xml')/resources"/>


  <!-- Top level parameter defining the font stuff -->
  <xsl:param name="title-font-size">11pt</xsl:param>
  <xsl:param name="text-font-size">10pt</xsl:param>
  <xsl:param name="header-font-size">7.5pt</xsl:param>
  <xsl:param name="font-family">sans-serif</xsl:param>

  <xsl:attribute-set name="font-normal">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$font-family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$text-font-size"/>
    </xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="font-bold">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$font-family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$text-font-size"/>
    </xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="font-header">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$font-family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$header-font-size"/>
    </xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="title-font-attrs">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$font-family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$title-font-size"/>
    </xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="table.style">
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
    <xsl:attribute name="inline-progression-dimension">17.5cm</xsl:attribute>
    <xsl:attribute name="border-style">none</xsl:attribute>
    <!--xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">0.2pt</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute-->
  </xsl:attribute-set>

  <xsl:attribute-set name="cell.style">
    <xsl:attribute name="border-style">none</xsl:attribute>
    <xsl:attribute name="padding">0.5em</xsl:attribute>
    <!--xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">0.2pt</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute-->
  </xsl:attribute-set>


  <!-- ========================================================= -->
  <!--
    Verarbeite das gesamte Dokument, um daraus das korrekte Bestellformular
    zu erzeugen.
    -->
  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

      <fo:layout-master-set>
        <!-- layout information -->
        <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm"
          margin-top="0.5cm" margin-bottom="0.5cm" margin-left="1.2cm" margin-right="1.5cm">
          <fo:region-body margin-top="1.5cm" margin-bottom="1.5cm"/>
          <fo:region-before extent="1.5cm"/>
          <fo:region-after extent="1.5cm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <!-- end: defines page layout -->

      <!-- Page sequence für das Betreibungsbegehren -->
      <fo:page-sequence master-reference="A4" initial-page-number="1">

        <fo:flow flow-name="xsl-region-body">

          <fo:table xsl:use-attribute-sets="table.style">
            <fo:table-column column-number="1" column-width="proportional-column-width(50)"/>
            <fo:table-column column-number="2" column-width="proportional-column-width(50)"/>
            <fo:table-body>
              <xsl:call-template name="prod-vorspann"/>
              <xsl:call-template name="prod-schuldner"/>
              <xsl:call-template name="prod-glaeubiger"/>
              <xsl:call-template name="prod-post-bank"/>
              <xsl:call-template name="prod-forderungen"/>
              <xsl:call-template name="prod-bemerkungen"/>
              <xsl:call-template name="prod-unterschrift"/>
              <xsl:call-template name="prod-abspann"/>
            </fo:table-body>
          </fo:table>

        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>


  <xsl:attribute-set name="table.style.address">
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
    <xsl:attribute name="width">100%</xsl:attribute>
    <xsl:attribute name="border-style">none</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cell.style.address">
    <xsl:attribute name="border-style">none</xsl:attribute>
    <xsl:attribute name="padding">0.5em</xsl:attribute>
  </xsl:attribute-set>

  <!-- ========================================================= -->
  <xsl:template name="prod-vorspann">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" number-columns-spanned="2">
        <!-- Ueberschrift: Betreibungsbegehren ... -->
        <fo:block space-before.optimum="1em" text-align-last="justify">
          <fo:inline xsl:use-attribute-sets="title-font-attrs">
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'formular_titel'"/>
            </xsl:call-template>
          </fo:inline>
          <fo:leader leader-pattern="space"/>
          <fo:inline xsl:use-attribute-sets="font-normal">
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'formular_quellsystem'"/>
            </xsl:call-template>
          </fo:inline>
        </fo:block>

        <!-- Vermerke: Betreibungsnr. und Eingang am -->
        <fo:block space-before.optimum="1em" start-indent="11cm">
          <fo:block xsl:use-attribute-sets="font-normal">
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'formular_betreibung_nr'"/>
            </xsl:call-template>
          </fo:block>
          <fo:block xsl:use-attribute-sets="font-normal">
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'formular_eingang_am'"/>
            </xsl:call-template>
          </fo:block>
        </fo:block>

        <!-- Adresse des Betreibungsamtes für ein Fenstercouvert platzieren -->
        <fo:block space-before.optimum="1.5cm"/>
        <fo:table xsl:use-attribute-sets="table.style.address">
          <fo:table-column column-number="1" column-width="proportional-column-width(50)"/>
          <fo:table-column column-number="2" column-width="proportional-column-width(50)"/>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell xsl:use-attribute-sets="cell.style.address">
                <fo:block  start-indent="1cm">
                  <xsl:apply-templates select="/pd:prosecutionDemand/pd:office"/>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell xsl:use-attribute-sets="cell.style.address">
                <fo:block start-indent="2cm">
                  <xsl:apply-templates select="/pd:prosecutionDemand/pd:office"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
        <fo:block space-before.optimum="2.0cm"/>

      </fo:table-cell>
    </fo:table-row>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:office">
    <xsl:variable name="office" select="."/>
    <fo:block text-align="left" xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="$office/pd:name"/>
    </fo:block>
    <xsl:if test="string($office/pd:nameAppendix)">
      <fo:block text-align="left" xsl:use-attribute-sets="font-normal">
        <xsl:value-of select="$office/pd:nameAppendix"/>
      </fo:block>
    </xsl:if>
    <xsl:apply-templates select="$office/pd:address" mode="amt"/>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-schuldner">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style">
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_schuldner'"/>
          </xsl:call-template>
        </fo:block>
        <xsl:apply-templates select="(/pd:prosecutionDemand/pd:debtorInd|/pd:prosecutionDemand/pd:debtorLeg)">
          <xsl:with-param name="showPostfach" select="'false'"/>
        </xsl:apply-templates>
      </fo:table-cell>

      <fo:table-cell xsl:use-attribute-sets="cell.style" >
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_mitbetriebene_person'"/>
          </xsl:call-template>
        </fo:block>
        <xsl:apply-templates select="(/pd:prosecutionDemand/pd:affectedIndividual)">
          <xsl:with-param name="showPostfach" select="'false'"/>
        </xsl:apply-templates>
      </fo:table-cell>

    </fo:table-row>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-glaeubiger">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" >
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_glaeubiger'"/>
          </xsl:call-template>
        </fo:block>
        <xsl:apply-templates select="(/pd:prosecutionDemand/pd:creditorInd|/pd:prosecutionDemand/pd:creditorLeg)"/>
      </fo:table-cell>
      <fo:table-cell xsl:use-attribute-sets="cell.style" >
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_glaeubiger_vertreter'"/>
          </xsl:call-template>
        </fo:block>
        <xsl:apply-templates select="(/pd:prosecutionDemand/pd:procurationInd|/pd:prosecutionDemand/pd:procurationLeg)"/>
      </fo:table-cell>
    </fo:table-row>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:creditorInd|pd:procurationInd|pd:debtorInd|pd:affectedIndividual">
    <xsl:param name="showPostfach" />
    <!-- Anrede, Name Vornamen -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <!-- Anrede, -->
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key">
          <xsl:value-of select="concat('formular_anrede_',translate(./pd:salutation,'MRS','mrs'))"/>
        </xsl:with-param>
      </xsl:call-template>

      <!-- Name Vornamen -->
      <xsl:if test="string(pd:surname)">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <xsl:value-of select="./pd:surname"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:givenName"/>
    </fo:block>

    <!-- Adresse -->
    <xsl:apply-templates select="./pd:address" mode="personen">
      <xsl:with-param name="showPostfach" select="$showPostfach"/>
    </xsl:apply-templates>

    <!-- Geburtsdatum: <Geburtsdatum> -->
    <!-- geb. Geburtsdatum darf nicht default datum sein -->
    <xsl:if test="string(pd:dateOfBirth) and string(pd:dateOfBirth) != '1900-01-01'">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'formular_geburtsdatum'"/>
        </xsl:call-template>
       <xsl:text>: </xsl:text>
       <xsl:call-template name="format-date">
         <xsl:with-param name="date" select="./pd:dateOfBirth"/>
       </xsl:call-template>
      </fo:block>
    </xsl:if>

    <!-- Kontakt: <TelNr>, <Email> -->
    <xsl:if test="string(pd:telephone1) or string(pd:email)">
      <fo:block xsl:use-attribute-sets="font-normal">
        <!-- (Kontakt: -->
        <xsl:text>(</xsl:text>
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'formular_kontakt'"/>
        </xsl:call-template>
        <xsl:text>: </xsl:text>

        <!-- TelNr -->
        <xsl:if test="string(pd:telephone1)">
          <xsl:value-of select="./pd:telephone1"/>
        </xsl:if>

        <!-- , Email) -->
        <xsl:if test="string(pd:email)">
          <xsl:if test="string(pd:telephone1)">
            <xsl:text>, </xsl:text>
          </xsl:if>
          <xsl:value-of select="./pd:email"/>
        </xsl:if>
        <xsl:text>)</xsl:text>
      </fo:block>
    </xsl:if>
  </xsl:template>

  <!-- ========================================================= -->
  <!-- Formatierung der Adressen der Personen -->
  <xsl:template match="pd:address" mode="personen">
  <xsl:param name="showPostfach" />
    <!-- Strasse Nr, Addr-Zusatz -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="pd:street"/>
      <xsl:if test="string(pd:number)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="pd:number"/>
      </xsl:if>
      <xsl:if test="string(pd:auxiliaryAddress)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="pd:auxiliaryAddress"/>
      </xsl:if>
    </fo:block>

    <xsl:if test="string($showPostfach) != 'false'">
      <!-- Postfach <Postfach> -->
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:if test="string(pd:pob)">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_postfach'"/>
          </xsl:call-template>
          <xsl:text> </xsl:text>
          <xsl:value-of select="pd:pob"/>
        </xsl:if>
      </fo:block>
    </xsl:if>

    <!-- PLZ Ort -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="pd:postCode"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="pd:location"/>
    </fo:block>

  </xsl:template>

  <!-- ========================================================= -->
  <!-- Formatierung der Adressen des Betreibungsamtes -->

  <xsl:template match="pd:address" mode="amt">
    <!-- Strasse Nr, Addr-Zusatz, Postfach -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="pd:street"/>
      <xsl:if test="string(pd:number)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="pd:number"/>
      </xsl:if>
    </fo:block>

    <xsl:if test="string(pd:auxiliaryAddress)">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:value-of select="pd:auxiliaryAddress"/>
      </fo:block>
    </xsl:if>

    <xsl:if test="string(pd:pob)">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:value-of select="pd:pob"/>
      </fo:block>
    </xsl:if>

    <!-- PLZ Ort -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="pd:postCode"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="pd:location"/>
    </fo:block>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:creditorLeg|pd:procurationLeg|pd:debtorLeg">
    <xsl:param name="showPostfach" />
    <fo:block xsl:use-attribute-sets="font-normal">
      <!-- ... Firmenname -->
      <xsl:value-of select="./pd:businessName"/>
    </fo:block>

    <!-- Strasse Nr, Addr-Zusatz -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="./pd:address/pd:street"/>
      <xsl:if test="string(pd:address/pd:number)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="./pd:address/pd:number"/>
      </xsl:if>
      <xsl:if test="string(pd:address/pd:auxiliaryAddress)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="./pd:address/pd:auxiliaryAddress"/>
      </xsl:if>
    </fo:block>

    <!-- Postfach <Postfach> -->
    <xsl:if test="string($showPostfach) != 'false'">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:if test="string(pd:address/pd:pob)">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_postfach'"/>
          </xsl:call-template>
          <xsl:text> </xsl:text>
          <xsl:value-of select="./pd:address/pd:pob"/>
        </xsl:if>
      </fo:block>
    </xsl:if>

    <!-- PLZ Ort -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="./pd:address/pd:postCode"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:address/pd:location"/>
    </fo:block>

   <!-- (Kontakt: Anrede Titel Name Vornamen, TelNr, Email) -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <!-- (Kontakt: -->
      <xsl:text>(</xsl:text>
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_kontakt'"/>
      </xsl:call-template>
      <xsl:text>: </xsl:text>

      <!-- ... Anrede -->
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key">
          <xsl:value-of select="concat('formular_anrede_',translate(./pd:contact/pd:salutation,'MRS','mrs'))"/>
        </xsl:with-param>
      </xsl:call-template>
      <!-- Titel Name Vornamen -->
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:contact/pd:title"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:contact/pd:surname"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:contact/pd:givenName"/>

      <!-- , TelNr -->
      <xsl:if test="string(pd:contact/pd:telephone1)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="./pd:contact/pd:telephone1"/>
      </xsl:if>

      <!-- , Email) -->
      <xsl:if test="string(pd:contact/pd:email)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="./pd:contact/pd:email"/>
      </xsl:if>
     <xsl:text>)</xsl:text>

    </fo:block>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-post-bank">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" >
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_postkonto'"/>
          </xsl:call-template>
        </fo:block>

        <!-- Kontoinfo oder "-" ausgeben -->
        <xsl:choose>
          <xsl:when test="string(/pd:prosecutionDemand/pd:account[pd:type = 'post'])">
            <xsl:apply-templates select="/pd:prosecutionDemand/pd:account[pd:type = 'post']"/>
          </xsl:when>
          <xsl:otherwise>
            <fo:block xsl:use-attribute-sets="font-normal">
              <xsl:text> - </xsl:text>
            </fo:block>
          </xsl:otherwise>
        </xsl:choose>

      </fo:table-cell>
      <fo:table-cell xsl:use-attribute-sets="cell.style" >
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_bankkonto'"/>
          </xsl:call-template>
        </fo:block>

        <!-- Kontoinfo oder "-" ausgeben -->
        <xsl:choose>
          <xsl:when test="string(/pd:prosecutionDemand/pd:account[pd:type = 'bank'])">
            <xsl:apply-templates select="pd:prosecutionDemand/pd:account[pd:type = 'bank']"/>
          </xsl:when>
          <xsl:otherwise>
            <fo:block xsl:use-attribute-sets="font-normal">
              <xsl:text> - </xsl:text>
            </fo:block>
          </xsl:otherwise>
        </xsl:choose>

      </fo:table-cell>
    </fo:table-row>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:account[pd:type = 'post']">
    <!-- PC <Kontonummer> (Inhaber: <Inhaber>) -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_pc'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:number"/>
      <xsl:text> (</xsl:text>
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_inhaber'"/>
      </xsl:call-template>
      <xsl:text>: </xsl:text>
      <xsl:value-of select="./pd:owner"/>
      <xsl:text>)</xsl:text>
    </fo:block>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:account[pd:type = 'bank']">

    <!-- Bank <Bankname>, <Ort>, <Land> -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_bank'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:bankName"/>
      <xsl:text>, </xsl:text>
      <xsl:value-of select="./pd:location"/>
      <xsl:text>, </xsl:text>
      <!-- vollständigen Ländernamen -->
      <xsl:value-of select="./pd:country"/>
    </fo:block>


    <!-- BC <Clearing-Nr>, PC <PC-Bank> -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_bc'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:clearingNumber"/>

      <xsl:if test="string(pd:postAccount)">
        <xsl:text>, </xsl:text>
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'formular_pc'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="./pd:postAccount"/>
      </xsl:if>
    </fo:block>

    <!-- Konto <Kontonummer> (Inhaber: <Inhaber>) -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_bankkonto_konto'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:number"/>
      <xsl:text> (</xsl:text>
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'formular_inhaber'"/>
      </xsl:call-template>
      <xsl:text>: </xsl:text>
      <xsl:value-of select="./pd:owner"/>
      <xsl:text>)</xsl:text>
    </fo:block>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-forderungen">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" number-columns-spanned="2">
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_forderungen'"/>
          </xsl:call-template>
        </fo:block>

        <fo:list-block>
          <xsl:apply-templates select="/pd:prosecutionDemand/pd:claim"/>
        </fo:list-block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:claim">
    <fo:list-item>
      <fo:list-item-label end-indent="label-end()">
        <fo:block xsl:use-attribute-sets="font-normal">
          <xsl:value-of select="position()"/>
          <xsl:text>.</xsl:text>
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block xsl:use-attribute-sets="font-normal">

          <!-- Forderungssumme: <Summe> Fr.-->
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_forderungssumme'"/>
          </xsl:call-template>
          <xsl:text>: </xsl:text>
          <xsl:value-of select="pd:outstandingAmount"/>
          <xsl:text> Fr.</xsl:text>

          <!-- nebst Zins zu <Zins>% seit <Datum> -->
          <xsl:if test="string(pd:interest)">
            <xsl:text> </xsl:text>
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'formular_nebst_zins'"/>
            </xsl:call-template>
            <xsl:text> </xsl:text>
            <xsl:value-of select="pd:interest"/>
            <xsl:text>% </xsl:text>
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'formular_seit'"/>
            </xsl:call-template>
            <xsl:text> </xsl:text>
            <xsl:call-template name="format-date">
              <xsl:with-param name="date" select="pd:interestSince"/>
            </xsl:call-template>
          </xsl:if>
        </fo:block>

        <!-- Forderungsgrund: <Grund> -->
        <fo:block xsl:use-attribute-sets="font-normal">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_forderungsgrund'"/>
          </xsl:call-template>
          <xsl:text>: </xsl:text>
          <xsl:value-of select="pd:reason"/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-bemerkungen">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" number-columns-spanned="2">
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_bemerkungen'"/>
          </xsl:call-template>
        </fo:block>

        <fo:block xsl:use-attribute-sets="font-normal">
          <xsl:choose>
            <xsl:when test="string(/pd:prosecutionDemand/pd:remark)">
              <xsl:value-of select="/pd:prosecutionDemand/pd:remark"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text> - </xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-unterschrift">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" number-columns-spanned="2">
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_unterschrift'"/>
          </xsl:call-template>
        </fo:block>

        <fo:block xsl:use-attribute-sets="font-normal">
          <fo:leader leader-pattern="space"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="font-normal">
          <fo:leader leader-pattern="space"/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-abspann">
    <fo:table-row>
      <fo:table-cell xsl:use-attribute-sets="cell.style" number-columns-spanned="2">
        <!-- Dieses Formular ist zu unterzeichnen ... -->
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_hinweis'"/>
          </xsl:call-template>
        </fo:block>

        <!-- Beachten Sie die Erläuterungen ... -->
        <fo:block xsl:use-attribute-sets="font-bold">
          <xsl:call-template name="getMsg">
            <xsl:with-param name="key" select="'formular_hinweis2'"/>
          </xsl:call-template>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>

  </xsl:template>

</xsl:stylesheet>