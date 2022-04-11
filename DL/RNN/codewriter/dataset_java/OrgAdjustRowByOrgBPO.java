package com.dw.org.orgadjustrolebyorg;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

import org.apache.commons.lang3.StringUtils;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;

public class OrgAdjustRowByOrgBPO extends BPO{
	public DataObject queryHrOrgLov4EmpAdd(DataObject para) throws AppException {
		String querylabel = para.getString("querylabel","");
		
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String belongorgno=para.getString("belongorgno",""); 
		DE de = DE.getInstance();
		DataObject result = DataObject.getInstance();
		
		de.clearSql();
		de.addSql(" select o.orgno,o.displayname,o.orgname,o.sleepflag,oy.typename  ");
		de.addSql(" from   odssu.ir_org_closure i, ");
		de.addSql("        odssu.orginfor o, ");
		de.addSql("        odssu.org_type oy ");
		de.addSql(" where  i.orgno = o.orgno  ");
		de.addSql("     and o.orgtype = oy.typeno ");
		de.addSql("        and o.sleepflag = '0' ");
		de.addSql("        and i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid )  ");
		if(StringUtils.isNotBlank(querylabel)) {
			de.addSql("        and ");
			de.addSql("        (o.orgno like :querylabel ");
			de.addSql("        or o.orgname like :querylabel  ");
			de.addSql("        or o.displayname like :querylabel  ");
			de.addSql("        or o.fullname like :querylabel ");
			de.addSql("        or upper(o.orgnamepy) like :querylabel  "); 
			de.addSql("        or upper(oy.typename) like :querylabel)  ");
			de.setString("querylabel", "%"+querylabel.toUpperCase()+"%");
		}
		if(belongorgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		
		de.setString("dbid", dbid);
		if(belongorgno!=""){
			de.setString("belongorgno", belongorgno);
		}
		DataStore orgds = de.query();

		result.put("orgds", orgds);
		return result;
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject dealOrgData(DataObject para) throws AppException {
		
		String belongorg = para.getString("orgno").toString();
		DataObject result = DataObject.getInstance();
		if(OdssuContants.ORGROOT.equals(belongorg)||OdssuContants.ORGROOT.equals(para.getString("querylabel","").toUpperCase())) {
			result = getOrgroot();
			return result;
		}
		
		para.put("belongorgno", belongorg);
		result = queryHrOrgLov4EmpAdd(para);
		return result;
	}
	public DataObject getOrgroot() throws AppException {
	
		DataObject orgrootdo = DataObject.getInstance();
		orgrootdo.put("orgno", OdssuContants.ORGROOT);
		orgrootdo.put("displayname", "顶级机构");
		orgrootdo.put("orgname", "顶级机构");
		orgrootdo.put("sleepflag", "0");
		orgrootdo.put("typename", "顶级机构");
		
		DataStore orgrootds = DataStore.getInstance();
		orgrootds.addRow(orgrootdo);
		
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgrootds);
		return result;
	}

}

