/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.conversion;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.exmaralda.common.corpusbuild.FileIO;
import org.exmaralda.common.corpusbuild.TEIMerger;
import org.exmaralda.common.jdomutilities.IOUtilities;
//Maybe bad to have those imports here
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.SegmentedTranscription;
import org.exmaralda.partitureditor.jexmaralda.convert.StylesheetFactory;
import org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import java.util.*;
import org.exmaralda.common.corpusbuild.TextFilter;

/**
 *
 * @author fsnv625
 */
public class EXB2INELISOTEI {

    //copied partly from exmaralda\src\org\exmaralda\partitureditor\jexmaralda\convert\TEIConverter.java
    String language = "en";

    static String TEI_SKELETON_STYLESHEET_ISO = "/main/java/de/uni_hamburg/corpora/conversion/resources/xsl/EXMARaLDA2ISOTEI_Skeleton.xsl"; //Constants.BASICTRANSCRIPTION2TEISKELETONStylesheet;
    static String SC_TO_TEI_U_STYLESHEET_ISO = "/main/java/de/uni_hamburg/corpora/conversion/resources/xsl/SegmentChain2ISOTEIUtterance.xsl";; //Constants.SEGMENTCHAIN2TEIUTTERANCEStylesheet;
    static String SORT_AND_CLEAN_STYLESHEET_ISO = "/main/java/de/uni_hamburg/corpora/conversion/resources/xsl/ISOTEICleanAndSort.xsl";; //Constants.TEICLEANANDSORTStylesheet;

     static String BODY_NODE = "//text";
    
    XSLTransformer transformer;
    XSLTransformer transformer2;
    XSLTransformer transformer3;
    
    public void writeMORPHEMEHIATISOTEIToFile(BasicTranscription bt, String filename) throws SAXException,
            FSMException,
            XSLTransformException,
            JDOMException,
            IOException,
            Exception {
        writeMORPHEMEHIATISOTEIToFile(bt, filename, false, "/basic-transcription/basic-body/tier[@id = \"mb\"]");
    }

    public void writeMORPHEMEHIATISOTEIToFile(BasicTranscription bt,
            String filename,
            boolean includeFullText, String XPath2Morphemes) throws SAXException,
            FSMException,
            XSLTransformException,
            JDOMException,
            IOException,
            Exception {
        // make a copy of the exb input
        BasicTranscription copyBT = bt.makeCopy();
        //normalize the exb (!)
        copyBT.normalize();
        System.out.println("started writing document...");
        //HIAT
        HIATSegmentation segmentation = new HIATSegmentation();
        //create a segmented exs (would it be better to use the INEL FSM??
        //Should probably a parameter instead of hard coded...
        SegmentedTranscription st = segmentation.BasicToSegmented(copyBT);
        System.out.println("Segmented transcription created");
        //Deep segmentation - what would that be for INEL? Sentence?
        String nameOfDeepSegmentation = "SpeakerContribution_Utterance_Word";
        //Document from segmented transcription
        Document stdoc = FileIO.readDocumentFromString(st.toXML());
        //MAGIC
        Document teiDoc = SegmentedTranscriptionToTEITranscription(stdoc,
                nameOfDeepSegmentation,
                "SpeakerContribution_Event",
                true,
                includeFullText);
        System.out.println("Merged");
        generateWordIDs(teiDoc);
        setDocLanguage(teiDoc, language);
        FileIO.writeDocumentToLocalFile(filename, teiDoc);
        System.out.println("document written.");
    }

    // new 30-03-2016
    //maybe needs to be adapted to morpheme ids - and changed for the IDs too 
    private void generateWordIDs(Document document) throws JDOMException {
        // added 30-03-2016
        HashSet<String> allExistingIDs = new HashSet<String>();
        XPath idXPath = XPath.newInstance("//tei:*[@xml:id]");
        idXPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        idXPath.addNamespace(Namespace.XML_NAMESPACE);
        List idElements = idXPath.selectNodes(document);
        for (Object o : idElements) {
            Element e = (Element) o;
            allExistingIDs.add(e.getAttributeValue("id", Namespace.XML_NAMESPACE));
        }

        // changed 30-03-2016
        XPath wordXPath = XPath.newInstance("//tei:w[not(@xml:id)]");
        wordXPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        wordXPath.addNamespace(Namespace.XML_NAMESPACE);

        List words = wordXPath.selectNodes(document);
        int count = 1;
        for (Object o : words) {
            Element word = (Element) o;
            while (allExistingIDs.contains("w" + Integer.toString(count))) {
                count++;
            }

            String wordID = "w" + Integer.toString(count);
            allExistingIDs.add(wordID);
            //System.out.println("*** " + wordID);
            word.setAttribute("id", wordID, Namespace.XML_NAMESPACE);
        }

        // new 02-12-2014
        XPath pcXPath = XPath.newInstance("//tei:pc[not(@xml:id)]");
        pcXPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        pcXPath.addNamespace(Namespace.XML_NAMESPACE);

        List pcs = pcXPath.selectNodes(document);
        count = 1;
        for (Object o : pcs) {
            Element pc = (Element) o;
            while (allExistingIDs.contains("pc" + Integer.toString(count))) {
                count++;
            }

            String pcID = "pc" + Integer.toString(count);
            allExistingIDs.add(pcID);
            //System.out.println("*** " + wordID);
            pc.setAttribute("id", pcID, Namespace.XML_NAMESPACE);
        }
    }

    private void setDocLanguage(Document teiDoc, String language) throws JDOMException {
        // /TEI/text[1]/@*[namespace-uri()='http://www.w3.org/XML/1998/namespace' and local-name()='lang']
        XPath xpathToLangAttribute = XPath.newInstance("//tei:text/@xml:lang");
        xpathToLangAttribute.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        xpathToLangAttribute.addNamespace(Namespace.XML_NAMESPACE);
        Attribute langAtt = (Attribute) xpathToLangAttribute.selectSingleNode(teiDoc);
        if (langAtt != null) {
            langAtt.setValue(language);
        } else {
            XPath xpathToTextElement = XPath.newInstance("//tei:text");
            xpathToTextElement.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
            xpathToTextElement.addNamespace(Namespace.XML_NAMESPACE);
            Element textEl = (Element) xpathToTextElement.selectSingleNode(teiDoc);
            textEl.setAttribute("lang", language, Namespace.XML_NAMESPACE);
        }
        System.out.println("Language of document set to " + language);

    }
    
            //Boolean for ISO!
    /*
    public TEIMerger(boolean beISO) throws XSLTransformException{
        ISO = beISO;
        if (beISO){
            //java.io.InputStream is = getClass().getResourceAsStream(TEI_SKELETON_STYLESHEET_ISO);
            java.io.InputStream is2 = getClass().getResourceAsStream(SC_TO_TEI_U_STYLESHEET_ISO);
            //java.io.InputStream is3 = getClass().getResourceAsStream(SORT_AND_CLEAN_STYLESHEET_ISO);
            //transformer = new XSLTransformer(is);
            transformer2 = new XSLTransformer(is2);
            //transformer3 = new XSLTransformer(is3);
        }
    }
    
    */
        
    public Document SegmentedTranscriptionToTEITranscription(Document segmentedTranscription,
                                                             String nameOfDeepSegmentation, 
                                                             String nameOfFlatSegmentation,
                                                             boolean useNewStylesheets,
                                                             boolean includeFullText) throws XSLTransformException, JDOMException, Exception {
        

        
        String skeleton_stylesheet = TEI_SKELETON_STYLESHEET_ISO;

        String transform_stylesheet = SC_TO_TEI_U_STYLESHEET_ISO;

        String sort_and_clean_stylesheet = SORT_AND_CLEAN_STYLESHEET_ISO;        
        

        Document teiDocument = null;
        if (useNewStylesheets){
            StylesheetFactory ssf = new StylesheetFactory(true);
            String result =
                ssf.applyInternalStylesheetToString(skeleton_stylesheet, IOUtilities.documentToString(segmentedTranscription));
            teiDocument = IOUtilities.readDocumentFromString(result);
        } else {
            teiDocument = transformer.transform(segmentedTranscription);            
        }
        
        //FileIO.writeDocumentToLocalFile("C:\\Users\\Schmidt\\Desktop\\TEI\\intermediate1.xml", teiDocument);      
        //System.out.println("STEP 1 completed.");
        
        Vector uElements = TEIMerge(segmentedTranscription, nameOfDeepSegmentation, nameOfFlatSegmentation, includeFullText);
        

        XPath xp = XPath.newInstance(BODY_NODE);
        if (useNewStylesheets){
            BODY_NODE = "//tei:body";
            xp = XPath.newInstance(BODY_NODE);
            xp.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        }


        Element textNode = (Element)(xp.selectSingleNode(teiDocument));
        textNode.addContent(uElements);

        //FileIO.writeDocumentToLocalFile("C:\\Users\\Schmidt\\Desktop\\TEI\\intermediate2.xml", teiDocument);
        //System.out.println("STEP 2 completed.");

        Document transformedDocument = null;
        if (useNewStylesheets){
            StylesheetFactory ssf = new StylesheetFactory(true);
            String result =
                ssf.applyInternalStylesheetToString(transform_stylesheet, IOUtilities.documentToString(teiDocument));
            transformedDocument = IOUtilities.readDocumentFromString(result);
            //fix for issue #89
            textNode = (Element)(xp.selectSingleNode(transformedDocument));
        } else {
            transformedDocument = transformer2.transform(teiDocument);
            textNode = (Element)(xp.selectSingleNode(transformedDocument));
        }

        //FileIO.writeDocumentToLocalFile("C:\\Users\\Schmidt\\Desktop\\TEI\\intermediate3.xml", transformedDocument);
        //System.out.println("STEP 3 completed.");

        // now take care of the events from tiers of type 'd'
        XPath xp2 = XPath.newInstance("//segmentation[@name='Event']/ats");
        List events = xp2.selectNodes(segmentedTranscription);
        for (int pos=0; pos<events.size(); pos++){
            Element exmaraldaEvent = (Element)(events.get(pos));
            String category = exmaraldaEvent.getParentElement().getParentElement().getAttributeValue("category");

            String elementName = "event";
            if (category.equals("pause")) {
                elementName ="pause";
            }
            
            Element teiEvent = new Element(elementName);
            
            String speakerID = exmaraldaEvent.getParentElement().getParentElement().getAttributeValue("speaker");
            if (speakerID!=null){
                teiEvent.setAttribute("who", speakerID);
            }
            teiEvent.setAttribute("start", exmaraldaEvent.getAttributeValue("s"));
            teiEvent.setAttribute("end", exmaraldaEvent.getAttributeValue("e"));
            if (!category.equals("pause")){
                teiEvent.setAttribute("desc", exmaraldaEvent.getText());
                teiEvent.setAttribute("type", category);
            } else {
                String duration = exmaraldaEvent.getText().replaceAll("\\(","").replaceAll("\\)","");
                teiEvent.setAttribute("dur", duration);
            }
            textNode.addContent(teiEvent);
        }

        //IOUtilities.writeDocumentToLocalFile("C:\\Dokumente und Einstellungen\\thomas\\Desktop\\Intermediate_TEI.xml", transformedDocument);

        
        Document finalDocument = null;
        if (useNewStylesheets){
            StylesheetFactory ssf = new StylesheetFactory(true);
            String result =
                ssf.applyInternalStylesheetToString(sort_and_clean_stylesheet, IOUtilities.documentToString(transformedDocument));
            finalDocument = IOUtilities.readDocumentFromString(result);
        } else {
            finalDocument = transformer3.transform(transformedDocument);
        }


        return finalDocument;
    }
    
    
     public static Vector TEIMerge(Document segmentedTranscription, String nameOfDeepSegmentation, String nameOfFlatSegmentation) throws Exception{                
        return TEIMerge(segmentedTranscription, nameOfDeepSegmentation, nameOfFlatSegmentation, false);
    }
    /** this method will take the segmented transcription and, for each speaker contribution in the segmentation with
     * the name 'nameOfDeepSegmentation' will add anchors from the segmentation with the name 'nameOfFlatSegmentation'
     * such that the temporal information provided in the flat segmentation is completely represented as anchors 
     * within the deep segmentation. The typical application scenario is to give this method a segmented HIAT transcription with
     * nameOfDeepSegmentation = 'SpeakerContribution_Utterance_Word' 
     * nameOfFlatSegmentation = 'SpeakerContribution_Event'
     * @param segmentedTranscription
     * @param nameOfDeepSegmentation
     * @param nameOfFlatSegmentation
     * @param includeFullText
     * the method returns a vector of speaker-contribution elements with 'who' attributes
     * @return  */
    public static Vector TEIMerge(Document segmentedTranscription, 
            String nameOfDeepSegmentation, 
            String nameOfFlatSegmentation,
            boolean includeFullText) throws Exception{                
        try {
            
            // Make a map of the timeline
            Hashtable timelineItems = new Hashtable();
            String xpath = "//tli";
            XPath xpx = XPath.newInstance(xpath);
            List tlis = xpx.selectNodes(segmentedTranscription);
            for (int pos=0; pos<tlis.size();pos++){
                timelineItems.put(((Element)(tlis.get(pos))).getAttributeValue("id"), pos);
            }
            
            
            Vector returnValue = new Vector();
            XPath xp1 = XPath.newInstance("//segmentation[@name='" + nameOfDeepSegmentation + "']/ts");
            List segmentChains = xp1.selectNodes(segmentedTranscription);        
            // go through all top level segment chains
            for (Object segmentChain : segmentChains) {
                Element sc = (Element) (segmentChain);
                sc.setAttribute("speaker", sc.getParentElement().getParentElement().getAttributeValue("speaker"));
                String tierref = sc.getParentElement().getAttributeValue("tierref");
                String start = sc.getAttributeValue("s");
                String end = sc.getAttributeValue("e");
                String xpath2 = "//segmentation[@name='" + nameOfFlatSegmentation + "' and @tierref='" + tierref + "']"
                        + "/ts[@s='" + start + "' and @e='" + end +"']";
                XPath xp2 = XPath.newInstance(xpath2);
                Element sc2 = (Element)(xp2.selectSingleNode(segmentedTranscription));                                               
                if (sc2==null){
                    //this means that no corresponding top level
                    //element was found in the second segmentation
                    //which should not happen
                    throw new Exception(tierref + " " + start + " " + end);
                }
                // this is where the magic happens
                Element mergedElement = merge(sc,sc2);
                

                // now take care of the corresponding annotations
                int s = ((Integer)(timelineItems.get(start)));
                int e = ((Integer)(timelineItems.get(end)));
                String xpath5 = "//segmented-tier[@id='" + tierref + "']/annotation/ta";
                XPath xp5 = XPath.newInstance(xpath5);
                List annotations = xp5.selectNodes(segmentedTranscription);
                for (Object annotation1 : annotations) {
                    Element anno = (Element) (annotation1);
                    String aStart = anno.getAttributeValue("s");
                    String aEnd = anno.getAttributeValue("e");
                    int as = ((Integer)(timelineItems.get(aStart)));
                    int ae = ((Integer)(timelineItems.get(aEnd)));
                    boolean annotationBelongsToThisElement = (as>=s && as<=e) || (ae>=s && ae<=e);
                    if (annotationBelongsToThisElement){
                        Element annotationsElement = mergedElement.getChild("annotations");
                        if (annotationsElement==null){
                            annotationsElement = new Element("annotations");
                            mergedElement.addContent(annotationsElement);
                        }
                        Element annotation = new Element("annotation");
                        annotation.setAttribute("start",aStart);
                        annotation.setAttribute("end",aEnd);
                        annotation.setAttribute("level",anno.getParentElement().getAttributeValue("name"));
                        annotation.setAttribute("value", anno.getText());
                        annotationsElement.addContent(annotation);
                    }
                    
                    //System.out.println(s + "/" + e + " **** " + as + "/" + ae);
                }
                
                
                //*****************************************
                // NEW 25-04-2016
                // include full text if Daniel J. wisheth thus
                if (includeFullText){
                    Element annotation = new Element("annotation");
                    annotation.setAttribute("start", start);
                    annotation.setAttribute("end", end);
                    annotation.setAttribute("level", "full-text");
                    
                    String fullText = "";
                    List l = XPath.selectNodes(sc2, "descendant::text()");
                    for (Object o : l){
                        Text text = (Text)o;
                        fullText+=text.getText();
                    }
                    annotation.setAttribute("value", fullText);
                    
                    Element annotationsElement = mergedElement.getChild("annotations");
                    if (annotationsElement==null){
                        annotationsElement = new Element("annotations");
                        mergedElement.addContent(annotationsElement);
                    }
                    annotationsElement.addContent(annotation);
                }
                //*****************************************
                
                returnValue.addElement(mergedElement.detach());
            }
            
            // issue #89 - Now the vector contains elements only from the 
            // segmentations passed as parameters
            // in particular, it seems that tiers of type 'd' (which end up as 
            // segmentation @name='Event' are lost
            
            return returnValue;
        } catch (JDOMException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    static Element merge(Element e1, Element e2){
        
        Iterator i1 = e1.getDescendants();
        Vector pcData1 = new Vector();
        while (i1.hasNext()){pcData1.addElement(i1.next());}
        
        Iterator i2 = e2.getDescendants(new TextFilter());
        Vector pcData2 = new Vector();
        while (i2.hasNext()){pcData2.addElement(i2.next());}
                  
        int charBoundary = 0;
        for (int pos=0; pos<pcData2.size()-1; pos++){
            Text eventText = (Text)(pcData2.elementAt(pos));
            Element anchor = new Element("anchor");
            Element event = eventText.getParentElement();
            String start = event.getAttributeValue("e");
            anchor.setAttribute("synch", start);

            charBoundary+= eventText.getText().length();
            // jetzt durch den anderen Baum laufen und den zugehoerigen Anker
            // an der richtigen Stelle einfuegen
            int charCount = 0;
            for (int pos2=0; pos2<pcData1.size(); pos2++){
                Object o = pcData1.elementAt(pos2);
                if (!(o instanceof Text)) continue;
                Text segmentText = (Text)o;
                int textLength = segmentText.getText().length();
                if (charCount+textLength<charBoundary){
                    charCount+=textLength;
                    continue;
                } else if (charCount+textLength==charBoundary){
                    Element parent = segmentText.getParentElement();
                    int index = parent.indexOf(segmentText);
                    Element parentOfParent = parent.getParentElement();
                    int index2 = parentOfParent.indexOf(parent);
                    parentOfParent.addContent(index2+1,anchor);
                    break;
                }
                // charCount+textLength>charBoundary
                String leftPart = segmentText.getText().substring(0, charBoundary-charCount);
                String rightPart = segmentText.getText().substring(charBoundary-charCount);
                Text leftText = new Text(leftPart);
                Text rightText = new Text(rightPart);


                // neue Sachen muessen zweimal eingefuegt werden - einmal
                // in den Vector, einmal in den Parent
                // Sachen im Vector muessen den richtigen Parent bekommen
                
                
                Element parent = segmentText.getParentElement();
                parent.removeContent(segmentText);
                parent.addContent(leftText);
                parent.addContent(anchor);
                parent.addContent(rightText);
                
                pcData1.remove(segmentText);
                pcData1.add(pos2,rightText);
                pcData1.add(pos2,anchor);
                pcData1.add(pos2,leftText);
                break;
            }
        }
        
        return e1;
    }

}
