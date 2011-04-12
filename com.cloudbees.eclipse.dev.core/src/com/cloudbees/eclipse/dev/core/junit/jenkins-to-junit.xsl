<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <testsuite name="Tests">
            <xsl:attribute name="time">
                <xsl:value-of select="testResult//duration"/>
            </xsl:attribute>
        
            <xsl:for-each select="testResult//suite">
                <testsuite>
                    <xsl:attribute name="name">
                        <xsl:value-of select="name"/>
                    </xsl:attribute>
                    <xsl:attribute name="time">
                        <xsl:value-of select="duration"/>
                    </xsl:attribute>
                    <xsl:for-each select="case">
                        <testcase>
                            <xsl:attribute name="classname">
                                <xsl:value-of select="className"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:value-of select="name"/>
                            </xsl:attribute>
                            <xsl:attribute name="time">
                                <xsl:value-of select="duration" />
                            </xsl:attribute>
                            <xsl:if test="translate(string(skipped),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ') = 'TRUE'">
    							<xsl:attribute name="ignored">
                                    <xsl:value-of select="skipped"/>
                                </xsl:attribute>
                            </xsl:if>
							<!-- PASSED, SKIPPED, FAILED, FIXED, REGRESSION -->
                            <xsl:if test="status = 'FAILED' or status = 'REGRESSION'">
                                <failure>
                                    <xsl:attribute name="type">
                                        <xsl:value-of select="status" />
                                    </xsl:attribute>
                                    <xsl:attribute name="message">
                                        <xsl:value-of select="errorDetails"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="errorStackTrace"/>
                                </failure>
                            </xsl:if>
                        </testcase>
                    </xsl:for-each>
                </testsuite>
            </xsl:for-each>
        </testsuite>
    </xsl:template>
</xsl:stylesheet>