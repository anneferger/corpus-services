package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The class that checks out that all annotations are from the annotation
 * specification file and that there are no annotations in the coma file not in
 * the annotation specification file.
 */
public class TierCheckerWithAnnotation extends Checker implements CorpusFunction {

    String comaLoc = "";
    HashMap<String, Collection<String>> annotationsInComa; // list for holding annotations of coma file
    ArrayList<String> annotations; // list for holding annotations of annotation spec file
    int counter = 0; // counter for controlling whether we are on coma or annotation spec file

    /**
     * Add annotations to the corresponding array from coma and annotation
     * specification file.
     */
    public void addAnnotations(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        if (cd.getURL().toString().endsWith(".coma")) {
            NodeList communications = doc.getElementsByTagName("Communication"); // divide by Communication tags
            annotationsInComa = new HashMap<String, Collection<String>>();
            for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
                Element communication = (Element) communications.item(i);
                NodeList transcriptions = communication.getElementsByTagName("Transcription"); // get transcriptions of current communication     
                for (int j = 0; j < transcriptions.getLength(); j++) {   // iterate through transcriptions 
                    Element transcription = (Element) transcriptions.item(j);
                    //Element name = (Element) transcription.getElementsByTagName("Name").item(0); //get the name of the file that has the transcription
                    String name = ((Element) transcription.getElementsByTagName("Name").item(0)).getTextContent(); //get the name of the file that has the transcription
                    NodeList keys = transcription.getElementsByTagName("Key");  // get keys of current transcription
                    boolean segmented = false;   // flag for distinguishing basic file from segmented file 
                    for (int k = 0; k < keys.getLength(); k++) {  // look for the key with "segmented" attribute 
                        Element key = (Element) keys.item(k);
                        if (key.getAttribute("Name").equals("segmented")) {
                            String seg = key.getTextContent();
                            if (seg.equals("true")) // check if transcription is segmented or not
                            {
                                segmented = true;        // if segmented transcription then turn the flag true
                            }
                            break;
                        }
                    }
                    if (segmented) { // get the names of the segmentation algorithms in the coma file
                        for (int k = 0; k < keys.getLength(); k++) {  // look for the keys with algorithm 
                            Element key = (Element) keys.item(k);
                            if (key.getAttribute("Name").contains("Annotation type:")) {
                                int colonIndex = key.getAttribute("Name").lastIndexOf(':');
                                if (annotationsInComa.containsKey(name)) {
                                    if (!annotationsInComa.get(name).contains(key.getAttribute("Name").substring(colonIndex + 2))) {
                                        Collection<String> c = annotationsInComa.get(name);
                                        c.add(key.getAttribute("Name").substring(colonIndex + 2));
                                        annotationsInComa.put(name, c);
                                    }
                                } else {
                                    Collection<String> c = new ArrayList<String>();
                                    c.add(key.getAttribute("Name").substring(colonIndex + 2));
                                    annotationsInComa.put(name, c);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            annotations = new ArrayList<String>();
            NodeList tags = doc.getElementsByTagName("tag"); // divide by tags
            for (int i = 0; i < tags.getLength(); i++) { //iterate through tags
                Element tag = (Element) tags.item(i);
                annotations.add(tag.getAttribute("name"));
            }
        }
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            if (counter < 1) {    //first add annotations from coma or annotation spec file depending on which is read first
                addAnnotations(cd);
                counter++;
            } else {             //then add the second annotations and check it                    
                addAnnotations(cd);
                stats = exceptionalCheck(cd);
            }
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, comaLoc + ": Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, comaLoc + ": Unknown file reading error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; compares the coma file with the
     * corresponding annotation specification file whether or not there is a
     * conflict regarding the annotation tags that are used in the coma file.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        Report stats = new Report(); //create a new report

        for (Map.Entry<String, Collection<String>> entry : annotationsInComa.entrySet()) {
            String name = entry.getKey();
            Collection<String> annotTypes = entry.getValue();
            for (String annotType : annotTypes) {   // iterate through annotations in the coma file
                if (!annotations.contains(annotType)) { // check if annotations not present in annotation spec file
                    System.err.println("Coma file is containing annotation (" + annotType
                            + ") for " + name + " not specified by annotation spec file!");
                    stats.addWarning("tier-checker-with-annotation", "annotation error: annotation ("
                            + annotType + ") for " + name + " not specified!");
                    int index = cd.getURL().getFile().lastIndexOf("/");
                    String filePath = cd.getURL().getFile().substring(0, index) + "/" + name + "/" + name +".exb";
                    exmaError.addError("tier-checker-with-annotation", filePath, "", "", false, "annotation error: annotation ("
                            + annotType + ") for " + name + " not specified in the annotation specification file!");
                }
            }
        }

        return stats; // return the report with warnings
    }

    /**
     * This feature does not have fix functionality yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            Class clSecond = Class.forName("de.uni_hamburg.corpora.AnnotationSpecification");
            IsUsableFor.add(cl);
            IsUsableFor.add(clSecond);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TierCheckerWithAnnotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}
