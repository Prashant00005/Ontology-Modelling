package kdeProjectQuestions;

import org.apache.jena.rdf.model.Model;

import com.vividsolutions.jts.io.ParseException;

import kdeProjectLibs.OntologyLoading;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import java.awt.*;

/** Implement a simple graphical interface. Run OntologyLoading before running this class. */
public class gui 
{

	 private JFrame frame;
	 private JPanel p, p1;
	 private JButton load;
	 private JButton runQuery;
	 
	 private JRadioButton r1,r2,r3,r4,r5,r6,r7,r8,r9,r10;
	 private JTextArea ta;
	 Model unionModel;
	 
	 private ButtonGroup bg;
	 private JLabel label;
	 private JLabel label_q;
	 
	 
	 public static void main(String[] args) throws IOException {
	 
	 new gui();
	 }

	 public gui()throws IOException
	 {
		 frame = new JFrame("User Interface");
    	 
		 p = new JPanel(new GridLayout(13,0));
		 //p.setSize(50,50);
		 p1 = new JPanel(new GridLayout(3,1));
		 
		label = new JLabel();
		label_q = new JLabel();
		
		ta = new JTextArea(30,100);
		ta.setFont(new Font("monospaced", Font.PLAIN,12));
		System.setOut(new PrintStream(System.out) {
            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);

                String msg = new String(buf, off, len);

                ta.setText(ta.getText() + msg);
                ta.update(ta.getGraphics());
            }
        });
		
		load = new JButton("Load Ontology");
    	 	runQuery = new JButton("Run Query");
    	 	
    	 	runQuery.setEnabled(false);
    	 	load.addActionListener(
    			 new ActionListener() 
    			 {

    				 @Override
    			public void actionPerformed(ActionEvent e)
    			{
    					 
    				try {
    						
						load_ont();
						JOptionPane.showMessageDialog(frame, "Ontology Loaded. You may run queries now");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    			}
    		 
    			 }
    	 			);
    	 
    	 	runQuery.addActionListener(
    			 new ActionListener() 
    			 {

    				 @Override
    				 public void actionPerformed(ActionEvent e) {
    					 
    					 try {
							run_query();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    				 }
    		 
    			 }
    	 			);
    	 	
    	 	
    	 	r1 = new JRadioButton("01. What is the county with the lowest density of adults from 20 to 29 (both included) in the 2011 census?");
    	 	r2 = new JRadioButton("02. What pair of adjacent Electoral Division have the highest absolute difference of total population in the 2011 census?"); 
    	 	r3 = new JRadioButton("03. Which county has the highest immigration density in the 2011 census?"); 
    	 	r4 = new JRadioButton("04. Which county has the highest density population aged 5 years and over who travel to work, school or college by" + "\n" + "Motorcycle or Scooter in the 2011 census?"); 
    	 	r5 = new JRadioButton("05. What is the county with the lowest population density of 15 years or older studying Mathematics and Computing?"); 
    	 	r6 = new JRadioButton("06. What is the county with the lowest density of adults with disabilities from 25 - 44(both included) in the 2011 census?"); 
    	 	r7 = new JRadioButton("07. What is  the county with the highest density of people aged 3 or over can speak Irish in the 2011 census?"); 
    	 	r8 = new JRadioButton("08. What are the legal towns or cities in a neighbouring county of Cork with the lowest and highest number of children with age lower than 10 in the 2011 census?"); 
    	 	r9 = new JRadioButton("09. Which county has the largest and smallest land per capita in the 2011 census?"); 
    	 	r10 = new JRadioButton("10. What are the legal towns or cities in a neighbouring county of Dublin with the lowest number of people aged over 15 studying Health and Welfare in the 2011 census?"); 
    	 	
    	 	bg = new ButtonGroup();    
    	 	bg.add(r1);
    	 	bg.add(r2); 
    	 	bg.add(r3); 
    	 	bg.add(r4); 
    	 	bg.add(r5); 
    	 	bg.add(r6); 
    	 	bg.add(r7); 
    	 	bg.add(r8); 
    	 	bg.add(r9); 
    	 	bg.add(r10); 
    	 	
    	 	r1.setSelected(true);
    	 	
    	 	p.add(load);
    	 	p.add(r1);
    	 	p.add(r2);
    	 	p.add(r3);
    	 	p.add(r4);
    	 	p.add(r5);
    	 	p.add(r6);
    	 	p.add(r7);
    	 	p.add(r8);
    	 	p.add(r9);
    	 	p.add(r10);
    	 	
    	 	p.add(runQuery);
    	 	
    	 	p1.add(label_q);
    	 	p1.add(label);
    	 	
    	 	
    	 	frame.add(new JScrollPane(ta));
    		frame.add(BorderLayout.PAGE_START,p);
    		p.setLocation(150, 10);
    	 
    	 	frame.setSize(1100,700);
    	 	frame.setResizable(false);
    	 	frame.setLocationRelativeTo(null);
    	 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	 	frame.setVisible(true);
	 }	
	 
	 public void run_query() throws IOException, ParseException
	 {
		 String select;
		 for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();) {
	            AbstractButton button = buttons.nextElement();
	            
	            if (button.isSelected()) {
	            		select = button.getText();
	                run_question(select);
	            }
	        }
	 }
	 
	 public void run_question(String s)
	 {
		 if(s.startsWith("01"))
			AllQuestions.q1(unionModel); 
		 else if(s.startsWith("02"))
				AllQuestions.q2(unionModel); 
		 else if(s.startsWith("03"))
				AllQuestions.q3(unionModel); 
		 else if(s.startsWith("04"))
				AllQuestions.q4(unionModel); 
		 else if(s.startsWith("05"))
				AllQuestions.q5(unionModel); 
		 else if(s.startsWith("06"))
				AllQuestions.q6(unionModel); 
		 else if(s.startsWith("07"))
				AllQuestions.q7(unionModel); 
		 else if(s.startsWith("08"))
				AllQuestions.q8(unionModel); 
		 else if(s.startsWith("09"))
				AllQuestions.q9(unionModel); 
		 else
			 AllQuestions.q10(unionModel); 
		 
	 }
	 
	 public void load_ont() throws IOException, ParseException
	 {
		 
		 OntologyLoading.loadAllDatasets(true);
		 unionModel = OntologyLoading.getUnionModel();
		 runQuery.setEnabled(true);
		 load.setEnabled(false);
		 System.out.println("Ontology Loaded!");
		 
	 }
}
