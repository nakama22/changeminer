package changeminer.HandlerForRA;

import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.parser.common.CMParserCommonData;
import extractor.common.tobj.TResult;

public class NS_REPORT_EXPORT extends HandlerForRA{



    public NS_REPORT_EXPORT() { }

   public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
       return RETURN_CONTINUE;
    }
}