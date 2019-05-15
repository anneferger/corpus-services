/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class ComaOverviewGeneration extends Checker implements CorpusFunction {

    final String COMA_OVERVIEW = "coma-overview";

    @Override
    public Report check(CorpusData cd){
        Report r = new Report();
        
        try{

            // get the XSLT stylesheet
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream("/xsl/Output_metadata_summary.xsl"));

            // create XSLTransformer and set the parameters 
            XSLTransformer xt = new XSLTransformer();
        
            // perform XSLT transformation
            String result = xt.transform(cd.toSaveableString(), xsl);
            //get locatino to save new result
            URL overviewurl = new URL(cd.getParentURL(), "coma_overview.html");
            CorpusIO cio = new CorpusIO();
            //save it
            cio.write(result, overviewurl);
            //everything worked
            r.addCorrect(COMA_OVERVIEW, cd, "created html overview at " + overviewurl);
            

        } catch (TransformerConfigurationException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Transformer configuration error");
        } catch (TransformerException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Transformer error");
        } catch (MalformedURLException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Malformed URL error");
        } catch (IOException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Unknown input/output error");
        } catch (ParserConfigurationException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Unknown Parser error");
        } catch (SAXException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Unknown XML error");
        } catch (XPathExpressionException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Unknown XPath error");
        }
        
        return r;
        
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
       return check(cd);
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        Class cl1;   
        try {
            cl1 = Class.forName("de.uni_hamburg.corpora.ComaData");
             IsUsableFor.add(cl1);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
            return IsUsableFor;
    }

}
