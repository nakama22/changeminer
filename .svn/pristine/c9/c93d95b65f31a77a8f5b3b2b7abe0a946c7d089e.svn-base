package changeminer.HandlerForUSA;

import com.itplus.cm.ce.addon.common.custom.HandlerForUSA;
import com.itplus.cm.ce.internal.meta.CM_OBJ_DPD;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.FileUtil;


public class CPP_TO_C99_USA_HANDLER extends HandlerForUSA {

	//5121002 /cppsub.h|csub(char const char const *)

	final int CPP_CALL_METHOD = 5121002;

	public CPP_TO_C99_USA_HANDLER(){

	}


	@Override
	public boolean generateDPD(String PREFIX, CM_SRC cm_src, CM_OBJ_DPD cm_obj_dpd) {
		boolean is_sql = false;


		if(cm_obj_dpd.getDPD_TYPE_ID() == CPP_CALL_METHOD){

			int startIdx = cm_obj_dpd.getCALL_TARGET().indexOf("|");

			if(startIdx >= 0){
				String methodName = cm_obj_dpd.getCALL_TARGET().substring(startIdx);
				System.out.println("MethodName >> " + methodName);
			}

			//cm_obj_dpd.setCALL_TARGET("/"+cm_obj_dpd.getCALL_TARGET()+".txt");
			//cm_obj_dpd.setGID(FileUtil.getGID("<FILE>", cm_obj_dpd.getCALL_TARGET()));
		}





		return is_sql;
	}


	public int abce = 0;

	public void check(String a,
						String b){


	}

}
