package com.dw.vap.org;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.framework.dbengine.DE;

public class OrgTypeVapBPO extends BPO{
	/**
	 * 查询机构类型的基本信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-7
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryOrgtype(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		DE de = DE.getInstance(); 
		de.clearSql();
  		de.addSql(" select typeno,typename,minlength,maxlength,comments,");
  		de.addSql("        allowletter,allowunderline,qzyfjgbhdt,typenature,decode(ywjbjgbz,'0','×','1','√') ");
  		de.addSql(" from odssu.org_type");
  		de.addSql(" where typeno = :typeno ");
  		de.addSql(" order by typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgtypes", vds);
		return vdo;
	}

	/**
	 *查询可以的使用的下级机构类型 ，要去除该机构类型 的上级机构类型 及以上。去除该机构类型的直接下级机构类型
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject querySubOrgtype4Add(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		String subtypeno1 = para.getString("subtypeno");
		DE de = DE.getInstance();
		de.clearSql();
		// 去除该机构类型 的上级机构类型 及以上
		de.addSql(" select  a.typeno orgtype , a.typename ");
		de.addSql(" from odssu.org_type a  ");
		de.addSql(" where a.typeno not in ( select suptypeno typeno from odssu.ir_org_type ");
		de.addSql("            start with subtypeno = :typeno  connect by nocycle prior suptypeno = subtypeno) and a.typeno !=  :typeno ");
		de.addSql("       and (typeno like :typename or typename like :typename) ");
		de.setString("typeno", typeno);
		de.setString("typename", "%" + subtypeno1 + "%");
		
		DataStore typeds = de.query();

		// 查询直接下属机构类型
		de.clearSql();
  		de.addSql(" select a.typeno orgtype , a.typename ");
  		de.addSql("   from odssu.org_type a , odssu.ir_org_type b ");
  		de.addSql("  where  b.suptypeno = :typeno and a.typeno = b.subtypeno ");
		de.setString("typeno", typeno);

		DataStore subds = de.query();

		DataObject vdo = DataObject.getInstance();
		if (subds.rowCount() == 0) {
			vdo.put("vds", typeds);
		}
		if (subds.rowCount() > 0) {
			for (int i = 0; i < subds.rowCount(); i++) {
				String subtypeno = subds.getString(i, "orgtype");
				for (int j = 0; j < typeds.rowCount(); j++) {
					String oldtypeno = typeds.getString(j, "orgtype");
					if (subtypeno.equals(oldtypeno)) {
						typeds.delRow(j);
						break;
					}
				}
			}
			vdo.put("vds", typeds);
		}

		return vdo;
	}

	/**
	 * 查询机构类型的上级机构类型
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-7
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject querySupOrgType(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		DE de = DE.getInstance();    	
		de.clearSql();
  		de.addSql(" select o.typeno,o.typename,o.minlength,o.maxlength,o.comments,");
  		de.addSql("        o.allowletter,o.allowunderline,o.qzyfjgbhdt,i.hzzsbz,i.hzzsmc ");
  		de.addSql(" from   odssu.ir_org_type i, ");
  		de.addSql("        odssu.org_type o ");
  		de.addSql(" where  i.suptypeno = o.typeno and i.subtypeno = :typeno ");
  		de.addSql(" order by o.typeno ");
		de.setString("typeno", typeno);
		DataStore vdssjlb = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgsuptypes", vdssjlb);
		return vdo;
	}

	/**
	 * 查询机构类型的下级机构类型
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-7
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject querySubOrgType(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select o.typeno,o.typename,o.minlength,o.maxlength,o.comments, ");
  		de.addSql("        o.allowletter,o.allowunderline,o.qzyfjgbhdt,i.hzzsbz,i.hzzsmc ");
  		de.addSql(" from   odssu.ir_org_type i, ");
  		de.addSql("        odssu.org_type o ");
  		de.addSql(" where  i.subtypeno = o.typeno and i.suptypeno = :typeno ");
  		de.addSql(" order by o.typeno ");
		de.setString("typeno", typeno);
		DataStore vdssjlb = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgsubtypes", vdssjlb);
		return vdo;
	}

	/**
	 * 查询机构类型的允许的角色类型
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-7
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryOrgRoleType(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select r.typeno,r.typename,r.comments ");
  		de.addSql(" from   odssu.ir_org_role_type t, ");
  		de.addSql("        odssu.role_type r ");
  		de.addSql(" where  t.roletypeno= r.typeno and t.orgtypeno = :typeno ");
  		de.addSql(" order by r.typeno ");
		de.setString("typeno", typeno);
		DataStore vdsjslx = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgroletypes", vdsjslx);
		return vdo;
	}

	/**
	 * 机构类型新增信息保存
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject saveOrgTypeInfor(DataObject para) throws Exception {
		String typeno, typename, allowletter, allowunderline, qzyfjgbhdt, typenature, comments;
		int minlength, maxlength;

		typeno = para.getString("typeno");
		typename = para.getString("typename");
		minlength = para.getInt("minlength");
		maxlength = para.getInt("maxlength");
		allowletter = para.getString("allowletter");
		allowunderline = para.getString("allowunderline");
		qzyfjgbhdt = para.getString("qzyfjgbhdt");
		typenature = para.getString("typenature");
		comments = para.getString("comments");

		if (typeno == null || "".equals(typeno)) {
			this.bizException("机构类型编号不能为空！！");
		}

		if (typeno == null || "".equals(typeno)) {
			this.bizException("机构类型编号不能为空！！");
		}

		if (minlength < 1) {
			this.bizException("最小长度不能小于1，保存失败！");
		}
		if (maxlength > 20) {
			this.bizException("最小长度不能大于20，保存失败！");
		}

		if (maxlength < minlength) {
			this.bizException("最大长度不能小于最小长度，保存失败！");
		}
		if (typename == null || "".equals(typename)) {
			this.bizException("机构类型名称不能为空！！");
		}
		if (typenature == null || "".equals(typenature)) {
			this.bizException("机构类型性质不能为空！！");
		}

		// 判断是否只含有数字、字母、下划线
		if (typeno.matches("[0-9A-Za-z_]*") == false) {
			this.bizException("机构类型编号中含有除数字、字母、下划线之外的其他字符，保存失败！");
		}
		DE de = DE.getInstance();		
  		de.clearSql();
  		de.addSql(" select 1 from odssu.org_type where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		if (vds.rowCount() > 0) {
			this.bizException("编号为【" + typeno + "】的机构类型已经存在，添加失败！");
		}

		de.clearSql();
  		de.addSql(" insert into odssu.org_type( typeno, typename ,  minlength , maxlength ,allowletter , allowunderline ,qzyfjgbhdt,typenature , comments ) ");
  		de.addSql("                     values( :typeno , :typename, :minlength, :maxlength , :allowletter , :allowunderline , :qzyfjgbhdt , :typenature  , :comments )");
		de.setString("typeno", typeno);
		de.setString("typename", typename);
		de.setInt("minlength", minlength);
		de.setInt("maxlength", maxlength);
		de.setString("allowletter", allowletter);
		de.setString("allowunderline", allowunderline);
		de.setString("qzyfjgbhdt", qzyfjgbhdt);
		de.setString("typenature", typenature);
		de.setString("comments", comments);
		int result = de.update();

		if (result == 0) {
			this.bizException("新增机构类型信息保存失败！！");
		}

		return null;
	}

	/**
	 * 机构类型信息 编辑后的保存
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject updateOrgTypeInfor(DataObject para) throws Exception {

		String typeno, typename, allowletter, allowunderline, qzyfjgbhdt, typenature, comments;
		int minlength, maxlength;

		typeno = para.getString("typeno");
		typename = para.getString("typename");
		minlength = para.getInt("minlength");
		maxlength = para.getInt("maxlength");
		allowletter = para.getString("allowletter");
		allowunderline = para.getString("allowunderline");
		qzyfjgbhdt = para.getString("qzyfjgbhdt");
		typenature = para.getString("typenature");
		comments = para.getString("comments");

		if (typeno == null || "".equals(typeno)) {
			this.bizException("机构类型编号不能为空！！");
		}

		if (minlength < 1) {
			this.bizException("最小长度不能小于1，保存失败！");
		}
		if (maxlength > 20) {
			this.bizException("最小长度不能大于20，保存失败！");
		}

		if (maxlength < minlength) {
			this.bizException("最大长度不能小于最小长度，保存失败！");
		}
		DE de = DE.getInstance();		
    	de.clearSql();
  		de.addSql(" select 1 from odssu.org_type where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("编号为【" + typeno + "】的机构类型不存在，保存失败！");
		}
    	de.clearSql();
  		de.addSql(" update odssu.org_type set typename = :typename , minlength = :minlength   , maxlength = :maxlength ,");
  		de.addSql("            allowletter = :allowletter , allowunderline = :allowunderline , qzyfjgbhdt = :qzyfjgbhdt,");
  		de.addSql("            typenature = :typenature,  comments = :comments ");
  		de.addSql("  where typeno = :typeno  ");
		de.setString("typename", typename);
		de.setInt("minlength", minlength);
		de.setInt("maxlength", maxlength);
		de.setString("allowletter", allowletter);
		de.setString("allowunderline", allowunderline);
		de.setString("qzyfjgbhdt", qzyfjgbhdt);
		de.setString("typenature", typenature);
		de.setString("comments", comments);
		de.setString("typeno", typeno);

		int result = de.update();

		if (result == 0) {
			this.bizException("更新机构类型信息保存失败！！");
		}

		return null;
	}

	/**
	 * 检测机构类型信息能否被删除
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-22
	 * @return
	 * @throws Exception
	 */
	public DataObject btnOrgTypeDeleteCheck(DataObject para) throws Exception {
		String typeno, flag = "true";

		typeno = para.getString("typeno");

		if (typeno == null || "".equals(typeno)) {
			this.bizException("机构类型编号为空！");
		}
		DE de = DE.getInstance();		
  		de.clearSql();
  		de.addSql(" select 1 from odssu.orginfor ");
  		de.addSql(" where orgtype = :typeno ");
		de.setString("typeno", typeno);

		DataStore vds = de.query();
		DataObject vdo = DataObject.getInstance();

		if (vds.rowCount() == 0) {
			flag = "false";
			vdo.put("flag", flag);
			return vdo;
		}
		vdo.put("flag", flag);

		return vdo;
	}

	/**
	 * 删除机构类型
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject btnOrgTypeDelete(DataObject para) throws Exception {
		String typeno;

		typeno = para.getString("typeno");

		if (typeno == null || "".equals(typeno)) {
			this.bizException("机构类型编号为空！");
		}
		DE de = DE.getInstance();		
    	de.clearSql();
  		de.addSql(" select 1 from odssu.org_type where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("编号为【" + typeno + "】的机构类型不存在，删除失败！");
		}
    	de.clearSql();
  		de.addSql(" delete from odssu.org_type ");
  		de.addSql(" where typeno = :typeno ");
		de.setString("typeno", typeno);
		int result = de.update();

		if (result == 0) {
			this.bizException("删除失败！！");
		}

		de.clearSql();
  		de.addSql(" delete from odssu.ir_org_type where suptypeno = :typeno or subtypeno = :typeno ");
		de.setString("typeno", typeno);
		de.update();

		de.clearSql();
  		de.addSql(" delete from odssu.ir_org_role_type where orgtypeno = :typeno  ");
		de.setString("typeno", typeno);
		de.update();

		DataObject vdo = DataObject.getInstance();

		return vdo;
	}

	/**
	 * 保存新增下级机构信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject saveSubOrgTypeInfor(DataObject para) throws Exception {
		String suptypeno, subtypeno, hzzsbz, hzzsmc, subtypename;

		suptypeno = para.getString("typeno");
		subtypeno = para.getString("subtypeno");
		subtypename = para.getString("subtypename");
		hzzsbz = para.getString("hzzsbz");
		hzzsmc = para.getString("hzzsmc");
		DE de = DE.getInstance();		
    	de.clearSql();
    	de.addSql(" select typename  from odssu.org_type where typeno = :subtypeno  ");
		de.setString("subtypeno", subtypeno);

		DataStore typeds = de.query();

		if (typeds.rowCount() == 0) {
			this.bizException("机构类型名称所对应的机构类型信息不存在！");
		}
		if (typeds.rowCount() > 0) {
			String oldtypeno = typeds.getString(0, "typename");
			if (!oldtypeno.equals(subtypename)) {
				this.bizException("机构类型名称所对应的机构编号错误！！");
			}
		}

		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_org_type where suptypeno = :suptypeno and subtypeno = :subtypeno ");
		de.setString("suptypeno", suptypeno);
		de.setString("subtypeno", subtypeno);
		DataStore vds = de.query();

		if (vds.rowCount() > 0) {
			this.bizException("此关系已经存在，保存失败！");
		}

		de.clearSql();
  		de.addSql(" insert into odssu.ir_org_type ( suptypeno , subtypeno ,hzzsbz ,hzzsmc )  ");
  		de.addSql("     values ( :suptypeno ,:subtypeno , :hzzsbz , :hzzsmc ) ");
		de.setString("suptypeno", suptypeno);
		de.setString("subtypeno", subtypeno);
		de.setString("hzzsbz", hzzsbz);
		de.setString("hzzsmc", hzzsmc);

		int result = de.update();

		if (result == 0) {
			this.bizException("插入下级机构类型信息失败！");
		}
		return null;
	}

	/**
	 * 删除下级机构类型信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject subTypeDelete(DataObject para) throws Exception {
		String subtypeno, suptypeno;

		subtypeno = para.getString("subtypeno");
		suptypeno = para.getString("suptypeno");

		if (subtypeno == null || "".equals(subtypeno)) {
			this.bizException("下级机构类型编号为空！");
		}
		if (suptypeno == null || "".equals(suptypeno)) {
			this.bizException("上级机构类型编号为空！");
		}
		DE de = DE.getInstance();
    	de.clearSql();
  		de.addSql(" select 1 from odssu.ir_org_type where suptypeno = :suptypeno and subtypeno = :subtypeno ");
		de.setString("suptypeno", suptypeno);
		de.setString("subtypeno", subtypeno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("此关系不存在，删除失败！");
		}
		
		de.clearSql();
  		de.addSql(" delete from odssu.ir_org_type ");
  		de.addSql(" where subtypeno = :subtypeno and suptypeno = :suptypeno ");
		de.setString("subtypeno", subtypeno);
		de.setString("suptypeno", suptypeno);

		int result = de.update();

		if (result == 0) {
			this.bizException("删除失败！！");
		}
		DataObject vdo = DataObject.getInstance();

		return vdo;
	}

	/**
	 * 查询本级机构类型名称
	 * 
	 * @author ZZZ
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public DataObject queryorgmc(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select typename orgmc,typeno ");
  		de.addSql(" from odssu.org_type ");
  		de.addSql(" where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("vds", vds);
		return vdo;
	}

	/**
	 * 查询新增角色类型的类型编号和名称
	 * 
	 * @author ZZZ
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject lovForRole(DataObject para) throws Exception {
		String roletypeno = para.getString("roletypeno");
		
		roletypeno = "%"+roletypeno+"%";
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select r.typeno roletypeno,r.typename");
  		de.addSql(" from odssu.role_type r");
  		de.addSql(" where r.typeno like  :roletypeno  or r.typename like :roletypeno ");
//		str.append(" select distinct i.roletypeno,r.typename");
//		str.append(" from odssu.ir_org_role_type i,odssu.role_type r");
//		str.append(" where i.roletypeno=r.typeno and ( r.typeno like  ?  or r.typename like ? )");
		de.setString("roletypeno", roletypeno);
		DataStore vdsrole = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("roletype", vdsrole);
		return vdo;
	}
	
	/**
	 * 查询机构类型对应的角色类型信息
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-24
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryOrgRoleInfor(DataObject para) throws Exception{
		String typeno , suborgtypeno ;
		
		typeno = para.getString("typeno");
		suborgtypeno = para.getString("suborgtypeno");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select a.suptypeno suptypeno , b.typename suptypename , a.subtypeno , c.typename subtypename , a.hzzsbz , a.hzzsmc   ");
  		de.addSql("  from odssu.ir_org_type a , odssu.org_type b ,odssu.org_type  c ");
  		de.addSql("  where a.suptypeno= :typeno and a.suptypeno = b.typeno and a.subtypeno = :suborgtypeno and a.subtypeno = c.typeno  ");
		de.setString("typeno",typeno);
		de.setString("suborgtypeno", suborgtypeno);
		
		DataStore roletypeds = de.query();
		
		DataObject vdo = DataObject.getInstance();

		vdo.put("orgtypeds", roletypeds);
		
		return vdo;
	}
	/**
	 * 保存新增角色类型信息
	 * 
	 * @throws Exception
	 */
	public void saveAddRole(DataObject para) throws Exception {
		String roletypeno = para.getString("roletypeno");
		String orgtypeno = para.getString("typeno");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_org_role_type  ");
  		de.addSql(" where roletypeno  = :roletypeno and orgtypeno = :orgtypeno ");
		de.setString("roletypeno", roletypeno);
		de.setString("orgtypeno", orgtypeno);
		DataStore vds = de.query();

		if (vds.rowCount() > 0) {
			this.bizException("此关系已存在，无需再次添加！");
		}

		de.clearSql();
  		de.addSql(" insert into odssu.ir_org_role_type ");
  		de.addSql(" (roletypeno,orgtypeno) ");
  		de.addSql(" values (:roletypeno,:orgtypeno)");
		de.setString("roletypeno", roletypeno);
		de.setString("orgtypeno", orgtypeno);
		de.update();
	}

	/**
	 * 删除机构类型允许的角色类型
	 * 
	 * @param para
	 * @throws Exception
	 */
	public void delOrgRoleType(DataObject para) throws Exception {
		String roletypeno = para.getString("roletypeno");
		String orgtypeno = para.getString("orgtypeno");
		DE de = DE.getInstance();
   		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_org_role_type  ");
  		de.addSql(" where roletypeno  = :roletypeno and orgtypeno = :orgtypeno ");
		de.setString("roletypeno", roletypeno);
		de.setString("orgtypeno", orgtypeno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("此关系不存在，删除失败！");
		}

		de.clearSql();
  		de.addSql(" delete from odssu.ir_org_role_type ");
  		de.addSql(" where roletypeno = :roletypeno and orgtypeno = :orgtypeno ");
		de.setString("roletypeno", roletypeno);
		de.setString("orgtypeno", orgtypeno);
		de.update();
	}
	
	/**
	 *保存下级机构类型编辑信息
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-25
	 * @param para
	 * @throws Exception 
	 */
	public void saveSubOrgTypeEditInfor(DataObject para) throws Exception{
		String subtypeno , suptypeno , hzzsmc , hzzsbz ,subtypename ;
		
		subtypeno = para.getString("subtypeno");
		suptypeno = para.getString("suptypeno");
		hzzsmc = para.getString("hzzsmc");
		hzzsbz = para.getString("hzzsbz");
		subtypename = para.getString("subtypename");
		
		if(subtypeno == null || "".equals(subtypeno.trim())){
			this.bizException("下级机构类型编号不能为空！");
		}
		
		if(suptypeno == null || "".equals(suptypeno.trim())){
			this.bizException("上级机构类型编号不能为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
    	de.addSql(" select typename  from odssu.org_type where typeno = :subtypeno  ");
		de.setString("subtypeno", subtypeno);

		DataStore typeds = de.query();

		if (typeds.rowCount() == 0) {
			this.bizException("机构类型名称所对应的机构类型信息不存在！");
		}
		if (typeds.rowCount() > 0) {
			String oldtypeno = typeds.getString(0, "typename");
			if (!oldtypeno.equals(subtypename)) {
				this.bizException("机构类型名称所对应的机构编号错误！！");
			}
		}
		
		de.clearSql();
  		de.addSql(" update odssu.ir_org_type set hzzsbz = :hzzsbz , hzzsmc = :hzzsmc  ");
  		de.addSql(" where suptypeno = :suptypeno and subtypeno = :subtypeno   ");
		de.setString("hzzsbz", hzzsbz);
		de.setString("hzzsmc", hzzsmc);
		de.setString("suptypeno", suptypeno);
		de.setString("subtypeno", subtypeno);
		
		int result = de.update();
		
		if(result == 0){
			this.bizException("更新下级机构信息失败！");
		}
	}
}
