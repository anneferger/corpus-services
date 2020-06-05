<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:string="https://inel.corpora.uni-hamburg.de/xmlns/string"
    xmlns:coma="http://www.exmaralda.org/xml/comacorpus.xsd"
    exclude-result-prefixes="xs string"
    version="2.0">
    
    <xsl:output method="text" media-type="application/json" encoding="UTF-8"/>
    
    <!-- ### Global parameters ### -->    
    <!--<xsl:param name="elastic-index" select="//*:xenoData/*:Corpus/*:Description/concat(Key[lower-case(@Name)='hzsk:corpusprefix'], '-', Key[lower-case(@Name)='hzsk:corpusversion'])[not(matches(., '^\-$'))]"/>-->
    <xsl:param name="elastic-index" select="'inel.utterances'"/>
    <xsl:param name="elastic-doctype" select="'utterance'" as="xs:string"/>
    <xsl:param name="corpus-name" select="//*:xenoData/*:Corpus/*:Description/Key[lower-case(@Name)='dc:title']"/>
    
    <xsl:param name="extract-utterance-tiers" select="'ref', 'st', 'ts', 'fe', 'fg', 'fr', 'fe_usas', 'fe-N_usas', 'fe-V_usas'" as="xs:string*"/>
    <xsl:param name="extract-token-tiers" select="'mc', 'ps', 'CS'" as="xs:string*"/>
    <xsl:param name="extract-morpheme-tiers" select="'mb', 'mp', 'ge', 'gr', 'gg'" as="xs:string*"/>
    <xsl:param name="tokenizable-fields" as="xs:string+">
        <!-- tokenizable corpus description keys -->
        <xsl:sequence select="'hzsk:keywords', 'olac:data-inputter', 'olac:developer', 'olac:researcher'"/>
        <!-- not listing communication description keys (all set to tokenizable below) -->
        <!-- tokenizable speaker description keys -->
        <xsl:sequence select="()"/>
        
    </xsl:param>
    <xsl:param name="tokenizer-regex" select="'\s*[;,]+\s*'" as="xs:string"/>
    
    <xsl:param name="write-to-file" select="true()" as="xs:boolean"/>        
    <xsl:param name="file-name" select="tokenize(base-uri(.), '/')[last()]" as="xs:string"/>
    
    
    <!-- ### Global variables ### -->
    <xsl:variable name="NEWLINE" as="xs:string"><xsl:text>
</xsl:text></xsl:variable>
    
    <xsl:variable name="coma-metadata" select="//*:xenoData/*:Corpus"/>
    
    <!-- fetching the corpus-wide metadata for re-use -->
    <xsl:variable name="json-coma-corpus-metadata">
        <xsl:for-each select="$coma-metadata">
            <xsl:value-of select="concat('&quot;Corpus | Name&quot;: &quot;', //*:Corpus/@Name, '&quot;, ')"/>
            <xsl:for-each select="//*:Corpus/*:Description/*:Key">
                <xsl:value-of select="concat('&quot;Corpus | ', @Name, '&quot;: ', string:valuefy(replace(., '\n+', ' '), $tokenizer-regex, (@Name = $tokenizable-fields)), ', ')"/>                    
            </xsl:for-each>
        </xsl:for-each>
    </xsl:variable>
    
    <!-- fetching the communication metadata for re-use -->
    <xsl:variable name="json-coma-communication-metadata">
        <xsl:if test="exists($coma-metadata//*:Communication[2])">
            <!-- would need to get Communication name from somewhere in TEI to avoid this -->
            <xsl:message select="'***ERROR: multiple Communication elements not yet supported.'"></xsl:message>
        </xsl:if>
        <xsl:for-each select="$coma-metadata//*:Communication">
            <communication id="{@Id}" name="{@Name}">
                <xsl:value-of select="concat('&quot;Communication | Name&quot;: &quot;', @Name, '&quot;, ')"/>
                <xsl:for-each select="*:Description/*:Key">
                    <xsl:value-of select="concat('&quot;Communication | ', @Name, '&quot;: ', string:valuefy(., $tokenizer-regex, (@Name = $tokenizable-fields)), ', ')"/>                    
                </xsl:for-each>
                <xsl:for-each select="*:Setting, *:Language, *:Location">
                    <xsl:variable name="element-type" select="local-name()" as="xs:string"/>
                    <xsl:variable name="category-type" select="@Type" as="xs:string?"/>
                    <xsl:if test="$element-type = 'Language'">
                        <xsl:value-of select="concat('&quot;Communication | ', $element-type, concat(' | ', string:map-categories($category-type))[exists($category-type)], ' | LanguageCode&quot;: &quot;', *:LanguageCode, '&quot;, ')"/>
                    </xsl:if>
                    <xsl:for-each select="*:Description/*:Key">
                        <xsl:value-of select="concat('&quot;Communication | ', $element-type, concat(' | ', string:map-categories($category-type))[exists($category-type)], ' | ', @Name, '&quot;: ', string:valuefy(., $tokenizer-regex, true()), ', ')"/>
                    </xsl:for-each>
                </xsl:for-each>                
            </communication>            
        </xsl:for-each>
    </xsl:variable>
    
    <!-- fetching speaker/s metadata or re-use -->
    <xsl:variable name="json-coma-speaker-metadata">
        <xsl:for-each select="$coma-metadata//*:Speaker">
            <speaker Id="{@Id}" sigle="{*:Sigle}">
                <xsl:for-each select="*:Sigle, *:Pseudo, *:KnownHuman, *:Sex">
                    <xsl:value-of select="concat('&quot;Speaker | ', local-name(), '&quot;: ', string:valuefy(., $tokenizer-regex, true()), ', ')"/> 
                </xsl:for-each>
                <xsl:for-each select="*:Description/*:Key">
                    <xsl:value-of select="concat('&quot;Speaker | ', @Name, '&quot;: ', string:valuefy(., $tokenizer-regex, true()), ', ')"/>                    
                </xsl:for-each>
                <xsl:for-each select="*:Language, *:Location">
                    <xsl:variable name="element-type" select="local-name()" as="xs:string"/>
                    <xsl:variable name="category-type" select="(@Type, position())[1]" as="xs:string?"/>
                    <xsl:if test="$element-type = 'Language'">
                        <xsl:value-of select="concat('&quot;Speaker | ', $element-type, concat(' | ', string:map-categories($category-type))[exists($category-type)], ' | LanguageCode&quot;: &quot;', *:LanguageCode, '&quot;, ')"/>
                    </xsl:if>
                    <xsl:for-each select="*:Description/*:Key">
                        <xsl:value-of select="concat('&quot;Speaker | ', $element-type, concat(' | ', string:map-categories($category-type))[exists($category-type)], ' | ', @Name, '&quot;: ', string:valuefy(., $tokenizer-regex, true()), ', ')"/>
                    </xsl:for-each>
                </xsl:for-each>
            </speaker>
        </xsl:for-each>
    </xsl:variable>
    
    
    <!-- ######### Keys ######### -->
    <xsl:key name="event-by-tier-category-and-speaker-and-start-end" match="*:event" use="concat(../@category, '#', ../@speaker, '#', @start, '#', @end)"></xsl:key>
    <xsl:key name="speaker-sigle-by-id" match="*:person/@n" use="../xml:id"></xsl:key>
    <xsl:key name="speaker-json-by-sigle" match="*:speaker" use="@sigle"/>
    <xsl:key name="span-by-from-to-spanGrpType" match="*:span" use="concat(@from, '#', @to, '#', ../@type)"/>
    <xsl:key name="person-abbr-by-id" match="*:abbr" use="../../@xml:id"/>
    
    <!-- ###### Templates ###### -->
    <xsl:template match="/">
        
        <xsl:variable name="result">
            
            <!-- start processing -->
            <xsl:variable name="doc" select="."/>
            <xsl:for-each select="//*:seg[@type='utterance']">
                <xsl:variable name="annotation-block" select="ancestor::*:annotationBlock"/>
                <xsl:variable name="seg-id" select="@xml:id" as="xs:string"/>
                <xsl:variable name="speaker-sigle" select="$annotation-block/@who" as="xs:string"/>
                <xsl:variable name="unit-ids" select="*/@xml:id" as="xs:string+"/>
                
                <xsl:value-of select="concat('{ &quot;index&quot;: { &quot;_index&quot;: &quot;', $elastic-index, '&quot;, &quot;_type&quot;: &quot;', $elastic-doctype, '&quot; }}', $NEWLINE)"/>
                <!-- open JSON for this utterance -->
                <xsl:value-of select="'{ &quot;doc&quot; : { '"/> 
                
                <!-- some general metadata fields-->
                <!--<xsl:value-of select="concat('&quot;corpus&quot; : &quot;', $corpus-name, '&quot;, ')"/>-->
                <xsl:value-of select="concat('&quot;file&quot; : &quot;', $file-name, '&quot;, ')"/>
                
                <!-- speaker metadata fields -->
                <!-- sometimes there are duplicate entries for speakers, so here only [1] is taken -->
                <xsl:value-of select="key('speaker-json-by-sigle', key('person-abbr-by-id', $speaker-sigle, $doc), $json-coma-speaker-metadata)[1]"/>
                
                <!-- communication metadata fields -->
                <xsl:value-of select="$json-coma-communication-metadata/*:communication[1]"/>
                
                <!-- fields for tiers from transcript -->
                <xsl:for-each select="$extract-utterance-tiers">                    
                    <xsl:variable name="tier-name" select="." as="xs:string"/>
                    <!-- utterance-based tiers -->
                    <xsl:for-each select="key('span-by-from-to-spanGrpType', concat($seg-id, '#', $seg-id, '#', .), $annotation-block)">
                        <xsl:variable name="value" select="string:multi-replace(., '&quot;', '\\&quot;')"/>
                        <xsl:value-of select="concat('&quot;', $tier-name, '&quot; : ', (concat('&quot;', $value, '&quot;')[not(matches(., '^&quot;\s*&quot;$'))],'null')[1], ', ')"/>
                    </xsl:for-each>
                </xsl:for-each>
                
                <xsl:for-each select="$extract-token-tiers">
                    <!-- morpheme-based tiers -->
                    <xsl:variable name="tier-name" select="." as="xs:string"/>
                    <xsl:for-each select="$annotation-block/descendant::*:spanGrp[@type=$tier-name]">
                        <xsl:value-of select="concat('&quot;', $tier-name, '&quot; : ')"/>
                        <xsl:variable name="value" select="concat('[&quot;', string-join(*:span/string:multi-replace(., '&quot;', '\\&quot;'), '&quot;, &quot;'), '&quot;]')" as="xs:string"/>                
                        <xsl:choose>
                            <xsl:when test="matches($value, '^[&quot;\s*&quot;]$')">
                                <xsl:value-of select="'null'"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$value"/>
                            </xsl:otherwise>
                        </xsl:choose> 
                        <xsl:value-of select="', '"/>  
                    </xsl:for-each>
                </xsl:for-each>
                
                <xsl:for-each select="$extract-morpheme-tiers">
                    <!-- morpheme-based tiers -->
                    <xsl:variable name="tier-name" select="." as="xs:string"/>
                    <xsl:for-each select="$annotation-block/descendant::*:spanGrp[@type=$tier-name]">
                        <xsl:value-of select="concat('&quot;', $tier-name, '&quot; : ')"/>
                        <xsl:variable name="value" select="concat('[&quot;', string-join(*:span/string-join(*:span/string:multi-replace(text(), '&quot;', '\\&quot;'), '-'), '&quot;, &quot;'), '&quot;]')" as="xs:string"/>                
                        <xsl:choose>
                            <xsl:when test="matches($value, '^[&quot;\s*&quot;]$')">
                                <xsl:value-of select="'null'"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$value"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:value-of select="', '"/> 
                    </xsl:for-each>  
                </xsl:for-each>
                
                
                <!-- corpus-wide metadata fields -->
                <xsl:value-of select="$json-coma-corpus-metadata"/>
                
                <!-- field for ref to transcript HTML -->
                <!--<xsl:value-of select="concat('&quot;html-ref&quot; : &quot;https://corpora.uni-hamburg.de/repository/transcript:', $corpus-name, '_', substring-before($file-name, '.exb'), '/SCORE/', substring-before($file-name, '.exb'), '-score.html#', $start, '&quot;')"/>-->
              
                <!-- close JSON for this utterance -->
                <xsl:value-of select="concat('}}', $NEWLINE)"/>                    
            </xsl:for-each>
        </xsl:variable>
        
        <xsl:variable name="cleaned-BulkJSON" select="string:multi-replace($result, ('&quot;\s*,\s*\}', '/', '[\r\t]'), ('&quot; }', '\\/', ' '))" as="xs:string"/>
        
        <xsl:choose>
            <xsl:when test="$write-to-file">
                <xsl:result-document href="{string-join(tokenize(base-uri(.), '[/\\]+')[position()!=last()], '/')}.esbulk">
                    <xsl:value-of select="$cleaned-BulkJSON"/>
                </xsl:result-document>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$cleaned-BulkJSON"/>
                <!-- to be escaped for JSON (acc. to https://stackoverflow.com/questions/983451/where-can-i-find-a-list-of-escape-characters-required-for-my-json-ajax-return-ty) -->
                <!-- \" \\ \/ \b \f \n \r \t \u -->
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
    

    <xsl:template match="text()"/>
    
    
    <xsl:function name="string:multi-replace" as="xs:string">
        <xsl:param name="string" as="xs:string"/>
        <xsl:param name="replacee" as="xs:string*"/>
        <xsl:param name="replacer" as="xs:string*"/>
        <xsl:choose>
            <xsl:when test="$replacee[1]">
                <xsl:value-of select="string:multi-replace(replace($string, $replacee[1], $replacer[1]), subsequence($replacee, 2), subsequence($replacer, 2))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    
    <xsl:function name="string:valuefy" as="xs:string">
        <xsl:param name="value" as="xs:string"/>
        <xsl:param name="tokenizer-regex" as="xs:string+"/>
        <xsl:param name="tokenizable" as="xs:boolean"/>
        <xsl:variable name="result">
            <xsl:choose>
                <xsl:when test="$tokenizable">
                    <xsl:value-of select="'['"/>
                    <xsl:for-each select="tokenize($value, $tokenizer-regex)[not(matches(., '^\s*$'))] ">
                        <xsl:value-of select="concat('&quot;', string:multi-replace(., '&quot;', '\\&quot;'), '&quot;')"/>
                        <xsl:if test="position() != last()"><xsl:value-of select="', '"/></xsl:if>
                    </xsl:for-each>
                    <xsl:value-of select="']'"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="(concat('&quot;', string:multi-replace($value, '&quot;', '\\&quot;'), '&quot;')[not(matches(., '^&quot;\s*&quot;$'))],'null')[1]"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="$result"/>
    </xsl:function>


    <xsl:function name="string:map-categories" as="xs:string">
        <xsl:param name="category" as="xs:string"/>
        <xsl:value-of select="string:multi-replace($category, 
            ('Basic biogr. data',     '^Second language$'), 
            ('Basic biographic data', '1 Second Language')
            )"/>
    </xsl:function>
    
</xsl:stylesheet>