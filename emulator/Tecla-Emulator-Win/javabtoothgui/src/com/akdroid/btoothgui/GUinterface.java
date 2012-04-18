package com.akdroid.btoothgui;
import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.event.MouseEvent;

import java.awt.event.MouseListener;

import java.awt.image.BufferedImage;


public class GUinterface {
	
	Window w;
	Button ecu1,ecu2,ecu3,ecu4,e1,e2,e3;
	JLabel status,switchstate,dummy;
	BufferedImage im;
	JFrame f;
	JFrame d;
	Byte state;
	BTserver bt;//=new BTserver();
			//bt.start();
GUinterface(){
	w=null;
	d=new JFrame("TeclaEmulator");
	d.setBounds(100, 100, 600, 400);
	state=0x3F;
	gui_init();
	bt=new BTserver();
	bt.start();
}
public void gui_init()
{
	f=new JFrame();
	ecu1=new Button();
	ecu2=new Button();
	ecu3=new Button();
	ecu4=new Button();
	e1=new Button();
	e2=new Button();
	e3=new Button();
	status=new JLabel();
	switchstate=new JLabel();
	dummy=new JLabel();
	switchstate.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	status.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	ecu1.setLabel("ECU1");
	ecu2.setLabel("ECU2");
	ecu3.setLabel("ECU3");
	ecu4.setLabel("ECU4");
	e1.setLabel(" E1 ");
	e2.setLabel(" E2 ");
	
	d.add(ecu1);
	d.add(ecu2);
	d.add(ecu3);
	d.add(ecu4);
	d.add(e1);
	d.add(e2);
	d.add(status);
	d.add(switchstate);
	d.add(dummy);
	int xoffset=50,yoffset=150;
	ecu1.setBounds(125+xoffset, 40+yoffset, 100, 60);
	ecu2.setBounds(125+xoffset, 120+yoffset, 100, 60);
	ecu3.setBounds(5+xoffset, 120+yoffset, 100, 60);
	ecu4.setBounds(245+xoffset, 120+yoffset, 100, 60);
	e1.setBounds(365+xoffset, 40+yoffset, 100, 60);
	//System.out.println("e2 bounds");
	e2.setBounds(365+xoffset, 120+yoffset, 100, 60);
	status.setBounds(xoffset-25, yoffset-50, 350, 60);
	status.setForeground(new Color(0x0000FF));
	status.setBackground(new Color(0xFFFFFF));
	switchstate.setForeground(new Color(0x0000FF));
	switchstate.setBackground(new Color(0xFF0000));
	status.setText("Welcome to TeclaEmulator");
	switchstate.setBounds(410 ,yoffset-50, 150, 60);
	switchstate.setText("SwitchState = 0x3F");
	ecu1.addMouseListener(new TeclaListener((byte)0x01,"ECU1"));
	ecu2.addMouseListener(new TeclaListener((byte)0x02,"ECU2"));
	ecu3.addMouseListener(new TeclaListener((byte)0x04,"ECU3"));
	ecu4.addMouseListener(new TeclaListener((byte)0x08,"ECU4"));
	e1.addMouseListener(new TeclaListener((byte)0x10,"E1"));
	e2.addMouseListener(new TeclaListener((byte)0x20,"E2"));
	//ecu1.setBounds(720, 220, 100, 80);
	//d= new Dialog(w,"TeclaEmulator");
}
@SuppressWarnings("deprecation")
public void show(){
	System.out.println("gui show");
	d.show();
	
}
@SuppressWarnings("deprecation")
public void hide(){
	d.hide();
}


class TeclaListener implements MouseListener {
	Byte mask;
	String name;
	TeclaListener(byte st,String nm){
		mask=st;
		name=nm;
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		state=(byte) (state & ~mask);
		switchstate.setText("switchstate = "+Integer.toHexString(state));
		status.setText(name + " Pressed ");
		bt.send(state);
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		state=(byte) (state | mask);
		switchstate.setText("switchstate = "+Integer.toHexString(state));
		status.setText(name + " Released ");
		bt.send(state);
	}
}


}
