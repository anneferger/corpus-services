package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author fsnv625
 */
class AnnotationSpecification implements CorpusData{
    Document jdom;
    URL url;

    public AnnotationSpecification(URL url) {
        try {
            this.url = url;
            SAXBuilder builder = new SAXBuilder();
            jdom = builder.build(url);
        } catch (JDOMException ex) {
            Logger.getLogger(CmdiData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CmdiData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String toSaveableString() {
        return toPrettyPrintedXML();
    }

    @Override
    public String toUnformattedString() {
        XMLOutputter xmOut = new XMLOutputter();
        return xmOut.outputString(jdom);
    }

    private String toPrettyPrintedXML() {
        String prettyCorpusData = indent(toUnformattedString(), "event");
        //String prettyCorpusData = indent(bt.toXML(bt.getTierFormatTable()), "event");
        return prettyCorpusData;
    }
}