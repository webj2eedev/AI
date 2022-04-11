package com.dw.hsuods.vap.pd.query;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dw.util.jxkh.PubUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class PdQueryBPO extends BPO{
	
	/**
	 * 方法详述:查询流程，入参orgno为空查询全部流程，不为空查询机构适用的流程
	 * @author fandq
	 * @date 创建时间 2016年11月18日
	 * @since V1.0
	 */
	public DataObject queryPdAboutOrg(DataObject para) throws AppException, BusinessException{
		String orgno = para.getString("orgno","");
		String querylabel = para.getString("querylabel","");
		DataStore pdds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();
		if (orgno.equals("")) {
			pdds = initDbidDSPDwithlabel(querylabel);
		}else {
			pdds = getAllpdAboutOrg(orgno,querylabel);
		}
		
		pdds.sort("pdid");
		
		result.put("pd", pdds);
		return result;
		
	}
	/**
	 * 方法详述:查询流程适用机构
	 * @author fandq
	 * @date 创建时间 2016年11月18日
	 * @since V1.0
	 */
	public DataObject queryPdAdaptOrg(DataObject para) throws AppException, BusinessException{
		String pdid = para.getString("pdid");
		boolean customPdAdaptOrg = para.getBoolean("customPdAdaptOrg",false);
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
  		de.clearSql();
  		de.addSql(" select standardflag,standard_pdid ");
  		de.addSql("   from bpzone.process_define ");
  		de.addSql("  where pdid = :pdid ");
		this.de.setString("pdid", pdid);
		DataStore stanardFlagVds = this.de.query();
		String standardFlag = null;
		if(stanardFlagVds != null && stanardFlagVds.size() > 0){
			standardFlag = stanardFlagVds.getString(0, "standardflag");
		}else{
			throw new AppException("未查询到编号为【" + pdid +"】流程的标准化信息!");
		}
		DataStore pdAdaptOrgVds = null;
		if("0".equals(standardFlag) || "1".equals(standardFlag)){
			pdAdaptOrgVds = getStandardPdAdaptOrg(pdid);
		}else{
			pdAdaptOrgVds = getCustomedPdAdaptOrg(pdid);
		}
		
		
		//获取该业务的相关流程适用的机构
		DataStore relatedPdAdaptOrg = DataStore.getInstance();
		if (customPdAdaptOrg) {
			if("0".equals(standardFlag) || "1".equals(standardFlag)){
				de.clearSql();
  				de.addSql("select null isadapt, ");
  				de.addSql("       a.orgno, ");
  				de.addSql("       a.orgname, ");
  				de.addSql("       '适用' || a.customed_pdid comments, ");
  				de.addSql("       0 sn ");
  				de.addSql("  from bpzone.pd_customed a ");
  				de.addSql(" where a.standard_pdid = :pdid ");
  				de.addSql("   and a.dbid = :dbid ");
				this.de.setString("pdid",pdid);
				this.de.setString("dbid",dbid);
				relatedPdAdaptOrg = this.de.query();
			}else{
				String standardpdid = stanardFlagVds.getString(0,"standard_pdid");
				relatedPdAdaptOrg = getStandardPdAdaptOrg(standardpdid);
				
				for (int i = 0; i < relatedPdAdaptOrg.size(); i++) {
					relatedPdAdaptOrg.put(i, "isadapt", "");
					relatedPdAdaptOrg.put(i, "comments", "适用"+standardpdid);
				}
				
				
				de.clearSql();
  				de.addSql("select null isadapt, ");
  				de.addSql("       a.orgno, ");
  				de.addSql("       a.orgname, ");
  				de.addSql("       '适用' || a.customed_pdid comments, ");
  				de.addSql("       0 sn ");
  				de.addSql("  from bpzone.pd_customed a ");
  				de.addSql(" where a.standard_pdid = :standardpdid ");
  				de.addSql("   and a.dbid = :dbid ");
  				de.addSql("   and a.customed_pdid <> :pdid ");
				this.de.setString("standardpdid",standardpdid);
				this.de.setString("dbid",dbid);
				this.de.setString("pdid",pdid);
				DataStore relatedPdAdaptOrg_tmp = this.de.query();
				
				relatedPdAdaptOrg.combineDatastore(relatedPdAdaptOrg_tmp);
				
			}
		}
		pdAdaptOrgVds.combineDatastore(relatedPdAdaptOrg);
		MultiSortUtil.multiSortDS(pdAdaptOrgVds, "isadapt:desc,sn:asc,orgno:asc");
		//获取可以办理流程的机构类型
		DataStore vds = DataStore.getInstance();
		String typename = "";

		de.clearSql();
  		de.addSql("select b.typename ");
  		de.addSql("  from odssu.ir_org_role_type a, odssu.org_type b ");
  		de.addSql(" where a.orgtypeno = b.typeno ");
  		de.addSql("   and exists (select 1 ");
  		de.addSql("          from bpzone.dproletype c ");
  		de.addSql("         where a.roletypeno = c.roletypeid ");
  		de.addSql("           and c.pdid = :pdid) ");
		this.de.setString("pdid",pdid);
		vds = this.de.query();

		for (int i = 0; i < vds.rowCount();i++) {
			if (typename.equals("")) {
				if (i == vds.rowCount()-1) {
					typename =typename+vds.getString(i, "typename");
				}else {
					typename =typename+vds.getString(i, "typename")+"、";
				}
			}else {
				if (i == vds.rowCount()-1) {
					typename =typename + vds.getString(i, "typename");
				}else {
					typename =typename + vds.getString(i, "typename")+"、";
				}
			}
		}
		DataStore orgtypeds = DataStore.getInstance();
		orgtypeds.put(0, "typename", typename);
		
		DataObject result = DataObject.getInstance();
		result.put("orgvds", pdAdaptOrgVds);
		result.put("orgtypeds", orgtypeds);
		return result;
		
	}
	/**
	 * 方法详述:获取机构适用的流程
	 * @author fandq
	 * @date 创建时间 2016年11月18日
	 * @since V1.0
	 */
	private DataStore getAllpdAboutOrg(String orgno,String querylabel) throws AppException, BusinessException{
  		DE de = DE.getInstance();
		DataStore vds = DataStore.getInstance();
		
		if (orgno == null || orgno.equals("")) {
			this.bizException("传入的机构编号为空。");
		}
		
		if (querylabel == null || "".equals(querylabel)) {
			querylabel = "%";
		} else {
			querylabel = "%" + querylabel + "%";
		}
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		boolean isOrgYwjg = OdssuUtil.isYwjbjgbycfsno(orgno);
		de.clearSql();
  		de.addSql("select distinct a.pdid, a.pdlabel ");
  		de.addSql("  from bpzone.process_define    a, ");
  		de.addSql("       bpzone.dproletype        b, ");
  		de.addSql("       odssu.ir_org_role_type   c, ");
		if (isOrgYwjg) {
  			de.addSql("       odssu.org_business_scope i, ");
		}
  		de.addSql("       odssu.custome_footstone  d ");
  		de.addSql(" where a.pdid = b.pdid ");
  		de.addSql("   and b.roletypeid = c.roletypeno ");
  		de.addSql("   and c.orgtypeno = d.orgtype ");
  		de.addSql("   and d.orgno = :orgno ");
  		de.addSql("   and a.standardflag = '0' ");
		if (isOrgYwjg) {
  			de.addSql("   and d.orgno = i.orgno ");
		}
  		de.addSql("   and not exists (select 1 ");
  		de.addSql("          from bpzone.pd_customed e ");
  		de.addSql("         where a.pdid = e.standard_pdid ");
  		de.addSql("           and d.orgno = e.orgno) ");
		if (isOrgYwjg) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from bpzone.process_businesstype h ");
  			de.addSql("         where a.pdaid = h.pdaid ");
  			de.addSql("           and i.scopeno = h.ywlxid) ");
		}
  		de.addSql("   and (a.pdid like :querylabel or a.pdaid like :querylabel or a.pdlabel like :querylabel or ");
  		de.addSql("       a.pdalias like :querylabel) ");
  		de.addSql("       and exists (select * ");
  		de.addSql("          from odssu.orginfor       org, ");
  		de.addSql("               odssu.ir_org_closure iro, ");
  		de.addSql("               odssu.ir_dbid_org    irdo ");
  		de.addSql("         where org.orgno = iro.orgno ");
  		de.addSql("           and org.cfsno = d.orgno ");
  		de.addSql("           and iro.belongorgno = irdo.orgno ");
  		de.addSql("           and irdo.dbid = :dbid )");
  		de.addSql("union ");
  		de.addSql("select distinct m.pdid, m.pdlabel ");
  		de.addSql("  from bpzone.process_define    m, ");
  		de.addSql("       bpzone.dproletype        n, ");
  		de.addSql("       odssu.ir_org_role_type   p, ");
		if (isOrgYwjg) {
  			de.addSql("       odssu.org_business_scope q, ");
		}
  		de.addSql("       odssu.custome_footstone  r ");
  		de.addSql(" where m.pdid = n.pdid ");
  		de.addSql("   and n.roletypeid = p.roletypeno ");
  		de.addSql("   and p.orgtypeno = r.orgtype ");
  		de.addSql("   and r.orgno = :orgno ");
  		de.addSql("   and m.standardflag = '1' ");
		if (isOrgYwjg) {
  			de.addSql("   and r.orgno = q.orgno ");
		}
  		de.addSql("   and not exists (select 1 ");
  		de.addSql("          from bpzone.pd_customed s ");
  		de.addSql("         where m.pdid = s.standard_pdid ");
  		de.addSql("           and r.orgno = s.orgno) ");
		if (isOrgYwjg) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from bpzone.process_businesstype t ");
  			de.addSql("         where m.pdaid = t.pdaid ");
  			de.addSql("           and q.scopeno = t.ywlxid) ");
		}
  		de.addSql("   and (m.pdid like :querylabel or m.pdaid like :querylabel or m.pdlabel like :querylabel or ");
  		de.addSql("       m.pdalias like :querylabel) ");
  		de.addSql(" and exists (select 1 ");
  		de.addSql("          from bpzone.pd_dbid db ");
  		de.addSql("         where m.pdid = db.pdid ");
  		de.addSql("           and db.dbid = :dbid )");
  		de.addSql("       and exists (select 1 ");
  		de.addSql("          from odssu.orginfor       orga, ");
  		de.addSql("               odssu.ir_org_closure iroa, ");
  		de.addSql("               odssu.ir_dbid_org    irdoa ");
  		de.addSql("         where orga.orgno = iroa.orgno ");
  		de.addSql("           and orga.cfsno = r.orgno ");
  		de.addSql("           and iroa.belongorgno = irdoa.orgno ");
  		de.addSql("           and irdoa.dbid = :dbid )");
  		de.addSql("union ");
  		de.addSql("select distinct f.pdid, f.pdlabel ");
  		de.addSql("  from bpzone.process_define f ");
  		de.addSql(" where f.standardflag = '2' ");
  		de.addSql("   and exists (select 1 ");
  		de.addSql("          from bpzone.pd_customed g ");
  		de.addSql("         where f.pdid = g.customed_pdid ");
  		de.addSql("           and g.dbid = :dbid");
  		de.addSql("           and g.orgno = :orgno) ");
  		de.addSql("   and (f.pdid like :querylabel or f.pdaid like :querylabel or f.pdlabel like :querylabel or ");
  		de.addSql("       f.pdalias like :querylabel) ");
		de.setString("orgno",orgno);
		de.setString("querylabel",querylabel);
		de.setString("dbid",dbid);
		vds = de.query();
		
		return vds;
		
	}
	/**
	 * 方法简介 ：加载流程任务对应的 面板应该展示什么 
	 *@author 郑海杰   2016年4月14日
	 * @throws AppException 
	 */
	public DataObject loadPDDPDutyPanel(DataObject para) throws AppException{
  		de.clearSql();
		DataObject result = DataObject.getInstance();
		boolean isDpAdaptOrg = false;
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String orgno = para.getString("orgno");
		
		de.clearSql();
  		de.addSql(" select a.wso_appid ");
  		de.addSql("   from bpzone.process_define_in_activiti a ");
  		de.addSql("  where a.pdid = :pdid ");
		this.de.setString("pdid",pdid);
		DataStore appvds = this.de.query();
		if (appvds.rowCount() == 0) {
			throw new AppException("获取不到流程对应的appid");
		}
		String appid = appvds.getString(0, "wso_appid");
		if (appid.equals("ODSSU")) {
			isDpAdaptOrg = true;
			result.put("isdpadaptorg", isDpAdaptOrg);
			return result;
		}
		
		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from bpzone.dproletype a, ");
  		de.addSql("        odssu.ir_org_role_type b, ");
  		de.addSql("        odssu.custome_footstone c ");
  		de.addSql("  where a.pdid = :pdid ");
  		de.addSql("    and a.dptdid = :dptdid ");
  		de.addSql("    and a.roletypeid = b.roletypeno ");
  		de.addSql("    and c.orgno = :orgno ");
  		de.addSql("    and c.orgtype = b.orgtypeno ");
		this.de.setString("pdid", pdid);
		this.de.setString("dptdid", dptdid);
		this.de.setString("orgno", orgno);
		DataStore isDpAdaptOrgVds = this.de.query();
		if(isDpAdaptOrgVds != null && isDpAdaptOrgVds.size() > 0){
			isDpAdaptOrg = true;
		}else{
			String notAdaptReason = getNotAdaptReason(orgno, pdid, dptdid);
			result.put("notadaptreason", notAdaptReason);
		}
		result.put("isdpadaptorg", isDpAdaptOrg);
		return result;
	}
	private String getNotAdaptReason(String orgno, String pdid, String dptdid) throws AppException{
  		de.clearSql();
  		de.addSql(" select a.orgname,a.orgtype ");
  		de.addSql("   from odssu.custome_footstone a ");
  		de.addSql("  where a.orgno = :orgno ");
		this.de.setString("orgno", orgno);
		DataStore orgInforVds = this.de.query();
		String orgname = "";
		if(orgInforVds != null && orgInforVds.size() > 0){
			orgname = orgInforVds.getString(0, "orgname");
		}
		StringBuffer reasonBF = new StringBuffer();
		String dpLabel = OdssuUtil.getDpLabelByDptdid(pdid, dptdid);
		reasonBF.append("任务【" + dpLabel + "】在【" + orgname +"】下，无法办理。");
		
		DataStore roletypeVds = getDpRoletypeVds(pdid, dptdid);
		String roletypeSql = PubUtil.VdsToSqlString(roletypeVds, "roletypeid");
		if(roletypeSql.equals("")){
			roletypeSql="('')";
		}
		de.clearSql();
  		de.addSql(" select b.typeno ");
  		de.addSql("   from odssu.ir_org_role_type a, ");
  		de.addSql("        odssu.org_type b ");
  		de.addSql("  where a.roletypeno in " + roletypeSql);
  		de.addSql("    and a.orgtypeno = b.typeno ");
  		de.addSql("  order by b.sn ");
		DataStore adaptOrgTypeVds = this.de.query();
		String adaptOrgType = "";
		if(adaptOrgTypeVds == null || adaptOrgTypeVds.size() == 0){
			return reasonBF.toString();
		}else{
			adaptOrgType = adaptOrgTypeVds.getString(0, "typeno");
		}
		
		String rsjno = getBelongRsxtnoByCFSNO(orgno);
		String rsjnoinsql = rsjno +"%";
		if(roletypeVds.find("roletypeid == " + OdssuContants.ROLETYPE_JBJYWJBL) != -1 
				|| roletypeVds.find("roletypeid == " + OdssuContants.ROLETYPE_QDSYWGLL) != -1){
			de.clearSql();
  			de.addSql(" select a.orgname ");
  			de.addSql("   from odssu.custome_footstone a, ");
  			de.addSql("        odssu.org_business_scope b, ");
  			de.addSql("        bpzone.process_businesstype c, ");
  			de.addSql("        bpzone.process_define d ");
  			de.addSql("  where a.orgno like :rsjnoinsql ");
  			de.addSql("    and a.orgtype = :adaptorgtype ");
  			de.addSql("    and a.orgno = b.orgno ");
  			de.addSql("    and d.pdid = :pdid ");
  			de.addSql("    and d.pdaid = c.pdaid ");
  			de.addSql("    and c.ywlxid = b.scopeno ");
  			this.de.setString("rsjnoinsql", rsjnoinsql);
			this.de.setString("adaptorgtype", adaptOrgType);
			this.de.setString("pdid", pdid);
			DataStore orgNameVds = this.de.query();
			String orgNameStr = changeOrgNameVdsToStr(orgNameVds);
			if(!"".equals(orgNameStr)){
				reasonBF.append("请在"+orgNameStr+"等机构下，查询有权岗位信息。");
			}
			
		}else{
			de.clearSql();
  			de.addSql(" select orgname ");
  			de.addSql("   from odssu.custome_footstone a ");
  			de.addSql("  where a.orgno like :rsjnoinsql");
  			de.addSql("    and a.orgtype = :adaptOrgType ");
  			this.de.setString("rsjnoinsql", rsjnoinsql);
			this.de.setString("adaptOrgType", adaptOrgType);
			DataStore orgNameVds = this.de.query();
			String orgNameStr = changeOrgNameVdsToStr(orgNameVds);
			if(!"".equals(orgNameStr)){
				reasonBF.append("请在"+orgNameStr+"等机构下，查询有权岗位信息。");
			}
		}
		return reasonBF.toString();
	}
	private String changeOrgNameVdsToStr(DataStore orgNameVds) throws AppException{
		StringBuffer orgNameBF = new StringBuffer();
		if(orgNameVds.size()==0){
			orgNameBF.append("");
		}else if(orgNameVds.size() == 1){
			orgNameBF.append("【" + orgNameVds.getString(0, "orgname")+"】");
		}else{
			for(int i = 0; i < orgNameVds.size(); i++){
				orgNameBF.append("【" + orgNameVds.getString(i, "orgname") + "】");
				if(i < orgNameVds.size()-1){
					orgNameBF.append(",");
				}
			}
		}
		return orgNameBF.toString();
	}
	private String getBelongRsxtnoByCFSNO(String orgno) throws AppException{
		if(orgno == null || "".equals(orgno)){
			throw new AppException("传入的orgno为空！");
		}
		int indexOfDot = orgno.indexOf(".");
		if(indexOfDot == -1){
	  		de.clearSql();
  			de.addSql(" select b.orgno ");
  			de.addSql("   from odssu.ir_org_closure a, ");
  			de.addSql("        odssu.orginfor b ");
  			de.addSql("  where a.orgno = :orgno ");
  			de.addSql("    and a.belongorgno = b.orgno ");
  			de.addSql("    and b.orgtype in (:srst,:dsrsxt) ");
			this.de.setString("orgno", orgno);
			this.de.setString("srst", OdssuContants.ORGTYPE_SRST);
			this.de.setString("dsrsxt", OdssuContants.ORGTYPE_DSRSXT);
			DataStore orgVds = this.de.query();
			if(orgVds == null || orgVds.rowCount() ==0){
				throw new AppException("未查询到机构编号为【"+orgno+"】的上级人社系统信息。");
			}
			return orgVds.getString(0, "orgno");
		}else{
			return orgno.substring(0, indexOfDot);
		}
	}
	/**
	 * 方法简介 ：获取流程任务对应 角色类别 
	 *@author 郑海杰   2016年4月14日
	 */
	private DataStore getDpRoletypeVds(String pdid, String dptdid) throws AppException{
  		de.clearSql();
  		de.addSql(" select a.roletypeid ");
  		de.addSql("   from bpzone.dproletype a");
  		de.addSql("  where a.pdid = :pdid ");
  		de.addSql("    and a.dptdid = :dptdid ");
  		de.addSql("    and a.roletypeid <> :roletypeid ");
		this.de.setString("pdid", pdid);
		this.de.setString("dptdid", dptdid);
		this.de.setString("roletypeid", OdssuContants.ROLETYPE_GYL);
		DataStore roleTypeVds = this.de.query();
		return roleTypeVds;
	}
	/**
	 * 方法简介 ： 查询流程信息
	 *@author 郑海杰   2016年4月11日
	 */
	public DataObject queryYwlc(DataObject para) throws Exception {
		String label = para.getString("label", "");
		DataStore vdsPd = initDbidDSPDwithlabel(label);
		vdsPd.sort("pdid");
		DataObject result = DataObject.getInstance();
		result.put("pd", vdsPd);
		return result;
	}
	/**
	 * 方法简介 ： 初始化流程dspd
	 *@author 郑海杰   2016年4月12日
	 */
	private DataStore initDbidDSPDwithlabel(String label) throws AppException {
		//判断传入的label是否为空，并且进行相应的处理
		if (label == null || "".equals(label)) {
			label = "%";
		} else {
			label = "%" + label + "%";
		}
		
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		
		// 标准普适流程
		de.clearSql();
		de.addSql(" select a.pdid, a.pdlabel, a.folderno ");
  		de.addSql("   from bpzone.process_define a   ");
  		de.addSql("  where a.standardflag = '0'  ");
  		de.addSql("    and ( a.pdid like :label or a.pdlabel like :label) ");
		// 当前的dbid下存在机构未对该流程进行客户化
		de.addSql("    and exists (select 1                                                ");
  		de.addSql("                  from odssu.custome_footstone b                         ");
  		de.addSql("                 where exists (select 1 ");
  		de.addSql("                          from odssu.orginfor       org, ");
  		de.addSql("                               odssu.ir_org_closure iro, ");
  		de.addSql("                               odssu.ir_dbid_org    irdo ");
  		de.addSql("                         where org.orgno = iro.orgno ");
  		de.addSql("                           and org.cfsno = b.orgno ");
  		de.addSql("                           and iro.belongorgno = irdo.orgno ");
  		de.addSql("                           and irdo.dbid = :dbid)");
  		de.addSql("                 and b.orgno not in                                   ");
  		de.addSql("                                    (select c.orgno                   ");
  		de.addSql("                                       from bpzone.pd_customed c, bpzone.process_define m   ");
  		de.addSql("                                      where m.pdid = c.standard_pdid )) ");
  		de.addSql(" union all                                                              ");
		// 标准专用流程
		de.addSql(" select d.pdid, d.pdlabel, d.folderno ");
  		de.addSql("   from bpzone.process_define d   ");
  		de.addSql("  where d.standardflag = '1' ");
  		de.addSql(" and ( d.pdid like :label or d.pdlabel like :label ) ");
		// 存在机构未对该流程客户化
		de.addSql("    and exists (select 1                                                ");
  		de.addSql("                  from odssu.custome_footstone g                         ");
  		de.addSql("                 where g.orgno not in                                   ");
  		de.addSql("                                    (select h.orgno                       ");
  		de.addSql("                                       from bpzone.pd_customed h,bpzone.process_define n    ");
  		de.addSql("                                      where n.pdid = h.standard_pdid )) ");
		// 标准专用的dbid等于当前系统的dbid
		de.addSql("    and exists (select 1 from bpzone.pd_dbid pdd                          ");
  		de.addSql("       where pdd.dbid = :dbid and pdd.pdid = d.pdid)");
  		de.addSql(" union  all                                                             ");
		// 客户化流程
		de.addSql("select i.pdid, i.pdlabel, i.folderno ");
  		de.addSql("  from bpzone.process_define i ");
  		de.addSql(" where i.standardflag = '2' ");
  		de.addSql("   and (i.pdid like :label or i.pdlabel like :label) ");
  		de.addSql("   and exists ");
  		de.addSql(" (select 1 ");
  		de.addSql("          from bpzone.pd_customed j ");
  		de.addSql("         where i.pdid = j.customed_pdid ");
  		de.addSql("           and exists ");
  		de.addSql("         (select 1 ");
  		de.addSql("                  from odssu.custome_footstone k ");
  		de.addSql("                 where exists (select 1 ");
  		de.addSql("                          from odssu.orginfor       orga, ");
  		de.addSql("                               odssu.ir_org_closure iroa, ");
  		de.addSql("                               odssu.ir_dbid_org    irdoa ");
  		de.addSql("                         where orga.orgno = iroa.orgno ");
  		de.addSql("                           and orga.cfsno = k.orgno ");
  		de.addSql("                           and iroa.belongorgno = irdoa.orgno ");
  		de.addSql("                           and irdoa.dbid = :dbid)");
  		de.addSql("                   and j.orgno = k.orgno)) ");
  		de.addSql(" order by pdid asc  ");
		de.setString("label", label);
		de.setString("dbid", dbid);
		DataStore dspd = de.query();
		return dspd;
	}
	public DataObject dealPdIsAdaptOrg(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
  		de.clearSql();
  		de.addSql(" select standardflag ");
  		de.addSql("   from bpzone.process_define ");
  		de.addSql("  where pdid = :pdid ");
		this.de.setString("pdid", pdid);
		DataStore stanardFlagVds = this.de.query();
		String standardFlag = null;
		if(stanardFlagVds != null && stanardFlagVds.size() > 0){
			standardFlag = stanardFlagVds.getString(0, "standardflag");
		}else{
			throw new AppException("未查询到编号为【" + pdid +"】流程的标准化信息!");
		}
		DataStore pdAdaptOrgVds = null;
		if("0".equals(standardFlag) || "1".equals(standardFlag)){
			pdAdaptOrgVds = getStandardPdAdaptOrg(pdid);
		}else{
			pdAdaptOrgVds = getCustomedPdAdaptOrg(pdid);
		}
		DataObject vdo = queryUserAllEntitledOrg(DataObject.getInstance());
		DataStore userEntitledOrgVds = vdo.getDataStore("orgvds");
		dealAdaptFlagInUserEntitiedOrgVds(userEntitledOrgVds, pdAdaptOrgVds);
		MultiSortUtil.multiSortDS(userEntitledOrgVds, "isadapt:desc,sn:asc,orgno:asc");
		DataObject result = DataObject.getInstance();
		result.put("orgvds", userEntitledOrgVds);
		return result;
	}
	/**
	 * 方法简介 ：处理流程适用机构 对号 
	 *@author 郑海杰   2016年4月12日
	 */
	private void dealAdaptFlagInUserEntitiedOrgVds(DataStore userEntitledOrgVds, DataStore pdAdaptOrgVds) throws AppException{
		if(userEntitledOrgVds.size() == 0 || pdAdaptOrgVds.size() == 0){
			return;
		}
		for(int i = 0; i < userEntitledOrgVds.size(); i++){
			String orgno = userEntitledOrgVds.getString(i, "orgno");
			int findIndex = pdAdaptOrgVds.find("orgno == " + orgno);
			if(findIndex != -1){
				userEntitledOrgVds.put(i, "isadapt", "√");
				int sn = pdAdaptOrgVds.getInt(findIndex, "sn");
				userEntitledOrgVds.put(i, "sn", sn);
			}else{
				userEntitledOrgVds.put(i, "isadapt", "");
			}
		}
	}
	/**
	 * 方法简介 ：获取标准普适流程 可能的机构 
	 *@author 郑海杰   2016年4月12日
	 * @throws AppException 
	 */
	private DataStore getStandardPdAdaptOrg(String pdid) throws AppException{
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		//取出 除 局本级业务经办类之外的 其他角色类型可能的机构
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql(" select '√' isadapt,c.orgno,c.orgname,d.sn,' ' comments ");
  		de.addSql("   from bpzone.dproletype a, ");
  		de.addSql("        odssu.ir_org_role_type b, ");
  		de.addSql("        odssu.custome_footstone c, ");
  		de.addSql("        odssu.role_type e, ");
  		de.addSql("        odssu.org_type d ");
  		de.addSql("  where a.pdid = :pdid ");
  		de.addSql("    and a.roletypeid = b.roletypeno ");
  		de.addSql("    and b.orgtypeno = c.orgtype ");
  		de.addSql("    and c.orgtype = d.typeno ");
  		de.addSql("    and a.roletypeid = e.typeno ");
  		de.addSql("    and e.jsgn = :jsgn1 ");
  		de.addSql("    and not exists(select 1 ");
  		de.addSql("                     from bpzone.pd_customed ea ");
  		de.addSql("                    where ea.standard_pdid = a.pdid ");
  		de.addSql("                      and ea.dbid = :dbid ");
  		de.addSql("                      and ea.orgno = c.orgno) ");
  		de.addSql("                 and exists (select 1 ");
  		de.addSql("                          from odssu.orginfor       org, ");
  		de.addSql("                               odssu.ir_org_closure iro, ");
  		de.addSql("                               odssu.ir_dbid_org    irdo ");
  		de.addSql("                         where org.orgno = iro.orgno ");
  		de.addSql("                           and org.cfsno = c.orgno ");
  		de.addSql("                           and iro.belongorgno = irdo.orgno ");
  		de.addSql("                           and irdo.dbid = :dbid) ");
  		de.addSql("   union ");
  		de.addSql(" select '√' isadapt,cb.orgno,cb.orgname,fb.sn,' ' comments ");
  		de.addSql("   from bpzone.dproletype ab, ");
  		de.addSql("        odssu.ir_org_role_type bb, ");
  		de.addSql("        odssu.custome_footstone cb, ");
  		de.addSql("        odssu.org_business_scope db, ");
  		de.addSql("        odssu.role_type eb, ");
  		de.addSql("        odssu.org_type fb ");
  		de.addSql("  where ab.pdid = :pdid ");
  		de.addSql("    and ab.roletypeid = bb.roletypeno ");
  		de.addSql("    and bb.orgtypeno = cb.orgtype ");
  		de.addSql("    and cb.orgtype = fb.typeno ");
  		de.addSql("    and ab.roletypeid = eb.typeno ");
  		de.addSql("    and eb.jsgn = :jsgn2 ");
  		de.addSql("    and cb.orgno = db.orgno ");
  		de.addSql("    and exists(select 1 ");
  		de.addSql("                 from bpzone.process_businesstype ec, ");
  		de.addSql("                      bpzone.process_define gc ");
  		de.addSql("                where ab.pdid = gc.pdid ");
  		de.addSql("                  and gc.pdaid = ec.pdaid ");
  		de.addSql("                  and ec.ywlxid = db.scopeno )");
  		de.addSql("    and not exists(select 1 ");
  		de.addSql("                     from bpzone.pd_customed hd ");
  		de.addSql("                    where hd.standard_pdid = ab.pdid ");
  		de.addSql("                      and hd.dbid = :dbid ");
  		de.addSql("                      and hd.orgno = cb.orgno) ");
  		de.addSql("                 and exists (select 1 ");
  		de.addSql("                          from odssu.orginfor       orga, ");
  		de.addSql("                               odssu.ir_org_closure iroa, ");
  		de.addSql("                               odssu.ir_dbid_org    irdoa ");
  		de.addSql("                         where orga.orgno = iroa.orgno ");
  		de.addSql("                           and orga.cfsno = cb.orgno ");
  		de.addSql("                           and iroa.belongorgno = irdoa.orgno ");
  		de.addSql("                           and irdoa.dbid = :dbid) ");
		de.setString("pdid",pdid);
		de.setString("jsgn1",OdssuContants.JSGN_INNERDUTY);
		de.setString("jsgn2",OdssuContants.JSGN_OUTERDUTY);
		de.setString("dbid",dbid);
		DataStore stardardPervasiveAdaptOrgVdss = de.query();
		return stardardPervasiveAdaptOrgVdss;
	}
	/**
	 * 方法简介 ：获取客户化普适流程 可能的机构 
	 *@author 郑海杰   2016年4月12日
	 * @throws AppException 
	 */
	private DataStore getCustomedPdAdaptOrg(String pdid) throws AppException{
		
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
  		de.clearSql();
  		de.addSql(" select '√' isadapt,b.orgno,b.orgname, c.sn,null comments ");
  		de.addSql("   from bpzone.pd_customed a, ");
  		de.addSql("        odssu.custome_footstone b, ");
  		de.addSql("        odssu.org_type c ");
  		de.addSql("  where a.customed_pdid = :pdid ");
  		de.addSql("    and a.dbid = :dbid ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and b.orgtype = c.typeno ");
		this.de.setString("pdid", pdid);
		this.de.setString("dbid", dbid);
		DataStore customedPdAdaptOrg = this.de.query();
		return customedPdAdaptOrg;
	}
	/**
	 * 方法简介 ： 查询用户 有权查看的机构 
	 *@author 郑海杰   2016年4月11日
	 * 查询规则 1 对于人社系统上的业务职能管理人 和 顶级机构上的系统管理员来说，直接返回所有机构
	 * @throws AppException 
	 */
	public DataObject queryUserAllEntitledOrg(DataObject para) throws AppException{
		String orgname =  para.getString("orgname","");
		orgname = "%"+orgname.toUpperCase()+"%";
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		de.clearSql();
  		de.addSql("select a.orgno, a.orgname, b.sn ");
  		de.addSql("   from odssu.custome_footstone a, odssu.org_type b ");
  		de.addSql("  where a.orgtype <> :orgroot");
  		de.addSql("    and a.orgtype = b.typeno ");
  		de.addSql("    and exists ");
  		de.addSql("  (select 1 ");
  		de.addSql("           from odssu.orginfor       org, ");
  		de.addSql("                odssu.ir_org_closure iro, ");
  		de.addSql("                odssu.ir_dbid_org    irdo ");
  		de.addSql("          where org.orgno = iro.orgno ");
  		de.addSql("            and org.cfsno = a.orgno ");
  		de.addSql("            and iro.belongorgno = irdo.orgno ");
  		de.addSql("            and irdo.dbid = :dbid )");
  		de.addSql("    and (a.orgno like :orgname or a.orgname like :orgname or exists ");
  		de.addSql("         (select 1 ");
  		de.addSql("            from odssu.orginfor orga ");
  		de.addSql("           where a.orgno = orga.cfsno ");
  		de.addSql("             and (orga.orgno like :orgname or orga.orgname like :orgname or ");
  		de.addSql("                 orga.displayname like :orgname or orga.fullname like :orgname or ");
  		de.addSql("                 orga.orgnamepy like :orgname or orga.displaynamepy like :orgname or ");
  		de.addSql("                 orga.fullnamepy like :orgname))) ");
  		de.addSql("  order by sn, orgno ");
  		de.setString("dbid", dbid);
		this.de.setString("orgname",orgname);
		this.de.setString("orgroot",OdssuContants.ORGTYPE_ORGROOT);
		DataStore orgVds = this.de.query();
		
		DataObject result = DataObject.getInstance();
		MultiSortUtil.multiSortDS(orgVds, "sn:asc,orgno:asc");
		result.put("orgvds", orgVds);
		return result;
	}
	/**
	 * 方法简介 ： 查询用户 有权查看的机构 省直客户化
	 *@author 郑海杰   2016年4月21日
	 * 查询规则 1 对于人社系统上的业务职能管理人 和 顶级机构上的系统管理员来说，直接返回所有机构
	 * @throws AppException 
	 */
	public DataObject queryUserAllEntitledOrg_379900(DataObject para) throws AppException{
		String userid = this.getUser().getUserid();
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b, ");
  		de.addSql("        odssu.org_type c ");
  		de.addSql("  where a.empno = :userid ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and a.roleno = :roleno");
  		de.addSql("    and b.orgtype = c.typeno ");
  		de.addSql("    and c.typeno in (:srsxt,:dsrsxt");
		this.de.setString("userid", userid);
		this.de.setString("roleno", OdssuContants.ROLE_ODS_DUTY_DINFINER);
		this.de.setString("srsxt", OdssuContants.ORGTYPE_SRSXT);
		this.de.setString("dsrsxt", OdssuContants.ORGTYPE_DSRSXT);
		DataStore dutyDfinerVds = this.de.query();
		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.ir_emp_org_all_role a ");
  		de.addSql("  where a.empno = :userid ");
  		de.addSql("    and a.orgno = :orgroot");
  		de.addSql("    and a.roleno = :roleno");
		this.de.setString("userid", userid);
		this.de.setString("orgroot", OdssuContants.ORGROOT);
		this.de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);
		DataStore sysAdminVds = this.de.query();
		DataStore resultVds =null;
		if(dutyDfinerVds.size() > 0 || sysAdminVds.size() > 0){
			resultVds = getAllCustomedFootStone();
		}else{
			resultVds = getUserEntitleCustomFootStone_379900();
		}
		
		DataObject result = DataObject.getInstance();
		MultiSortUtil.multiSortDS(resultVds, "sn:asc,orgno:asc");
		result.put("orgvds", resultVds);
		return result;
	}
	/**
	 * 方法简介 ：获取所有可能的客户化基石机构 ，系统管理员 和 人社系统上的 业务职能管理人
	 *@author 郑海杰   2016年4月12日
	 */
	private DataStore getAllCustomedFootStone() throws AppException{
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		de.clearSql();
  		de.addSql(" select a.orgno,a.orgname,b.sn ");
  		de.addSql("   from odssu.custome_footstone a, ");
  		de.addSql("        odssu.org_type b ");
  		de.addSql(" where a.orgtype not in(:orgroot,:srsxt,:dsrsxt,:dsrsj,:qxrsj,:dsejdw,:qxejdw,:stejdw,:srst)"); 
  		de.addSql("   and a.orgtype = b.typeno ");
  		de.addSql("                 and exists (select 1 ");
  		de.addSql("                          from odssu.orginfor       org, ");
  		de.addSql("                               odssu.ir_org_closure iro, ");
  		de.addSql("                               odssu.ir_dbid_org    irdo ");
  		de.addSql("                         where org.orgno = iro.orgno ");
  		de.addSql("                           and org.cfsno = a.orgno ");
  		de.addSql("                           and iro.belongorgno = irdo.orgno ");
  		de.addSql("                           and irdo.dbid = :dbid");
  		de.addSql("  order by sn,orgno ");
  		this.de.setString("orgroot", OdssuContants.ORGTYPE_ORGROOT);
  		this.de.setString("srsxt", OdssuContants.ORGTYPE_SRSXT);
  		this.de.setString("dsrsxt", OdssuContants.ORGTYPE_DSRSXT);
  		this.de.setString("dsrsj", OdssuContants.ORGTYPE_DSRSJ);
  		this.de.setString("qxrsj", OdssuContants.ORGTYPE_QXRSJ);
  		this.de.setString("dsejdw", OdssuContants.ORGTYPE_DSEJDW);
  		this.de.setString("qxejdw", OdssuContants.ORGTYPE_QXEJDW);
  		this.de.setString("stejdw", OdssuContants.ORGTYPE_STEJDW);
  		this.de.setString("srst", OdssuContants.ORGTYPE_SRST);
  		this.de.setString("dbid", dbid);
		DataStore orgVds = this.de.query();
		return orgVds;
	}
	/**
	 * 方法简介 ：获取普通用户 有权的 客户化基石机构
	 *   结果集机构 = 人员有管理权的机构所在人社局下或者 人隶属的人社局下的 所有客户化基石机构 
	 *@author 郑海杰   2016年4月12日
	 */
	private DataStore getUserEntitleCustomFootStone_379900() throws AppException{
		String userid = this.getUser().getUserid();
  		de.clearSql();
  		de.addSql(" select c.orgno ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.ir_org_closure b, ");
  		de.addSql("        odssu.orginfor c ");
  		de.addSql("  where a.empno = :userid ");
  		de.addSql("    and a.roleno in(:role_ods_duty_dinfiner,:ole_ods_orgadmin,:role_ods_work_dispatch_)");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and b.belongorgno = c.orgno ");
  		de.addSql("    and c.orgno in " + OdssuUtil.getUserQuerableOrg(this.getUser().getUserid()));
  		de.addSql("    and c.orgtype in (:srst,:stejdw,:stejdwywjg)");
  		de.addSql("  union ");
  		de.addSql(" select ca.orgno ");
  		de.addSql("   from odssu.ir_emp_org aa, ");
  		de.addSql("        odssu.ir_org_closure ba, ");
  		de.addSql("        odssu.orginfor ca ");
  		de.addSql("  where aa.empno = :userid ");
  		de.addSql("    and aa.orgno = ba.orgno ");
  		de.addSql("    and ba.belongorgno = ca.orgno ");
  		de.addSql("    and ca.orgno in " + OdssuUtil.getUserQuerableOrg(this.getUser().getUserid()));
  		de.addSql("    and ca.orgtype in (:srst,:stejdw,:stejdwywjg");
		this.de.setString("userid", userid);
		this.de.setString("role_ods_duty_dinfiner", OdssuContants.ROLE_ODS_DUTY_DINFINER);
		this.de.setString("ole_ods_orgadmin", OdssuContants.ROLE_ODS_ORGADMIN);
		this.de.setString("role_ods_work_dispatch_", OdssuContants.ROLE_ODS_WORK_DISPATCH_);
		this.de.setString("srst", OdssuContants.ORGTYPE_SRST);
		this.de.setString("stejdw", OdssuContants.ORGTYPE_STEJDW);
		this.de.setString("stejdwywjg", OdssuContants.ORGTYPE_STEJDWYWJG);
		DataStore entilleRSJVds = this.de.query();
		DataStore entilteOrgVds = DataStore.getInstance();
		for(int i = 0; i < entilleRSJVds.size(); i++){
			String orgno = entilleRSJVds.getString(i, "orgno");
			DataStore orgVds = getCustomFootStoneByRsjno(orgno);
			entilteOrgVds.combineDatastore(orgVds);
		}
		return entilteOrgVds;
	}
	/**
	 * 方法简介 ：根据人社局编号获取 人社局下的客户化基石机构 
	 *@author 郑海杰   2016年4月12日
	 */
	private DataStore getCustomFootStoneByRsjno(String rsjno) throws AppException{
  		de.clearSql();
  		de.addSql(" select a.orgno,a.orgname,b.sn ");
  		de.addSql("   from odssu.custome_footstone a, ");
  		de.addSql("        odssu.org_type b ");
  		de.addSql("  where a.orgtype = b.typeno ");
  		de.addSql("    and a.orgno like :rsjno");
  		de.addSql("    and a.orgtype not in(:orgroot,:srsxt,:dsrsxt,:dsrsj,:qxrsj,:dsejdw,:qxejdw,:stejdw,:srst)");
		this.de.setString("rsjno", rsjno);
		this.de.setString("orgroot", OdssuContants.ORGTYPE_ORGROOT);
  		this.de.setString("srsxt", OdssuContants.ORGTYPE_SRSXT);
  		this.de.setString("dsrsxt", OdssuContants.ORGTYPE_DSRSXT);
  		this.de.setString("dsrsj", OdssuContants.ORGTYPE_DSRSJ);
  		this.de.setString("qxrsj", OdssuContants.ORGTYPE_QXRSJ);
  		this.de.setString("dsejdw", OdssuContants.ORGTYPE_DSEJDW);
  		this.de.setString("qxejdw", OdssuContants.ORGTYPE_QXEJDW);
  		this.de.setString("stejdw", OdssuContants.ORGTYPE_STEJDW);
  		this.de.setString("srst", OdssuContants.ORGTYPE_SRST);
		DataStore orgVds = this.de.query();
		return orgVds;
	}
	/**
	 * 方法简介 ：获取 流程查询OIP的Label 
	 *@author 郑海杰   2016年4月12日
	 */
	public DataObject getPdInOrgOipLabel(DataObject para) throws Exception{
		String orgno = para.getString("orgno","");
		String pdid = para.getString("pdid","");
  		de.clearSql();
		DataObject result = DataObject.getInstance();
		
		String pdlabel = OdssuUtil.getPdLabelByPdid(pdid);
		
		if (orgno != null && !orgno.equals("")) {
			de.clearSql();
  			de.addSql(" select orgname from odssu.custome_footstone where orgno = :orgno ");
			this.de.setString("orgno", orgno);
			DataStore orgNameVds = this.de.query();
			String orgname = "";
			
			if(orgNameVds != null && orgNameVds.size() > 0){
				orgname = orgNameVds.getString(0, "orgname");
			}
			
			result.put("oiplabel" , orgname + "\n" + pdlabel);
		}else {
			result.put("oiplabel" , pdid + "\n" + pdlabel);
		}
		
		return result;
	}
	/**
	 * 方法简介 ：转向 岗位流程图 页面页面 
	 *@author 郑海杰   2016年4月12日
	 */
	public DataObject fwPagePdDPMNInforJsp(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
		String pdlabel = para.getString("pdlabel");
		DataStore pdinfords = DataStore.getInstance();
		pdinfords.put(0, "pdid", pdid);
		pdinfords.put(0, "pdlabel", pdlabel);
		DataObject result = DataObject.getInstance();
		result.put("pdinfords", pdinfords);
		return result;
	}
	/**
	 * 方法简介 ：查询流程业务范畴信息 
	 *@author 郑海杰   2016年4月11日
	 */
	public DataObject queryPdYwfc(DataObject para) throws AppException, BusinessException {
		String pdid = para.getString("pdid");
		if (pdid == null || "".equals(pdid.trim())) {
			throw new AppException("传入的pdid为空");
		}

		DataStore dsbusscope = DataStore.getInstance();
		StringBuffer pubstr = new StringBuffer();
		// 养老收
		DataObject ylvdo = DataObject.getInstance();
		ylvdo.put("ywmc", "养老收");
		ylvdo.put("busscope1", "101");
		ylvdo.put("selected1", "1");
		ylvdo.put("busscope2", "201");
		ylvdo.put("selected2", "1");
		ylvdo.put("busscope3", "701");
		ylvdo.put("selected3", "1");
		dsbusscope.addRow(ylvdo);
		pubstr.append("('101','201','701',");

		// 医疗收
		DataObject medigetvdo = DataObject.getInstance();
		medigetvdo.put("ywmc", "医疗收");
		medigetvdo.put("busscope1", "102");
		medigetvdo.put("selected1", "1");
		medigetvdo.put("busscope2", "202");
		medigetvdo.put("selected2", "1");
		medigetvdo.put("busscope3", "801");
		medigetvdo.put("selected3", "1");
		dsbusscope.addRow(medigetvdo);
		pubstr.append("'102','202','801',");
		// 工伤收
		DataObject gsgetvdo = DataObject.getInstance();
		gsgetvdo.put("ywmc", "工伤收");
		gsgetvdo.put("busscope1", "103");
		gsgetvdo.put("selected1", "1");
		gsgetvdo.put("busscope2", "203");
		gsgetvdo.put("selected2", "1");
		gsgetvdo.put("busscope3", "");
		gsgetvdo.put("selected3", "0");
		dsbusscope.addRow(gsgetvdo);
		pubstr.append("'103','203',");
		// 生育收
		DataObject bbgetvdo = DataObject.getInstance();
		bbgetvdo.put("ywmc", "生育收");
		bbgetvdo.put("busscope1", "104");
		bbgetvdo.put("selected1", "1");
		bbgetvdo.put("busscope2", "204");
		bbgetvdo.put("selected2", "1");
		bbgetvdo.put("busscope3", "");
		bbgetvdo.put("selected3", "0");
		dsbusscope.addRow(bbgetvdo);
		pubstr.append("'104','204',");
		// 失业收
		DataObject ljgetvdo = DataObject.getInstance();
		ljgetvdo.put("ywmc", "失业收");
		ljgetvdo.put("busscope1", "105");
		ljgetvdo.put("selected1", "1");
		ljgetvdo.put("busscope2", "205");
		ljgetvdo.put("selected2", "1");
		ljgetvdo.put("busscope3", "");
		ljgetvdo.put("selected3", "0");
		dsbusscope.addRow(ljgetvdo);
		pubstr.append("'105','205',");

		// 养老支
		DataObject ylsetvdo = DataObject.getInstance();
		ylsetvdo.put("ywmc", "养老支");
		ylsetvdo.put("busscope1", "301");
		ylsetvdo.put("selected1", "1");
		ylsetvdo.put("busscope2", "401");
		ylsetvdo.put("selected2", "1");
		ylsetvdo.put("busscope3", "702");
		ylsetvdo.put("selected3", "1");
		dsbusscope.addRow(ylsetvdo);
		pubstr.append("'301','401','702',");
		// 医疗支
		DataObject medisetvdo = DataObject.getInstance();
		medisetvdo.put("ywmc", "医疗支");
		medisetvdo.put("busscope1", "302");
		medisetvdo.put("selected1", "1");
		medisetvdo.put("busscope2", "402");
		medisetvdo.put("selected2", "1");
		medisetvdo.put("busscope3", "802");
		medisetvdo.put("selected3", "1");
		dsbusscope.addRow(medisetvdo);
		pubstr.append("'302','402','802',");
		// 工伤支
		DataObject gssetvdo = DataObject.getInstance();
		gssetvdo.put("ywmc", "工伤支");
		gssetvdo.put("busscope1", "303");
		gssetvdo.put("selected1", "1");
		gssetvdo.put("busscope2", "403");
		gssetvdo.put("selected2", "1");
		gssetvdo.put("busscope3", "");
		gssetvdo.put("selected3", "0");
		dsbusscope.addRow(gssetvdo);
		pubstr.append("'303','403',");
		// 生育支
		DataObject bbsetvdo = DataObject.getInstance();
		bbsetvdo.put("ywmc", "生育支");
		bbsetvdo.put("busscope1", "304");
		bbsetvdo.put("selected1", "1");
		bbsetvdo.put("busscope2", "404");
		bbsetvdo.put("selected2", "1");
		bbsetvdo.put("busscope3", "");
		bbsetvdo.put("selected3", "0");
		dsbusscope.addRow(bbsetvdo);
		pubstr.append("'304','404',");
		// 失业支
		DataObject sysetvdo = DataObject.getInstance();
		sysetvdo.put("ywmc", "失业支");
		sysetvdo.put("busscope1", "305");
		sysetvdo.put("selected1", "1");
		sysetvdo.put("busscope2", "405");
		sysetvdo.put("selected2", "1");
		sysetvdo.put("busscope3", "");
		sysetvdo.put("selected3", "0");
		dsbusscope.addRow(sysetvdo);
		pubstr.append("'305','405')");

		dsbusscope = this.dealYwfcData(dsbusscope, pdid);

		// 其他业务范畴
		DataStore dsotherscope = DataStore.getInstance();
		de.clearSql();
  		de.addSql(" select a.scopeno,a.scopename,'1' selected ");
  		de.addSql("   from odssu.business_scope a ");
  		de.addSql("  where a.scopeno not in " + pubstr.toString() + " ");
		dsotherscope = de.query();

		dsotherscope = this.dealOtherYwfcData(dsotherscope, pdid,pubstr);

		DataObject vdo = DataObject.getInstance();
		vdo.put("vdsywfc", dsbusscope);
		vdo.put("dsotherscope", dsotherscope);
		return vdo;

	}
	/**
	 * 方法简介：处理其他业务范畴
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	private DataStore dealOtherYwfcData(DataStore dsotherscope, String pdid,StringBuffer pubstr) throws AppException, BusinessException {
  		de.clearSql();
  		de.addSql(" select a.ywlxid scopeno                 ");
  		de.addSql("   from bpzone.process_businesstype a,   ");
  		de.addSql("        bpzone.process_define b          ");
  		de.addSql("   where a.pdaid = b.pdaid               ");
  		de.addSql("     and b.pdid = :pdid                     ");
  		de.addSql("  and  a.ywlxid not in " + pubstr.toString() + " ");
		de.setString("pdid", pdid);
		DataStore dsOrgScope = de.query();
		for (DataObject vdo : dsOrgScope) {
			String scopeno = vdo.getString("scopeno");
			int j = dsotherscope.find(" scopeno == " + scopeno);
			if (j >= 0) {
				dsotherscope.put(j, "selected", "2");
			}
		}
		return dsotherscope;
	}

	/**
	 * 方法简介：处理业务范畴
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	private DataStore dealYwfcData(DataStore dsscope, String pdid) throws AppException, BusinessException {
  		de.clearSql();
  		de.addSql(" select a.ywlxid scopeno                 ");
  		de.addSql("   from bpzone.process_businesstype a,   ");
  		de.addSql("        bpzone.process_define b          ");
  		de.addSql("   where a.pdaid = b.pdaid               ");
  		de.addSql("     and b.pdid = :pdid                    ");
		de.setString("pdid", pdid);
		DataStore dsOrgScope = de.query();
		for (DataObject vdo : dsOrgScope) {
			String scopeno = vdo.getString("scopeno");
			int j = dsscope.find(" busscope1 == " + scopeno);
			if (j >= 0) {
				dsscope.put(j, "selected1", "2");
			} else {
				j = dsscope.find("busscope2 == " + scopeno);
				if (j >= 0) {
					dsscope.put(j, "selected2", "2");
				} else {
					j = dsscope.find("busscope3 == " + scopeno);
					if (j >= 0) {
						dsscope.put(j, "selected3", "2");
					} else {
						// 暂时break掉 其他业务范畴类型
						continue;
						// this.bizException("该机构配置业务范畴出错");
					}
				}

			}
		}
		return dsscope;
	}
	
	/**
	 * 方法简介：加载流程任务页面dp数据
	 * 
	 * @author fandq
	 * @throws AppException
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPddp(DataObject para) throws AppException {
		// 获取变量
		String pdid = para.getString("pdid");
		DataStore dpds = DataStore.getInstance();// 保存全部dp
		DataStore dpads = DataStore.getInstance();// 保存隶属当前活动版本的流程的dp

		DataObject result = DataObject.getInstance();

		// 根据pdid获取流程下所有dp
		de.clearSql();
		de.addSql(" select distinct '1' lslc,dptdid,dptdlabel  ");
		de.addSql("   from bpzone.dutyposition_task   ");
		de.addSql("  where pdid = :pdid                   ");
		de.addSql("    and nvl(status,'0') <> '2' ");
		de.setString("pdid", pdid);
		dpds = de.query();
		
		// 获取当前活动的流程版本的dp
		de.clearSql();
  		de.addSql(" select distinct a.dptdid         ");
  		de.addSql("   from bpzone.task_point a,      ");
  		de.addSql("        bpzone.process_define b   ");
  		de.addSql("  where a.pdaid = b.pdaid         ");
  		de.addSql("    and b.pdid = :pdid                ");
		de.setString("pdid", pdid);
		dpads = de.query();

		// 将两个结果进行比较，获取“√”逻辑
		for (int i = 0; i < dpds.rowCount(); i++) {
			// 将dpds中也在dpads中的，lslc设置为2（即隶属活动流程）
			if (dpads.find("dptdid == " + dpds.getString(i, "dptdid")) >= 0) {
				dpds.put(i, "lslc", "2");
			}

		}
		result.put("dpds", dpds);
		return result;

	}
	/**
	 * 方法简介：加载流程任务页面grid数据
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject loadPddpDutyInfor(DataObject para) throws Exception {
		// 获取流程任务ID
		String dptdid = para.getString("dptdid");
		String pdid = para.getString("pdid");
		String orgno = para.getString("orgno");
		String orgtype = getOrgTypeInCFSByOrgno(orgno);
  		de.clearSql();
		DataStore roleds = DataStore.getInstance();
		if(OdssuUtil.isRealOrg(orgtype)){
			de.clearSql();
  			de.addSql("select distinct b.orgname,b.rolename,b.orgno,b.roleno ");
  			de.addSql("  from bpzone.dutyposition_task_role a, odssu.inner_duty b ");
  			de.addSql(" where a.roleid = b.roleno ");
  			de.addSql("   and a.pdid = :pdid ");
  			de.addSql("   and a.dptdid = :dptdid ");
  			de.addSql("   and b.orgno in (select orgno from odssu.orginfor where cfsno = :orgno) ");
			this.de.setString("pdid",pdid);
			this.de.setString("dptdid",dptdid);
			this.de.setString("orgno",orgno);
			roleds = this.de.query();
			
			for(int i = 0; i < roleds.size(); i++){
				String orgno_temp = roleds.getString(i, "orgno");
				String roleno_temp = roleds.getString(i, "roleno");
				de.clearSql();
  				de.addSql(" select b.empname,a.formalflag ");
  				de.addSql("   from odssu.emp_inner_duty a,");
  				de.addSql("        odssu.empinfor b ");
  				de.addSql("  where a.orgno = :orgno_temp ");
  				de.addSql("    and a.roleno = :roleno_temp ");
  				de.addSql("    and a.empno = b.empno ");
				this.de.setString("orgno_temp", orgno_temp);
				this.de.setString("roleno_temp", roleno_temp);
				DataStore dutyEmpVds = this.de.query();
				String dutyEmpNameStr = converDutyEmpVdsToDuytEmpStr(dutyEmpVds);
				roleds.put(i, "empnamestr", dutyEmpNameStr);
				// 查询隶属机构起效范围
				de.clearSql();
  				de.addSql("select b.orgtype ");
  				de.addSql("  from odssu.roleinfor a, odssu.orginfor b ");
  				de.addSql(" where a.deforgno = b.orgno ");
  				de.addSql("   and a.roleno = :roleno_temp ");
				this.de.setString("roleno_temp", roleno_temp);
				DataStore orgtypevds = this.de.query();
				String orgtype_temp = orgtypevds.getString(0, "orgtype");
				String qxfw = "0";
				if (OdssuUtil.yxdymb(orgtype_temp)){
					qxfw = "1";
				}
				roleds.put(i, "qxfw", qxfw);
			}
		}

		if(OdssuUtil.yxface(orgtype)){
			// 查询流程任务对应的岗位信息
			de.clearSql();
  			de.addSql(" select d.orgname,b.roleno,c.inorgno orgno, b.rolename,c.dutyno ");
  			de.addSql("   from bpzone.dutyposition_task_role a, ");
  			de.addSql("        odssu.roleinfor b, ");
  			de.addSql("        odssu.outer_duty c, ");
  			de.addSql("        odssu.orginfor d ");
  			de.addSql("  where a.dptdid = :dptdid ");
  			de.addSql("    and a.pdid = :pdid ");
  			de.addSql("    and a.roleid = b.roleno ");
  			de.addSql("    and b.roleno = c.roleno ");
  			de.addSql("    and c.faceorgno in (select orgno from odssu.orginfor where cfsno = :orgno) ");
  			de.addSql("    and c.inorgno = d.orgno ");
			this.de.setString("dptdid", dptdid);
			this.de.setString("pdid", pdid);
			this.de.setString("orgno", orgno);
			roleds = this.de.query();
			
			for(int i = 0; i < roleds.size(); i++){
				String dutyno = roleds.getString(i, "dutyno");
				de.clearSql();
  				de.addSql(" select b.empname,a.formalflag ");
  				de.addSql("   from odssu.emp_outer_duty a,");
  				de.addSql("        odssu.empinfor b ");
  				de.addSql("  where a.dutyno = :dutyno ");
  				de.addSql("    and a.empno = b.empno ");
				this.de.setString("dutyno", dutyno);
				DataStore dutyEmpVds = this.de.query();
				String dutyEmpNameStr = converDutyEmpVdsToDuytEmpStr(dutyEmpVds);
				roleds.put(i, "empnamestr", dutyEmpNameStr);
				// 查询隶属机构起效范围
				String roleno_temp = roleds.getString(i, "roleno");
				de.clearSql();
  				de.addSql("select b.orgtype ");
  				de.addSql("  from odssu.roleinfor a, odssu.orginfor b ");
  				de.addSql(" where a.deforgno = b.orgno ");
  				de.addSql("   and a.roleno = :roleno_temp ");
				this.de.setString("roleno_temp", roleno_temp);
				DataStore orgtypevds = this.de.query();
				String orgtype_temp = orgtypevds.getString(0, "orgtype");
				String qxfw = "0";
				if (OdssuUtil.yxdymb(orgtype_temp)) {
					qxfw = "1";
				}
				roleds.put(i, "qxfw", qxfw);
			}
		}
		
		DataObject result = DataObject.getInstance();
		result.put("ds", roleds);
		return result;
	}
	private String converDutyEmpVdsToDuytEmpStr(DataStore dutyEmpVds) throws AppException{
		if(dutyEmpVds == null || dutyEmpVds.size() == 0){
			return "";
		}
		StringBuffer sqlBF = new StringBuffer();
		for(int i = 0; i < dutyEmpVds.size(); i++){
			String empname = dutyEmpVds.getString(i, "empname");
			String formalflag = dutyEmpVds.getString(i, "formalflag");
			if("0".equals(formalflag)){
				sqlBF.append(empname + "【临时代岗】");
			}else{
				sqlBF.append(empname);
			}
			if(i < dutyEmpVds.size() - 1){
				sqlBF.append(",");
			}
		}
		return sqlBF.toString();
	}
	private String getOrgTypeInCFSByOrgno(String orgno) throws AppException{
  		de.clearSql();
  		de.addSql(" select orgtype ");
  		de.addSql("   from odssu.custome_footstone a ");
  		de.addSql("  where a.orgno = :orgno ");
		this.de.setString("orgno", orgno);
		DataStore orgTypeVds = this.de.query();
		String orgtype = "";
		if(orgTypeVds != null && orgTypeVds.size() > 0){
			orgtype = orgTypeVds.getString(0, "orgtype");
		}
		return orgtype;
	}
	public DataObject openViewDpmnByDptdidRES(DataObject para) throws AppException{
		DataStore vds = DataStore.getInstance();
		vds.put(0, "dptdid", para.getString("dptdid"));
		vds.put(0, "dptdlabel", para.getString("dptdlabel"));
		DataObject result = DataObject.getInstance();
		result.put("dpinfords", vds);
		return result;
	}
	/**
	 * 方法简介：加载流程流程图
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdBpmn(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdid");
		DataObject result = DataObject.getInstance();

		DataStore vds = DataStore.getInstance();
  		de.clearSql();
  		de.addSql(" select a.dpmnpng                            ");
  		de.addSql("   from bpzone.process_define_in_activiti a, ");
  		de.addSql("        bpzone.process_define b              ");
  		de.addSql("  where a.pdaid = b.pdaid                    ");
  		de.addSql("    and b.pdid = :pdid                           ");
		this.de.setString("pdid", pdid);

		vds = this.de.query();
		if(vds == null || vds.rowCount() ==0){
			throw new AppException("获取BPMN的png图像时出错！");
		}
		// Blob png_content = (Blob) vds.getObject(0, "dpmnpng");
		byte[] png_content = vds.getByteArray(0, "dpmnpng");
		// 获取blob对象的byte流
		result.put("imageStream", png_content);
		return result;
	}
	
	/**
	 * 方法简介 ： 加载带红圈的岗位任务流程图
	 *@author 郑海杰   2016年4月12日
	 */
	public DataObject queryPdBpmnByDptdid(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdaid");
		String dptdid = para.getString("dptdid");
		DataObject result = DataObject.getInstance();

		DataStore vds = DataStore.getInstance();
  		de.clearSql();
  		de.addSql(" select a.dpmnpng ");
  		de.addSql("   from bpzone.task_point a ");
  		de.addSql("  where a.pdaid = :pdid ");
  		de.addSql("    and a.dptdid = :dptdid ");
		this.de.setString("pdid", pdid);
		this.de.setString("dptdid", dptdid);
		vds = this.de.query();
		if(vds == null || vds.rowCount() ==0){
			throw new AppException("获取BPMN的png图像时出错！");
		}
		// Blob png_content = (Blob) vds.getObject(0, "dpmnpng");
		byte[] png_content = vds.getByteArray(0, "dpmnpng");

		// 获取blob对象的byte流
		result.put("imageStream", png_content);
		return result;
	}
	/**
	 * 方法简介：查询标准流程的客户话流程机构信息
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject loadCustomedOrgData(DataObject para) throws AppException {
		// 获取参数
		String customedpdid = para.getString("customedpdid");
		DataStore pdds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();
  		de.clearSql();

		// 根据标准流程ID和客户化流程ID获取其客户化流程以及客户化流程对应的机构信息
		de.clearSql();
  		de.addSql(" select  distinct a.orgno,b.orgname ");
  		de.addSql(" from bpzone.pd_customed a,   ");
  		de.addSql("      odssu.custome_footstone b   ");
  		de.addSql(" where a.orgno = b.orgno   ");
  		de.addSql(" and   a.customed_pdid = :customedpdid   ");
  		de.addSql(" order by a.orgno    ");
		this.de.setString("customedpdid", customedpdid);
		pdds = this.de.query();

		// 将返回结果保存到dataobject中
		result.put("customeorgds", pdds);

		return result;

	}
	/**
	 * 方法简介：加载流程类别
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdlb(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdid");
		DataStore flagds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();

		// 从数据库表中查出流程信息
		de.clearSql();
  		de.addSql(" select standardflag from bpzone.process_define");
  		de.addSql(" where pdid = :pdid ");
		de.setString("pdid", pdid);
		flagds = de.query();
		if(flagds == null || flagds.rowCount() ==0){
			throw new AppException("获取流程编号为【"+pdid+"】的流程信息时出错！");
		}
		String standardflag = flagds.getString(0, "standardflag");
		result.put("standardflag", standardflag);

		return result;
	}
	/**
	 * 方法简介：查询普适流程流程的客户化流程信息
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryStandardPdInfor(DataObject para) throws Exception {
		// 获取参数
		DataObject result = DataObject.getInstance();
		result.put("pdinfords", queryPdInfor(para).getDataStore("pdinfords"));
		return result;
	}
	public DataObject queryPdcusInfor(DataObject para) throws Exception {
		// 获取流程定义ID
		String pdid = para.getString("pdid");

		DataObject result = DataObject.getInstance();

		// 流程基本信息
		result.put("pdinfords", queryPdInfor(para).getDataStore("pdinfords"));
		// 流程适用的机构信息
		para.put("customedpdid", pdid);
		result.put("customeorgds", loadCustomedOrgData(para).getDataStore("customeorgds"));

		return result;
	}
	/**
	 * 方法简介：加载流程目录
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdInfor(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdid");
		DataStore pdinfords = DataStore.getInstance();
		DataObject result = DataObject.getInstance();

		// 从数据库表中查出流程信息
		de.clearSql();
  		de.addSql(" select b.pdid,b.pdlabel,b.pdalias,b.standardflag,null lbsy");
  		de.addSql(" from  bpzone.process_define b  ");
  		de.addSql(" where b.pdid = :pdid ");
		this.de.setString("pdid", pdid);
		pdinfords = this.de.query();
		if(pdinfords == null || pdinfords.rowCount() ==0){
			throw new AppException("获取流程编号为【"+pdid+"】的流程信息时出错！");
		}
		// 标准专用流程
		if (pdinfords.getString(0, "standardflag").equals("1")) {
			// 搜索DBID
			
			String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
			
			de.clearSql();
  			de.addSql(" select dbid from bpzone.pd_dbid where pdid = :pdid ");
			this.de.setString("pdid", pdid);
			DataStore dbidds = this.de.query();
			if (dbidds.find("dbid == " + dbid) != -1) {
				pdinfords.put(0, "lbsy", "0");
			} else {
				pdinfords.put(0, "lbsy", "1");
			}
		}
		// 客户化流程
		if (pdinfords.getString(0, "standardflag").equals("2")) {
			// 搜索流程对应的标准流程信息
			de.clearSql();
  			de.addSql(" select c.pdid,c.pdlabel,c.pdalias ");
  			de.addSql(" from bpzone.process_define a, ");
  			de.addSql("      bpzone.process_define c ");
  			de.addSql(" where a.pdid = :pdid ");
  			de.addSql(" and a.standard_pdid = c.pdid ");
			this.de.setString("pdid", pdid);
			DataStore standardpdds = this.de.query();
			if (standardpdds.rowCount() == 0) {
				throw new Exception("未找到该客户化流程的标准流程信息，请检查数据！");
			}
			pdinfords.put(0, "standardpd", standardpdds.getString(0, "pdalias") + "(" + standardpdds.getString(0, "pdid") + ")");
		}

		result.put("pdinfords", pdinfords);

		return result;
	}
	public DataObject loadDpRoleTypeInfor(DataObject para) throws AppException{
		// 获取变量
		String pdid = para.getString("pdid");
		DataStore dpds = DataStore.getInstance();// 保存全部dp
		DataStore dpads = DataStore.getInstance();// 保存隶属当前活动版本的流程的dp

		DataObject result = DataObject.getInstance();

		// 根据pdid获取流程下所有dp
		de.clearSql();
		de.addSql(" select distinct '1' lslc,dptdid,dptdlabel  ");
		de.addSql("   from bpzone.dutyposition_task   ");
		de.addSql("  where pdid = :pdid                   ");
		de.addSql("    and nvl(status,'0') <> '2' ");
		this.de.setString("pdid", pdid);
		dpds = this.de.query();

		// 获取当前活动的流程版本的dp
		de.clearSql();
  		de.addSql(" select distinct a.dptdid         ");
  		de.addSql("   from bpzone.task_point a,      ");
  		de.addSql("        bpzone.process_define b   ");
  		de.addSql("  where a.pdaid = b.pdaid         ");
  		de.addSql("    and b.pdid = :pdid                ");
		this.de.setString("pdid", pdid);
		dpads = this.de.query();

		// 将两个结果进行比较，获取“√”逻辑
		for (int i = 0; i < dpds.rowCount(); i++) {
			// 将dpds中也在dpads中的，lslc设置为2（即隶属活动流程）
			if (dpads.find("dptdid == " + dpds.getString(i, "dptdid")) >= 0) {
				dpds.put(i, "lslc", "2");
			}
			String dptdid = dpds.getString(i, "dptdid");
			String roleTypeStr = dpRoleTypeStr(pdid,dptdid);
			dpds.put(i, "roletype", roleTypeStr);
		}
		result.put("dpds", dpds);
		return result;
	}
	private String dpRoleTypeStr(String pdid, String dptdid) throws AppException{
  		de.clearSql();
  		de.addSql(" select a.roletypelabel ");
  		de.addSql("   from bpzone.dproletype a ");
  		de.addSql("  where a.pdid = :pdid ");
  		de.addSql("    and a.dptdid = :dptdid ");
		this.de.setString("pdid", pdid);
		this.de.setString("dptdid", dptdid);
		DataStore roleTypeLabelVds = this.de.query();
		StringBuffer sqlBF = new StringBuffer();
		sqlBF.setLength(0);
		for(int i = 0; i < roleTypeLabelVds.size(); i++){
			String roletypelabel = roleTypeLabelVds.getString(i, "roletypelabel");
			sqlBF.append(roletypelabel);
			if(i < roleTypeLabelVds.size() - 1){
				sqlBF.append(",");
			}
		}
		return sqlBF.toString();
	}
	public DataObject getPdNotAdaptOrgReason(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
		String pdlabel = para.getString("pdlabel");
		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");

		//获取因 roletype无法使用的原因
		de.clearSql();
		de.addSql(" select c.orgno,c.orgname ");
  		de.addSql("   from bpzone.dproletype a, ");
  		de.addSql("        odssu.ir_org_role_type b, ");
  		de.addSql("        odssu.custome_footstone c ");
  		de.addSql("  where a.pdid = :pdid ");
  		de.addSql("    and a.roletypeid = b.roletypeno ");
  		de.addSql("    and b.orgtypeno = c.orgtype ");
  		de.addSql("    and c.orgno = :orgno ");
		this.de.setString("pdid", pdid);
		this.de.setString("orgno", orgno);
		DataStore orgVds = this.de.query();
		StringBuffer reasonBF = new StringBuffer();
		reasonBF.append("");
		if(orgVds == null || orgVds.size() == 0){
			reasonBF.append("流程【" + pdlabel +"】适用的机构类型，与机构【" + orgname + "】对应的机构类型不匹配\r\n");
		}
		if(OdssuUtil.isYwjbjgbycfsno(orgno)){
			de.clearSql();
  			de.addSql(" select 1 ");
  			de.addSql("   from bpzone.process_define a, ");
  			de.addSql("        bpzone.process_businesstype b, ");
  			de.addSql("        odssu.org_business_scope c ");
  			de.addSql("  where a.pdid = :pdid ");
  			de.addSql("    and a.pdaid = b.pdaid ");
  			de.addSql("    and c.orgno = :orgno ");
  			de.addSql("    and b.ywlxid = c.scopeno ");
			this.de.setString("pdid", pdid);
			this.de.setString("orgno", orgno);
			DataStore scopeVds = this.de.query();
			if(scopeVds == null || scopeVds.size() == 0){
				reasonBF.append("流程【" + pdlabel + "】的业务范畴，与机构【" + orgname + "】对应的业务范畴不匹配\r\n");
			}
		}
		DataObject result = DataObject.getInstance();
		result.put("reasonstr", reasonBF.toString());
		return result;
	}
}
