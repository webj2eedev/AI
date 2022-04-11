package com.dw.hsuods.vap.org;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class OverallOuterDutyTreeBPO extends BPO{

	public DataObject queryOverallOuterDuty(DataObject para) throws AppException {
		// 定义变量
		String orgno = para.getString("orgno");
		String rolename = para.getString("rolename", "");
		if(rolename == null){
			rolename = "";
		}
		String rolenameinsql =  "%" + rolename + "%";
		String rolenamepy =  "%" + rolename.toUpperCase() + "%";
		// 根据orgno获取机构下定义的岗位
		de.clearSql();
  		de.addSql(" select rolename,roleno ");
  		de.addSql("   from odssu.roleinfor ");
  		de.addSql("  where deforgno = :orgno ");
  		de.addSql("    and roletype in ('HS_JBJYWJBL','HS_QDSYWGLL')");
  		de.addSql("    and sleepflag = '0' ");
  		de.addSql("    and (rolename like :rolenameinsql ");
  		de.addSql("         or roleno like :rolenameinsql ");
  		de.addSql("         or rolenamepy like :rolenamepy) ");
  		de.addSql("  order by rolename ");
		this.de.setString("orgno", orgno);
		this.de.setString("rolenameinsql", rolenameinsql);
		this.de.setString("rolenamepy", rolenamepy);
		DataStore vds = this.de.query();
		DataObject result = DataObject.getInstance();
		result.put("dswg", vds);
		return result;
	}

	/**
	 * @描述：查询岗位职能
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryOverallOuterDutyPDDP(DataObject para) throws Exception {
		String roleno = para.getString("roleno");
		String pdpara = para.getString("pdpara", "");
		if(pdpara == null){
			pdpara = "";
		}
		String pdparainsql = "%" + pdpara + "%";
		de.clearSql();
		de.addSql("select * from ( ");
		de.addSql("select a.pdid, a.dptdid, b.dptdlabel, d.pdlabel,d.pdalias,'-' toccode ");
		de.addSql("  from bpzone.dutyposition_task_role     a, ");
		de.addSql("       bpzone.dutyposition_task          b, ");
		de.addSql("       bpzone.process_define d ");
		de.addSql(" where a.pdid = b.pdid ");
		de.addSql("       and a.dptdid = b.dptdid ");
		de.addSql("       and a.pdid=d.pdid ");
		de.addSql("       and nvl(b.status,'0') <> '2' ");
		de.addSql("       and a.roleid = :roleno ");
		de.addSql("       and a.toccode='-' ");
		de.addSql("       and b.tocdmbh='-' ");
		de.addSql("union ");
		de.addSql("select m.pdid, m.dptdid, n.dptdlabel||'('||q.name||')' dptdlabel, p.pdlabel, p.pdalias, m.toccode ");
		de.addSql("  from bpzone.dutyposition_task_role     m, ");
		de.addSql("       bpzone.dutyposition_task          n, ");
		de.addSql("       bpzone.process_define p, ");
		de.addSql("       bpzone.syscode q ");
		de.addSql(" where m.pdid = n.pdid ");
		de.addSql("       and m.dptdid = n.dptdid ");
		de.addSql("       and m.pdid=p.pdid ");
		de.addSql("       and nvl(n.status,'0') <> '2' ");
		de.addSql("       and m.roleid = :roleno ");
		de.addSql("       and m.toccode<>'-' ");
		de.addSql("       and n.tocdmbh<>'-' ");
		de.addSql("       and n.tocdmbh=q.code ");
		de.addSql("       and m.toccode=q.value ) w  ");
		de.addSql("where w.dptdid like :pdparainsql or w.dptdlabel like :pdparainsql ");
		de.addSql("      or w.pdid like :pdparainsql or w.pdlabel like :pdparainsql  ");
		this.de.setString("roleno", roleno);
		this.de.setString("pdparainsql", pdparainsql);
		DataStore dsdp = de.query();
		DataObject result = DataObject.getInstance();
		result.put("gwzn", dsdp);
		return result;
	}

	/**
	 * @描述：查询功能任务,根据roleno查询
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject queryOverallOuterDutyFN(DataObject para) throws Exception {
		String roleno = para.getString("roleno");
		String fnpara = para.getString("fnpara");
		if(fnpara == null){
			fnpara = "";
		}
		String fnparainsql = "%"+ fnpara +"%";
		de.clearSql();
  		de.addSql("select a.roleno, b.functionid, b.functionname, c.fnfolderid, c.folderlabel folderlabel ");
  		de.addSql("  from odssu.role_function_manual a, ");
  		de.addSql("       odsv.notbpfn_view b, ");
  		de.addSql("       odssu.fn_folder c ");
  		de.addSql(" where a.functionid=b.functionid ");
  		de.addSql("   and b.fnfolderid=c.fnfolderid ");
  		de.addSql("   and a.roleno=:roleno ");
  		de.addSql("   and (b.functionid like :fnparainsql or b.functionname like :fnparainsql )");
		this.de.setString("roleno", roleno);
		this.de.setString("fnparainsql", fnparainsql);
		DataStore dsfn = this.de.query();
		DataObject result = DataObject.getInstance();
		result.put("dsfn", dsfn);
		return result;
	}

	/**
	 * 方法简介：查询业务范畴,根据roleno查询 郑海杰 2015-7-24
	 */
	public DataObject queryYwfc(DataObject para) throws Exception {
		String roleno = para.getString("roleno");
  		de.clearSql();
  		de.addSql(" select a.scopeno,a.scopename ");
  		de.addSql("   from odssu.business_scope a, ");
  		de.addSql("        odssu.ir_role_business_scope b ");
  		de.addSql("  where b.roleno = :roleno ");
  		de.addSql("    and a.scopeno = b.scopeno ");
		this.de.setString("roleno", roleno);
		DataStore dsfn = this.de.query();

		DataObject result = DataObject.getInstance();
		result.put("dsywfc", dsfn);
		return result;
	}

	/**
	 * 方法简介.获取岗位起用情况
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月11日
	 */
	public DataObject queryQyjg(DataObject para) throws Exception {
		// 定义变量
		String roleno, orgno;
  		de.clearSql();
		DataStore orgds = DataStore.getInstance();// 存放所有机构查询结果
		DataStore ds = DataStore.getInstance();// 存放已经启用了岗位的机构查询结果
		DataStore resultds = DataStore.getInstance();// 存放最后结果
		DataObject result = DataObject.getInstance();// 存放最后的返回结果

		// 获取变量值
		roleno = para.getString("roleno");
		orgno = para.getString("orgno");

		String roleType = OdssuUtil.getRoleInforByRoleno(roleno).getString("roletype");
		
		de.clearSql();
  		de.addSql(" select  b.typename roletype,decode(a.yxxjjgsy,'1','√','0','×') yxxjjgsy ");
  		de.addSql("   from odssu.roleinfor a, ");
  		de.addSql("        odssu.role_type b ");
  		de.addSql("  where a.roleno = :roleno ");
  		de.addSql("    and a.roletype = b.typeno ");
		this.de.setString("roleno", roleno);

		DataStore basicInfor = this.de.query();
		
		// 已经启用了岗位的机构信息
		de.clearSql();
  		de.addSql(" select a.orgno faceorgno,b.orgno inorgno,a.orgname faceorg,b.orgname inorg ");
  		de.addSql("   from odssu.orginfor a, ");
  		de.addSql("        odssu.orginfor b, ");
  		de.addSql("        odssu.outer_duty c ");
  		de.addSql("  where c.roleno = :roleno ");
  		de.addSql("    and a.orgno = c.faceorgno ");
  		de.addSql("    and b.orgno = c.inorgno ");
		this.de.setString("roleno", roleno);
		ds = this.de.query();

		// 人社系统的下级经办机构和二级单位暨经办机构
		if(roleType.equals(OdssuContants.ROLETYPE_JBJYWJBL)){
			de.clearSql();
  			de.addSql(" select b.orgno faceorgno,b.orgname faceorg ");
  			de.addSql(" from odssu.ir_org_closure a,   ");
  			de.addSql("      odssu.orginfor b          ");
  			de.addSql(" where a.orgno = b.orgno        ");
  			de.addSql("   and b.orgtype in (:dsejdwywjg,:dsywjg,:qxejdwywjg,:qxywjg)");
  			de.addSql("   and a.belongorgno = :orgno ");
			this.de.setString("orgno", orgno);
			this.de.setString("dsejdwywjg", OdssuContants.ORGTYPE_DSEJDWYWJG);
			this.de.setString("dsywjg", OdssuContants.ORGTYPE_DSYWJG);
			this.de.setString("qxejdwywjg", OdssuContants.ORGTYPE_QXEJDWYWJG);
			this.de.setString("qxywjg", OdssuContants.ORGTYPE_QXYWJG);
			orgds = this.de.query();
		}else if (roleType.equals(OdssuContants.ROLETYPE_QDSYWGLL)){
			de.clearSql();
  			de.addSql(" select b.orgno faceorgno,b.orgname faceorg ");
  			de.addSql(" from odssu.ir_org_closure a,   ");
  			de.addSql("      odssu.orginfor b          ");
  			de.addSql(" where a.orgno = b.orgno        ");
  			de.addSql("   and b.orgtype in (:dsejdwywjg,:dsywjg)");
  			de.addSql("   and a.belongorgno = :orgno ");
			this.de.setString("orgno", orgno);
			this.de.setString("dsejdwywjg", OdssuContants.ORGTYPE_DSEJDWYWJG);
			this.de.setString("dsywjg", OdssuContants.ORGTYPE_DSYWJG);
			orgds = this.de.query();
		}
		// 获取岗位的业务范畴
		de.clearSql();
  		de.addSql(" select scopeno                    ");
  		de.addSql("   from odssu.ir_role_business_scope ");
  		de.addSql("  where roleno = :roleno                  ");
		this.de.setString("roleno", roleno);
		DataStore ywfcds = this.de.query();

		// 获取机构业务范畴，并将其与岗位业务范畴对比，将有交集的机构编码取出
		int j = 0;
		for (int i = 0; i < orgds.rowCount(); i++) {
			DataStore orgywfc = DataStore.getInstance();
			orgywfc = OdssuUtil.getYwfcVdsByOrgno(orgds.getString(i, "faceorgno"));

			// 如果机构的业务范畴和岗位业务范畴存在交集，则将机构信息放入最终的结果集中
			if (getIts(ywfcds, orgywfc)) {
				resultds.put(j, "faceorgno", orgds.getString(i, "faceorgno"));
				resultds.put(j, "faceorg", orgds.getString(i, "faceorg"));
				resultds.put(j, "qy", "0");
				resultds.put(j, "inorg", "");
				resultds.put(j, "inorgno", "");
				resultds.put(j, "ckgwqyjg", "进入业务机构查看");
				j++;
			}

		}

		// 将得出的机构最终结果与已经启用该岗位的机构进行对比，修改qy和inorg信息
		resultds = getds(resultds, ds);
		resultds.multiSort("qy:desc,faceorg:asc");
		result.put("dsqyjg", resultds);
		result.put("basicinfo", basicInfor);
		return result;
	}

	/**
	 * 方法简介.判断两个ds是否有交集(getIntersection=getIts)
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月11日
	 */
	private boolean getIts(DataStore ds1, DataStore ds2) {
		boolean flag = false;
		DataStore dsnew = DataStore.getInstance(ds1);
		DataStore dsold = DataStore.getInstance(ds2);
		dsnew.retainAll(dsold);
		if (!dsnew.isEmpty()) {
			flag = true;
		}
		return flag;
	}

	private DataStore getds(DataStore result, DataStore ds) throws AppException {
		// 定义变量
		String orgno;
		int j = 0;
		// 将result中与ds中对应的信息修改
		for (int i = 0; i < ds.rowCount(); i++) {
			orgno = ds.getString(i, "faceorgno");
			j = result.find("faceorgno == " + orgno);
			if (j != -1) {
				result.put(j, "qy", "1");
				result.put(j, "inorg", ds.getString(i, "inorg"));
				result.put(j, "inorgno", ds.getString(i, "inorgno"));
			}
		}
		return result;
	}


	/**
	 * @描述：点击Fn目录的节点，展示Fn
	 * @param para
	 * @return
	 * @throws Exception 2015-6-11
	 */
	public DataObject queryGnrwByFolder(DataObject para) throws Exception {
		String folderid = para.getString("folderid");
		String orgno = para.getString("orgno");
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		de.clearSql();
  		de.addSql("select a.functionid, a.functionname  ");
  		de.addSql("from odsv.notbpfn_view a, ");
  		de.addSql("     odssu.db_appfunction b,  ");
  		de.addSql("     odssu.fn_business_scope c, ");
  		de.addSql("     odssu.org_business_scope d, ");
  		de.addSql("     odssu.fn_roletype e, ");
  		de.addSql("     odssu.ir_org_role_type f,  ");
  		de.addSql("     odssu.orginfor g ");
  		de.addSql("where a.functionid=b.functionid ");
  		de.addSql("      and a.functionid=c.functionid ");
  		de.addSql("      and c.scopeid=d.scopeno ");
  		de.addSql("      and a.functionid=e.functionid ");
  		de.addSql("      and e.roletypeno=f.roletypeno ");
  		de.addSql("      and f.orgtypeno=g.orgtype ");
  		de.addSql("      and a.fnfolderid=:folderid ");
  		de.addSql("      and b.dbid=:dbid ");
  		de.addSql("      and d.orgno=:orgno ");
  		de.addSql("      and g.orgno=:orgno ");
		this.de.setString("folderid", folderid);
		this.de.setString("dbid", dbid);
		this.de.setString("orgno", orgno);
		this.de.setString("orgno", orgno);
		DataStore dsfn = this.de.query();

		DataObject result = DataObject.getInstance();
		result.put("dsfn", dsfn);
		return result;
	}

	/**
	 * @描述：点击Fn,展示Fn的有权处理角色
	 * @param para
	 * @return
	 * @throws Exception 2015-6-11
	 */
	public DataObject queryGngwry(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String functionid = para.getString("functionid");

		DataObject result = DataObject.getInstance();
  		de.clearSql();
		de.clearSql();
  		de.addSql("select a.dutyno, c.orgname inorgname, d.rolename dutyname  ");
  		de.addSql("from odssu.outer_duty a, ");
  		de.addSql("     odssu.role_function_manual b, ");
  		de.addSql("     odssu.orginfor c, ");
  		de.addSql("     odssu.roleinfor d ");
  		de.addSql("where a.roleno=b.roleno ");
  		de.addSql("      and a.inorgno=c.orgno ");
  		de.addSql("      and a.roleno=d.roleno ");
  		de.addSql("      and a.faceorgno=:orgno ");
  		de.addSql("      and b.functionid=:functionid ");
		this.de.setString("orgno", orgno);
		this.de.setString("functionid", functionid);
		DataStore dsFngw = this.de.query();
		if (dsFngw == null || dsFngw.rowCount() == 0) {
			return result;
		}

		// 拼接前台显示的DS
		DataStore dsreturn = DataStore.getInstance();
		for (int i = 0; i < dsFngw.rowCount(); i++) {
			String dutyno = dsFngw.getString(i, "dutyno");
			String inorgname = dsFngw.getString(i, "inorgname");
			String dutyname = dsFngw.getString(i, "dutyname");
			DataObject inorgObj = DataObject.getInstance();
			inorgObj.put("inorgname", inorgname);
			inorgObj.put("dutyname", dutyname);
			dsreturn.addRow(inorgObj);

			// 查询科室下有权处理岗位的人员
			de.clearSql();
  			de.addSql("select  b.empname ");
  			de.addSql("from odssu.emp_outer_duty a, ");
  			de.addSql("     odssu.empinfor b ");
  			de.addSql("where a.empno=b.empno ");
  			de.addSql("      and a.dutyno=:dutyno ");
			this.de.setString("dutyno", dutyno);
			DataStore dsemp = this.de.query();
			if (dsemp == null || dsemp.rowCount() == 0) {
				continue;
			}
			for (int j = 0; j < dsemp.rowCount(); j++) {
				String empname = dsemp.getString(j, "empname");
				DataObject empObj = DataObject.getInstance();
				empObj.put("inorgname", "");
				empObj.put("dutyname", empname);
				dsreturn.addRow(empObj);
			}
		}
		result.put("dsfngwry", dsreturn);
		return result;
	}

	/**
	 * @描述：查询科室下岗位的有权办理人员
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryKsgwry(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String pdid = para.getString("pdid");
		String dpid = para.getString("dpid");
		DataObject result = DataObject.getInstance();
		de.clearSql();
  		de.addSql("select a.dutyno, c.orgname inorgname, d.rolename dutyname  ");
  		de.addSql("from odssu.outer_duty a, ");
  		de.addSql("     bpzone.dutyposition_task_role b, ");
  		de.addSql("     odssu.orginfor c, ");
  		de.addSql("     odssu.roleinfor d ");
  		de.addSql("where a.roleno=b.roleid ");
  		de.addSql("      and a.roleno=d.roleno ");
  		de.addSql("      and a.inorgno=c.orgno ");
  		de.addSql("      and a.faceorgno=:orgno ");
  		de.addSql("      and b.pdid=:pdid ");
  		de.addSql("      and b.dptdid=:dpid ");
		this.de.setString("orgno", orgno);
		this.de.setString("pdid", pdid);
		this.de.setString("dpid", dpid);
		DataStore dsOrgDuty = this.de.query();
		if (dsOrgDuty == null || dsOrgDuty.rowCount() == 0) {
			return result;
		}

		// 拼接前台显示的DS
		DataStore dsreturn = DataStore.getInstance();
		for (int i = 0; i < dsOrgDuty.rowCount(); i++) {
			String dutyno = dsOrgDuty.getString(i, "dutyno");
			String inorgname = dsOrgDuty.getString(i, "inorgname");
			String dutyname = dsOrgDuty.getString(i, "dutyname");
			DataObject inorgObj = DataObject.getInstance();
			inorgObj.put("inorgname", inorgname);
			inorgObj.put("dutyname", dutyname);
			dsreturn.addRow(inorgObj);

			// 查询科室下有权处理岗位的人员
			de.clearSql();
  			de.addSql("select  b.empname ");
  			de.addSql("from odssu.emp_outer_duty a, ");
  			de.addSql("     odssu.empinfor b ");
  			de.addSql("where a.empno=b.empno ");
  			de.addSql("      and a.dutyno=:dutyno ");
			this.de.setString("dutyno", dutyno);
			DataStore dsemp = this.de.query();
			if (dsemp == null || dsemp.rowCount() == 0) {
				continue;
			}
			for (int j = 0; j < dsemp.rowCount(); j++) {
				String empname = dsemp.getString(j, "empname");
				DataObject empObj = DataObject.getInstance();
				empObj.put("inorgname", "");
				empObj.put("dutyname", empname);
				dsreturn.addRow(empObj);
			}
		}
		result.put("dslcrwxggwry", dsreturn);// 流程任务相关岗位人员
		return result;
	}
}
