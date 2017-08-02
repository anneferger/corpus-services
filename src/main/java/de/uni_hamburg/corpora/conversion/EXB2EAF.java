/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.conversion;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;

/** *
 * @author Daniel Jettka
 * 
 * This class represents a converter for EXMARaLDA Basic Transcriptions into ELAN transcriptions.
 * It operates on String instances of the transcriptions.
 * The class re-uses classes and methods from the EXMARaLDA package.
 */
public class EXB2EAF {
    
    /** the XSLT stylesheet for converting an EXMARaLDA basic transcription to an EAF document 
        (path applies when in context of a class in exmaralda package, see below) **/
    static final String EX2ELAN_STYLESHEET = "/org/exmaralda/partitureditor/jexmaralda/xsl/BasicTranscription2EAF.xsl";
    
    
    /** Creates a new instance of EXB2EAF */
    public EXB2EAF() {
        
    }
    
    
    /** reads the EXB as String specified by basicTranscrition and returns an ELAN Transcription
     * @param basicTranscription Representation of EXMARaLDA basic transcrition in String
     * @return Representation of ELAN transcription in a String
     * @throws org.exmaralda.partitureditor.jexmaralda.JexmaraldaException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerConfigurationException */
    public String EXB2EAF(String basicTranscription) throws SAXException, 
                                                            JexmaraldaException, 
                                                            ParserConfigurationException, 
                                                            IOException, 
                                                            TransformerException{
        return convert(basicTranscription);
    }
    
        
    /** reads the EXB as String specified by basicTranscrition and returns an ELAN Transcription
     * @param basicTranscription Representation of EXMARaLDA basic transcrition in String
     * @return Representation of ELAN transcription in a String
     * @throws org.exmaralda.partitureditor.jexmaralda.JexmaraldaException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerConfigurationException */
    public String convert(String basicTranscription) throws SAXException, 
                                                            JexmaraldaException, 
                                                            ParserConfigurationException, 
                                                            IOException, 
                                                            TransformerException{
        
        /* ELANConverter in EXMARaLDA works with BasicTranscription object */
        BasicTranscription bt = TypeConverter.String2BasicTranscription(basicTranscription);
        
        /* NOTE: conversion method from ELANConverter in EXMARaLDA cannot be used directly (private),
           so that directives from private method BasicTranscriptionToELAN method from ELANConverter 
           have to replicated here */
        
        
        // interpolate the timeline, i.e. calculate absoulute time values for timeline items
        // that don't have an absolute time value assigned
        // (is this necessary or can ELAN also handle time slots without absolute time values?)
        bt.getBody().getCommonTimeline().completeTimes();
        
        // read BasicTranscription into a String
        String exb = bt.toXML();
        
        // read the XSL stylesheet into a String
        String xsl = TypeConverter.InputStream2String(org.exmaralda.partitureditor.jexmaralda.convert.ELANConverter.class.getResourceAsStream(EX2ELAN_STYLESHEET));
                
        // create a class for performing a stylesheet transformation
        XSLTransformer xt = new XSLTransformer();
        String eaf = xt.transform(exb, xsl);
        
        return eaf;
    }
    
}
