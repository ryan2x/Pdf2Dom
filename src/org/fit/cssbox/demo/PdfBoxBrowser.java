/**
 * PdfBoxBrowser.java
* (c) Radek Burget, 2011
 *
 * PdfDOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * PdfDOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 *
 * Created on 4.10.2011, 11:22:11 by burgetr
 */
package org.fit.cssbox.demo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;
import javax.swing.tree.DefaultTreeModel;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.pdf.PdfBrowserCanvas;
import org.w3c.dom.Document;

/**
 * This demo shows a simple browser of PDF files based on transforming the files to DOM and rendering by CSSBox.
 * It is based on the {@link BoxBrowser} demo from the CSSBox packages, only the document pre-processing part is changed.
 * 
 * @author burgetr
 */
public class PdfBoxBrowser extends org.fit.cssbox.demo.BoxBrowser
{

    @Override
    public void displayURL(String urlstring)
    {
        try {
            if (!urlstring.startsWith("http:") &&
                !urlstring.startsWith("ftp:") &&
                !urlstring.startsWith("file:"))
                    urlstring = "http://" + urlstring;
            
            URL url = new URL(urlstring);
            urlText.setText(url.toString());
            
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; BoxBrowserTest/2.x; Linux) CSSBox/2.x (like Gecko)");
            InputStream is = con.getInputStream();
            
            System.out.println("Parsing PDF: " + url);
            PDDocument doc = loadPdf(is);
            
            is.close();

            contentCanvas = new PdfBrowserCanvas(doc, null, contentScroll.getSize(), url);
            contentCanvas.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e)
                {
                    System.out.println("Click: " + e.getX() + ":" + e.getY());
                    canvasClick(e.getX(), e.getY());
                }
                public void mousePressed(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) { }
                public void mouseEntered(MouseEvent e) { }
                public void mouseExited(MouseEvent e) { }
            });
            contentScroll.setViewportView(contentCanvas);

            //box tree
            Viewport viewport = ((BrowserCanvas) contentCanvas).getViewport();
            root = createBoxTree(viewport);
            boxTree.setModel(new DefaultTreeModel(root));
            
            //dom tree
            Document dom = ((PdfBrowserCanvas) contentCanvas).getBoxTree().getDocument();
            domTree.setModel(new DefaultTreeModel(createDomTree(dom)));
            
            //=============================================================================
           
        } catch (Exception e) {
            System.err.println("*** Error: "+e.getMessage());
            e.printStackTrace();
        }
        
    }

    protected PDDocument loadPdf(InputStream is) throws IOException
    {
        PDDocument document = null;
        document = PDDocument.load(is);
        if( document.isEncrypted() )
        {
            try
            {
                document.decrypt("");
            }
            catch( InvalidPasswordException e )
            {
                System.err.println( "Error: Document is encrypted with a password." );
                System.exit( 1 );
            }
            catch( CryptographyException e )
            {
                System.err.println( "Error: Document is encrypted with a password." );
                System.exit( 1 );
            }
        }
        return document;
    }
    
    public static void main(String[] args)
    {
        browser = new PdfBoxBrowser();
        JFrame main = browser.getMainWindow();
        main.setSize(1200,600);
        main.setVisible(true);
    }

}
