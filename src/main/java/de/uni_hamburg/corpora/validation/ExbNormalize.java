/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.Collection;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;

/**
 *
 * @author fsnv625
 */
public class ExbNormalize extends Checker implements CorpusFunction {

    Document doc = null;
    BasicTranscriptionData btd = null;
    Boolean fixWhiteSpaces = false;
    String ne = "NormalizeExb";

    @Override
    public Report check(CorpusData cd) {
        report.addCritical(ne, cd.getURL().getFile(), "Checking option is not available");
        return report;
    }

    @Override
    public Report fix(CorpusData cd) {
        try {
            btd = (BasicTranscriptionData) cd;
            BasicTranscription bt = btd.getEXMARaLDAbt();
            bt.normalize();
            if (fixWhiteSpaces) {
                bt.normalizeWhiteSpace();
            }
            btd.setReadbtasjdom(bt.toJDOMDocument());
            btd.setOriginalString(bt.toXML());
            //btd.updateReadbtasjdom();
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
            if (cd != null) {
                report.addCorrect(ne, cd, "normalized the file");
            } else {
                report.addCritical(ne, cd, "normalizing was not possible");
            }
        } catch (JDOMException ex) {
            report.addException(ex, ne, cd, "unknown xml exception");
        } catch (IOException ex) {
            report.addException(ex, ne, cd, "unknown IO exception");
        }
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);

        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    public void setfixWhiteSpaces(String s) {
        fixWhiteSpaces = false;
        if (s.equals("true") || s.equals("wahr") || s.equals("ja") || s.equals("yes")) {
            fixWhiteSpaces = true;
        }
    }

}
