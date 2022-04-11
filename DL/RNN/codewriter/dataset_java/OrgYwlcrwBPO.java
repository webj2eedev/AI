package com.dw.hsuods.vap.org;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.taglib.multiselecttree.MultiSelectTreeDS;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.hsuods.ws.org.util.OuterDutyPDDPDataSource;
import com.dw.util.OdssuUtil;

public class OrgYwlcrwBPO extends BPO{

	/**
	 * @描述：查询业务机构相关外岗
	 * @param para
	 * @return
	 * @throws Exception
	 * 2015-5-28
	 */
	public DataObject queYwjgxgwg(DataObject para) throws Exception{
		String orgno = para.getString("orgno");
		String dptdid = para.getString("dptdid");
		String pdid = para.getString("pdid");

		//查询人员
		de.clearSql();
  		de.addSql("select c.empno, c.empname  ");
  		de.addSql("from odssu.ir_emp_org_all_role a, ");
  		de.addSql("     bpzone.dutyposition_task_role b, ");
  		de.addSql("     odssu.empinfor c ");
  		de.addSql("where a.roleno=b.roleid ");
  		de.addSql("      and a.empno=c.empno ");
  		de.addSql("      and a.orgno=:orgno ");
  		de.addSql("      and b.dptdid=:dptdid ");
  		de.addSql("      and b.pdid=:pdid ");
		this.de.setString("orgno", orgno);
		this.de.setString("dptdid", dptdid);
		this.de.setString("pdid", pdid);
		DataStore dsemp = this.de.query();

		//取orgno下的所有外岗
		de.clearSql();
  		de.addSql("select a.dutyno, a.inorgno, b.rolename  ");
  		de.addSql("from odssu.outer_duty a, ");
  		de.addSql("     odssu.roleinfor b ");
  		de.addSql("where a.roleno=b.roleno ");
  		de.addSql("      and a.faceorgno=:orgno ");
		this.de.setString("orgno", orgno);
 		DataStore dsduty=this.de.query();
		
		if(dsduty==null || dsduty.rowCount()==0){
			DataObject result = DataObject.getInstance();
			result.put("dswg", DataStore.getInstance());
			result.put("dsemp", dsemp);
			return result;
		}
		//过滤外岗  外岗对应角色在DP对应的角色中
		DataStore dswg = DataStore.getInstance();//orgno下的外岗
		for(int i=0; i<dsduty.rowCount(); i++){
			String dutyno = dsduty.getString(i, "dutyno");
			de.clearSql();
  			de.addSql("select *  ");
  			de.addSql("from odssu.outer_duty a ");
  			de.addSql("where a.dutyno=:dutyno      ");
  			de.addSql("      and a.roleno in ( select b.roleid roleno  ");
  			de.addSql("                        from bpzone.dutyposition_task_role b  ");
  			de.addSql("                        where b.dptdid=:dptdid ) ");
  			de.addSql("                              and b.pdid=:pdid ) ");
			this.de.setString("dutyno", dutyno);
			this.de.setString("dptdid", dptdid);
			this.de.setString("pdid", pdid);
			DataStore dstemp=this.de.query();
			if(dstemp==null || dstemp.rowCount()==0){
				continue;
			}else{
				dswg.addRow(dsduty.getRow(i));
			}
		}

		DataObject result = DataObject.getInstance();
		result.put("dswg", dswg);
		result.put("dsemp", dsemp);
		return result;
	}
	
	/**
	 * @描述：所有业务机构相关外岗的Leaf页面    当选中目录是非DP时，展示orgno的所有外岗
	 * @param para
	 * @return
	 * @throws Exception
	 * 2015-5-28
	 */
	public DataObject fwPageAllYwjgxgwgJsp(DataObject para) throws Exception{
		String orgno = para.getString("orgno");
		//取orgno下的所有外岗
		de.clearSql();
  		de.addSql("select a.dutyno, a.inorgno, b.rolename  ");
  		de.addSql("from odssu.outer_duty a, ");
  		de.addSql("     odssu.roleinfor b ");
  		de.addSql("where a.roleno=b.roleno ");
  		de.addSql("      and a.faceorgno=:orgno ");
		this.de.setString("orgno", orgno);
		DataStore dsduty=this.de.query();
		//此时人员为空
		DataStore dsemp = DataStore.getInstance();
		
		DataObject result = DataObject.getInstance();
		result.put("dswg", dsduty);
		result.put("dsemp", dsemp);
		return result;
	}
	
	/**
	 * @描述：根据机构类型加载业务流程任务的数据
	 * @param para
	 * @return
	 * @throws Exception
	 * @author：wh
	 * 2015-6-3
	 */
	public DataObject refreshYwlcrwForOrgType(DataObject para) throws Exception{
		String orgno = para.getString("orgno");
		String orgflag = para.getString("orgflag");
		int orgflagCase = Integer.parseInt(orgflag);
		String orgtypeStr = "";
		switch(orgflagCase){
		case 2 :  //部门
			orgtypeStr = "'HSDOMAIN_RSCKS'";
			break;
		case 3 : //所
			orgtypeStr = "'HSDOMAIN_SBS'";
			break;
		case 4 : //站
			orgtypeStr = "'HSDOMAIN_SBZ'";
			break;
		default :
			throw new AppException("入参中机构类型不合法。");
		}
		//加载岗位的Grid

		de.clearSql();
  		de.addSql(" select t.roleno, t.rolename ");
  		de.addSql(" from odssu.inner_duty t ");
  		de.addSql(" where t.orgno=:orgno ");
  		de.addSql("       and t.orgtype in ("+orgtypeStr+") ");
		de.setString("orgno", orgno);
		DataStore dsgw = de.query();
		      
		//加载树
		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
//		DataStore dstree = mtree.genDPTreeDSByOrgAll(orgno);
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid" ):OdssuNames.DBID;

		
		/**
		 * 上边的原有代码 是要获得 业务岗位设置 树上的流程任务一览节点
		 * 即  获取 一个经办机构下 所有的流程及其对应的岗位任务
		 */
		//这里要改成掉 私有方法 1 初始化PD 2 初始化 DP 3 用DP裁剪PD 4用PD裁剪DP 5 初始化Folder 6 裁剪Forlder
		DataStore ywfcVds = OdssuUtil.getYwfcVdsByOrgno(orgno);
		//1 初始化PD
		DataStore pdVds = odPdDpFolderTree.initDSPD(orgno, ywfcVds, null,dbid);
		//2 初始化 DP
		//2.1 获取机构类型Vds
		DataStore roleTypeVds = OdssuUtil.getOuterDutyRoleTypeVdsByOrgNo(orgno);
		//2.2 将机构类型vds转换成机构类型Str
		String roleTypeStr = OdssuUtil.roleTypeVdsToroleTypeString(roleTypeVds);
//		DataStore dpVds = odPdDpFolderTree.initDSDPForOrgAll(orgno);
		DataStore dpVds = odPdDpFolderTree.initDSDP(roleTypeStr);
		//3 用DP裁剪PD
		pdVds = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(pdVds, dpVds);
		pdVds.sort("pdid");
		//4用PD裁剪DP
		dpVds = odPdDpFolderTree.cleanDS1ByDS2ViaPDID(dpVds, pdVds);
		//5 初始化Folder
		DataStore folderVds = odPdDpFolderTree.initDSFolder();
		//用PdVds裁剪folder
		folderVds = odPdDpFolderTree.cleanDSFolder(pdVds, folderVds);
		//组装树对应的DS
		DataStore dstree = odPdDpFolderTree.AssembleMtree(folderVds, pdVds, dpVds);
		
		MultiSelectTreeDS dsmtree = new MultiSelectTreeDS();
		if(dstree!=null && dstree.rowCount()>0){
			for(int i=0; i<dstree.rowCount(); i++){
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
}
