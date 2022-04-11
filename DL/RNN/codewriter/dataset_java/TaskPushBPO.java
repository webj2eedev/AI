package com.dw.hsuods.op.taskpush;

import org.apache.commons.lang3.StringUtils;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.convert.taskpush.TashPushConvert;
import com.dw.util.OdssuUtil;
import com.dw.util.SendMsgUtil;

public class TaskPushBPO extends BPO {

	public DataObject getPDInfo(DataObject para) throws AppException {
		
		String key = para.getString("pdlabel","");
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		
		TashPushConvert convert = new TashPushConvert(dbid);
		DataStore vds = convert.getPDListWithKey(key);
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("pdinfo", vds);

		return vdo;
	}

	public DataObject getRSJInfo(DataObject para) throws AppException {
		String rsjname = para.getString("rsjname");
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;

		de.clearSql();
		de.addSql("select a.orgno , a.orgname ");
		de.addSql("  from odssu.orginfor a ");
		de.addSql(" where a.sleepflag = '0' ");
		
		if("YB".equals(OdssuUtil.getDBIDType(dbid))) {
			de.addSql("   and a.orgtype in ('YB_DSYBJ' , 'YB_QXYBJ' )");
		}else {
			de.addSql("   and a.orgtype in ('HSDOMAIN_DSRSJ' , 'HSDOMAIN_QXRSJ' )");
		}
		if(StringUtils.isNotBlank(rsjname)) {
			de.addSql("   and (a.orgno like :orgname or a.displayname like :orgname) ");
			de.setString("orgname",  "%" + rsjname + "%");
		}
		de.addSql(" order by a.orgno ");
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("rsjinfo", vds);
		return vdo;
	}

	public DataObject getOrgInfo(DataObject para) throws AppException {
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String dbidtype = OdssuUtil.getDBIDType(dbid);
		String orgtype =  "SB".equals(dbidtype)?"HSDOMAIN_SBS":"YB_JD";
		String orgno = para.getString("orgno");
		String rsjno = para.getString("rsjno");

		de.clearSql();
		de.addSql("select a.orgno, a.orgname ");
		de.addSql("  from odssu.orginfor a ");
		de.addSql(" where a.sleepflag = '0' ");
		de.addSql("   and a.orgtype = :orgtype ");
		if(StringUtils.isNotBlank(orgno)) {
			de.addSql("   and (a.orgno like :orgno or a.orgname like :orgno) ");
			de.setString("orgno", "%"+orgno+"%");
		}
		if (StringUtils.isNotBlank(rsjno)) {
			de.addSql("   and a.belongorgno = :rsjno ");
			de.setString("rsjno", rsjno);
		}
		de.addSql(" order by a.orgno ");
		
		de.setString("orgtype", orgtype);
		DataStore vds = this.de.query();
		DataObject vdo = DataObject.getInstance();

		vdo.put("orginfo", vds);
		return vdo;
	}

	public DataObject getTaskPushInfor(DataObject para) throws AppException {
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String pdid = para.getString("pdid");
		String orgno = para.getString("orgno");
		String rsjno = para.getString("rsjno");
		String dbidtype = OdssuUtil.getDBIDType(dbid);
		String orgtype =  "SB".equals(dbidtype)?"HSDOMAIN_SBS":"YB_JD";
		
		de.clearSql();
		de.addSql(" select b.orgno ,b.orgname ,a.pdid,a.elementid ,'1' approvelocal");
		de.addSql("   from odssu.orginfor b ,odssu.sbs_taskpush a");
		de.addSql("  where a.orgno = b.orgno");
		de.addSql("    and b.orgtype = :orgtype ");
		
		if (StringUtils.isNotBlank(orgno)) {
			de.addSql("   and b.orgno = :orgno ");
			de.setString("orgno", orgno);
		}
		if (StringUtils.isNotBlank(rsjno)) {
			de.addSql("   and b.belongorgno = :rsjno ");
			de.setString("rsjno", rsjno);
		}
		de.setString("orgtype",orgtype);
		DataStore nopushorgds = de.query();
		
		TashPushConvert convert = new TashPushConvert(dbid);
		DataStore pdlist = convert.getPDList(pdid); 
		DataStore resultds = nopushorgds.natureJoin(pdlist,"pdid,elementid");
		
		de.clearSql();
		de.addSql(" select b.orgno ,b.orgname , '0' approvelocal, '1' naturejoin ");
		de.addSql("   from odssu.orginfor b ");
		de.addSql("  where b.orgtype = :orgtype ");
		de.addSql("   and not exists (select 1 ");
		de.addSql("          from odssu.sbs_taskpush c ");
		de.addSql("         where c.orgno = b.orgno) ");
		if (StringUtils.isNotBlank(orgno)) {
			de.addSql("   and b.orgno = :orgno ");
			de.setString("orgno", orgno);
		}
		if (StringUtils.isNotBlank(rsjno)) {
			de.addSql("   and b.belongorgno = :rsjno ");
			de.setString("rsjno", rsjno);
		}
		de.setString("orgtype", orgtype);
		DataStore pushorgds = de.query();
		
		for(DataObject tempdo : pdlist) {
			tempdo.put("naturejoin","1");
		}
		
		resultds.combineDatastore(pushorgds.natureJoin(pdlist, "naturejoin"));
		
		nopushorgds.combineDatastore(pushorgds);
	
		DataObject vdo = DataObject.getInstance();
		vdo.put("taskpushvds", resultds);

		return vdo;
	}

	public DataObject getDSJType(DataObject para) throws AppException {
		
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String orgname = "SB".equals(OdssuUtil.getDBIDType(dbid))?"人社局":"医保局";
		
		DataObject result = DataObject.getInstance();
		result.put("dsjtype",orgname);
		
		return result;	
	}
	public void saveApproveType(DataObject para) throws AppException {
		String pdid = para.getString("pdid");
		String orgno = para.getString("orgno");
		String elementid = para.getString("elementid");
		String approvelocal = para.getString("approvelocal");

		de.clearSql();
		de.addSql("delete from odssu.sbs_taskpush a ");
		de.addSql(" where a.orgno = :orgno ");
		de.addSql("   and a.pdid = :pdid ");
		de.addSql("   and a.elementid = :elementid ");
		de.setString("orgno", orgno);
		de.setString("elementid", elementid);
		de.setString("pdid", pdid);
		de.update();
		if (approvelocal.equals("1")) {
			de.clearSql();
			de.addSql("insert into odssu.sbs_taskpush ");
			de.addSql("  (orgno, pdid, elementid) ");
			de.addSql("values ");
			de.addSql("  (:orgno, :pdid, :elementid) ");
			de.setString("orgno", orgno);
			de.setString("elementid", elementid);
			de.setString("pdid", pdid);
			de.update();
		}
		if(GlobalNames.DEPLOY_IN_ECO) {
			SendMsgUtil.SynTaskPush(para);
		}
	}
}