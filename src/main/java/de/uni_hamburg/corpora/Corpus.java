/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;
import java.util.Collection;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import static de.uni_hamburg.corpora.CorpusIO.isDirectory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class Corpus {

    //only the metadata file, coma or cmdi in most cases, or a list of files
    Collection<Metadata> metadata = new ArrayList();
    //the transcriptions
    Collection<ContentData> contentdata = new ArrayList();
    Collection<Recording> recording = new ArrayList();
    Collection<AdditionalData> additionaldata = new ArrayList();
    Collection<AnnotationSpecification> annotationspecification = new ArrayList();
    Collection<ConfigParameters> configparameters = new ArrayList();
    private Collection<CmdiData> cmdidata = new ArrayList();
    //all the data together
    Collection<CorpusData> cdc;
    URL basedirectory;
    Boolean comastructured = false;

    public Corpus() {

    }

    //we need to make a difference between an unsorted folder, a miscellaneous file or a Coma file which represents a complete folder structure of the corpus
    public Corpus(URL url) throws MalformedURLException, MalformedURLException, MalformedURLException, SAXException, JexmaraldaException, URISyntaxException, IOException {
        CorpusIO cio = new CorpusIO();
        //check if the input is an unsorted directory
        if (cio.isDirectory(url)) {
            cdc = cio.read(url);
            //read in the File Types of the unstr. folder
            for (CorpusData cd : cdc) {
                if (cd instanceof ContentData) {
                    contentdata.add((ContentData) cd);
                } else if (cd instanceof Recording) {
                    recording.add((Recording) cd);
                } else if (cd instanceof AdditionalData) {
                    additionaldata.add((AdditionalData) cd);
                } else if (cd instanceof Metadata) {
                    metadata.add((Metadata) cd);
                } else if (cd instanceof AnnotationSpecification) {
                    annotationspecification.add((AnnotationSpecification) cd);
                } else if (cd instanceof ConfigParameters) {
                    configparameters.add((ConfigParameters) cd);
                } else if (cd instanceof CmdiData) {
                    cmdidata.add((CmdiData) cd);
                }
            }
            basedirectory = url;
            //the URL points to one file - could be a coma or another file
        } else {
            CorpusData cd = cio.readFileURL(url);
            if (cd instanceof ContentData) {
                contentdata.add((ContentData) cd);
            } else if (cd instanceof Recording) {
                recording.add((Recording) cd);
            } else if (cd instanceof AdditionalData) {
                additionaldata.add((AdditionalData) cd);
            } else if (cd instanceof Metadata) {
                metadata.add((Metadata) cd);
                //it could be a ComaFile if it is a Metadata file
                if (cd instanceof ComaData) {
                    //if it is we set the boolean
                    comastructured = true;
                }
            } else if (cd instanceof AnnotationSpecification) {
                annotationspecification.add((AnnotationSpecification) cd);
            } else if (cd instanceof ConfigParameters) {
                configparameters.add((ConfigParameters) cd);
            } else if (cd instanceof CmdiData) {
                cmdidata.add((CmdiData) cd);
            }
            basedirectory = cd.getParentURL();
        }
    }

    public Collection<CorpusData> getCorpusData() {
        return cdc;
    }

    public Collection<Metadata> getMetadata() {
        return metadata;
    }

    public Collection<ContentData> getContentdata() {
        return contentdata;
    }

    public Collection<Recording> getRecording() {
        return recording;
    }

    public Collection<AdditionalData> getAdditionaldata() {
        return additionaldata;
    }

    public Collection<AnnotationSpecification> getAnnotationspecification() {
        return annotationspecification;
    }

    public Collection<ConfigParameters> getConfigparameters() {
        return configparameters;
    }

    public Collection<CmdiData> getCmdidata() {
        return cmdidata;
    }

    public void setMetadata(Collection<Metadata> metadata) {
        this.metadata = metadata;
    }

    public void setContentdata(Collection<ContentData> contentdata) {
        this.contentdata = contentdata;
    }

    public void setRecording(Collection<Recording> recording) {
        this.recording = recording;
    }

    public void setAdditionaldata(Collection<AdditionalData> additionaldata) {
        this.additionaldata = additionaldata;
    }

    public void setAnnotationspecification(Collection<AnnotationSpecification> annotationspecification) {
        this.annotationspecification = annotationspecification;
    }

    public void setConfigparameters(Collection<ConfigParameters> configparameters) {
        this.configparameters = configparameters;
    }

    public void setCdc(Collection<CorpusData> cdc) {
        this.cdc = cdc;
    }

    public void setCmdidata(Collection<CmdiData> cmdidata) {
        this.cmdidata = cmdidata;
    }

    public URL getBaseDirectory() {
        return basedirectory;
    }
}
