package de.fu_berlin.inf.dpp.test.jupiter.ui;

import javax.swing.JFrame;

import org.apache.log4j.PropertyConfigurator;


public class UITest {
	static {
		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
	}
//	public void testStartUI() throws Exception{
//		JupiterTestUI testUI = new JupiterTestUI();
//		testUI.setVisible(true);
//		System.out.println("test");
//	}
	
	public static void main(String[] args){
		JupiterTestUI ui = new JupiterTestUI();
//		
		JFrame frame = new JFrame();
		frame.add(ui);
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( 508, 452 ); 
	    
	    frame.setVisible( true ); 
	    
	}
}
