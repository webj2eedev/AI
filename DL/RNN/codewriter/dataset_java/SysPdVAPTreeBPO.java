package com.dw.res.systask;

import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.convert.dpbypd.DPWithPDConvert;
import com.dw.convert.singlepd.SinglePDConvert;

public class SysPdVAPTreeBPO extends BPO {
	/**
	 * 查询流程基本信息 zwh 2020-03-10
	 */
	public DataObject getPdBaseInfo(final DataObject para) throws Exception {
		String pdid = para.getString("pdid");
		
		SinglePDConvert convert = new SinglePDConvert(pdid);
		
		String pdalias = convert.getPDLabel();
		para.put("pdalias", pdalias);
		
		String appid = "", appname = "";
		
		appid = convert.getAppid();
		if(appid != null && !appid.equals("")) {
			de.clearSql();
			de.addSql("select a.appname ");
			de.addSql("  from odssu.appinfo a ");
			de.addSql(" where a.appid = :appid   ");
			de.setString("appid", appid);
			DataStore appnameds = de.query();
			if(appnameds !=null && appnameds.rowCount() >0) {
				appname = appnameds.getString(0,"appname");
			}
		}
	
		para.put("appid", appid);
		para.put("appname", appname);

		return para;
	}
	
	/**
	 * 查询流程BPMN图 zwh 2020-03-10
	 */
	public DataObject queryPdBPMN(final DataObject para) throws Exception {
		return para;
	}

	/**
	 * 流程任务 zwh 2020-03-10
	 */
	public DataObject queryProcess(final DataObject para) throws Exception {
		String pdid = para.getString("pdid");
		String appid = para.getString("appid");
		String appname = para.getString("appname");

		DPWithPDConvert convert = new DPWithPDConvert(pdid); 
		DataStore dplist = convert.getDPList();
		DataStore result = DataStore.getInstance();
		
		for(int i = 0; i < dplist.rowCount();i++) {
			DataObject tempdo = dplist.get(i);
			tempdo.put("appid", appid);
			tempdo.put("appname", appname);
			result.addRow(tempdo);
		}
		result.remainDistinct();
		DataObject vdo = DataObject.getInstance();
		vdo.put("pdlist",result );
		return vdo;
	}
}