/*
 * @file NgexmaraldaCorpusChecker.java
 *
 * Nganasan Spoken Language Corpus specific checkers.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.BasicBody;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;

/**
 * This is the check procedure for the Nganasan Corpus
 *
 * @author hanna
 */
public class NgexmaraldaCorpusChecker extends Checker implements CorpusFunction {

    private Element communication;
    private Element basTrans;
    private Element segTrans;
    private Element rec;
    private String comafilename;
    private File comafile;
    private String comadirname;
    final String NSLC = "nslc";

    public Report check() {
        Report stats = new Report();
        try {
            stats = exceptionalCheck();
            stats.merge(requireObligatoryAnnotationTiersAndTypes());
        } catch (JexmaraldaException je) {
            stats.addException(je, "Unknown parsing error");
        } catch (JDOMException jdome) {
            stats.addException(jdome, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, "Reading error");
        }
        return stats;
    }

    public Report exceptionalCheck() throws JDOMException,
            IOException {
        Report stats = new Report();
        Document nganasanCorpus
                = org.exmaralda.common.jdomutilities.IOUtilities.readDocumentFromLocalFile(comafilename);
        XPath xpCommunications = XPath.newInstance("//Communication");
        List allCommunications = xpCommunications.selectNodes(nganasanCorpus);
        for (Object o : allCommunications) {
            communication = (Element) o;
            //retrieve the communication name
            String communicationName = communication.getAttributeValue("Name");
            //pick up basic transcriptions
            XPath xpBasTrans = XPath.newInstance("Transcription[Description"
                    + "/Key[@Name='segmented']/text()='false']");
            List allBasTrans = xpBasTrans.selectNodes(communication);
            for (Object oB : allBasTrans) {
                basTrans = (Element) oB;
                String relPath = basTrans.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    stats.addCritical(NSLC,
                            "Basic transcription file doesn't exist at "
                            + "NSLink for " + communicationName);
                } else if (Paths.get(relPath).isAbsolute()) {
                    stats.addCritical(NSLC,
                            "Basic transcription NSLink is absolute for "
                            + communicationName);
                } else if (!relPath.endsWith(communicationName + ".exb")) {
                    stats.addCritical(NSLC,
                            "Wrong basic transcription NSLink for "
                            + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Basic transcription NSLink OK: "
                            + communicationName);
                }
                String basTransName = basTrans.getChildText("Name");
                if (!basTransName.equals(communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong basic transcription name for "
                            + communicationName, basTransName + " should be "
                            + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Basic transcription name OK for "
                            + communicationName + ": " + basTransName);
                }
                if (!basTrans.getChildText("Filename").equals(
                        communicationName + ".exb")) {
                    stats.addCritical(NSLC,
                            "Wrong basic transcripton filename for "
                            + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Correct Filename for basic transcription "
                            + communicationName);
                }
            }
            XPath xpSegTrans = XPath.newInstance(
                    "Transcription[Description"
                    + "/Key[@Name='segmented']/text()='true']");
            List allSegTrans = xpSegTrans.selectNodes(communication);
            for (Object oS : allSegTrans) {
                segTrans = (Element) oS;
                String relPath = segTrans.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    stats.addCritical(NSLC,
                            "Segmented transcription file doesn't exist at"
                            + " NSLink for " + communicationName);
                } else if (Paths.get("relPath").isAbsolute()) {
                    stats.addCritical(NSLC,
                            "Segmented transcription NSLink is absolute for "
                            + communicationName);
                } else if (!relPath.endsWith(communicationName + "_s.exs")) {
                    stats.addCritical(NSLC,
                            "Wrong segmented transcription NSLink for "
                            + communicationName, relPath + " should end in "
                            + communicationName + "_s.exs");
                } else {
                    stats.addCorrect(NSLC,
                            "Correct segmented transcription NSLink for "
                            + communicationName);
                }
                String segTransName = segTrans.getChildText("Name");
                if (!segTransName.equals(communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong segmented transcription name for "
                            + communicationName, segTransName + " should be "
                            + communicationName);
                } else if (!segTrans.getChildText("Filename").equals(
                        communicationName + "_s.exs")) {
                    stats.addCritical(NSLC,
                            "Wrong segmented transcription filename for "
                            + communicationName,
                            segTrans.getChildText("Filename")
                            + " should be " + communicationName + "_s.exs");
                } else {
                    stats.addCorrect(NSLC,
                            "Correct Filename for segmented transcription "
                            + communicationName);
                }
            }
            XPath xpRec = XPath.newInstance("Recording/Media");
            List allRec = xpRec.selectNodes(communication);
            for (Object oR : allRec) {
                Element media = (Element) oR;
                rec = media.getParentElement();
                String relPath = media.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    stats.addCritical(NSLC,
                            "Recording file doesn't exist at NSLink for "
                            + communicationName);
                } else if (Paths.get("relPath").isAbsolute()) {
                    stats.addCritical(NSLC,
                            "Recording NSLink is absolute for "
                            + communicationName);
                } else if (!StringUtils.substringBefore(relPath, ".").endsWith(
                        communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong recording NSLink for "
                            + communicationName,
                            StringUtils.substringBefore(relPath, ".")
                            + " should end with " + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Recording NSLink is correct for "
                            + communicationName);
                }
                String recName = rec.getChildText("Name");
                if (!recName.equals(communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong recording name for " + communicationName,
                            recName + " should be " + communicationName);
                }
            }
        }
        return stats;
    }

    /**
     * Checks that NSLC transcripts have required annotation tiers. Uses the
     * list of known abbreviations from annotation guidelines. Checks for
     * existence of those marked obligatory, that type matches and also that no
     * unexpected tiers are there.
     *
     * <table>
     * <thead>
     * <tr>
     * <th>TIERS</th>
     * <th>Comments</th>
     * <th>Type</th>
     * <th>Category</th>
     * </tr>
     * </thead><tbody>
     * <tr>
     * <td>ref</td>
     * <td>Name of the communication</td>
     * <td><code>d</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>st</td>
     * <td>Source texts: normally in Cyrillic transliteration</td>
     * <td><code>d</code></td>
     * <td>optional</td>
     * </tr><tr>
     * <td>ts</td>
     * <td>Transcription (what is heard)</td>
     * <td><code>d</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>tx</td>
     * <td>Tier for interlinearization</td>
     * <td>transcription</td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>mb</td>
     * <td>Morpheme break</td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>mp</td>
     * <td>Morphophonemes, underlying forms</td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>gr</td>
     * <td>Morphological <code>a</code>: Russian gloss of each morpheme </td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>ge</td>
     * <td>Morphological <code>a</code>: English gloss of each morpheme</td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>mc</td>
     * <td>Part of speech of each morpheme</td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>ps</td>
     * <td>Part of speech of each word</td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>SeR</td>
     * <td>Annotation of semantic roles </td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>SyF</td>
     * <td>Annotation of syntactic function</td>
     * <td><code>a</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>IST</td>
     * <td>Annotation of information status </td>
     * <td><code>a</code></td>
     * <td>optional</td>
     * </tr><tr>
     * <td>CW</td>
     * <td>Annotation of code switching</td>
     * <td><code>a</code></td>
     * <td>optional</td>
     * </tr><tr>
     * <td>fr</td>
     * <td>Russian free translation</td>
     * <td><code>d</code></td>
     * <td>obligatory</td>
     * </tr><tr>
     * <td>fe</td>
     * <td>English free translation</td>
     * <td><code>d</code></td>
     * <td>optional</td>
     * </tr><tr>
     * <td>fg</td>
     * <td>German free translation</td>
     * <td><code>d</code></td>
     * <td>optional</td>
     * </tr><tr>
     * <td>nt</td>
     * <td>Notes on the text unit</td>
     * <td><code>d</code></td>
     * <td>optional</td>
     * </tr><tr>
     * </table>
     *
     */
    public Report requireObligatoryAnnotationTiersAndTypes() throws
            SAXException, JDOMException, IOException, JexmaraldaException {
        Map<String, String> obligatoryTiers = new HashMap<String, String>();
        Map<String, String> optionalTiers = new HashMap<String, String>();
        obligatoryTiers.put("ref", "Name of the communication");
        optionalTiers.put("st", "Source texts: normally in Cyrillic "
                + "transliteration");
        obligatoryTiers.put("ts", "Transcription (what is heard)");
        obligatoryTiers.put("tx", "Tier for interlinearization)");
        obligatoryTiers.put("mb", "Morpheme break");
        obligatoryTiers.put("mp", "Morphophonemes, underlying forms");
        obligatoryTiers.put("gr", "Morphological annotation: Russian gloss of "
                + "each morpheme");
        obligatoryTiers.put("ge", "Morphological annotation: Egnlish gloss of "
                + "each morpheme");
        obligatoryTiers.put("mc", "Part of speech of each morpheme");
        obligatoryTiers.put("ps", "Part of speech of each word");
        obligatoryTiers.put("SeR", "Annotation of semantic roles");
        obligatoryTiers.put("SyF", "Annotation of syntactic function");
        optionalTiers.put("IST", "Annotation of information status");
        optionalTiers.put("CW", "Annotation of code switching");
        obligatoryTiers.put("fr", "Russian free translation");
        optionalTiers.put("fe", "English free translation");
        optionalTiers.put("fh", "Hungarian free translation");
        optionalTiers.put("so", "Source origin");
        optionalTiers.put("fg", "German free translation");
        optionalTiers.put("nt", "Notes on the text unit");
        Map<String, String> tierTypes = new HashMap<String, String>();
        tierTypes.put("ref", "d");
        tierTypes.put("st", "d");
        tierTypes.put("ts", "d");
        tierTypes.put("tx", "t");
        tierTypes.put("mb", "a");
        tierTypes.put("mp", "a");
        tierTypes.put("gr", "a");
        tierTypes.put("ge", "a");
        tierTypes.put("mc", "a");
        tierTypes.put("ps", "a");
        tierTypes.put("SeR", "a");
        tierTypes.put("SyF", "a");
        tierTypes.put("IST", "a");
        tierTypes.put("CW", "a");
        tierTypes.put("fr", "d");
        tierTypes.put("fe", "d");
        tierTypes.put("fg", "d");
        tierTypes.put("nt", "d");
        tierTypes.put("so", "d");
        tierTypes.put("fh", "d");

        Report stats = new Report();
        Document nganasanCorpus
                = org.exmaralda.common.jdomutilities.IOUtilities.readDocumentFromLocalFile(comafilename);
        XPath xpCommunications = XPath.newInstance("//Communication");
        List allCommunications = xpCommunications.selectNodes(nganasanCorpus);
        Set<String> skipTiers = new HashSet<String>();
        skipTiers.add("COLUMN-LABEL");
        skipTiers.add("ROW-LABEL");
        skipTiers.add("SUB-ROW-LABEL");
        skipTiers.add("EMPTY");
        skipTiers.add("EMPTY-EDITOR");

        for (Object o : allCommunications) {
            communication = (Element) o;
            //retrieve the communication name
            String communicationName = communication.getAttributeValue("Name");
            //pick up basic transcriptions
            XPath xpBasTrans = XPath.newInstance("Transcription[Description"
                    + "/Key[@Name='segmented']/text()='false']");
            List allBasTrans = xpBasTrans.selectNodes(communication);
            for (Object oB : allBasTrans) {
                basTrans = (Element) oB;
                String relPath = basTrans.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    // we already checked validity of files in other checks
                    continue;
                }
                Set<String> obligatoriesSeen = new HashSet<String>();
                Set<String> optionalsSeen = new HashSet<String>();
                Element desc = basTrans.getChild("Description");
                BasicTranscription bt = new BasicTranscription(filePath);
                BasicBody bb = bt.getBody();
                String[] tierIDs = bb.getAllTierIDs();
                for (String tierID : tierIDs) {
                    if (skipTiers.contains(tierID)) {
                        stats.addNote(NSLC,
                                "Skipped a tier: " + tierID,
                                "This tier does not need to be included in "
                                + "coma file");
                        continue;
                    }
                    Tier tier = null;
                    try {
                        tier = bb.getTierWithID(tierID);
                    } catch (JexmaraldaException je) {
                        stats.addException(je, "ERRORR: tier with ID " + tierID
                                + " is lost...");
                        exmaError.addError(NSLC, comadirname+relPath, tierID, "", false, "ERROR: tier with ID " + tierID
                                + " is lost...");
                        continue;
                    }
                    String displayName = tier.getDisplayName();
                    String category = tier.getCategory();
                    String tierType = tier.getType();
                    if (obligatoryTiers.containsKey(category)) {
                        obligatoriesSeen.add(category);
                    } else if (optionalTiers.containsKey(category)) {
                        optionalsSeen.add(category);
                    } else {
                        stats.addCritical(NSLC,
                                "Unrecognised tier name: "
                                + tierID);
                        exmaError.addError(NSLC, comadirname+relPath, tierID, "", false, "Unrecognised tier name: "
                                + tierID);

                    }   
                    if (tierTypes.containsKey(category)) {
                        if (!tierTypes.get(category).equals(tierType)) {
                            stats.addCritical(NSLC,
                                    "Wrong tier type for: "
                                    + tierID, "Switch to annotation or "
                                    + " description tier");
                            exmaError.addError(NSLC, comadirname+relPath, tierID, "", false, "Wrong tier type for: "
                                    + tierID);

                        } else {
                            stats.addCorrect(NSLC,
                                    "Correct tier type for: " + tierID);
                        }
                    } else {
                        stats.addWarning(NSLC,
                                "Not known if tier: "
                                + tierID + " should be annotation or "
                                + "description");
                        exmaError.addError(NSLC, comadirname+relPath, tierID, "", false, "Not known if tier: "
                                + tierID + " should be annotation or "
                                + "description");

                    }
                    if (!category.equals(tierID)) {
                        stats.addCritical(NSLC,
                                "Tier ID should match category, "
                                + "but " + tierID + " is not " + category);
                        exmaError.addError(NSLC, comadirname+relPath, tierID, "", false, "Tier ID should match category, "
                                + "but " + tierID + " is not " + category);

                    }
                } // for each tier
                for (Map.Entry<String, String> entry : obligatoryTiers.entrySet()) {
                    boolean found = false;
                    for (String seen : obligatoriesSeen) {
                        if (entry.getKey().equals(seen)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        stats.addCritical(
                                "Missing required tier: "
                                + entry.getKey() + ": " + entry.getValue());
                        exmaError.addError(NSLC, comadirname+relPath, "", "", false, "Missing required tier: "
                                + entry.getKey() + ": " + entry.getValue());
                    }
                }
            } // for each transcirption
        }
        return stats;
    }

    public static void main(String[] args) {
        NgexmaraldaCorpusChecker checker;
        try {
            checker = new NgexmaraldaCorpusChecker();
            checker.exceptionalCheck();
            System.exit(0);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
            stats.merge(requireObligatoryAnnotationTiersAndTypes());
        } catch (JexmaraldaException je) {
            stats.addException(je, "Unknown parsing error");
        } catch (JDOMException jdome) {
            stats.addException(jdome, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, "Reading/writing error");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NgexmaraldaCorpusChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks if the file names in the corpus comply
     * with the coma file.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, JDOMException {
        Report stats = new Report();
        comafilename = cd.getURL().getFile();
        comadirname = comafilename.substring(0, comafilename.lastIndexOf("/") + 1);
        Document nganasanCorpus
                = org.exmaralda.common.jdomutilities.IOUtilities.readDocumentFromLocalFile(comafilename);
        XPath xpCommunications = XPath.newInstance("//Communication");
        List allCommunications = xpCommunications.selectNodes(nganasanCorpus);
        for (Object o : allCommunications) {
            communication = (Element) o;
            //retrieve the communication name
            String communicationName = communication.getAttributeValue("Name");
            //pick up basic transcriptions
            XPath xpBasTrans = XPath.newInstance("Transcription[Description"
                    + "/Key[@Name='segmented']/text()='false']");
            List allBasTrans = xpBasTrans.selectNodes(communication);
            for (Object oB : allBasTrans) {
                basTrans = (Element) oB;
                String relPath = basTrans.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    stats.addCritical(NSLC,
                            "Basic transcription file doesn't exist at "
                            + "NSLink for " + communicationName);
                } else if (Paths.get(relPath).isAbsolute()) {
                    stats.addCritical(NSLC,
                            "Basic transcription NSLink is absolute for "
                            + communicationName);
                } else if (!relPath.endsWith(communicationName + ".exb")) {
                    stats.addCritical(NSLC,
                            "Wrong basic transcription NSLink for "
                            + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Basic transcription NSLink OK: "
                            + communicationName);
                }
                String basTransName = basTrans.getChildText("Name");
                if (!basTransName.equals(communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong basic transcription name for "
                            + communicationName, basTransName + " should be "
                            + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Basic transcription name OK for "
                            + communicationName + ": " + basTransName);
                }
                if (!basTrans.getChildText("Filename").equals(
                        communicationName + ".exb")) {
                    stats.addCritical(NSLC,
                            "Wrong basic transcripton filename for "
                            + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Correct Filename for basic transcription "
                            + communicationName);
                }
            }
            XPath xpSegTrans = XPath.newInstance(
                    "Transcription[Description"
                    + "/Key[@Name='segmented']/text()='true']");
            List allSegTrans = xpSegTrans.selectNodes(communication);
            for (Object oS : allSegTrans) {
                segTrans = (Element) oS;
                String relPath = segTrans.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    stats.addCritical(NSLC,
                            "Segmented transcription file doesn't exist at"
                            + " NSLink for " + communicationName);
                } else if (Paths.get("relPath").isAbsolute()) {
                    stats.addCritical(NSLC,
                            "Segmented transcription NSLink is absolute for "
                            + communicationName);
                } else if (!relPath.endsWith(communicationName + "_s.exs")) {
                    stats.addCritical(NSLC,
                            "Wrong segmented transcription NSLink for "
                            + communicationName, relPath + " should end in "
                            + communicationName + "_s.exs");
                } else {
                    stats.addCorrect(NSLC,
                            "Correct segmented transcription NSLink for "
                            + communicationName);
                }
                String segTransName = segTrans.getChildText("Name");
                if (!segTransName.equals(communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong segmented transcription name for "
                            + communicationName, segTransName + " should be "
                            + communicationName);
                } else if (!segTrans.getChildText("Filename").equals(
                        communicationName + "_s.exs")) {
                    stats.addCritical(NSLC,
                            "Wrong segmented transcription filename for "
                            + communicationName,
                            segTrans.getChildText("Filename")
                            + " should be " + communicationName + "_s.exs");
                } else {
                    stats.addCorrect(NSLC,
                            "Correct Filename for segmented transcription "
                            + communicationName);
                }
            }
            XPath xpRec = XPath.newInstance("Recording/Media");
            List allRec = xpRec.selectNodes(communication);
            for (Object oR : allRec) {
                Element media = (Element) oR;
                rec = media.getParentElement();
                String relPath = media.getChildText("NSLink");
                String filePath = comadirname + File.separator + relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    stats.addCritical(NSLC,
                            "Recording file doesn't exist at NSLink for "
                            + communicationName);
                } else if (Paths.get("relPath").isAbsolute()) {
                    stats.addCritical(NSLC,
                            "Recording NSLink is absolute for "
                            + communicationName);
                } else if (!StringUtils.substringBefore(relPath, ".").endsWith(
                        communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong recording NSLink for "
                            + communicationName,
                            StringUtils.substringBefore(relPath, ".")
                            + " should end with " + communicationName);
                } else {
                    stats.addCorrect(NSLC,
                            "Recording NSLink is correct for "
                            + communicationName);
                }
                String recName = rec.getChildText("Name");
                if (!recName.equals(communicationName)) {
                    stats.addCritical(NSLC,
                            "Wrong recording name for " + communicationName,
                            recName + " should be " + communicationName);
                }
            }
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(NSLC,
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
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NgexmaraldaCorpusChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class is the check procedure for the Nganasan"
                + " Corpus and checks if the file names in the corpus comply with"
                + " the coma file.";
        return description;
    }
}
