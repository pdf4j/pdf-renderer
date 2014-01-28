package com.atlassian.pdfview.example;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PdfImageLoader
{
    private PDFFile pdfFile;
    private File pdfSystemFile;

    public PdfImageLoader(String file) throws IOException
    {
        pdfSystemFile = new File(file);
        byte[] bytes = FileUtils.readFileToByteArray(pdfSystemFile);
        ByteBuffer b = ByteBuffer.wrap(bytes);
        pdfFile = new PDFFile(b);
    }

    public BufferedImage loadPage(int pageNumber, float scale)
    {
        if (pdfFile.getNumPages() < pageNumber)
        {
            return null;
        }
        
        PDFPage page = pdfFile.getPage(pageNumber);
        
        Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
        Image img = page.getImage((int)(rect.width*scale), (int) (rect.height*scale), rect, null, true, true);
        
        BufferedImage bufferedImage = new BufferedImage((int)(rect.width*scale), (int) (rect.height*scale), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        bufferedImage.flush();
        return bufferedImage;
    }
    
    public BufferedImage loadPage(int pageNumber)
    {
        return loadPage(pageNumber, 1.0f);
    }
    
    public int getNumberOfPages() 
    {
        return pdfFile.getNumPages();
    }

    public void generateImage(File path) throws IOException
    {
        final String extImage = "jpg";
        int numOfPages = pdfFile.getNumPages();

        for (int i = 1; i <= numOfPages; i++)
        {
            String pdfName = pdfSystemFile.getName(); 
            File folderImage = new File(path, pdfName);
            if (!folderImage.exists())
            {
                folderImage.mkdirs();
            }
            File outputFile = new File(folderImage, pdfName+"-page_" + String.format("%02d", i) + "." + extImage);
            if (outputFile.exists())
            {
                outputFile.delete();
            }

            BufferedImage bufferedImage = loadPage(i);
            ImageIO.write(bufferedImage, "jpg", outputFile);

        }
    }

}
