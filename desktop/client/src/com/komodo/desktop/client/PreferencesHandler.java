/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;
import com.komodo.desktop.interfaces.ShieldEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * PreferencesHandler manages Preferences stored in an xml document.
 * Currently only one document is being used as preferences.
 * Support for multiple documents has to be added.
 * This class  creates,reads and modifies the XML document.
 * Document Format:
 * <TeclaClient name="client_name">
 * <ShieldButton value="button_number">
 * <ShieldEvent 
 * eventid="shieldevent_id"
 * device="device number"
 * options="dx_value,dy_value"
 * value="val1,val2,val3,"
 * >
 * </ShieldEvent>
 * </ShieldButton>
 * </TeclaClient> 
 * TeclaClient is the root element. only one such element possible in a document.
 * where button_number,shieldevent_id,device number,dx_value,dy_value,val1,val2,val3 are integers
 * the last comma in value field of ShieldEvent is essential for proper parsing.
 * @author Akhil
 * @author ankitdaf
 */

public class PreferencesHandler {
   
    File dir;                       //Path where all preferences will be stored
    File current_config;            //File object representing the xml file for the current configuration
    File[] available_configs;       //Configuration list
    DocumentBuilderFactory docf;    //Documentfactory used to build documents as per w3c
    DocumentBuilder docb;           //Document builder used to create documents
    public Document doc;            //xml Document object. 
    FileWriter filew;               //Filewriter used to write to the file.
    Element rootElement;            //the element directly below the document in DOM heirarchy
    ShieldButton ecu1,ecu2,ecu3,ecu4,e1,e2; //ShieldButton objects one for each button.
    String location;
    public PreferencesHandler(String filepath) {
        /*
         * 
         * This constructor uses filepath to open the config file.
         * If the file doesn't exist ,a new file will be created and fill 
         * it with default values which are all NONE.
         * if the file exists the file will be parsed into the document.
         * 
         */
        location=filepath;
        dir=new File(filepath);      //Directory containing preferences
        available_configs = dir.listFiles();    //List all available configs in the directory
        if(available_configs==null || available_configs.length==0)
        {
            createXmlFile();
        }
        else{
            try {
                current_config=available_configs[0];    //Default config if erroneously none are set to true
                for (int i=0;i < available_configs.length ; i++)
                {
                    docf=DocumentBuilderFactory.newInstance();  //Initialize the document builder
                    docb=docf.newDocumentBuilder(); //initialize document builder
                    doc=docb.parse(available_configs[i]);  //parse the config file into documents consisting of nodelist
                    rootElement=(Element)doc.getFirstChild(); //initialize the root element
                if(rootElement.getAttribute("is_default").equals("true")) 
                {
                    current_config=available_configs[i];
                    break;
                }
                }
            }  catch (SAXException ex) {
                    Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }         
        }
      initialize_events(); //Parse the information in the config file to ComEvents.
      
    }   
    
    public void createXmlFile(){
        try {
            try {
                current_config = new File(location+"/default.xml");   //Create a default config
                current_config.getParentFile().mkdirs();    //Create all parent directories
                current_config.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            docf=DocumentBuilderFactory.newInstance();
            docb=docf.newDocumentBuilder();
            doc=docb.newDocument();//Create a new empty DOM document.
            rootElement=doc.createElement("TeclaClient"); //RootElement=>TeclaClient
            rootElement.setAttribute("name", "default");
            rootElement.setAttribute("connection",""+EventConstant.CONNECT_TO_ANDROID);
            rootElement.setAttribute("password",""+"TeclaShield");
            rootElement.setAttribute("is_default", "true"); //Make the current config the default
            doc.appendChild(rootElement); //appends the rootelement to the document as Child.
            makedefault(doc,rootElement); //Initialize all the values.
            commitchanges(doc);  //Write changes to the file.
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public File[] get_available_configs()
    {
        available_configs=dir.listFiles();
        if(available_configs == null || available_configs.length==0)
        {
            createXmlFile();
            available_configs=dir.listFiles();
        }
        return available_configs;
    }

    public Document get_doc()
    {
        return doc;
    }
    
    public void save_config(File selected_config)
    {
        try {
            docf=DocumentBuilderFactory.newInstance();  //Initialize the document builder
            docb=docf.newDocumentBuilder(); //initialize document builder
            doc=docb.parse(selected_config);  //initialize the root element
            rootElement=(Element)doc.getFirstChild(); //initialize the root element
            rootElement.setAttribute("is_default", "false");
            commitchanges(doc);
        } catch (SAXException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void save_config(File old_config, File new_config)
    {
        try {
            docf=DocumentBuilderFactory.newInstance();  //Initialize the document builder
            docb=docf.newDocumentBuilder(); //initialize document builder
            doc=docb.parse(old_config);  //initialize the root element
            rootElement=(Element)doc.getFirstChild(); //initialize the root element
            rootElement.setAttribute("is_default", "false");
            current_config = new_config;
            commitchanges(doc);
        } catch (SAXException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void set_selected_config(File selected_config)
    {
        try {
            current_config=selected_config;                 // Use the config selected as the current config
            docf=DocumentBuilderFactory.newInstance();  //Initialize the document builder
            docb=docf.newDocumentBuilder(); //initialize document builder
            doc = docb.parse(selected_config);      //parse the config file into documents consisting of nodelist
            rootElement=(Element)doc.getFirstChild(); //initialize the root element
            rootElement.setAttribute("is_default", "true");
            commitchanges(doc);
            initialize_events();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public File get_current_config()
    {
        return current_config;
    }
    
    public void set_current_config(File this_config)
    {
        current_config = this_config;
    }
    
    public void commitchanges(Document doc_){
        TransformerFactory tf =TransformerFactory.newInstance(); //
        try {
            Transformer transformer=tf.newTransformer(); //Transformer transforms a DOM document to
                                                         //a stream output such as stdout or file.               
            DOMSource source=new DOMSource(doc_) ;       //source document    
            try {
                filew=new FileWriter(current_config);
            } catch (IOException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            StreamResult result=new StreamResult(filew); //destination stream
            try {
                transformer.transform(source, result);   //transform the source into result.
                filew.close();
            } catch (IOException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }  
    public Element addButton(Document document,Element root,int buttonid){
        /*
         * Adds a ShieldButton Element to the documents'root element as a Child
         * and sets its button value as its attribute.
         * returns added Element.
         */
        Element buttonelem=document.createElement("ShieldButton");
        buttonelem.setAttribute("value",""+buttonid);
        buttonelem.setAttribute("RTR","0");
        buttonelem.setAttribute("RTR_delay","100"); //delay in ms
        root.appendChild(buttonelem);
        return buttonelem;
    }
    public Element addeventinfo(Document document,Element parent,int eventid,int device,int[] value,int[] options ){
        /*
         * Adds a ShieldEvent Element as a child to the parent ShieldButton element.
         * The atrributes of ShieldEvent include eventid , device ,value ,options
         * The meaning of these attributes is explained in ComEvent.java
         * returns created ShieldEventElement.
         */
        Element event = document.createElement("ShieldEvent"); 
        event.setAttribute("device", ""+device);
        event.setAttribute("eventid",""+eventid);
        event.setIdAttribute("eventid", true);
        String eventvalues="";
        for(int i=0;i<value.length;i++){
            eventvalues=eventvalues.concat(""+value[i]+",");
        }
        //eventvalues=eventvalues.substring(0,eventvalues.length()-1);
        event.setAttribute("value",eventvalues);
        String optionsvalues="";
        for(int i=0;i<options.length;i++){
            optionsvalues=optionsvalues.concat(""+options[i]+",");

        }
        optionsvalues=optionsvalues.substring(0,optionsvalues.length()-1);
        if(optionsvalues.length()>0)
        event.setAttribute("options",optionsvalues);
        parent.appendChild(event);
        return event;
    }
    public void makedefault(Document doc_,Element root_){
        /*
         * Adds the default values of events into all shieldbuttons 
         * i.e to initialize the document with default values.
         * device=none , values=0,options=0,0;
         */
        
        int[] val,options;
        val=new int[1]; 
        options=new int[2];
        options[0]=options[1]=val[0]=EventConstant.NONE;
        for(int i=ShieldEvent.ECU1;i<=ShieldEvent.E2;i++){
           Element buttonElement= addButton(doc_,root_,i);
           for(int j=ShieldEvent.EVENT_PRESSED;j<=ShieldEvent.EVENT_LONGPRESS;j++){
                addeventinfo(doc_,buttonElement,j,EventConstant.NONE,val,options);
           }
        }
    }
    public Element getButton(int buttonid){
        /*
         * returns ShieldButton Element corresponding to the given buttonid 
         * returns null if not found.
         */
        Element button=(Element)rootElement.getFirstChild();
        while(button != null){
            if(button.getAttribute("value").equals(""+buttonid))
                break;
            button=(Element)button.getNextSibling();
           
        }
        return button;
    }
    public Element getShieldEvent(Element button,int eventid){
        /*
         * returns ShieldEvent Element for the corresponding button and eventid
         * returns null if not found.
         */
        Element event=(Element) button.getFirstChild();
        while(event != null){
            if(event.getAttribute("eventid").equals(""+eventid)){
                break;
            }
            event=(Element)event.getNextSibling();
        }
        return event;
    }
    public void setEventattribute(int buttonid,int eventid,String key,String value){
        /*
         * sets the ShieldEvent given by buttonid and eventid's
         * attribute 'key' with 'value'
         */
        Element event =getShieldEvent(getButton(buttonid),eventid);
        event.setAttribute(key, value);
    }
    public String getEventattribute(int buttonid,int eventid,String key){
        
        /*
         * retrieves the value of the ShieldEvent described by eventid and buttonid
         * attribute having the attribute name 'key'
         */
        Element event =getShieldEvent(getButton(buttonid),eventid);
        return event.getAttribute(key);
    }
    public void initialize_events(){
        /*
         * Initializes the ShieldButtons one for each button on the Shield
         * as well as the ComEvents associated with them
         */
        if(doc!=null){
            ecu1=new ShieldButton(ShieldEvent.ECU1,getButton(ShieldEvent.ECU1));
            ecu2=new ShieldButton(ShieldEvent.ECU2,getButton(ShieldEvent.ECU2));
            ecu3=new ShieldButton(ShieldEvent.ECU3,getButton(ShieldEvent.ECU3));
            ecu4=new ShieldButton(ShieldEvent.ECU4,getButton(ShieldEvent.ECU4));
            e1 = new ShieldButton(ShieldEvent.E1,getButton(ShieldEvent.E1));
            e2 = new ShieldButton(ShieldEvent.E2,getButton(ShieldEvent.E2));
        } 
    }
    public ShieldButton getShieldButton(int number){
        
        /*
         * returns the ShieldButton decided by the button number or the buttonid
         * returns null if the button number is not valid.
         */
        ShieldButton buttonret=null;
        switch(number){
            case ShieldEvent.ECU1:
                buttonret= ecu1;
                break;
            case ShieldEvent.ECU2:
                buttonret= ecu2;
                break;
            case ShieldEvent.ECU3:      
                buttonret= ecu3;
                break;
            case ShieldEvent.ECU4:
                buttonret= ecu4;
                break;
            case  ShieldEvent.E1:
                buttonret= e1;
                break;
            case ShieldEvent.E2:
                buttonret= e2;
                break;
        }
        return buttonret;
    }
    
    public String getPassword(){
        return rootElement.getAttribute("password");
    }
    public int getChoice(){
        int x=-1;
        x=Integer.parseInt(rootElement.getAttribute("connection"));
        return x;
    }
}
