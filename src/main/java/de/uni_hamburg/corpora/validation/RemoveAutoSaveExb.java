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
import java.util.List;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.utilities.TypeConverter;

/**
 *
 * @author fsnv625
 */
public class RemoveAutoSaveExb extends Checker implements CorpusFunction {

    Document doc = null;
    BasicTranscriptionData btd = null;
    String rase = "RemoveAutoSaveExb";
    
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        try {
            List al = findAllAutoSaveInstances(cd);
            //if there is no autosave, nothing needs to be done
            if (al.isEmpty()) {
                report.addCorrect(rase, cd, "there is no autosave info left, nothing to do");
            } else {
                report.addCritical(rase, cd, "autosave info needs to be removed");
                exmaError.addError("RemoveAutoSaveExb", cd.getURL().getFile(), "", "", false, "autosave info needs to be removed");
            }
        } catch (JDOMException ex) {
            report.addException(ex, rase, cd, "unknown reading error");
        }
        return report;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        List al = findAllAutoSaveInstances(cd);
        if (!al.isEmpty()) {
            for (Object o: al){
                Element e = (Element) o;
                System.out.println(e);
                //remove it
                e.getParent().removeContent(e);
            }
                //then save file
                //add a report message
            btd.setReadbtasjdom(doc);
            btd.setOriginalString(TypeConverter.JdomDocument2String(doc));
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
             report.addCorrect(rase, cd, "removed AutoSave info");
        } else {
            report.addCorrect(rase, cd, "there is no autosave info left, nothing to do");
        }
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
             Class cl2 = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl2);

        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    public List findAllAutoSaveInstances(CorpusData cd) throws JDOMException {
        btd = (BasicTranscriptionData) cd;
        doc = btd.getReadbtasjdom();
        XPath xp1;
        xp1 = XPath.newInstance("/basic-transcription/head/meta-information/ud-meta-information/ud-information[@attribute-name='AutoSave']");
        List allAutoSaveInfo = xp1.selectNodes(doc);
        return allAutoSaveInfo;
    }

}
