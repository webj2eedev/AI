package com.dw.hsuods.approvequery;

import java.util.Date;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class ApproveQueryBPO extends BPO{
	
	/**
	 * 描述 :查询申请人信息
	 * author: sjn
	 * date: 2016年12月29日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryEmpInfo(DataObject para) throws AppException, BusinessException {
		String empname = para.getString("empname");
		String orgno = para.getString("orgno");
  		  		de.clearSql();
		DataStore vds = DataStore.getInstance();
		
		String empnameUpper = "%"+empname.toUpperCase()+"%";
		empname = "%" + empname + "%";
		
		if (orgno.equals("")) {
			de.clearSql();
  			de.addSql("select distinct a.empno, a.empname, c.orgno, c.orgname ");
  			de.addSql("  from odssu.empinfor a, odssuws.requestinfor b, odssu.orginfor c ");
  			de.addSql(" where a.empno = b.sqr and a.hrbelong = c.orgno ");
  			de.addSql("   and (upper(a.empno) like :empnameupper or upper(a.empname) like :empnameupper or upper(a.rname) like :empnameupper or ");
  			de.addSql("       upper(a.empnamepy) like :empnameupper or upper(a.rnamepy) like :empnameupper)");
			this.de.setString("empnameupper",empnameUpper);
			
			vds = this.de.query();
		}else {
			de.clearSql();
  			de.addSql("select distinct a.empno, a.empname, c.orgno, c.orgname ");
  			de.addSql("  from odssu.empinfor a, odssuws.requestinfor b, odssu.orginfor c ");
  			de.addSql(" where a.empno = b.sqr and a.hrbelong = c.orgno ");
  			de.addSql("   and (upper(a.empno) like :empnameupper or upper(a.empname) like :empnameupper or upper(a.rname) like :empnameupper or ");
  			de.addSql("       upper(a.empnamepy) like :empnameupper or upper(a.rnamepy) like :empnameupper) and a.hrbelong = :orgno ");
			this.de.setString("empnameupper",empnameUpper);
			this.de.setString("orgno",orgno);

			vds = this.de.query();
		}
		
		DataObject result = DataObject.getInstance();
		result.put("empds", vds);
		
		return result;
	}	
	
	
	/**
	 * 描述：查询隶属机构信息
	 * author: sjn
	 * date: 2016年12月29日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryinorg(DataObject para) throws AppException, BusinessException {
		String orgname = para.getString("orgname");
		
		// 当前dbid
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		// 当前的dbid对应的根节点

		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() == 0) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		String rootorgno = orgds.getString(0, "orgno");
		if (rootorgno == null || rootorgno.equals("")) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		
		String orgnameUpper = "%"+orgname.toUpperCase()+"%";
		orgname = "%" + orgname + "%";
  		  		de.clearSql();
		de.clearSql();
  		de.addSql("  select * from( ");
  		de.addSql("select a.orgno, a.orgname, a.displayname, a.fullname,b.typename orgtype");
  		de.addSql("  from odssu.orginfor a,odssu.org_type b ");
  		de.addSql(" where (a.orgno like :orgname or upper(orgname) like :orgnameupper or upper(orgnamepy) like :orgnameupper or ");
  		de.addSql("       upper(displayname) like :orgnameupper or upper(displaynamepy) like :orgnameupper or ");
  		de.addSql("       upper(fullname) like :orgnameupper or upper(fullnamepy) like :orgnameupper) ");
  		de.addSql("   and b.typenature in ('B','C') ");
  		de.addSql("   and b.typeno = a.orgtype ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("           and exists (select 1 from odssu.ir_org_closure c where c.orgno = a.orgno and c.belongorgno = :rootorgno) ");
  		de.addSql("   order by b.sn asc,a.orgno asc");
  		de.addSql("   )  ");
		de.setString("orgname", orgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("rootorgno", rootorgno);
		de.setQueryScope(300);
		DataStore dstemp = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("ksds", dstemp);
		
		return result;
	}	
	
	/**
	 * 描述：通过限制条件查询申请结果
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryApproveInfo(DataObject para) throws AppException, BusinessException {
		String ywblzt = para.getString("ywblzt");
		Date kssj = para.getDate("kssj");
		Date jssj = para.getDate("jssj");
		
		if (kssj.after(jssj)) {
			throw new BusinessException("您选择的开始时间比结束时间晚，请重新选择！");
		}
		
		DataObject appvdo = DataObject.getInstance();
		if (ywblzt.equals("1")) {//如果选择办结的业务
			appvdo.put("approveQueryResult", queryBJApproveInfo(para));
			
		}else if (ywblzt.equals("2")) {//如果选择待办的业务
			appvdo.put("approveQueryResult", queryDBApproveInfo(para));
		}else {
			DataStore appvds = DataStore.getInstance();
			DataStore approveInfoDS = queryDBApproveInfo(para);
			approveInfoDS.combineDatastore(queryBJApproveInfo(para));
			appvds = approveInfoDS.clone();
			appvdo.put("approveQueryResult", appvds);
		}
		return appvdo;
	}
	
	/**
	 * 描述：查询办结的申请业务信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	private DataStore queryBJApproveInfo(DataObject para) throws AppException, BusinessException {
		String empno = para.getString("empno");
		String orgno = para.getString("orgno");
		Date kssj = para.getDate("kssj");
		Date jssj = para.getDate("jssj");
		String spyjdm = para.getString("spyjdm","");
		DataStore vds = DataStore.getInstance();
		
		de.clearSql();
  		de.addSql("select a.piid, b.empname, c.orgname hrbelong, a.sqsj, d.pdlabel, '1' zt ");
  		de.addSql("  from odssuws.requestinfor               a, ");
  		de.addSql("       odssu.empinfor                     b, ");
  		de.addSql("       odssu.orginfor                     c, ");
  		de.addSql("       bpzone.pi_hi_view_with_define_info d, ");
  		de.addSql("       odssuws.spinfor                    e ");
  		de.addSql(" where a.sqr = b.empno  ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = d.piid ");
  		de.addSql("   and a.piid = e.piid");
  		de.addSql("   and d.endtime is not null ");
  		de.addSql("   and (a.sqsj between :kssj and :jssj) ");
		if (!(empno == null || empno.equals(""))) {
  			de.addSql("   and a.sqr = :empno");
		}
		if (!(orgno == null || orgno.equals(""))) {
  			de.addSql("   and b.hrbelong = :orgno");
		}
		if(!(spyjdm == null || spyjdm.equals(""))){
  			de.addSql("   and e.spyjdm = :spyjdm");
		}
  		de.addSql("  order by a.sqsj desc ");
		this.de.setDateTime("kssj",kssj);
		this.de.setDateTime("jssj",jssj);
		if (!(empno == null || empno.equals(""))) {
  			this.de.setString("empno", empno);
		}
		if (!(orgno == null || orgno.equals(""))) {
			this.de.setString("orgno", orgno);
		}
		if(!(spyjdm == null || spyjdm.equals(""))){
			this.de.setString("spyjdm", spyjdm);
		}
		vds = this.de.query();
		
		return vds;
	}
	
	/**
	 * 描述：查询待办的申请业务信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	private DataStore queryDBApproveInfo(DataObject para) throws AppException, BusinessException {
		String empno = para.getString("empno");
		String orgno = para.getString("orgno");
		Date kssj = para.getDate("kssj");
		Date jssj = para.getDate("jssj");
		DataStore vds = DataStore.getInstance();
		
		de.clearSql();
  		de.addSql("select a.piid, b.empname, c.orgname hrbelong, a.sqsj, d.pdlabel, '0' zt ");
  		de.addSql("  from odssuws.requestinfor               a, ");
  		de.addSql("       odssu.empinfor                     b, ");
  		de.addSql("       odssu.orginfor                     c, ");
  		de.addSql("       bpzone.pi_hi_view_with_define_info d, ");
  		de.addSql("       bpzone.ti_view                     e, ");
  		de.addSql("       bpzone.task_point                  f ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = d.piid ");
  		de.addSql("   and a.piid = e.piid ");
  		de.addSql("   and e.tpid = f.tpid ");
  		de.addSql("   and f.tdid = 'ods_approve' ");
  		de.addSql("   and (a.sqsj between :kssj and :jssj) ");
		if (!(empno == null || empno.equals(""))) {
  			de.addSql("   and a.sqr = :empno");
		}
		if (!(orgno == null || orgno.equals(""))) {
  			de.addSql("   and b.hrbelong = :orgno");
		}
  		de.addSql("  order by a.sqsj desc ");
		this.de.setDateTime("kssj",kssj);
		this.de.setDateTime("jssj",jssj);
		if (!(empno == null || empno.equals(""))) {
			this.de.setString("empno", empno);
		}
		if (!(orgno == null || orgno.equals(""))) {
			this.de.setString("orgno", orgno);
		}
		vds = this.de.query();
		
		return vds;
	}
	
	/**
	 * 描述：查询外岗新增、修改业务详情信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveWGXG(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		de.setString("piid",piid);
		DataStore sqvds = de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		de.setString("piid",piid);
		DataStore spvds = de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryGwspWGInfo(para);
		String deforgno = gwvds.getString(0, "deforgno");
		String inorgno = gwvds.getString(0, "inorgno");
		String faceorgno = gwvds.getString(0, "faceorgno");
		
		// 查询业务范畴差异
		DataStore ywfcvds = queryYwfcDiffer(para);
		boolean ywfcflag = ywfcvds.getBoolean(0, "ywfcflag");
		
		// 查询流程任务差异
		DataStore lcrwvds = queryLcrwDiffer(para);
		boolean lcrwflag = lcrwvds.getBoolean(0, "lcrwflag");
		
		// 查询功能任务差异
		DataStore gnrwvds = queryGnrwDiffer(para);
		boolean gnrwflag = gnrwvds.getBoolean(0, "gnrwflag");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("deforgno", deforgno);
		vdo.put("inorgno", inorgno);
		vdo.put("faceorgno", faceorgno);
		vdo.put("ywfcresult", ywfcvds);
		vdo.put("ywfcflag", ywfcflag);
		vdo.put("lcrwresult", lcrwvds);
		vdo.put("lcrwflag", lcrwflag);
		vdo.put("gnrwresult", gnrwvds);
		vdo.put("gnrwflag", gnrwflag);
		
		return vdo;
	}
	
	/**
	 * 描述：查询内岗新增、修改业务详情信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveNGXG(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryGwspNGInfo(para);
		
		// 查询业务范畴差异
		DataStore ywfcvds = queryYwfcDiffer(para);
		boolean ywfcflag = ywfcvds.getBoolean(0, "ywfcflag");
		
		// 查询流程任务差异
		DataStore lcrwvds = queryLcrwDiffer(para);
		boolean lcrwflag = lcrwvds.getBoolean(0, "lcrwflag");
		
		// 查询功能任务差异
		DataStore gnrwvds = queryGnrwDiffer(para);
		boolean gnrwflag = gnrwvds.getBoolean(0, "gnrwflag");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("ywfcresult", ywfcvds);
		vdo.put("ywfcflag", ywfcflag);
		vdo.put("lcrwresult", lcrwvds);
		vdo.put("lcrwflag", lcrwflag);
		vdo.put("gnrwresult", gnrwvds);
		vdo.put("gnrwflag", gnrwflag);
		
		return vdo;
	}
	
	/**
	 * 描述：查询岗位复制业务详情信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveGWFZ(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryGwfzInfo(para);
		String inorgno = gwvds.getString(0, "inorgno");
		String faceorgno = gwvds.getString(0, "faceorgno");
		
		// 查询业务范畴差异
		DataStore ywfcvds = queryYwfcDiffer(para);
		boolean ywfcflag = ywfcvds.getBoolean(0, "ywfcflag");
		
		// 查询流程任务差异
		DataStore lcrwvds = queryLcrwDiffer(para);
		boolean lcrwflag = lcrwvds.getBoolean(0, "lcrwflag");
		
		// 查询功能任务差异
		DataStore gnrwvds = queryGnrwDiffer(para);
		boolean gnrwflag = gnrwvds.getBoolean(0, "gnrwflag");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("inorgno", inorgno);
		vdo.put("faceorgno", faceorgno);
		vdo.put("ywfcresult", ywfcvds);
		vdo.put("ywfcflag", ywfcflag);
		vdo.put("lcrwresult", lcrwvds);
		vdo.put("lcrwflag", lcrwflag);
		vdo.put("gnrwresult", gnrwvds);
		vdo.put("gnrwflag", gnrwflag);
		
		return vdo;
	}
	
	/**
	 * 描述：查询外岗岗位信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryGwspWGInfo(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		// 查询岗位信息
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select a.dutyno,a.deforgno, a.rolename,a.faceorgno,a.inorgno,c.typename  ");
  		de.addSql("  from odssuws.outerduty a, odssu.role_type c ");
  		de.addSql("  where a.roletypeno = c.typeno  ");
  		de.addSql("  and piid = :piid ");
		de.setString("piid", piid);
		DataStore vds = de.query();
		if (vds == null || vds.rowCount() == 0) {
			throw new BusinessException("没有要查询的岗位信息。");
		}
		
		String dutyno = vds.getString(0, "dutyno");
		String deforgno = vds.getString(0, "deforgno");
		String faceorgno = vds.getString(0, "faceorgno");
		String inorgno = vds.getString(0, "faceorgno");
		// 判断dutyno是不是还存在
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssu.outer_duty ");
  		de.addSql(" where dutyno = :dutyno ");
		de.setString("dutyno",dutyno);
		DataStore dutyDataStore = de.query();
		if (dutyno != null && !dutyno.equals("") && !(dutyDataStore == null || dutyDataStore.rowCount() == 0)) {
			de.clearSql();
  			de.addSql("select a.faceorgno,a.inorgno,c.orgname ");
  			de.addSql("  from odssu.outer_duty  a, ");
  			de.addSql("       odssu.roleinfor   b, ");
  			de.addSql("       odssu.orginfor    c ");
  			de.addSql(" where b.deforgno = c.orgno ");
  			de.addSql("   and a.roleno = b.roleno ");
  			de.addSql("   and a.dutyno = :dutyno ");
			de.setString("dutyno",dutyno);
			DataStore dutyvds = de.query();
			
			if (dutyvds == null || dutyvds.rowCount() == 0) {
				throw new BusinessException("岗位已经不存在。");
			}
			
			faceorgno = dutyvds.getString(0, "faceorgno");
			inorgno = dutyvds.getString(0, "inorgno");
			
			vds.put(0, "faceorgname", OdssuUtil.getOrgNameByOrgno(faceorgno));
			vds.put(0, "inorgname", OdssuUtil.getOrgNameByOrgno(inorgno));
			vds.put(0, "deforg", dutyvds.getString(0, "orgname"));
		}else if (deforgno != null && !deforgno.equals("")) {
			de.clearSql();
  			de.addSql("select orgname ");
  			de.addSql("  from odssu.orginfor ");
  			de.addSql(" where orgno = :deforgno ");
			de.setString("deforgno",deforgno);
			DataStore deforgvds = de.query();
			vds.put(0, "deforg", deforgvds.getString(0, "orgname"));
			if (faceorgno != null && !faceorgno.equals("")) {
				vds.put(0, "faceorgname", OdssuUtil.getOrgNameByOrgno(faceorgno));
			}else {
				vds.put(0, "faceorgname", deforgvds.getString(0, "orgname"));
			}
			if (inorgno != null && !inorgno.equals("")) {
				vds.put(0, "inorgname", OdssuUtil.getOrgNameByOrgno(inorgno));
			}
		}else {
			if (faceorgno != null && !faceorgno.equals("")) {
				String faceorgname = OdssuUtil.getOrgNameByOrgno(faceorgno);
				vds.put(0, "faceorgname", faceorgname);
				vds.put(0, "deforg", faceorgname);
			}
			if (inorgno != null && !inorgno.equals("")) {
				vds.put(0, "inorgname", OdssuUtil.getOrgNameByOrgno(inorgno));
			}
		}
	
		vds.put(0, "deforgno", deforgno);
		vds.put(0, "faceorgno", faceorgno);
		vds.put(0, "inorgno", inorgno);
		
		return vds;
	}
	
	/**
	 * 描述：查询内岗岗位信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryGwspNGInfo(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		
		// 查询岗位信息
		de.clearSql();
  		de.addSql("select a.roletypeno, a.deforgno, a.rolename,c.typename  ");
  		de.addSql("  from odssuws.inner_duty a, odssu.role_type c ");
  		de.addSql("  where a.roletypeno = c.typeno  ");
  		de.addSql("  and piid = :piid ");
		this.de.setString("piid", piid);
		DataStore vds = this.de.query();
		
		if (vds == null || vds.rowCount() == 0) {
			throw new BusinessException("没有要查询的内岗岗位信息。");
		}
	
		String deforgno = vds.getString(0, "deforgno");
		
		vds.put(0, "deforg", OdssuUtil.getOrgNameByOrgno(deforgno));
		
		return vds;
	}
	
	/**
	 * 描述：查询岗位复制岗位信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryGwfzInfo(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		
		// 查询岗位信息
		de.clearSql();
  		de.addSql("select a.roletype, a.deforgno, a.rolename,a.faceorgno,a.inorgno,c.typename  ");
  		de.addSql("  from odssuws.gwfz a, odssu.role_type c ");
  		de.addSql("  where a.roletype = c.typeno  ");
  		de.addSql("  and piid = :piid ");
		this.de.setString("piid", piid);
		DataStore vds = this.de.query();
	
		String deforgno = vds.getString(0, "deforgno");
		String faceorgno = vds.getString(0, "faceorgno");
		String inorgno = vds.getString(0, "inorgno");
		
		String faceorgname = "",inorgname = "";
		if (faceorgno != null && !faceorgno.equals("")) {
			faceorgname = OdssuUtil.getOrgNameByOrgno(faceorgno);
		}
		if (inorgno != null && !inorgno.equals("")) {
			inorgname = OdssuUtil.getOrgNameByOrgno(inorgno);
		}
		
		vds.put(0, "deforg", OdssuUtil.getOrgNameByOrgno(deforgno));
		vds.put(0, "faceorgno", faceorgno);
		vds.put(0, "faceorgname", faceorgname);
		vds.put(0, "inorgno", inorgno);
		vds.put(0, "inorgname", inorgname);
		
		return vds;
	}
	
	/**
	 * 描述：查询业务范畴差异
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryYwfcDiffer(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		boolean ywfcflag = true;
		// 查询业务范畴差异信息
		DE de =DE.getInstance();
		de.clearSql();
  		de.addSql("select distinct b.scopename, a.differ ");
  		de.addSql("  from odssuws.differ_duty_ywfc a, odssu.business_scope b ");
  		de.addSql("  where a.scopeno = b.scopeno ");
  		de.addSql("  and piid = :piid order by a.differ desc");
		de.setString("piid", piid);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() == 0){
			ywfcflag = false;
		}
		vds.put(0, "ywfcflag", ywfcflag);
		
		return vds;
	}
	
	/**
	 * 描述：查询流程任务差异
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryLcrwDiffer(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		boolean lcrwflag = true;
		// 查询流程任务差异信息
		de.clearSql();
  		de.addSql("select distinct a.pdid, ");
  		de.addSql("       a.dpid, ");
  		de.addSql("       b.pdlabel, ");
  		de.addSql("       c.dptdlabel, ");
  		de.addSql("       a.differ ");
  		de.addSql("  from odssuws.differ_duty_lcrw          a, ");
  		de.addSql("       bpzone.process_define_in_activiti b, ");
  		de.addSql("       bpzone.dutyposition_task          c ");
  		de.addSql(" where a.pdid = b.pdid ");
  		de.addSql("   and a.pdid = c.pdid ");
  		de.addSql("   and a.dpid = c.dptdid ");
  		de.addSql("   and a.piid = :piid order by a.differ desc");
		this.de.setString("piid", piid);
		DataStore vds = this.de.query();
		if(vds == null || vds.rowCount() == 0){
			lcrwflag = false;
		}
		vds.put(0, "lcrwflag", lcrwflag);
		
		return vds;
	}
	
	/**
	 * 描述：查询功能任务差异
	 * author: sjn
	 * date: 2017年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryGnrwDiffer(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		boolean gnrwflag = true;
		// 查看功能任务差异信息
		de.clearSql();
  		de.addSql("select distinct a.functionid, ");
  		de.addSql("       b.functionname, ");
  		de.addSql("       a.differ ");
  		de.addSql("  from odssuws.differ_duty_gnrw a, odssu.appfunction b ");
  		de.addSql(" where a.functionid = b.functionid ");
  		de.addSql("   and a.piid = :piid order by a.differ desc ");
		this.de.setString("piid",piid);
		DataStore vds = this.de.query();
		if(vds == null || vds.rowCount() == 0){
			gnrwflag = false;
		}
		vds.put(0, "gnrwflag", gnrwflag);
		
		return vds;
	}
	
	/**
	 * 描述：查询外岗人员调整业务详情信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveWGRY(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryGwspWGInfo(para);
		String inorgno = gwvds.getString(0, "inorgno");
		String faceorgno = gwvds.getString(0, "faceorgno");
		
		// 查询岗位人员差异
		para.put("inorgno", inorgno);
		DataStore ryxxvds = queryRyxxDiffer(para);
		boolean ryxxflag = ryxxvds.getBoolean(0, "ryxxflag");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("inorgno", inorgno);
		vdo.put("faceorgno", faceorgno);
		vdo.put("ryxxresult", ryxxvds);
		vdo.put("ryxxflag", ryxxflag);
		
		return vdo;
	}
	
	/**
	 * 描述：查询内岗人员调整业务详情信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveNGRY(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryNgrytzInfo(para);
		String inorgno = gwvds.getString(0, "inorgno");
		
		// 查询岗位人员差异
		para.put("inorgno", inorgno);
		DataStore ryxxvds = queryRyxxDiffer(para);
		boolean ryxxflag = ryxxvds.getBoolean(0, "ryxxflag");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("inorgno", inorgno);
		vdo.put("ryxxresult", ryxxvds);
		vdo.put("ryxxflag", ryxxflag);
		
		return vdo;
	}
	
	/**
	 * 描述：查询内岗人员调整岗位信息
	 * author: sjn
	 * date: 2017年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryNgrytzInfo(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		// 查询岗位信息
		de.clearSql();
  		de.addSql("select a.roletypeno, a.deforgno,a.roleno, a.rolename,c.typename,d.orgname inorgname  ");
  		de.addSql("  from odssuws.inner_duty_sc_xg a, odssu.role_type c, odssu.orginfor d ");
  		de.addSql("  where a.roletypeno = c.typeno  ");
  		de.addSql("  and a.deforgno = d.orgno  ");
  		de.addSql("  and piid = :piid ");
		this.de.setString("piid", piid);
		DataStore vds = this.de.query();
		
		if (vds == null || vds.rowCount() == 0) {
			throw new BusinessException("没有要查询的内岗岗位信息。");
		}
		
		String inorgno = vds.getString(0, "deforgno");
		String roleno = vds.getString(0, "roleno");
		de.clearSql();
  		de.addSql("select b.orgname  ");
  		de.addSql("  from odssu.roleinfor a , odssu.orginfor b ");
  		de.addSql("  where a.deforgno = b.orgno  ");
  		de.addSql("  and a.roleno = :roleno ");
		this.de.setString("roleno", roleno);
		DataStore deforgvds = this.de.query();
		String orgname = deforgvds.getString(0, "orgname");
		
		vds.put(0, "inorgno", inorgno);
		vds.put(0, "deforg", orgname);
		
		return vds;
	}
	
	/**
	 * 描述：查询人员信息差异
	 * author: sjn
	 * date: 2017年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryRyxxDiffer(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String inorgno = para.getString("inorgno");
		boolean ryxxflag = true;
		DataStore vds = DataStore.getInstance();
		
		
		de.clearSql();
		de.addSql("select distinct b.loginname, b.empname, e.rolename, a.differ,e.rolesn ");
		de.addSql("  from odssu.empinfor b, ");
		de.addSql("           odssuws.differ_duty_ryxx a left outer join ");
		de.addSql("       (select c.empno, d.rolename,c.roleno,d.rolesn ");
		de.addSql("          from odssu.ir_emp_org_all_role c, odssu.roleinfor d ");
		de.addSql("         where c.jsgn = '1' ");
		de.addSql("           and c.orgno = :inorgno ");
		de.addSql("           and c.roleno = d.roleno) e ");
		de.addSql(" 			on a.empno = e.empno ");
		de.addSql("   where a.empno = b.empno ");
		de.addSql("   and a.piid = :piid ");
		de.addSql("   order by a.differ desc,e.rolesn ");
		this.de.setString("inorgno", inorgno);
		this.de.setString("piid", piid);
		vds = this.de.query();
		
		if (vds == null || vds.rowCount() == 0) {
			ryxxflag = false;
		}
		
		vds.put(0, "ryxxflag", ryxxflag);
		
		return vds;
	}
	
	/**
	 * 描述：查询隶属科室调整业务详情信息
	 * author: sjn
	 * date: 2016年12月30日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveLSKS(DataObject para) throws AppException ,BusinessException{
		String piid = para.getString("piid");
  		  		de.clearSql();
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryGwspWGInfo(para);
		String faceorgno = gwvds.getString(0, "faceorgno");
		
		// 查询隶属科室差异
		DataStore lsksvds = queryLsksDiffer(para);
		boolean lsksflag = lsksvds.getBoolean(0, "lsksflag");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("faceorgno", faceorgno);
		vdo.put("lsksresult", lsksvds);
		vdo.put("lsksflag", lsksflag);
		
		return vdo;
	}
	
	/**
	 * 描述：查询隶属科室差异
	 * author: sjn
	 * date: 2017年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataStore queryLsksDiffer(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		boolean lsksflag = true;
		DataStore vds = DataStore.getInstance();
		
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssuws.differ_duty_lsks ");
  		de.addSql(" where piid = :piid ");
		this.de.setString("piid",piid);
		DataStore flagvds = this.de.query();
		
		if (flagvds == null || flagvds.rowCount() == 0) {
			lsksflag = false;
		}else {
			this.de.clearSql();
			this.de.addSql("select wm_concat(b.orgname) oldinorg ");
  			this.de.addSql("  from odssuws.differ_duty_lsks a, odssu.orginfor b ");
  			this.de.addSql(" where a.inorgno = b.orgno ");
  			this.de.addSql(" and a.differ = '0' ");
  			this.de.addSql(" and a.piid = :piid ");
			this.de.setString("piid",piid);
			vds = this.de.query();
			for(int i = 0 ;i <vds.rowCount();i++) {
				vds.put(i, "oldinorg", vds.getClobAsString(i, "oldinorg"));
			}
			
			this.de.clearSql();
			this.de.addSql("select wm_concat(b.orgname) newinorg ");
			this.de.addSql("  from odssuws.differ_duty_lsks a, odssu.orginfor b ");
			this.de.addSql(" where a.inorgno = b.orgno ");
			this.de.addSql(" and a.differ = '1' ");
			this.de.addSql(" and a.piid = :piid ");
			this.de.setString("piid",piid);
			DataStore newvds = this.de.query();
			
			vds.put(0, "newinorg", newvds.getClobAsString(0, "newinorg"));
			vds.put(0, "lsksflag", lsksflag);
		}
		
		vds.put(0, "lsksflag", lsksflag);
		
		return vds;
	}
	
	/**
	 * 描述：查询外岗岗位删除信息
	 * author: sjn
	 * date: 2017年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveWGSC(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		DataStore gwvds = queryGwspWGInfo(para);
		String faceorgno = gwvds.getString(0, "faceorgno");
		String inorgno = gwvds.getString(0, "inorgno");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		vdo.put("faceorgno", faceorgno);
		vdo.put("inorgno", inorgno);
		
		return vdo;
	}
	
	/**
	 * 描述：查询内岗岗位删除信息
	 * author: sjn
	 * date: 2017年1月4日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveNGSC(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		// 查询岗位信息
		de.clearSql();
  		de.addSql("select b.rolename, c.orgname deforg, d.typename ");
  		de.addSql("  from odssuws.inner_duty_sc_xg a, ");
  		de.addSql("       odssu.roleinfor          b, ");
  		de.addSql("       odssu.orginfor           c, ");
  		de.addSql("       odssu.role_type          d ");
  		de.addSql(" where a.roleno = b.roleno ");
  		de.addSql("   and b.deforgno = c.orgno ");
  		de.addSql("   and b.roletype = d.typeno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid", piid);
		DataStore gwvds = this.de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("gwresult", gwvds);
		
		return vdo;
	}
	
	/**
	 * 描述：查询人员业务岗位调整信息
	 * author: sjn
	 * date: 2017年1月5日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryDetailApproveRYGW(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		boolean rygwflag = true;
		DataStore gwvds = DataStore.getInstance();
		DataStore ryxxvds = DataStore.getInstance();
		// 查询申请信息
		de.clearSql();
  		de.addSql("select b.empname sqr, a.sqsj, a.sqyy, c.orgname ");
  		de.addSql("  from odssuws.requestinfor a, odssu.empinfor b, odssu.orginfor c ");
  		de.addSql(" where a.sqr = b.empno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqvds = this.de.query();
		
		// 查询审批信息
		de.clearSql();
  		de.addSql("select b.empname spr, a.spsj, a.spyjdm, a.spsm ");
  		de.addSql("  from odssuws.spinfor a, odssu.empinfor b ");
  		de.addSql(" where a.spr = b.empno ");
  		de.addSql("   and a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore spvds = this.de.query();
		
		//查询岗位差异
		de.clearSql();
  		de.addSql("select a.faceorgno,a.inorgno from odssuws.differ_emp_duty a where a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore orgvds = this.de.query();
		
		if (orgvds == null || orgvds.rowCount() == 0) {
			rygwflag = false;
		}else {
			//查询人员信息
			de.clearSql();
  			de.addSql("select d.empname,b.loginname,c.orgname ");
  			de.addSql("  from odssu.empinfor b, odssu.orginfor c, odssuws.ryywgwtz d ");
  			de.addSql(" where d.empno = b.empno ");
  			de.addSql("   and b.hrbelong = c.orgno ");
  			de.addSql("   and d.piid = :piid ");
			this.de.setString("piid",piid);
			ryxxvds = this.de.query();
			
			//查询岗位信息
			de.clearSql();
			de.addSql("select b.rolename, ");
			de.addSql("       c.orgname faceorgname, ");
			de.addSql("       d.orgname inorgname, ");
			de.addSql("       a.differ ");
			de.addSql("  from odssu.roleinfor         b, ");
			de.addSql("       odssuws.differ_emp_duty a left outer join odssu.orginfor c ");
			de.addSql("       on a.faceorgno = c.orgno, ");
			de.addSql("       odssu.orginfor          d right outer join odssuws.differ_emp_duty t ");
			de.addSql("       on d.orgno = t.inorgno ");
			de.addSql(" where a.roleno = b.roleno ");
			de.addSql("   and a.piid = :piid ");
			de.addSql("   order by a.differ desc ");
			this.de.setString("piid", piid);
			gwvds = this.de.query();
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("sqxxresult", sqvds);
		vdo.put("spxxresult", spvds);
		vdo.put("ryxxresult", ryxxvds);
		vdo.put("rygwflag", rygwflag);
		vdo.put("gwresult", gwvds);
		
		return vdo;
	}
}
