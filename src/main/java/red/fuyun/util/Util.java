package red.fuyun.util;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import okhttp3.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import org.springframework.core.io.ClassPathResource;
import red.fuyun.dao.ScheduleMapper;
import red.fuyun.dao.TempletMapper;
import red.fuyun.dao.UserInfoMapper;
import red.fuyun.pojo.Do.ScheduleDo;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.campusphere.*;
import red.fuyun.pojo.Do.UserInfoDo;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/9/21 21:52
 */
public class Util {

    private static final String ANDROID_DEFAULT_KEY_NEW= "b3L26XNL";

    private static final String ANDROID_DEFAULT_KEY = "ST83=@XV";
    //苹果解密userStoreAppList 里面得cpdaliyinfo
    private static final String IOS_DEFAULT_KEY = "XCE927==";

    private static final String IOS_DEFAULT_KEY_NEW = "b3L26XNL";

    private static String html;
    static{
        ClassPathResource classPathResource = new ClassPathResource("index.html");
        try {
            InputStream inputStream = classPathResource.getInputStream();
            html =  IoUtil.read(inputStream, Charset.forName("UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    public static  boolean isIOS(String ua){
        return ua.contains("iPhone") && !ua.contains("Android");
    }
    public static String getKey(boolean isIos){
        if (isIos){
            return IOS_DEFAULT_KEY;
        }
        return ANDROID_DEFAULT_KEY;
    }



    public static JSONObject decryptDESUserInfo(String cpdailyInfo) throws Exception {
        JSONObject jsonObject = null;
        try {
            String  s = DES.decryptDES(cpdailyInfo,ANDROID_DEFAULT_KEY);
            jsonObject = JSONObject.parseObject(s);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return jsonObject;
    }
    public static String encryptDES(String cpdailyInfo) throws Exception {
        String  s = DES.encryptDES(cpdailyInfo,ANDROID_DEFAULT_KEY_NEW);
        return s;
    }
    public static JSONObject decryptDESUserStoreAppList(String cpdailyInfo,String ua) throws Exception {
        boolean ios = isIOS(ua);

        JSONObject jsonObject = null;
        try {
            String  s="";
            s = DES.decryptDES(cpdailyInfo,getKey(ios));
            jsonObject = JSONObject.parseObject(s);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return jsonObject;
    }
    public static String encryptDESUserInfo(String cpdailyInfo) throws Exception {
        String  s = DES.encryptDES(cpdailyInfo,ANDROID_DEFAULT_KEY);
        return s;
    }

//    public static JSONArray CollectorTempletConvertoSubmitJsonObject(JSONObject templet, JSONObject newDatas) {
//        JSONArray templetRows = templet.getJSONArray("rows");
//        JSONArray newDatasRows = newDatas.getJSONArray("rows");
//        if (templetRows.size() != newDatasRows.size()) {
//            return null;
//        }
//        for (int i = 0; i < templetRows.size(); i++) {
//            JSONObject templetRow = templetRows.getJSONObject(i);
//            JSONObject newDatasRow = newDatasRows.getJSONObject(i);
//            String templetColName = templetRow.getString("colName");
//            String newDataColName = newDatasRow.getString("colName");
//            Integer templetFieldType = templetRow.getInteger("fieldType");
//            Integer newDataFieldType = templetRow.getInteger("fieldType");
//            if (newDatasRow.getInteger("isRequired").equals(0)) {
//                JSONArray fieldItems = newDatasRow.getJSONArray("fieldItems");
//                fieldItems.clear();
//                continue;
//            }
//            if (templetColName.equals(newDataColName)) {
//                if (templetFieldType.equals(newDataFieldType)) {
//                    if (newDataFieldType.equals(1)) {
//                        newDatasRow.put("value", templetRow.getString("value"));
//                    }
//                    if (newDataFieldType.equals(2)) {
//                        JSONArray templetFieldItems = templetRow.getJSONArray("fieldItems");
//                        JSONArray newDataFieldItems = newDatasRow.getJSONArray("fieldItems");
//
//                        if (templetFieldItems.size() == newDataFieldItems.size()) {
//                            for (int j = 0; j < templetFieldItems.size(); j++) {
//                                JSONObject templetItem = templetFieldItems.getJSONObject(j);
//                                Integer isSelected = templetItem.getInteger("isSelected");
//                                String templetContent = templetItem.getString("content");
//                                if (isSelected != null) {
//                                    JSONObject newDataItem = newDataFieldItems.getJSONObject(j);
//                                    String newDataContent = newDataItem.getString("content");
//                                    if (templetContent.equals(newDataContent)) {
//                                        newDataItem.put("isSelected", 1);
//
//                                        newDataFieldItems.clear();
//                                        newDataFieldItems.add(newDataItem);
//                                        String itemWid = newDataItem.getString("itemWid");
//                                        newDatasRow.put("value", itemWid);
//                                        break;
//                                    }
//
//                                    for (int k = 0; k < newDataFieldItems.size(); k++) {
//                                        JSONObject newDataItem2 = newDataFieldItems.getJSONObject(k);
//                                        String newDataContent2 = newDataItem2.getString("content");
//                                        if (templetContent.equals(newDataContent2)) {
//                                            newDataItem2.put("isSelected", 1);
//                                            JSONArray fieldItems = new JSONArray();
//                                            fieldItems.add(newDataItem2);
//                                            newDataFieldItems.clear();
//                                            newDataFieldItems.add(fieldItems);
//                                            String itemWid = newDataItem2.getString("itemWid");
//                                            newDatasRow.put("value", itemWid);
//                                        }
//                                    }
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//
//        }
//        return newDatas.getJSONArray("rows");
//    }
//
//    public static JSONObject SigneTempletConvertoSubmitJsonObject(JSONObject templet, JSONObject newDatas) {
//        JSONObject datas = newDatas.getJSONObject("datas");
//
//        Integer isPhoto = datas.getInteger("isPhoto");
//        if (isPhoto.equals(1)) {
//            return null;
//        }
//
//        if (isPhoto.equals(0)) {
//            String signInstanceWid = datas.getString("signInstanceWid");
//            JSONArray newDataExtraField = datas.getJSONArray("extraField");
//            JSONArray templetExtraFieldItems = templet.getJSONArray("extraFieldItems");
//
//            if (newDataExtraField.size() == templetExtraFieldItems.size()) {
//                for (int i = 0; i < newDataExtraField.size(); i++) {
//                    JSONObject newDataFiled = newDataExtraField.getJSONObject(i);
//                    JSONObject templetFiled = templetExtraFieldItems.getJSONObject(i);
//
//                    String extraFieldItemValue = templetFiled.getString("extraFieldItemValue");
//
//                    JSONArray extraFieldItems = newDataFiled.getJSONArray("extraFieldItems");
//
//                    if (extraFieldItems.size() > 1) {
//                        for (int j = 0; j < extraFieldItems.size(); j++) {
//                            JSONObject items = extraFieldItems.getJSONObject(j);
//                            String content = items.getString("content");
//                            if (content.equals(extraFieldItemValue)) {
//                                String wid = items.getString("wid");
//                                templetFiled.put("extraFieldItemWid", wid);
//                            }
//
//                        }
//                    }
//
//                    if (extraFieldItems.size() == 1) {
//                        JSONObject items = extraFieldItems.getJSONObject(0);
//                        String wid = items.getString("wid");
//                        templetFiled.put("extraFieldItemWid", wid);
//                    }
//                }
//            }
//
//            templet.put("signInstanceWid", signInstanceWid);
//        }
//        return templet;
//    }


    public static JSONObject buildSubmitBody(Integer isMalposition, Integer isNeedExtra, String latitude, String longitude, String position, String signInstanceWid, JSONArray extraFieldItems,String signPhotoUrl){
        JSONObject submitBody = new JSONObject(true);
        submitBody.put("longitude",longitude);
        submitBody.put("latitude",latitude);
        submitBody.put("isMalposition",isMalposition);
        submitBody.put("abnormalReason","");
        submitBody.put("signPhotoUrl",signPhotoUrl);
        submitBody.put("isNeedExtra",isNeedExtra);
        submitBody.put("position",position);
        submitBody.put("uaIsCpadaily",true);
        submitBody.put("signInstanceWid",signInstanceWid);
        if (Objects.nonNull(extraFieldItems)){
            submitBody.put("extraFieldItems",extraFieldItems);
        }
        return  submitBody;
    }


    public static JSONArray buildForm(JSONArray newTemplet, JSONArray templet, String collectorPhotoUrl){
        if (!compareCollectorTemplet(newTemplet,templet)){
            return null;
        }
        for (int i = 0; i < templet.size(); i++) {
            JSONObject row = templet.getJSONObject(i);
            JSONObject newRow = newTemplet.getJSONObject(i);
            Integer fieldType = row.getInteger("fieldType");

            if (fieldType.equals(1)){
                String value = row.getString("value");
                newRow.put("value",value);
            }

            if (fieldType.equals(2)){
                JSONArray fieldItems = row.getJSONArray("fieldItems");
                String content = "";
                for (int j = 0; j < fieldItems.size(); j++) {
                    JSONObject jsonObject = fieldItems.getJSONObject(j);
                    Boolean isSelected = jsonObject.getBoolean("isSelected");
                    if (Objects.isNull(isSelected)){
                        continue;
                    }
                    if (isSelected){
                        content = jsonObject.getString("content");
                        break;
                    }
                }
                JSONArray newFieldItems = newRow.getJSONArray("fieldItems");
                JSONObject isSelectObject = null;

                for (int j = 0; j < newFieldItems.size(); j++) {
                    JSONObject jsonObject = newFieldItems.getJSONObject(j);
                    String newContent = jsonObject.getString("content");
                    if (Objects.isNull(newContent)){
                        continue;
                    }
                    if (newContent.equals(content)){
                        jsonObject.put("isSelected",1);
                        isSelectObject = jsonObject;
                        break;
                    }
                }
                newFieldItems.clear();
                newFieldItems.add(isSelectObject);

            }


            if (fieldType.equals(3)){
                JSONArray fieldItems = row.getJSONArray("fieldItems");
                ArrayList<JSONObject> isSelectedList = new ArrayList<>();
                for (int j = 0; j < fieldItems.size(); j++) {
                    JSONObject jsonObject = fieldItems.getJSONObject(j);
                    Boolean isSelected = jsonObject.getBoolean("isSelected");
                    if (Objects.isNull(isSelected)){
                        continue;
                    }
                    if (isSelected){
                        isSelectedList.add(jsonObject);
                    }
                }
                JSONArray newFieldItems = newRow.getJSONArray("fieldItems");
                ArrayList<JSONObject> newSelectedList = new ArrayList<>();
                for (int j = 0; j <newFieldItems.size() ; j++) {
                    JSONObject jsonObject = newFieldItems.getJSONObject(j);
                    String newContent = jsonObject.getString("content");
                    if (Objects.isNull(newContent)){
                        continue;
                    }
                    for (JSONObject x : isSelectedList) {
                        String xcontent = x.getString("content");
                        if (xcontent.equals(newContent)) {
                            newSelectedList.add(jsonObject);
                            break;
                        }
                    }
                }
                newFieldItems.clear();
                newSelectedList.forEach(x->{
                    newFieldItems.add(x);
                });
            }

            if (fieldType.equals(4)){
                newRow.put("value",collectorPhotoUrl);
            }
        }

        return newTemplet;
    }

    public static boolean compareCollectorTemplet(JSONArray templet,JSONArray templ){
        int templetSize = templet.size();
        int templSize = templ.size();

        if (templetSize != templSize){
            return false;
        }

        for (int i = 0; i < templetSize; i++) {
            JSONObject templetJSONObject = templet.getJSONObject(i);
            JSONObject templJSONObject = templ.getJSONObject(i);

            Integer templetFieldType = templetJSONObject.getInteger("fieldType");
            Integer templFieldType = templJSONObject.getInteger("fieldType");
            boolean fieldType = templetFieldType.equals(templFieldType);

//            String templetTitle = templetJSONObject.getString("title");
//            String templTitle = templJSONObject.getString("title");
//            boolean title = templetTitle.equals(templTitle);

            String templetColName = templetJSONObject.getString("colName");
            String templColName = templJSONObject.getString("colName");
            boolean colname = templetColName.equals(templColName);

            boolean result = fieldType && colname;

            if (!result){
                return false;
            }
        }

        return true;
    }

    public static JSONArray buildExtraFieldItems(JSONArray templet, JSONArray extraField) {
        JSONArray returnExtraFieldItems = new JSONArray();
        for (int i = 0; i <templet.size() ; i++) {
            JSONObject temp = templet.getJSONObject(i);
            Integer hasOtherItems = temp.getInteger("hasOtherItems");
            JSONObject items = findItems(extraField, hasOtherItems,i);
            if (items == null){
                System.out.println("没有找到Items");
                return null;
            }
            JSONArray extraFieldItems = temp.getJSONArray("extraFieldItems");
            JSONArray ItemsextraFieldItems = items.getJSONArray("extraFieldItems");
            JSONObject item = findItembyIsSelected(extraFieldItems);
            if (item == null){
                System.out.println("没有找到Item");
                return null;
            }
            String content = item.getString("content");

            JSONObject itemByContent = findItemByContent(ItemsextraFieldItems, content);
            String value = item.getString("value");
            String wid = itemByContent.getString("wid");
            JSONObject it = new JSONObject();
            it.put("extraFieldItemValue",value);
            it.put("extraFieldItemWid",wid);
            returnExtraFieldItems.add(it);
        }
        return returnExtraFieldItems;
    }

    private static JSONObject findItembyIsSelected(JSONArray extraFieldItems) {
        for (int i = 0; i <extraFieldItems.size() ; i++) {
            JSONObject jsonObject = extraFieldItems.getJSONObject(i);
            Boolean isSelected = jsonObject.getBoolean("isSelected");

            if (isSelected != null && isSelected){
                return jsonObject;
            }
        }

        return null;
    }
    private static JSONObject findItemByContent(JSONArray extraFieldItems, String content) {
        final double SIMILARITYFINAL  = 0.51;
        for (int i = 0; i <extraFieldItems.size() ; i++) {
            JSONObject jsonObject = extraFieldItems.getJSONObject(i);
            String content1 = jsonObject.getString("content");
            double similarity = ParticipleUtils.findSimilarity(content, content1);
            if (similarity > SIMILARITYFINAL){
                return jsonObject;
            }
        }
        //找不到匹配项就选择第一个
        JSONObject jsonObject = extraFieldItems.getJSONObject(0);
        return jsonObject;
    }

    private static JSONObject findItems(JSONArray extraField, Integer hasOtherItems,Integer index){
        for (int j = index; j < extraField.size(); j++) {
            JSONObject field = extraField.getJSONObject(j);
            Integer fieldHasOtherItems = field.getInteger("hasOtherItems");
            if (hasOtherItems.equals(fieldHasOtherItems)){
                return field;
            }
        }
        return null;
    }



    public synchronized static void Kq(SqlSessionFactory sqlSessionFactory, HttpRequest httpRequest, FullHttpResponse httpResponse, String uri){

        //https://cqyygz.campusphere.net/wec-counselor-sign-apps/stu/sign/detailSignTaskInst
        if (uri.contains("detailSignTaskInst")){
            SqlSession sqlSession = sqlSessionFactory.openSession();
            UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
            try {
                String content = httpResponse.content().toString(Charset.forName("UTF8"));
                JSONObject json = JSONObject.parseObject(content);
                JSONObject datas = json.getJSONObject("datas");
                JSONObject signedStuInfo = datas.getJSONObject("signedStuInfo");
                String userId = signedStuInfo.getString("userId");
                HttpHeaders headers = httpRequest.headers();
                String host = headers.get("host");
                String origin = "https://"+host;
                UserInfoDo userInfoDo = userInfoMapper.queryByStudentNo(userId);
                if (Objects.isNull(userInfoDo)){
                    userInfoDo = new UserInfoDo();
                    userInfoDo.setOrigin(origin);
                    userInfoDo.setStudentNo(userId);
                    userInfoMapper.insert(userInfoDo);
                }else {
                    userInfoDo.setOrigin(origin);
                    userInfoMapper.update(userInfoDo);
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                sqlSession.close();
            }
        }


        if  (uri.contains("v6/user/new/myMainPage")){
            if (Objects.isNull(httpRequest) || Objects.isNull(httpResponse)){
                return;
            }
            HttpHeaders headers = httpRequest.headers();
            String ua = headers.get("User-Agent");
            if (!ua.contains("cpdaily")){
                return;
            }

            SqlSession sqlSession = sqlSessionFactory.openSession();
            UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
            try {

                System.out.println(headers);
                String cookie = headers.get("Cookie");
                ByteBuf content = httpResponse.content();

                JSONObject infoJson = JSONObject.parseObject(content.toString(StandardCharsets.UTF_8));
                JSONObject data = infoJson.getJSONObject("data");
                String studentNo = data.getString("studentNo");
                String tenantId = data.getString("tenantId");
                UserInfoDo userInfo = userInfoMapper.queryByStudentNo(studentNo);
                String origin =null;
                if (Objects.nonNull(userInfo)){
                    origin = userInfo.getOrigin();
                    if (Objects.isNull(origin) || origin.isEmpty()){
                        String ampCookie = userInfo.getAmpCookie();
                        if (Objects.nonNull(ampCookie) && !ampCookie.isEmpty()){
                            origin = getOrigin(ampCookie,cookie);
                        }
                    }
                }
                UserInfoDo userInfoDo = new UserInfoDo(null, tenantId, ua, cookie, studentNo, origin, null,"");

                content.clear();
                httpResponse.headers().set("content-type","text/html; charset=UTF-8");
                if (Objects.isNull(userInfo)){
                    Integer insert = userInfoMapper.insert(userInfoDo);
                    content.writeBytes("<html><head><title>HELPCATS</title></head><body><script>alert('失败,请检查之前步骤是否操作正确!');</script></body></html>".getBytes(StandardCharsets.UTF_8));
                }else if(Objects.isNull(origin)){
                    content.writeBytes("<html><head><title>HELPCATS</title></head><body><script>alert('Origin未获取到!');</script></body></html>".getBytes(StandardCharsets.UTF_8));
                } else {
                    ScheduleMapper scheduleMapper = sqlSession.getMapper(ScheduleMapper.class);
                    userInfoDo.setId(userInfo.getId());
                    userInfoDo.setOrigin(origin);
                    userInfoDo.setCpdailyInfo(userInfo.getCpdailyInfo());
                    userInfoMapper.update(userInfoDo);
                    init(sqlSession,userInfoDo,"初始化账号");
                    List<ScheduleDo> scheduleDos = scheduleMapper.queryByUid(userInfo.getId());
                    String table = "<table class='table'><caption>任务创建如下,请核对信息!</caption><tr><th>开始时间</th><th>结束时间</th><th>任务类型</th></tr>%s</table>";
                    String task ="";

                    for (ScheduleDo scheduleDo : scheduleDos) {
                        Integer type = scheduleDo.getTaskType();
                        String taskType ="";
                        if (type.equals(1)){
                            taskType = "签到任务";
                        }
                        if (type.equals(2)){
                            taskType =  "信息采集";
                        }
                        if (type.equals(3)){
                            taskType =  "查寝任务";
                        }

                        task += String.format("<tr><td>%tT</td><td>%tT</td><td>%s</td></tr>", scheduleDo.getRateTaskBeginTime(),scheduleDo.getRateTaskEndTime(),taskType);
                    }
                    table = String.format(table,task);

                    String format = buildHtml(html,table,userInfoDo.getCpdailyInfo());
                    content.writeBytes(format.getBytes(StandardCharsets.UTF_8));
                }
            }catch (Exception e){
                System.out.println("错误------------------------------->");
                e.printStackTrace();
            }finally {
                sqlSession.close();
            }
        }

        if (uri.contains("/update/location")){
            httpResponse.setStatus(HttpResponseStatus.OK);
            httpResponse.headers().set("Content-Type","application/json;charset=UTF-8");
            System.out.println(httpRequest.headers());
            System.out.println(httpRequest.uri());
            SqlSession sqlSession = sqlSessionFactory.openSession();
            TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
            HttpHeaders headers = httpRequest.headers();
            String origin = "http://www.baidu.com";
            String ua = headers.get("User-Agent");
            String url = origin+uri;
            HttpUrl httpUrl = HttpUrl.parse(url);
            String flag = httpUrl.queryParameter("flag");
            String lng = httpUrl.queryParameter("lng");
            String lat = httpUrl.queryParameter("lat");
            String address = httpUrl.queryParameter("address");
            String cpdailyInfo = httpUrl.queryParameter("cpdailyInfo");
            try {
                JSONObject jsonObject = decryptDESUserInfo(cpdailyInfo);
                jsonObject.put("lon",lng);
                jsonObject.put("lat",lat);
                String encryptDES = encryptDES(jsonObject.toString());
                String userId = jsonObject.getString("userId");
                List<TempletDo> templetDos = templetMapper.queryTempletByUserId(userId);
                if (flag.equals("1")){
                    templetDos.forEach(templetDo -> {
                        if (templetDo.getTaskType().equals(1)){
                            templetMapper.updateCpdailyInfoById(templetDo.getId(), encryptDES,lng,lat,address);
                        }
                    });
                }

                if (flag.equals("2")){
                    templetDos.forEach(templetDo -> {
                        if (templetDo.getTaskType().equals(2)){
                            templetMapper.updateCpdailyInfoById(templetDo.getId(),encryptDES,lng,lat,address);
                        }
                    });
                }

                if (flag.equals("3")){
                    templetDos.forEach(templetDo -> {
                        templetMapper.updateCpdailyInfoById(templetDo.getId(),encryptDES,lng,lat,address);
                    });
                }
                ByteBuf content = httpResponse.content();
                content.clear();
                JSONObject resultMsg = new JSONObject();
                resultMsg.put("code",0);
                resultMsg.put("msg","定位更新成功!");
                resultMsg.put("data",null);
                content.writeBytes(resultMsg.toString().getBytes());
            } catch (Exception e) {
                ByteBuf content = httpResponse.content();
                content.clear();
                JSONObject resultMsg = new JSONObject();
                resultMsg.put("code",1);
                resultMsg.put("msg","定位更新失败,请重试!");
                resultMsg.put("data",null);
                content.writeBytes(resultMsg.toString().getBytes());
                e.printStackTrace();
            }finally {
                sqlSession.close();
            }


        }

    }

    private static void init(SqlSession sqlSession, UserInfoDo userInfoDo, String keyword) {
        if ("初始化账号".equals(keyword)) {
            UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
            TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
            ScheduleMapper scheduleMapper = sqlSession.getMapper(ScheduleMapper.class);
            try {
                scheduleMapper.delScheduleByUid(userInfoDo.getId());
                templetMapper.delTempletByUid(userInfoDo.getId());
                LoginCat loginCat = new LoginCat(userInfoDo.getOrigin(), userInfoDo.getUa(), userInfoDo.getCookie());
                String modAuthCas = loginCat.login();
                Sign sign = BaseHolder.getApplicationContext().getBean(Sign.class,modAuthCas, userInfoDo.getUa(), userInfoDo.getOrigin());

                Collector collector = BaseHolder.getApplicationContext().getBean(Collector.class,modAuthCas, userInfoDo.getUa(), userInfoDo.getOrigin());


                Attendance attendance = BaseHolder.getApplicationContext().getBean(Attendance.class,modAuthCas, userInfoDo.getUa(), userInfoDo.getOrigin());

                AutoGenTempletAndTask autoGenTempletAndTask = new AutoGenTempletAndTask(sign, userInfoDo,collector,attendance);


                JSONObject stuSignInfosByWeekMonth = sign.getStuSignInfosByWeekMonth();
                JSONObject stuSignInfosDatas = stuSignInfosByWeekMonth.getJSONObject("datas");
                JSONArray rows = stuSignInfosDatas.getJSONArray("rows");
                autoGenTempletAndTask.findMeetConditionsignedTasks(rows);

                JSONObject jsonObject = collector.queryCollectorHistoryList();
                JSONObject collectorData = jsonObject.getJSONObject("datas");
                JSONArray collectorRows = collectorData.getJSONArray("rows");
                autoGenTempletAndTask.findMeetConditionsignedCollector(collectorRows);

                JSONObject signStatisticsInfoByMonth = attendance.getSignStatisticsInfoByMonth();
                JSONObject attendanceDatas = signStatisticsInfoByMonth.getJSONObject("datas");
                JSONArray signedDatas = attendanceDatas.getJSONArray("signedData");
                autoGenTempletAndTask.findMeetConditionsignedAttendance(signedDatas);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("--------------->LoginCat<---------------------");
            }
        }

    }


    public synchronized static void userStoreAppList(FullHttpRequest httpRequest, SqlSessionFactory sqlSessionFactory){
        if (Objects.isNull(httpRequest)){
            return;
        }
        HttpHeaders headers = httpRequest.headers();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
        String cpdailyInfo = headers.get("CpdailyInfo");
        String AmpCookies = headers.get("AmpCookies");
        String ua = headers.get("User-Agent");
        try {
            JSONObject cpdailyInfoObject = Util.decryptDESUserStoreAppList(cpdailyInfo,ua);
            cpdailyInfo = encryptDESUserInfo(cpdailyInfoObject.toString());

            String userId = cpdailyInfoObject.getString("userId");
            UserInfoDo userinfo = userInfoMapper.queryByStudentNo(userId);
            UserInfoDo userInfoDo = new UserInfoDo();
            userInfoDo.setStudentNo(userId);
            userInfoDo.setCpdailyInfo(cpdailyInfo);
            userInfoDo.setAmpCookie(AmpCookies);
            if (userinfo != null){
                userInfoDo.setId(userinfo.getId());
                Integer update = userInfoMapper.update(userInfoDo);
                if (update<=0){
                    System.out.println(userId+":"+"更新失败!1");
                }
                return;

            }else {
                Integer insert = userInfoMapper.insert(userInfoDo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }
    }

    private static String getOrigin(String AmpCookies,String Cookie) throws IOException {
        Headers build = new Headers.Builder()
                .add("AmpCookies", AmpCookies)
                .add("Cookie", Cookie)
                .build();
        OkHttpClient client = OkHttpUtil.getClient();
        Request request = new Request.Builder()
                .headers(build)
                .url("https://pullapp.campushoy.com/wec-open-app/app/userAppListGroupByCategory")
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String content = response.body().string();
        response.close();
        JSONObject contentJson = JSONObject.parseObject(content);
        JSONArray datas = contentJson.getJSONArray("datas");
        int size = datas.size();
        if (size <= 0){
            return null;
        }

        for (int i = 0; i < size; i++) {
            JSONObject data = datas.getJSONObject(i);
            JSONArray apps = data.getJSONArray("apps");
            for (int j = 0; j < apps.size(); j++) {
                JSONObject app = apps.getJSONObject(j);
                String name = app.getString("name");
                boolean finded = name.contains("签到") ||name.equals("信息采集") || name.equals("查寝");
                if (finded){
                    String openUrl = app.getString("openUrl");
                    HttpUrl parse = HttpUrl.parse(openUrl);
                    String url = parse.scheme()+"://"+parse.host();
                    return url;
                }
            }
        }

        return null;
    }


    public static String buildHtml(String html,String ... args){
        for (String arg: args) {
            html = html.replaceFirst("#2909#", arg);
        }
        return html;
    }


    public static boolean matchUri(String uri){
        boolean myMainPage = uri.contains("v6/user/new/myMainPage");
        boolean userStoreAppList = uri.contains("newmobile/client/userStoreAppList");
        boolean detailSignTaskInst = uri.contains("/detailSignInstance");
        boolean location = uri.contains("/update/location");
        boolean detailCollector = uri.contains("/detailCollector");
        boolean qq = uri.contains("/cgi-bin/qm/qr");
        boolean qqImage = uri.contains("wpa/images");
        return myMainPage || userStoreAppList || detailSignTaskInst || location || detailCollector ||qq || qqImage;
    }

    public static boolean matchHost(String host){
        boolean wecres = host.contains("wecres");
        boolean cpdaily = host.contains("cpdaily");
        boolean campushoy = host.contains("campushoy");
        boolean applink = host.contains("applink");
        boolean campusphere = host.contains("campusphere");
        boolean mapBaidu = host.contains("map.baidu");
        boolean maponline = host.contains("maponline");
        boolean jsdelivr = host.contains("jsdelivr");
        return wecres || cpdaily || campushoy || applink || campusphere || mapBaidu || maponline || jsdelivr;
    }




    public static void uploadImage(JSONObject datas,String filepath) throws IOException {
        OkHttpClient client = OkHttpUtil.getClient();
        String host = datas.getString("host");
        String accessKeyId =datas.getString("accessid");
        String signature = datas.getString("signature");
        String fileName = datas.getString("fileName")+".png";
        String policy = datas.getString("policy");
        //所有图片类型
        RequestBody file = RequestBody.create( MediaType.parse("image/png"),new File(filepath));
        MultipartBody multipartBody = new MultipartBody.Builder("----WebKitFormBoundaryKeBqSY2mo3rmvkwh")
                .setType(MultipartBody.FORM)
                .addFormDataPart("key",fileName)
                .addFormDataPart("policy",policy)
                .addFormDataPart("OSSAccessKeyId",accessKeyId)
                .addFormDataPart("success_action_status","200")
                .addFormDataPart("signature",signature)
                .addFormDataPart("file","blob",file)
                .build();

        Request request= new Request.Builder()
                .url(host)
                .post(multipartBody)
                .build();
        Call call = client.newCall(request);
        Response res = call.execute();
        Headers headers = res.headers();

    }





    public static Integer randomRangeNumber(Integer start,Integer end){
        Random random = new Random();
        int i = random.nextInt(end)+start;
        return i;
    }

}



