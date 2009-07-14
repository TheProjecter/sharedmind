/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2007  Christian Foltin, Dimitry Polivaev and others.
 *
 *See COPYING for Details
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
 * Created on 11.09.2007
 */
/*$Id: NodeNoteRegistration.java,v 1.1.2.7 2008/04/12 21:46:00 christianfoltin Exp $*/

package accessories.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;

import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.TextResources;

import freemind.controller.MenuItemEnabledListener;
import freemind.controller.MenuItemSelectedListener;
import freemind.controller.actions.generated.instance.EditNoteToNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.extensions.HookRegistration;
import freemind.main.FreeMind;
import freemind.main.FreeMindMain;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.ModeController.NodeLifetimeListener;
import freemind.modes.ModeController.NodeSelectionListener;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.xml.ActorXml;
import freemind.view.mindmapview.NodeView;

public class NodeNoteRegistration implements HookRegistration, ActorXml, MenuItemSelectedListener {
	private static class SouthPanel extends JPanel {
		public SouthPanel() {
			super(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		}
		
		protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			return super.processKeyBinding(ks, e, condition, pressed) 
			|| e.getKeyChar() == KeyEvent.VK_SPACE
			|| e.getKeyChar() == KeyEvent.VK_ALT;			
		}
	}



	private final class NoteDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent arg0) {
            docEvent();
        }

        private void docEvent() {
            // make map dirty in order to enable automatic save on note
            // change.
            getMindMapController().getMap().setSaved(false);
        }

        public void insertUpdate(DocumentEvent arg0) {
            docEvent();
        }

        public void removeUpdate(DocumentEvent arg0) {
            docEvent();
        }
    }

    // private NodeTextListener listener;

    private final class NotesManager implements NodeSelectionListener,
            NodeLifetimeListener {

        private MindMapNode node;

        public NotesManager() {
        }

        public void onDeselectHook(NodeView node) {
            // logger.info("onLooseFocuse for node " + node.toString() + "
            // and noteViewerComponent=" + noteViewerComponent);
            noteViewerComponent.getDocument().removeDocumentListener(
                    mNoteDocumentListener);
            // store its content:
            onSaveNode(node.getModel());
            this.node = null;
            // getHtmlEditorPanel().setCurrentDocumentContent("Note", "");
        }

        public void onSelectHook(NodeView nodeView) {
            this.node = nodeView.getModel();
            HTMLDocument document = noteViewerComponent.getDocument();
            // remove listener to avoid unnecessary dirty events.
            document.removeDocumentListener(
                    mNoteDocumentListener);
			try {
				document.setBase(node.getMap().getURL());
			}
			catch (MalformedURLException e) {} 

            // logger.info("onReceiveFocuse for node " + node.toString());
            String note = node.getNoteText();
            if (note != null) {
                noteViewerComponent.setCurrentDocumentContent(note);
                mLastContentEmpty = false;
            } else if (!mLastContentEmpty) {
                noteViewerComponent.setCurrentDocumentContent("");
                mLastContentEmpty = true;
            }
            document.addDocumentListener(
                    mNoteDocumentListener);
        }

        public void onUpdateNodeHook(MindMapNode node) {
        }

        public void onSaveNode(MindMapNode node) {
            if (this.node != node) {
                return;
            }
            boolean editorContentEmpty = true;
            String documentText = noteViewerComponent.getDocumentText();
            // editorContentEmpty =
            // HtmlTools.removeAllTagsFromString(documentText).matches("[\\s\\n]*");
            editorContentEmpty = documentText.equals(NodeNote.EMPTY_EDITOR_STRING)
                    || documentText.equals(NodeNote.EMPTY_EDITOR_STRING_ALTERNATIVE);
            // logger.info("Current document: '" +
            // documentText.replaceAll("\n", "\\\\n") + "', empty="+
            // editorContentEmpty);
            controller.deregisterNodeSelectionListener(this);
            if (noteViewerComponent.needsSaving()) {
                if (editorContentEmpty) {
                    changeNoteText(null, node);
                } else {
                    changeNoteText(documentText, node);
                }
                mLastContentEmpty = editorContentEmpty;
            }
            controller.registerNodeSelectionListener(this);

        }

        public void onCreateNodeHook(MindMapNode node) {
            if (node.getXmlNoteText() != null) {
                setStateIcon(node, true);
            }
        }

        public void onDeleteNodeHook(MindMapNode node) {
        }
    }

    private static SHTMLPanel htmlEditorPanel;

    /**
     * Indicates, whether or not the main panel has to be refreshed with new
     * content. The typical content will be empty, so this state is saved
     * here.
     */
    private static boolean mLastContentEmpty = true;

    private final MindMapController controller;

    protected SHTMLPanel noteViewerComponent;

    private final MindMap mMap;

    private final java.util.logging.Logger logger;

    private NotesManager mNotesManager;

    private static ImageIcon noteIcon = null;

    private NoteDocumentListener mNoteDocumentListener;

    static Integer sPositionToRecover = null;
    
	private JSplitPane mSplitPane = null;
    
    public NodeNoteRegistration(ModeController controller, MindMap map) {
        this.controller = (MindMapController) controller;
        mMap = map;
        logger = controller.getFrame().getLogger(this.getClass().getName());

    }
    
    public boolean shouldUseSplitPane() {
    	return "true".equals(controller.getFrame()
    			.getProperty(FreeMind.RESOURCES_USE_SPLIT_PANE));
    }

    class JumpToMapAction extends AbstractAction{
        public void actionPerformed(ActionEvent e) {
            if (sPositionToRecover != null) {
                mSplitPane.setDividerLocation(sPositionToRecover
                        .intValue());
                sPositionToRecover = null;
            }
            controller.getView().getSelected().requestFocus();
        }
    };
    public void register() {
        logger.fine("Registration of note handler.");
        FreeMindMain frame = controller.getFrame();
        controller.getActionFactory().registerActor(this,
                getDoActionClass());
        // moved to registration:
        noteViewerComponent = getNoteViewerComponent();
		// register "leave note" action:
		Action jumpToMapAction = new JumpToMapAction();
		String keystroke = controller
				.getFrame()
				.getAdjustableProperty(
						"keystroke_accessories/plugins/NodeNote_jumpto.keystroke.alt_N");
		noteViewerComponent.getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(keystroke), "jumpToMapAction");

		// Register action
		noteViewerComponent.getActionMap().put("jumpToMapAction",
				jumpToMapAction);

        if (shouldUseSplitPane()) {
			showNotesPanel();
		}
		mNotesManager = new NotesManager();
        controller.registerNodeSelectionListener(mNotesManager);
        controller.registerNodeLifetimeListener(mNotesManager);
        mNoteDocumentListener = new NoteDocumentListener();
    }

    public void deRegister() {
        controller.deregisterNodeSelectionListener(mNotesManager);
        controller.deregisterNodeLifetimeListener(mNotesManager);
        noteViewerComponent.getActionMap().remove("jumpToMapAction");

        if (noteViewerComponent != null && shouldUseSplitPane()) {
            hideNotesPanel();
            noteViewerComponent = null;
        }
        logger.fine("Deregistration of note undo handler.");
        controller.getActionFactory().deregisterActor(getDoActionClass());
    }

    public void showNotesPanel() {
    	SouthPanel southPanel = new SouthPanel();
    	southPanel.add(noteViewerComponent, BorderLayout.CENTER);
    	noteViewerComponent.setVisible(true);
    	mSplitPane = controller.getFrame().insertComponentIntoSplitPane(
    			southPanel);
    	southPanel.revalidate();
    }
    
	public void hideNotesPanel() {
		// shut down the display:
		noteViewerComponent.setVisible(false);
		controller.getFrame().removeSplitPane();
		mSplitPane = null;
	}

    protected void setStateIcon(MindMapNode node, boolean enabled) {
        // icon
        if (noteIcon == null) {
            noteIcon = new ImageIcon(controller
                    .getResource("images/knotes.png"));
        }
        boolean showIcon = enabled;
        if (Resources.getInstance().getBoolProperty(
				FreeMind.RESOURCES_DON_T_SHOW_NOTE_ICONS)) {
			showIcon = false;
		}
        node.setStateIcon(this.getClass().getName(), (showIcon) ? noteIcon
                : null);
        // tooltip, first try.
        getMindMapController().setToolTip(node, "nodeNoteText", (enabled)?node.getNoteText():null);
    }

    public void act(XmlAction action) {
        if (action instanceof EditNoteToNodeAction) {
            EditNoteToNodeAction noteTextAction = (EditNoteToNodeAction) action;
            MindMapNode node = controller.getNodeFromID(noteTextAction
                    .getNode());
            String newText = noteTextAction.getText();
            String oldText = node.getNoteText();
            if (!Tools.safeEquals(newText, oldText)) {
                node.setNoteText(newText);
                // update display only, if the node is displayed.
                if (node == controller.getSelected()
                        && (!Tools.safeEquals(newText, getHtmlEditorPanel()
                                .getDocumentText()))) {
                    getHtmlEditorPanel().setCurrentDocumentContent(
                            newText == null ? "" : newText);
                }
                setStateIcon(node, ! (newText == null || newText.equals("")));
                controller.nodeChanged(node);
            }
        }
    }

    public Class getDoActionClass() {
        return EditNoteToNodeAction.class;
    }

    /**
     * Set text with undo:
     * 
     */
    public void changeNoteText(String text, MindMapNode node) {
    	getMindMapController().setNoteText(node, text);        	
    }

    /**
     */
    private MindMapController getMindMapController() {
        return controller;
    }


    protected SHTMLPanel getNoteViewerComponent() {
        return getHtmlEditorPanel();
    }

    public static SHTMLPanel getHtmlEditorPanel() {
        if (htmlEditorPanel == null) {
            SHTMLPanel.setResources(new TextResources(){
                public String getString(String pKey) {
                	pKey = "simplyhtml." + pKey;
                	String resourceString = Resources.getInstance().getResourceString(pKey, null);
                	if(resourceString == null){
                		resourceString = Resources.getInstance().getProperty(pKey);
                	}
                	return resourceString;
                }                        
            });
            htmlEditorPanel = SHTMLPanel.createSHTMLPanel();
            htmlEditorPanel.setMinimumSize(new Dimension(100, 100));
        }
        return htmlEditorPanel;
    }

	public JSplitPane getSplitPane() {
		return mSplitPane;
	}

	public boolean isSelected(JMenuItem pCheckItem, Action pAction) {
		return getSplitPane() != null;
	}
}