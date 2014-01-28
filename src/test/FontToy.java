/*
 * $Id: FontToy.java,v 1.3 2007/12/20 18:33:32 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package test;

/*
 * FontToy.java
 *
 * Created on September 15, 2003, 7:38 AM
 */
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Font;

import java.awt.event.*;
import java.awt.geom.*;

import java.io.*;

import java.util.*;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.*;
import javax.swing.event.*;

import com.sun.pdfview.*;
import com.sun.pdfview.font.*;


/**
 *
 */
public class FontToy extends JPanel {
    /** the panel to draw to */
    private static FontToy toy;
    
    /** the list of glyphs in the current font */
    private static JComboBox glyphBox;
    
    /** the fonts */
    private Set fonts;
    
    /** the current font */
    private PDFFont font;
    
    /** the current glyph */
    private PDFGlyph glyph;
    
    /** the font to draw glyphs in */
    private Font gfont;
    
    /** Creates a new instance of FontToy */
    public FontToy(PDFFile pdf) throws IOException {
        PDFObject root = pdf.getRoot();
        
        PDFObject pagesObj = (PDFObject) root.getDictRef("Pages");
        fonts = findFonts(pagesObj, new HashMap());

        gfont = new Font("Sans-serif", Font.PLAIN, 10);
    }
    
    /** get the set of associated fonts */
    public Set getFonts() {
        return fonts;
    }
    
    /** get the current font */
    public PDFFont getCurrentFont() {
        return font;
    }
    
    /** set the current font */
    public void setCurrentFont(PDFFont font) {
        this.font = font;
    }
    
    /** get the current glyph */
    public PDFGlyph getCurrentGlyph() {
        return glyph;
    }
    
    /** set the current glyph */
    public void setCurrentGlyph(PDFGlyph glyph) {
        this.glyph = glyph;
    }
    
    /**
     * paint the current glyph on the screen
     */
    public void paint(Graphics g) {
//        System.out.println("Repaint!");
        
        Graphics2D g2 = (Graphics2D) g;
        
        int width = getWidth();
        int height = getHeight();
        
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        
        g2.setColor(Color.BLACK);
        
        if (glyph == null) {
            return;
        }
        
        GeneralPath gp = glyph.getShape();
        PDFPage page = glyph.getPage();
        if (gp != null) {
            drawShape(g2, gp, width, height);
        } else if (page != null) {
            drawPage(g2, page, width, height);
        }
    }
    
    /**
     * Draw a shpae in fancy outline form
     */
    private void drawShape(Graphics2D g2, GeneralPath gp, int w, int h) {
//        System.out.println("Drawing shape in: " + w + " x " + h);
        
        float curX = 0;
        float curY = 0;
        float startX = 0;
        float startY = 0;
        
        Rectangle2D border = gp.getBounds2D(); 
        
        double scaleX = (w - 20) / border.getWidth();
        double scaleY = (h - 20) / border.getHeight();
        
        if (scaleX < scaleY) {
            scaleY = scaleX;
        } else {
            scaleX = scaleY;
        }
        
        double transX = 10 - (border.getX() * scaleX);
        double transY = h - 10 + (border.getY() * scaleY);
        
        AffineTransform at = new AffineTransform(scaleX, 0, 0, -scaleY, 
                                                 transX, transY);
        
        Rectangle2D borderTrans = gp.createTransformedShape(at).getBounds2D();
        
        g2.setColor(Color.CYAN);
        g2.fill(gp.createTransformedShape(at));
        g2.setColor(Color.BLACK);
        
        int num = 0;
        
        PathIterator pi = gp.getPathIterator(at);
        while (!pi.isDone()) {
            float[] coords = new float[6];
            
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    curX = coords[0];
                    curY = coords[1];
                    
                    drawPoint(g2, num++, curX, curY, false);
                    
                    startX = curX;
                    startY = curY;                
                    break;
                case PathIterator.SEG_LINETO:
                    Line2D line = new Line2D.Float(curX, curY, 
                                                   coords[0], coords[1]);
                    g2.draw(line);
                    
                    drawPoint(g2, num++, coords[0], coords[1], false);
                    
                    curX = coords[0];
                    curY = coords[1];
                    break;
                case PathIterator.SEG_CUBICTO:
                    CubicCurve2D curve = new CubicCurve2D.Float(curX, curY, 
                                                                coords[0], 
                                                                coords[1],
                                                                coords[2],
                                                                coords[3],
                                                                coords[4],
                                                                coords[5]);
                    g2.draw(curve);
                    
                    drawPoint(g2, num++, coords[0], coords[1], true);
                    drawPoint(g2, num++, coords[2], coords[3], true);
                    drawPoint(g2, num++, coords[4], coords[5], false);
                    
                    curX = coords[4];
                    curY = coords[5];
                    break;
                case PathIterator.SEG_QUADTO:
                    QuadCurve2D curveQ = new QuadCurve2D.Float(curX, curY,
                                                               coords[0],
                                                               coords[1],
                                                               coords[2],
                                                               coords[3]);
                    g2.draw(curveQ);
                    
                    drawPoint(g2, num++, coords[0], coords[1], true);
                    drawPoint(g2, num++, coords[2], coords[3], false);
                    
                    curX = coords[2];
                    curY = coords[3];
                    break;
                case PathIterator.SEG_CLOSE:
                    Line2D.Float line2 = new Line2D.Float(curX, curY, startX, startY);
                    
                    g2.draw(line2);
                    curX = startX;
                    curY = startY;
                    break;
                    
            }
            
            pi.next();
        }
    }
    
    /**
     * Draw and label a point
     */
    public void drawPoint(Graphics2D g, int num, float x, float y, boolean curvectl) {
        GeneralPath gp = new GeneralPath();
	if (curvectl) {
            gp.moveTo(x - 1, y - 1);
            gp.lineTo(x + 1, y + 1);
            gp.moveTo(x - 1, y + 1);
            gp.lineTo(x + 1, y - 1);
        } else {
            gp.moveTo(x - 1, y - 1);
            gp.lineTo(x - 1, y + 1);
            gp.lineTo(x + 1, y + 1);
            gp.lineTo(x + 1, y - 1);
            gp.closePath();
        }
	
        g.setColor(Color.red);
        g.draw(gp);
	g.setColor(Color.blue);
	g.setFont(gfont);
	g.drawString(String.valueOf(num), x + 3, y + 3);
    }
    
      
    /**
     * Draw a page
     */
    private void drawPage(Graphics2D g2, PDFPage page, int w, int h) {
//        System.out.println("Drawing page in: " + w + " x " + h);
    
        Dimension pageSize = page.getUnstretchedSize(w - 20, h - 20, null);
        Image image = page.getImage(pageSize.width, pageSize.height, null, null, true, true);
    
        g2.drawImage(image, 0, 0, null);
    }
    
    /**
     * Walk the PDF Tree looking for fonts
     *
     * @param pagedict the top of the pages tree
     * @param resources a HashMap that will be filled with any resource
     *                  definitions encountered on the search for the page
     */
    private Set findFonts(PDFObject pagedict, Map resources) 
        throws IOException
    {
        Set outSet = new HashSet();
        
        PDFObject rsrcObj = pagedict.getDictRef("Resources");
        if (rsrcObj != null) {
            // copy the resources so we don't overwrite them in
            // children
            HashMap rsrcMap = new HashMap();
            rsrcMap.putAll(resources);
            
            Map rsrc = rsrcObj.getDictionary();
            rsrcMap.putAll(rsrc);
            
            if (rsrc.containsKey("Font")) {
                PDFObject fontsObj = (PDFObject) rsrc.get("Font");
            
                for (Iterator i = fontsObj.getDictKeys(); i.hasNext();) {
                    String key = (String) i.next();
                    PDFObject fontObj = (PDFObject) fontsObj.getDictRef(key);

                    try {
                        PDFFont font = PDFFont.getFont(fontObj, rsrcMap);
                        
//                        System.out.println("Found font: " + font.getBaseFont());
                        
                        outSet.add(font);
                    } catch (Exception ex) {
                        // oh well
                        System.out.println("Error finding font from " + fontObj);
                        ex.printStackTrace();
                    }
                }
            }
            
            // look at XObjects for fonts as well
            if (rsrc.containsKey("XObject")) {
                PDFObject xobjsObj = (PDFObject) rsrc.get("XObject");
                
                for (Iterator i = xobjsObj.getDictKeys(); i.hasNext();) {
                    String key = (String) i.next();
                    PDFObject xobj = (PDFObject) xobjsObj.getDictRef(key);
                    outSet.addAll(findFonts(xobj, new HashMap()));
                }
            }
            
            resources = rsrcMap;
        }
        
        PDFObject kidsObj = pagedict.getDictRef("Kids");
        if (kidsObj != null) {
            PDFObject[] kids = kidsObj.getArray();
            for (int i = 0; i < kids.length; i++) {
                outSet.addAll(findFonts(kids[i], resources));
            }
        }
        
        return outSet;
    }
    
    /**
     * Called when a key is typed
     */
    private static void keyPressed(KeyEvent k) {
        int curIndex = glyphBox.getSelectedIndex();
        int nextIndex = curIndex;
        
        if (k.getKeyCode() == KeyEvent.VK_LEFT) {
            nextIndex--;
            if (nextIndex < 0) {
                nextIndex = glyphBox.getItemCount() - 1;
            }
        } else if (k.getKeyCode() == KeyEvent.VK_RIGHT) {
            nextIndex++;
            
            if (nextIndex >= glyphBox.getItemCount()) {
                nextIndex = 0;
            }
        }
        
        if (nextIndex != curIndex) {
            glyphBox.setSelectedIndex(nextIndex);
        }
    }
    
    /**
     * Called when a new glyph is selected
     */
    private static void glyphSelected(Integer glyphID) {
//        System.out.println("Glyph " + glyphID + " selected.");
        
        char glyphChar = (char) (glyphID.intValue());
        String s = String.valueOf(glyphChar);
        
        PDFFont font = toy.getCurrentFont();
        
        List l = font.getGlyphs(s);
        
        PDFGlyph glyph = (PDFGlyph) l.get(0);
        
        toy.setCurrentGlyph(glyph);
        toy.repaint();
    }
    
    /**
     * Called when a new font is selected
     */
    private static void fontSelected(PDFFont font) {
//        System.out.println("Font " + font + " selected.");
        
        toy.setCurrentFont(font);
        
        int start = 0;
        int end = 255;
        
        if (font instanceof OutlineFont) {
            start = ((OutlineFont) font).getFirstChar();
            end = ((OutlineFont) font).getLastChar();
        } else if (font instanceof Type3Font) {
            start = ((Type3Font) font).getFirstChar();
            end = ((Type3Font) font).getLastChar();
        }
        
        if (start < 0) {
            start = 0;
        }
        
        if (end < 0) {
            end = 255;
        }

        
        Vector objs = new Vector(end - start + 1);
        for (int i = start; i <= end; i++) {
            objs.add(new Integer(i));
        }
        
        glyphBox.setModel(new DefaultComboBoxModel(objs));
        
        glyphSelected(new Integer(start));
    }
    
    /**
     * Create the frame and stuff
     */
    private static void createUI() 
        throws Exception
    {
        Box controlPanel = Box.createHorizontalBox();
        
        glyphBox = new JComboBox();
        glyphBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    glyphSelected((Integer) ie.getItem());
                }
            }
        });
        
        Object[] fontObjs = toy.getFonts().toArray();
        
        JComboBox comboBox = new JComboBox(fontObjs);
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    fontSelected((PDFFont) ie.getItem());
                }
            }
        });
        comboBox.setMaximumSize(new Dimension(200, 50));
      
        fontSelected((PDFFont) fontObjs[0]);
        
        controlPanel.add(new JLabel("Fonts:"));
        controlPanel.add(comboBox);
        
        controlPanel.add(Box.createHorizontalStrut(15));
        
        controlPanel.add(new JLabel("Glyphs:"));
        controlPanel.add(glyphBox);
        
        JPanel ftPanel = new JPanel();
        ftPanel.setLayout(new BorderLayout());
        
        ftPanel.add(controlPanel, BorderLayout.NORTH);
        ftPanel.add(toy, BorderLayout.CENTER);
        
        ftPanel.setFocusable(true);
        ftPanel.requestFocus();
        ftPanel.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent k) {
                keyPressed(k);
            }
        });
        
        JFrame jf = new JFrame("Font Toy");
        jf.setContentPane(ftPanel);
        
        jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(-1);
            }
        });
        
        jf.setSize(640, 480);
   
        jf.show();
    }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:");
            System.out.println("    FontToy <pdf-file>");
            System.exit(-1);
        }
        
        String fileName = args[0];
        
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf =
                channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            
            PDFFile file = new PDFFile(buf);
     
            toy = new FontToy(file);
            createUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
