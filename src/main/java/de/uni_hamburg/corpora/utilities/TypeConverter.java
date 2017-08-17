/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.utilities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka
 */
public class TypeConverter {
    
    public static String InputStream2String (InputStream is){
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    } 
    
    public static InputStream String2InputStream(String s){
        InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        return stream;
    }
    public static String BasicTranscription2String (BasicTranscription bt){        
        return bt.toXML();
    } 
    
    public static BasicTranscription String2BasicTranscription (String btAsString){
        BasicTranscription btResult = null;
        try {
            BasicTranscription bt = new BasicTranscription();
            bt.BasicTranscriptionFromString(btAsString);
            btResult = bt;
        } catch (SAXException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return btResult;
    }
    
    public static StreamSource String2StreamSource(String s){
        StreamSource ss = new StreamSource(new StringReader(s));
        return ss;
    }
    
    public static String JdomDocument2String(org.jdom.Document jdomDocument){
        return new XMLOutputter().outputString(jdomDocument);

    }

    public static org.jdom.Document W3cDocument2JdomDocument(org.w3c.dom.Document input) {
        org.jdom.Document jdomDoc = null;        
        try{
            DOMBuilder builder = new DOMBuilder();
            jdomDoc = builder.build(input);
        } catch(Exception e){
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, e);
        }        
        return jdomDoc;
    }
    
    public static org.w3c.dom.Document JdomDocument2W3cDocument(org.jdom.Document jdomDoc) {        
        org.w3c.dom.Document w3cDoc = null;        
        try{
            DOMOutputter outputter = new DOMOutputter();
            w3cDoc = outputter.output(jdomDoc);
        } catch(JDOMException je){
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, je);
        }        
        return w3cDoc;
    }
    
}
