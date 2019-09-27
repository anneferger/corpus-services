/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for exb errors for HZSK repository purposes.
 *
 * @author Ozan Ozdemir <ozan.oezdemir@studium.uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.xml.sax.ErrorHandler;

/**
 * A class that can load basic transcription data and check for potential problems with HZSK
 * repository depositing.
 */

public class ExbSchemaChecker extends Checker implements CorpusFunction {

    final String EXB_DTD_CHECKER = "exb-dtd";

    /**
     * Validate an exb file with a DTD file.
     */
    
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
            stats.addException(je, "Unknown parsing error");
        } catch(JDOMException jdome) {
            stats.addException(jdome, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, "Reading/writing error");
        } catch (TransformerException ex) {
            stats.addException(ex, "Reading/writing error");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, "Reading/writing error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, "Reading/writing error");
        }
        return stats;
    }
    
    /**
    * Main functionality of the feature; validates an exb file with a DTD file.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException, TransformerException, ParserConfigurationException, XPathExpressionException{
        System.out.println("Checking the exb file against DTD...");
        String exbSchemaPath = new File("src\\test\\java\\de\\uni_hamburg\\corpora\\resources\\schemas\\exb_schema.xsd").getAbsolutePath();
        URL exbSchema = Paths.get(exbSchemaPath).toUri().toURL();//;new URL(exbSchemaPath);
        Source xmlStream = new StreamSource(TypeConverter.String2InputStream(cd.toSaveableString()));
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(exbSchema);
        Validator validator = schema.newValidator();
        ExbErrorReportGenerator eh = new ExbErrorReportGenerator(cd.getFilename());
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);
        return eh.getErrors();
    }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(EXB_DTD_CHECKER,
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
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaXsdChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}

