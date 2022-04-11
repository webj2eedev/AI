package com.dw.hsuods.ws.function;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class OpenedFnBPO extends BPO{
	public DataObject refreshOpenFnGrid(DataObject para) throws AppException{
		String label = para.getString("label","");
  		de.clearSql();
  		de.addSql(" select a.functionid,a.functionname,a.fnfolderid,b.folderlabel ");
  		de.addSql(" from odssu.opened_function a,odssu.fn_folder b ");
  		de.addSql(" where a.fnfolderid = b.fnfolderid ");
		if (label!=null&&!label.equals("")) {
			label = "%"+ label +"%";
  			de.addSql(" and (a.functionid like :label or a.functionname like :label)");
  			de.setString("label", label);
		}
		DataStore fnds = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("dsfn", fnds);
		return result;
	}

}
