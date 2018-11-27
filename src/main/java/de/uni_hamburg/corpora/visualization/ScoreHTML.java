/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import org.exmaralda.common.corpusbuild.FileIO;
import org.exmaralda.partitureditor.interlinearText.HTMLParameters;
import org.exmaralda.partitureditor.interlinearText.InterlinearText;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.convert.ItConverter;
import org.exmaralda.partitureditor.jexmaralda.TierFormatTable;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 *
 * @author Daniel Jettka
 */
public class ScoreHTML extends AbstractVisualization {

    // resources loaded from directory supplied in pom.xml
    private static final String STYLESHEET_PATH = "/xsl/Score2HTML.xsl";
    private final String SERVICE_NAME = "ScoreHTML";
    private Integer width = 900;


    public ScoreHTML(String btAsString){
        createFromBasicTranscription(btAsString);
    }

    /**
	 * This method deals performs the transformation of EXB to horizontal Score HTML
	 *
	 * @param  btAsString  the EXB file represented in a String object
	 * @return
	 */
    public void createFromBasicTranscription(String btAsString){

        basicTranscriptionString = btAsString;
        basicTranscription = TypeConverter.String2BasicTranscription(btAsString);

        String result = null;

        try{

            BasicTranscription bt = basicTranscription;
            bt.normalize();

            TierFormatTable tft = new TierFormatTable(bt);

            ItConverter itc = new ItConverter();
            InterlinearText it = itc.BasicTranscriptionToInterlinearText(bt, tft, 0);

            //setting HTML parameters
            HTMLParameters param = new HTMLParameters();
            param.setWidth(width);
            param.stretchFactor = 1.2;
            param.smoothRightBoundary = true;
            param.includeSyncPoints = true;
            param.putSyncPointsOutside = true;
            param.outputAnchors = true;
            param.frame = "lrtb";
            param.frameStyle = "Solid";
            param.setFrameColor(new java.awt.Color(153, 153, 153));

            it.trim(param);

            String itAsString = it.toXML();
            String styles = "/* EMTPY TIER FORMAT TABLE!!! */";
            if (bt.getTierFormatTable() != null) {
                styles = bt.getTierFormatTable().toTDCSS();
            }

            final Document itDocument = FileIO.readDocumentFromString(itAsString);
            Document btDocument = bt.toJDOMDocument();

            // remove "line" elements (die stoeren nur)
            Iterator i = itDocument.getRootElement().getDescendants(new ElementFilter("line"));
            Vector toBeRemoved = new Vector();
            while (i.hasNext()) {
                toBeRemoved.addElement(i.next());
            }
            for (int pos = 0; pos < toBeRemoved.size(); pos++) {
                Element e = (Element) (toBeRemoved.elementAt(pos));
                e.detach();
            }


            XPath xpath1 = XPath.newInstance("//common-timeline");
            Element timeline = (Element) (xpath1.selectSingleNode(btDocument));
            timeline.detach();

            XPath xpath2 = XPath.newInstance("//head");
            Element head = (Element) (xpath2.selectSingleNode(btDocument));
            head.detach();

            XPath xpath3 = XPath.newInstance("//tier");
            List tiers = xpath3.selectNodes(btDocument);
            Element tiersElement = new Element("tiers");
            for (int pos = 0; pos < tiers.size(); pos++) {
                Element t = (Element) (tiers.get(pos));
                t.detach();
                t.removeContent();
                tiersElement.addContent(t);
            }

            Element tableWidthElement = new Element("table-width");
            tableWidthElement.setAttribute("table-width", Long.toString(Math.round(param.getWidth())));

            Element btElement = new Element("basic-transcription");
    //            btElement.addContent(nameElement);
            btElement.addContent(tableWidthElement);
            btElement.addContent(head);
            btElement.addContent(timeline);
            btElement.addContent(tiersElement);

            itDocument.getRootElement().addContent(btElement);

            XMLOutputter xmOut = new XMLOutputter();
            String xml = xmOut.outputString(itDocument);

            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(STYLESHEET_PATH));

            XSLTransformer xt = new XSLTransformer();
            xt.setParameter("EMAIL_ADDRESS", EMAIL_ADDRESS);
            xt.setParameter("WEBSERVICE_NAME", SERVICE_NAME);
            xt.setParameter("HZSK_WEBSITE", HZSK_WEBSITE);
            xt.setParameter("STYLES", styles);

            // perform XSLT transformation
            result = xt.transform(xml, xsl);

            // insert JavaScript for highlighting
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
            
            
            String js = TypeConverter.InputStream2String(getClass().getResourceAsStream(JS_HIGHLIGHTING_PATH));
            result = result.replace("<!--jsholder-->", js);

        }
        catch(IOException ex){
            Logger.getLogger(ScoreHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ScoreHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(ScoreHTML.class.getName()).log(Level.SEVERE, null, ex);
        }

        setHTML(result);
    }

	/**
	 * Get the width that was set for the Score HTML visualization
	 *
	 * @param  btAsString  the EXB file represented in a String object
	 * @return
	 */
    public Integer getWidth() {
        return width;
    }

	/**
	 * Set the width for the Score HTML visualization
	 *
	 * @param  width  width in px for the HTML visualization
	 * @return
	 */
    public void setWidth(Integer width) {
        this.width = width;
    }





    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Usage: " + ScoreHTML.class.getName() +
                        "EXB [HTML]");
                System.exit(1);
            } else {
                byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
                String btString = new String(encoded, "UTF-8");
                ScoreHTML score = new ScoreHTML(btString);
                if (args.length >= 2) {
                    PrintWriter htmlOut = new PrintWriter(args[1]);
                    htmlOut.print(score.getHTML());
                    htmlOut.close();
                } else {
                    System.out.println(score.getHTML());
                }
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
