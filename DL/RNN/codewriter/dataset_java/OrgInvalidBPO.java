package com.dw.org.orginvalid;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class OrgInvalidBPO extends BPO{
	/**
	 * 跳转到修改机构审批界面 zwh 2020-1-6
	 */
	public final DataObject queryOrgInvalidInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance(), dstmp, yjds = DataStore.getInstance();
  		de.clearSql();
		String piid, orgno = "";
		String spr = "", spyj = "", spsm = "";
		String sprq = null;
		// 流程开始获取piid
		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select * 			");
  		de.addSql("  from odssuws.jgcx  ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();

		if (dstmp.rowCount() > 0) {
			orgno = dstmp.getString(0, "orgno");
			spr = dstmp.getString(0, "reviewer");
			sprq = dstmp.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = dstmp.getString(0, "spyj");
			spsm = dstmp.getString(0, "spsm");
		}
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		DataObject mdo = getOrgxx(orgno);
		gdxxds = mdo.getDataStore("orgds");
		gdxxds.put(0, "piid", piid);

		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		return vdo;
	}

	/**
	 * 获取机构信息  zwh 2020-1-6
	 */
	public final DataObject getOrgxx(String orgno) throws Exception {
  		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore orgds, typedstmp, belongnodstmp;
		String orgtype, belongorgno, typename = "", rankname = "", belongorgname = "";

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.orginfor  ");
  		de.addSql(" where orgno = :orgno		 ");
  		de.addSql("   and sleepflag = '0' ");
		this.de.setString("orgno", orgno);
		orgds = this.de.query();
		if (orgds.rowCount() == 0) {
			throw new AppException("没有找到此机构的信息，请检查！");
		}

		orgtype = orgds.getString(0, "orgtype");
		if (!"".equals(orgtype) && orgtype != null) {
			de.clearSql();
  			de.addSql("select typename		 ");
  			de.addSql("  from odssu.org_type  ");
  			de.addSql(" where typeno = :orgtype		 ");
			this.de.setString("orgtype", orgtype);
			typedstmp = this.de.query();
			if (typedstmp.rowCount() > 0) {
				typename = typedstmp.getString(0, "typename");
			}
		}

		belongorgno = orgds.getString(0, "belongorgno");
		if (!"".equals(belongorgno) && belongorgno != null) {
			de.clearSql();
  			de.addSql("select orgname	 ");
  			de.addSql("  from odssu.orginfor  ");
  			de.addSql(" where orgno = :belongorgno		 ");
			this.de.setString("belongorgno", belongorgno);
			belongnodstmp = this.de.query();
			if (belongnodstmp.rowCount() > 0) {
				belongorgname = belongnodstmp.getString(0, "orgname");
			}
		}

		orgds.put(0, "typename", typename);
		orgds.put(0, "rankname", rankname);
		orgds.put(0, "belongorgname", belongorgname);
		vdo.put("orgds", orgds);

		return vdo;
	}
}