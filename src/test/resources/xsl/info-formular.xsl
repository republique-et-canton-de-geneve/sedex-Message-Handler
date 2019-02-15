<?xml version="1.0" encoding='iso-8859-1'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pd="http://eschkg.bj.admin.ch/info_formular/1_2"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:exslt="http://exslt.org/common"
  xmlns:java="http://xml.apache.org/xalan/java" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:str="http://www.ora.com/XSLTCookbook/namespaces/strings"
  exclude-result-prefixes="java xalan" extension-element-prefixes="str exslt java">
  <!--
		Dieses XSL Stylesheet erstellt ein Auskunftsgesuch.

		$Id: info-formular.xsl 36 2008-08-07 16:19:05Z sasha $
		$Author: sasha $
		$Revision: 36 $
		$Date: 2008-08-07 18:19:05 +0200 (Do, 07 Aug 2008) $

		Von Hand kann das ausgeführt werden wie folgt:

		cd /cygdrive/c/workspace/suis-eschkg/src/xsl
		cp ../metadata/sample-prosecutioninfo-1.2.xml .
		fop -xsl info-formular.xsl -xml sample-prosecutioninfo-1.2.xml -pdf gaga.pdf

		Die Labels für dieses Formular müssen in Plone unter

		http://calvin.glue.ch:25087/eschkg/form_labels/info_formular/folder_contents

		bereitgestellt werden. Anschliessend müssen die Labels als XML Dokument
		wie folgt bereitgestellt werden:

		wget http://calvin.glue.ch:25087/eschkg/form_labels/info_formular/getAllFormResources -O info_form_labels.xml
	-->

  <xsl:output method="xml"/>

  <!-- Top level parameters, which need to be passed by the application -->
  <xsl:param name="lang">de</xsl:param>

  <xsl:include href="utils.xsl"/>

  <!-- Internationalisierte Labels einlesen und zur Verfügung stellen -->
  <xsl:variable name="labels" select="document('info_form_labels.xml')/resources"/>

  <!-- $hasSubject ist true, gdw. das Auskunftsgesuch ein Subjekt enthält -->
  <xsl:variable name="hasSubject"
    select="count(/pd:prosecutionInfo/pd:subjectInd) != 0 or count(/pd:prosecutionInfo/pd:subjectLeg) != 0"/>

  <!-- Top level parameter defining the font stuff -->
  <xsl:param name="title-font-size">11pt</xsl:param>
  <xsl:param name="text-font-size">10pt</xsl:param>
  <xsl:param name="small-font-size">9pt</xsl:param>
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

  <xsl:attribute-set name="font-small-normal">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$font-family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$small-font-size"/>
    </xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="font-small-bold">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$font-family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$small-font-size"/>
    </xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
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
          <xsl:choose>
            <xsl:when test="$hasSubject">
              <xsl:apply-templates select="/pd:prosecutionInfo" mode="drittauskunft"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="/pd:prosecutionInfo" mode="selbstauskunft"/>
            </xsl:otherwise>
          </xsl:choose>

        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>


  <!-- ========================================================= -->
  <xsl:template match="pd:prosecutionInfo" mode="selbstauskunft">
    <xsl:call-template name="prod-vorspann"/>
    <xsl:call-template name="prod-gesuchsteller"/>
    <xsl:call-template name="prod-unterschrift"/>

    <!-- Beilagen -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="2em"
      space-after.optimum="1em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_beilagen'"/>
      </xsl:call-template>
    </fo:block>
    <!-- Ausweiskopie des Gesuchstellers -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_ausweiskopie'"/>
      </xsl:call-template>
    </fo:block>
  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:prosecutionInfo" mode="drittauskunft">
    <xsl:call-template name="prod-vorspann"/>
    <xsl:call-template name="prod-gesuchsteller"/>

    <!-- Antrag und Adresse des Subjektes -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="1em"
      space-after.optimum="1em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_antrag_dritter'"/>
      </xsl:call-template>
    </fo:block>
    <xsl:apply-templates select="(pd:subjectInd|pd:subjectLeg)"/>

    <!-- Begründung -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="1em"
      space-after.optimum="1em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_begruendung'"/>
      </xsl:call-template>
    </fo:block>
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="pd:justification"/>
    </fo:block>

    <!-- Unterschrift -->
    <xsl:call-template name="prod-unterschrift"/>

    <!-- Beilagen -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="2em"
      space-after.optimum="1em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_beilagen'"/>
      </xsl:call-template>
    </fo:block>
    <!-- 1) Ausweiskopie des Gesuchstellers -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:text>1) </xsl:text>
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_ausweiskopie'"/>
      </xsl:call-template>
    </fo:block>
    <!-- 2) Dokumentation zum berechtigten Interesse -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:text>2) </xsl:text>
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_dokumentation'"/>
      </xsl:call-template>
      <xsl:text> (</xsl:text>
      <xsl:value-of select="pd:enclosure"/>
      <xsl:text>)</xsl:text>
    </fo:block>

    <!-- Hinweis -->
    <fo:block xsl:use-attribute-sets="font-small-bold" space-before.optimum="1em"
      space-after.optimum="1em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_hinweis_titel'"/>
      </xsl:call-template>
    </fo:block>
    <fo:list-block xsl:use-attribute-sets="font-small-normal">
      <fo:list-item>
        <fo:list-item-label end-indent="label-end()">
          <fo:block>1)</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="body-start()">
          <fo:block>
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'info_formular_hinweis_p1'"/>
            </xsl:call-template>
          </fo:block>
        </fo:list-item-body>
      </fo:list-item>
      <fo:list-item>
        <fo:list-item-label end-indent="label-end()">
          <fo:block>2)</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="body-start()">
          <fo:block>
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'info_formular_hinweis_p2'"/>
            </xsl:call-template>
          </fo:block>
          <fo:block space-before.optimum="1em">
            <xsl:call-template name="getMsg">
              <xsl:with-param name="key" select="'info_formular_hinweis_p3'"/>
            </xsl:call-template>
          </fo:block>
        </fo:list-item-body>
      </fo:list-item>

    </fo:list-block>
  </xsl:template>


  <xsl:attribute-set name="inner-block.style">
    <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="table.style.address">
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
    <xsl:attribute name="inline-progression-dimension">17.5cm</xsl:attribute>
    <xsl:attribute name="border-style">none</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="cell.style.address">
    <xsl:attribute name="border-style">none</xsl:attribute>
    <xsl:attribute name="padding">0.5em</xsl:attribute>
  </xsl:attribute-set>

  <!-- ========================================================= -->
  <xsl:template name="prod-vorspann">
    <!-- Ueberschrift: Auskunftsgesuch ... -->
    <fo:block space-before.optimum="1em" text-align-last="justify">
      <fo:inline xsl:use-attribute-sets="title-font-attrs">
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_titel'"/>
        </xsl:call-template>
      </fo:inline>
      <fo:leader leader-pattern="space"/>
      <fo:inline xsl:use-attribute-sets="font-normal">
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_quellsystem'"/>
        </xsl:call-template>
      </fo:inline>
    </fo:block>

    <!-- Vermerk: Eingang am -->
    <fo:block space-before.optimum="1em" start-indent="11cm">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_eingang_am'"/>
        </xsl:call-template>
      </fo:block>
    </fo:block>

    <!-- Adresse des Betreibungsamtes für ein Fenstercouvert platzieren -->
    <fo:block space-before.optimum="1.7cm"/>
    <fo:table xsl:use-attribute-sets="table.style.address">
      <fo:table-column column-number="1" column-width="proportional-column-width(50)"/>
      <fo:table-column column-number="2" column-width="proportional-column-width(50)"/>
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell xsl:use-attribute-sets="cell.style.address">
            <fo:block start-indent="1cm">
              <xsl:apply-templates select="pd:office"/>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell xsl:use-attribute-sets="cell.style.address">
            <fo:block start-indent="2cm">
              <xsl:apply-templates select="pd:office"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
    <fo:block space-before.optimum="2.0cm"/>

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
  <xsl:template name="prod-gesuchsteller">
    <!-- ... Gesuchsteller/in: -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="3em"
      space-after.optimum="1em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_gesuchsteller'"/>
      </xsl:call-template>
      <xsl:text>:</xsl:text>
    </fo:block>
    <xsl:apply-templates select="(pd:requestorInd|pd:requestorLeg)"/>

    <!-- Im Fall der Auskunft über die eigene Person kommt der Antrag hier hin. -->
    <xsl:if test="$hasSubject = false">
      <!-- Kein Subjekt vorhanden, also ist's eine Auskunft über eigene Person -->
      <!-- ... Der/die Gesuchsteller/in beantragt ... -->
      <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="1em">
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_antrag_selbst'"/>
        </xsl:call-template>
      </fo:block>
    </xsl:if>
  </xsl:template>

  <!-- ========================================================= -->
  <!-- Ausgabe der Angaben zu einer natürlichen Person. -->
  <xsl:template match="pd:requestorInd|pd:subjectInd">
    <!-- Anrede, Titel Name, Vornamen -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <!-- ... Anrede -->
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key">
          <xsl:value-of
            select="concat('info_formular_anrede_',translate(./pd:salutation,'MRS','mrs'))"/>
        </xsl:with-param>
      </xsl:call-template>

      <!-- Titel -->
      <xsl:if test="string(pd:title)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="./pd:title"/>
      </xsl:if>

      <!-- ... Name, Vornamen -->
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:surname"/>
      <xsl:text>, </xsl:text>
      <xsl:value-of select="./pd:givenName"/>
    </fo:block>

    <!-- geb. Geburtsdatum darf nicht default datum sein -->
    <xsl:if test="string(pd:dateOfBirth) and string(pd:dateOfBirth) != '1900-01-01'">
      <fo:block xsl:use-attribute-sets="font-normal">
        <!-- ... geb. Geburtsdatum -->
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_geboren'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:call-template name="format-date">
          <xsl:with-param name="date" select="pd:dateOfBirth"/>
        </xsl:call-template>
      </fo:block>
    </xsl:if>

    <xsl:apply-templates select="./pd:address" mode="personen"/>

    <!-- Telefonnumer -->
    <xsl:if test="string(pd:telephone1)">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:value-of select="./pd:telephone1"/>
      </fo:block>
    </xsl:if>

    <!-- E-Mail Adresse -->
    <xsl:if test="string(pd:email)">
      <fo:block xsl:use-attribute-sets="font-normal">
        <xsl:value-of select="./pd:email"/>
      </fo:block>
    </xsl:if>

  </xsl:template>

  <!-- ========================================================= -->
  <!-- Formatierung der Adressen der Personen -->
  <xsl:template match="pd:address" mode="personen">
    <!-- Strasse Nr, Addr-Zusatz, Postfach -->
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
      <xsl:if test="string(pd:pob)">
        <xsl:text>, </xsl:text>
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_postfach'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="pd:pob"/>
      </xsl:if>
    </fo:block>

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
  <xsl:template match="pd:requestorLeg|pd:subjectLeg">
    <fo:block xsl:use-attribute-sets="font-normal">
      <!-- ... Firmenname -->
      <xsl:value-of select="./pd:businessName"/>
    </fo:block>

    <!-- Strasse Nr, Addr-Zusatz, Postfach -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="./pd:address/pd:street"/>
      <xsl:if test="string(pd:address/pd:number)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="./pd:address/pd:number"/>
      </xsl:if>
      <xsl:if test="string(pd:address/pd:auxiliaryAddress)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="./pd:address/pd:auxiliaryAddress"/>
        <xsl:text>,</xsl:text>
      </xsl:if>
      <xsl:if test="string(pd:address/pd:pob)">
        <xsl:text>, </xsl:text>
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'formular_postfach'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="./pd:address/pd:pob"/>
      </xsl:if>
    </fo:block>

    <!-- PLZ Ort -->
    <fo:block xsl:use-attribute-sets="font-normal">
      <xsl:value-of select="./pd:address/pd:postCode"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:address/pd:location"/>
    </fo:block>

    <!-- Kontakt: <Anrede> <Name>, <Vorname>, <TelNr>, <Email> -->
    <xsl:apply-templates select="pd:contact"/>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template match="pd:contact">
    <fo:block xsl:use-attribute-sets="font-normal">
      <!-- (Kontakt: -->
      <xsl:text>(</xsl:text>
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_kontakt'"/>
      </xsl:call-template>
      <xsl:text>: </xsl:text>

      <!-- ... Anrede -->
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key">
          <xsl:value-of
            select="concat('info_formular_anrede_',translate(./pd:salutation,'MRS','mrs'))"/>
        </xsl:with-param>
      </xsl:call-template>

      <!-- ... Title -->
      <xsl:if test="string(pd:title)">
        <xsl:text> </xsl:text>
        <xsl:value-of select="./pd:title"/>
      </xsl:if>

      <!-- ... Name, Vornamen -->
      <xsl:text> </xsl:text>
      <xsl:value-of select="./pd:surname"/>
      <xsl:text>, </xsl:text>
      <xsl:value-of select="./pd:givenName"/>

      <!-- , TelNr -->
      <xsl:if test="string(pd:telephone1)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="pd:telephone1"/>
      </xsl:if>

      <!-- , Email) -->
      <xsl:if test="string(pd:email)">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="pd:email"/>
      </xsl:if>

      <xsl:text>)</xsl:text>
    </fo:block>

  </xsl:template>

  <!-- ========================================================= -->
  <xsl:template name="prod-unterschrift">
    <xsl:if test="$hasSubject">
      <!-- Subjekt vorhanden, also ist's eine Auskunftüber einen Dritten -->
      <!-- ... Der/die Gesuchsteller/in erklärt unterschriftlich ... -->
      <fo:block xsl:use-attribute-sets="font-normal" space-before.optimum="1em">
        <xsl:call-template name="getMsg">
          <xsl:with-param name="key" select="'info_formular_hinweis_wahr'"/>
        </xsl:call-template>
      </fo:block>
    </xsl:if>

    <!-- ... Ort, Datum: ... -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="2em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_ort_datum'"/>
      </xsl:call-template>
      <fo:leader leader-length="1cm" leader-pattern="space"/>
      <fo:inline xsl:use-attribute-sets="font-normal">
        <fo:leader leader-length="10cm" leader-pattern="dots"/>
      </fo:inline>
    </fo:block>

    <!-- ... Unterschrift: ... -->
    <fo:block xsl:use-attribute-sets="font-bold" space-before.optimum="2em">
      <xsl:call-template name="getMsg">
        <xsl:with-param name="key" select="'info_formular_unterschrift'"/>
      </xsl:call-template>
      <fo:leader leader-length="1cm" leader-pattern="space"/>
      <fo:inline xsl:use-attribute-sets="font-normal">
        <fo:leader leader-length="10cm" leader-pattern="dots"/>
      </fo:inline>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
