package com.dw.odssu.dataexp;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.*;
import com.dareway.framework.workFlow.BPO;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Date;
import java.util.Iterator;

public class DataExpBPO extends BPO {

    public DataObject getOrgNameInfo(DataObject para) throws Exception {
        String orgno = para.getString("orgno","");
        DataObject vdo = DataObject.getInstance();
        DataStore vds = DataStore.getInstance();

        de.clearSql();
        de.addSql(" select a.orgname from odssu.orginfor a where a.orgno = :orgno ");
        de.setString("orgno",orgno);
        vds = de.query();
        if (vds != null && vds.size() != 0){
            String orgname = vds.getString(0,"orgname");
            vdo.put("orgname",orgname);
        }

        return vdo;
    }

    private byte[] createOdsDataJson(DataStore vds) throws Exception {
        String jsonStr = "";
        byte[] jsonStrByte = null;
        JSONArray jsonArray = new JSONArray();
        String typelist = vds.getTypeList();
        String[] columns = typelist.split(",");
        int colLen = columns.length;
        for (int i=0;i<vds.rowCount();i++) {
            JSONObject json = new JSONObject();
            for (int j = 0; j < colLen; j++) {
                String[] column = columns[j].split(":");

                String colName = vds.getColumnName()[j];

                switch (column[1].charAt(0)) {
                    case 'd':
                        Date valueDate = vds.getDate(i, colName);
                        String valueDateStr = DateUtil.dateToString(valueDate,"yyyy-MM-dd HH:mm:ss");
                        json.put(colName, valueDateStr);
                        break;
                    default:
                        String valueStr = vds.getString(i, colName);
                        json.put(colName, valueStr);
                        break;
                }
            }
            jsonArray.put(json);
        }
        jsonStr = jsonArray.toString();
        jsonStrByte = StringUtil.stringToByteArray(jsonStr);

        return jsonStrByte;
    }
    public DataObject createOdsDataExpFile(DataObject para) throws Exception {
        String orgno = para.getString("orgno","");
        String orgname = para.getString("orgname","");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.setEncoding("utf-8");
        try {
            de.clearSql();
            de.addSql(" select * from odssu.empinfor e where e.empno in( ");
            de.addSql(" select a.empno from odssu.empinfor a, odssu.ir_org_closure b where ");
            de.addSql(" a.hrbelong = b.orgno and b.belongorgno = :belongorgno and a.sleepflag = '0')");
            de.setString("belongorgno",orgno);
            DataStore empinfords = de.query();
            zos.putNextEntry(new ZipEntry("./empinfor/" + "empinfor.txt"));
            zos.write(this.createOdsDataJson(empinfords));
            zos.putNextEntry(new ZipEntry("./empinfor/" + "empinfor.lx"));
            zos.write(StringUtil.stringToByteArray(empinfords.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.orginfor o where o.orgno in( ");
            de.addSql(" select a.orgno from odssu.orginfor a ,odssu.ir_org_closure b ");
            de.addSql(" where a.belongorgno = b.orgno and b.belongorgno = :belongorgno ");
            de.addSql(" and a.sleepflag = '0' and a.orgtype in ('HSDOMAIN_RSCKS','HSDOMAIN_SBS','HSDOMAIN_SBZ')) ");
            de.setString("belongorgno",orgno);
            DataStore orginfords = de.query();
            zos.putNextEntry(new ZipEntry("./orginfor/" + "orginfor.txt"));
            zos.write(this.createOdsDataJson(orginfords));
            zos.putNextEntry(new ZipEntry("./orginfor/" + "orginfor.lx"));
            zos.write(StringUtil.stringToByteArray(orginfords.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.roleinfor a where a.deforgno =:deforgno and sleepflag = '0' ");
            de.setString("deforgno",orgno);
            DataStore roleinfords = de.query();
            zos.putNextEntry(new ZipEntry("./roleinfor/" + "roleinfor.txt"));
            zos.write(this.createOdsDataJson(roleinfords));
            zos.putNextEntry(new ZipEntry("./roleinfor/" + "roleinfor.lx"));
            zos.write(StringUtil.stringToByteArray(roleinfords.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.role_folder a where a.deforgno = :deforgno ");
            de.setString("deforgno",orgno);
            DataStore role_folderds = de.query();
            zos.putNextEntry(new ZipEntry("./role_folder/" + "role_folder.txt"));
            zos.write(this.createOdsDataJson(role_folderds));
            zos.putNextEntry(new ZipEntry("./role_folder/" + "role_folder.lx"));
            zos.write(StringUtil.stringToByteArray(role_folderds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.ir_emp_org e where e.empno in ( ");
            de.addSql(" select a.empno from odssu.empinfor a, odssu.ir_org_closure b where ");
            de.addSql(" a.hrbelong = b.orgno and b.belongorgno = :belongorgno and a.sleepflag = '0')");
            de.setString("belongorgno",orgno);
            DataStore ir_emp_orgds = de.query();
            zos.putNextEntry(new ZipEntry("./ir_emp_org/" + "ir_emp_org.txt"));
            zos.write(this.createOdsDataJson(ir_emp_orgds));
            zos.putNextEntry(new ZipEntry("./ir_emp_org/" + "ir_emp_org.lx"));
            zos.write(StringUtil.stringToByteArray(ir_emp_orgds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.emp_app e where e.empno in ( ");
            de.addSql(" select a.empno from odssu.empinfor a, odssu.ir_org_closure b where ");
            de.addSql(" a.hrbelong = b.orgno and b.belongorgno = :belongorgno and a.sleepflag = '0')");
            de.setString("belongorgno",orgno);
            DataStore emp_appds = de.query();
            zos.putNextEntry(new ZipEntry("./emp_app/" + "emp_app.txt"));
            zos.write(this.createOdsDataJson(emp_appds));
            zos.putNextEntry(new ZipEntry("./emp_app/" + "emp_app.lx"));
            zos.write(StringUtil.stringToByteArray(emp_appds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.ir_emp_org_all_role e where e.empno in ( ");
            de.addSql(" select a.empno from odssu.empinfor a, odssu.ir_org_closure b where ");
            de.addSql(" a.hrbelong = b.orgno and b.belongorgno = :belongorgno and a.sleepflag = '0')");
            de.setString("belongorgno",orgno);
            DataStore ir_emp_org_all_roleds = de.query();
            zos.putNextEntry(new ZipEntry("./ir_emp_org_all_role/" + "ir_emp_org_all_role.txt"));
            zos.write(this.createOdsDataJson(ir_emp_org_all_roleds));
            zos.putNextEntry(new ZipEntry("./ir_emp_org_all_role/" + "ir_emp_org_all_role.lx"));
            zos.write(StringUtil.stringToByteArray(ir_emp_org_all_roleds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.org_business_scope o where o.orgno in ( ");
            de.addSql(" select a.orgno from odssu.orginfor a ,odssu.ir_org_closure b ");
            de.addSql(" where a.belongorgno = b.orgno and b.belongorgno = :belongorgno ");
            de.addSql(" and a.sleepflag = '0' and a.orgtype in ('HSDOMAIN_RSCKS','HSDOMAIN_SBS','HSDOMAIN_SBZ')) ");
            de.setString("belongorgno",orgno);
            DataStore org_business_scopeds = de.query();
            zos.putNextEntry(new ZipEntry("./org_business_scope/" + "org_business_scope.txt"));
            zos.write(this.createOdsDataJson(org_business_scopeds));
            zos.putNextEntry(new ZipEntry("./org_business_scope/" + "org_business_scope.lx"));
            zos.write(StringUtil.stringToByteArray(org_business_scopeds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.role_orgtype a where a.roleno in ");
            de.addSql(" (select b.roleno from odssu.roleinfor b where b.deforgno = :deforgno and b.sleepflag = '0' ) ");
            de.setString("deforgno",orgno);
            DataStore role_orgtypeds = de.query();
            zos.putNextEntry(new ZipEntry("./role_orgtype/" + "role_orgtype.txt"));
            zos.write(this.createOdsDataJson(role_orgtypeds));
            zos.putNextEntry(new ZipEntry("./role_orgtype/" + "role_orgtype.lx"));
            zos.write(StringUtil.stringToByteArray(role_orgtypeds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.ir_role_closure a where a.roleno in ");
            de.addSql(" (select b.roleno from odssu.roleinfor b where b.deforgno = :deforgno and b.sleepflag = '0' ) ");
            de.setString("deforgno",orgno);
            DataStore ir_role_closureds = de.query();
            zos.putNextEntry(new ZipEntry("./ir_role_closure/" + "ir_role_closure.txt"));
            zos.write(this.createOdsDataJson(ir_role_closureds));
            zos.putNextEntry(new ZipEntry("./ir_role_closure/" + "ir_role_closure.lx"));
            zos.write(StringUtil.stringToByteArray(ir_role_closureds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.role_function_manual a where a.roleno in ");
            de.addSql(" (select b.roleno from odssu.roleinfor b where b.deforgno = :deforgno and b.sleepflag = '0' ) ");
            de.setString("deforgno",orgno);
            DataStore role_function_manualds = de.query();
            zos.putNextEntry(new ZipEntry("./role_function_manual/" + "role_function_manual.txt"));
            zos.write(this.createOdsDataJson(role_function_manualds));
            zos.putNextEntry(new ZipEntry("./role_function_manual/" + "role_function_manual.lx"));
            zos.write(StringUtil.stringToByteArray(role_function_manualds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select a.roleid, a.pdid, a.dptdid , a.toccode from bpzone.dutyposition_task_role a where a.roleid in ");
            de.addSql(" (select b.roleno from odssu.roleinfor b where b.deforgno = :deforgno and b.sleepflag = '0' ) ");
            de.setString("deforgno",orgno);
            DataStore dutyposition_task_roleds = de.query();
            zos.putNextEntry(new ZipEntry("./dutyposition_task_role/" + "dutyposition_task_role.txt"));
            zos.write(this.createOdsDataJson(dutyposition_task_roleds));
            zos.putNextEntry(new ZipEntry("./dutyposition_task_role/" + "dutyposition_task_role.lx"));
            zos.write(StringUtil.stringToByteArray(dutyposition_task_roleds.getTypeList()));
            zos.closeEntry();

            de.clearSql();
            de.addSql(" select * from odssu.emp_rolefolder e where e.empno = 'ADMIN' and e.folderid in ");
            de.addSql(" (select a.folderid from odssu.role_folder a where a.collect_query = '1' and a.deforgno = :deforgno) ");
            de.setString("deforgno",orgno);
            DataStore emp_rolefolderds = de.query();
            zos.putNextEntry(new ZipEntry("./emp_rolefolder/" + "emp_rolefolder.txt"));
            zos.write(this.createOdsDataJson(emp_rolefolderds));
            zos.putNextEntry(new ZipEntry("./emp_rolefolder/" + "emp_rolefolder.lx"));
            zos.write(StringUtil.stringToByteArray(emp_rolefolderds.getTypeList()));
            zos.closeEntry();

            // 压缩完成
            zos.flush();
        } catch (JSONException e) {
            throw new AppException(e);
        } catch (IOException e) {
            throw new AppException(e);
        } finally {
            if(zos!=null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String zipFileName = orgno + "-" + orgname + "-" + DateUtil.getCurrentDateToString()+".zip";

        byte[] zipFileBytes = baos.toByteArray();
        DataObject result = DataObject.getInstance();
        result.put("zipFileName", zipFileName);
        result.put("zipFileBytes", zipFileBytes);
        return result;
    }

    public DataObject checkImportOds(DataObject para) throws AppException {

        // 查找模板文件夹中以.txt扩展名结尾的模板信息描述文件
        File folder = (File) para.getObject("fileFolder");
        String deforgno = para.getString("deforgno");
        File fileInfo = null;
        StringBuffer repeatInfoBuffer = new StringBuffer();
        File[] allFiles = folder.listFiles();
        for (int i = 0; i < allFiles.length; i++) {
            if (allFiles[i].getName().endsWith(".txt")) {
                fileInfo = allFiles[i];
                break;
            }
        }

        if (fileInfo == null) {
            throw new AppException(
                    "文件夹【" + folder.getName() + "】中不存在文件【" + folder.getName() + ".txt】!");
        }


        FileInputStream fis = null;
        try {
            //获取表名
            String fileName = fileInfo.getName();
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
            fis = new FileInputStream(fileInfo);
            byte[] fileInfoBytes = IOUtils.toByteArray(fis);
            JSONArray jsonArray = new JSONArray(StringUtil.byteArrayToString(fileInfoBytes));
            if ("empinfor".equals(fileName)){
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject fileInfoJson = jsonArray.getJSONObject(i);
                    String empno = fileInfoJson.getString("empno");

                    de.clearSql();
                    de.addSql("select 1 from odssu.empinfor a where a.empno =:empno");
                    de.setString("empno",empno);
                    DataStore empnods = de.query();
                    if( empnods.rowCount() > 0){
                        repeatInfoBuffer.append(fileName+"表中已存在empno=【" + empno + "】的值.\\n");
                    }

                }
            }
            if ("orginfor".equals(fileName)){
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject fileInfoJson = jsonArray.getJSONObject(i);
                    String orgno = fileInfoJson.getString("orgno");

                    de.clearSql();
                    de.addSql("select 1 from odssu.orginfor a where a.orgno = :orgno");
                    de.setString("orgno",orgno);
                    DataStore orgnods = de.query();
                    if( orgnods.rowCount() > 0){
                        repeatInfoBuffer.append(fileName+"表中已存在orgno=【" + orgno + "】的值.\\n");
                    }

                }
            }
            if ("roleinfor".equals(fileName)){
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject fileInfoJson = jsonArray.getJSONObject(i);
                    String roleno = fileInfoJson.getString("roleno");

                    de.clearSql();
                    de.addSql("select 1 from odssu.roleinfor a where a.roleno = :roleno ");
                    de.setString("roleno",roleno);
                    DataStore rolenods = de.query();
                    if( rolenods.rowCount() > 0){
                        repeatInfoBuffer.append(fileName+"表中已存在roleno=【" + roleno + "】的值.\\n");
                    }

                }
            }
            if ("role_folder".equals(fileName)){

                    de.clearSql();
                    de.addSql("select 1 from odssu.role_folder a where a.deforgno = :deforgno ");
                    de.setString("deforgno",deforgno);
                    DataStore rolenods = de.query();
                    if( rolenods.rowCount() > 2) {
                        repeatInfoBuffer.append("请在生产库的ods->角色管理中，找到当前地市系统的角色目录，查看目录是否有效，目录下是否有角色");
                }
            }
            DataObject vdo = DataObject.getInstance();
            vdo.put("repeatInfo",repeatInfoBuffer.toString().trim());
            return vdo;
        } catch (FileNotFoundException e) {
            throw new AppException(e);
        } catch (IOException e) {
            throw new AppException(e);
        } catch (JSONException e) {
            throw new AppException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new AppException(e);
                }
            }
        }
    }

    public DataObject saveImportOds(DataObject para) throws AppException {

        File folder = (File) para.getObject("fileFolder");
        String deforgno = para.getString("deforgno");
        // 查找模板文件夹中以.txt扩展名结尾的模板信息描述文件
        File fileInfo = null;
        File lxFileInfo = null;
        File[] allFiles = folder.listFiles();
        for (int i = 0; i < allFiles.length; i++) {
            if (allFiles[i].getName().endsWith(".txt")) {
                fileInfo = allFiles[i];
            }
            if (allFiles[i].getName().endsWith(".lx")) {
                lxFileInfo = allFiles[i];
            }
        }

        if (fileInfo == null) {
            throw new AppException(
                    "文件夹【" + folder.getName() + "】中不存在文件【" + folder.getName() + ".txt】!");
        }
        if (lxFileInfo == null) {
            throw new AppException(
                    "文件夹【" + folder.getName() + "】中不存在文件【" + folder.getName() + ".lx】!");
        }

        FileInputStream lxfis = null;
        FileInputStream fis = null;
        try {
            lxfis = new FileInputStream(lxFileInfo);
            byte[] lxFileInfoBytes = IOUtils.toByteArray(lxfis);
            String typelist = StringUtil.byteArrayToString(lxFileInfoBytes);
            String[] columns = typelist.split(",");
            StringBuffer sqlBF = new StringBuffer();
            int colLen = columns.length;
            //获取表名
            String fileName = fileInfo.getName();
            fileName = fileName.substring(0,fileName.lastIndexOf("."));

            fis = new FileInputStream(fileInfo);
            byte[] fileInfoBytes = IOUtils.toByteArray(fis);
            JSONArray jsonArray = new JSONArray(StringUtil.byteArrayToString(fileInfoBytes));

            for (int i=0;i<jsonArray.length();i++){
                JSONObject fileInfoJson = jsonArray.getJSONObject(i);
                String colstr = "";
                String parastr = "";
                Iterator<?> itor = fileInfoJson.keys();
                while (itor.hasNext()) {
                    String key = (String) itor.next();
                    colstr += key + ",";
                    parastr += ":"+key+",";
                }

                if (colstr.endsWith(",") && colstr.length() >= 2) {
                    colstr = colstr.substring(0, colstr.length() - 1);
                }
                if (parastr.endsWith(",") && parastr.length() >= 2) {
                    parastr = parastr.substring(0, parastr.length() - 1);
                }

                if ("dutyposition_task_role".equals(fileName)){
                    sqlBF.setLength(0);
                    sqlBF.append(" insert into bpzone." + fileName + "(" + colstr + ")  ");
                    sqlBF.append(" values(" + parastr + ")                         ");
                    de.clearSql();
                    de.addSql(sqlBF.toString());

                    Iterator<?> valueitor = fileInfoJson.keys();
                    while (valueitor.hasNext()) {
                        String key = (String) valueitor.next();
                        Object value = fileInfoJson.get(key);
                        for (int j = 0; j < colLen; j++) {
                            String[] column = columns[j].split(":");
                            if (column[0].equals(key)){
                                switch (column[1].charAt(0)) {
                                    case 'n':
                                        de.setDouble(column[0], (Double) value);
                                        break;
                                    case 'd':
                                        Date date = DateUtil.stringToDate((String) value);
                                        de.setDateTime(column[0], date);
                                        break;
                                    default:
                                        de.setString(column[0], (String) value);
                                        break;
                                }
                            }
                        }
                    }
                }else if ("role_folder".equals(fileName)){

                    sqlBF.setLength(0);
                    sqlBF.append(" delete from odssu.role_folder where deforgno = :deforgno  ");
                    de.clearSql();
                    de.addSql(sqlBF.toString());
                    de.setString("deforgno",deforgno);
                    de.update();

                    sqlBF.setLength(0);
                    sqlBF.append(" insert into odssu." + fileName + "(" + colstr + ")  ");
                    sqlBF.append(" values(" + parastr + ")                         ");
                    de.clearSql();
                    de.addSql(sqlBF.toString());

                    Iterator<?> valueitor = fileInfoJson.keys();
                    while (valueitor.hasNext()) {
                        String key = (String) valueitor.next();
                        Object value = fileInfoJson.get(key);
                        for (int j = 0; j < colLen; j++) {
                            String[] column = columns[j].split(":");
                            if (column[0].equals(key)){
                                switch (column[1].charAt(0)) {
                                    case 'n':
                                        de.setDouble(column[0], (Double) value);
                                        break;
                                    case 'd':
                                        Date date = DateUtil.stringToDate((String) value);
                                        de.setDateTime(column[0], date);
                                        break;
                                    default:
                                        de.setString(column[0], (String) value);
                                        break;
                                }
                            }
                        }
                    }
                }else {
                    sqlBF.setLength(0);
                    sqlBF.append(" insert into odssu." + fileName + "(" + colstr + ")  ");
                    sqlBF.append(" values(" + parastr + ")                         ");
                    de.clearSql();
                    de.addSql(sqlBF.toString());

                    Iterator<?> valueitor = fileInfoJson.keys();
                    while (valueitor.hasNext()) {
                        String key = (String) valueitor.next();
                        Object value = fileInfoJson.get(key);
                        for (int j = 0; j < colLen; j++) {
                            String[] column = columns[j].split(":");
                            if (column[0].equals(key)){
                                switch (column[1].charAt(0)) {
                                    case 'n':
                                        de.setDouble(column[0], (Double) value);
                                        break;
                                    case 'd':
                                        Date date = DateUtil.stringToDate((String) value);
                                        de.setDateTime(column[0], date);
                                        break;
                                    default:
                                        de.setString(column[0], (String) value);
                                        break;
                                }
                            }
                        }
                    }
                }

                de.update();
            }

            DataObject result = DataObject.getInstance();
            return result;
        } catch (FileNotFoundException e) {
            throw new AppException(e);
        } catch (IOException e) {
            throw new AppException(e);
        } catch (JSONException e) {
            throw new AppException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new AppException(e);
                }
            }
            if (lxfis != null) {
                try {
                    lxfis.close();
                } catch (IOException e) {
                    throw new AppException(e);
                }
            }
        }
    }

    public DataObject addActivitiAndBpzoneData(DataObject para) throws AppException {

        de.clearSql();
        de.addSql(" insert into odssu.ir_role_closure(roleno,isroleno) select roleno,roleno from  odssu.roleinfor a ");
        de.addSql(" where a.roleno not in (select b.roleno from odssu.ir_role_closure b) ");
        de.update();

        de.clearSql();
        de.addSql(" insert into activiti.act_id_user(id_,rev_,last_,pwd_) ");
        de.addSql(" select a.empno , c2n('2') , a.empname , 'admin' from odssu.empinfor a ");
        de.addSql(" where a.sleepflag = '0' and a.empno not in (select b.id_ from activiti.act_id_user b )");
        de.update();

        de.clearSql();
        de.addSql(" insert into bpzone.active_user(userid,username) select a.empno , a.empname from odssu.empinfor a ");
        de.addSql(" where a.sleepflag = '0' and a.empno not in (select b.userid from bpzone.active_user b )");
        de.update();

        de.clearSql();
        de.addSql(" insert into bpzone.role(roleid,rolename) select a.roleno , a.rolename from odssu.roleinfor a ");
        de.addSql(" where a.sleepflag = '0' and a.roleno not in (select b.roleid from bpzone.role b ) ");
        de.update();

        /*补充user_role_orgn*/
        de.clearSql();
        de.addSql(" insert into bpzone.user_role_orgn(userid,roleid,orgnid,role4orgn)" );
        de.addSql(       "  select distinct a.empno userid," );
        de.addSql(       "                  a.roleno roleid," );
        de.addSql(       "                  a.ORGNO orgnid," );
        de.addSql(       "                  (a.roleno || '@' || a.orgno) role4orgn" );
        de.addSql(       "    from odssu.ir_emp_org_all_role a" );
        de.addSql(      "   where  not exists (select 1 from bpzone.user_role_orgn c where a.empno = c.userid and a.roleno = c.roleid and a.orgno = c.orgnid) ");
        de.update();

        /*补充user_dp_orgn*/
        de.clearSql();
        de.addSql(" insert into bpzone.user_dp_orgn(userid,dp4orgn)" );
        de.addSql(        "  select distinct a.userid," );
        de.addSql(        "                  (b.pdid ||" );
        de.addSql(        "                         '.' ||" );
        de.addSql(        "                         b.dptdid ||" );
        de.addSql(        "                         '.' ||" );
        de.addSql(        "                         b.toccode ||" );
        de.addSql(        "                         '@' ||" );
        de.addSql(       "                         a.orgnid) dp4orgn" );
        de.addSql(        "    from bpzone.user_role_orgn a, bpzone.dutyposition_task_role b" );
        de.addSql(        "   where a.roleid = b.roleid" );
        de.addSql(        "   and not exists (select 1 from bpzone.user_dp_orgn c where a.userid = c.userid and (b.pdid || '.' || b.dptdid || '.' || b.toccode || '@' || a.orgnid) = c.dp4orgn) ");
        de.update();

        /*补充ods的数据*/
        de.clearSql();
        de.addSql(" insert into odssu.ir_role_business_scope(roleno,scopeno) " );
        de.addSql(        " select a.roleno , b.scopeno" );
        de.addSql(        " from odssu.roleinfor a , odssu.business_scope b " );
        de.addSql(        " where a.sleepflag = '0'" );
        de.addSql(        " and a.jsgn = '3'" );
        de.addSql(        " and not exists ( select 1 from odssu.ir_role_business_scope c where c.roleno = a.roleno and c.scopeno = b.scopeno) ");
        de.update();

        de.clearSql();
        de.addSql(" insert into odssu.dutyposition_task_role(pdid,pdlabel,dptdid,dptdlabel,roleno,toccode) " );
        de.addSql(        " select a.pdid,a.pdlabel ,b.dptdid,b.dptdlabel,c.roleid,c.toccode " );
        de.addSql(        " from bpzone.process_define a ,bpzone.dutyposition_task b,bpzone.dutyposition_task_role c " );
        de.addSql(        " where a.pdid = c.pdid and b.pdid = c.pdid and b.dptdid = c.dptdid " );
        de.addSql(        " and not exists ( select 1 from odssu.dutyposition_task_role d " );
        de.addSql(        " where c.pdid = d.pdid and c.dptdid = d.dptdid and c.toccode = d.toccode and c.roleid = d.roleno) ");
        de.update();

        de.clearSql();
        de.addSql(" update odssu.dutyposition_task_role a " );
        de.addSql(        "set standardflag = '2' " );
        de.addSql(        "where  a.pdid in " );
        de.addSql(        "(select b.pdid from bpzone.process_define b, odssu.dutyposition_task_role c where c.pdid = b.pdid and b.standardflag = '2') " );
        de.update();

        de.clearSql();
        de.addSql(" update odssu.dutyposition_task_role a " );
        de.addSql(        " set standard_pdid = (select b.standard_pdid from bpzone.process_define b where a.pdid = b.pdid) " );
        de.addSql(        " where  a.standardflag = '2' ");
        de.update();

        return null;
    }
}
