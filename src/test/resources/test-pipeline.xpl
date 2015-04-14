<p:declare-step version='1.0' name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:dx="http://www.deltaxml.com/ns/well-formed-delta-v1"
                exclude-inline-prefixes="c cx dx">
<p:output port="result"/>

<p:import href="../../../resources/library.xpl"/>

<cx:delta-xml>
  <p:input port="source">
    <p:document href="doc-a.xml"/>
  </p:input>
  <p:input port="alternate">
    <p:document href="doc-b.xml"/>
  </p:input>
  <p:input port="dxp">
    <p:inline>
      <comparatorPipeline description="XML Compare, output recombinable delta" id="raw">
        <outputProperties>
          <property name="indent" literalValue="no"/>
        </outputProperties>
        <outputFileExtension extension="xml"/>
        <comparatorFeatures>
          <feature name="http://deltaxml.com/api/feature/isFullDelta" literalValue="false"/>
          <feature name="http://deltaxml.com/api/feature/enhancedMatch1" literalValue="true"/>
        </comparatorFeatures>
      </comparatorPipeline>
    </p:inline>
  </p:input>
</cx:delta-xml>

<p:count>
  <p:input port="source" select="//para[@dx:deltaV2='A=B']"/>
</p:count>

<p:choose>
  <p:when test="/c:result = '1'">
    <p:identity>
      <p:input port="source">
        <p:inline><c:result>PASS</c:result></p:inline>
      </p:input>
    </p:identity>
  </p:when>
  <p:otherwise>
    <p:error code="FAIL">
      <p:input port="source">
        <p:inline><message>Did not find expected text.</message></p:inline>
      </p:input>
    </p:error>
  </p:otherwise>
</p:choose>

</p:declare-step>
