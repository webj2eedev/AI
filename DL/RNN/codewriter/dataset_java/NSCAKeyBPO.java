package com.dw.odssu.ws.nscaKey;

import java.util.Date;

import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.SendMsgUtil;

public class NSCAKeyBPO extends BPO{
	/**
	 * 20151223 lzx
	 * 销Key
	 */
	public DataObject withdrawUkey(DataObject para) throws Exception {
		String keyid = para.getString("keyid", "");
		String empno = null;
		String keyzt = null;
		DataStore keyds = null;
		DE de = DE.getInstance();

		keyid = keyid.replaceAll(" ", "");
		
		// 检测入参
		if ("".equals(keyid)) {
			throw new BusinessException("传入的keyid为空，请检查!");
		}
		
		
		// 检测key是否可注销
  		de.clearSql();
  		de.addSql("select keyzt, empno from odssu.ukey_info where keyid = :keyid ");
		de.setString("keyid", keyid);
		keyds = de.query();
		if (keyds.rowCount() == 0) {
			throw new BusinessException("不存在编号为【" + keyid + "】的key信息，请检查!");
		}
		
		keyzt = keyds.getString(0, "keyzt");
		empno = keyds.getString(0, "empno");
		if(!"1".equals( keyzt )){
			throw new BusinessException("编号为【"+ keyid +"】的key已处于注销状态，不能进行【销key】操作!");
		}
		
		
		// 注销key
  		de.clearSql();
  		de.addSql("update odssu.ukey_info set keyzt = :keyzt where keyid = :keyid ");
		de.setString("keyzt", "0");
		de.setString("keyid", keyid);
		de.update();
		
		
		// 记录操作历史
  		de.clearSql();
  		de.addSql("insert into odssu.ukey_history (keyid, empno, operatetype, operatetime, operateempno) values (:keyid, :empno, :para3, :para4, :para5) ");
		de.setString("keyid", keyid);
		de.setString("empno", empno);
		de.setString("para3", "120");
		de.setDateTime("para4", new Date());
		de.setString("para5", this.getUser().getUserid());
		de.update();
		return null;
	}
	
	/**
	 * 20151208 lzx
	 * 发UKey
	 */
	public DataObject distUkey(DataObject para) throws Exception {
		
		// 获取用户id和keyid
		DE de = DE.getInstance();
		String empno = para.getString("empno");
		String keyid = para.getString("keyid");
		if (keyid == null || "".equals(keyid) || empno == null || "".equals(empno)) {
			throw new BusinessException("用户信息或key信息没有读取出，请重新操作！");
		}
		
		keyid = keyid.replaceAll(" ", "");
		
		// 判定是否已发过key
  		de.clearSql();
  		de.addSql("select keyzt, empno from odssu.ukey_info where keyid = :keyid");
		de.setString("keyid", keyid);
		DataStore keyds = de.query();
		if (keyds.rowCount() > 0) {
			
			String ckeyzt = keyds.getString(0, "keyzt");
			String cempno = keyds.getString(0, "empno");
			if("1".equals(ckeyzt)){
				throw new BusinessException("编号为【"+keyid+"】key正在被【"+cempno+"】使用，请勿重复发key!");
			}
			
			// 发key - 更新
  			de.clearSql();
  			de.addSql("update odssu.ukey_info set keyzt = :keyzt, empno = :empno where keyid = :keyid");
			de.setString("keyzt", "1");
			de.setString("empno", empno);
			de.setString("keyid", keyid);
			de.update();
		}else{
			// 发key - 新增
  			de.clearSql();
  			de.addSql("insert into odssu.ukey_info(keyid, empno, keyzt) values(:keyid, :empno, :para3)");
			de.setString("keyid", keyid);
			de.setString("empno", empno);
			de.setString("para3", "1");
			de.update();
		}
		
		
		// 记录操作历史
  		de.clearSql();
  		de.addSql("insert into odssu.ukey_history (keyid, empno, operatetype, operatetime, operateempno) values (:keyid, :empno, :para3, :para4, :para5) ");
		de.setString("keyid", keyid);
		de.setString("empno", empno);
		de.setString("para3", "021");
		de.setDateTime("para4", new Date());
		de.setString("para5", this.getUser().getUserid());
		de.update();
		if(GlobalNames.DEPLOY_IN_ECO) {
			SendMsgUtil.SynAddUkey(keyid);
		}
		
		return null;
	}
	
	/**
	 * 20151208 lzx
	 * 从ODS中查询人员信息
	 */
	public DataObject getEmpInfoFromOds(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();

		//用户名 大小写不敏感，统一转成大写去比较
		String gjz = para.getString("gjz");
		gjz = ((gjz == null || "".equals(gjz)) ? "%" : "%" + gjz + "%");
		gjz = gjz.toUpperCase();

		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from (select a.empno,a.idcardno,a.empname,a.rname,a.empnamepy,a.rnamepy,a.gender,a.emptype,a.sleepflag,a.officetel,a.mphone,a.email,a.hrbelong,a.loginname,a.password,a.empcreatedate,a.signet,a.finger_vein,b.displayname                 ");
  		de.addSql("           from odssu.empinfor a left outer join odssu.orginfor b	 ");
  		de.addSql("                on a.hrbelong = b.orgno   ");
  		de.addSql("         where (upper(a.loginname) like :gjz or a.idcardno like :gjz or upper(a.empnamepy) like :gjz or a.empname like :gjz or a.rname like :gjz ");
  		de.addSql("            or  upper(a.rnamepy) like :gjz ) ");
  		de.addSql("         order by a.sleepflag,a.empno,a.empname) tem     ");
		this.de.setString("gjz", gjz);
		this.de.setQueryScope(100);
		DataStore empds = this.de.query();

		vdo.put("empds", empds);
		return vdo;
	}
	
	
	/**
	 * 查询Key信息
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryKeyInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		
		String keyId = para.getString("keyid", "");
		String empno = para.getString("empno", "");
		String empname = para.getString("empname", "");
		
		keyId = keyId.replaceAll(" ", "");
  		  		de.addSql("  select k.keyid,");
  		de.addSql("      o.empno,");
  		de.addSql("     o.empname,");
  		de.addSql("     k.keyzt,");
  		de.addSql("      decode(k.keyzt, '1', '注销', '发key') keyoperate");
  		de.addSql("  from odssu.ukey_info k right outer join odssu.empinfor o on o.empno = k.empno");

		if (!"".equals(empname)) {
  			de.addSql("     and o.empname like :empname			");
  			this.de.setString("empname", "%" + empname + "%");
		}
		if (!"".equals(empno)) {
  			de.addSql("     and o.empno like :empname				");
  			this.de.setString("empname", "%" + empname + "%");
		}
		if (!"".equals(keyId)) {
  			de.addSql("     and k.keyid like :empname				");
  			this.de.setString("empname", "%" + empname + "%");
		}
    		de.addSql("   order by keyid asc");

		
		DataStore empds = this.de.query();
		vdo.put("keyinfo", empds);
		return vdo;
	}
	
	
	/**
	 * 查询Key信息
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryKeyHis(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		
		String keyId = para.getString("keyid", "");
		String empno = para.getString("empno", "");
		String empname = para.getString("empname", "");
		String operatetype = para.getString("operatetype", "");
		
		keyId = keyId.replaceAll(" ", "");
  		de.addSql("  select k.keyid,");
  		de.addSql("     k.empno,");
  		de.addSql("     o.empname,");
  		de.addSql("     k.operatetype,");
  		de.addSql("     k.operatetime,");
  		de.addSql("      k.operateempno,");
  		de.addSql("      p.empname operateempname");
  		de.addSql("   from odssu.ukey_history k, odssu.empinfor o, odssu.empinfor p");
  		de.addSql("  where k.empno = o.empno");
  		de.addSql("   and k.operateempno = p.empno");
		
		if(!"".equals(operatetype)){
  			de.addSql("     and k.operatetype like :keyid			");
  			this.de.setString("keyid", "%"+keyId+"%");
		}
		if(!"".equals(empname)){
  			de.addSql("     and o.empname like :keyid			");
  			this.de.setString("keyid", "%"+keyId+"%");
		}
		if(!"".equals(empno)){
  			de.addSql("     and o.empno like :keyid			");
  			this.de.setString("keyid", "%"+keyId+"%");
		}
		if(!"".equals(keyId)){
  			de.addSql("     and k.keyid like :keyid			");
  			this.de.setString("keyid", "%"+keyId+"%");
		}
  		  		de.addSql("   order by keyid asc");
		DataStore empds = this.de.query();
		vdo.put("keyinfo", empds);
		return vdo;
	}
}
