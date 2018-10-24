<?xml version="1.0" encoding="UTF-8"?>
<!-- change 03-03-2016: additional namespaces no longer necessary 
        xmlns:standoff="http://standoff.proposal"
-->        
<xsl:stylesheet version="2.0" xmlns="http://www.tei-c.org/ns/1.0" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tesla="http://www.exmaralda.org"
	xmlns:xs="http://www.w3.org/2001/XMLSchema">

	<!-- new 25-06-2018 -->
	<!-- if this parameter is set to TRUE, XPointers will be used instead of IDREFs -->
	<xsl:param name="USE_XPOINTER">FALSE</xsl:param>


	<xsl:variable name="XPOINTER_HASH">
		<xsl:choose>
			<xsl:when test="$USE_XPOINTER = 'TRUE'">#</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:variable>

	<!-- I've been having loads of trouble with namespaces in this stylesheet -->
	<!-- The whole thing could probably look much simpler without these problems -->

	<!-- ***************************** -->

	<!-- memorizes position of timeline items for quicker reference -->
	<xsl:variable name="timeline-positions">
		<positions>
			<xsl:for-each select="//*:when">
				<item>
					<xsl:attribute name="id">
						<xsl:value-of select="@xml:id"/>
					</xsl:attribute>
					<xsl:attribute name="position">
						<xsl:value-of select="count(preceding-sibling::*:when)"/>
					</xsl:attribute>
				</item>
			</xsl:for-each>
		</positions>
	</xsl:variable>

	<!-- returns the position number of the timeline item with the given id -->
	<xsl:function name="tesla:timeline-position" as="xs:integer">
		<xsl:param name="timeline-id"/>
		<xsl:value-of select="$timeline-positions/descendant::*:item[@id = $timeline-id]/@position"/>
	</xsl:function>

	<!-- the word nodes -->
	<xsl:variable name="words" select="//*:w"/>

	<!-- the event nodes -->
	<xsl:variable name="events" select="//*:event"/>

	<!-- returns the word id for given annotation start -->
	<xsl:function name="tei:word-annotation-from" as="xs:string">
		<xsl:param name="annotationstart"/>
		<!-- we use the annotation, find the start and the end and find the word with the same start and end -->
		<!-- the case when there is no "normal" timeline id needs to be checked too -->
		<xsl:choose>
			<xsl:when test="($words[@s = $annotationstart]/@xml:id) != ''">
				<xsl:value-of select="$words[@s = $annotationstart]/@xml:id"/>
			</xsl:when>
			<!-- the corresponding word cannot be found - this happens if the annotation corresponds to an incident -->
			<xsl:when test="$events[@s = $annotationstart]/@xml:id != ''">
				<xsl:value-of select="$events[@s = $annotationstart]/@xml:id"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- the corresponding word cannot be found and it's not an incident either - there is an error in the exb-->
				<xsl:message terminate="no">
					<!-- Error message -->
                                    there is mismatch of annotation in the exb file, that happens after the segmentation
                                    - probably a missing whitespace after a word
                                </xsl:message>
				<xsl:value-of select="'broken'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- returns the word id for given annotation end -->
	<xsl:function name="tei:word-annotation-to" as="xs:string">
		<xsl:param name="annotationend"/>
		<!-- we use the annotation, find the start and the end and find the word with the same start and end -->
		<!-- the case when there is no "normal" timeline id needs to be checked too -->
		<xsl:choose>
			<xsl:when test="($words[@e = $annotationend]/@xml:id) != ''">
				<xsl:value-of select="$words[@e = $annotationend]/@xml:id"/>
			</xsl:when>
			<!-- the corresponding word cannot be found - this happens if the annotation corresponds to an incident -->
			<xsl:when test="($events[@e = $annotationend]/@xml:id) != ''">
				<xsl:value-of select="$events[@e = $annotationend]/@xml:id"/>
			</xsl:when>
			<!-- the corresponding word cannot be found and it's not an incident either - there is an error in the exb-->
			<xsl:otherwise>
				<xsl:value-of select="'broken'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- splits concatenated morphemes to after INEL rules -->
	<!-- the rules are: split at "-" and "." -->
	<!-- <xsl:function name="tei:splitmorphemes" as="element()">
		<xsl:param name="text"/>
		<xsl:value-of select="$timeline-positions/descendant::*:item[@id = $timeline-id]/@position"/> 
	</xsl:function>-->

	<!-- ***************************** -->

	<!-- root template: copy everything if no other instruction is found -->
	<xsl:template match="/ | @* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- body template: sort u, event, pauses and apply templates -->
	<xsl:template match="*:body">
		<xsl:element name="body" xmlns="http://www.tei-c.org/ns/1.0">
			<xsl:apply-templates select="*:u | *:event | *:pause">
				<xsl:sort select="tesla:timeline-position(@start)" data-type="number"/>
				<xsl:sort select="tesla:timeline-position(@end)" data-type="number"/>
			</xsl:apply-templates>
		</xsl:element>
	</xsl:template>

	<!-- events -->
	<xsl:template match="*:event">
		<xsl:element name="incident">
			<!-- add an id for references -->
			<xsl:attribute name="xml:id"/>
			<xsl:copy-of select="@start"/>
			<xsl:copy-of select="@end"/>
			<xsl:copy-of select="@who"/>
			<xsl:element name="desc">
				<xsl:value-of select="@desc"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<!-- u elements -->
	<xsl:template match="*:u">
		<!-- change 03-03-2016: element renamed, namespace switch no longer necessary -->
		<xsl:element name="annotationBlock" xmlns="http://www.tei-c.org/ns/1.0">
			<xsl:attribute name="who">
				<xsl:value-of select="$XPOINTER_HASH"/>
				<xsl:value-of select="@who"/>
			</xsl:attribute>
			<xsl:attribute name="start">
				<xsl:value-of select="$XPOINTER_HASH"/>
				<xsl:value-of select="@start"/>
			</xsl:attribute>
			<xsl:attribute name="end">
				<xsl:value-of select="$XPOINTER_HASH"/>
				<xsl:value-of select="@end"/>
			</xsl:attribute>
			<xsl:attribute name="xml:id">
				<xsl:text>au</xsl:text>
				<xsl:value-of select="count(preceding::*:u) + 1"/>
			</xsl:attribute>
			<xsl:element name="u" xmlns="http://www.tei-c.org/ns/1.0">
				<xsl:attribute name="xml:id">
					<xsl:text>u</xsl:text>
					<xsl:value-of select="count(preceding::*:u) + 1"/>
				</xsl:attribute>
				<xsl:apply-templates select="child::*[not(self::*:annotations)]"/>
			</xsl:element>
			<!-- this needs to be changed for INEL -->
			<xsl:for-each-group select="*:annotations/*:annotation" group-by="@level">
				<xsl:element name="spanGrp" xmlns="http://www.tei-c.org/ns/1.0">
					<xsl:attribute name="type">
						<xsl:value-of select="current-grouping-key()"/>
					</xsl:attribute>
					<xsl:for-each select="current-group()">
						<xsl:choose>
							<!-- here needs to be every tier that has word (or smaller) based annotations -->
							<xsl:when test="@level = ('mb', 'mp', 'ge', 'gg', 'gr', 'mc', 'hn', 'ps', 'SeR', 'SyF', 'IST', 'BOR', 'BOR-Phon', 'BOR-Morph', 'CS')">
								<xsl:element name="span">
									<!-- this needs to be changed for INEL -->
									<xsl:attribute name="from">
										<!-- <xsl:value-of select="$XPOINTER_HASH"/>
									<xsl:value-of select="@start"/> -->
										<xsl:value-of select="tei:word-annotation-from(@start)"/>
									</xsl:attribute>
									<xsl:attribute name="to">
										<!--<xsl:value-of select="$XPOINTER_HASH"/>
									<xsl:value-of select="@end"/> -->
										<xsl:value-of select="tei:word-annotation-to(@end)"/>
									</xsl:attribute>
									<!-- the further morpheme based segmentation and references here -->
									<xsl:choose>
										<xsl:when test="@level = ('mb')">
											<!-- this needs to be changed for INEL -->
											<!-- !!! here we split the morphemes and correspond the matching annotations -->
											<xsl:variable name="mbValue" select="./@value"/>
											<xsl:variable name="position">
												<xsl:value-of select="(count(preceding-sibling::*[@level = 'mb' and @value != '']/tokenize(@value, '-')))+1"/>
											</xsl:variable>
											<xsl:variable name="tokenizedMb" select="tokenize($mbValue, '-')"/>
											<xsl:for-each select="$tokenizedMb">
												<xsl:variable name="realposition">
													<xsl:value-of select="($position) + (position()-1)"/>
												</xsl:variable>
												<xsl:element name="span">
													<xsl:attribute name="xml:id">
														<xsl:value-of select="concat('m', $realposition)"/>
													</xsl:attribute>
													<xsl:value-of select="."/>
												</xsl:element>
												<!-- 
													<xsl:attribute name="from">
														 <xsl:value-of select="$XPOINTER_HASH"/>
									<xsl:value-of select="@start"/> -->
												<!--<xsl:value-of select="tei:word-annotation-from(@start)"/> 
													</xsl:attribute> -->
												<!-- <xsl:attribute name="to">
													<xsl:value-of select="$XPOINTER_HASH"/>
									<xsl:value-of select="@end"/> -->
												<!--	<xsl:value-of select="tei:word-annotation-to(@end)"/>
													</xsl:attribute> -->
												<!--  <xsl:attribute name="id">
														<xsl:value-of select="$XPOINTER_HASH"/>
									<xsl:value-of select="@end"/>
														<xsl:value-of select="m"/>
													</xsl:attribute>  -->
												<!-- the further morpheme based segmentation and references needs to be placed here -->
											</xsl:for-each>
										</xsl:when>
										<xsl:when test="@level = ('mp', 'ge', 'gg', 'gr', 'mc')">
											<xsl:variable name="mAnnoValue" select="./@value"/>
											<xsl:variable name="tokenizedmAnno" select="tokenize($mAnnoValue, '-')"/>
											<xsl:for-each select="$tokenizedmAnno">
												<xsl:element name="span">
													<xsl:value-of select="."/>
												</xsl:element>
											</xsl:for-each>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="@value"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:element name="span">
									<xsl:attribute name="from">
										<xsl:value-of select="$XPOINTER_HASH"/>
										<xsl:value-of select="@start"/>
									</xsl:attribute>
									<xsl:attribute name="to">
										<xsl:value-of select="$XPOINTER_HASH"/>
										<xsl:value-of select="@end"/>
									</xsl:attribute>
									<xsl:value-of select="@value"/>
								</xsl:element>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each-group>
		</xsl:element>
	</xsl:template>

	<!-- seg whose last child is an anchor -->
	<xsl:template match="//*:seg[@type = 'utterance' and name(child::*[last()]) = 'anchor']">
		<xsl:element name="seg">
			<xsl:attribute name="xml:id">
				<xsl:text>seg</xsl:text>
				<xsl:value-of select="count(preceding::*:seg[@type = 'utterance'])"/>
			</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:element>
		<xsl:element name="anchor">
			<xsl:attribute name="synch">
				<xsl:value-of select="$XPOINTER_HASH"/>
				<xsl:value-of select="child::*[last()]/@synch"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- seg whose last child is NOT an anchor -->
	<xsl:template match="//*:seg[@type = 'utterance' and not(name(child::*[last()]) = 'anchor')]">
		<xsl:element name="seg">
			<xsl:attribute name="xml:id">
				<xsl:text>seg</xsl:text>
				<xsl:value-of select="count(preceding::*:seg[@type = 'utterance'])"/>
			</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*:seg/@type">
		<xsl:attribute name="type">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="*:seg/@mode">
		<xsl:attribute name="subtype">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>

	<!-- ************************************************************** -->
	<!-- templates for things between uncertain-start and uncertain-end -->
	<!-- ************************************************************** -->

	<xsl:template match="*:uncertain-start[following-sibling::*:uncertain-end]">
		<!-- removed uncertain 11-07-2018 -->
		<!-- <xsl:element name="unclear" xmlns="http://www.tei-c.org/ns/1.0"> -->
		<xsl:apply-templates select="following-sibling::*[count(preceding-sibling::*) &lt; count(current()/following-sibling::*:uncertain-end/preceding-sibling::*)]" mode="grab_em"/>
		<!-- </xsl:element> -->
	</xsl:template>

	<xsl:template match="*:w[not(self::*:uncertain-start) and preceding-sibling::*:uncertain-start and following-sibling::*:uncertain-end]" mode="grab_em">
		<xsl:element name="w">
			<!-- added 11-07-2018 -->
			<xsl:attribute name="xml:id">
				<xsl:value-of select="@xml:id"/>
			</xsl:attribute>
			<xsl:attribute name="type">uncertain</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:element>
	</xsl:template>

	<!-- added 12-03-2015 -->
	<xsl:template match="*:pc[not(self::*:uncertain-start) and preceding-sibling::*:uncertain-start and following-sibling::*:uncertain-end]" mode="grab_em">
		<xsl:element name="pc">
			<!-- added 11-07-2018 -->
			<xsl:attribute name="xml:id">
				<xsl:value-of select="@xml:id"/>
			</xsl:attribute>
			<xsl:attribute name="type">uncertain</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*[not(self::*:pause) and not(self::*:uncertain-start) and preceding-sibling::*:uncertain-start and following-sibling::*:uncertain-end]">
		<!-- do nothing if you encounter this while NOT in mode="grab_em"--> </xsl:template>

	<!-- ****************************************************************** -->
	<!-- end templates for things between uncertain-start and uncertain-end -->
	<!-- ****************************************************************** -->

	<xsl:template match="//*:seg[@type = 'utterance']/*:anchor[not(following-sibling::*)]">
		<!-- do not copy the last anchor? --> </xsl:template>

	<!-- matches a short pause followed by a short pause followed by something other than a short pause -->
	<!-- changed because there was ambiguity wrt short pause preceded by a short pause -->
	<xsl:template
		match="*:pause[@dur = 'short' and following-sibling::*[1][self::*:pause and @dur = 'short'] and following-sibling::*[2][not(self::*:pause)] and not(preceding-sibling::*[1][self::*:pause and @dur = 'short'])]">
		<xsl:element name="pause" xmlns="http://www.tei-c.org/ns/1.0">
			<xsl:attribute name="type">medium</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- matches a short pause followed by a short pause followed by a short pause-->
	<xsl:template match="*:pause[@dur = 'short' and following-sibling::*[1][self::*:pause and @dur = 'short'] and following-sibling::*[2][self::*:pause and @dur = 'short']]">
		<xsl:element name="pause" xmlns="http://www.tei-c.org/ns/1.0">
			<xsl:attribute name="type">long</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!--matches a short pause not followed or preceded by a short pause -->
	<xsl:template match="*:pause[@dur = 'short' and not(preceding-sibling::*[1][self::*:pause and @dur = 'short']) and not(following-sibling::*[1][self::*:pause and @dur = 'short'])]">
		<xsl:element name="pause" xmlns="http://www.tei-c.org/ns/1.0">
			<xsl:attribute name="type">short</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!--matches a short pause preceded by a short pause -->
	<xsl:template match="*:pause[@dur = 'short' and preceding-sibling::*[1][self::*:pause and @dur = 'short']]">
		<!-- do nothing --> </xsl:template>

	<!-- the remaining cases -->
	<xsl:template match="*:pause[@dur = 'micro' or @dur = 'medium' or @dur = 'long' and not(preceding-sibling::*[1][self::*:pause] or following-sibling::*[1][self::*:pause])]">
		<xsl:element name="pause" xmlns="http://www.tei-c.org/ns/1.0">
			<xsl:attribute name="type" select="@dur"/>
		</xsl:element>
	</xsl:template>



	<xsl:template match="*:u/@*">
		<!-- do nothing --> </xsl:template>

	<xsl:template match="*:uncertain-end">
		<!-- do nothing --> </xsl:template>

	<!--************************-->

	<xsl:template match="*:w">
		<xsl:element name="w">
			<xsl:attribute name="xml:id">
				<xsl:value-of select="@xml:id"/>
			</xsl:attribute>
			<xsl:if test="following-sibling::*[1][self::*:repair]">
				<xsl:attribute name="type">repair</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*:anchor">
		<xsl:element name="anchor">
			<xsl:attribute name="synch">
				<xsl:value-of select="$XPOINTER_HASH"/>
				<xsl:value-of select="@synch"/>
			</xsl:attribute>
			<!-- <xsl:apply-templates select="@synch"/> -->
		</xsl:element>
	</xsl:template>

	<xsl:template match="*:c">
		<xsl:element name="c">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*:repair">
		<!-- <xsl:element name="incident">
			<xsl:attribute name="type">repair</xsl:attribute>
		</xsl:element> --> </xsl:template>

	<xsl:template match="*:pause[not(@dur = 'micro' or @dur = 'short' or @dur = 'medium' or @dur = 'long')]">
		<xsl:element name="pause">
			<xsl:attribute name="dur">
				<xsl:text>PT</xsl:text>
				<xsl:value-of select="translate(@dur, ',s', '.S')"/>
			</xsl:attribute>
			<xsl:apply-templates select="@*[not(name() = 'dur')] | node()"/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
