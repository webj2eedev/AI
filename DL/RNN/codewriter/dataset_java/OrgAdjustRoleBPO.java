package com.dw.org.orgadjustrole;
import java.util.HashMap;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class OrgAdjustRoleBPO extends BPO{
	/**
	 * 跳转到修改机构审批界面 zwh 2020-1-6
	 */
	public final DataObject queryOrgAdjustRoleInfo(DataObject para) throws Exception {
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
		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		return vdo;
	}

	/**
	 * 校验此机构是否允许作为当前机构的上级机构 zwh 2020-1-6
	 */
	public DataObject checkOrgAdjustRole(DataObject para) throws Exception {
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
	
	/**
	 * 获取已过滤的角色信息
	 */
	public DataObject haveUncheckRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String orgno = para.getString("orgno");
		DataStore typeds =  getOrgType(orgno);
		String orgtypeno = typeds.getString(0, "typeno");

		DE de = DE.getInstance();
		de.clearSql();
		/*查询所有已过滤角色信息*/
		de.addSql(" SELECT DISTINCT a.roleno, b.orgtypeno, a.rolename, c.typename ");
		de.addSql(" FROM odssu.roleinfor a ");
		de.addSql("	LEFT OUTER JOIN odssu.role_orgtype b ON a.roleno = b.roleno ");
		de.addSql("	LEFT OUTER JOIN odssu.org_type c ON b.orgtypeno = c.typeno ");
		de.addSql(" WHERE a.sleepflag = '0' ");
		de.addSql("	AND a.roleno NOT IN ( SELECT d.roleno FROM odssu.role_orgtype d WHERE d.orgtypeno = :typeno ) ");
		de.addSql("  and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("typeno", orgtypeno);
		DataStore roleds = de.query();
		roleds = dealOrgType(roleds);
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	//获取机构 的机构类型
	public DataStore getOrgType(String orgno) throws AppException{
		DE de =DE.getInstance();
		de.clearSql();
		de.addSql("select b.typename,b.typeno  from   odssu.orginfor  a , odssu.org_type b   where  a.orgno = :orgno   and    a.orgtype = b.typeno ");
		de.setString("orgno", orgno);
		DataStore ds = de.query();
		return ds;
	}
	//对角色所适用的机构类型用逗号进行分隔
	public DataStore dealOrgType(DataStore ds) throws AppException{
		
		if(ds == null) {
			return DataStore.getInstance();
		}
		if(ds != null && ds.rowCount() ==0) {
			return ds;
		}
		
		HashMap<String,Integer> rolesExists = new HashMap<String,Integer>();
		DataStore orgtypeds = DataStore.getInstance(ds.rowCount());
		StringBuffer typename = new StringBuffer();
		for(int i = 0;i<ds.size();i++){
			String roleno = ds.getString(i, "roleno");
			Integer index = rolesExists.get(roleno);
			if(index != null){
				typename.setLength(0);
				typename.append(ds.getString(i, "typename"));
				typename.append(","+orgtypeds.getString(index, "typename"));
				orgtypeds.put(index, "typename", typename.toString());
			}else{
				orgtypeds.addRow(ds.get(i));
				rolesExists.put(roleno, orgtypeds.rowCount()-1);
			}
		}
		return orgtypeds;
	}
}