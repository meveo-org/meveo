package org.meveo.model.scripts;

import org.junit.Test;

import static org.junit.Assert.fail;

public class TestFunctionUtils {

    @Test
    public void replaceWithCorrectCodeTest() {
        String jmxFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.0 r1840935\">\n" +
                "  <hashTree>\n" +
                "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"alibaba.connector.1\" enabled=\"true\">\n" +
                "      <stringProp name=\"TestPlan.comments\"></stringProp>\n" +
                "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n" +
                "      <boolProp name=\"TestPlan.tearDown_on_shutdown\">true</boolProp>\n" +
                "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n" +
                "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" enabled=\"true\">\n" +
                "        <collectionProp name=\"Arguments.arguments\"/>\n" +
                "      </elementProp>\n" +
                "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" +
                "    </TestPlan>\n" +
                "    <hashTree>\n" +
                "      <SetupThreadGroup guiclass=\"SetupThreadGroupGui\" testclass=\"SetupThreadGroup\" testname=\"Login and get proxies\" enabled=\"true\">\n" +
                "        <stringProp name=\"ThreadGroup.on_sample_error\">continue</stringProp>\n" +
                "        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\" guiclass=\"LoopControlPanel\" testclass=\"LoopController\" testname=\"Contrôleur Boucle\" enabled=\"true\">\n" +
                "          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n" +
                "          <stringProp name=\"LoopController.loops\">1</stringProp>\n" +
                "        </elementProp>\n" +
                "        <stringProp name=\"ThreadGroup.num_threads\">1</stringProp>\n" +
                "        <stringProp name=\"ThreadGroup.ramp_time\">1</stringProp>\n" +
                "        <boolProp name=\"ThreadGroup.scheduler\">false</boolProp>\n" +
                "        <stringProp name=\"ThreadGroup.duration\"></stringProp>\n" +
                "        <stringProp name=\"ThreadGroup.delay\"></stringProp>\n" +
                "      </SetupThreadGroup>\n" +
                "      <hashTree>\n" +
                "        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" testname=\"Login\" enabled=\"true\">\n" +
                "          <elementProp name=\"HTTPsampler.Arguments\" elementType=\"Arguments\" guiclass=\"HTTPArgumentsPanel\" testclass=\"Arguments\" testname=\"Variables pré-définies\" enabled=\"true\">\n" +
                "            <collectionProp name=\"Arguments.arguments\">\n" +
                "              <elementProp name=\"client_id\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">meveo-web</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">client_id</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"username\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">meveo.admin</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">username</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"password\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">meveo</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">password</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"grant_type\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">password</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">grant_type</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"client_secret\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">afe07e5a-68cb-4fb0-8b75-5b6053b07dc3</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">client_secret</stringProp>\n" +
                "              </elementProp>\n" +
                "            </collectionProp>\n" +
                "          </elementProp>\n" +
                "          <stringProp name=\"HTTPSampler.domain\">dev.webdrone.fr</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.port\">2096</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.protocol\">https</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.contentEncoding\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.path\">/auth/realms/investigation-core/protocol/openid-connect/token</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.method\">POST</stringProp>\n" +
                "          <boolProp name=\"HTTPSampler.follow_redirects\">true</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.auto_redirects\">false</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.use_keepalive\">true</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.DO_MULTIPART_POST\">false</boolProp>\n" +
                "          <stringProp name=\"HTTPSampler.embedded_url_re\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.connect_timeout\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.response_timeout\"></stringProp>\n" +
                "        </HTTPSamplerProxy>\n" +
                "        <hashTree>\n" +
                "          <JSONPostProcessor guiclass=\"JSONPostProcessorGui\" testclass=\"JSONPostProcessor\" testname=\"Extract token\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSONPostProcessor.referenceNames\">access_token</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.jsonPathExprs\">$.access_token</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.match_numbers\"></stringProp>\n" +
                "          </JSONPostProcessor>\n" +
                "          <hashTree/>\n" +
                "        </hashTree>\n" +
                "        <HeaderManager guiclass=\"HeaderPanel\" testclass=\"HeaderManager\" testname=\"Set bearer token\" enabled=\"true\">\n" +
                "          <collectionProp name=\"HeaderManager.headers\">\n" +
                "            <elementProp name=\"\" elementType=\"Header\">\n" +
                "              <stringProp name=\"Header.name\">Authorization</stringProp>\n" +
                "              <stringProp name=\"Header.value\">Bearer ${access_token}</stringProp>\n" +
                "            </elementProp>\n" +
                "          </collectionProp>\n" +
                "          <stringProp name=\"TestPlan.comments\">Set bearer token</stringProp>\n" +
                "        </HeaderManager>\n" +
                "        <hashTree/>\n" +
                "        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" testname=\"Get proxy\" enabled=\"true\">\n" +
                "          <elementProp name=\"HTTPsampler.Arguments\" elementType=\"Arguments\" guiclass=\"HTTPArgumentsPanel\" testclass=\"Arguments\" testname=\"Variables pré-définies\" enabled=\"true\">\n" +
                "            <collectionProp name=\"Arguments.arguments\">\n" +
                "              <elementProp name=\"country\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">false</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">FR</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">country</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"maxResults\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">false</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">1</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">maxResults</stringProp>\n" +
                "              </elementProp>\n" +
                "            </collectionProp>\n" +
                "          </elementProp>\n" +
                "          <stringProp name=\"HTTPSampler.domain\">dev.webdrone.fr</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.port\">2096</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.protocol\">https</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.contentEncoding\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.path\">/investigation-core/rest/proxies</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.method\">GET</stringProp>\n" +
                "          <boolProp name=\"HTTPSampler.follow_redirects\">true</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.auto_redirects\">false</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.use_keepalive\">true</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.DO_MULTIPART_POST\">false</boolProp>\n" +
                "          <stringProp name=\"HTTPSampler.embedded_url_re\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.connect_timeout\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.response_timeout\"></stringProp>\n" +
                "        </HTTPSamplerProxy>\n" +
                "        <hashTree>\n" +
                "          <JSONPostProcessor guiclass=\"JSONPostProcessorGui\" testclass=\"JSONPostProcessor\" testname=\"Proxy IP\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSONPostProcessor.referenceNames\">proxyIP</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.jsonPathExprs\">$.[0].ip</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.match_numbers\"></stringProp>\n" +
                "          </JSONPostProcessor>\n" +
                "          <hashTree/>\n" +
                "          <JSONPostProcessor guiclass=\"JSONPostProcessorGui\" testclass=\"JSONPostProcessor\" testname=\"Proxy port\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSONPostProcessor.referenceNames\">proxyPort</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.jsonPathExprs\">$.[0].port</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.match_numbers\"></stringProp>\n" +
                "          </JSONPostProcessor>\n" +
                "          <hashTree/>\n" +
                "          <JSONPostProcessor guiclass=\"JSONPostProcessorGui\" testclass=\"JSONPostProcessor\" testname=\"Proxy username\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSONPostProcessor.referenceNames\">proxyUsername</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.jsonPathExprs\">$.[0].credential.username</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.match_numbers\"></stringProp>\n" +
                "          </JSONPostProcessor>\n" +
                "          <hashTree/>\n" +
                "          <JSONPostProcessor guiclass=\"JSONPostProcessorGui\" testclass=\"JSONPostProcessor\" testname=\"Proxy password\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSONPostProcessor.referenceNames\">proxyPassword</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.jsonPathExprs\">$.[0].credential.password</stringProp>\n" +
                "            <stringProp name=\"JSONPostProcessor.match_numbers\"></stringProp>\n" +
                "          </JSONPostProcessor>\n" +
                "          <hashTree/>\n" +
                "        </hashTree>\n" +
                "        <BeanShellSampler guiclass=\"BeanShellSamplerGui\" testclass=\"BeanShellSampler\" testname=\"Set proxy\" enabled=\"true\">\n" +
                "          <stringProp name=\"BeanShellSampler.query\">${__setProperty(proxyIP,${proxyIP})}; ${__setProperty(proxyPort,${proxyPort})}; ${__setProperty(proxyUsername,${proxyUsername})}; ${__setProperty(proxyPassword,${proxyPassword})}</stringProp>\n" +
                "          <stringProp name=\"BeanShellSampler.filename\"></stringProp>\n" +
                "          <stringProp name=\"BeanShellSampler.parameters\"></stringProp>\n" +
                "          <boolProp name=\"BeanShellSampler.resetInterpreter\">false</boolProp>\n" +
                "        </BeanShellSampler>\n" +
                "        <hashTree/>\n" +
                "      </hashTree>\n" +
                "      <org.meveo.jmeter.threadgroup.model.MeveoThreadGroup guiclass=\"org.meveo.jmeter.threadgroup.gui.MeveoThreadGroupGui\" testclass=\"org.meveo.jmeter.threadgroup.model.MeveoThreadGroup\" testname=\"Meveo - Function Test\" enabled=\"true\">\n" +
                "        <intProp name=\"ThreadGroup.num_threads\">1</intProp>\n" +
                "        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\">\n" +
                "          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n" +
                "          <intProp name=\"LoopController.loops\">1</intProp>\n" +
                "        </elementProp>\n" +
                "        <stringProp name=\"functionCode\">alibaba.connector.1</stringProp>\n" +
                "        <stringProp name=\"periodicity\">Daily-One-Hour-AM</stringProp>\n" +
                "      </org.meveo.jmeter.threadgroup.model.MeveoThreadGroup>\n" +
                "      <hashTree>\n" +
                "        <org.meveo.jmeter.sampler.model.MeveoSampler guiclass=\"org.meveo.jmeter.sampler.gui.MeveoSamplerGui\" testclass=\"org.meveo.jmeter.sampler.model.MeveoSampler\" testname=\"Execute function\" enabled=\"true\">\n" +
                "          <elementProp name=\"arguments\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" enabled=\"true\">\n" +
                "            <collectionProp name=\"Arguments.arguments\">\n" +
                "              <elementProp name=\"TLDS\" elementType=\"Argument\">\n" +
                "                <stringProp name=\"Argument.name\">TLDS</stringProp>\n" +
                "                <stringProp name=\"Argument.value\">www</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"TLDS\" elementType=\"Argument\">\n" +
                "                <stringProp name=\"Argument.name\">TLDS</stringProp>\n" +
                "                <stringProp name=\"Argument.value\">french</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"proxy\" elementType=\"Argument\">\n" +
                "                <stringProp name=\"Argument.name\">proxy</stringProp>\n" +
                "                <stringProp name=\"Argument.value\">${__property(proxyIP)}</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"port\" elementType=\"Argument\">\n" +
                "                <stringProp name=\"Argument.name\">port</stringProp>\n" +
                "                <stringProp name=\"Argument.value\">${__property(proxyPort)}</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"login\" elementType=\"Argument\">\n" +
                "                <stringProp name=\"Argument.name\">login</stringProp>\n" +
                "                <stringProp name=\"Argument.value\">${__property(proxyUsername)}</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "              </elementProp>\n" +
                "              <elementProp name=\"password\" elementType=\"Argument\">\n" +
                "                <stringProp name=\"Argument.name\">password</stringProp>\n" +
                "                <stringProp name=\"Argument.value\">${__property(proxyPassword)}</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "              </elementProp>\n" +
                "            </collectionProp>\n" +
                "          </elementProp>\n" +
                "          <stringProp name=\"code\">alibaba.connector.1</stringProp>\n" +
                "        </org.meveo.jmeter.sampler.model.MeveoSampler>\n" +
                "        <hashTree>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"Exists Offer Title Test\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Offering&apos;)].properties.title</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\">.+</stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">true</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"Exists Offer OfferingTtype \" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Offering&apos;)].properties.offeringType</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\">(?i).*(SALE_OFFER).*</stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">true</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"Exists Offer websiteCategory\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Offering&apos;)].properties.websiteCategory</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\">.*</stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">true</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exists Offer externalId\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Offering&apos;)].properties.externalId</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\"></stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">false</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"Exists Product Name Test\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Product&apos;)].properties.name</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\">.+</stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">true</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"Exists Product categories Test\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Product&apos;)].properties.categories</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\">(?i).*All Industries.*</stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">true</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exists Product Brand Test---\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Product&apos;)].properties.brand</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\">(?i).+</stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">true</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exists Seller Name Test\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Profile&apos;)].properties.names</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\"></stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">false</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exists Seller ExternalID\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Profile&apos;)].properties.externalId</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\"></stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">false</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exists Seller directlinks--- \" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Profile&apos;)].properties.directlinks</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\"></stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">false</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exists Price Value Test\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.type==&apos;Price&apos;)].properties.value</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\"></stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">false</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"---Exist Price Currency Test ---\" enabled=\"true\">\n" +
                "            <stringProp name=\"JSON_PATH\">$..entityOrRelations[?(@.name==&apos;price&apos;)].properties.currency</stringProp>\n" +
                "            <stringProp name=\"EXPECTED_VALUE\"></stringProp>\n" +
                "            <boolProp name=\"JSONVALIDATION\">false</boolProp>\n" +
                "            <boolProp name=\"EXPECT_NULL\">false</boolProp>\n" +
                "            <boolProp name=\"INVERT\">false</boolProp>\n" +
                "            <boolProp name=\"ISREGEX\">true</boolProp>\n" +
                "          </JSONPathAssertion>\n" +
                "          <hashTree/>\n" +
                "        </hashTree>\n" +
                "      </hashTree>\n" +
                "      <ResultCollector guiclass=\"ViewResultsFullVisualizer\" testclass=\"ResultCollector\" testname=\"Arbre de résultats\" enabled=\"true\">\n" +
                "        <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n" +
                "        <objProp>\n" +
                "          <name>saveConfig</name>\n" +
                "          <value class=\"SampleSaveConfiguration\">\n" +
                "            <time>true</time>\n" +
                "            <latency>true</latency>\n" +
                "            <timestamp>true</timestamp>\n" +
                "            <success>true</success>\n" +
                "            <label>true</label>\n" +
                "            <code>true</code>\n" +
                "            <message>true</message>\n" +
                "            <threadName>true</threadName>\n" +
                "            <dataType>true</dataType>\n" +
                "            <encoding>false</encoding>\n" +
                "            <assertions>true</assertions>\n" +
                "            <subresults>true</subresults>\n" +
                "            <responseData>false</responseData>\n" +
                "            <samplerData>false</samplerData>\n" +
                "            <xml>false</xml>\n" +
                "            <fieldNames>true</fieldNames>\n" +
                "            <responseHeaders>false</responseHeaders>\n" +
                "            <requestHeaders>false</requestHeaders>\n" +
                "            <responseDataOnError>false</responseDataOnError>\n" +
                "            <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>\n" +
                "            <assertionsResultsToSave>0</assertionsResultsToSave>\n" +
                "            <bytes>true</bytes>\n" +
                "            <sentBytes>true</sentBytes>\n" +
                "            <url>true</url>\n" +
                "            <threadCounts>true</threadCounts>\n" +
                "            <idleTime>true</idleTime>\n" +
                "            <connectTime>true</connectTime>\n" +
                "          </value>\n" +
                "        </objProp>\n" +
                "        <stringProp name=\"filename\"></stringProp>\n" +
                "      </ResultCollector>\n" +
                "      <hashTree/>\n" +
                "    </hashTree>\n" +
                "  </hashTree>\n" +
                "</jmeterTestPlan>\n";

        String result = FunctionUtils.replaceWithCorrectCode(jmxFile, "alibaba.connector.2");
        String checkResult = FunctionUtils.checkTestSuite(result, "alibaba.connector.2");
        if(checkResult != null) {
            fail("Wrong attribute " + checkResult);
        }
    }

}