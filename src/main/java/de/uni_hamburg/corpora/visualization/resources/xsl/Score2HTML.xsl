<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:exmaralda="http://www.exmaralda.org/xml"
    version="2.0">
    <xsl:output encoding="UTF-8" method="xml" omit-xml-declaration="yes"/>


    <xsl:key name="tier-title-by-category" match="@title" use="../@category"/>

    <!-- ********************** -->
    <!-- Parameters Declaration -->
    <!-- ********************** -->
    
    <xsl:param name="TRANSCRIPTION_ID" as="xs:string?" required="no"/>
    <xsl:param name="COMMUNICATION_ID" as="xs:string?" required="no"/>
    <xsl:param name="RECORDING_PATH" as="xs:string?" required="no"/>
    <xsl:param name="RECORDING_TYPE" as="xs:string?" required="no"/>
    <xsl:param name="EMAIL_ADDRESS" as="xs:string?" required="no"/>
    <xsl:param name="WEBSERVICE_NAME" as="xs:string?" required="no"/>
    <xsl:param name="HZSK_WEBSITE" as="xs:string?" required="no"/>
    
    
    <!-- ********************* -->
    <!-- Variables Declaration -->
    <!-- ********************* -->
    
    <!-- The displayed name of the corpus -->
    <!-- occurs, for example in the navigation bar -->
    <xsl:variable name="CORPUS_NAME" select="//project-name" as="xs:string?"/>

    <!-- The displayed name of the transcription -->
    <!-- occurs, for example in the navigation bar -->
    <xsl:variable name="TRANSCRIPTION_NAME" select="//transcription-name" as="xs:string?"/>

    <!-- the base of the filename from which the names of all linked files are derived -->
    <xsl:variable name="BASE_FILENAME">
        <xsl:value-of select="//referenced-file[1]/@url"/>
        <!-- <xsl:value-of select="//ud-information[@attribute-name='Code']"/> -->
    </xsl:variable>
    
    <!-- the path to the folder with resources -->
    <xsl:variable name="TOP_LEVEL_PATH" as="xs:string" select="'https://corpora.uni-hamburg.de/drupal/sites/default/files/visualization/'"/>

    <xsl:variable name="DATASTREAM_VIDEO" as="xs:string?" select="$RECORDING_PATH"/>

    <xsl:variable name="DATASTREAM_AUDIO" as="xs:string?" select="$RECORDING_PATH"/>

    <!-- the name of the project which owns the corpus -->
    <xsl:variable name="PROJECT_NAME" as="xs:string" select="'EXMARaLDA'"/>

    <!-- the URL of the project which owns the corpus -->
    <xsl:variable name="PROJECT_URL" as="xs:string" select="'http://www.exmaralda.org/'"/>

    <!-- whether or not the transcription contains video -->
    <xsl:variable name="HAS_VIDEO" as="xs:boolean" select="$RECORDING_TYPE=('WEBM', 'MPEG', 'MPG')"/>

    <!-- whether or not the transcription contains video -->
    <xsl:variable name="HAS_AUDIO" as="xs:boolean" select="$RECORDING_TYPE=('WAV', 'OGG', 'MP3')"/>
    
    <!-- Titles of tiers by category -->
    <xsl:variable name="TIER_TITLES">
        <tier category="k" title="Commentary tier"/>
        <tier category="nn" title="NN tier"/>
        <tier category="pause" title="Pause tier"/>
        <tier category="en" title="English tranlsation tier"/>
        <tier category="de" title="German translation tier"/>
        <tier category="nr" title="News tier"/>
        <tier category="cs" title="Code switch tier"/>
        <tier category="pho" title="Phonetics tier"/>
        <tier category="sup" title="intonation tier"/>
        <tier category="nv" title="non-verbal actions tier"/>
    </xsl:variable>
    

    <!-- ******************************************************************************************************************************************** -->

    <!-- ... and then specify those which are only valid for this kind of visualisation document -->

    <!-- the path to the CSS stylesheet to be used with this HTML visualisation -->
    <xsl:variable name="CSS_PATH" as="xs:string" select="concat($TOP_LEVEL_PATH, 'VisualizationFormat.css')"/>
    <xsl:variable name="CSS_PATH_SCORE" as="xs:string" select="concat($TOP_LEVEL_PATH, 'PartiturFormat-new.css')"/>

    <!-- ************************ -->
    <!--    Top level template   -->
    <!-- ************************ -->

    <xsl:template match="/">
        <html>
            <head>
                <xsl:call-template name="HEAD_DATA"/>
                <style>
                    /* ************************************************** */
                    /*                 styles for main document parts            */
                    /* ************************************************** */
                    
                    body{
                        margin:0;
                        overflow:hidden;
                        font-family:calibri, helvetica, sans-serif;
                    }
                    
                    /* the header */
                    div#head{
                        background-color:#40627C;
                        color:white;
                        font-size:12pt;
                        font-weight:bold;
                        padding:7px;
                        z-index:100;
                        position:absolute;
                        width:100%;
                        height:45px;
                        top:0;
                        left:0;
                    }
                    
                    
                    div#content{
                        position:absolute;
                        top:55px;
                        bottom:30px;
                        width:100%;
                    }
                    
                    div#controls{
                        float:left;
                        /*min-width:300px;*/
                        /*width:20%;*/
                    }
                    
                    div.sidebarcontrol{
                        padding:5px;
                        padding-top:20px;
                        float:left;
                        clear:left;
                        min-width:320px;
                    }
                    
                    
                    div#main{
                        /*padding-top: 20px;*/
                        /*padding-bottom: 200px;*/
                        border:2px solid #cfd6de;
                        /*margin:0 auto;*/
                        margin:20px 0px 10px 340px;
                        height:97%;
                        /*height:800px;*/
                        /*width:75%;*/
                        /*float:right;*/
                        /*height:98%;*/
                        /*width:80%;*/
                        /*margin-left:300px;*/
                        /*clear: right;*/
                        /*min-width:1100px;*/
                    }
                    
                    div#transcription{
                        overflow:auto;
                        /*min-width:1100px;*/
                        /*height:800px;*/
                        max-height:98%;
                    }
                    
                    
                   <!-- div#footer{
                        position:absolute;
                        height:35px;
                        bottom:0;
                        left:5px;
                        color:gray;
                        background-color:white;
                        border:1px solid gray;
                        text-align:center;
                        font-size:10pt;
                        margin-top:0px;
                        min-width:320px;
                    
                    }-->
                    
                    div#mediaplayer{
                    }
                    
                    .collapse_box{
                        border:2px solid black;
                        padding:0;
                    }
                    .collapse_box .collapse_title{
                        background-color:#adc1d6;
                        font-weight:bold;
                        height:17px;
                        padding-left:5px;
                        padding-bottom:2px;
                    }
                    .collapse_box .collapse_content{
                        background-color:#e4e9f5;
                        padding:7px 5px 3px 5px;
                        margin:0;
                    }
                    
                    <!-- span#corpus-title{
                        color:blue;
                    }
                    #head a{
                        text-decoration:none;
                        color:white;
                    }
                    #previous-doc{
                        font-size:10pt;
                    }
                    #next-doc{
                        font-size:10pt;
                    }
                    
                    a.HeadLink{
                        font-family:Sans-Serif;
                        font-size:16pt;
                        font-style:normal;
                        font-weight:bold;
                        text-decoration:none
                    } -->
                    
                    /* ************************************************** */
                    /*                 styles for generic elements                   */
                    /* ************************************************** */
                    
                    td{
                        white-space:nowrap
                    }
                    
                    p.spacer{
                        margin-top:50px
                    }
                    
                    
                    img{
                        border-width:0px;
                    }
                    
                    
                    
                    /* ************************************************** */
                    /*                 styles for partitur numbering                 */
                    /* ************************************************** */
                    
                    p.pno{
                        padding-top:4px;
                        padding-bottom:4px;
                        font-weight:bold;
                        margin-left:50px
                    }
                    
                    span.pno{
                        font-weight:bold;
                    }
                    
                    /* ************************************************** */
                    /**** styles for generic parts of the Partitur table **/
                    /* ************************************************** */
                    
                    /* style for the table as a whole */
                    table.p{
                        border-collapse:collapse;
                        border-spacing:0px;
                        empty-cells:show;
                        margin-left:2em;
                    }
                    
                    /* style for an empty cell inside a partitur */
                    td.emp{
                        font-size:6pt
                    }
                    
                    /* style for a cell above the bottom of the partitur frame */
                    td.b{
                        border-bottom-color:rgb(153, 153, 153);
                        border-bottom-style:Solid;
                        border-bottom-width:1px;
                    
                    }
                    
                    /* style for a cell before the right edge of the partitur frame */
                    td.r{
                        border-right-color:rgb(153, 153, 153);
                        border-right-style:Solid;
                        border-right-width:1px;
                    }
                    
                    /* style for cells with no left border (tier labels outside the partitur frame? */
                    td.nlb{
                        border-left-style:None;
                        border-left-width:0px;
                        border-right-style:None;
                        border-right-width:0px;
                    }
                    
                    /* style for main tier labels (i.e. labels for tiers of type T(ranscription) */
                    td.tlm{
                        font-family:"Times New Roman", Times, serif;
                        font-size:11pt;
                        font-style:normal;
                        font-weight:bold;
                        color:rgb(0, 0, 0);
                        background-color:white;
                        border-left-color:rgb(153, 153, 153);
                        border-left-style:Solid;
                        border-left-width:1px;
                        border-right:1px solid rgb(153, 153, 153);
                    }
                    
                    /* style for other tier labels (i.e. labels for tiers of type other than T(ranscription) */
                    td.tlo{
                        font-family:"Times New Roman", Times, serif;
                        font-size:10pt;
                        font-style:normal;
                        font-weight:bold;
                        color:rgb(200, 200, 200);
                        background-color:white;
                        border-left-color:rgb(153, 153, 153);
                        border-left-style:Solid;
                        border-left-width:1px;
                        border-right:1px solid rgb(153, 153, 153);
                    }
                    
                    /* the style for the table cell above the tier labels in the timeline row */
                    td.snc-emp{
                        border-bottom-color:rgb(153, 153, 153);
                        border-bottom-style:Solid;
                        border-bottom-width:1px;
                    }
                    
                    /* the style for the table cell representing a timeline-item */
                    td.snc{
                        border-bottom-color:rgb(100, 100, 100);
                        border-bottom-style:Solid;
                        background-color:silver;
                        color:white;
                        border-bottom-width:1px;
                    }
                    
                    /* the style for the anchor from the partitur to the utterance list */
                    a.snc-anc{
                        font-family:sans-serif;
                        font-size:8pt;
                        font-style:normal;
                        font-weight:bold;
                        color:white;
                        background-color:silver;
                        text-decoration:none
                    }
                    
                    /* *********************************************** */
                    /* styles for tiers according to their categories */
                    /* *********************************************** */
                    
                    td.v{
                        font-family:"Arial Unicode MS", Arial, sans-serif;
                        font-size:11pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 0);
                        text-align:justify;
                    }
                    
                    td.sup{
                        font-family:"Times New Roman", Times, serif;
                        font-size:8pt;
                        font-weight:normal;
                        color:rgb(0, 0, 0);
                        background-color:rgb(255, 255, 200);
                        border-right:solid 1px rgb(255, 255, 255)
                    }
                    
                    tr.sup{
                        height:8px;
                    }
                    
                    td.k{
                        font-family:"Times New Roman", Times, serif;
                        font-size:8pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 0);
                        background-color:rgb(240, 240, 240);
                    }
                    
                    td.pause{
                        font-family:"Times New Roman", Times, serif;
                        font-size:10pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 255);
                    }
                    td.nn{
                        font-family:"Times New Roman", Times, serif;
                        font-size:10pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 255)
                    }
                    
                    td.nsp{
                        font-family:"Times New Roman", Times, serif;
                        font-size:10pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 255)
                    }
                    
                    td.nv{
                        font-family:"Times New Roman", Times, serif;
                        font-size:9pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 100);
                        border-right:1px solid rgb(0, 0, 100);
                        border-top:1px dotted rgb(0, 0, 100);
                    }
                    
                    
                    td.nr{
                        font-family:"Times New Roman", Times, serif;
                        font-size:10pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(0, 0, 255)
                    }
                    td.akz{
                        font-family:"Times New Roman", Times, serif;
                        font-size:1pt;
                        font-style:normal;
                        font-weight:normal;
                        color:rgb(100, 100, 100);
                        background-color:rgb(100, 100, 100);
                    }
                    tr.akz{
                        height:1px;
                    }
                    td.en{
                        font-family:"Times New Roman", Times, serif;
                        font-size:8pt;
                        font-style:italic;
                        font-weight:normal;
                        color:rgb(100, 100, 100);
                        text-align:justify;
                    
                    }
                    
                    td.de{
                        font-family:"Times New Roman", Times, serif;
                        font-size:8pt;
                        font-style:italic;
                        font-weight:normal;
                        color:rgb(100, 100, 100)
                    }
                    
                    td.hd{
                        font-family:"Times New Roman", Times, serif;
                        font-size:8pt;
                        font-style:italic;
                        font-weight:normal;
                        color:rgb(100, 100, 100)
                    }<!--
                    #transcription{
                        background-color:#E4E9F5;
                    }
                    div#head{
                        background-color:#40627C;
                    }
                    td{
                        background-color:#FFFFFF;
                    }
                    td.snc{
                        background-color:#E4E9F5;
                    }
                    .snc-emp{
                        background-color:#E4E9F5;
                    }
                    a.snc-anc{
                        background-color:#E4E9F5;
                        color:black;
                    }-->
                    <!-- div#footer-new{
                        position:absolute;
                        height:35px;
                        bottom:0;
                        left:5px;
                        color:gray;
                        background-color:white;
                        border:1px solid gray;
                        text-align:center;
                        font-size:10pt;
                        margin-top:0px;
                        min-width:320px;
                    }-->
                    
                    .invert{
                        -webkit-filter:invert(100%);
                        filter:invert(100%);
                    }</style>
            <script type="text/javascript">
                <xsl:comment>jsholder</xsl:comment>
            </script>
            </head>
            <body>
                <xsl:call-template name="MAKE_TITLE"/>
                <div id="content">
                    <div id="controls">
                        <xsl:call-template name="MAKE_PLAYER_DIV"/>
                        <xsl:call-template name="MAKE_TIER_DISPLAY_CONTROL"/>
                        <xsl:call-template name="MAKE_WEB_SERVICE_INFO"/>
                        <!-- <xsl:call-template name="MAKE_DOWNLOAD_FILES_CONTROL"/> -->
                        <!-- <xsl:call-template name="MAKE_FOOTER"/>-->
                    </div>
                    <div id="main">
                        <div id="transcription">
                            <xsl:apply-templates select="//it-bundle"/>
                            <!-- I removed multiple breaks from here, one seemed to be enough (I'm not exactly sure if even that is needed, but I guess it does no harm -Niko) -->
                            <p>
                                <br/>
                            </p>
                        </div>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- ************************ -->
    <!--     one partitur area    -->
    <!-- ************************ -->

    <xsl:template match="it-bundle">
        <!-- the numbering for this partitur area -->
        <span class="pno">
            <xsl:for-each select="anchor">
                <a name="{.}"/>
            </xsl:for-each>
            <xsl:value-of select="concat('[', position(), ']')"/>
        </span>

        <!-- the table representing the actual partitur area -->
        <table class="p" width="{round(1.4*//table-width/@table-width)}">
            <xsl:apply-templates select="sync-points"/>
            <xsl:apply-templates select="it-line"/>
        </table>
    </xsl:template>


    <!-- ********************************* -->
    <!-- syncpoints aka timeline items -->
    <!-- ********************************* -->

    <xsl:template match="sync-points">
        <tr>
            <!-- one empty cell above the tier labels -->
            <td class="snc-emp"/>
            <!-- now the real syncPoints -->
            <xsl:apply-templates select="sync-point"/>
        </tr>
    </xsl:template>

    <!-- ********************************************* -->
    <!-- an individual syncpoint aka timeline item -->
    <!-- ********************************************* -->
    <xsl:template match="sync-point">
        <td class="snc">
            <!-- anchor for media playback -->
            <xsl:if test="//tli[@id=current()/@id]/@time">
                <xsl:variable name="TIME" select="0 + //tli[@id=current()/@id]/@time"/>
                <a onclick="jump('{format-number(($TIME + 0.03), '#.##')}');">
                    <img class="media invert" title="{exmaralda:FORMAT_TIME($TIME)}&#x0020;-&#x0020;Click to start player" src="{$TOP_LEVEL_PATH}pbn.gif"/>
                </a>
            </xsl:if>
        </td>
    </xsl:template>

    <!-- ****************************** -->
    <!-- an individual it-line aka tier -->
    <!-- ****************************** -->
    <xsl:template match="it-line">
        <tr class="{//tier[@id=current()/@formatref]/@category}" name="{//tier[@id=current()/@formatref]/@category}"></tr>

            <xsl:variable name="itLinePosition" select="position()" as="xs:integer"/>

            <!-- aply the template for the tier label -->
            <xsl:apply-templates select="it-label"/>

            <xsl:for-each select="../sync-points/sync-point">

                <xsl:variable name="Pos" select="1+count(preceding-sibling::*)" as="xs:integer"/>

                <xsl:variable name="interval_is_covered">
                    <xsl:for-each select="../../it-line[$itLinePosition+0]/it-chunk">
                        <xsl:variable name="startPos" select="1+count(../../sync-points/sync-point[@id=current()/@start-sync]/preceding-sibling::*)" as="xs:integer"/>
                        <xsl:variable name="endPos" select="1+count(../../sync-points/sync-point[@id=current()/@end-sync]/preceding-sibling::*)" as="xs:integer"/>
                        <xsl:if test="$startPos+0&lt;=$Pos+0 and $endPos+0&gt;$Pos+0">X</xsl:if>
                    </xsl:for-each>
                </xsl:variable>

                <xsl:choose>
                    <!-- case where there is no event at or across the current timepoint -->
                    <xsl:when test="not(contains($interval_is_covered,'X'))">
                        <td>
                            <xsl:attribute name="class">
                                <!-- TODO: check why this is so complex - don't we have the variable's value already? -->
                                <xsl:variable name="CATEGORY" select="//tier[@id=current()/../../it-line[$itLinePosition+0]/@formatref]/@category"/>
                                <!-- TODO: check why this is so complex , use parameters maybe -->
                                <xsl:if
                                    test="($CATEGORY!='k' and count(current()/../../it-line[$itLinePosition+0]/following-sibling::*)=0) or ($CATEGORY!='k' and //tier[@id=current()/../../it-line[$itLinePosition+0]/following-sibling::*[1]/@formatref]/@category='k')">
                                    <xsl:text>b </xsl:text>
                                </xsl:if>
                                <xsl:if
                                    test="$CATEGORY!='k' and count(current()/following-sibling::*)=0">
                                    <xsl:text>r </xsl:text>
                                </xsl:if>
                                <xsl:text>emp</xsl:text>
                            </xsl:attribute>

                            <!-- if this is the last entry in that row: stretch it! -->
                            <xsl:if test="count(current()/following-sibling::*)=0">
                                <xsl:attribute name="width">100%</xsl:attribute>
                            </xsl:if>
                        </td>
                    </xsl:when>

                    <!-- case where there IS an event at the current timepoint -->
                    <xsl:otherwise>
                        <xsl:apply-templates
                            select="../../it-line[$itLinePosition+0]/it-chunk[@start-sync=current()/@id]"
                        />
                    </xsl:otherwise>

                </xsl:choose>
            </xsl:for-each>
        
    </xsl:template>

    <xsl:template match="it-label">
        <xsl:variable name="CATEGORY" select="//tier[@id=current()/../@formatref]/@category"/>
        <xsl:element name="td">
            <xsl:attribute name="class">
                <!-- check if it is the last tier in the partitur frame -->
                <xsl:if
                    test="($CATEGORY!='k' and count(../following-sibling::*)=0) or ($CATEGORY!='k' and //tier[@id=current()/../following-sibling::*[1]/@formatref]/@category='k')">
                    <xsl:text>b </xsl:text>
                </xsl:if>
                <!-- check whether it is a main or a subordinate tier -->
                <xsl:choose>
                    <xsl:when test="//tier[@id=current()/../@formatref]/@category='v'"
                        >tlm</xsl:when>
                    <xsl:otherwise>tlo</xsl:otherwise>
                </xsl:choose>
                <!-- TODO: check if this can/should be parameterized -->
                <xsl:if test="//tier[@id=current()/../@formatref]/@category='k' ">
                    <xsl:text> nlb</xsl:text>
                </xsl:if>
            </xsl:attribute>

            <!-- the tooltip title for this tier -->
            <xsl:attribute name="title">
                <xsl:variable name="SPEAKER_ID" select="//tier[@id=current()/../@formatref]/@speaker" as="xs:string"/>
                <xsl:value-of select="key('tier-title-by-category', $CATEGORY, $TIER_TITLES)"/>
            </xsl:attribute>

            <xsl:value-of select="run/text()"/>

            <!-- two non-breaking spaces behind the tier-label -->
            <xsl:if test="not(string-length(run/text())=0)">
                <xsl:text>&#x00A0;&#x00A0;</xsl:text>
            </xsl:if>

        </xsl:element>
    </xsl:template>

    <xsl:template match="it-chunk">
        <xsl:variable name="CATEGORY" select="//tier[@id=current()/../@formatref]/@category"/>
        <xsl:variable name="cellspan" select="count(../../sync-points/sync-point[@id=current()/@end-sync]/preceding-sibling::*)-count(../../sync-points/sync-point[@id=current()/@start-sync]/preceding-sibling::*)"/>
        <xsl:variable name="tiercategory" select="//tier[@id=current()/@formatref]/@category"/>

        <td colspan="{$cellspan}">            
            <xsl:attribute name="class">
                <xsl:if
                    test="($CATEGORY!='k' and count(../following-sibling::*)=0) or ($CATEGORY!='k' and //tier[@id=current()/../following-sibling::*[1]/@formatref]/@category='k')">
                    <xsl:text>b </xsl:text>
                </xsl:if>
                <xsl:value-of select="$tiercategory"/>
            </xsl:attribute>
            
            <xsl:attribute name="data-tl">
                <xsl:variable name="TIMESTART" select="0 + //tli[@id=current()/@start-sync]/@time"/>                
                <xsl:variable name="TIMEEND">
                    <xsl:choose>
                        <xsl:when
                            test="number(//tli[@id=current()/@end-sync]/@time) = number(//tli[@id=current()/@end-sync]/@time)">
                            <!-- One needs to adjust the 0 so that the previous annotation would not get highlighted when one is started by clicking play icon -->
                            <xsl:value-of select="0 + //tli[@id=current()/@end-sync]/@time"/>
                        </xsl:when>
                        <xsl:when
                            test="number(//tli[@id=current()/../../following-sibling::it-bundle[1]/it-line[1]/it-chunk[1]/@end-sync]/@time) = number(//tli[@id=current()/../../following-sibling::it-bundle[1]/it-line[1]/it-chunk[1]/@end-sync]/@time)">
                            <xsl:value-of
                                select="0 + //tli[@id=current()/../../following-sibling::it-bundle[1]/it-line[1]/it-chunk[1]/@end-sync]/@time"
                            />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="0 + max(//tli/@time)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="concat(format-number($TIMESTART, '#.##'), '-', format-number($TIMEEND, '#.##'))"/>
            </xsl:attribute>
            
            <xsl:apply-templates/>
        </td>
    </xsl:template>

    <!-- *********************************************************************************** -->
    <!-- *********************************************************************************** -->
    <!-- ************************** HTML Templates ***************************************** -->
    <!-- *********************************************************************************** -->
    <!-- *********************************************************************************** -->

    <!-- Generates the HTML head information for this transcription document -->
    <!-- i.e. the document title, the document encoding etc. -->
    <xsl:template name="HEAD_DATA">
        <title>
            <xsl:value-of select="$CORPUS_NAME"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="$TRANSCRIPTION_NAME"/>
        </title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <script type="text/javascript" src="{$TOP_LEVEL_PATH}jsfunctions.js">
            <xsl:text><![CDATA[  
			]]></xsl:text>
        </script>
    </xsl:template>

    <xsl:template name="MAKE_TITLE">
        <div id="head">
            <span id="document-title">
                <xsl:value-of select="$TRANSCRIPTION_NAME"/>
            </span>
        </div>
    </xsl:template>

    <xsl:template name="MAKE_PLAYER_DIV">
        <div id="mediaplayer" class="sidebarcontrol">
            <xsl:if test="$HAS_VIDEO">
                <video controls="controls" width="320" height="240" data-tlid="media">
                    <source src="{$DATASTREAM_VIDEO}" type="video/webm"/>
                </video>
            </xsl:if>
            <xsl:if test="not($HAS_VIDEO) and ($HAS_AUDIO)">
                <audio controls="controls" data-tlid="media">
                    <source src="{$DATASTREAM_AUDIO}" type="audio/ogg"/>
                </audio>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="MAKE_TIER_DISPLAY_CONTROL">
        <div class="sidebarcontrol">
            <div class="collapse_box" id="tier_display">
                <div class="collapse_title">
                    <!--<img alt="Minimize_grey" class="collapse_icon" src="../../resources/minusButton.png" /> --> 
					Tier display
				</div>
                <div class="collapse_content">
                    <xsl:for-each-group select="//tier/@category" group-by=".">
                        <xsl:sort select="."/>
                        <input style="margin-left:7px;" type="checkbox" name="category" value="{current-grouping-key()}" checked="checked" onclick="showHideTier(this,{current-grouping-key()}')">
                            <b><xsl:value-of select="current-grouping-key()"/></b>
                        </input>
                    </xsl:for-each-group>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="MAKE_DOWNLOAD_FILES_CONTROL">
        <div class="sidebarcontrol">
            <div class="collapse_box" id="tier_display">
                <div class="collapse_title">
				 	Files
				</div>
                <div class="collapse_content">
                    <img alt="EXB" class="collapse_icon" src="{$TOP_LEVEL_PATH}exb-icon.png"/>
                    <a href="{test}/EXB" style="text-decoration:none;font-weight:bold;font-family:sans-serif;font-size:10pt;">
                        EXMARaLDA Basic Transcription
                    </a>
                    <br/>                    
                    <img alt="EXS" class="collapse_icon" src="{$TOP_LEVEL_PATH}exs-icon.png"/>
                    <a href="{$RECORDING_PATH}/EXB" style="text-decoration:none;font-weight:bold;font-family:sans-serif;font-size:10pt;">
                        EXMARaLDA Segmented Transcription
                    </a>
                    <br/>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="MAKE_FOOTER">
        <div id="footer-new">
            <p>
                This visualization was generated on <xsl:value-of select="current-dateTime()"/> 
                with <xsl:value-of select="$WEBSERVICE_NAME"/>. 
                Please contact HZSK for more information: <xsl:value-of select="$EMAIL_ADDRESS"/>
            </p>
        </div>
    </xsl:template>

    <xsl:template name="MAKE_WEB_SERVICE_INFO">
        <div class="sidebarcontrol">
            <div class="collapse_box" id="tier_display">
                <div class="collapse_title"> Web service information </div>
                <div class="collapse_content" style="width:310;">
                    <p>
                        This visualization was generated on <xsl:value-of select="current-dateTime()"/> 
                        with <xsl:value-of select="$WEBSERVICE_NAME"/>. 
                        The web service source code can be found at <a href="https://github.com/hzsk/visualizations">GitHub</a>.
                    </p>
                    <p>Please contact <a href="{$HZSK_WEBSITE}">Hamburger Zentrum für Sprachkorpora</a> for more information.</p>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:function name="exmaralda:FORMAT_TIME">
        <xsl:param name="TIME"/>
        <xsl:variable name="totalseconds" select="0 + $TIME"/>
        <xsl:variable name="hours" select="0 + floor($totalseconds div 3600)"/>
        <xsl:variable name="minutes" select="0 + floor(($totalseconds - 3600*$hours) div 60)"/>
        <xsl:variable name="seconds" select="0 + ($totalseconds - 3600*$hours - 60*$minutes)"/>
        <xsl:if test="$hours+0 &lt; 10 and $hours &gt;0">
            <xsl:text>0</xsl:text>
            <xsl:value-of select="$hours"/>
        </xsl:if>
        <xsl:if test="$hours + 0 = 0">
            <xsl:text>00</xsl:text>
        </xsl:if>
        <xsl:text>:</xsl:text>
        <xsl:if test="$minutes+0 &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes"/>
        <xsl:text>:</xsl:text>
        <xsl:if test="$seconds+0 &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="round($seconds*100) div 100"/>
    </xsl:function>

    <xsl:function name="exmaralda:FORMAT_TIMELIGHT">
        <xsl:param name="TIME"/>
        <xsl:variable name="totalseconds" select="0 + $TIME"/>
        <xsl:value-of select="$totalseconds"/>
    </xsl:function>

</xsl:stylesheet>
