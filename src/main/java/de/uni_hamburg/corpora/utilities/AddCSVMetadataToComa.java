/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.utilities;

//import com.opencsv.CSVReader;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 *
 * @author fsnv625
 *
 * this class can be used from the command line to insert data in a csv file
 * into an existing coma file 
 * there needs to be a header with information of the
 * information in the columns 
 * the first line has to consist of the sigle of the
 * speaker or name of the communication the metadata should be assigned to
 */
public class AddCSVMetadataToComa //extends AbstractCorpusProcessor 
{

    private String comaFile;
    private String csvFile;
    private Document coma; 
    private String SpeakerOrCommunication;
    private Boolean IsSpeaker;
    /**
     * creates a new instance of AddCSVMetadataToComa
     */
    public AddCSVMetadataToComa(String corpusPath) {
        //super(corpusPath);
    }

    /**
     * creates a new instance of AddCSVMetadataToComa
     */
    public AddCSVMetadataToComa(String corpusPath, String csvPath, String SpeakerOrCommunication) {
        //super(corpusPath);
        this.comaFile = corpusPath;
        this.csvFile = csvPath;
        this.SpeakerOrCommunication = SpeakerOrCommunication;
        if (SpeakerOrCommunication.equals("speaker")){
            IsSpeaker = true;
    }
         if (SpeakerOrCommunication.equals("communication")){
            IsSpeaker = false;
    }
    }

    /**
     * gets the csv data and puts it into the coma file
     */
    public void inputData() throws IOException, JDOMException {
        insertDataIntoComa(readData());
    }

    /**
     * reads the data from the csv file
     */
    public List<String[]> readData() throws FileNotFoundException, IOException {
        //CSVReader reader = new CSVReader(new FileReader(csvFile), ';');
        List<String[]> allElements = null;
                //allElements = reader.readAll();
        return allElements;
    }

    /**
     * puts the data into the coma file
     */
    public void insertDataIntoComa(List<String[]> allElements) throws JDOMException, IOException {

        for (String[] row : allElements) {
            System.out.println(Arrays.toString(row));

            //first row = keys
            //other rows = values
            //first column = communication or speaker name
        }
        System.out.println(Arrays.toString(allElements.get(0)));
             
        System.out.println(allElements.get(0)[0]);
        coma = org.exmaralda.common.jdomutilities.IOUtilities.readDocumentFromLocalFile(comaFile);
        //add the key and value to speaker/description or communication/description
       for (int i = 1; i < allElements.size(); i++)
       {      
        for (int a = 1; a < allElements.get(i).length; a++)
        {
            if(IsSpeaker){
        //the place is the xpath where it should be inserted
        String place = "//Speaker[Sigle/text()=\"" + allElements.get(i)[0] + "\"]/Description";
        System.out.println(place);
        XPath p = XPath.newInstance(place);
        //System.out.println(p.selectSingleNode(coma));
        Object o = p.selectSingleNode(coma);
				if (o!=null){
                                    Element desc = (Element) o; 
                                    //the new Key element that is inserted
                                    Element key = new Element("Key");
                                    desc.addContent(key);                                   
                                    key.setAttribute("Name", allElements.get(0)[a]);    
                                    System.out.println(desc.getAttributes());
                                    key.setText(allElements.get(i)[a]);                                         
                                }
       }
          if(!IsSpeaker){
        //the place is the xpath where it should be inserted
        String place = "//Communication[@Name=\"" + allElements.get(i)[0] + "\"]/Description";
        System.out.println(place);
        XPath p = XPath.newInstance(place);
        System.out.println(p.selectSingleNode(coma));
        Object o = p.selectSingleNode(coma);
				if (o!=null){
                                    Element desc = (Element) o; 
                                    //the new Key element that is inserted
                                    Element key = new Element("Key");
                                    desc.addContent(key);                                   
                                    key.setAttribute("Name", allElements.get(0)[a]);    
                                    System.out.println(desc.getAttributes());
                                    key.setText(allElements.get(i)[a]);                                         
                                }
       }  
        }      
       }
       //save the coma file!
       Writer fileWriter = new FileWriter(comaFile);

            XMLOutputter serializer = new XMLOutputter();
            serializer.output(coma, fileWriter);
    }

    //@Override
    public String getXpathToTranscriptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
    }

    //@Override
    public void process(String filename) throws JexmaraldaException, SAXException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: Coma-file CSV-file Boolean IsSpeaker");
            }
            AddCSVMetadataToComa metadatainputter = new AddCSVMetadataToComa(args[0], args[1], args[2]);
            try {
                metadatainputter.inputData();
            } catch (JDOMException ex) {
                Logger.getLogger(AddCSVMetadataToComa.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(AddCSVMetadataToComa.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
