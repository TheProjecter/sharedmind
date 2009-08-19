package freemind.modes.mindmapmode.actions.xml;

import org.apache.log4j.Logger;

import freemind.controller.Controller;
import freemind.controller.actions.generated.instance.NewNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MergedMapInterface;

public class MergingActionFactory extends ActionFactory {
	private Logger log = Logger.getLogger(MergingActionFactory.class);
	
	private MergedMapInterface merged_map;
	private Controller c;
	
	public MergingActionFactory(Controller c, MergedMapInterface merged_map) {
		super(c);
		this.c = c;
		this.merged_map = merged_map;
	}

	public boolean executeAction (ActionPair pair) {
		boolean return_value = super.executeAction(pair);
		log.debug("execute: " + pair.getDoAction().toString());
		log.debug(return_value);
		if (return_value) {
			XmlAction do_action = pair.getDoAction();
			if (do_action instanceof NewNodeAction) {
				NewNodeAction new_node_action = (NewNodeAction) do_action;
				NodeAdapter parent = 
					this.c.getModeController().getNodeFromID(new_node_action.getNode());
				merged_map.addNodeToMergedMap(parent.getObjectId(merged_map.getMergedMap()),
						new_node_action.getNewId());
			}
		}
		return return_value;
	}
}
