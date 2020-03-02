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
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.commons.cli.Option;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.partiture.transcriptionActions.GetSegmentationErrorsAction;
import org.jdom.JDOMException;

/**
 * This class checks basic transcription files for structural anomalies. 
 * 
 */
public class ExbStructureChecker extends Checker implements CorpusFunction {

    String exbName;
    String filename;
    BasicTranscription bt;
    File exbfile;
    ValidatorSettings settings;

    final String EXB_STRUCTURE = "exb-structure";

    /**
     * Check for structural errors.
     *
     * @see GetSegmentationErrorsAction
     */
    public Report check(File f) {
        Report stats = new Report();
        try {
            exbName = f.getName();
            stats = exceptionalCheck(f);
        } catch (JexmaraldaException je) {
            stats.addException("exb-parse", je, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException("exb-parse", saxe, "Unknown parsing error");
        }
        return stats;
    }

    public Report
            exceptionalCheck(File f) throws SAXException, JexmaraldaException {
        filename = f.getAbsolutePath();
        bt = new BasicTranscription(filename);
        Report stats = new Report();
        String[] duplicateTranscriptionTiers
                = bt.getDuplicateTranscriptionTiers();
        String[] orphanedTranscriptionTiers
                = bt.getOrphanedTranscriptionTiers();
        String[] orphanedAnnotationTiers = bt.getOrphanedAnnotationTiers();
        String[] temporalAnomalies
                = bt.getBody().getCommonTimeline().getInconsistencies();
        Hashtable<String, String[]> annotationMismatches
                = bt.getAnnotationMismatches();

        for (String tierID : duplicateTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "More than one transcription tier for one "
                    + "speaker. Tier: " + tierID, "Open in PartiturEditor, "
                    + "change tier type or merge tiers.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Temporal anomaly at timeline item: " + tliID);
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Orphaned transcription tier:" + tierID);
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Orphaned annotation tier:" + tierID);
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(EXB_STRUCTURE, exbName + ": "
                        + "Annotation mismatch: tier " + tierID
                        + " event " + eventID);
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ExbStructureChecker",
                "Checks Exmaralda .exb file for segmentation problems",
                "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for segmentation "
                    + "problems...");
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
        ExbStructureChecker checker = new ExbStructureChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            exbName = cd.getFilename();
            stats = exceptionalCheck(cd);
        } catch (JexmaraldaException je) {
            stats.addException("exb-parse", je, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException("exb-parse", saxe, "Unknown parsing error");
        } catch (JDOMException ex) {
            stats.addException("exb-parse", ex, "Unknown JDOM error");
        } catch (IOException ex) {
            stats.addException("exb-parse", ex, "Unknown IO error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks basic transcription files for
     * structural anomalies.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException {
        filename = cd.getURL().getFile();
        bt = new BasicTranscription(filename);
        Report stats = new Report();
        String[] duplicateTranscriptionTiers
                = bt.getDuplicateTranscriptionTiers();
        String[] orphanedTranscriptionTiers
                = bt.getOrphanedTranscriptionTiers();
        String[] orphanedAnnotationTiers = bt.getOrphanedAnnotationTiers();
        String[] temporalAnomalies
                = bt.getBody().getCommonTimeline().getInconsistencies();
        Hashtable<String, String[]> annotationMismatches
                = bt.getAnnotationMismatches();

        for (String tierID : duplicateTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, cd,
                    "More than one transcription tier for one "
                    + "speaker. Tier: " + tierID + "Open in PartiturEditor, "
                    + "change tier type or merge tiers.");
            exmaError.addError(EXB_STRUCTURE, filename, tierID, "", false,
                    "More than one transcription tier for one speaker. Tier: "
                    + tierID + ". Change tier type or merge tiers.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(EXB_STRUCTURE, cd,
                    "Temporal anomaly at timeline item: " + tliID);
            exmaError.addError(EXB_STRUCTURE, filename, "", "", false,
                    "Temporal anomaly at timeline item: " + tliID);
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, cd,
                    "Orphaned transcription tier:" + tierID);
            exmaError.addError(EXB_STRUCTURE, filename, tierID, "", false,
                    "Orphaned transcription tier:" + tierID);
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(EXB_STRUCTURE, cd, 
                    "Orphaned annotation tier:" + tierID);
            exmaError.addError(EXB_STRUCTURE, filename, tierID, "", false,
                    "Orphaned annotation tier:" + tierID);
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(EXB_STRUCTURE, cd,
                        "Annotation mismatch: tier " + tierID
                        + " event " + eventID);
                exmaError.addError(EXB_STRUCTURE, filename, tierID, eventID, false,
                        "Annotation mismatch: tier " + tierID
                        + " event " + eventID);
            }
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(EXB_STRUCTURE,
                "No fix is applicable for this feature yet.");
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
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks basic transcription files for structural anomalies. ";
        return description;
    }
}
