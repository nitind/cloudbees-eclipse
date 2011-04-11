<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <testsuites>
            <xsl:for-each select="testResult//suite">
                <testsuite>
                    <xsl:attribute name="name">
                        <xsl:value-of select="name"/>
                    </xsl:attribute>
                    <xsl:attribute name="time">
                        <xsl:value-of select="duration"/>
                    </xsl:attribute>
                    <!--xsl:attribute name="tests">
                        <xsl:value-of select="@NumberOfRunTests"/>
                    </xsl:attribute>
                    <xsl:attribute name="failures">
                        <xsl:value-of select="@NumberOfFailures"/>
                    </xsl:attribute>
                    <xsl:attribute name="errors">
                        <xsl:value-of select="@NumberOfErrors"/>
                    </xsl:attribute>
                    <xsl:attribute name="skipped">
                        <xsl:value-of select="@NumberOfIgnoredTests"/>
                    </xsl:attribute-->
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
							<xsl:attribute name="ignored">
                                <xsl:value-of select="skipped"/>
                            </xsl:attribute>
							<!-- PASSED, SKIPPED, FAILED, FIXED, REGRESSION -->
                            <xsl:if test="status='FAILED'">
                                <failure type="failed">
                                    <xsl:attribute name="message">
                                        <xsl:value-of select="errorDetails"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="errorStackTrace"/>
                                </failure>
                            </xsl:if>
                            <xsl:if test="status='REGRESSION'">
                                <failure type="regression">
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
        </testsuites>
    </xsl:template>
</xsl:stylesheet>