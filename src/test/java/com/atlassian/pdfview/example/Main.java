package com.atlassian.pdfview.example;

import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;

public class Main
{
    static final JTextField filesource = new JTextField("/Users/tue.dang/Downloads/pdfrenderer/CONF-19819/Balta IT - Wakkensteenweg 2 Sint-Baafs-Vijve.pdf");
    
    static JTextField pageNumber;
    
    static PdfImageLoader pdfImageLoader;
    static JLabel imageHolder = new JLabel();
    static final JComboBox listFile = new JComboBox();
    
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
        filesource.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
              warn();
            }
            public void removeUpdate(DocumentEvent e) {
              warn();
            }
            public void insertUpdate(DocumentEvent e) {
              warn();
            }

            public void warn() {
                String[] extFiles = {"pdf"};
                Collection<File> pdfFiles = FileUtils.listFiles(new File(filesource.getText().trim()), extFiles, true);
                listFile.removeAllItems();
                for (File file : pdfFiles) {
                    listFile.addItem(file);
                }
                
                
            }
          });
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
                    String fsrc;
                    if (new File(filesource.getText()).isFile()) {
                        fsrc = filesource.getText();
                    } else {
                        fsrc = listFile.getSelectedItem().toString();
                    }
                    pdfImageLoader = new PdfImageLoader(fsrc);
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
        
        //list file
        p.add(loadPdf);
        frame.getContentPane().add(p);
        listFile.updateUI();
        frame.getContentPane().add(listFile);
        
        frame.add(imageHolder);
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
