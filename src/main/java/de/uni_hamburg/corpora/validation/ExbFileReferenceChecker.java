/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface for checking errors in
 * exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A validator for EXB-file's references.
 */
public class ExbFileReferenceChecker extends Checker implements CommandLineable, CorpusFunction {

    ValidatorSettings settings;

    final String EXB_REFS = "exb-referenced-file";

    String exbName = "";

    File exbFile;

    CorpusData cd;

    /**
     * Check for referenced-files.
     */
    public Report check(File f) {
        Report stats = new Report();
        try {
            exbName = f.getName();
            stats = exceptionalCheck(f);
        } catch (IOException ioe) {
            stats.addException(ioe, EXB_REFS, cd, "Reading error");
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, EXB_REFS, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, EXB_REFS, cd, "Unknown parsing error");
        }
        return stats;
    }

    public Report exceptionalCheck(File f)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        int reffilesFound = 0;
        int reffilesMissing = 0;
        Report stats = new Report();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element) reffiles.item(i);
            String url = reffile.getAttribute("url");
            if(!url.isEmpty()){
            if (url.startsWith("file:///C:") || url.startsWith("file:/C:")) {
                stats.addCritical(EXB_REFS, cd, "Referenced-file " + url + " points to absolute local path, fix it to relative first");
            }
            File justFile = new File(url);
            boolean found = false;
            if (justFile.exists()) {
                found = true;
            }
            //the absolute path needs to be true - not just the filename and the local path
            String relfilename = url;
            /*if (url.lastIndexOf("/") != -1) {
                relfilename = url.substring(url.lastIndexOf("/"));
            }*/
            String referencePath = f.getParentFile().getCanonicalPath();
            String absPath = referencePath + File.separator + relfilename;
            File absFile = new File(absPath);
            if (absFile.exists()) {
                found = true;
            }
            if (!found) {
                reffilesMissing++;
                stats.addCritical(EXB_REFS, cd,
                        "File in referenced-file NOT found: " + url);
                exmaError.addError(EXB_REFS, f.getName(), "", "", false, "Error: File in referenced-file NOT found: " + url);
            } else {
                reffilesFound++;
                stats.addCorrect(EXB_REFS, cd, "File in referenced-file was found: " + url);
            }
            } else{
                stats.addCorrect(EXB_REFS, cd, "No file was referenced in this transcription");
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ExbFileReferenceChecker",
                "Checks Exmaralda .exb file for file references that do not "
                + "exist", "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for references...");
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
        ExbFileReferenceChecker checker = new ExbFileReferenceChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        this.cd = cd;
        try {
            exbName = cd.getURL().toString().substring(cd.getURL().toString().lastIndexOf("/") + 1);
            stats = exceptionalCheck(cd);
        } catch (IOException ioe) {
            stats.addException(ioe, "Reading error");
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (TransformerException ex) {
            stats.addException(ex, "Unknown parsing error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, "Unknown parsing error");
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks Exmaralda .exb file for file
     * references, if a referenced file does not exist, issues a warning.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        if (cd.getURL().toString().contains("file:/")) {
            exbFile = new File(cd.getURL().toString().substring(cd.getURL().toString().indexOf("file:/") + 6));
        } else {
            exbFile = new File(cd.getURL().toString());
        }
        int reffilesFound = 0;
        int reffilesMissing = 0;
        Report stats = new Report();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element) reffiles.item(i);
            String url = reffile.getAttribute("url");
            if (url.startsWith("file:///C:") || url.startsWith("file:/C:")) {
                stats.addCritical(EXB_REFS, cd, "Referenced-file " + url
                        + " points to absolute local path, fix to relative path first");
            }
            File justFile = new File(url);
            boolean found = false;
            if (justFile.exists()) {
                found = true;
            }
            //the absolute path needs to be true - nut just the filename and the local path
            String relfilename = url;
            /*if (url.lastIndexOf("/") != -1) {
                relfilename = url.substring(url.lastIndexOf("/"));
            }*/
            String referencePath = exbFile.getParentFile().getCanonicalPath();
            String absPath = referencePath + File.separator + relfilename;
            File absFile = new File(absPath);
            if (absFile.exists()) {
                found = true;
            }
            if (!found) {
                reffilesMissing++;
                stats.addCritical(EXB_REFS, cd, "File in referenced-file NOT found: " + url);
                exmaError.addError(EXB_REFS, cd.getURL().getFile(), "", "", false, "Error: File in referenced-file NOT found: " + url);
            } else {
                reffilesFound++;
                stats.addCorrect(EXB_REFS, cd, "File in referenced-file was found: " + url);
            }
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        this.cd = cd;
        report.addCritical(EXB_REFS,
                "Automatic fix is not yet supported.");
        return report;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

}
