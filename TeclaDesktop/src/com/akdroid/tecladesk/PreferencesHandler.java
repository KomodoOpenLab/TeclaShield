/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;
import com.akdroid.interfaces.ShieldEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
/**
 *
 * @author Akhil
 */
public class PreferencesHandler {
    File config;
    DocumentBuilderFactory docf;
    DocumentBuilder docb;
    public Document doc;
    FileWriter filew;
    Element rootElement;
    ShieldButton ecu1,ecu2,ecu3,ecu4,e1,e2;
    public PreferencesHandler(String filepath){
        config=new File(filepath);
        docf=DocumentBuilderFactory.newInstance();
        
            try {
                docb=docf.newDocumentBuilder();
              //  doc=docb.parse(config);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } 
       if(!config.exists())
           createXmlFile();
       else{
            try {
                doc=docb.parse(config);
                rootElement=(Element)doc.getFirstChild();
            } catch (SAXException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       initialize_events();
    }
    public void createXmlFile(){
        try {
            config.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        doc=docb.newDocument();
        rootElement=doc.createElement("TeclaClient");
        doc.appendChild(rootElement);
        makedefault(doc,rootElement);
        commitchanges(doc);
        
    }
    public void commitchanges(Document doc_){
        TransformerFactory tf =TransformerFactory.newInstance();
        try {
            Transformer transformer=tf.newTransformer();
            DOMSource source=new DOMSource(doc_) ;
            try {
                filew=new FileWriter(config);
            } catch (IOException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            StreamResult result=new StreamResult(filew);
            try {
                transformer.transform(source, result);
            } catch (TransformerException ex) {
                Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(PreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }  
    public Element addButton(Document document,Element root,int buttonid){
        Element buttonelem=document.createElement("ShieldButton");
        buttonelem.setAttribute("value",""+buttonid);
        
        root.appendChild(buttonelem);
        return buttonelem;
    }
    public Element addeventinfo(Document document,Element parent,int eventid,int device,int[] value,int[] options ){
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

        Element button=(Element)rootElement.getFirstChild();
        int j=10;
        while(button != null){
            if(button.getAttribute("value").equals(""+buttonid))
                break;
            button=(Element)button.getNextSibling();
            //System.out.println(j++);
        }
        return button;
    }
    public Element getShieldEvent(Element button,int eventid){
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
        Element event =getShieldEvent(getButton(buttonid),eventid);
        event.setAttribute(key, value);
    }
    public String getEventattribute(int buttonid,int eventid,String key){
        Element event =getShieldEvent(getButton(buttonid),eventid);
        return event.getAttribute(key);
    }
    public void initialize_events(){
        if(doc!=null){
            ecu1=new ShieldButton(ShieldEvent.ECU1,getButton(ShieldEvent.ECU1));
            ecu2=new ShieldButton(ShieldEvent.ECU2,getButton(ShieldEvent.ECU2));
            ecu3=new ShieldButton(ShieldEvent.ECU3,getButton(ShieldEvent.ECU3));
            ecu4=new ShieldButton(ShieldEvent.ECU4,getButton(ShieldEvent.ECU4));
            e1=new ShieldButton(ShieldEvent.E1,getButton(ShieldEvent.E1));
            e2=new ShieldButton(ShieldEvent.E2,getButton(ShieldEvent.E2));
        } 
    }
    public ShieldButton getShieldButton(int number){
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

}
