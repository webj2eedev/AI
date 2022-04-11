package com.dw.org.orgvalid;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class OrgValidBPO extends BPO{
	/**
	 * 跳转到修改机构审批界面 zwh 2020-1-6
	 */
	public final DataObject queryOrgValidInfo(DataObject para) throws Exception {
		DataStore dstmp, yjds = DataStore.getInstance();
  		de.clearSql();
		String spr = "", spyj = "", spsm = "";
		String sprq = null;
		
		String piid = para.getString("piid");
		if (piid == null || "".equals(piid)) {
			this.bizException("para中传入的piid为空！");
		}
		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jgjbxxxzwzb   ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();
		if(dstmp == null || dstmp.rowCount() == 0 ){
			throw new AppException("获取工单编号为【"+piid +"】的工单信息失败！");
		}
		if ( "1".equals(dstmp.getString(0, "spyj")) ||  "0".equals(dstmp.getString(0, "spyj")) ) {	
			spr = dstmp.getString(0, "reviewer");
			sprq = dstmp.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = dstmp.getString(0, "spyj");
			spsm = dstmp.getString(0, "spsm");
		} 
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);
		
		de.clearSql();
  		de.addSql("select *  ");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		DataStore gdxxds = this.de.query();
		if (gdxxds == null || gdxxds.rowCount() == 0 ) {
			throw new AppException("没有找到编号为【" + piid + "】的工单信息！");
		}
		gdxxds.put(0, "belongorgname", OdssuUtil.getOrgNameByOrgno(gdxxds.getString(0, "belongorgno")));
		gdxxds.put(0, "typename", OdssuUtil.getOrgTypeNameByTypeNo(gdxxds.getString(0, "orgtype")));
		DataObject vdo = DataObject.getInstance();
		String xzqhdm = (String) ProcessUtil.getRunTEEVarByPiid(piid, "xzqhdm");
		gdxxds.put(0, "xzqhdm", xzqhdm);
		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		return vdo;
	}

	/**
	 * 校验此机构是否允许作为当前机构的上级机构 zwh 2020-1-6
	 */
	public DataObject checkOrgValid(DataObject para) throws Exception {
		String sfyxzwsjjg = "0";//0不允许1允许
		String orgno = para.getString("orgno");
		String orgtype = para.getString("orgtype");
		String belongorgno = para.getString("belongorgno");
		
		de.clearSql();
		de.addSql(" SELECT 1  FROM odssu.orginfor a ");
		de.addSql(" WHERE a.orgtype IN ( ");
		de.addSql(" SELECT b.suptypeno orgtype ");
		de.addSql(" FROM odssu.orginfor c, odssu.ir_org_type b ");
		de.addSql(" WHERE c.orgtype = b.subtypeno ");
		de.addSql("	AND c.orgtype = :orgtype ");
		de.addSql("	AND c.orgno = :orgno ) ");
		de.addSql(" AND a.SLEEPFLAG = '0' ");
		de.addSql(" AND a.orgno = :belongorgno ");
		de.setString("orgtype", orgtype);
		de.setString("orgno", orgno);
		de.setString("belongorgno", belongorgno);
		DataStore sfyxzwsjjgds = de.query();
		if(sfyxzwsjjgds.rowCount()>0){
			sfyxzwsjjg = "1";
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sfyxzwsjjg", sfyxzwsjjg);
		return vdo;
	}
}