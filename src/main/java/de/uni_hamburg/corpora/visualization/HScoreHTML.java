/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.TierFormatTable;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka
 */
public class HScoreHTML extends Visualizer {

    // resources loaded from directory supplied in pom.xml
    private static final String STYLESHEET_PATH = "/xsl/EXB2hScoreHTML.xsl";
    private final String SERVICE_NAME = "HScoreHTML";
    Report stats;
    URL targeturl;
    CorpusData cd;
    String corpusname = "";

    public HScoreHTML() {

    }

    public HScoreHTML(String btAsString) {
        createFromBasicTranscription(btAsString);
    }

    /**
     * This method deals performs the transformation of EXB to horizontal Score
     * HTML
     *
     * @param btAsString the EXB file represented in a String object
     * @return
     */
    public String createFromBasicTranscription(String btAsString) {

        basicTranscriptionString = btAsString;
        basicTranscription = TypeConverter.String2BasicTranscription(btAsString);

        String result = null;

        try {
            BasicTranscription bt = basicTranscription;
            bt.normalize();
            basicTranscriptionString = bt.toXML();
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(STYLESHEET_PATH));

            // perform XSLT transformation
            XSLTransformer xt = new XSLTransformer();
            xt.setParameter("EMAIL_ADDRESS", EMAIL_ADDRESS);
            xt.setParameter("WEBSERVICE_NAME", SERVICE_NAME);
            xt.setParameter("HZSK_WEBSITE", HZSK_WEBSITE);
            String referencedRecording = bt.getHead().getMetaInformation().getReferencedFile("wav");
            if (referencedRecording != null) {
                System.out.println("not null " + referencedRecording);
                xt.setParameter("RECORDING_PATH", referencedRecording);
                xt.setParameter("RECORDING_TYPE", "wav");
            }
            result = xt.transform(basicTranscriptionString, xsl);

        } catch (TransformerException ex) {
            Logger.getLogger(HScoreHTML.class.getName()).log(Level.SEVERE, null, ex);
        }

        setHTML(result);

        return result;
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Usage: " + HScoreHTML.class.getName()
                        + "EXB [HTML]");
                System.exit(1);
            } else {
                byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
                String btString = new String(encoded, "UTF-8");
                HScoreHTML score = new HScoreHTML(btString);
                if (args.length >= 2) {
                    PrintWriter htmlOut = new PrintWriter(args[1]);
                    htmlOut.print(score.getHTML());
                    htmlOut.close();
                } else {
                    System.out.println(score.getHTML());
                }
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public Report visualize(CorpusData cod) {
        try {
            cd = cod;
            stats = new Report();
            String result = createFromBasicTranscription(cd.toSaveableString());
            targeturl = new URL(cd.getParentURL() + cd.getFilenameWithoutFileEnding() + "_hscore.html");
            CorpusIO cio = new CorpusIO();
            cio.write(result, targeturl);
            stats.addCorrect(SERVICE_NAME, cd, "Visualization of file was successfully saved at " + targeturl);
        } catch (MalformedURLException ex) {
            stats.addException(SERVICE_NAME, ex, "Malformed URL used");
        } catch (IOException ex) {
            stats.addException(SERVICE_NAME, ex, "Input Output Exception");
        } catch (TransformerException ex) {
            stats.addException(SERVICE_NAME, ex, "Transformer Exception");
        } catch (ParserConfigurationException ex) {
            stats.addException(SERVICE_NAME, ex, "Parser Exception");
        } catch (SAXException ex) {
            stats.addException(SERVICE_NAME, ex, "XML Exception");
        } catch (XPathExpressionException ex) {
            stats.addException(SERVICE_NAME, ex, "XPath Exception");
        }
        return stats;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            stats.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    @Override
    public Report doMain(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Usage: " + ScoreHTML.class.getName()
                        + "EXB [HTML]");
                System.exit(1);
            } else {
                byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
                String btString = new String(encoded, "UTF-8");
                ScoreHTML score = new ScoreHTML(btString);
                if (args.length >= 2) {
                    PrintWriter htmlOut = new PrintWriter(args[1]);
                    htmlOut.print(score.getHTML());
                    htmlOut.close();
                } else {
                    System.out.println(score.getHTML());
                }
            }
        } catch (UnsupportedEncodingException uee) {
            stats.addException(SERVICE_NAME, uee, "encoding exception");
        } catch (IOException ioe) {
            stats.addException(SERVICE_NAME, ioe, "input output exception");
        }
        return stats;
    }

}
