/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;



/**
 * This class validates the coma file with the respective XML schema.
 * 
 */
public class ComaXsdChecker extends Checker implements CorpusFunction {

    public ComaXsdChecker() {
    }

    /**
     * Validate a coma file with XML schema from internet.
     *
     * @return true, if file is passable (valid enough for HZSK),
     *         false otherwise.
     */
    public Report check(File f) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(f);
        } catch(SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error.");
        } catch(IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown reading error.");
        }
        return stats;
    }


    private Report exceptionalCheck(File f)
            throws SAXException, IOException {
        URL COMA_XSD = new URL("http://www.exmaralda.org/xml/comacorpus.xsd");
        Source xmlStream = new StreamSource(f);
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(COMA_XSD);
        Validator validator = schema.newValidator();
        ComaErrorReportGenerator eh = new ComaErrorReportGenerator();
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);
        return eh.getErrors();
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ComaXSDChecker",
                "Checks Exmaralda .coma file against XML Schema",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files against schema...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            stats = check(f);
        }
        return stats;
    }

    public static void main(String[] args) {
        ComaXsdChecker checker = new ComaXsdChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }
    
    /**
    * Default check function which calls the exceptionalCheck function so that the
    * primal functionality of the feature can be implemented, and additionally 
    * checks for exceptions.
    */   
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch(JexmaraldaException je) {
            stats.addException(je, function, cd, "Unknown parsing error");
        } catch(JDOMException jdome) {
            stats.addException(jdome, function, cd, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, function, cd, "Reading/writing error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Reading/writing error");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, function, cd, "Reading/writing error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Reading/writing error");
        }
        return stats;
    }
    
    /**
    * Main functionality of the feature; validates a coma file with XML schema from internet.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException, TransformerException, ParserConfigurationException, XPathExpressionException{
        System.out.println("Checking COMA file against schema...");
        URL COMA_XSD = new URL("http://www.exmaralda.org/xml/comacorpus.xsd");
        Source xmlStream = new StreamSource(TypeConverter.String2InputStream(cd.toSaveableString()));
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(COMA_XSD);
        Validator validator = schema.newValidator();
        ComaErrorReportGenerator eh = new ComaErrorReportGenerator();
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);
        return eh.getErrors();
    }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(function,
                "No fix is applicable for this feature.");
        return report;
    }
    
    /**
    * Default function which determines for what type of files (basic transcription, 
    * segmented transcription, coma etc.) this feature can be used.
    */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class validates the coma file with the respective XML schema. ";
        return description;
    }

    @Override
    public Report check(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report function(CorpusData cd, Boolean fix) throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
