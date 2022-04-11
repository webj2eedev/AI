package com.dw.org.orgchange;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class OrgChangeBPO extends BPO{
	/**
	 * 跳转到修改机构审批界面 zwh 2020-1-6
	 */
	public final DataObject queryOrgChangeInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
		DataStore xgds = DataStore.getInstance();
		String piid;
		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		if (gdxxds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + piid + "】的流程的工单信息！");
		}
		String orgno = gdxxds.getString(0, "orgno");
		String oldorgname = gdxxds.getString(0, "oldorgname");
		String neworgname = gdxxds.getString(0, "neworgname");
		String oldxzqhdm = gdxxds.getString(0, "oldxzqhdm");
		String newxzqhdm = gdxxds.getString(0, "newxzqhdm");

		String spr = gdxxds.getString(0, "reviewer");
		String sprq = gdxxds.getDateToString(0, "reviewtime", "yyyy-mm-dd");
		String spyj = gdxxds.getString(0, "spyj");
		String spsm = gdxxds.getString(0, "spsm");

		DataStore yjds = DataStore.getInstance();
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		if (!oldorgname.equals(neworgname)) {
			xgds.put(xgds.rowCount(), "xgx", "机构名称");
			xgds.put(xgds.rowCount() - 1, "yz", oldorgname);
			xgds.put(xgds.rowCount() - 1, "xz", neworgname);
		}
		if (oldxzqhdm == null || !oldxzqhdm.equals(newxzqhdm)) {
			xgds.put(xgds.rowCount(), "xgx", "行政区划");
			xgds.put(xgds.rowCount() - 1, "yz", oldxzqhdm);
			xgds.put(xgds.rowCount() - 1, "xz", newxzqhdm);
		}
    	
		de.clearSql();
  		de.addSql(" select  orgname,  displayname, fullname,orgtype,xzqhdm ");
  		de.addSql("from odssu.orginfor ");
  		de.addSql(" where orgno=:orgno ");
  		de.addSql(" and sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore vdsorg = de.query();
		if (vdsorg.rowCount() == 0) {
			this.bizException("没有找到编号为【" + orgno + "】的机构的信息！");
		}
		String orgname = vdsorg.getString(0, "orgname");

		gdxxds.put(0, "orgname", orgname);

		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		vdo.put("xgds", xgds);

		return vdo;
	}

	/**
	 * 查询工单信息  zwh 2020-1-6
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();

  		//需要往这个表里插入字段newxzqhdm,oldxzqhdm
		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jgjbxxxg ");
  		de.addSql(" where piid=:piid  ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}
}