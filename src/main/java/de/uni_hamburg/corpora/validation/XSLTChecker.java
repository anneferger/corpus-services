/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;

/**
 *
 * @author Daniel Jettka, daniel.jettka@uni-hamburg.de
 */
public class XSLTChecker extends Checker implements CorpusFunction {

    String xslresource = "/xsl/nslc-checks.xsl";
    String xc = "XSLTChecker";

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report fix(Collection<CorpusData> cdc) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report execute(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {

        Report r = new Report();

        try {

            // get the XSLT stylesheet
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(xslresource));

            // create XSLTransformer and set the parameters 
            XSLTransformer xt = new XSLTransformer();

            // perform XSLT transformation
            String result = xt.transform(cd.toSaveableString(), xsl);

            //read lines and add to Report
            Scanner scanner = new Scanner(result);

            int i = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                //split line by ;
                String[] lineParts = line.split(";", -1);

                switch (lineParts[0].toUpperCase()) {
                    case "WARNING":
                        r.addWarning(xc, cd, lineParts[1]);
                        exmaError.addError("XSLTChecker", cd.getURL().getFile(), lineParts[2], lineParts[3], false, lineParts[1]);
                        break;
                    case "CRITICAL":
                        r.addCritical(xc, cd, lineParts[1]);
                        exmaError.addError("XSLTChecker", cd.getURL().getFile(), lineParts[2], lineParts[3], false, lineParts[1]);
                        break;
                    case "NOTE":
                        r.addNote(xc, cd, lineParts[1]);
                        break;
                    case "MISSING":
                        r.addMissing(xc, cd, lineParts[1]);
                        exmaError.addError("XSLTChecker", cd.getURL().getFile(), lineParts[2], lineParts[3], false, lineParts[1]);
                        break;
                    default:
                        r.addCritical(xc, cd, "(Unrecognized report type): " + lineParts[1]);
                        exmaError.addError("XSLTChecker", cd.getURL().getFile(), lineParts[2], lineParts[3], false, lineParts[1]);
                }

                i++;
            }

            scanner.close();

        } catch (TransformerConfigurationException ex) {
            report.addException(ex, xc, cd, "unknown tranformation configuration error");
        } catch (TransformerException ex) {
            report.addException(ex, xc, cd, "unknown tranformation error");
        }

        return r;

    }


    public void setXSLresource(String s) {
        xslresource = s;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            Class cl1 = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl1);
            //Class cl2 = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            //IsUsableFor.add(cl2);
            //Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");
            //IsUsableFor.add(cl3);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }
}
