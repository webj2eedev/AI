package com.dw.duty.dutyONP;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class DutyONPBPO extends BPO{
	/**
	 * 打开全地市统一定义岗位
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject openQdsTyDuty(DataObject para) throws AppException, BusinessException {
		String deforgno = para.getString("orgno", "");
		DE de = DE.getInstance();

		String orgtype = OdssuUtil.getOrgTypeByOrgNo(deforgno);
		
		DataStore dutyds = DataStore.getInstance();
		int rownum = 0;
		
		DataStore outerroletypeds = DataStore.getInstance();
		OdssuUtil.getSubOrgtypeAdaptRoletype(outerroletypeds, orgtype, OdssuContants.JSGN_OUTERDUTY);
		for (int i = 0; i < outerroletypeds.rowCount(); i++) {
			String typeno = outerroletypeds.getString(i, "roletypeno");
			String typename = outerroletypeds.getString(i, "typename");
			
			de.clearSql();
  			de.addSql("  select count(1) wgsl from odssu.roleinfor a ");
  			de.addSql(" where a.deforgno=:deforgno and roletype = :typeno ");
  			de.addSql("  and sleepflag = '0'");
			de.setString("deforgno", deforgno);
			de.setString("typeno", typeno);
			DataStore dutywg = de.query();
			Integer wgsl = dutywg.getInt(0, "wgsl");
			dutyds.put(rownum, "dutylb", typename);
			dutyds.put(rownum, "typeno", typeno);
			dutyds.put(rownum, "dutynumber", wgsl.toString());
			rownum++;
		}
		
		DataStore inroletypeds = DataStore.getInstance();
		OdssuUtil.getSubOrgtypeAdaptRoletype(inroletypeds, orgtype, OdssuContants.JSGN_INNERDUTY);
		for (int i = 0; i < inroletypeds.size(); i++) {
			String typeno = inroletypeds.getString(i, "roletypeno");
			String typename = inroletypeds.getString(i, "typename");
			de.clearSql();
  			de.addSql("  select count(1) wgsl from odssu.roleinfor a ");
  			de.addSql(" where a.deforgno=:deforgno and roletype = :typeno ");
  			de.addSql("  and sleepflag = '0'");
			de.setString("deforgno", deforgno);
			de.setString("typeno", typeno);
			DataStore dutywg = de.query();
			Integer wgsl = dutywg.getInt(0, "wgsl");
			dutyds.put(rownum, "dutylb", typename);
			dutyds.put(rownum, "typeno", typeno);
			dutyds.put(rownum, "dutynumber", wgsl.toString());
			rownum++;
		}

		DataObject result = DataObject.getInstance();
		result.put("qdstyduty", dutyds);
		result.put("deforgno", deforgno);
		return result;
	}


	/**
	 * 获取内岗
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject getOveralInnerDuty(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String deforgno = para.getString("orgno");
		String roletype = para.getString("roletype");
  		de.clearSql();
  		de.addSql("  select roleno,rolename from odssu.roleinfor   ");
  		de.addSql(" where deforgno=:deforgno and roletype = :roletype and sleepflag = '0'");
  		de.addSql(" order by rolename ");
		de.setString("deforgno", deforgno);
		de.setString("roletype", roletype);
		DataStore ds = de.query();
		DataObject result = DataObject.getInstance();
		result.put("duty", ds);
		return result;
	}

	/**
	 * 打开业务外岗
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject openYwwg(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String orgno = para.getString("orgno");
		String roletype = para.getString("roletype");
		de.clearSql();
  		de.addSql("  select a.classno,a.classname from odssu.duty_classification a  where a.orgno=:orgno order by sn,classname ");
		de.setString("orgno", orgno);
		DataStore ds = de.query();
		for (int i = 0; i < ds.rowCount(); i++) {
			String classno = ds.getString(i, "classno");
			de.clearSql();
  			de.addSql("  select count(1) classnumber from odssu.roleinfor  where deforgno=:orgno and classno=:classno and roletype = :roletype and sleepflag = '0' ");
			de.setString("orgno", orgno);
			de.setString("classno", classno);
			de.setString("roletype", roletype);
			DataStore vds = de.query();
			ds.put(i, "classnumber", ((Integer) vds.getInt(0, "classnumber")).toString());
		}
		de.clearSql();
		de.addSql("select count(*) classnumber from odssu.roleinfor a where a.deforgno = :orgno and a.jsgn = '3' and a.roletype = :roletype and a.classno is null and a.sleepflag = '0' ");
		de.setString("orgno", orgno);
		de.setString("roletype", roletype);
		DataStore dsWithoutclass = de.query();

		int withoutClassnumber = dsWithoutclass.getInt(0, "classnumber");
		if (withoutClassnumber > 0) {
			ds.put(ds.rowCount(), "classno", "");
			ds.put(ds.rowCount() - 1, "classname", "未分组");
			ds.put(ds.rowCount() - 1, "classnumber", withoutClassnumber);
		}

		DataObject result = DataObject.getInstance();
		result.put("ywwg", ds);
		return result;

	}

	/**
	 * 打开业务外岗的具体分类
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject openClassifyDuty(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();

		String classno = para.getString("classno", "");
		String deforgno = para.getString("orgno");
		String roletype = para.getString("roletype","");
		DataStore vds = DataStore.getInstance();
		if (classno == null || classno.equals("")) {
			de.clearSql();
  			de.addSql("  select roleno,rolename from odssu.roleinfor  where deforgno=:deforgno and classno is null and roletype = :roletype and sleepflag = '0'");
  			de.addSql(" order by rolename ");
			de.setString("deforgno", deforgno);
			de.setString("roletype", roletype);
			vds = de.query();
		} else {
			de.clearSql();
  			de.addSql("  select roleno,rolename from odssu.roleinfor  where deforgno=:deforgno and classno=:classno and roletype = :roletype and sleepflag = '0'");
  			de.addSql(" order by rolename ");
			de.setString("deforgno", deforgno);
			de.setString("classno", classno);
			de.setString("roletype", roletype);
			vds = de.query();
		}

		DataObject result = DataObject.getInstance();
		result.put("classduty", vds);
		return result;

	}

	/**
	 * 打开分类目录
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject openSelectFloder(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String deforgno = para.getString("deforgno");
		de.clearSql();
  		de.addSql("  select a.classno,a.classname from odssu.duty_classification a  where a.orgno=:deforgno  ");
		de.setString("deforgno", deforgno);
		DataStore ds = de.query();
		DataObject result = DataObject.getInstance();
		result.put("folderds", ds);
		return result;

	}

	/**
	 * 保存修改的岗位分类目录
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject saveSelectFolder(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();

		String classno = para.getString("classno");
		String roleno = para.getString("roleno");
		de.clearSql();
  		de.addSql("update odssu.roleinfor set classno = :classno  ");
		de.addSql("where roleno = :roleno  ");
		de.setString("classno", classno);
		de.setString("roleno", roleno);
		de.update();

		return null;

	}

	/**
	 * 方法详述:保存分类编辑结果
	 * 
	 * @author fandq
	 * @throws AppException
	 * @throws BusinessException
	 * @date 创建时间 2016年10月18日
	 * @since V1.0
	 */
	public DataObject saveClassification(DataObject para) throws AppException, BusinessException {
		String orgno = para.getString("orgno", "");
		DataStore classds = para.getDataStore("classgrid");
		StringBuffer classnoBF = new StringBuffer();
		DataStore class_tmp = DataStore.getInstance();
		DE de = DE.getInstance();
		// 首先对数据进行校验
		for (int i = 0; i < classds.size(); i++) {
			String classname = classds.getString(i, "classname");
			String classno = classds.getString(i, "classno");

			for (int j = 0; j < classds.size(); j++) {
				if (i == j) {
					continue;
				}
				String classname_tmp = classds.getString(j, "classname");
				if (classname.equals(classname_tmp)) {
					this.bizException("存在相同的分类名称：【" + classname + "】。");
				}
			}

			if (classno != null && !classno.equals("")) {
				classnoBF.append(",'"+classno+"'");
			}
		}

		String classnolist = "";
		if (!classnoBF.toString().equals("")) {
			classnolist = classnoBF.toString().substring(1);
		}

		de.clearSql();
  		de.addSql("select a.classno,a.classname                                   ");
  		de.addSql("  from odssu.duty_classification a                             ");
  		de.addSql(" where a.orgno = :orgno                                        ");
		if (!classnolist.equals("")) {
  			de.addSql("   and a.classno not in (" + classnolist + ")              ");
		}
  		de.addSql("   and exists                                                  ");
  		de.addSql(" (select * from odssu.roleinfor b where b.classno = a.classno and b.sleepflag = '0') ");
		de.setString("orgno", orgno);
		DataStore tmpds = de.query();

		if (tmpds != null && tmpds.rowCount() != 0) {
			String delclass = "";
			for (int i = 0; i < tmpds.size(); i++) {
				delclass = delclass + "【" + tmpds.getString(i, "classname")
						+ "】";
			}
			this.bizException(delclass + "分类下存在未注销岗位，不允许删除。");
		}

		// 采用先删后插的方式更新岗位分类

		de.clearSql();
		de.addSql(" delete from odssu.duty_classification a where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		de.update();

		// 首先对数据进行校验
		for (int i = 0; i < classds.size(); i++) {
			String classname = classds.getString(i, "classname");
			String classno = classds.getString(i, "classno");

			if (classno == null || classno.equals("")) {
				//classno = DBUtil.getSequence("odssu.idno");
				classno = de.getNextVal("ods_id");
			}
			de.clearSql();
			de.addSql(" insert into odssu.duty_classification ( classno,classname,orgno,sn) ");
			de.addSql("                          values (  :classno ,  :classname , :orgno ,:i)");
			de.setString("classno", classno);
			de.setString("classname", classname);
			de.setString("orgno", orgno);
			de.setInt("i", i);
			de.update();

		}

		DataObject result = DataObject.getInstance();

		return result;
	}

	/**
	 * 保存新增的分类目录
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject saveNewClassification(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String deforgno = para.getString("deforgno");
		String classname = para.getString("classname", "");

		if (classname == null || classname.equals("")) {
			this.bizException("请填写分类名称。");
		}

		String classno ;
		//classno = DBUtil.getSequence("odssu.idno");
		classno = de.getNextVal("ods_id");
  		de.clearSql();
		de.clearSql();
  		de.addSql("  select 1 from odssu.duty_classification a  where a.orgno=:deforgno and a.classname=:classname ");
		de.setString("deforgno", deforgno);
		de.setString("classname", classname);
		DataStore vds = de.query();
		if (vds.rowCount() > 0) {
			this.bizException("该机构下已经存在名称为【" + classname + "】的岗位分类！");
		}

		int sn = 0;
		de.clearSql();
  		de.addSql(" select max(nvl(sn,0)) sn ");
  		de.addSql("   from odssu.duty_classification ");
  		de.addSql("  where orgno = :deforgno ");
  		de.setString("deforgno", deforgno);
		DataStore snvds = de.query();
		if (snvds != null && snvds.rowCount() > 0) {
			sn = snvds.getInt(0, "sn") + 1;
		}
		de.clearSql();
		de.addSql(" insert into odssu.duty_classification ( classno,classname,orgno,sn) ");
		de.addSql("                          values (  :classno ,  :classname , :deforgno ,:sn)");
		de.setString("classno", classno);
		de.setString("classname", classname);
		de.setString("deforgno", deforgno);
		de.setInt("sn", sn);
		de.update();

		DataObject result = DataObject.getInstance();
		result.put("classno", classno);

		return result;

	}

	/**
	 * 方法详述：更新岗位分类名称修改
	 * 
	 * @author fandq
	 * @date 创建时间 2016年9月30日
	 * @since V1.0
	 */
	public DataObject saveEditClassification(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String classname = para.getString("classname");
		String classno = para.getString("classno");

		if (classname == null || classname.equals("")) {
			this.bizException("请填写分类名称后保存。");
		}
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssu.duty_classification a, odssu.duty_classification b ");
  		de.addSql(" where a.orgno = b.orgno ");
  		de.addSql("   and b.classno = :classno ");
  		de.addSql("   and a.classno <> b.classno ");
  		de.addSql("   and a.classname = :classname ");
		de.setString("classno", classno);
		de.setString("classname", classname);
		DataStore vds = de.query();
		if (vds.rowCount() > 0) {
			this.bizException("该机构下已经存在名称为【" + classname + "】的岗位分类！");
		}
		de.clearSql();
		de.addSql(" update odssu.duty_classification set classname = :classname where classno = :classno  ");
		de.setString("classname", classname);
		de.setString("classno", classno);
		de.update();

		return DataObject.getInstance();

	}

	/**
	 * 删除分类目录
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject deleteDutyClassification(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String deforgno = para.getString("deforgno");
		String classno = para.getString("classno");
		de.clearSql();
  		de.addSql("  select 1 from odssu.roleinfor a  where a.deforgno=:deforgno and a.classno=:classno ");
		de.setString("deforgno", deforgno);
		de.setString("classno", classno);
		DataStore vds = de.query();
		if (vds.rowCount() > 0) {
			this.bizException("该岗位分类下存在岗位，无法删除！");
		}
  		de.clearSql();
  		de.addSql(" delete from  odssu.duty_classification where classno= :classno  ");
		de.setString("classno", classno);
		de.update();

		return null;
	}

	/**
	 * 根据inorg获取岗位
	 * 
	 * @author myn
	 * @throws BusinessException
	 * @date
	 */
	public DataObject getdutyAboutInorg(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		// 查询内岗
		DE de = DE.getInstance();

		de.clearSql();
		de.addSql("select '1' zyfw,'innerduty' dutyflag,null dutyno,a.roleno, a.rolename,a.orgno faceorgno, '(本科室)' orgname,deforgno  "); 
		de.addSql("   from odssu.inner_duty a  "); 
		de.addSql("  where a.orgno = :orgno "); 
		de.setString("orgno", orgno);
		DataStore vdsInnerDuty = de.query();

		// 查询外岗
		de.clearSql();
  		de.addSql("  select '1' zyfw,'outerduty' dutyflag,a.dutyno, b.roleno,b.rolename,c.orgno faceorgno,c.orgname,b.deforgno ");
  		de.addSql("    from odssu.outer_duty a,");
  		de.addSql("         odssu.roleinfor b,");
  		de.addSql("         odssu.orginfor   c");
  		de.addSql("   where a.inorgno = :orgno");
  		de.addSql("     and a.roleno = b.roleno");
  		de.addSql("     and a.faceorgno = c.orgno ");
  		de.addSql("     and b.sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore vdsOuterDuty = de.query();
		vdsOuterDuty.combineDatastore(vdsInnerDuty);
		DataStore vds = vdsOuterDuty.clone();

		DataObject vdo = DataObject.getInstance();

		// 判断角色定义机构类型，如果为人社系统则将其作用范围改为全局标志
		for (int i = 0; i < vds.rowCount(); i++) {
			if (!vds.getString(i, "deforgno").equals(orgno)) {
				vds.put(i, "zyfw", "2");
			} 
		}
		vds = MultiSortUtil.multiSortDS(vds, "zyfw:desc,rolename:asc");

		// 获取所有的面向机构
		de.clearSql();
  		de.addSql("  select c.orgno value,c.orgname content");
  		de.addSql("    from odssu.outer_duty a,");
  		de.addSql("         odssu.roleinfor b,");
  		de.addSql("         odssu.orginfor   c");
  		de.addSql("   where a.inorgno = :orgno");
  		de.addSql("     and a.roleno = b.roleno");
  		de.addSql("     and a.faceorgno = c.orgno ");
  		de.addSql("     and b.sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore faceorglist = de.query();

		if (vdsInnerDuty != null && vdsInnerDuty.size() > 0) {
			faceorglist.put(faceorglist.rowCount(), "value", "innerduty");
			faceorglist.put(faceorglist.rowCount() - 1, "content", "(本科室)");
		}

		vdo.put("dutyinfor", vds);
		vdo.put("faceorglist", faceorglist);
		return vdo;
	}

	public DataObject getDutyAboutInorgFaceorg(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		String faceorg = para.getString("faceorg");
		DE de = DE.getInstance();

		DataStore vdsInnerDuty = DataStore.getInstance();
		DataStore vdsOuterDuty = DataStore.getInstance();
		// 查询内岗
		if (faceorg == null || faceorg.equals("")
				|| faceorg.equals("innerduty")) {
			de.clearSql();
  			de.addSql(" select '1' zyfw,'innerduty' dutyflag,null dutyno,a.roleno, a.rolename,a.orgno faceorgno, '(本科室)' orgname,deforgno ");
  			de.addSql("   from odssu.inner_duty a ");
  			de.addSql("  where a.orgno = :orgno ");
			de.setString("orgno", orgno);
			vdsInnerDuty = de.query();

		}

		// 查询外岗
		if (faceorg == null || faceorg.equals("")
				|| !faceorg.equals("innerduty")) {
			de.clearSql();
  			de.addSql("  select '1' zyfw,'outerduty' dutyflag,a.dutyno, b.roleno,b.rolename,c.orgno faceorgno,c.orgname,b.deforgno ");
  			de.addSql("    from odssu.outer_duty a,");
  			de.addSql("         odssu.roleinfor b,");
  			de.addSql("         odssu.orginfor   c");
  			de.addSql("   where a.inorgno = :orgno");
  			de.addSql("     and a.roleno = b.roleno");
  			de.addSql("     and a.faceorgno = c.orgno ");
  			de.addSql("     and b.sleepflag = '0' ");
			if (faceorg != null && !faceorg.equals("")) {
  				de.addSql("     and a.faceorgno = :faceorg ");
			};
			de.setString("orgno", orgno);
			if (faceorg != null && !faceorg.equals("")) {
				de.setString("faceorg", faceorg);
			}
			vdsOuterDuty = de.query();
		}
		vdsInnerDuty.combineDatastore(vdsOuterDuty);
		DataStore vds = vdsInnerDuty.clone();

		DataObject vdo = DataObject.getInstance();

		// 判断角色定义机构类型，如果为人社系统则将其作用范围改为全局标志
		for (int i = 0; i < vds.rowCount(); i++) {
			if (OdssuUtil.isRsxt(vds.getString(i, "deforgno"))) {
				vds.put(i, "zyfw", "2");
			} else if (vds.getString(i, "deforgno").equals(orgno)
					&& (OdssuUtil.isRss(orgno) || OdssuUtil.isRsz(orgno) || OdssuUtil.isRscks(orgno))) {
				vds.put(i, "zyfw", "0");
			}
		}
		vds = MultiSortUtil.multiSortDS(vds, "zyfw:desc,rolename:asc");

		vdo.put("dutyinfor", vds);
		return vdo;
	}


	/**
	 * 打开人社局下内岗岗位
	 * 
	 * @author myn
	 * @date
	 */
	public DataObject loadInnerDutyInfor(DataObject para) throws AppException {
		String roletype = para.getString("roletype");
		String orgno = para.getString("orgno");
		DE de = DE.getInstance();

		if (roletype == null || roletype.isEmpty()) {
			throw new AppException("获取到的roletype为空");
		}

		//本机构定义的内岗
		de.clearSql();
  		de.addSql("select distinct '1' zyfw, a.roleno, a.rolename, a.deforgno ");
  		de.addSql("  from odssu.roleinfor a ");
  		de.addSql(" where a.deforgno = :orgno ");
  		de.addSql("   and a.roletype = :roletype ");
  		de.addSql("   and a.sleepflag = '0' ");
		de.setString("orgno",orgno);
		de.setString("roletype",roletype);
		DataStore vds = de.query();
		
		de.clearSql();
  		de.addSql("select distinct '2' zyfw, a.roleno, a.rolename, a.deforgno ");
  		de.addSql("  from odssu.roleinfor a ");
  		de.addSql(" where a.deforgno in ");
  		de.addSql("       (select b.belongorgno ");
  		de.addSql("          from odssu.ir_org_closure b, odssu.orginfor c, odssu.org_type d ");
  		de.addSql("         where b.belongorgno = c.orgno ");
  		de.addSql("           and c.orgtype = d.typeno ");
  		de.addSql("           and (d.yxdymb = '1' or d.yxdyng = '1') ");
  		de.addSql("           and b.orgno = :orgno ");
  		de.addSql("           and b.belongorgno <> :orgno) ");
  		de.addSql("   and a.roletype = :roletype ");
  		de.addSql("   and a.sleepflag = '0' ");
		de.setString("orgno",orgno);
		de.setString("roletype",roletype);
		DataStore vds2 = de.query();
		vds2.combineDatastore(vds);
		DataStore orgtypeds = vds2.clone();
		// 根据岗位定义机构判断是全局还是专用岗位

		orgtypeds = MultiSortUtil.multiSortDS(orgtypeds, "zyfw:desc,rolename:asc");

		DataObject vdo = DataObject.getInstance();
		vdo.put("innerdutyds", orgtypeds);
		return vdo;
	}

	/**
	 * 打开岗位人员信息
	 * 
	 * @author myn
	 * @date
	 */
	public DataObject openGwryJsp(DataObject para) throws AppException {
		String inorgno = para.getString("inorgno");
		if (inorgno == null || inorgno.trim().isEmpty()) {
			throw new AppException("获取的inorgno为空");
		}

		String dutyflag = para.getString("dutyflag");
		if (dutyflag == null || dutyflag.trim().isEmpty()) {
			throw new AppException("获取的dutyflag为空");
		}
		String dutyno = "";
		if (dutyflag.equals("outerduty")) {

			dutyno = para.getString("dutyno", "");
			if (dutyno == null || dutyno.trim().isEmpty()) {
				throw new AppException("获取的dutyno为空");
			}
		}
		String roleno = para.getString("roleno");
		if (roleno == null || roleno.trim().isEmpty()) {
			throw new AppException("获取的roleno为空");
		}
		// 查询本机构的直属人员
		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql(" select b.empno, b.empname, null own, null rolename,b.loginname username  ");
  		de.addSql("   from odssu.ir_emp_org a, ");
  		de.addSql("        odssu.empinfor b    ");
  		de.addSql("  where a.orgno = :inorgno ");
  		de.addSql("    and a.empno = b.empno  ");
		de.setString("inorgno", inorgno);
		DataStore vds1 = de.query();

		// 根据内外岗 分别查询岗位下的 人员
		DataStore vds2 = DataStore.getInstance();
		if (dutyflag.equals("outerduty")) {
			de.clearSql();
  			de.addSql("   select a.empno");
  			de.addSql("     from odssu.emp_outer_duty_view a ");
  			de.addSql("    where a.dutyno = :dutyno ");
			de.setString("dutyno", dutyno);
			vds2 = de.query();
		} else if (dutyflag.equals("innerduty")) {
			de.clearSql();
  			de.addSql(" select a.empno");
  			de.addSql("     from odssu.emp_inner_duty a");
  			de.addSql("    where a.roleno = :roleno");
  			de.addSql("      and a.orgno = :inorgno ");
			de.setString("roleno", roleno);
			de.setString("inorgno", inorgno);
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

		for (int i = 0; i < vds1.size(); i++) {
			String empnoall = vds1.getString(i, "empno");
			de.clearSql();
  			de.addSql(" select rolename,b.rolesn ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empnoall ");
  			de.addSql("    and a.orgno = :inorgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :rolenature");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :jsgn");
			de.setString("empnoall", empnoall);
			de.setString("inorgno", inorgno);
			de.setString("rolenature", OdssuContants.ROLENATURE_CYJS);
			de.setString("jsgn", OdssuContants.JSGN_POST);
			DataStore roleNameVds = de.query();
			StringBuffer roleNameBF = new StringBuffer();
			int rolesn = 100;
			roleNameBF.append("");
			for (int j = 0; j < roleNameVds.size(); j++) {
				String roleName = roleNameVds.getString(j, "rolename");
				roleNameBF.append(roleName + ",");
				Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
				if (rolesnInt != null && rolesnInt != 0) {
					if (rolesnInt < rolesn) {
						rolesn = rolesnInt;
					}
				}
			}
			if (roleNameBF.length() > 0) {
				roleNameBF.deleteCharAt(roleNameBF.length() - 1);
			}
			vds1.put(i, "rolename", roleNameBF.toString());
			vds1.put(i, "rolesn", rolesn);
		}

		DataObject vdo = DataObject.getInstance();
		vds1 = MultiSortUtil.multiSortDS(vds1, "rolesn:asc,username:asc,own:desc");
		vdo.put("zsry", vds1);
		return vdo;
	}

	/**
	 * 方法详述：获取本业务机构适用的地市外岗岗位分类及对应的启用情况
	 * 
	 * @author fandq
	 * @date 创建时间 2016年9月29日
	 * @since V1.0
	 */
	public DataObject getOverallDutyClassbyOrgno(DataObject para) throws AppException {
		String orgno = para.getString("orgno", "");
		DE de = DE.getInstance();
		DataStore classds = DataStore.getInstance();
		
		//取机构适用的外岗类型
		DataStore roletypeds = OdssuUtil.getOrgtypeAdaptRoletype(OdssuUtil.getOrgTypeByOrgNo(orgno), OdssuContants.JSGN_OUTERDUTY);
		
		String roletype = OdssuUtil.roleTypeVdsToroleTypeString(roletypeds);

		// 取该业务机构对应的业务范畴
		DataStore rsjYwfc = OdssuUtil.getYwfcVdsByOrgno(orgno);

		// 取该机构上级允许定义外岗模板的机构编号
		de.clearSql();
  		de.addSql("select b.belongorgno ");
  		de.addSql("  from odssu.ir_org_closure b, odssu.orginfor c, odssu.org_type d ");
  		de.addSql(" where b.belongorgno = c.orgno ");
  		de.addSql("   and c.orgtype = d.typeno ");
  		de.addSql("   and d.yxdymb = '1' ");
  		de.addSql("   and b.orgno = :orgno ");
		de.setString("orgno",orgno);
		DataStore rsxtOrgds = de.query();
		if (rsxtOrgds == null || rsxtOrgds.rowCount() == 0) {
			return DataObject.getInstance();
		}

		for (int rownum = 0; rownum < rsxtOrgds.rowCount(); rownum++) {
			String rsxtorgno = rsxtOrgds.getString(rownum, "belongorgno");
			// 查询该人社系统下的全局外配岗位
			de.clearSql();
  			de.addSql(" select roleno,rolename  ");
  			de.addSql(" from odssu.roleinfor    ");
  			de.addSql(" where roletype in "+roletype+" ");
  			de.addSql(" and deforgno = :rsxtorgno ");
  			de.addSql(" and sleepflag = '0' ");
			de.setString("rsxtorgno", rsxtorgno);
			DataStore gygwds = de.query();

			// 遍历人社系统下的全局外配岗位，将其业务范畴取出，并与业务机构下的业务范畴比较
			for (int i = 0; i < gygwds.rowCount(); i++) {
				DataStore ywfc = DataStore.getInstance();
				String roleno = gygwds.getString(i, "roleno");

				de.clearSql();
  				de.addSql("select scopeno from odssu.ir_role_business_scope where roleno = :roleno");
				de.setString("roleno", roleno);
				ywfc = de.query();
				// 如果两个业务范畴集合存在交集，则将该岗位的目录放在结果集中
				if (OdssuUtil.getIts(rsjYwfc, ywfc)) {
					de.clearSql();
  					de.addSql("select a.classno, a.classname,a.sn ");
  					de.addSql("  from odssu.duty_classification a, odssu.roleinfor b ");
  					de.addSql(" where a.orgno = :rsxtorgno ");
  					de.addSql("   and b.roleno = :roleno ");
  					de.addSql("   and a.classno = b.classno ");
					de.setString("rsxtorgno", rsxtorgno);
					de.setString("roleno", roleno);
					DataStore dstemp = de.query();
					if (dstemp == null || dstemp.rowCount() == 0) {
						continue;
					}

					String classno = dstemp.getString(0, "classno");
					String classname = dstemp.getString(0, "classname");
					int sn = dstemp.getInt(0, "sn");

					int row = classds.find("classno == " + classno);
					if (row >= 0) {
    						de.clearSql();
    						de.addSql(" select 1 from odssu.outer_duty a where a.roleno = :roleno and a.faceorgno = :orgno ");
						de.setString("roleno", roleno);
						de.setString("orgno", orgno);
						DataStore dutyds = de.query();

						if (dutyds.rowCount() > 0) {
							classds.put(row, "qy", classds.getInt(row, "qy") + 1);
						} else {
							classds.put(row, "wqy", classds.getInt(row, "wqy") + 1);
						}
						continue;
					}
					classds.put(classds.rowCount(), "classno", classno);
					classds.put(classds.rowCount() - 1, "classname", classname);
					classds.put(classds.rowCount() - 1, "sn", sn);
  					de.clearSql();
  					de.addSql(" select 1 from odssu.outer_duty a where a.roleno = :roleno and a.faceorgno = :orgno ");
					de.setString("roleno", roleno);
					de.setString("orgno", orgno);
					DataStore dutyds = de.query();

					if (dutyds.rowCount() > 0) {
						classds.put(classds.rowCount() - 1, "qy", 1);
						classds.put(classds.rowCount() - 1, "wqy", 0);
					} else {
						classds.put(classds.rowCount() - 1, "qy", 0);
						classds.put(classds.rowCount() - 1, "wqy", 1);
					}
				}
			}

		}

		classds.sort("sn");

		DataObject result = DataObject.getInstance();

		result.put("dsywwg", classds);
		return result;
	}

	/**
	 * 方法详述:根据一个机构编号和一个岗位类别编号获取机构下该类别的全地市统一建立的岗位
	 * 
	 * @author fandq
	 * @throws AppException
	 * @date 创建时间 2016年9月29日
	 * @since V1.0
	 */
	public DataObject getOverallDutybyOrgAClass(DataObject para) throws AppException {
		String orgno = para.getString("orgno", "");
		String classno = para.getString("classno", "");
		DE de = DE.getInstance();

		DataStore roletypeds = OdssuUtil.getOrgtypeAdaptRoletype(OdssuUtil.getOrgTypeByOrgNo(orgno), OdssuContants.JSGN_OUTERDUTY);
		String roletype = OdssuUtil.roleTypeVdsToroleTypeString(roletypeds);
		
		DataStore dutyds = DataStore.getInstance();

		// 取该业务机构对应的业务范畴
		DataStore rsjYwfc = OdssuUtil.getYwfcVdsByOrgno(orgno);
		// 取该机构上级的人社系统机构编号
		de.clearSql();
  		de.addSql("select b.belongorgno orgno ");
  		de.addSql("  from odssu.ir_org_closure b, odssu.orginfor c, odssu.org_type d ");
  		de.addSql(" where b.belongorgno = c.orgno ");
  		de.addSql("   and c.orgtype = d.typeno ");
  		de.addSql("   and d.yxdymb = '1' ");
  		de.addSql("   and b.orgno = :orgno ");
		de.setString("orgno",orgno);
		DataStore rsxtOrgds = de.query();
		if (rsxtOrgds != null && rsxtOrgds.size() > 0) {
			String rsxtorgno = OdssuUtil.orgnoVdsToOrgnoString(rsxtOrgds);
			// 查询该人社系统下关于一个目录的全局外配岗位
			de.clearSql();
  			de.addSql(" select roleno,rolename  ");
  			de.addSql(" from odssu.roleinfor    ");
  			de.addSql(" where roletype in "+roletype+" ");
  			de.addSql(" and deforgno in "+rsxtorgno+" ");
  			de.addSql(" and sleepflag = '0' ");
  			de.addSql(" and classno = :classno ");
			de.setString("classno", classno);
			DataStore gygwds = de.query();

			// 遍历人社系统下的全局外配岗位，将其业务范畴取出，并与业务机构下的业务范畴比较
			for (int i = 0; i < gygwds.rowCount(); i++) {
				DataStore ywfc = DataStore.getInstance();
				String roleno = gygwds.getString(i, "roleno");

				de.clearSql();
  				de.addSql("select scopeno from odssu.ir_role_business_scope where roleno = :roleno");
				de.setString("roleno", roleno);
				ywfc = de.query();
				// 如果两个业务范畴集合存在交集，则将该岗位的目录放在结果集中
				if (OdssuUtil.getIts(rsjYwfc, ywfc)) {
					de.clearSql();
  					de.addSql("select a.roleno, a.deforgno, a.rolename, null inorgno, null inorgname,null dutyno ");
  					de.addSql("  from odssu.roleinfor a ");
  					de.addSql(" where a.roleno = :roleno ");
					de.setString("roleno", roleno);
					DataStore dstemp = de.query();
					if (dstemp.rowCount() == 0) {
						continue;
					}
					DataObject dutydo = dstemp.get(0);

					de.clearSql();
  					de.addSql("select a.dutyno, a.inorgno, b.orgname ");
  					de.addSql("   from odssu.outer_duty a, odssu.orginfor b ");
  					de.addSql("  where a.inorgno = b.orgno ");
  					de.addSql("    and a.roleno = :roleno ");
  					de.addSql("    and a.faceorgno = :orgno ");
					de.setString("roleno", roleno);
					de.setString("orgno", orgno);
					DataStore dstmp = de.query();

					if (dstmp != null && dstmp.rowCount() != 0) {
						for (int j = 0; j < dstmp.rowCount(); j++) {
							String dutyno = dstmp.getString(j, "dutyno");
							String inorgno = dstmp.getString(j, "inorgno");
							String orgname = dstmp.getString(j, "orgname");
							
							dutydo.put("dutyno", dutyno);
							dutydo.put("inorgno", inorgno);
							dutydo.put("inorgname", orgname);
							dutyds.addRow(dutydo.clone());
						}
					} else {
						dutydo.put("dutyno", "");
						dutydo.put("inorgno", "");
						dutydo.put("inorgname", "(未启用)");
						dutyds.addRow(dutydo);
					}
				}
			}
		}

		dutyds = MultiSortUtil.multiSortDS(dutyds, "inorgname:desc,rolename:asc");
		DataObject result = DataObject.getInstance();
		result.put("classduty", dutyds);
		return result;
	}


	/**
	 * 打开业务经办机构信息
	 * 
	 * @author myn
	 * @date
	 */
	public DataObject openYwJbjg(final DataObject para) throws Exception {
		String belongorgno = para.getString("orgno");
		String orgtype = para.getString("orgtype");
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();

		de.clearSql();
  		de.addSql("select a.orgno,a.orgname   ");
  		de.addSql("  from odssu.orginfor a    ");
  		de.addSql("   where a.belongorgno = :belongorgno     ");
  		de.addSql("   and a.orgtype = :orgtype    ");
  		de.addSql(" order by a.orgsn,a.orgno ");
		de.setString("belongorgno", belongorgno);
		de.setString("orgtype", orgtype);
		DataStore vds = de.query();
		
		DataStore roletypeds = OdssuUtil.getOrgtypeAdaptRoletype(orgtype, OdssuContants.JSGN_OUTERDUTY);
		String roletype = OdssuUtil.roleTypeVdsToroleTypeString(roletypeds);

		for (int i = 0; i < vds.rowCount(); i++) {
			String orgno = vds.getString(i, "orgno");
			de.clearSql();
  			de.addSql("  select count(1) defdutybnumber from odssu.roleinfor  where deforgno = :orgno and sleepflag = '0'");
  			de.addSql("  and roletype in "+roletype+" ");
			de.setString("orgno", orgno);
			DataStore vds2 = de.query();
			vds.put(i, "defdutybnumber", ((Integer) vds2.getInt(0, "defdutybnumber")).toString());
			Integer usegws = 0;
			Integer allgws = 0;
			// 取该业务机构的业务范畴
			DataStore JgYwfc = OdssuUtil.getYwfcVdsByOrgno(orgno);

			// 取该机构上级的人社系统机构编号
			de.clearSql();
  			de.addSql("select b.belongorgno orgno ");
  			de.addSql("  from odssu.ir_org_closure b, odssu.orginfor c, odssu.org_type d ");
  			de.addSql(" where b.belongorgno = c.orgno ");
  			de.addSql("   and c.orgtype = d.typeno ");
  			de.addSql("   and d.yxdymb = '1' ");
  			de.addSql("   and b.orgno = :orgno ");
			de.setString("orgno",orgno);
			DataStore rsxtOrgds = de.query();
			if (rsxtOrgds != null && rsxtOrgds.size() > 0) {
				String rsxtorgno = OdssuUtil.orgnoVdsToOrgnoString(rsxtOrgds);

				// 查询该人社系统下的岗位
				de.clearSql();
  				de.addSql(" select roleno,rolename  ");
  				de.addSql(" from odssu.roleinfor    ");
  				de.addSql("      where roletype in "+roletype+" ");
  				de.addSql(" and deforgno in "+rsxtorgno+" ");
  				de.addSql(" and sleepflag = '0' ");
				DataStore gwds = de.query();

				// 遍历人社系统下的岗位，将其业务范畴取出，并与业务经办机构的业务范畴比较
				for (int j = 0; j < gwds.rowCount(); j++) {
					DataStore ywfc = DataStore.getInstance();
					String roleno = gwds.getString(j, "roleno");

					de.clearSql();
  					de.addSql("select scopeno from odssu.ir_role_business_scope where roleno = :roleno");
					de.setString("roleno", roleno);
					ywfc = de.query();

					// 如果两个业务范畴集合存在交集，则将该岗位其他信息查出并放于结果集中
					if (OdssuUtil.getIts(JgYwfc, ywfc)) {
						allgws++;
						de.clearSql();
  						de.addSql(" select a.dutyno,a.inorgno,a.faceorgno,b.orgname inorgname  ");
  						de.addSql(" from odssu.outer_duty a ,");
  						de.addSql("      odssu.orginfor b");
  						de.addSql(" where a.roleno = :roleno");
  						de.addSql(" and a.inorgno = b.orgno ");
						de.setString("roleno", roleno);
						DataStore vds3 = de.query();

						// 判断查询出的结果集是否为空,不为空的话将其中的结果进行遍历
						if (vds3.rowCount() != 0) {
							// 遍历结果集
							for (int k = 0; k < vds3.rowCount(); k++) {
								// 判断外岗表中是否存在该业务机构下的该岗位（即该岗位已经在该人社局设置了科室）
								if (orgno.equals(vds3.getString(k, "faceorgno"))
										&& (!vds3.getString(k, "inorgno").trim().equals("") 
										&& vds3.getString(k, "inorgno") != null)) {
									usegws++;
								}
							}
						}
					}
				}
			}
			Integer nousegws = allgws - usegws;
			vds.put(i, "usegws", usegws.toString());
			vds.put(i, "nousegws", nousegws.toString());
		}
		vdo.put("vds", vds);
		return vdo;
	}

	/**
	 * 查询面向机构的外岗
	 * @author fandq
	 * @date
	 */
	public DataObject queryOuterDutyFace(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String qiyong = para.getString("qiyong","false");
		DE de = DE.getInstance();
		
		//取机构适用的外岗类型
		DataStore roletypeds = OdssuUtil.getOrgtypeAdaptRoletype(OdssuUtil.getOrgTypeByOrgNo(orgno), OdssuContants.JSGN_OUTERDUTY);
		
		String roletype = OdssuUtil.roleTypeVdsToroleTypeString(roletypeds);
		
		//本机构自定义的外岗
		de.clearSql();
  		de.addSql("select '1' zyfw, ");
  		de.addSql("       b.dutyno, ");
  		de.addSql("       b.inorgno, ");
  		de.addSql("       a.roleno, ");
  		de.addSql("       a.rolename, ");
  		de.addSql("       c.orgname inorgname, ");
  		de.addSql("       a.deforgno ");
  		de.addSql("  from odssu.roleinfor a, odssu.outer_duty b, odssu.orginfor c ");
  		de.addSql(" where a.roleno = b.roleno ");
  		de.addSql("   and b.inorgno = c.orgno ");
  		de.addSql("   and a.deforgno = :orgno ");
  		de.addSql("   and a.roletype in "+roletype+" ");
		de.setString("orgno",orgno);
		DataStore dswg = de.query();

		// 取该业务机构对应的业务范畴
		DataStore JgYwfc = OdssuUtil.getYwfcVdsByOrgno(orgno);

		// 取该机构上级允许定义外岗模板的机构编号
		de.clearSql();
  		de.addSql("select b.belongorgno ");
  		de.addSql("  from odssu.ir_org_closure b, odssu.orginfor c, odssu.org_type d ");
  		de.addSql(" where b.belongorgno = c.orgno ");
  		de.addSql("   and c.orgtype = d.typeno ");
  		de.addSql("   and d.yxdymb = '1' ");
  		de.addSql("   and b.orgno = :orgno ");
		de.setString("orgno",orgno);
		DataStore rsxtOrgds = de.query();

			for (int row = 0; row < rsxtOrgds.rowCount(); row++) {
				String rsxtorgno = rsxtOrgds.getString(row, "belongorgno");

				// 查询该机构下定义的岗位模板
				de.clearSql();
  				de.addSql(" select roleno,rolename  ");
  				de.addSql(" from odssu.roleinfor    ");
  				de.addSql(" where roletype in "+roletype+" ");
  				de.addSql(" and deforgno = :rsxtorgno ");
  				de.addSql(" and sleepflag = '0' ");
				de.setString("rsxtorgno", rsxtorgno);
				DataStore gygwds = de.query();

				// 遍历岗位模板
				for (int i = 0; i < gygwds.rowCount(); i++) {
					DataStore ywfc = DataStore.getInstance();
					String roleno = gygwds.getString(i, "roleno");
					String rolename = gygwds.getString(i, "rolename");
					String dutyno = "";
					String inorgno = "";
					String inorgname = "";

					de.clearSql();
  					de.addSql("select scopeno from odssu.ir_role_business_scope where roleno = :roleno");
					de.setString("roleno", roleno);
					ywfc = de.query();
					// 如果两个业务范畴集合存在交集，则将该岗位其他信息查出并放于结果集中

					if (OdssuUtil.getIts(JgYwfc, ywfc)) {
						de.clearSql();
  						de.addSql(" select a.dutyno,a.inorgno,a.faceorgno,b.orgname inorgname  ");
  						de.addSql(" from odssu.outer_duty a ,");
  						de.addSql("      odssu.orginfor b");
  						de.addSql(" where a.roleno = :roleno");
  						de.addSql(" and a.inorgno = b.orgno ");
						de.setString("roleno", roleno);
						DataStore vds = de.query();

						// 判断查询出的结果集是否为空,不为空的话将其中的结果进行遍历
						if (vds.rowCount() != 0) {
							// 遍历结果集
							for (int j = 0; j < vds.rowCount(); j++) {
								// 判断外岗表中是否存在该人社局下的该岗位（即该岗位已经在该人社局设置了科室）
								if (orgno.equals(vds.getString(j, "faceorgno"))) {
									dutyno = vds.getString(j, "dutyno");
									inorgno = vds.getString(j, "inorgno");
									inorgname = vds.getString(j, "inorgname");
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
						
						if (qiyong.equals("true")&&inorgname.equals("")) {
							int n = dswg.rowCount();
							dswg.put(n, "zyfw", "2");
							dswg.put(n, "roleno", roleno);
							dswg.put(n, "rolename", rolename);
							dswg.put(n, "inorgno", inorgno);
							dswg.put(n, "inorgname", "(未启用)");
							dswg.put(n, "dutyno", dutyno);
							dswg.put(n, "deforgno", rsxtorgno);
						}
					}
				}
			}

		dswg = MultiSortUtil.multiSortDS(dswg, "zyfw:desc,rolename:asc");

		DataObject result = DataObject.getInstance();
		result.put("dswg", dswg);
		return result;
	}

	/**
	 * 打开业务经办机构下具体机构本局定义信息
	 * 
	 * @author myn
	 * @date
	 */
	public DataObject openYwjgBjdyGw(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql(" select a.classno,a.classname  ");
  		de.addSql(" from odssu.duty_classification a  ");
  		de.addSql(" where a.orgno = :orgno   ");
  		de.addSql(" order by a.sn   ");
		de.setString("orgno", orgno);
		DataStore gwfl = de.query();
		DataStore ds = DataStore.getInstance(gwfl);
		int currentRow = 0;

		for (int i = 0; i < gwfl.rowCount(); i++) {
			String classno = gwfl.getString(i, "classno");
			de.clearSql();
			// 查询classno下的duty

			de.addSql(" select a.dutyno,a.inorgno,a.roleno,b.rolename,d.orgname inorgname ");
  			de.addSql(" from odssu.outer_duty a,odssu.roleinfor b,                                 ");
  			de.addSql("      odssu.orginfor d                                    ");
  			de.addSql(" where a.inorgno = d.orgno                                                  ");
  			de.addSql(" and a.roleno = b.roleno                                                    ");
  			de.addSql(" and b.sleepflag = '0'                                                    ");
  			de.addSql(" and b.deforgno = a.faceorgno                                                   ");
  			de.addSql(" and a.faceorgno = :orgno   and b.classno = :classno                ");
			de.setString("orgno", orgno);
			de.setString("classno", classno);
			DataStore tmpResult = de.query();
			if (i == 0) {
				currentRow = 0;
			} else {
				currentRow++;
			}
			for (int j = 0; j < tmpResult.rowCount(); j++) {
				currentRow++;
				if (currentRow > ds.rowCount()) {
					ds.addRow(tmpResult.get(j));
				} else {
					ds.insertRow(currentRow, tmpResult.get(j));
				}
			}
		}
		DataObject unGroupedTitle = DataObject.getInstance();
		unGroupedTitle.put("classno", "");
		String title = "未分组";
		unGroupedTitle.put("classname", title);
		unGroupedTitle.put("dutyno", "");
		unGroupedTitle.put("inorgno", "");
		unGroupedTitle.put("roleno", "");
		unGroupedTitle.put("rolename", "");
		unGroupedTitle.put("inorgname", "");
		currentRow = currentRow + 1;
		ds.addRow(unGroupedTitle);

		de.clearSql();
  		de.addSql(" select a.dutyno,a.inorgno,a.roleno,b.rolename,d.orgname inorgname ");
  		de.addSql(" from odssu.outer_duty a,odssu.roleinfor b,                                 ");
  		de.addSql("      odssu.orginfor d                                     ");
  		de.addSql(" where a.inorgno = d.orgno                                                  ");
  		de.addSql(" and a.roleno = b.roleno                                                    ");
  		de.addSql(" and b.sleepflag = '0'                                                    ");
  		de.addSql(" and b.deforgno = a.faceorgno                                                   ");
  		de.addSql(" and a.faceorgno = :orgno    ");
  		de.addSql(" and b.classno is null  ");
		de.setString("orgno", orgno);
		DataStore tmpResult2 = de.query();
		for (int j = 0; j < tmpResult2.rowCount(); j++) {
			ds.addRow(tmpResult2.get(j));
		}

		DataObject result = DataObject.getInstance();
		result.put("gwfl", ds);
		return result;
	}

	/**
	 * 打开业务经办机构下具体机构本局定义的分类
	 * 
	 * @author myn
	 * @date
	 */
	public DataObject openYwjgBjClassification(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String classno = para.getString("classno", "");
		DE de = DE.getInstance();
		DataStore tmpResult = DataStore.getInstance();
		if (classno == null || classno.equals("")) {
			de.clearSql();
  			de.addSql(" select a.dutyno,a.inorgno,a.roleno,b.rolename,d.orgname inorgname,b.deforgno ");
  			de.addSql(" from odssu.outer_duty a,odssu.roleinfor b,                                 ");
  			de.addSql("      odssu.orginfor d                                     ");
  			de.addSql(" where a.inorgno = d.orgno                                                  ");
  			de.addSql(" and a.roleno = b.roleno                                                    ");
  			de.addSql(" and b.sleepflag = '0'                                                    ");
  			de.addSql(" and b.deforgno = a.faceorgno                                                   ");
  			de.addSql(" and a.faceorgno = :orgno   and b.classno is null               ");
  			de.addSql(" order by  b.rolename                 ");
			de.setString("orgno", orgno);
			tmpResult = de.query();
		} else {
			de.clearSql();
  			de.addSql(" select a.dutyno,a.inorgno,a.roleno,b.rolename,d.orgname inorgname,b.deforgno ");
  			de.addSql(" from odssu.outer_duty a,odssu.roleinfor b,                                 ");
  			de.addSql("      odssu.orginfor d                                    ");
  			de.addSql(" where a.inorgno = d.orgno                                                  ");
  			de.addSql(" and a.roleno = b.roleno                                                    ");
  			de.addSql(" and b.sleepflag = '0'                                                    ");
  			de.addSql(" and b.deforgno = a.faceorgno                                                   ");
  			de.addSql(" and a.faceorgno = :orgno   and b.classno=:classno                ");
  			de.addSql(" order by  b.rolename                 ");
			de.setString("orgno", orgno);
			de.setString("classno", classno);
			tmpResult = de.query();
		}

		DataObject result = DataObject.getInstance();
		result.put("classduty", tmpResult);
		return result;
	}

	/**
	 * 方法详述:获取机构下外岗分类
	 * 
	 * @author fandq
	 * @date 创建时间 2016年9月30日
	 * @since V1.0
	 */
	public DataObject getClassificationByOrg(DataObject para) throws AppException {
		DE de = DE.getInstance();
		String orgno = para.getString("orgno");
		de.clearSql();
    	de.addSql("  select  * from odssu.duty_classification a  where a.orgno=:orgno order by sn,classname ");
		de.setString("orgno", orgno);
		DataStore ds = de.query();
		for (int i = 0; i < ds.rowCount(); i++) {
			String classno = ds.getString(i, "classno");
			de.clearSql();
  			de.addSql("  select count(1) classnumber from odssu.roleinfor  where deforgno=:orgno and classno=:classno and sleepflag = '0' ");
			de.setString("orgno", orgno);
			de.setString("classno", classno);
			DataStore vds = de.query();
			ds.put(i, "classnumber", ((Integer) vds.getInt(0, "classnumber")).toString());
		}

		DataObject result = DataObject.getInstance();
		result.put("classds", ds);
		return result;
	}

	public DataObject getClassification(DataObject para) throws AppException {
		String classno = para.getString("classno");
		DE de = DE.getInstance();

  		de.clearSql();
  		de.addSql("select * from odssu.duty_classification a where a.classno = :classno ");
		de.setString("classno", classno);
		DataStore classds = de.query();

		DataObject result = DataObject.getInstance();
		result.put("classds", classds);
		return result;
	}

	public DataObject moveClassDown(DataObject para) throws AppException, BusinessException {

		String classno = para.getString("classno");
		String orgno = para.getString("orgno");
		if (null == classno || "".equals(classno)) {
			throw new AppException("排序出错，传入的岗位分类编号为空。");
		}
		if (null == orgno || "".equals(orgno)) {
			throw new AppException("排序出错，传入的机构编号为空。");
		}
		// 对该机构下的分类进行重新排序
		sortClass(orgno);
		DE de = DE.getInstance();
    	de.clearSql();
    	de.addSql("select * from odssu.duty_classification a where a.classno = :classno ");
		de.setString("classno", classno);
		DataStore classds = de.query();

		int sn = classds.getInt(0, "sn");

		// 校验当前是否为最后一个

		de.clearSql();
		de.addSql("select max(a.sn) sn from odssu.duty_classification  a where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tmpdsSn = de.query();
		if (null == tmpdsSn || tmpdsSn.rowCount() == 0) {
			throw new AppException("未查询到当前机构下面的岗位分类顺序信息。");
		}
		int maxSn = tmpdsSn.getInt(0, "sn");
		if (maxSn == sn) {
			this.bizException("该分类已经是最下面的节点，不能再下移。");
		}

		int newsn = sn + 1;
		while (true) {
  			de.clearSql();
  			de.addSql("select 1 from odssu.duty_classification a where a.sn = :newsn ");
			de.setInt("newsn", newsn);
			DataStore tmpds = de.query();

			if (tmpds != null && tmpds.rowCount() != 0) {
  				de.clearSql();
  				de.addSql("update odssu.duty_classification a set a.sn = :newsn where a.classno = :classno ");
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();
				de.clearSql();
				de.addSql("update odssu.duty_classification a set sn = :sn where a.orgno = :orgno and a.sn = :newsn and a.classno <> :classno ");
				de.setInt("sn", sn);
				de.setString("orgno", orgno);
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();
				break;
			}
			newsn = newsn + 1;
		}

		DataObject result = DataObject.getInstance();
		result.put("result_msg", "true");
		return result;
	}

	public DataObject moveClassBottom(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String classno = para.getString("classno");
		String orgno = para.getString("orgno");
		if (null == classno || "".equals(classno)) {
			throw new AppException("排序出错，传入的岗位分类编号为空。");
		}
		if (null == orgno || "".equals(orgno)) {
			throw new AppException("排序出错，传入的机构编号为空。");
		}
		// 对该机构下的分类进行重新排序
		sortClass(orgno);
		de.clearSql();
		de.addSql("select * from odssu.duty_classification a where a.classno = :classno ");
		de.setString("classno", classno);
		DataStore classds = de.query();

		int sn = classds.getInt(0, "sn");

		// 校验当前是否为最后一个

		de.clearSql();
		de.addSql("select max(a.sn) sn from odssu.duty_classification  a where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tmpdsSn = de.query();
		if (null == tmpdsSn || tmpdsSn.rowCount() == 0) {
			throw new AppException("未查询到当前机构下面的岗位分类顺序信息。");
		}
		int maxSn = tmpdsSn.getInt(0, "sn");
		if (maxSn == sn) {
			this.bizException("该分类已经是最下面的节点，不能再下移。");
		}

		int newsn = sn + 1;
		while (true) {
  			de.clearSql();
  			de.addSql("select 1 from odssu.duty_classification a where a.sn = :newsn ");
			de.setInt("newsn", newsn);
			DataStore tmpds = de.query();

			if (tmpds != null && tmpds.rowCount() != 0) {
  				de.clearSql();
  				de.addSql("update odssu.duty_classification a set sn = :newsn where a.classno = :classno ");
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();
				de.clearSql();
				de.addSql("update odssu.duty_classification a set sn = :sn where a.orgno = :orgno and a.sn = :newsn and a.classno <> :classno ");
				de.setInt("sn", sn);
				de.setString("orgno", orgno);
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();

				de.commit();
			}

			if (maxSn == newsn) {
				break;
			}

			newsn = newsn + 1;
		}

		DataObject result = DataObject.getInstance();
		result.put("result_msg", "true");
		return result;
	}

	public DataObject moveClassUp(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String classno = para.getString("classno");
		String orgno = para.getString("orgno");
		if (null == classno || "".equals(classno)) {
			throw new AppException("排序出错，传入的岗位分类编号为空。");
		}
		if (null == orgno || "".equals(orgno)) {
			throw new AppException("排序出错，传入的机构编号为空。");
		}
		// 对该机构下的分类进行重新排序
		sortClass(orgno);
    		de.clearSql();
    		de.addSql("select * from odssu.duty_classification a where a.classno = :classno ");
		de.setString("classno", classno);
		DataStore classds = de.query();

		int sn = classds.getInt(0, "sn");

		// 校验当前是否为最前面一个

		de.clearSql();
		de.addSql("select min(a.sn) sn from odssu.duty_classification  a where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tmpdsSn = de.query();
		if (null == tmpdsSn || tmpdsSn.rowCount() == 0) {
			throw new AppException("未查询到当前机构下面的岗位分类顺序信息。");
		}
		int minSn = tmpdsSn.getInt(0, "sn");
		if (minSn == sn) {
			this.bizException("该分类已经是最前面的节点，不能再上移。");
		}

		int newsn = sn - 1;
		while (true) {
  			de.clearSql();
  			de.addSql("select 1 from odssu.duty_classification a where a.sn = :newsn ");
			de.setInt("newsn", newsn);
			DataStore tmpds = de.query();

			if (tmpds != null && tmpds.rowCount() != 0) {
  				de.clearSql();
  				de.addSql("update odssu.duty_classification a set sn = :newsn where a.classno = :classno ");
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();
				de.clearSql();
				de.addSql("update odssu.duty_classification a set sn = :sn where a.orgno = :orgno and a.sn = :newsn and a.classno <> :classno ");
				de.setInt("sn", sn);
				de.setString("orgno", orgno);
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();
				break;
			}
			newsn = newsn - 1;
		}

		DataObject result = DataObject.getInstance();
		result.put("result_msg", "true");
		return result;
	}

	public DataObject moveClassTop(DataObject para) throws AppException, BusinessException {
		DE de = DE.getInstance();
		String classno = para.getString("classno");
		String orgno = para.getString("orgno");
		if (null == classno || "".equals(classno)) {
			throw new AppException("排序出错，传入的岗位分类编号为空。");
		}
		if (null == orgno || "".equals(orgno)) {
			throw new AppException("排序出错，传入的机构编号为空。");
		}
		// 对该机构下的分类进行重新排序
		sortClass(orgno);
		de.clearSql();
		de.addSql("select * from odssu.duty_classification a where a.classno = :classno ");
		de.setString("classno", classno);
		DataStore classds = de.query();

		int sn = classds.getInt(0, "sn");

		// 校验当前是否为最前面一个

		de.clearSql();
		de.addSql("select min(a.sn) sn from odssu.duty_classification  a where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tmpdsSn = de.query();
		if (null == tmpdsSn || tmpdsSn.rowCount() == 0) {
			throw new AppException("未查询到当前机构下面的岗位分类顺序信息。");
		}
		int minSn = tmpdsSn.getInt(0, "sn");
		if (minSn == sn) {
			this.bizException("该分类已经是最前面的节点，不能再上移。");
		}

		int newsn = sn - 1;
		while (true) {
  			de.clearSql();
  			de.addSql("select 1 from odssu.duty_classification a where a.sn = :newsn ");
			de.setInt("newsn", newsn);
			DataStore tmpds = de.query();

			if (tmpds != null && tmpds.rowCount() != 0) {
  				de.clearSql();
  				de.addSql("update odssu.duty_classification a set sn = :newsn where a.classno = :classno ");
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();
				de.clearSql();
				de.addSql("update odssu.duty_classification a set sn = :sn where a.orgno = :orgno and a.sn = :newsn and a.classno <> :classno ");
				de.setInt("sn", sn);
				de.setString("orgno", orgno);
				de.setInt("newsn", newsn);
				de.setString("classno", classno);
				de.update();

				de.commit();
			}

			if (newsn == minSn) {
				break;
			}

			newsn = newsn - 1;
		}

		DataObject result = DataObject.getInstance();
		result.put("result_msg", "true");
		return result;
	}

	private void sortClass(String orgno) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select distinct a.classno,a.classname,a.sn ");
  		de.addSql("  from odssu.duty_classification a, odssu.duty_classification b ");
  		de.addSql(" where a.classno <> b.classno ");
  		de.addSql("   and a.sn = b.sn ");
  		de.addSql("   and a.orgno = :orgno ");
  		de.addSql("   and a.orgno = b.orgno ");
  		de.addSql("order by a.sn,a.classname ");
		de.setString("orgno", orgno);
		DataStore dstemp = de.query();
		if (dstemp == null || dstemp.rowCount() == 0) {
			return;
		}
		String classno = dstemp.getString(0, "classno");
		int sn = dstemp.getInt(0, "sn");
		de.clearSql();
		de.addSql("update odssu.duty_classification a set sn = sn+1 where a.classno <> :classno and a.sn >=:sn and a.orgno = :orgno  ");
		de.setString("classno", classno);
		de.setInt("sn", sn);
		de.setString("orgno", orgno);
		de.update();

		de.commit();

		sortClass(orgno);
	}
}
