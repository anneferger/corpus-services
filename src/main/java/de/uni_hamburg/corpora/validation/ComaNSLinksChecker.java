/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.ComaData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class checks for existence of files linked in the 
 * coma file.
 */
public class ComaNSLinksChecker extends Checker implements CorpusFunction {

    String referencePath = "./";
    String comaLoc = "";
    String communicationname;

    public ComaNSLinksChecker() {
        super("coma-nslinks");
    }

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error.");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            stats.addException(ioe, function, cd, "Unknown file reading error.");
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            stats.addException(ex, function, cd, "Unknown file reading error.");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error.");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error.");
        }
        return stats;
    }

    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Document doc = TypeConverter.JdomDocument2W3cDocument(TypeConverter.String2JdomDocument(cd.toSaveableString()));
        NodeList nslinks = doc.getElementsByTagName("NSLink");
        Report stats = new Report();
        ComaData cdcoma = (ComaData) cd;
        for (int i = 0; i < nslinks.getLength(); i++) {
            Element nslink = (Element) nslinks.item(i);
            Node communication = nslink.getParentNode();
            if (communication.getNodeName() != null && communication.getNodeName().equals("Transcription")) {
                communicationname = communication.getParentNode().getAttributes().getNamedItem("Name").getTextContent();
            } else if (communication.getNodeName() != null && communication.getNodeName().equals("Media")) {
                communicationname = communication.getParentNode().getParentNode().getAttributes().getNamedItem("Name").getTextContent();
            } else {
                //could not find matching communication name
                communicationname = "Could not figure out Communication name";
            }
            NodeList nstexts = nslink.getChildNodes();
            for (int j = 0; j < nstexts.getLength(); j++) {
                Node maybeText = nstexts.item(j);
                if (maybeText.getNodeType() != Node.TEXT_NODE) {
                    System.err.print("This is not a text node: "
                            + maybeText);
                    continue;
                }
                Text nstext = (Text) nstexts.item(j);
                String nspath = nstext.getWholeText().replace("/", File.separator);
                File justFile = new File(nspath);
                boolean found = false;
                if (justFile.exists()) {
                    found = true;
                }
                String absPath = referencePath + File.separator + nspath;
                //System.out.println(absPath + "##############");
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if (cd.getURL() != null) {
                    URL urlPath = cd.getURL();
                    //I think here is the Linux Problem 
                    URL urlAbsPath = new URL(urlPath, nspath.replace(File.separator, "/"));
                    //System.out.println(urlPath + "##############");
                    File dataFile = new File(urlAbsPath.toURI());
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (cdcoma.getBasedirectory() != null) {
                    //File basedirectory = new File(cdcoma.getBasedirectory());
                    URI uri = cdcoma.getBasedirectory().toURI();
                    URI parentURI = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
                    String basePath
                            = Paths.get(parentURI).toString()
                            + File.separator + nspath;
                    File baseFile = new File(basePath);
                    if (baseFile.exists()) {
                        found = true;
                    }
                }
                if (!found) {
                    stats.addCritical(function, cd,
                            "In Communication: " + communicationname + " File in NSLink not found: " + nspath);
                } else {
                    stats.addCorrect(function, cd,
                            "File in NSLink was found: " + nspath);
                }
            }
        }
        NodeList relpathnodes = doc.getElementsByTagName("relPath");
        for (int i = 0; i < relpathnodes.getLength(); i++) {
            Element relpathnode = (Element) relpathnodes.item(i);
            NodeList reltexts = relpathnode.getChildNodes();
            for (int j = 0; j < reltexts.getLength(); j++) {
                Node maybeText = reltexts.item(j);
                Node communicationrel = maybeText.getParentNode().getParentNode();
                if (communicationrel.getNodeName() != null && communicationrel.getNodeName().equals("File") && communicationrel.getParentNode().hasAttributes() && communicationrel.getParentNode().getAttributes().getNamedItem("Name") != null) {
                    communicationname = communicationrel.getParentNode().getAttributes().getNamedItem("Name").getTextContent();
                } else {
                    //could not find matching communication name
                    communicationname = "Could not figure out Communication name";
                }
                if (maybeText.getNodeType() != Node.TEXT_NODE) {
                    System.err.print("This is not a text node: "
                            + maybeText);
                    continue;
                }
                Text reltext = (Text) reltexts.item(j);
                String relpath = reltext.getWholeText().replace("/", File.separator);
                File justFile = new File(relpath);
                boolean found = false;
                if (justFile.exists()) {
                    found = true;
                }
                String absPath = referencePath + File.separator + relpath;
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if (cd.getURL() != null) {
                    URL urlPath = cd.getURL();
                    URL urlRelPath = new URL(urlPath, relpath.replace("\\", "/"));
                    File dataFile = new File(urlRelPath.toURI());
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
               if (cdcoma.getBasedirectory() != null) {
                    URI uri = cdcoma.getBasedirectory().toURI();
                    URI parentURI = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
                    String basePath
                            = Paths.get(parentURI).toString()
                            + File.separator + relpath;
                    File baseFile = new File(basePath);
                    if (baseFile.exists()) {
                        found = true;
                    }
                }
                if (!found) {
                    stats.addCritical(function, cd,
                            "In Communication: " + communicationname + " File in relPath not found: " + relpath);
                } else {
                    stats.addCorrect(function, cd,
                            "File in relPath was found: " + relpath);
                }
            }
        }
        return stats;
    }
    

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(function, cd,
                "Wrong NS links cannot be fixed automatically");
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    @Override
    public Report execute(Corpus c) {
        for (CorpusData cd : c.getCorpusData()) {
            report.merge(check(cd));
        }
        return report;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks for existence of files linked in the "
                + "coma file.";
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
