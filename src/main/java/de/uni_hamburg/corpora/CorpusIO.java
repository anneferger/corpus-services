package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.System.out;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 * Still to do
 *
 * @author fsnv625
 */
public class CorpusIO {

    public CorpusIO() {
    }

    //The content in here probably has not much to do with what we decided in UML now,
    //need to be reworked
    //that's the local filepath or repository url
    URL url;
    Collection<CorpusData> cdc = new ArrayList();
    Collection<URL> recursed = new ArrayList();
    Collection<URL> alldata = new ArrayList();

    public String CorpusData2String(CorpusData cd) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        return cd.toSaveableString();
    }


    /*
     * The following methods need to be in the Iterators for Coma and CMDI that don't exist yet
     *

     public abstract Collection getAllTranscripts();

     public abstract Collection getAllAudioFiles();

     public abstract Collection getAllVideoFiles();

     public abstract String getAudioLinkForTranscript();

     public abstract String getVideoLinkForTranscript();

     */
    public void write(CorpusData cd, URL url) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        write(cd.toSaveableString(), cd.getURL());
    }

    //TODO
    public void write(String s, URL url) throws FileNotFoundException, IOException {
        //If URL is on fileserver only...
        System.out.println("started writing document...");
        outappend("============================\n");
        FileOutputStream fos = new FileOutputStream(new File(url.getFile()));
        fos.write(s.getBytes(("UTF-8")));
        fos.close();
        System.out.println("Document written...");
    }

    public void write(Document doc, URL url) throws IOException, TransformerException, ParserConfigurationException, ParserConfigurationException, UnsupportedEncodingException, UnsupportedEncodingException, SAXException, XPathExpressionException {
        XMLOutputter xmOut = new XMLOutputter();
        String unformattedCorpusData = xmOut.outputString(doc);
        String prettyCorpusData = indent(unformattedCorpusData, "event");
        write(prettyCorpusData, url);
    }

    public void outappend(String a) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String time = sdf.format(cal.getTime());
        out.append("[" + time + "] ");
        out.append(a);
    }

    public void write(Collection<CorpusData> cdc, URL url) {

    }

    //read a single file as a corpus data object from an url
    public CorpusData readFileURL(URL url) {
        if (url.getPath().endsWith("exb")) {
            BasicTranscriptionData bt = new BasicTranscriptionData(url);
            //bt.loadFile(f);
            return bt;
        } else if (url.getPath().endsWith("coma")) {
            ComaData cm = new ComaData(url);
            return cm;
        } else if (url.getPath().endsWith("xml") && ((url.getPath().contains("Annotation") || url.getPath().contains("annotation")))) {
            AnnotationSpecification as = new AnnotationSpecification(url);
            return as;
        } else if ((url.getPath().endsWith("xml") && url.getPath().contains("cmdi")) || url.getPath().endsWith("cmdi")) {
            CmdiData cmdi = new CmdiData(url);
            return cmdi;
        } else if (url.getPath().endsWith("xml")) {
            UnspecifiedXMLData usd = new UnspecifiedXMLData(url);
            return usd;
        } else if (url.getPath().endsWith("exs")) {
            UnspecifiedXMLData usd = new UnspecifiedXMLData(url);
            return usd;
        } else {
            System.out.println(url + " is not xml CorpusData");
            CorpusData cd = null;
            return cd;
        }
    }

    //read all the files as corpus data objects from a directory url
    public Collection<CorpusData> read(URL url) throws URISyntaxException, IOException {
        alldata = URLtoList(url);
        for (URL readurl : alldata) {
            CorpusData cdread = readFileURL(readurl);
            cdc.add(cdread);
        }
        return cdc;
    }

    public String readInternalResourceAsString(String path2resource) throws JDOMException, IOException {
        String xslstring = TypeConverter.InputStream2String(getClass().getResourceAsStream(path2resource));
        System.out.println(path2resource);
        if (xslstring == null) {
            throw new IOException("Stylesheet not found!");
        }
        return xslstring;
    }

    public Collection<URL> URLtoList(URL url) throws URISyntaxException, IOException {
        if (isLocalFile(url)) {
            //if the url points to a directory
            if (isDirectory(url)) {
                //we need to iterate    
                //and add everything to the list
                Path path = Paths.get(url.toURI());
                listFiles(path);
                for (URL urlread : recursed) {
                    if (!isDirectory(urlread)) {
                        alldata.add(urlread);
                    }
                }
                return alldata;
            } //if the url points to a file
            else {
                //we need to add just this file
                alldata.add(url);
                return alldata;
            }
        } else {
            //it's a datastream in the repo
            //TODO later          
            return null;
        }
    }

    /**
     * Whether the URL is a file in the local file system.
     */
    public static boolean isLocalFile(java.net.URL url) {
        String scheme = url.getProtocol();
        return "file".equalsIgnoreCase(scheme) && !hasHost(url);
    }

    /**
     * Whether the URL is a directory in the local file system.
     */
    public static boolean isDirectory(java.net.URL url) throws URISyntaxException {
        //return new File(url.toURI()).isDirectory();
        return Files.isDirectory(Paths.get(url.toURI()));
    }

    public static boolean hasHost(java.net.URL url) {
        String host = url.getHost();
        return host != null && !"".equals(host);
    }

    public void writePrettyPrinted(CorpusData cd, URL url) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        write(cd.toSaveableString(), cd.getURL());
    }

    public void zipThings() {

    }

    void listFiles(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.{exb, exs, coma, xml, cmdi, eaf, flextext, esa, tei, xsl}")) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(entry);
                }
                recursed.add(entry.toUri().toURL());
            }
        }
    }
}
