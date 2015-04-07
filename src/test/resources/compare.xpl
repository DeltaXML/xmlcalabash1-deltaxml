<p:declare-step version='1.0' name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:err="http://www.w3.org/ns/xproc-error">
<p:input port="source"/>
<p:input port="alternate"/>
<p:output port="result"/>

<p:declare-step type="cx:delta-xml">
  <p:input port="source"/>
  <p:input port="alternate"/>
  <p:input port="dxp"/>
  <p:output port="result"/>
</p:declare-step>

<cx:delta-xml>
  <p:input port="source">
    <p:pipe step="main" port="source"/>
  </p:input>
  <p:input port="alternate">
    <p:pipe step="main" port="alternate"/>
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
</p:declare-step>
