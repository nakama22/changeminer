package changeminer.HandlerForUSA;

import com.itplus.cm.ce.addon.common.custom.HandlerForUSA;
import com.itplus.cm.ce.internal.meta.CM_OBJ_DPD;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.FileUtil;


public class NS_USA_CALL_COBOL extends HandlerForUSA {

	public final int USA_CALL = 1100038;
	public final int CALL_MQ = 1111003;

	public NS_USA_CALL_COBOL(){

	}


	@Override
	public boolean generateDPD(String PREFIX, CM_SRC cm_src, CM_OBJ_DPD cm_obj_dpd) {
		boolean is_sql = false;

		if(cm_obj_dpd.getDPD_TYPE_ID() == USA_CALL){

			cm_obj_dpd.setGID(FileUtil.getGID("<COBOL>", cm_obj_dpd.getCALL_TARGET()));

			if(cm_obj_dpd.getCALL_TARGET().equals("MCTS023G")){
				cm_obj_dpd.setDPD_TYPE_ID(CALL_MQ);
			}
		}

		return is_sql;
	}

}
