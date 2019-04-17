/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.conversion;

import com.opencsv.CSVReader;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.conversion.AddCSVMetadataToComa;
import de.uni_hamburg.corpora.validation.ExbSegmentationChecker;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ozzy
 */
public class AddCSVMetadataToComaTest {
    
    public AddCSVMetadataToComaTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }



    /**
     * Test of main method, of class AddCSVMetadataToComa.
     */
    @Test
    public void testMain() throws IOException, JDOMException {
        /* System.out.println("main");
        
        File upTwo = new File(System.getProperty("user.dir"));
        String comaFile = upTwo.getPath() + "\\src\\test\\java\\de\\uni_hamburg\\corpora\\resources\\example\\ExampleCorpus.coma";
        String csvFile = upTwo.getPath() + "\\src\\test\\java\\de\\uni_hamburg\\corpora\\resources\\example\\PKZ_196X_BirdBringsFish_flk-FG.csv";
        String isSpeaker = "communication";
        AddCSVMetadataToComa instance = new AddCSVMetadataToComa(comaFile, csvFile, isSpeaker);
        instance.inputData();
*/
    }
        /**
     * Test of check method, of class AddCSVMetadataToComa.
     */
    @Test
    public void testCheck() throws Exception {
        System.out.println("check");
        String corpusFolder = "src/test/java/de/uni_hamburg/corpora/resources/example";
        URL corpusURL = Paths.get(corpusFolder).toUri().toURL();
        Corpus corp = new Corpus(corpusURL);
        AddCSVMetadataToComa instance = new AddCSVMetadataToComa();
        instance.setCSVFilePath("C:\\Users\\Ozzy\\HZSK\\hzsk-corpus-services\\src"
                + "\\test\\java\\de\\uni_hamburg\\corpora\\resources\\example\\sample-csv.csv");
        instance.setSpeakerOrCommunication("speaker");
        //instance.report = new Report();
        Collection<CorpusData> cdc;
        //what happens when we check exb files
        for (CorpusData cd : corp.getMetadata()) {
            assertNotNull(instance.check(cd));
        }
    }

    /**
     * Test of getIsUsableFor method, of class AddCSVMetadataToComa.
     */
    @Test
    public void testGetIsUsableFor() {
        System.out.println("getIsUsableFor");
        AddCSVMetadataToComa instance = new AddCSVMetadataToComa();
        //Collection<Class> expResult = null;
        Collection<Class<? extends CorpusData>> result = instance.getIsUsableFor();
        //no null object here
        assertNotNull(result);
    }
    
}
