/*FreeMind - a program for creating and viewing mindmaps
 *Copyright (C) 2000-2006  Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 *See COPYING for details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
/*$Id: EditNodeWYSIWYG.java,v 1.1.4.36 2008/04/11 18:34:21 dpolivaev Exp $*/

package freemind.view.mindmapview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import com.lightdev.app.shtm.SHTMLPanel;

import freemind.main.FreeMindMain;
import freemind.main.HtmlTools;
import freemind.main.Tools;
import freemind.modes.ModeController;
import freemind.view.mindmapview.EditNodeBase.EditDialog.CancelAction;

/**
 * @author Daniel Polansky
 *
 */
public class EditNodeWYSIWYG extends EditNodeBase {
    
    private KeyEvent firstEvent;
    
    private static HTMLDialog htmlEditorWindow;
    private static class HTMLDialog extends EditDialog{
        private SHTMLPanel htmlEditorPanel;
        HTMLDialog(EditNodeBase base) throws Exception {
            super(base);
            createEditorPanel();
            getContentPane().add(htmlEditorPanel, BorderLayout.CENTER);
            Tools.addEscapeActionToDialog(this, new CancelAction());
            final JButton okButton = new JButton();
            final JButton cancelButton = new JButton();
            final JButton splitButton = new JButton();

            Tools.setLabelAndMnemonic(okButton, base.getText("ok"));
            Tools.setLabelAndMnemonic(cancelButton, base.getText("cancel"));
            Tools.setLabelAndMnemonic(splitButton, base.getText("split"));
             
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    submit();
                }
            });
            
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
            
            splitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    split();
                }
            });
            
            JPanel buttonPane = new JPanel();
            buttonPane.add(okButton);
            buttonPane.add(cancelButton);
            buttonPane.add(splitButton);
            buttonPane.setMaximumSize(new Dimension(1000, 20));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            
        }
        private SHTMLPanel createEditorPanel() throws Exception {
            if(htmlEditorPanel == null){
                htmlEditorPanel  = SHTMLPanel.createSHTMLPanel();
            }
            return htmlEditorPanel;     
        }
        /**
         * @return Returns the htmlEditorPanel.
         */
        public SHTMLPanel getHtmlEditorPanel() {
            return htmlEditorPanel;
        }
        /* (non-Javadoc)
         * @see freemind.view.mindmapview.EditNodeBase.Dialog#close()
         */
        protected void submit() {
            htmlEditorPanel.getDocument().getStyleSheet().removeStyle("body");
            if (htmlEditorPanel.needsSaving()) {
                getBase().getEditControl().ok(HtmlTools.unescapeHTMLUnicodeEntity
                        (htmlEditorPanel.getDocumentText())); }
            else{
                getBase().getEditControl().cancel();
            }
            super.submit();
        }
        /* (non-Javadoc)
         * @see freemind.view.mindmapview.EditNodeBase.Dialog#split()
         */
        protected void split() {
            htmlEditorPanel.getDocument().getStyleSheet().removeStyle("body");
            getBase().getEditControl().split(HtmlTools.unescapeHTMLUnicodeEntity
                    (htmlEditorPanel.getDocumentText()),
                    htmlEditorPanel.getCaretPosition());
            super.split();
        }
        
        /* (non-Javadoc)
         * @see freemind.view.mindmapview.EditNodeBase.Dialog#close()
         */
        protected void cancel() {
            htmlEditorPanel.getDocument().getStyleSheet().removeStyle("body");
            getBase().getEditControl().cancel();
            super.cancel();
        }
        
        protected boolean isChanged() {
            return htmlEditorPanel.needsSaving();
        }
		public Component getMostRecentFocusOwner() {
			if (isFocused()) {
				return getFocusOwner();
			}
			else {
				return htmlEditorPanel.getMostRecentFocusOwner();
			}
		}
    }
    public EditNodeWYSIWYG
    (final NodeView node,
            final String text,
            final KeyEvent firstEvent,
            ModeController controller,
            EditControl editControl) {
        super(node, text, controller, editControl);
        this.firstEvent = firstEvent; }
    public void show() {
        // Return true if successful.
        try {
            final FreeMindMain frame = getFrame();
            if (htmlEditorWindow == null) {             
                htmlEditorWindow = new HTMLDialog(this);                          
            }
            htmlEditorWindow.setBase(this);
            final SHTMLPanel htmlEditorPanel = ((HTMLDialog)htmlEditorWindow).getHtmlEditorPanel();
            String rule = "BODY {";
            final Font font = node.getTextFont();
            final Color nodeTextBackground = node.getTextBackground();
            rule += "font-family: "+font.getFamily()+";";
            rule += "font-size: "+font.getSize()+"pt;";
            if (node.getModel().isItalic()) {
                rule+="font-style: italic; "; }
            if (node.getModel().isBold()) {
                rule+="font-weight: bold; "; }
            final Color nodeTextColor = node.getTextColor();
			rule+="color: "+Tools.colorToXml(nodeTextColor)+";"; 
            rule += "}\n";
            rule += "p {";
            rule += "margin-top:0;";            
            rule += "}\n";
            final HTMLDocument document = htmlEditorPanel.getDocument();
            final JEditorPane editorPane = htmlEditorPanel.getEditorPane();
            editorPane.setForeground(nodeTextColor);
            editorPane.setBackground(nodeTextBackground);
            editorPane.setCaretColor(nodeTextColor);
			document.getStyleSheet().addRule(rule);
			try {
				document.setBase(node.getMap().getModel().getURL());
			}
			catch (MalformedURLException e) {} 
            
            //{ -- Set size (can be refactored to share code with long node editor)
            int preferredHeight = (int)(node.getMainView().getHeight() * 1.2);
            preferredHeight =
                Math.max (preferredHeight, Integer.parseInt(frame.getProperty("el__min_default_window_height")));
            preferredHeight =
                Math.min (preferredHeight, Integer.parseInt(frame.getProperty("el__max_default_window_height")));
            int preferredWidth = (int)(node.getMainView().getWidth() * 1.2);
            preferredWidth =
                Math.max (preferredWidth, Integer.parseInt(frame.getProperty("el__min_default_window_width")));
            preferredWidth =
                Math.min (preferredWidth, Integer.parseInt(frame.getProperty("el__max_default_window_width")));
            htmlEditorPanel.setContentPanePreferredSize(new Dimension(preferredWidth, preferredHeight));
            //}
            
            htmlEditorWindow.pack();
            
            Tools.setDialogLocationRelativeTo(htmlEditorWindow, node.getMainView());
            
    		String content = node.getModel().toString();
    		if (!HtmlTools.isHtmlNode(content)) {
    			content = HtmlTools.plainToHTML(content);
    		}
    		htmlEditorPanel.setCurrentDocumentContent(content); 
    		if (firstEvent instanceof KeyEvent) {
    			final KeyEvent firstKeyEvent = (KeyEvent) firstEvent;
    			final JTextComponent currentPane = htmlEditorPanel.getEditorPane();
    			if(currentPane == htmlEditorPanel.getMostRecentFocusOwner()){
    				redispatchKeyEvents(currentPane, firstKeyEvent);
    			}
    		} // 1st key event defined
    		htmlEditorPanel.getMostRecentFocusOwner().requestFocus();
            htmlEditorWindow.show();
        }
        catch (Exception ex) { // Probably class not found exception
        	freemind.main.Resources.getInstance().logException(ex);
        	System.err.println("Loading of WYSIWYG HTML editor failed. Use the other editors instead."); 
        }}
}

