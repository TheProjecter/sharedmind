/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2005  Joerg Mueller, Daniel Polansky, Christian Foltin and others.
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
 * Created on 04.02.2005
 */
/* $Id: TimeList.java,v 1.1.2.5 2007/08/28 21:27:41 dpolivaev Exp $ */
package accessories.plugins.time;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.jgoodies.forms.factories.ButtonBarFactory;

import freemind.controller.BlindIcon;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.actions.generated.instance.TimeWindowColumnSetting;
import freemind.controller.actions.generated.instance.TimeWindowConfigurationStorage;
import freemind.controller.actions.generated.instance.WindowConfigurationStorage;
import freemind.main.HtmlTools;
import freemind.main.Tools;
import freemind.modes.MindIcon;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.common.plugins.ReminderHookBase;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.hooks.MindMapHookAdapter;
import freemind.view.mindmapview.MultipleImage;

/**
 * @author foltin
 *
 *TODO: 
 * - Extract HTML from nodes and notes.
 */
public class TimeList extends MindMapHookAdapter {

	private static final int TYPE_DELAY_TIME = 500;
	
	private static  String COLUMN_MODIFIED = "Modified";

	private static  String COLUMN_CREATED = "Created";

	private static  String COLUMN_ICONS = "Icons";

	private static  String COLUMN_TEXT = "Text";

	private static  String COLUMN_DATE = "Date";

	private static  String COLUMN_NOTES = "Notes";

	private static final int DATE_COLUMN = 0;

	public static final int NODE_TEXT_COLUMN = 1;

	protected static final int NODE_ICON_COLUMN = 2;

	protected static final int NODE_CREATED_COLUMN = 3;

	protected static final int NODE_MODIFIED_COLUMN = 4;

	protected static final int NODE_NOTES_COLUMN = 5;

	private JDialog dialog;

	private JPanel timePanel;

	private JTable timeTable;

	private DefaultTableModel timeTableModel;

	private accessories.plugins.time.TableSorter sorter;

	private DateRenderer dateRenderer;

	private NodeRenderer nodeRenderer;

	private IconsRenderer iconsRenderer;

	private boolean showAllNodes = false;

	private static final String WINDOW_PREFERENCE_STORAGE_PROPERTY = TimeList.class
			.getName()
			+ "_properties";

	private FlatNodeTableFilterModel mFlatNodeTableFilterModel;

	private JTextField mFilterTextSearchField;

	private JTextField mFilterTextReplaceField;

	private NotesRenderer notesRenderer;

	private JLabel mTreeLabel;

	public void startupMapHook() {
		super.startupMapHook();

		// get strings from resources:
		COLUMN_MODIFIED = getResourceString("plugins/TimeList.xml_Modified");
		COLUMN_CREATED = getResourceString("plugins/TimeList.xml_Created");
		COLUMN_ICONS = getResourceString("plugins/TimeList.xml_Icons");
		COLUMN_TEXT = getResourceString("plugins/TimeList.xml_Text");
		COLUMN_DATE = getResourceString("plugins/TimeList.xml_Date");
		COLUMN_NOTES = getResourceString("plugins/TimeList.xml_Notes");



		showAllNodes = Tools.xmlToBoolean(getResourceString("show_all_nodes"));
		dialog = new JDialog(getController().getFrame().getJFrame(), true /* modal */);
		String windowTitle;
		if (showAllNodes) {
			windowTitle = "plugins/TimeManagement.xml_WindowTitle_All_Nodes";
		} else {
			windowTitle = "plugins/TimeManagement.xml_WindowTitle";
		}
		dialog
				.setTitle(getResourceString(windowTitle));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent event) {
		        disposeDialog();
		    }
		});
		Tools.addEscapeActionToDialog(dialog, new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				disposeDialog();
			}
		});
		Container contentPane = dialog.getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 1.0f };
		gbl.rowWeights = new double[] { 1.0f };
		contentPane.setLayout(gbl);
		contentPane.add(new JLabel(getResourceString("plugins/TimeManagement.xml_Find")), new GridBagConstraints(0,0,1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		mFilterTextSearchField = new JTextField();
		mFilterTextSearchField.getDocument().addDocumentListener(new FilterTextDocumentListener());
		mFilterTextSearchField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent pEvent) {
//				logger.info("Key event:" + pEvent.getKeyCode());
				if(pEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					logger.info("Set Focus to replace fields");
					mFilterTextReplaceField.requestFocusInWindow();
				}
			}});
		contentPane.add(/*new JScrollPane*/(mFilterTextSearchField), new GridBagConstraints(0,1, 
					1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		contentPane.add(new JLabel(getResourceString("plugins/TimeManagement.xml_Replace")), new GridBagConstraints(0,2,1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		mFilterTextReplaceField = new JTextField();
		contentPane.add(/*new JScrollPane*/(mFilterTextReplaceField), new GridBagConstraints(0,3, 
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		mFilterTextReplaceField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent pEvent) {
				if(pEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					logger.info("Set Focus to table");
					timeTable.requestFocusInWindow();
				} else if(pEvent.getKeyCode() == KeyEvent.VK_UP) {
					logger.info("Set Focus to table");
					mFilterTextSearchField.requestFocusInWindow();
				} 
			}});
		dateRenderer = new DateRenderer();
		nodeRenderer = new NodeRenderer();
		notesRenderer = new NotesRenderer();
		iconsRenderer = new IconsRenderer(getController());
		timeTable = new FlatNodeTable();
		timeTable.addKeyListener(new FlatNodeTableKeyListener());
		//double click = goto.
		timeTable.addMouseListener(new FlatNodeTableMouseAdapter());
		//disable moving:
		timeTable.getTableHeader().setReorderingAllowed(false);
		timeTableModel = updateModel();
		mFlatNodeTableFilterModel = new FlatNodeTableFilterModel(timeTableModel, NODE_TEXT_COLUMN);
		sorter = new TableSorter(mFlatNodeTableFilterModel);
		timeTable.setModel(sorter);

		sorter.setTableHeader(timeTable.getTableHeader());
		sorter.setColumnComparator(Date.class,
				TableSorter.COMPARABLE_COMAPRATOR);
		sorter.setColumnComparator(MindMapNode.class,
				TableSorter.LEXICAL_COMPARATOR);
		sorter.setColumnComparator(IconsHolder.class,
				TableSorter.COMPARABLE_COMAPRATOR);
		// Sort by default by date.
		sorter.setSortingStatus(DATE_COLUMN, TableSorter.ASCENDING);
		JScrollPane pane = new JScrollPane(timeTable);
		contentPane.add(pane, new GridBagConstraints(0,4, 
				1, 1, 1.0, 10.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		mTreeLabel = new JLabel();
		contentPane.add(new JScrollPane(mTreeLabel), new GridBagConstraints(0,5, 
				1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// button bar
		final AbstractAction selectAction = new AbstractAction(getResourceString("plugins/TimeManagement.xml_Select")){
					public void actionPerformed(ActionEvent arg0) {
						selectSelectedRows();
					}};
		JButton selectButton = new JButton(selectAction);
        final AbstractAction exportAction = new AbstractAction(getResourceString("plugins/TimeManagement.xml_Export")){
				    public void actionPerformed(ActionEvent arg0) {
				        exportSelectedRowsAndClose();
				    }};
	    JButton exportButton = new JButton(exportAction);
		AbstractAction replaceAllAction = new AbstractAction(getResourceString("plugins/TimeManagement.xml_Replace_All")){
					public void actionPerformed(ActionEvent arg0) {
						replace(new ReplaceAllInfo());
					}};
		JButton replaceAllButton = new JButton(replaceAllAction);
		final AbstractAction replaceSelectedAction = new AbstractAction(getResourceString("plugins/TimeManagement.xml_Replace_Selected")){
					public void actionPerformed(ActionEvent arg0) {
						replace(new ReplaceSelectedInfo());
					}};
		JButton replaceSelectedButton = new JButton(replaceSelectedAction);
		final AbstractAction gotoAction = new AbstractAction(getResourceString("plugins/TimeManagement.xml_Goto")){
					public void actionPerformed(ActionEvent arg0) {
						selectSelectedRows();
						disposeDialog();
					}};
		JButton gotoButton = new JButton(gotoAction);
		AbstractAction disposeAction = new AbstractAction(getResourceString("plugins/TimeManagement.xml_Cancel")){
					public void actionPerformed(ActionEvent arg0) {
						disposeDialog();
					}};
		JButton cancelButton = new JButton(disposeAction);
		/* Initial State */
		selectAction.setEnabled(false);
		gotoAction.setEnabled(false);
		exportAction.setEnabled(false);
		replaceSelectedAction.setEnabled(false);
		
		JPanel bar = ButtonBarFactory.buildGrowingBar(new JButton[]{
				cancelButton,
				exportButton, 
				replaceAllButton, 
				replaceSelectedButton, 
				gotoButton, 
				selectButton, 
				});
		contentPane.add(/*new JScrollPane*/(bar), new GridBagConstraints(0,6,1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu(getResourceString("plugins/TimeManagement.xml_menu_actions"));
		AbstractAction[] actionList = new AbstractAction[] { selectAction,
				gotoAction, 
				replaceSelectedAction, replaceAllAction, exportAction,
				disposeAction };
		for (int i = 0; i < actionList.length; i++) {
			AbstractAction action = actionList[i];
			JMenuItem item = menu.add(action);
			item.setIcon(new BlindIcon(StructuredMenuHolder.ICON_SIZE));
		}
		menuBar.add(menu);
		dialog.setJMenuBar(menuBar);

		// table selection listeners to enable/disable menu actions:
		
		ListSelectionModel rowSM = timeTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages.
		        if (e.getValueIsAdjusting()) return;
		        
		        ListSelectionModel lsm =
		            (ListSelectionModel)e.getSource();
		        boolean enable = !(lsm.isSelectionEmpty());
		        replaceSelectedAction.setEnabled(enable);
		        selectAction.setEnabled(enable);
		        gotoAction.setEnabled(enable);
		        exportAction.setEnabled(enable);
		    }
		});
		// table selection listener to display the history of the selected nodes
		rowSM.addListSelectionListener(new ListSelectionListener() {
			String getNodeText(MindMapNode node) {
				return node.getShortText(getController()) 
				   + ((node.isRoot())?"":
					   ( " <- " + getNodeText(node.getParentNode())));
			}
			public void valueChanged(ListSelectionEvent e) {
				//Ignore extra messages.
				if (e.getValueIsAdjusting()) return;
				
				ListSelectionModel lsm =
					(ListSelectionModel)e.getSource();
				if(lsm.isSelectionEmpty()) {
					mTreeLabel.setText("");
					return;
				}
				int selectedRow = lsm.getLeadSelectionIndex();
				MindMapNode mindMapNode = getMindMapNode(selectedRow);
				mTreeLabel.setText(getNodeText(mindMapNode));
			}
		});
		
		// restore preferences:
		//Retrieve window size and column positions.
		WindowConfigurationStorage storage = getMindMapController().decorateDialog(dialog, WINDOW_PREFERENCE_STORAGE_PROPERTY);
		if (storage != null) {
			//			 Disable auto resizing
			timeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			int column = 0;
			for (Iterator i = ((TimeWindowConfigurationStorage) storage).getListTimeWindowColumnSettingList().iterator(); i.hasNext();) {
                TimeWindowColumnSetting setting = (TimeWindowColumnSetting) i.next();
                timeTable.getColumnModel().getColumn(column).setPreferredWidth(setting.getColumnWidth());
                sorter.setSortingStatus(column, setting.getColumnSorting());
                column++;
            }
		}
		dialog.setVisible(true);
	}

	protected void exportSelectedRowsAndClose() {
        int[] selectedRows = timeTable.getSelectedRows();
        Vector selectedNodes = new Vector();
        for (int i = 0; i < selectedRows.length; i++) {
            int row = selectedRows[i];
            selectedNodes.add(getMindMapNode(row));
        }
        // create new map:
        MindMap newMap = getMindMapController().newMap();
        MindMapController newMindMapController = (MindMapController) newMap.getModeController();
//        Tools.BooleanHolder booleanHolder = new Tools.BooleanHolder();
//        booleanHolder.setValue(false);
        for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
            MindMapNode node = (MindMapNode) iter.next();
//            MindMapNode newNode = newMindMapController.addNewNode( newMap.getRootNode(), 0, booleanHolder);
//            // copy style:
//            freemind.controller.actions.generated.instance.Pattern pattern = StylePatternFactory.createPatternFromNode(node);
//            newMindMapController.applyPattern(newNode, pattern);
//            // copy text:
//            newMindMapController.setNodeText(newNode, node.getText());
            MindMapNode copy = node.shallowCopy();
            if(copy != null) {
            	  newMindMapController.insertNodeInto(copy, newMap.getRootNode());
            }
        }
        disposeDialog();
    }

    public interface IReplaceInputInformation {
		int getLength();
		NodeHolder getNodeHolderAt(int i);
        void changeString(NodeHolder holder, String newText);
	}

    private void replace(IReplaceInputInformation info) {
        try {
            String searchString = getText(mFilterTextSearchField.getDocument());
            String replaceString = getText(mFilterTextReplaceField
                    .getDocument());
            replace(info, searchString, replaceString);
            timeTableModel.fireTableDataChanged();
            mFlatNodeTableFilterModel.resetFilter();
            mFilterTextSearchField.setText("");
        } catch (BadLocationException e) {
            freemind.main.Resources.getInstance().logException(e);
        }

    }
    
	
	public static void replace(IReplaceInputInformation info, String searchString, String replaceString) {
        String regExp = "(" + getPureRegularExpression(searchString) + ")";
		Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        String replacement = getPureRegularExpression(replaceString);
		int length = info.getLength();
		for (int i = 0; i < length; i++) {
			NodeHolder nodeHolder = info.getNodeHolderAt(i);
			String text = nodeHolder.node.getText();
			String replaceResult = HtmlTools.getInstance().getReplaceResult(p, replacement, text);
            if (!Tools.safeEquals(text, replaceResult)) {
                // set new node text only, if different.
                info.changeString(nodeHolder, replaceResult);
            }
		}
	}

    private class ReplaceAllInfo implements IReplaceInputInformation {
		public int getLength() {
			return mFlatNodeTableFilterModel.getRowCount();
		}

		public NodeHolder getNodeHolderAt(int i) {
			return (NodeHolder) mFlatNodeTableFilterModel.getValueAt(i,
					NODE_TEXT_COLUMN);
		}

        public void changeString(NodeHolder nodeHolder, String newText) {
            getMindMapController().setNodeText(nodeHolder.node, newText);
        }
	}

	private class ReplaceSelectedInfo implements IReplaceInputInformation {
		public int getLength() {
			return timeTable.getSelectedRowCount();
		}
		
		public NodeHolder getNodeHolderAt(int i) {
			return (NodeHolder) sorter.getValueAt(timeTable.getSelectedRows()[i],
					NODE_TEXT_COLUMN);
		}
		public void changeString(NodeHolder nodeHolder, String newText) {
		    getMindMapController().setNodeText(nodeHolder.node, newText);
		}
	}
	
	private void selectSelectedRows() {
		selectNodes(timeTable.getSelectedRow(), timeTable.getSelectedRows());
	}

	private void gotoNodesAndClose(int focussedRow, int[] selectedRows) {
		selectNodes(focussedRow, selectedRows);
		disposeDialog();
	}

	private void selectNodes(int focussedRow, int[] selectedRows) {
		if (focussedRow >= 0) {
			MindMapNode focussedNode = getMindMapNode(focussedRow);
//			getController().centerNode(focussedNode);
			Vector selectedNodes = new Vector();
            for (int i = 0; i < selectedRows.length; i++) {
				int row = selectedRows[i];
				selectedNodes.add(getMindMapNode(row));
			}
            getMindMapController().selectMultipleNodes(focussedNode, selectedNodes);
		}
	}

	/**
     */
    private MindMapNode getMindMapNode(int focussedRow) {
        MindMapNode selectedNode = ((NodeHolder) timeTable.getModel()
        		.getValueAt(focussedRow, NODE_TEXT_COLUMN)).node;
        return selectedNode;
    }

    /**
	 * Creates a table model for the new table and returns it.
	 */
	private DefaultTableModel updateModel() {
		MindMapNode node = getController().getMap().getRootNode();
		DefaultTableModel model = new DefaultTableModel() {
			/*
			 * (non-Javadoc)
			 *
			 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
			 */
			public Class getColumnClass(int arg0) {
				switch (arg0) {
				case DATE_COLUMN:
				case NODE_CREATED_COLUMN:
				case NODE_MODIFIED_COLUMN:
					return Date.class;
				case NODE_TEXT_COLUMN:
					return NodeHolder.class;
				case NODE_ICON_COLUMN:
					return IconsHolder.class;
				case NODE_NOTES_COLUMN:
					return NotesHolder.class;
				default:
					return Object.class;
				}
			}
		};

		model.addColumn(COLUMN_DATE);
		model.addColumn(COLUMN_TEXT);
		model.addColumn(COLUMN_ICONS);
		model.addColumn(COLUMN_CREATED);
		model.addColumn(COLUMN_MODIFIED);
		model.addColumn(COLUMN_NOTES);
		updateModel(model, node);
		return model;
	}

	private void updateModel(DefaultTableModel model, MindMapNode node) {
		ReminderHookBase hook = TimeManagementOrganizer.getHook(node);
		Date date = null;
		if (hook != null) {
			date = new Date(hook.getRemindUserAt());
		}
		// show all nodes or only those with reminder:
		if (showAllNodes || hook != null) {
			model.addRow(new Object[] { date, new NodeHolder(node),
					new IconsHolder(node),
					node.getHistoryInformation().getCreatedAt(),
					node.getHistoryInformation().getLastModifiedAt(),
					new NotesHolder(node)});
		}
		for (Iterator i = node.childrenUnfolded(); i.hasNext();) {
			MindMapNode child = (MindMapNode) i.next();
			updateModel(model, child);
		}
	}

	/**
	 */
	private JPanel getTimePanel() {
		if (timePanel == null) {
			timePanel = new JPanel();
			timePanel.setLayout(new GridBagLayout());
			//			{
			//				GridBagConstraints gb2 = new GridBagConstraints();
			//				gb2.gridx = 0;
			//				gb2.gridy = 0;
			//				gb2.fill = GridBagConstraints.HORIZONTAL;
			//				timePanel.add(new JLabel(
			//						getResourceString("plugins/TimeManagement.xml_hour")),
			//						gb2);
			//			}
		}
		return timePanel;
	}

	/**
	 *
	 */
	private void disposeDialog() {
		// store window positions:

		TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
        for(int i = 0; i< timeTable.getColumnCount(); i++) {
        	TimeWindowColumnSetting setting = new TimeWindowColumnSetting();
        	setting.setColumnWidth(timeTable.getColumnModel().getColumn(i).getWidth());
        	setting.setColumnSorting(sorter.getSortingStatus(i));
        	storage.addTimeWindowColumnSetting(setting);
        }
        getMindMapController().storeDialogPositions(dialog, storage, WINDOW_PREFERENCE_STORAGE_PROPERTY);
		dialog.setVisible(false);
		dialog.dispose();
	}

	public static String getRegularExpression(String text) throws BadLocationException {
		text = ".*("+text+").*";
		return text;
	}

    /**
     * @throws BadLocationException
     */
    private String getText(Document document) throws BadLocationException {
        String text = document.getText(
				0, document.getLength());
        return text;
    }

    /**
     */
    public static String getPureRegularExpression(String text) {
        // remove regexp:
		text=text.replaceAll("([()\\.\\[\\]^$|])", "\\\\\\1");
		text=text.replaceAll("\\*", ".*");
        return text;
    }

	private final class FilterTextDocumentListener implements DocumentListener {
		private Timer mTypeDelayTimer = null;
		
		private synchronized void change(DocumentEvent event) {
			// stop old timer, if present:
			if(mTypeDelayTimer!= null) {
				mTypeDelayTimer.cancel();
				mTypeDelayTimer = null;
			}
			mTypeDelayTimer = new Timer();
			mTypeDelayTimer.schedule(new DelayedTextEntry(event), TYPE_DELAY_TIME);
		}
		
		public void insertUpdate(DocumentEvent event) {
			change(event);
		}

		public void removeUpdate(DocumentEvent event) {
			change(event);
			
		}

		public void changedUpdate(DocumentEvent event) {
			change(event);
			
		}
	    protected class DelayedTextEntry extends TimerTask {

	        private final DocumentEvent event;

			DelayedTextEntry(DocumentEvent event) {
				this.event = event;
	        }

	        public void run() {
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    try {
	                        Document document = event.getDocument();
	                        String text = getRegularExpression(getText(document));
	                        mFlatNodeTableFilterModel.setFilter(text);
	                    } catch (BadLocationException e) {
	                        freemind.main.Resources.getInstance().logException(					e);
	                        mFlatNodeTableFilterModel.resetFilter();
	                    }
	                }
	            }    
	            );
	        }
	    }

	}

	private final class FlatNodeTableMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				Point p = e.getPoint();
				int row = timeTable.rowAtPoint(p);
				gotoNodesAndClose(row, new int[]{row});
			}
		}
	}

	private final class FlatNodeTableKeyListener implements KeyListener {
		public void keyTyped(KeyEvent arg0) {
		}

		public void keyPressed(KeyEvent arg0) {
		}

		public void keyReleased(KeyEvent arg0) {
			if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
				disposeDialog();
			}
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				selectSelectedRows();
				disposeDialog();
			}
		}
	}

	private final class FlatNodeTable extends JTable {
		public TableCellRenderer getCellRenderer(int row, int column) {
			Object object = getModel().getValueAt(row, column);
			if (object instanceof Date)
				return dateRenderer;
			if (object instanceof NodeHolder)
				return nodeRenderer;
			if (object instanceof NotesHolder)
				return notesRenderer;
			if (object instanceof IconsHolder)
				return iconsRenderer;
			return super.getCellRenderer(row, column);
		}

		public boolean isCellEditable(int rowIndex, int vColIndex) {
			return false;
		}

		protected void processKeyEvent(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				EventListener[] el = super.getListeners(KeyListener.class);
				if (e.getID() != KeyEvent.KEY_RELEASED)
					return;
				for (int i = 0; i < el.length; i++) {
					KeyListener kl = (KeyListener) el[i];
					kl.keyReleased(e);
				}
				return;
			}
			super.processKeyEvent(e);
		}
	}

	static class DateRenderer extends DefaultTableCellRenderer {
		DateFormat formatter;

		public DateRenderer() {
			super();
		}

		public void setValue(Object value) {
			if (formatter == null) {
				formatter = DateFormat.getDateTimeInstance();
			}
			setText((value == null) ? "" : formatter.format(value));
		}
	}

	static class NodeRenderer extends DefaultTableCellRenderer {
		public NodeRenderer() {
			super();
		}

		public void setValue(Object value) {
			setText((value == null) ? "" : ((NodeHolder) value).getUntaggedNodeText());
		}
	}

	static class NotesRenderer extends DefaultTableCellRenderer {
		public NotesRenderer() {
			super();
		}
		
		public void setValue(Object value) {
			setText((value == null) ? "" : ((NotesHolder) value).getUntaggedNotesText());
		}
	}
	
	/** removes html in nodes before comparison.*/
	public static class NodeHolder implements Comparable {
		private final MindMapNode node;
		private String untaggedNodeText=null;
		private String originalNodeText=null;

		/**
		 *
		 */
		public NodeHolder(MindMapNode node) {
			this.node = node;
		}

		public int compareTo(Object compareToObject) {
			return toString().compareTo(compareToObject.toString());
		}

		public String toString() {
            return getUntaggedNodeText();
		}

        public String getUntaggedNodeText() {
            String nodeText = node.getText();
            if(untaggedNodeText==null || (originalNodeText != null && !originalNodeText.equals(nodeText))) {
                originalNodeText = nodeText;
                // remove tags:
                untaggedNodeText = HtmlTools.removeHtmlTagsFromString(nodeText).replaceAll("\\s+", " ");
            }
            return untaggedNodeText;
        }

	}
	/** removes html in notes before comparison.*/
	public static class NotesHolder implements Comparable {
		private final MindMapNode node;
		private String untaggedNotesText=null;
		private String originalNotesText=null;
		
		/**
		 *
		 */
		public NotesHolder(MindMapNode node) {
			this.node = node;
		}
		
		public int compareTo(Object compareToObject) {
			return toString().compareTo(compareToObject.toString());
		}
		
		public String toString() {
			return getUntaggedNotesText();
		}
		
		public String getUntaggedNotesText() {
			String notesText = node.getNoteText();
			if(notesText == null) 
				return "";
			if(untaggedNotesText==null || (originalNotesText != null && !originalNotesText.equals(notesText))) {
				originalNotesText = notesText;
				// remove tags:
					untaggedNotesText = HtmlTools.removeHtmlTagsFromString(notesText).replaceAll("\\s+", " ");
			}
			return untaggedNotesText;
		}
		
	}

	static class IconsHolder implements Comparable {
		Vector icons = new Vector();

		private Vector iconNames;

		public IconsHolder(MindMapNode node) {
			icons.addAll(node.getIcons());
			// sorting the output.
			iconNames = new Vector();
			for (Iterator i = icons.iterator(); i.hasNext();) {
				MindIcon icon = (MindIcon) i.next();
				iconNames.add(icon.getName());
			}
			Collections.sort(iconNames);
		}

		public int compareTo(Object compareToObject) {
			return toString().compareTo(compareToObject.toString());
		}

		public Vector getIcons() {
			return icons;
		}

		/** Returns a sorted list of icon names. */
		public String toString() {
			String result = "";
			for (Iterator i = iconNames.iterator(); i.hasNext();) {
				String name = (String) i.next();
				result += name + " ";
			}
			return result;
		}
	}

	static class IconsRenderer extends DefaultTableCellRenderer {
		private final ModeController modeController;

		public IconsRenderer(ModeController controller) {
			super();
			modeController = controller;
		}

		public void setValue(Object value) {
			if (value instanceof IconsHolder) {
				IconsHolder iconsHolder = (IconsHolder) value;
				MultipleImage iconImages = new MultipleImage(1.0f);
				for (Iterator i = iconsHolder.getIcons().iterator(); i
						.hasNext();) {
					MindIcon icon = (MindIcon) i.next();
					iconImages
							.addImage(icon.getIcon());
				}
				if (iconImages.getImageCount() > 0) {
					setIcon(iconImages);
				} else {
					setIcon(null);
				}
			}
		}
	}
}
