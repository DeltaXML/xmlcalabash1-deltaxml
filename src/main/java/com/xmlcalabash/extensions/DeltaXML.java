package com.xmlcalabash.extensions;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.S9apiUtils;
import com.deltaxml.core.DXPConfiguration;
import com.deltaxml.core.PipelinedComparator;
import com.xmlcalabash.util.XProcURIResolver;
import net.sf.saxon.s9api.*;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 21, 2008
 * Time: 8:51:10 AM
 * To change this template use File | Settings | File Templates.
 */

@XMLCalabash(
        name = "cx:delta-xml",
        type = "{http://xmlcalabash.com/ns/extensions}delta-xml")

public class DeltaXML extends DefaultStep {
    private static final String library_xpl = "http://xmlcalabash.com/extension/steps/delta-xml.xpl";
    private static final String library_url = "/com/xmlcalabash/extensions/delta-xml/library.xpl";

    private ReadablePipe source = null;
    private ReadablePipe alternate = null;
    private ReadablePipe dxp = null;
    private WritablePipe result = null;

    public DeltaXML(XProcRuntime runtime, XAtomicStep step) {
        super(runtime,step);
    }

    public static boolean isAvailable() {
        try {
            Class<?> name = Class.forName("com.deltaxml.core.DXPConfiguration", false, DeltaXML.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public void setInput(String port, ReadablePipe pipe) {
        if ("source".equals(port)) {
            source = pipe;
        } else if ("alternate".equals(port)) {
            alternate = pipe;
        } else {
            dxp = pipe;
        }
    }

    public void setOutput(String port, WritablePipe pipe) {
        result = pipe;
    }

    public void reset() {
        result.resetWriter();
    }

    public void run() throws SaxonApiException {
        super.run();

        XdmNode docA = source.read();
        XdmNode docB = alternate.read();
        XdmNode dxpdoc = dxp.read();

        try {
            DXPConfiguration dxpconfig = new DXPConfiguration(S9apiUtils.xdmToInputSource(runtime, dxpdoc), null, false);
            PipelinedComparator comparator = dxpconfig.generate();

            // FIXME: Grotesque hackery!

            StringWriter sw = new StringWriter();
            Serializer serializer = runtime.getProcessor().newSerializer();
            serializer.setOutputWriter(sw);
            S9apiUtils.serialize(runtime, docA, serializer);

            String docAxml = sw.toString();

            sw = new StringWriter();
            serializer = runtime.getProcessor().newSerializer();
            serializer.setOutputWriter(sw);
            S9apiUtils.serialize(runtime, docB, serializer);

            String docBxml = sw.toString();

            StringBuffer buf = new StringBuffer();

            comparator.compare(docAxml, docBxml, buf);

            StringReader sr = new StringReader(buf.toString());
            XdmNode doc = runtime.parse(new InputSource(sr));
            result.write(doc);
        } catch (Exception e) {
            throw new XProcException(e);
        }
    }

    public static void configureStep(XProcRuntime runtime) {
        XProcURIResolver resolver = runtime.getResolver();
        URIResolver uriResolver = resolver.getUnderlyingURIResolver();
        URIResolver myResolver = new StepResolver(uriResolver);
        resolver.setUnderlyingURIResolver(myResolver);
    }

    private static class StepResolver implements URIResolver {
        Logger logger = LoggerFactory.getLogger(DeltaXML.class);
        URIResolver nextResolver = null;

        public StepResolver(URIResolver next) {
            nextResolver = next;
        }

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            try {
                URI baseURI = new URI(base);
                URI xpl = baseURI.resolve(href);
                if (library_xpl.equals(xpl.toASCIIString())) {
                    URL url = DeltaXML.class.getResource(library_url);
                    logger.debug("Reading library.xpl for cx:delta-xml from " + url);
                    InputStream s = DeltaXML.class.getResourceAsStream(library_url);
                    if (s != null) {
                        SAXSource source = new SAXSource(new InputSource(s));
                        return source;
                    } else {
                        logger.info("Failed to read " + library_url + " for cx:delta-xml");
                    }
                }
            } catch (URISyntaxException e) {
                // nevermind
            }

            if (nextResolver != null) {
                return nextResolver.resolve(href, base);
            } else {
                return null;
            }
        }
    }
}
