package com.dw.org.orgquerycheck;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;

public class OrgQueryCheckBPO extends BPO{
	/**
	 * 点击一个机构下面显示的数据
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataStore queryHrOrgLov4EmpAdd(DataObject para) throws AppException {
		String roletype1 = para.getString("roletype1","");
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String orgno=para.getString("belongorgno",""); 

		de.clearSql();
		de.addSql(" select o.orgno,o.displayname,o.orgname,o.sleepflag,oy.typename  ");
		de.addSql(" from   odssu.ir_org_closure i, ");
		de.addSql("        odssu.orginfor o, ");
		de.addSql("       odssu.org_type oy ");
		de.addSql(" where  i.orgno = o.orgno  ");
		de.addSql("     and o.orgtype = oy.typeno ");
		de.addSql("     and o.sleepflag = '0'  ");
		de.addSql("     and( i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid )");
		de.addSql("      or o.orgno = :orgroot  )");
		de.addSql("     and  oy.typename   in  ( "+roletype1 +")");
		if(orgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
		}
		if(OdssuContants.ORGROOT.equals(orgno)) {
			de.addSql(" and o.orgno = :orgroot");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		
		de.setString("dbid", dbid);
		de.setString("orgroot", OdssuContants.ORGROOT);
		if(orgno!=""){
			de.setString("belongorgno", orgno);
		}
		DataStore orgds = de.query();
		return orgds;
		
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息，包括已选中的机构进行处理
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject dealOrgData(DataObject para) throws AppException {
		DataObject result = DataObject.getInstance();
		String belongorg = para.getString("orgno");
		String roleno = para.getString("roleno");
 		String piid = para.getString("piid");
		String empno = para.getString("empno");
		String roletype = para.getString("roletype");
		String typename = roletype;
		para.put("belongorgno", belongorg);
		String [] roletypearr = roletype.split(",");
		//已选中的机构信息
		DataStore orgchoosedds = initOrgTree(piid,roleno,empno);
		orgchoosedds = getSelectRow(orgchoosedds);
		String roletype1 = "";
		for(int i=0;i<roletypearr.length-1;i++){
			roletype1 = roletype1+"'"+roletypearr[i]+"',";
		}
		roletype1 = roletype1+"'"+roletypearr[roletypearr.length-1]+"'";
		para.put("roletype1", roletype1);
		//全部的机构信息
		DataStore roledsbeforedel = queryHrOrgLov4EmpAdd(para);
		DataStore orgds = getSelectRowROle(roledsbeforedel,orgchoosedds,roleno,empno,typename);
	
		result.put("orgds", orgds);
		return result;
	}
	
	/**
	 * 在机构页面选择结束之后，对树种的节点的勾选框进行处理
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject roleOrgChoosed(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		String piid = para.getString("piid");
		String empno = para.getString("empno");
		DataStore orgds = initOrgTree(piid,roleno,empno);
		orgds = getSelectRow(orgds);
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	/**
	 * 初始化已选择的机构页面，即获取某人在某机构下拥有的角色信息
	 * @param piid
	 * @param roleno
	 * @param empno
	 * @return
	 * @throws AppException
	 */
	public DataStore initOrgTree(String piid,String roleno,String empno) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select   a.orgno,c.orgname ,d.typename ,'true'   row_selected   ,:empno    empno ,:roleno roleno "); 
		de.addSql("from     odssuws.emp4role_detal  a  ,                          "); 
		de.addSql("         odssu.orginfor   c    ,                                   "); 
		de.addSql("         odssu.org_type   d                                       "); 
		de.addSql("where    a.empno = :empno  										  "); 
		de.addSql("         and   a.opflag = '(+)'  								  "); 
		de.addSql("         and   a.orgno  =  c.orgno 							  "); 
		de.addSql("         and   a.roleno   = :roleno                                ");
		de.addSql("         and   a.piid   = :piid                                    ");
		de.addSql("         and  c.sleepflag = '0' ");
		de.addSql("         and  c.orgtype = d.typeno ");
		de.addSql("order by  a.opflag   desc                                          ");
		de.setString("empno",empno);
		de.setString("roleno", roleno);
		de.setString("piid", piid);
		DataStore rolechoosedds = de.query();
		//账表中除了删除之后的数据
		de.clearSql();
		de.addSql("select   distinct  a.orgno,c.orgname, d.typename ,'true'   row_selected   ,:empno    empno ,:roleno roleno "); 
		de.addSql("from     odssu.ir_emp_org_all_role  a  ,                   "); 
		de.addSql("         odssu.orginfor   c,                               "); 
		de.addSql("         odssu.org_type   d                                "); 
		de.addSql("where    a.empno = :empno                                  "); 
		de.addSql("         and  not EXISTS(                                  "); 
		de.addSql("                select  1  from odssuws.emp4role_detal b   "); 
		de.addSql("                where  a.empno = b.empno                   "); 
		de.addSql("                       and   a.roleno  = b.roleno          "); 
		de.addSql("                       and   a.orgno = b.orgno             "); 
		de.addSql("                       and   b.opflag  =  '(-)'            "); 
		de.addSql("                       and   b.piid = :piid                ");
		de.addSql("         )                                                 "); 
		de.addSql("         and   a.roleno  = :roleno                           ");
		de.addSql("         and   a.orgno  =  c.orgno 					  "); 
		de.addSql("         and  c.sleepflag = '0' ");
		de.addSql("         and  c.orgtype = d.typeno ");
		de.setString("empno",empno);
		de.setString("piid", piid);
		de.setString("roleno", roleno);
		DataStore vds = de.query();
		//全部的已选择的数据
		rolechoosedds.combineDatastore(vds);
		//查询所有的角色信息
		return rolechoosedds;
	}
	
	/**
	 * 对已选的勾选框进行处理
	 * @param roledsbeforedel
	 * @param rolechoosedds
	 * @param roleno
	 * @param empno
	 * @param roletype
	 * @return
	 * @throws AppException
	 */
	public DataStore getSelectRowROle(DataStore roledsbeforedel,DataStore rolechoosedds,String roleno,String empno,String roletype) throws AppException{
		DataStore roleds = DataStore.getInstance();
		for(int i =0 ;i<roledsbeforedel.size();i++){
			DataObject vds = roledsbeforedel.get(i);
			String orgno1 = roledsbeforedel.getString(i, "orgno");
			vds.put("_row_selected", false);
			vds.put("roleno", roleno);
			vds.put("empno", empno);
			vds.put("roletype", roletype);
			for (int j=0;j<rolechoosedds.size();j++){
				String orgno2 = rolechoosedds.getString(j, "orgno");
				if(orgno1.equals(orgno2)){
					vds.put("_row_selected", true);
					break;
				} 
			}
			roleds.addRow(vds);
		}
		roleds.sortdesc("_row_selected");
		return roleds;
	}
	public DataStore getSelectRow(DataStore orgds) throws AppException{
		for (int i=0;i<orgds.size();i++){
			orgds.put(i,"_row_selected", true);
		}
		return orgds;
	}
}

