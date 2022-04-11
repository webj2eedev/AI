package com.dw.odssu.ws.emp.SyncUact;



import java.text.SimpleDateFormat;
import java.util.*;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.util.DateUtil;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.ASOException;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.CurrentUser;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.ASO;
import com.dareway.framework.workFlow.BPO;
import com.dareway.spring.SpringBeanUtil;
import com.dw.odssu.acc.emp.ryjbxxxz.aso.EmpAddASO;
import com.dw.odssu.ws.emp.ryjbxxxzkjb.HttpJSONFrowardClient;
import com.dw.odssu.ws.emp.ryjbxxxzkjb.UactServiceConsumer;
import com.dw.util.OdssuUtil;

import cn.hsa.cep.auc.dto.SysOrguntDTO;
import cn.hsa.cep.auc.dto.SysUactDTO;
import cn.hsa.cep.auc.dto.UserSearchDTO;
import cn.hsa.cep.auc.login.dto.UactDTO;
import cn.hsa.cep.auc.login.dto.UactQueryDTO;
import cn.hsa.cep.auc.login.service.UserService;
import cn.hsa.cep.auc.org.dto.OrguntQueryDTO;
import cn.hsa.cep.auc.org.dto.UnitDTO;
import cn.hsa.cep.auc.org.service.UnitService;
import cn.hsa.cep.auc.service.OrguntService;
import cn.hsa.cep.auc.service.SysUactService;
import cn.hsa.hsaf.core.framework.util.PageResult;
import cn.hsa.hsaf.core.framework.web.WrapperResponse;

public class SyncUactBPO extends BPO {

	public DataObject queryAddUactInfor(DataObject para) throws AppException, BusinessException {


		DataStore xzqhdmds = this.queryUserXZQHDM();


		DataStore uactds = this.queryUserPage(xzqhdmds).getDataStore("empds");

		de.clearSql();
		de.addSql("select uactid ");
		de.addSql("  from odssu.empinfor ");
		de.addSql("  where uactid is not null ");
		DataStore vds = this.de.query();

		Map hsmap = new HashMap(vds.rowCount());
		for (int i = 0; i < vds.rowCount(); i++) {
			hsmap.put(vds.getString(i, "uactid"), 0);
		}

		DataStore adduactds = DataStore.getInstance();
		for (int j = 0; j < uactds.rowCount(); j++) {

			if (!hsmap.containsKey(uactds.getString(j, "uactid"))) {
				DataObject uactdo = DataObject.getInstance();
				uactdo.put("uactid", uactds.getString(j, "uactid"));
				uactdo.put("username", uactds.getString(j, "username"));
				uactdo.put("certno", uactds.getString(j, "certno"));
				uactdo.put("uact", uactds.getString(j, "uact"));
				uactdo.put("orgname", uactds.getString(j, "orgname"));
				uactdo.put("orgcodg", uactds.getString(j, "orgcodg"));
				uactdo.put("orguntId", uactds.getString(j, "orguntId"));
				uactdo.put("admdvs", uactds.getString(j, "admdvs"));
				uactdo.put("tel", uactds.getString(j, "tel"));
				uactdo.put("mob", uactds.getString(j, "mob"));
				uactdo.put("email", uactds.getString(j, "email"));

				adduactds.addRow(uactdo);
			}
		}

		DataObject result = DataObject.getInstance();
		result.put("uactds", adduactds);
		return result;
	}

	public final DataObject queryUserPage(DataStore para) throws AppException {
		String dbid = OdssuNames.DBID;
		//String dbid = "189";
		//当前的dbid对应的根节点
		if ("178".equals(dbid)) {
			//371499
			UserService user = (UserService) SpringBeanUtil.getBean("userService");
			DataObject vdo = DataObject.getInstance();
			DataStore vds = DataStore.getInstance();
			//当前dbid

			for (int j = 0; j < para.rowCount(); j++) {
				int i = 0;
				DataStore ds = DataStore.getInstance();
//			UactQueryDTO userdto = new UactQueryDTO();
//			userdto.setAdmdvs(para.getString(j, "xzqhdm"));			
//			WrapperResponse<PageResult<UactDTO>> result =  user.queryUserPage(userdto);


				SysUactService sysUactService = (SysUactService) SpringBeanUtil.getBean(SysUactService.class);
				UserSearchDTO userSearchDTO = new UserSearchDTO();
				userSearchDTO.setAdmdvs(para.getString(j, "xzqhdm"));
				WrapperResponse<List<SysUactDTO>> result = sysUactService.getUsersBySearch(userSearchDTO);
				String type = result.getType();
				if (type.equals("success")) {
					List<SysUactDTO> listSysUactDTO = result.getData();
					for (SysUactDTO usera : listSysUactDTO) {
						String uactId = usera.getUactId();
						String userName = usera.getUserName();
						String email = usera.getEmail();
						String orgname = usera.getOrgName();
						String certno = usera.getCertNO();
						String tel = usera.getTel();
						String mob = usera.getMob();
						String uact = usera.getUact();
						String orgCodg = usera.getOrgCodg();
						//隶属机构
						String orguntId = usera.getOrguntId();
						//行政区划
						String admdvs = usera.getAdmdvs();

						ds.put(i, "uactid", uactId);
						ds.put(i, "username", userName);
						ds.put(i, "email", email);
						ds.put(i, "orgname", orgname);
						ds.put(i, "certno", certno);
						ds.put(i, "tel", tel);
						ds.put(i, "mob", mob);
						ds.put(i, "uact", uact);
						ds.put(i, "orgcodg", orgCodg);
						ds.put(i, "orguntId", orguntId);
						ds.put(i, "admdvs", admdvs);
						++i;
					}
				}

				vds.combineDatastore(ds);
			}
			vdo.put("empds", vds);
			return vdo;
		}
		if ("189".equals(dbid) || OdssuNames.nova_fsjz) {
			DataObject vdo = DataObject.getInstance();
			DataStore vds = DataStore.getInstance();
			for (int j = 0; j < para.rowCount(); j++) {
				int i = 0;
				DataStore ds = DataStore.getInstance();
				String xzqhdm = para.getString(j, "xzqhdm");
				try {
					JSONObject jsb = new JSONObject();
					jsb.put("admdvs", xzqhdm);
					DataObject empdo = HttpJSONFrowardClient.invokeService("sysUactService", "getUsersBySearch", jsb.toString());
					if (!empdo.get("code").equals(0)){
						throw new AppException("调用getUsersBySearch接口同步门户系统操作员报错！行政区划代码:"+xzqhdm+"code:"+empdo.get("code") + "错误信息:"+empdo.get("message"));
					}
					if (empdo.get("type").equals("success")) {

						DataStore tsds = empdo.getDataStore("data");
						for (int k = 0; k < tsds.rowCount(); k++) {
							ds.put(k, "uactid", tsds.getString(k, "uactId"));
							ds.put(k, "username", tsds.getString(k, "userName"));
							ds.put(k, "email", tsds.getString(k, "email"));
							ds.put(k, "orgname", tsds.getString(k, "orgname"));
							ds.put(k, "certno", tsds.getString(k, "certno"));
							ds.put(k, "tel", tsds.getString(k, "tel"));
							ds.put(k, "mob", tsds.getString(k, "mob"));
							ds.put(k, "uact", tsds.getString(k, "uact"));
							ds.put(k, "orgcodg", tsds.getString(k, "orgCodg"));
							ds.put(k, "orguntId", tsds.getString(k, "orguntId"));//隶属机构
							ds.put(k, "admdvs", tsds.getString(k, "admdvs"));//行政区划
						}
					}

					vds.combineDatastore(ds);
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			}
			vdo.put("empds", vds);
			return vdo;
		}
		return null;
	}


	public final DataStore queryUserXZQHDM() throws AppException {
		CurrentUser cu = this.getUser();
		String userid = cu.getUserid();
		de.clearSql();
		de.addSql("select distinct  o.xzqhdm ");
		de.addSql("  from odssu.ir_emp_org_all_role e, ");
		de.addSql("       odssu.roleinfor           r, ");
		de.addSql("       odssu.orginfor            o, ");
		de.addSql("       odssu.ir_org_closure      i ");
		de.addSql(" where e.empno = :empno");
		de.addSql("   and e.roleno = r.roleno");
		de.addSql("   and o.sleepflag = '0'");
		de.addSql("   and r.sleepflag = '0'");
		de.addSql("   and e.orgno = i.belongorgno");
		de.addSql("   and i.orgno = o.orgno");
		de.addSql("   and o.orgtype in ('YB_DSYBJ', 'YB_QXYBJ', 'YB_SYBJ','YB_DSYBXT','YB_SZYBXT','YB_SYBXT')");
		de.addSql(" and o.xzqhdm is not null ");
		de.addSql(" union ");
		de.addSql(" select distinct u.admdvs xzqhdm  ");
		de.addSql(" from odssu.uact_admdvs u, ");
		de.addSql(" odssu.ir_emp_org_all_role eor  ");
		de.addSql(" where eor.empno = :empno");
		de.addSql(" and eor.orgno = u.orgno ");
		de.setString("empno", userid);
		DataStore ds = de.query();

//		DataStore vds = DataStore.getInstance();
//		vds.put(0, "xzqhdm" ,"370800");
//		return vds;
		return ds;
	}

	/****************************************
	 * 新增人员需要客户化
	 * 1隶属机构校验（国标机构）
	 * 2	  
	 *
	 * ************************************************/
	public DataObject syncUactInfor(DataObject para) throws AppException, BusinessException {

		DataStore uactds = para.getDataStore("uactgrid");

		DataStore errds = DataStore.getInstance();
		DataStore rightds = DataStore.getInstance();
		for (int i = 0; i < uactds.rowCount(); i++) {
			String empno = uactds.getString(i, "uact");
			String loginname = uactds.getString(i, "uact");
			String empname = uactds.getString(i, "username");
			String idcardno = uactds.getString(i, "certno");
			String orguntId = uactds.getString(i, "orguntId");
			String gender = "1";
			String officetel = uactds.getString(i, "tel");
			String mphone = uactds.getString(i, "mob");
			String email = uactds.getString(i, "email");
			String orgname = uactds.getString(i, "orgname");
			String uactid = uactds.getString(i, "uactid");
			String admdvs = uactds.getString(i, "admdvs");


			try {
				//计算操作员的隶属机构
				de.clearSql();
				de.addSql("select orgno from odssu.orginfor where xzqhdm = :admdvs and orgtype in ('YB_DSYBJ', 'YB_QXYBJ', 'YB_SYBJ','YB_DSYBXT','YB_SZYBXT','YB_SYBXT')");
				de.addSql("union ");
				de.addSql("select u.orgno from odssu.uact_admdvs u where u.admdvs = :admdvs");
				de.setString("admdvs", admdvs);
				DataStore ds = de.query();
				if (ds == null || ds.size() == 0) {
					throw new ASOException("根据操作员行政区划未获取到对应的医保局");
				}
				String belongorgno = ds.getString(0, "orgno");

				DataStore emp_org = DataStore.getInstance();
				emp_org.put(0, "empno", empno);
				emp_org.put(0, "orgno", belongorgno);

				DataObject paravdo = DataObject.getInstance();
				paravdo.put("pjbh", "Service");
				paravdo.put("pdid", "ryjbxxxzkjb");
				paravdo.put("objectid", "ODS.NewEmp:idcardno=;biz=ORGROOT");
				paravdo.put("userid", "ADMIN");
				paravdo.put("empno", empno);
				paravdo.put("empname", empname);
				paravdo.put("rname", empname);
				paravdo.put("idcardno", idcardno);
				paravdo.put("hrbelong", belongorgno);
				paravdo.put("gender", gender);
				paravdo.put("officetel", officetel);
				paravdo.put("mphone", mphone);
				paravdo.put("email", email);
				paravdo.put("loginname", loginname);
				paravdo.put("empcreatedate", new Date());
				paravdo.put("emp_org", emp_org);
				paravdo.put("orguntId", orguntId);
				paravdo.put("uactid", uactid);
				paravdo.put("uact", loginname);
				paravdo.put("uactusername", empname);
				DataObject vdo = DataObject.getInstance();
				//调用新增人员ASO

				ASO i_EmpAddASO = this.newASO(EmpAddASO.class);
				i_EmpAddASO.doEntry(paravdo);
				DataObject rightdo = DataObject.getInstance();

				rightdo.put("uact", loginname);
				rightdo.put("username", empname);
				rightdo.put("orgname", orgname);
				rightdo.put("orguntId", belongorgno);
				rightdo.put("relmsg", "成功");
				rightds.addRow(rightdo);
				de.commit();
			} catch (ASOException e) {
				DataObject errdo = DataObject.getInstance();
				//throw new AppException("同步国标操作员【"+empno+"】时出错！"+e.getMessage());
				de.rollback();
				errdo.put("uact", loginname);
				errdo.put("username", empname);
				errdo.put("orgname", orgname);
				errdo.put("orguntId", orguntId);
				errdo.put("errmsg", e.getMessage());
				errds.addRow(errdo);
			}
		}
		DataObject result = DataObject.getInstance();
		result.put("errds", errds);
		result.put("rightds", rightds);
		return result;
	}


	public DataObject queryAddUactOrgInfor(DataObject para) throws AppException, BusinessException {
		String dbid = OdssuNames.DBID;
		//当前的dbid对应的根节点
		if ("178".equals(dbid)) {
			DataStore orgds = DataStore.getInstance();
			UnitService unit = (UnitService) SpringBeanUtil.getBean(UnitService.class);
			OrguntService orguntService = (OrguntService) SpringBeanUtil.getBean(OrguntService.class);
			DataStore xzqhdmds = this.queryUserXZQHDM();
			for (int j = 0; j < xzqhdmds.rowCount(); j++) {
				String admdvs = xzqhdmds.getString(j, "xzqhdm");
				//根据入参行政区划查询机构列表
				OrguntQueryDTO orguntQueryDTO = new OrguntQueryDTO();
				orguntQueryDTO.setAdmdvs(admdvs);
				WrapperResponse<List<UnitDTO>> result = unit.queryOrguntByCond(orguntQueryDTO);

				String type = result.getType();
				if (type.equals("success")) {

					List<UnitDTO> unitDTOList = result.getData();
					for (int i = 0; i < unitDTOList.size(); i++) {
						UnitDTO unitDTO = unitDTOList.get(i);
						String orguntid = unitDTO.getOrguntId();
						de.clearSql();
						de.addSql("select orguntid from odssu.gb_orginfor where admdvs = :admdvs and orguntid = :orguntid ");
						de.setString("admdvs", admdvs);
						de.setString("orguntid", orguntid);
						DataStore ds = de.query();

						if (ds == null || ds.rowCount() == 0) {
							WrapperResponse<SysOrguntDTO> orgn = orguntService.queryOrguntByOrguntId(orguntid);
							if (orgn.getType().equals("success")) {
								SysOrguntDTO sysOrguntDTO = orgn.getData();
								String rid = sysOrguntDTO.getRid();
								String orguntId = sysOrguntDTO.getOrguntId();
								String orgCodg = sysOrguntDTO.getOrgCodg();
								String orgName = sysOrguntDTO.getOrgName();
								//String admdvs = sysOrguntDTO.getAdmdvs();
								String orgTypeCode = sysOrguntDTO.getOrgTypeCode();
								String valiFlag = sysOrguntDTO.getValiFlag();
								String abbr = sysOrguntDTO.getAbbr();
								Date crteTime = sysOrguntDTO.getCrteTime();
								String crteOptinsNo = sysOrguntDTO.getCrteOptinsNo();
								String optinsNo = sysOrguntDTO.getOptinsNo();
								String orgRltsId = sysOrguntDTO.getOrgRltsId();
								String prntOrgId = sysOrguntDTO.getPrntOrgId();
								String prntPath = sysOrguntDTO.getPrntPath();
								String leafnodFlag = sysOrguntDTO.getLeafnodFlag();
								int lv = sysOrguntDTO.getLv();
								int seq = sysOrguntDTO.getSeq();

								DataObject paravdo = DataObject.getInstance();
								paravdo.put("rid", rid);
								paravdo.put("orguntId", orguntId);
								paravdo.put("orgCodg", orgCodg);
								paravdo.put("orgName", orgName);
								paravdo.put("admdvs", admdvs);
								paravdo.put("orgTypeCode", orgTypeCode);
								paravdo.put("valiFlag", valiFlag);
								paravdo.put("abbr", abbr);
								paravdo.put("crteTime", crteTime);
								paravdo.put("crteOptinsNo", crteOptinsNo);
								paravdo.put("optinsNo", optinsNo);
								paravdo.put("orgRltsId", orgRltsId);
								paravdo.put("prntOrgId", prntOrgId);
								paravdo.put("prntPath", prntPath);
								paravdo.put("leafnodFlag", leafnodFlag);
								paravdo.put("lv", lv);
								paravdo.put("seq", seq);

								orgds.addRow(paravdo);
							}
						}
					}
				}
			}
			DataObject vdo = DataObject.getInstance();
			vdo.put("orgds", orgds);
			return vdo;
		}
		if ("189".equals(dbid) || OdssuNames.nova_fsjz) {
			DataStore orgds = DataStore.getInstance();
			UnitService unit = (UnitService) SpringBeanUtil.getBean(UnitService.class);
			OrguntService orguntService = (OrguntService) SpringBeanUtil.getBean(OrguntService.class);
			DataStore xzqhdmds = this.queryUserXZQHDM();
			for (int j = 0; j < xzqhdmds.rowCount(); j++) {
				String admdvs = xzqhdmds.getString(j, "xzqhdm");
				//根据入参行政区划查询机构列表
				JSONObject jsb = new JSONObject();
				jsb.put("admdvs", admdvs);
				DataObject result = HttpJSONFrowardClient.invokeService("UnitService", "queryOrguntByCond", jsb.toString());
				if (!result.get("code").equals(0)){
					throw new AppException("调用queryOrguntByCond接口同步门户系统机构报错！行政区划代码:"+admdvs+"code:"+result.get("code") + "错误信息:"+result.get("message"));
				}
				if (result.get("type").equals("success")) {
					DataStore tds = result.getDataStore("data");
					for (int i = 0; i < tds.rowCount(); i++) {
						String orguntid = tds.getString(i, "orguntid");
						de.clearSql();
						de.addSql("select orguntid from odssu.gb_orginfor where admdvs = :admdvs and orguntid = :orguntid ");
						de.setString("admdvs", admdvs);
						de.setString("orguntid", orguntid);
						DataStore ds = de.query();
						if (ds == null || ds.rowCount() == 0) {
							//WrapperResponse<SysOrguntDTO> orgn =  orguntService.queryOrguntByOrguntId(orguntid);
							DataObject orgn = HttpJSONFrowardClient.invokeService("OrguntService", "queryOrguntByOrguntId", orguntid);
							Date now = DateUtil.getCurrentDate();
							if (orgn.get("type").equals("success")) {

								String str = orgn.get("data").toString();
								DataObject tsds = DataObject.parseJSON(str);
								DataObject paravdo = DataObject.getInstance();
								paravdo.put("rid", tsds.getString("rid"));
								paravdo.put("orguntId", tsds.getString("orguntId"));
								paravdo.put("orgCodg", tsds.getString("orgCodg"));
								paravdo.put("orgName", tsds.getString("orgName"));
								paravdo.put("admdvs", tsds.getString("admdvs"));
								paravdo.put("orgTypeCode", tsds.getString("orgTypeCode"));
								paravdo.put("valiFlag", tsds.getString("valiFlag"));
								paravdo.put("abbr", tsds.getString("abbr"));
								paravdo.put("crteTime", now);
								paravdo.put("crteOptinsNo", tsds.getString("crteOptinsNo"));
								paravdo.put("optinsNo", tsds.getString("optinsNo"));
								paravdo.put("orgRltsId", tsds.getString("orgRltsId"));
								paravdo.put("prntOrgId", tsds.getString("prntOrgId"));
								paravdo.put("prntPath", tsds.getString("prntPath"));
								paravdo.put("leafnodFlag", tsds.getString("leafnodFlag"));
								paravdo.put("lv", tsds.getInt("lv"));
								paravdo.put("seq", tsds.getInt("seq"));

								orgds.addRow(paravdo);
							}
						}
					}
				}
			}

			DataObject vdo = DataObject.getInstance();
			vdo.put("orgds", orgds);
			return vdo;
		}
		return null;
	}

	public DataObject syncUactOrgInfor(DataObject para) throws AppException {

		DataStore orgds = para.getDataStore("orggrid");

		for (int i = 0; i < orgds.rowCount(); i++) {
			String rid = orgds.getString(i, "rid");
			String orguntId = orgds.getString(i, "orguntId");
			String orgCodg = orgds.getString(i, "orgCodg");
			String orgName = orgds.getString(i, "orgName");
			String admdvs = orgds.getString(i, "admdvs");
			String orgTypeCode = orgds.getString(i, "orgTypeCode");
			String valiFlag = orgds.getString(i, "valiFlag");
			String abbr = orgds.getString(i, "abbr");
			Date crteTime = orgds.getDate(i, "crteTime");
			String crteOptinsNo = orgds.getString(i, "crteOptinsNo");
			String optinsNo = orgds.getString(i, "optinsNo");
			String orgRltsId = orgds.getString(i, "orgRltsId");
			String prntOrgId = orgds.getString(i, "prntOrgId");
			String prntPath = orgds.getString(i, "prntPath");
			String leafnodFlag = orgds.getString(i, "leafnodFlag");
			int lv = orgds.getInt(i, "lv");
			int seq = orgds.getInt(i, "seq");
			try {
				de.clearSql();
				de.addSql(" insert into odssu.gb_orginfor ");
				de.addSql(" (rid, orguntid, orgcodg, orgname, admdvs, orgtypecode, valiflag, abbr, crtetime, crteoptinsno, optinsno, orgrltsid, prntorgid, prntpath, leafnodflag, lv, seq) ");
				de.addSql(" values ");
				de.addSql(" (:rid, :orguntid, :orgcodg, :orgname, :admdvs, :orgtypecode, :valiflag, :abbr, :crtetime, :crteoptinsno, :optinsno, :orgrltsid, :prntorgid, :prntpath, :leafnodflag, :lv, :seq) ");
				de.setString("rid", rid);
				de.setString("orguntid", orguntId);
				de.setString("orgcodg", orgCodg);
				de.setString("orgname", orgName);
				de.setString("admdvs", admdvs);
				de.setString("orgtypecode", orgTypeCode);
				de.setString("valiflag", valiFlag);
				de.setString("abbr", abbr);
				de.setDateTime("crtetime", crteTime);
				de.setString("crteoptinsno", crteOptinsNo);
				de.setString("optinsno", optinsNo);
				de.setString("orgrltsid", orgRltsId);
				de.setString("prntorgid", prntOrgId);
				de.setString("prntpath", prntPath);
				de.setString("leafnodflag", leafnodFlag);
				de.setInt("lv", lv);
				de.setInt("seq", seq);
				de.update();

			} catch (Exception e) {
				throw new AppException("同步机构【" + orguntId + "】时出错！" + e.getMessage());
			}
		}

		return para;

	}

	public DataObject getEmpInorg(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String empno, empname, gender, email, username;
		String officetel, sleepflag, mphone;
		DataStore vds = DataStore.getInstance();
		try {
			de.clearSql();
			de.addSql(" select     e.empno,e.empname,e.gender,e.email,e.loginname username,                                                      ");
			de.addSql("            e.officetel, e.mphone , decode(e.sleepflag,'1','注销','在职') sleepflag   ");
			de.addSql("  from odssu.empinfor e left join odssu.uactinfor u on  e.uactid = u.uactid                                                                                           ");
			de.addSql(" where     u.orguntid =:orgno                                                   ");
			de.addSql("     order by  e.empno                                                                                                 ");
			de.setString("orgno", orgno);

			vds = de.query();
		} catch (Exception e) {
			throw new AppException("+++++++++++++++" + e.getMessage());
		}
		DataStore empvds = DataStore.getInstance();
		for (int i = 0; i < vds.rowCount(); i++) {
			empno = vds.getString(i, "empno");
			empname = vds.getString(i, "empname");
			gender = vds.getString(i, "gender");
			email = vds.getString(i, "email");
			username = vds.getString(i, "username");
			officetel = vds.getString(i, "officetel");
			sleepflag = vds.getString(i, "sleepflag");
			mphone = vds.getString(i, "mphone");

			DataObject showdo = DataObject.getInstance();
			showdo.put("empno", empno);
			showdo.put("empname", empname);
			showdo.put("gender", gender);
			showdo.put("email", email);
			showdo.put("username", username);
			showdo.put("officetel", officetel);
			showdo.put("sleepflag", sleepflag);
			showdo.put("mphone", mphone);

			empvds.addRow(showdo);
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empvds);
		int empzscount = empvds.rowCount();
		vdo.put("empzscount", empzscount + "");
		return vdo;
	}

	public DataObject updateUactid(DataObject para) throws Exception, BusinessException {


		String ret = "更新UACTID失败";
		DataObject retdo = DataObject.getInstance();
		DE de = DE.getInstance();
		int i = 0, y = 0;
		System.out.println("+++++++++++++++开始更新+++++++++++++++++");
		if ("189".equals(OdssuNames.DBID) || OdssuNames.nova_fsjz) {
			de.clearSql();
			de.addSql("select e.loginname from  ");
			de.addSql("  odssu.empinfor e , odssu.uactinfor u ");
			de.addSql("       where e.loginname = u.uact ");
			DataStore uactds = de.query();

			try {
				for (int j = 0; j < uactds.rowCount(); j++) {
					String loginname = uactds.getString(j, "loginname");
					try {
						JSONObject jsb = new JSONObject();
						jsb.put("uact", loginname);
						//jsb.put("uact", "DEZHOU");
						DataObject empdo = HttpJSONFrowardClient.invokeService("userService", "queryUserPage", jsb.toString());

						if (empdo.get("type").equals("success")) {
							String str = empdo.get("data").toString();
							DataObject tsdo = DataObject.parseJSON(str);
							DataStore tsds = tsdo.getDataStore("data");
							if (tsds.rowCount() == 0) {
								//存在根据uact字段查询，接口返回的数据为空的情况，
								y++;
								System.out.println(y + "uact为【" + loginname + "】的操作员返回信息为空");

							} else {
								String uactId = tsds.getString(0, "uactId");
								String uact = tsds.getString(0, "uact");
								String orguntId = tsds.getString(0, "orguntId");

								de.clearSql();
								de.addSql("update odssu.uactinfor set orguntid = :orguntid, uactid = :uactid where uact = upper(:uact) ");
								de.setString("orguntid", orguntId);
								de.setString("uactid", uactId);
								de.setString("uact", uact);
								de.update();

								de.clearSql();
								de.addSql("update odssu.empinfor set uactid = :uactid where loginname = upper(:uact) ");
								de.setString("uactid", uactId);
								de.setString("uact", uact);
								de.update();
							}

						}

					} catch (BusinessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i = j;
				}
				de.commit();
				//de.rollback();
			} catch (Exception e) {
				de.rollback();
				retdo.put("ret", ret + e.getMessage());
				return retdo;

			}
		}
		if ("178".equals(OdssuNames.DBID)) {
			de.clearSql();
			de.addSql("select e.loginname from  ");
			de.addSql("  odssu.empinfor e , odssu.uactinfor u ");
			de.addSql("       where e.loginname = u.uact ");
			DataStore uactds = de.query();
			UserService user = (UserService) SpringBeanUtil.getBean("userService");
			try {
				for (int j = 0; j < uactds.rowCount(); j++) {
					String loginname = uactds.getString(j, "loginname");
					UactQueryDTO userdto = new UactQueryDTO();
					userdto.setUact(loginname);

					WrapperResponse<PageResult<UactDTO>> result = user.queryUserPage(userdto);
					String type = result.getType();
					if (type.equals("success")) {
						PageResult<UactDTO> userres = result.getData();
						List<UactDTO> userlist = userres.getData();
						if (userlist.size() == 0) {
							y++;
							System.out.println(y + "uact为【" + loginname + "】的操作员返回信息为空");
						} else {
							UactDTO usera = userlist.get(0);
							String uactId = usera.getUactId();
							String uact = usera.getUact();
							String orguntId = usera.getOrguntId();

							de.clearSql();
							de.addSql("update odssu.uactinfor set orguntid = :orguntid, uactid = :uactid where uact = upper(:uact) ");
							de.setString("orguntid", orguntId);
							de.setString("uactid", uactId);
							de.setString("uact", uact);
							de.update();

							de.clearSql();
							de.addSql("update odssu.empinfor set uactid = :uactid where loginname = upper(:uact) ");
							de.setString("uactid", uactId);
							de.setString("uact", uact);
							de.update();
						}

					}

					i = j;
				}
				de.commit();
				//de.rollback();
			} catch (Exception e) {
				de.rollback();
				retdo.put("ret", ret + e.getMessage());
				return retdo;

			}

		}
		System.out.println("+++++++++++++++更新完成+++++++++++++++++");
		ret = "更新uactid和orgunitid完成，本次共更新了" + i + "条操作员信息," + y + "条未更新！";
		retdo.put("ret", ret);
		return retdo;

	}
}
