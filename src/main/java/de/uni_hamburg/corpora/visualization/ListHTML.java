/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.common.corpusbuild.FileIO;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.ListTranscription;
import org.exmaralda.partitureditor.jexmaralda.SegmentedTranscription;
import org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.GATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.GenericSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.IPASegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.SegmentedToListInfo;
import org.jdom.Document;
import org.xml.sax.SAXException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Daniel Jettka
 */
public class ListHTML extends Visualizer {

    private String utteranceList = null;
    private String segmentationAlgorithm = "Generic";

    // resources loaded from directory supplied in pom.xml
    private static final String STYLESHEET_PATH = "/xsl/HIAT2ListHTML.xsl";
    private static final String GAT_STYLESHEET_PATH = "/xsl/GAT2ListHTML.xsl";
    private static final String GENERIC_STYLESHEET_PATH = "/xsl/Generic2ListHTML.xsl";
    private static final String SERVICE_NAME = "ListHTML";


    public ListHTML(String btAsString, String segmAlgorithm){
        createFromBasicTranscription(btAsString, segmAlgorithm);
    }


	/**
	* This method deals with transforming EXB to list HTML
	*
	* @param  btAsString  the EXB file represented in a String object
	* @return
	*/
    private void createFromBasicTranscription(String btAsString, String segmAlgorithm){

        basicTranscriptionString = btAsString;
        basicTranscription = TypeConverter.String2BasicTranscription(basicTranscriptionString);
        segmentationAlgorithm = segmAlgorithm;

        String result = null;

        try{

            // create an utterance list as XML basis for transformation
            createUtteranceList();

            // get the XSLT stylesheet
            String xsl = "";
            if (segmAlgorithm.equals("HIAT")) {
                xsl = TypeConverter.InputStream2String(
                        getClass().getResourceAsStream(STYLESHEET_PATH));
            } else if (segmAlgorithm.equals("GAT")) {
                xsl = TypeConverter.InputStream2String(
                        getClass().getResourceAsStream(GAT_STYLESHEET_PATH));
            } else if (segmAlgorithm.equals("Generic")) {
                xsl = TypeConverter.InputStream2String(
                        getClass().getResourceAsStream(GENERIC_STYLESHEET_PATH));
            } else {
                xsl = TypeConverter.InputStream2String(
                        getClass().getResourceAsStream(GENERIC_STYLESHEET_PATH));
            }

            // create XSLTransformer and set the parameters
            XSLTransformer xt = new XSLTransformer();
            xt.setParameter("EMAIL_ADDRESS", EMAIL_ADDRESS);
            xt.setParameter("WEBSERVICE_NAME", SERVICE_NAME+" ("+segmAlgorithm+")");
            xt.setParameter("HZSK_WEBSITE", HZSK_WEBSITE);

            // perform XSLT transformation
            result = xt.transform(getUtteranceList(), xsl);

            // replace JS/CSS placeholders from XSLT output
            try{                
                Pattern regex = Pattern.compile("(<hzsk\\-pi:include( xmlns:hzsk\\-pi=\"https://corpora\\.uni\\-hamburg\\.de/hzsk/xmlns/processing\\-instruction\")?>([^<]+)</hzsk\\-pi:include>)", Pattern.DOTALL);
                Matcher m = regex.matcher(result);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String insertion = TypeConverter.InputStream2String(getClass().getResourceAsStream(m.group(3)));
                    m.appendReplacement(sb, m.group(0).replaceFirst(Pattern.quote(m.group(1)), insertion));
                }
                m.appendTail(sb);
                result = sb.toString();
            } catch(Exception e){
                setHTML("Custom Exception for inserting JS/CSS into result: " + e.getLocalizedMessage() + "\n" + result);
                return;
            }


        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ListHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ListHTML.class.getName()).log(Level.SEVERE, null, ex);
        }

        setHTML(result);
    }

	/**
	 * This method creates the intermediate utterance list needed for List visualization
	 *
	 * @param
	 * @return
	 */
	 private void createUtteranceList(){

        String list = null;

        try{

            switch (segmentationAlgorithm) {
                case "HIAT":
                    {
                        HIATSegmentation hS = new HIATSegmentation();
                        ListTranscription lt = hS.BasicToUtteranceList(basicTranscription);
                        final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                        list = IOUtilities.documentToString(listXML);
                        break;
                    }
                case "CHAT":
                    {
                        CHATSegmentation cS = new CHATSegmentation();
                        ListTranscription lt = cS.BasicToUtteranceList(basicTranscription);
                        final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                        list = IOUtilities.documentToString(listXML);
                        break;
                    }
                case "GAT":
                    {
                        GATSegmentation gS = new GATSegmentation();
                        ListTranscription lt = gS.BasicToIntonationUnitList(basicTranscription);
                        final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                        list = IOUtilities.documentToString(listXML);
                        break;
                    }
                case "IPA":
                    {
                        IPASegmentation ipaS = new IPASegmentation();
                        SegmentedTranscription st = ipaS.BasicToSegmented(basicTranscription);
                        ListTranscription lt = st.toListTranscription(new SegmentedToListInfo(st, SegmentedToListInfo.TURN_SEGMENTATION));
                        final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                        list = IOUtilities.documentToString(listXML);
                        break;
                    }
                case "Generic":
                    {
                        GenericSegmentation genS = new GenericSegmentation();
                        SegmentedTranscription st = genS.BasicToSegmented(basicTranscription);
                        ListTranscription lt = st.toListTranscription(new SegmentedToListInfo(st, SegmentedToListInfo.TURN_SEGMENTATION));
                        final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                        list = IOUtilities.documentToString(listXML);
                        break;
                    }
                default:
                    throw new Exception("createUtteranceList - unsupported parameter segmAlgorithm='"+segmentationAlgorithm+"'");
            }

        } catch (SAXException ex) {
            Logger.getLogger(ListHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FSMException ex) {
            Logger.getLogger(ListHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ListHTML.class.getName()).log(Level.SEVERE, null, ex);
        }

        setUtteranceList(list);
    }

    /**
	 * Manually set the utterance list (intermediate transformation format) for this visualization
	 *
	 * @param  u  the String representing the utterance list
	 * @return
	 */
    public void setUtteranceList(String u){
        utteranceList = u;
    }

	/**
	 * Get the utterance list (intermediate transformation format) for this visualization
	 *
	 * @param
	 * @return  String representing the utterance list
	 */
    public String getUtteranceList(){
        return utteranceList;
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Usage: " + ListHTML.class.getName() +
                        "EXB SEGMENTATION [HTML]");
                System.out.println("\nSEGMENTATION is one of: " +
                       " HIAT, CHAT, IPA, Generic");
                System.exit(1);
            } else {
                byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
                String btString = new String(encoded, "UTF-8");
                ListHTML list = new ListHTML(btString, args[1]);
                if (args.length >= 3) {
                    PrintWriter htmlOut = new PrintWriter(args[2]);
                    htmlOut.print(list.getHTML());
                    htmlOut.close();
                } else {
                    System.out.println(list.getHTML());
                }
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    @Override
    public Report visualize(CorpusData cd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report doMain(String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
