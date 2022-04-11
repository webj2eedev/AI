package com.dw.hsuods.vap.org;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.taglib.multiselecttree.MultiSelectTreeDS;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.hsuods.ws.org.util.FnDataSource;
import com.dw.hsuods.ws.org.util.OuterDutyPDDPDataSource;
import com.dw.util.OdssuUtil;

public class OrgGygwDefineBPO extends BPO{

	public DataObject getFaceOrgnoByDutyno(DataObject para) throws AppException {
		String dutyno = para.getString("dutyno");
  		de.clearSql();
  		de.addSql(" select faceorgno ");
  		de.addSql("   from odssu.outer_duty ");
  		de.addSql("  where dutyno = :dutyno ");
		de.setString("dutyno", dutyno);
		DataStore vds = de.query();
		String faceorgno = "";
		if (vds != null && vds.size() > 0) {
			faceorgno = vds.getString(0, "faceorgno");
		}
		DataObject result = DataObject.getInstance();
		result.put("faceorgno", faceorgno);
		return result;
	}

	public DataObject getInorgInfo(DataObject para) throws AppException {
		String inorgo = para.getString("inorgno");
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where orgno = :inorgo ");
		de.setString("inorgo", inorgo);
		DataStore orgInforVds = de.query();
		String orgtype = "";
		String orgname = "";
		if (orgInforVds != null && orgInforVds.size() > 0) {
			orgtype = orgInforVds.getString(0, "orgtype");
			orgname = orgInforVds.getString(0, "orgname");
		}
		DataObject result = DataObject.getInstance();
		result.put("orgtype", orgtype);
		result.put("orgname", orgname);
		return result;
	}

	public DataObject getRsjorgnoByInorgno(DataObject para) throws AppException {
		String inorgno = para.getString("inorgno");
  		de.clearSql();
  		de.addSql(" select b.orgno ");
  		de.addSql("   from odssu.ir_org_closure a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.orgno = :inorgno ");
  		de.addSql("    and a.belongorgno = b.orgno ");
  		de.addSql("    and b.orgtype in ('HSDOMAIN_DSRSJ','HSDOMAIN_QXRSJ','HSDOMAIN_SRST') ");
		de.setString("inorgno", inorgno);
		DataStore rsjOrgnoVds = de.query();
		String rsjorgno = "";
		if (rsjOrgnoVds != null && rsjOrgnoVds.size() > 0) {
			rsjorgno = rsjOrgnoVds.getString(0, "orgno");
		}
		DataObject result = DataObject.getInstance();
		result.put("rsjorgno", rsjorgno);
		return result;
	}

	/**
	 * 方法简介：查询直属岗位的 流程任务 郑海杰 2015-7-27
	 */
	public DataObject queryZsgwLcrw(DataObject para) throws AppException {
		String dutyno = para.getString("dutyno");
		if (dutyno == null || dutyno.trim().isEmpty()) {
			throw new AppException("获取的dutyno为空");
		}
		String roleno = null;
		DataObject outerDutyInfor = OdssuUtil.getOutDutyInfor(dutyno);
		if (outerDutyInfor != null) {
			roleno = outerDutyInfor.getString("roleno");
		} else {
			roleno = dutyno;
		}
  		de.clearSql();
		// 查询dp
		de.clearSql();
  		de.addSql("  select b.pdlabel,c.dptdlabel");
  		de.addSql("    from bpzone.dutyposition_task_role a,");
  		de.addSql("         bpzone.process_define b,");
  		de.addSql("         bpzone.dutyposition_task c");
  		de.addSql("    where a.roleid = :roleno ");
  		de.addSql("      and a.pdid = b.pdid ");
  		de.addSql("      and a.pdid = c.pdid ");
  		de.addSql("      and a.dptdid =c.dptdid ");
  		de.addSql("      and nvl(c.status,'0') <> '2' ");
		de.setString("roleno", roleno);

		DataStore pdvds = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("lcrw", pdvds);
		return vdo;
	}

	public DataObject queryZsgwGnrw(DataObject para) throws AppException {
		String dutyno = para.getString("dutyno");
		if (dutyno == null || dutyno.trim().isEmpty()) {
			throw new AppException("获取的dutyno为空");
		}
		String roleno = null;
		DataObject outerDutyInfor = OdssuUtil.getOutDutyInfor(dutyno);
		if (outerDutyInfor != null) {
			roleno = outerDutyInfor.getString("roleno");
		} else {
			roleno = dutyno;
		}
  		de.clearSql();
		de.clearSql();
  		de.addSql(" select b.functionid, d.functionname");
  		de.addSql("   from odssu.role_function_manual b, ");
  		de.addSql("        odsv.notbpfn_view d");
  		de.addSql("  where b.roleno = :roleno");
  		de.addSql("    and b.functionid = d.functionid ");
		de.setString("roleno", roleno);
		DataStore functionvds = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("dsfn", functionvds);
		return vdo;
	}

	public DataObject queryZsgwZsry(DataObject para) throws AppException {
		String inorgno = para.getString("inorgno");
		if (inorgno == null || inorgno.trim().isEmpty()) {
			throw new AppException("获取的inorgno为空");
		}

		String dutyno = para.getString("dutyno");
		if (dutyno == null || dutyno.trim().isEmpty()) {
			throw new AppException("获取的dutyno为空");
		}
		String roleno = para.getString("roleno");
		if (roleno == null || roleno.trim().isEmpty()) {
			throw new AppException("获取的roleno为空");
		}
		// 查询本机构的直属人员

		de.clearSql();
  		de.addSql(" select b.empno, b.empname  ");
  		de.addSql("   from odssu.ir_emp_org a, ");
  		de.addSql("        odssu.empinfor b    ");
  		de.addSql("  where a.orgno = :inorgno ");
  		de.addSql("    and a.empno = b.empno  ");
		de.setString("inorgno", inorgno);
		DataStore vds1 = de.query();
		String dutyflag = para.getString("dutyflag");
		if (dutyflag == null || dutyflag.trim().isEmpty()) {
			throw new AppException("获取的dutyflag为空");
		}
		// 根据内外岗 分别查询岗位下的 人员
		DataStore vds2 = DataStore.getInstance();
		if (dutyflag.equals("outerduty")) {
			de.clearSql();
  			de.addSql("   select a.empno");
  			de.addSql("     from odssu.emp_outer_duty a ");
  			de.addSql("    where a.dutyno = :dutyno ");
			de.setString("dutyno", dutyno);
			vds2 = de.query();
		} else if (dutyflag.equals("innerduty")) {
			de.clearSql();
  			de.addSql(" select a.empno");
  			de.addSql("     from odssu.emp_inner_duty a");
  			de.addSql("    where a.roleno = :roleno");
			de.setString("roleno", roleno);
			vds2 = de.query();
		}

		int vds1Count = vds1.rowCount();
		int vds2Count = vds2.rowCount();
		for (int i = 0; i < vds2Count; i++) {
			String hasDutyEmpno = vds2.getString(i, "empno");
			for (int n = 0; n < vds1Count; n++) {
				String hrbelongEmpno = vds1.getString(n, "empno");
				if (hrbelongEmpno.equals(hasDutyEmpno)) {
					vds1.put(n, "own", "√");
				}
			}
		}
		DataObject vdo = DataObject.getInstance();
		vds1.sort("own");
		vdo.put("zsry", vds1);
		return vdo;
	}

	public DataObject queryZsgwYwfc(DataObject para) throws AppException {
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
	 * @描述：取orgno下的所有外岗
	 * @param para
	 * @return
	 * @throws Exception 2015-6-1
	 */
	public DataObject queryYwjgxgwg(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
  		de.clearSql();
		// 取orgno下的所有专岗
		de.clearSql();
  		de.addSql(" select '1' zyfw,a.dutyno,a.inorgno,a.roleno,b.rolename,d.orgname inorgname,b.deforgno ");
  		de.addSql(" from odssu.outer_duty a,odssu.roleinfor b,                                 ");
  		de.addSql("      odssu.orginfor d,odssu.orginfor e                                     ");
  		de.addSql(" where a.inorgno = d.orgno                                                  ");
  		de.addSql(" and a.roleno = b.roleno                                                    ");
  		de.addSql(" and b.deforgno = e.orgno                                                   ");
  		de.addSql(" and a.faceorgno = :orgno                                                        ");
  		de.addSql(" and a.faceorgno = b.deforgno                                                       ");
  		de.addSql(" and b.sleepflag = '0'                                                        ");
  		de.addSql(" and e.orgtype not in ('HSDOMAIN_DSRSXT','HSDOMAIN_SRSXT')                  ");
		this.de.setString("orgno", orgno);
		DataStore dswg = this.de.query();

		// 取该人社局杜鹰的业务范畴
		DataStore rsjYwfc = OdssuUtil.getYwfcVdsByOrgno(orgno);

		// 取该人社局上级的人社系统机构编号
		de.clearSql();
  		de.addSql(" select a.belongorgno           ");
  		de.addSql("  from odssu.ir_org_closure a,  ");
  		de.addSql("       odssu.orginfor b         ");
  		de.addSql(" where a.belongorgno = b.orgno  ");
  		de.addSql("   and a.orgno = :orgno       ");
  		de.addSql("   and b.orgtype in (:dsrsxt,:srsxt)");
		this.de.setString("orgno", orgno);
		this.de.setString("dsrsxt", OdssuContants.ORGTYPE_DSRSXT);
		this.de.setString("srsxt", OdssuContants.ORGTYPE_SRSXT);
		DataStore rsxtOrgds = this.de.query();
		if(rsxtOrgds!=null && rsxtOrgds.size()>0){
			String rsxtorgno = rsxtOrgds.getString(0, "belongorgno");

			// 查询该人社系统下的柜员岗位
			de.clearSql();
  			de.addSql(" select roleno,rolename  ");
  			de.addSql(" from odssu.roleinfor    ");
  			de.addSql("      where roletype = 'HS_GYL' ");
  			de.addSql(" and deforgno = :rsxtorgno ");
  			de.addSql(" and sleepflag = '0' ");
			this.de.setString("rsxtorgno", rsxtorgno);
			DataStore gygwds = this.de.query();

			// 遍历人社系统下的柜员岗位，将其业务范畴取出，并与人社局下的业务范畴比较
			for (int i = 0; i < gygwds.rowCount(); i++) {
				DataStore ywfc = DataStore.getInstance();
				String roleno = gygwds.getString(i, "roleno");
				String rolename = gygwds.getString(i, "rolename");
				String dutyno = "";
				String inorgno = "";
				String inorgname = "(未启用)";

				de.clearSql();
  				de.addSql("select scopeno from odssu.ir_role_business_scope where roleno = :roleno");
				this.de.setString("roleno", roleno);
				ywfc = this.de.query();

				// 如果两个业务范畴集合存在交集，则将该岗位其他信息查出并放于结果集中
				if (getIts(rsjYwfc, ywfc)) {
					de.clearSql();
  					de.addSql(" select a.dutyno,a.inorgno,a.faceorgno,b.orgname inorgname  ");
  					de.addSql(" from odssu.outer_duty a ,");
  					de.addSql("      odssu.orginfor b");
  					de.addSql(" where a.roleno = :roleno");
  					de.addSql(" and a.inorgno = b.orgno ");
					this.de.setString("roleno", roleno);
					DataStore vds = this.de.query();

					// 判断查询出的结果集是否为空,不为空的话将其中的结果进行遍历
					if (vds.rowCount() != 0) {
						// 遍历结果集
						for (int j = 0; j < vds.rowCount(); j++) {
							// 判断外岗表中是否存在该人社局下的该岗位（即该岗位已经在该人社局设置了科室）
							if (orgno.equals(vds.getString(j, "faceorgno"))) {
								dutyno = vds.getString(j, "dutyno");
								inorgno = vds.getString(j, "inorgno");
								inorgname = vds.getString(j, "inorgname");
							}

						}
					}
					int n = dswg.rowCount();
					dswg.put(n, "zyfw", "2");
					dswg.put(n, "roleno", roleno);
					dswg.put(n, "rolename", rolename);
					dswg.put(n, "inorgno", inorgno);
					dswg.put(n, "inorgname", inorgname);
					dswg.put(n, "dutyno", dutyno);
					dswg.put(n, "deforgno", rsxtorgno);
				}
			}
		}
		DataObject result = DataObject.getInstance();
		result.put("dswg", dswg);
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

	/**
	 * @描述：查询岗位职能
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryGwzn(DataObject para) throws Exception {
		String roleno = para.getString("roleno");
  		de.clearSql();
		de.clearSql();
  		de.addSql("select a.pdid, a.dptdid, b.dptdlabel, d.pdlabel,d.pdalias ");
  		de.addSql("from bpzone.dutyposition_task_role     a, ");
  		de.addSql("      bpzone.dutyposition_task          b, ");
  		de.addSql("      bpzone.process_define d ");
  		de.addSql("where a.pdid = b.pdid ");
  		de.addSql("      and a.dptdid = b.dptdid ");
  		de.addSql("      and a.pdid = d.pdid ");
  		de.addSql("      and nvl(b.status,'0') <> '2' ");
  		de.addSql("      and a.roleid = :roleno ");
		this.de.setString("roleno", roleno);

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
	public DataObject queryGnrw(DataObject para) throws Exception {
		String roleno = para.getString("roleno");
  		de.clearSql();
		de.clearSql();
  		de.addSql("select a.roleno, b.functionid, b.functionname, c.fnfolderid, c.folderlabel folderlabel ");
  		de.addSql("from odssu.role_function_manual a, ");
  		de.addSql("     odsv.notbpfn_view b, ");
  		de.addSql("     odssu.fn_folder c ");
  		de.addSql("where a.functionid=b.functionid      ");
  		de.addSql("      and b.fnfolderid=c.fnfolderid ");
  		de.addSql("      and a.roleno=:roleno ");
		this.de.setString("roleno", roleno);
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
	 * @描述：查询岗位下的直属人员
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryZsry(DataObject para) throws Exception {
		String dutyno = para.getString("dutyno");
		String inorgno = null;
		if (dutyno == null || dutyno.equals("null") || dutyno.equals("")) {
			return null;
		} else {
			inorgno = OdssuUtil.getOutDutyInfor(dutyno).getString("inorgno");
		}
  		de.clearSql();
		de.clearSql();
  		de.addSql(" select ei.empname,decode(t.formalflag,'0',null,'1','√') formalflag,ei.empno ");
  		de.addSql("   from  odssu.emp_outer_duty t,odssu.empinfor ei ");
  		de.addSql("  where t.dutyno=:dutyno and t.empno = ei.empno order by empno");
		this.de.setString("dutyno", dutyno);

		DataStore zsry = this.de.query();
		de.clearSql();
  		de.addSql(" select ei.empname,ei.empno,null rolename, null formalflag  ");
  		de.addSql("   from  odssu.empinfor ei ,odssu.ir_emp_org ieo  ");
  		de.addSql("  where ieo.orgno=:inorgno and ieo.empno = ei.empno order by empno");
		this.de.setString("inorgno", inorgno);
		DataStore allemp = this.de.query();

		for (int m = 0; m < allemp.rowCount(); m++) {
			String empnoall = allemp.getString(m, "empno");

			for (int n = 0; n < zsry.rowCount(); n++) {
				String empnozsry = zsry.getString(n, "empno");
				if (empnoall.equals(empnozsry)) {
					allemp.put(m, "formalflag", zsry.getString(n, "formalflag"));
					break;
				}
			}
		}
		
		for(int i = 0; i < allemp.size(); i++){
			String empnoall = allemp.getString(i, "empno");
			de.clearSql();
  			de.addSql(" select rolename ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empnoall ");
  			de.addSql("    and a.orgno = :inorgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :rolenature");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :jsgn");
			this.de.setString("empnoall", empnoall);
			this.de.setString("inorgno", inorgno);
			this.de.setString("rolenature", OdssuContants.ROLENATURE_CYJS);
			this.de.setString("jsgn", OdssuContants.JSGN_POST);
			DataStore roleNameVds = this.de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			for(int j = 0; j < roleNameVds.size(); j++){
				String roleName = roleNameVds.getString(j, "rolename");
				roleNameBF.append(roleName+",");
			}
			if(roleNameBF.length()>0){
				roleNameBF.deleteCharAt(roleNameBF.length()-1);
			}
			allemp.put(i, "rolename", roleNameBF.toString());
		}
		
		DataObject result = DataObject.getInstance();
		allemp.multiSort("rolename:desc,formalflag:asc");
		result.put("zsry", allemp);
		return result;
	}

	/**
	 * @描述：选中流程目录，显示流程目录下的PD+DP，流程任务相关岗位与人员
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryLcflxx(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String pdfolderid = para.getString("pdfolderid");
		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
		// DataStore dsdppd = dptree.genPDDPGridDSByOrgAndFolder(orgno,
		// pdfolderid);
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid" ):OdssuNames.DBID;

		
		/**
		 * 根据orgno和选中流程目录的folderid，显示流程目录下的PD+DP
		 */
		// 1 获取机构对应的 业务范畴
		DataStore ywfcVds = OdssuUtil.getYwfcVdsByOrgno(orgno);
		// 2 获取机构对应的PD
		DataStore vdsPd = odPdDpFolderTree.initDSPD(orgno, ywfcVds, pdfolderid,dbid);
		// 3 获取机构对应的DP
		// 3.1 获取 机构适用的角色类型
		DataStore roleTypeVds = OdssuUtil.getOuterDutyRoleTypeVdsByOrgNo(orgno);
		// 3.2 将角色类型 vds转换为字符串
		String roleTypeStr = OdssuUtil.roleTypeVdsToroleTypeString(roleTypeVds);
		// DataStore vdsDp = odPdDpFolderTree.initDSDPForOrgAll(orgno);
		DataStore vdsDp = odPdDpFolderTree.initDSDP(roleTypeStr);
		// 4 根据DP裁剪PD
		vdsPd = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(vdsPd, vdsDp);
		vdsPd.sort("pdid");
		// 5 根据PD裁剪DP
		vdsDp = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(vdsDp, vdsPd);

		DataStore dspddp = DataStore.getInstance();
		for (int i = 0; i < vdsPd.rowCount(); i++) {
			String pdid = vdsPd.getString(i, "pdid");
			DataStore dsdpget = vdsDp.findAll("pdid == " + pdid);
			if (dsdpget == null || dsdpget.rowCount() == 0) {
				continue;
			}
			for (int j = 0; j < dsdpget.rowCount(); j++) {
				String dpid = dsdpget.getString(j, "dptdid");
				String pdlabel = vdsPd.getString(i, "pdlabel");
				String dptdlabel = dsdpget.getString(j, "dptdlabel");
				DataObject pddpObj = DataObject.getInstance();
				pddpObj.put("pdid", pdid);
				pddpObj.put("dpid", dpid);
				pddpObj.put("pdlabel", pdlabel);
				pddpObj.put("dptdlabel", dptdlabel);
				dspddp.addRow(pddpObj);
			}
		}

		DataObject result = DataObject.getInstance();
		result.put("pddp", dspddp);
		return result;
	}

	/**
	 * @描述：查询尚未设立岗位的流程任务
	 * @param para
	 * @return
	 * @throws Exception 2015-6-11
	 */
	public DataObject querySwslgwdlcrw(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
		// DataStore dspddp = dptree.genPDDPGridDSByOrgNoRole(orgno);
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid" ):OdssuNames.DBID;

		
		/**
		 * 查找一个经办机构所有的流程及岗位任务——显示无岗位的任务功能 （岗位下无角色） （也可以理解为
		 * 一个流程任务，没有ODS中的外岗、内岗与之相对应） 即 对应于 业务岗位设置下的 尚未设置岗位的任务流程
		 */
		// 1 获取机构对应的 业务范畴
		DataStore ywfcVds = OdssuUtil.getYwfcVdsByOrgno(orgno);
		// 2 获取机构对应的PD
		DataStore vdsPd = odPdDpFolderTree.initDSPD(orgno, ywfcVds, null,dbid);
		// 3 获取机构对应的DP
		DataStore vdsDp = odPdDpFolderTree.initDSDPNoRoleForYwjgAndRsjGyOuterDuty(orgno);
		// 4 根据DP裁剪PD
		vdsPd = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(vdsPd, vdsDp);
		vdsPd.sort("pdid");
		// 5 根据PD裁剪DP
		vdsDp = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(vdsDp, vdsPd);

		// 6 组装Grid数据
		DataStore dspddp = DataStore.getInstance();
		for (int i = 0; i < vdsPd.rowCount(); i++) {
			String pdid = vdsPd.getString(i, "pdid");
			DataStore dsdpget = vdsDp.findAll("pdid == " + pdid);
			if (dsdpget == null || dsdpget.rowCount() == 0) {
				continue;
			}

			for (int j = 0; j < dsdpget.rowCount(); j++) {
				String dpid = dsdpget.getString(j, "dptdid");
				String pdlabel = vdsPd.getString(i, "pdlabel");
				String dptdlabel = dsdpget.getString(j, "dptdlabel");
				DataObject pddpObj = DataObject.getInstance();
				pddpObj.put("pdid", pdid);
				pddpObj.put("dpid", dpid);
				pddpObj.put("pdlabel", pdlabel);
				pddpObj.put("dptdlabel", dptdlabel);
				dspddp.addRow(pddpObj);
			}
		}

		DataObject result = DataObject.getInstance();
		result.put("dspddp", dspddp);
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

	/**
	 * @描述：刷新页面的MTree和Grid
	 * @param para
	 * @return
	 * @throws Exception 2015-6-1
	 */
	public DataObject queryXgwgMTreeAndZsryGrid(DataObject para) throws Exception {
		String dutyno = para.getString("dutyno");
		// 刷新树的checkbox

		// 刷新人员,人员在role下是否有权

		de.clearSql();
  		de.addSql(" select a.empname, null currentdp  ");
  		de.addSql(" from odssu.emp_outer_duty t, ");
  		de.addSql("      odssu.empinfor a ");
  		de.addSql(" where t.empno=a.empno ");
  		de.addSql("       and t.dutyno=:dutyno ");
		de.setString("dutyno", dutyno);
		DataStore dsemp = de.query();

		DataObject result = DataObject.getInstance();
		result.put("dsmtree", null);
		result.put("dsemp", dsemp);
		return result;
	}

	/**
	 * @描述：根据机构类型加载业务岗位设置的数据
	 * @param para
	 * @return 2015-6-2
	 */
	public DataObject refreshYwgwszForOrgType(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgflag = para.getString("orgflag");
		int orgflagCase = Integer.parseInt(orgflag);
		String orgtypeStr = "";
		switch (orgflagCase) {
			case 2: // 部门
				orgtypeStr = "'HSDOMAIN_RSCKS'";
				break;
			case 3: // 所
				orgtypeStr = "'HSDOMAIN_SBS'";
				break;
			case 4: // 站
				orgtypeStr = "'HSDOMAIN_SBZ'";
				break;
			default:
				throw new AppException("入参中机构类型不合法。");
		}
		// 加载岗位的Grid

		de.clearSql();
  		de.addSql(" select t.roleno, t.rolename ");
  		de.addSql(" from odssu.inner_duty t ");
  		de.addSql(" where t.orgno=:orgno ");
  		de.addSql("       and t.orgtype = :orgtypeStr");
		de.setString("orgno", orgno);
		de.setString("orgtypeStr", orgtypeStr);
		DataStore dsgw = de.query();

		// 加载树
		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
		// DataStore dstree = mtree.genDPTreeDSByOrgAll(orgno);
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid" ):OdssuNames.DBID;

		
		/**
		 * 上边的原有代码 是要获得 业务岗位设置 树上的流程任务一览节点 获取 一个经办机构下 所有的流程及其对应的岗位任务
		 */
		// 这里要改成掉 私有方法 1 初始化PD 2 初始化 DP 3 用DP裁剪PD 4用PD裁剪DP 5 初始化Folder 6
		// 裁剪Forlder
		DataStore ywfcVds = OdssuUtil.getYwfcVdsByOrgno(orgno);
		// 1 初始化PD
		DataStore pdVds = odPdDpFolderTree.initDSPD(orgno, ywfcVds, null,dbid);
		// 2 初始化 DP
		// 2.1 获取 机构适用的角色类型
		DataStore roleTypeVds = OdssuUtil.getOuterDutyRoleTypeVdsByOrgNo(orgno);
		// 2.3 将角色类型 vds转换为字符串
		String roleTypeStr = OdssuUtil.roleTypeVdsToroleTypeString(roleTypeVds);
		// DataStore dpVds = odPdDpFolderTree.initDSDPForOrgAll(orgno);
		DataStore dpVds = odPdDpFolderTree.initDSDP(roleTypeStr);
		// 3 用DP裁剪PD
		pdVds = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(pdVds, dpVds);
		pdVds.sort("pdid");
		// 4用PD裁剪DP
		dpVds = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(dpVds, pdVds);
		// 5 初始化Folder
		DataStore folderVds = odPdDpFolderTree.initDSFolder();
		// 用PdVds裁剪folder
		folderVds = odPdDpFolderTree.cleanDSFolder(pdVds, folderVds);
		// 组装树对应的DS
		DataStore dstree = odPdDpFolderTree.AssembleMtree(folderVds, pdVds, dpVds);

		MultiSelectTreeDS dsmtree = new MultiSelectTreeDS();
		if (dstree != null && dstree.rowCount() > 0) {
			for (int i = 0; i < dstree.rowCount(); i++) {
				String nodeid = dstree.getString(i, "nodeid");
				String fnodeid = dstree.getString(i, "fnodeid");
				String nodelabel = dstree.getString(i, "nodelabel");
				dsmtree.addItem(nodeid, fnodeid, nodelabel, null, "0");
			}
		}

		DataObject result = DataObject.getInstance();
		result.put("dsgw", dsgw);
		result.put("dsmtree", dsmtree);
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
	 * @描述：展示尚未设立岗位任务的Fn
	 * @param para
	 * @return
	 * @throws Exception 2015-6-11
	 */
	public DataObject querySwslgwdgnrw(DataObject para) throws Exception {
		
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		
		String orgno = para.getString("orgno");
  		de.clearSql();
		de.clearSql();
  		de.addSql("select t.functionid, t.functionname, m.folderlabel ");
  		de.addSql("from( select a.functionid, a.functionname, a.fnfolderid ");
  		de.addSql("      from odsv.notbpfn_view        a, ");
  		de.addSql("           odssu.db_appfunction     b, ");
  		de.addSql("           odssu.fn_business_scope  c, ");
  		de.addSql("           odssu.org_business_scope d, ");
  		de.addSql("           odssu.fn_roletype        e, ");
  		de.addSql("           odssu.ir_org_role_type   f, ");
  		de.addSql("           odssu.orginfor           g ");
  		de.addSql("      where a.functionid = b.functionid ");
  		de.addSql("       and a.functionid = c.functionid ");
  		de.addSql("       and c.scopeid = d.scopeno ");
  		de.addSql("       and a.functionid = e.functionid ");
  		de.addSql("       and e.roletypeno = f.roletypeno ");
  		de.addSql("       and f.orgtypeno = g.orgtype ");
  		de.addSql("       and b.dbid = :dbid ");
  		de.addSql("       and d.orgno = :orgno ");
  		de.addSql("       and g.orgno = :orgno ");
  		de.addSql("       and not exists ( select *  ");
  		de.addSql("                        from odssu.outer_duty h, ");
  		de.addSql("                             odssu.role_function_manual j ");
  		de.addSql("                        where h.roleno=j.roleno  ");
  		de.addSql("                              and j.functionid=a.functionid ");
  		de.addSql("                              and h.faceorgno=:orgno)) t, ");
  		de.addSql("      odssu.fn_folder m ");
  		de.addSql("where t.fnfolderid = m.fnfolderid ");
  		de.addSql("order by t.functionid ");
		this.de.setString("dbid", dbid);
		this.de.setString("orgno", orgno);
		DataStore dsfn = this.de.query();
		DataObject result = DataObject.getInstance();
		result.put("dsfn", dsfn);
		return result;
	}

	/**
	 * @描述：转向流程任务一览的Leaf页面
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject fwPageLcrwylJsp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		// 查询org允许的角色类型

		de.clearSql();
		de.clearSql();
  		de.addSql("select c.typeno value, c.typename content ");
  		de.addSql("from odssu.ir_org_role_type a, ");
  		de.addSql("     odssu.orginfor b, ");
  		de.addSql("     odssu.role_type c ");
  		de.addSql("where a.orgtypeno=b.orgtype ");
  		de.addSql("      and a.roletypeno=c.typeno ");
  		de.addSql("      and b.orgno=:orgno ");
		this.de.setString("orgno", orgno);
		DataStore roletypeds = this.de.query();
		DataStore dspddp = this.queryLcrw(para).getDataStore("dspddp");
		DataObject result = DataObject.getInstance();
		result.put("roletypeds", roletypeds);
		result.put("dspddp", dspddp);
		return result;
	}

	/**
	 * @描述：流程任务
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject queryLcrw(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String roletypeno = para.getString("roletypeno", "");
		String label = para.getString("label", "");
		if (label == null || "".equals(label)) {
			label = "%";
		} else {
			label = "%" + label + "%";
		}
		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
		// DataStore dspddp = dptree.genPDDPGridDSByOrgAndRoleType(orgno,
		// roletypeno, label, ywfcVds);
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid" ):OdssuNames.DBID;

		
		/**
		 * 根据orgno和roletypeno、label，显示流程目录下的PD+DP, 模糊查询
		 */
		// 1 获取业务范畴
		DataStore ywfcVds = OdssuUtil.getYwfcVdsByOrgno(orgno);
		// 2 获取Pd
		DataStore pdVds = odPdDpFolderTree.initDSPD(orgno, ywfcVds, null,dbid);
		// 3 获取Dp
		DataStore dpVds = odPdDpFolderTree.initDSDP(roletypeno, label);
		// 4 根据DP清理PD
		pdVds = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(pdVds, dpVds);
		pdVds.sort("pdid");
		// 5 根据PD清理DP
		dpVds = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(dpVds, pdVds);
		DataStore dspddp = DataStore.getInstance();
		for (int i = 0; i < dpVds.rowCount(); i++) {
			String pdlabel = dpVds.getString(i, "pdlabel");
			String pdid = dpVds.getString(i, "pdid");
			String dpid = dpVds.getString(i, "dptdid");
			String dptdlabel = dpVds.getString(i, "dptdlabel");
			DataObject pddpObj = DataObject.getInstance();
			pddpObj.put("pdid", pdid);
			pddpObj.put("dpid", dpid);
			pddpObj.put("pdlabel", pdlabel);
			pddpObj.put("dptdlabel", dptdlabel);
			dspddp.addRow(pddpObj);
		}

		DataObject result = DataObject.getInstance();
		result.put("dspddp", dspddp);
		return result;
	}

	/**
	 * @描述：转向流程任务一览的Leaf页面
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject fwPageGnrwylJsp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
  		de.clearSql();
		de.clearSql();
  		de.addSql("select c.typeno value, c.typename content ");
  		de.addSql("from odssu.ir_org_role_type a, ");
  		de.addSql("     odssu.orginfor b, ");
  		de.addSql("     odssu.role_type c ");
  		de.addSql("where a.orgtypeno=b.orgtype ");
  		de.addSql("      and a.roletypeno=c.typeno ");
  		de.addSql("      and b.orgno=:orgno ");
		this.de.setString("orgno", orgno);
		DataStore roletypeds = this.de.query();
		DataStore dsfn = this.queryGnrwLike(para).getDataStore("dsfn");
		DataObject result = DataObject.getInstance();
		result.put("roletypeds", roletypeds);
		result.put("dsfn", dsfn);
		return result;
	}

	/**
	 * @描述：流程任务
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject queryGnrwLike(DataObject para) throws Exception {
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid" ):OdssuNames.DBID;

		
		String orgno = para.getString("orgno");
		String roletypeno = para.getString("roletypeno", "");
		String label = para.getString("label", "");
		if (roletypeno == null || "".equals(roletypeno)) {
			roletypeno = "%";
		} else {
			roletypeno = "%" + roletypeno + "%";
		}
		if (label == null || "".equals(label)) {
			label = "%";
		} else {
			label = "%" + label + "%";
		}
		FnDataSource fn = new FnDataSource();
		DataStore ywfcVds = OdssuUtil.getYwfcVdsByOrgno(orgno);
		DataStore dsfn = fn.initFnByOrgAndRoleType(orgno, roletypeno, label, ywfcVds,dbid);
		DataObject result = DataObject.getInstance();
		result.put("dsfn", dsfn);
		return result;
	}

}
