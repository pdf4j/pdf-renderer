package com.atlassian.pdfview.example;

import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Main
{
    static final JTextField filesource = new JTextField("/Users/tue.dang/Downloads/pdfrenderer/does not show correctly.pdf");
    
    static JTextField pageNumber;
    
    static PdfImageLoader pdfImageLoader;
    static JLabel imageHolder = new JLabel();
    
    public static int getPageNumber()
    {
        return new Integer(pageNumber.getText());
    }
    public static void resetPageNumber() 
    {
        pageNumber.setText("1");
    }
    
    public static void viewImage() throws IOException
    {
        //frame
        final JFrame frame = new JFrame("My incredible PDF document");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        
        //Panel with url pdf
        JPanel p = new JPanel(); 
        p.add(filesource);
        
        //Load button
        JButton loadPdf = new JButton("Load...");
        loadPdf.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e) 
            {
                resetPageNumber();
                try
                {
                    pdfImageLoader = new PdfImageLoader(filesource.getText());
                    Image img = pdfImageLoader.loadPage(getPageNumber());
                    imageHolder.setIcon(new ImageIcon(img));
                    frame.setTitle(frame.getTitle()+"/"+pdfImageLoader.getNumberOfPages());
                    frame.pack();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
        
        //view page button
        final JButton viewPageButton = new JButton("View page");
        viewPageButton.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e) 
            {
                if(pdfImageLoader==null) return;
                Image img = pdfImageLoader.loadPage(getPageNumber());
                imageHolder.setIcon(new ImageIcon(img));
                frame.pack();
            }
        });
        
        //next page button
        JButton nextPageButton = new JButton("Next page");
        nextPageButton.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e) 
            {
                int pnumber = getPageNumber();
                pnumber++;
                pageNumber.setText(String.valueOf(pnumber));

                if(pdfImageLoader==null) return;
                Image img = pdfImageLoader.loadPage(getPageNumber());
                imageHolder.setIcon(new ImageIcon(img));
                frame.pack();
            }
        });
        
        //Generate image button
        JButton generateImageButton = new JButton("Generate image");
        generateImageButton.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e) 
            {
                if(pdfImageLoader==null) return;
                File outFile = new File("target/generated-images");
                if(!outFile.exists()) 
                {
                    outFile.mkdirs();
                }
                try
                {
                    pdfImageLoader.generateImage(outFile);
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
        
        //page number
        pageNumber = new JTextField("1");
        
        //Generate image button
        
        
        frame.add(imageHolder);
        frame.getContentPane().add(p);
        frame.getContentPane().add(loadPdf);
        frame.getContentPane().add(generateImageButton);
        frame.getContentPane().add(pageNumber);
        frame.getContentPane().add(viewPageButton);
        frame.getContentPane().add(nextPageButton);
        frame.pack();
        frame.setVisible(true);

    }
    
    public static void main(final String[] args) throws IOException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    Main.viewImage();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

}
