package red.fuyun.campusphere;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import red.fuyun.dao.ScheduleMapper;
import red.fuyun.dao.TempletMapper;
import red.fuyun.pojo.Do.ScheduleDo;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.pojo.Do.UserInfoDo;
import red.fuyun.util.BaseHolder;
import red.fuyun.util.OkHttpUtil;
import red.fuyun.util.ParticipleUtils;
import red.fuyun.util.Util;
import java.io.IOException;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/5 22:49
 */
public class AutoGenTempletAndTask {

    private SqlSessionFactory sqlSessionFactory = BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);
    private Sign sign;
    private UserInfoDo userInfoDo;
    private Collector collector;
    private Attendance attendance;
    public AutoGenTempletAndTask(Sign sign, UserInfoDo userInfoDo,Collector collector,Attendance attendance){
        this.sign = sign;
        this.userInfoDo = userInfoDo;
        this.collector = collector;
        this.attendance = attendance;
    }


    private class TimeRange implements Comparable<TimeRange>{
        LocalTime beginTime;
        LocalTime endTime;
        JSONObject data;
        JSONArray collectData;
        public TimeRange( LocalTime beginTime,LocalTime endTime){
            this.beginTime = beginTime;
            this.endTime = endTime;
        }
        @Override
        public int compareTo(@NotNull TimeRange target) {
            int begin = target.beginTime.compareTo(this.beginTime);
            int end = target.endTime.compareTo(this.endTime);
            begin = Math.abs(begin);
            end = Math.abs(end);
            return begin+end;
        }
    }



    public TimeRange getTimeRange(JSONObject signedTask){
        String rateTaskBeginTime = signedTask.getString("rateTaskBeginTime");
        String rateTaskEndTime = signedTask.getString("rateTaskEndTime");
        if(Objects.isNull(rateTaskBeginTime) || Objects.isNull(rateTaskEndTime)){
            return null;
        }

        TimeRange timeRange = null;
        try {

            LocalTime beginTime = LocalTime.parse(rateTaskBeginTime);
            LocalTime endTime = LocalTime.parse(rateTaskEndTime);
            timeRange  = new TimeRange(beginTime,endTime);
        } catch (DateTimeParseException ex) {
            System.out.println("时间解析错误");
            throw ex;
        } catch (RuntimeException ex) {
            System.out.println("运行时出错");
            throw ex;
        }
        return timeRange;
    }

    public boolean checkSignTaskIsExists(TimeRange timeRange, ArrayList<TimeRange> timeRangeList){

        boolean match = false;
        match = timeRangeList.stream().anyMatch(timeR -> {
            return timeR.compareTo(timeRange) == 0;
        });
        return match;
    }

    public void findMeetConditionsignedCollector(JSONArray rows) throws IOException {
        ArrayList<String> subjects = new ArrayList<>();
        ArrayList<TimeRange>  timeRangeList = new ArrayList<>();
        ArrayList<JSONObject> matchRow = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        HashMap<String,Integer> subjectMap = new HashMap<>();
        ArrayList<JSONArray> templets = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            JSONObject row = rows.getJSONObject(i);
            Integer isHandled = row.getInteger("isHandled");
            if (isHandled.equals(1)){

                String subject = row.getString("subject");
                String endTimeStr = row.getString("endTime");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime endTime = LocalDateTime.parse(endTimeStr, dateTimeFormatter);
                Duration duration = Duration.between(endTime,now);
                System.out.println(endTime.toString()+"---当前时间差了:"+duration.toDays());
                //只要七天之内的数据
                if (duration.toDays() >8){
                    continue;
                }


                boolean match = subjects.stream().anyMatch(sub -> {
                    return sub.equals(subject);
                });

                if (match){
                    continue;
                }
                subjects.add(subject);
                matchRow.add(row);
            }
        }


        //删除无关的信息采集
        //根据信息采集出现的次数 数量来剔除杂量信息



//
//        Set<Map.Entry<String, Integer>> entrySet = subjectMap.entrySet();
//        for (Map.Entry<String, Integer> entry :entrySet) {
//            String key = entry.getKey();
//            Integer value = entry.getValue();
//            //当前类型的信息采集数量 少于总数量的四分之一 就删除
//            if (value < isHandledCount/4){
//                //删除的是subjects里面的采集信息的名称信息
//                boolean remove = subjects.remove(key);
//            }
//        }

        //此时subjects内容已经都是 满足条件的信息采集名称
        //根据subjects 的内容删除不符合条件的 matchRow
//        Iterator<JSONObject> iterator = matchRow.iterator();
//        while (iterator.hasNext()){
//            JSONObject next = iterator.next();
//            String subject = next.getString("subject");
//            boolean match = subjects.stream().anyMatch(sub -> {
//                return sub.equals(subject);
//            });
//            if (!match){
//                iterator.remove();
//            }
//        }



        String wid = null;
        for (JSONObject row : matchRow) {
            String formWid = row.getString("formWid");
            wid = row.getString("wid");
            String startTimeStr = row.getString("startTime");
            String endTimeStr = row.getString("endTime");

            JSONObject formFields = collector.getFormFields(formWid, wid);
            JSONArray dataRows = formFields.getJSONObject("datas").getJSONArray("rows");

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalTime startTime = LocalDateTime.parse(startTimeStr,dateTimeFormatter).toLocalTime();
            LocalTime endTime = LocalDateTime.parse(endTimeStr,dateTimeFormatter).toLocalTime();
            TimeRange timeRange = new TimeRange(startTime,endTime);
            timeRange.collectData = dataRows;
            templets.add(dataRows);
            timeRangeList.add(timeRange);
        }
        if (Objects.isNull(wid)){
            System.out.println();
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession();

        delRepeatCollector(templets);
        delRepeatTimeRange(timeRangeList);
        try {
            ArrayList<TempletDo> templetDos = insertTemplet(templets, wid, sqlSession);
            insertCollectorSchedule(timeRangeList,templetDos,sqlSession);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }


    }

    public void findMeetConditionsignedAttendance(JSONArray rows) throws IOException {
        if (Objects.isNull(rows) || rows.size() <= 0){
            return;
        }
        ArrayList<JSONObject> list  =  new ArrayList<>();


        for (int i = 0; i <rows.size() ; i++) {
            JSONObject row = rows.getJSONObject(i);
            String signDateStr = row.getString("signDate");
            if (Objects.isNull(signDateStr) || signDateStr.isEmpty()){
                System.out.println("signDateStr有问题");
                continue;
            }
            LocalDate signDate = LocalDate.parse(signDateStr);
            LocalDate now = LocalDate.now();
            Period between = Period.between(signDate, now);
            long days =  between.getDays();
            if (days>8){
                continue;
            }
            list.add(row);
        }

        Map<String, List<JSONObject>> map = list.stream().collect(Collectors.groupingBy((row) -> {
            String signDate = row.getString("signDate");
            return signDate;
        }));

        Set<Map.Entry<String, List<JSONObject>>> entrySet = map.entrySet();
        int maxCount = 0;
        String maxKey = "";
        for (Map.Entry<String, List<JSONObject>> entry :entrySet) {
            String key = entry.getKey();
            List<JSONObject> value = entry.getValue();
            if (value.size()>maxCount){
                maxCount = value.size();
                maxKey= key;
            }
        }
        List<JSONObject> finalResultList = map.get(maxKey);

        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            for (int i = 0; i < finalResultList.size(); i++) {
                JSONObject row = finalResultList.get(i);
                String signInstanceWid = row.getString("signInstanceWid");
                String signWid = row.getString("signWid");
                String stuSignWid = row.getString("stuSignWid");
                JSONObject detailSignInstance = attendance.detailSignInstance(signInstanceWid, signWid);
                JSONObject datas = detailSignInstance.getJSONObject("datas");
                insertTempletAttendance(datas,sqlSession);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sqlSession.close();
        }




    }


    private void insertTempletAttendance(JSONObject datas,SqlSession sqlSession) throws Exception {
        TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
        ScheduleMapper scheduleMapper = sqlSession.getMapper(ScheduleMapper.class);
        Integer isPhoto = datas.getInteger("isPhoto");
        String rateTaskBeginTime = datas.getString("rateTaskBeginTime");
        String rateTaskEndTime = datas.getString("rateTaskEndTime");
        String signAddress = datas.getString("signAddress");
        String signPhotoUrl = datas.getString("signPhotoUrl");
        signPhotoUrl = Objects.isNull(signPhotoUrl)?"":signPhotoUrl;
        String latitude = datas.getString("latitude");
        String longitude = datas.getString("longitude");
        String cpdailyInfoStr = userInfoDo.getCpdailyInfo();

        JSONObject cpdaily = parseCpdailyInfo(cpdailyInfoStr);
        String systemName = cpdaily.getString("systemName");
        String systemVersion = cpdaily.getString("systemVersion");
        String model = cpdaily.getString("model");
        String deviceId = cpdaily.getString("deviceId");
        String appVersion = cpdaily.getString("appVersion");
        String userId = cpdaily.getString("userId");
        cpdaily.put("lon",longitude);
        cpdaily.put("lat",latitude);
        String cpdailyInfo = Util.encryptDES(cpdaily.toString());
        JSONArray rows = new JSONArray();
        TempletDo templetDo = new TempletDo(null, userInfoDo.getId(), cpdailyInfo, rows.toString(), longitude, latitude, model, signAddress, appVersion, systemName, deviceId, userId, "", "",signPhotoUrl,3);
        templetMapper.insert(templetDo);
        LocalTime beginTime = LocalTime.parse(rateTaskBeginTime);
        LocalTime endTime = LocalTime.parse(rateTaskEndTime);
        LocalTime now = LocalTime.now();
        Integer state = now.isBefore(endTime)?0:2;
        ScheduleDo scheduleDo = new ScheduleDo(null,userInfoDo.getId(),templetDo.getId(), Time.valueOf(beginTime),Time.valueOf(endTime),3,0,"",state);
        scheduleMapper.insert(scheduleDo);
    }


    private void delRepeatTimeRange(ArrayList<TimeRange> timeRangeList) {
        int size = timeRangeList.size();
        if (size <= 1){
            return;
        }
        for (int i = 0; i < size; i++) {
            TimeRange timeRange = timeRangeList.get(i);
            Iterator<TimeRange> it = timeRangeList.iterator();
            while(it.hasNext()){
                TimeRange next = it.next();
                if (timeRange == next){
                    continue;
                }
                boolean b = timeRange.beginTime.compareTo(next.beginTime) == 0;
//                boolean b = Util.compareCollectorTemplet(timeRange, next);
                if (b){
                    it.remove();
                    size--;
                }
            }
        }

    }

    private void insertCollectorSchedule(ArrayList<TimeRange> timeRangeList, ArrayList<TempletDo> templetDos, SqlSession sqlSession) {
        ScheduleMapper sqlSessionMapper = sqlSession.getMapper(ScheduleMapper.class);
        Integer uid = userInfoDo.getId();
        timeRangeList.forEach(timeRange -> {
            JSONArray collectData = timeRange.collectData;
            for (TempletDo templetDo : templetDos) {
                String data = templetDo.getData();
                JSONArray templet = JSONArray.parseArray(data);
                if (Util.compareCollectorTemplet(collectData, templet)) {
                    Integer tid = templetDo.getId();
                    Time rateTaskBeginTime = Time.valueOf(timeRange.beginTime);
                    Time rateTaskEndTime = Time.valueOf(timeRange.endTime);

                    LocalTime now = LocalTime.now();
                    Integer state = now.isBefore(timeRange.beginTime) ? 0 : 2;
                    ScheduleDo scheduleDo = new ScheduleDo(null, uid, tid, rateTaskBeginTime, rateTaskEndTime, 2, 0, "", state);
                    Integer insert = sqlSessionMapper.insert(scheduleDo);
                    if (insert <= 0) {
                        System.out.println(rateTaskBeginTime + "~" + rateTaskEndTime + ":任务创建失败");
                    }
                    break;
                }
            }
        });


    }


    public  ArrayList<TempletDo> insertTemplet(ArrayList<JSONArray> templets, String wid,SqlSession sqlSession) throws Exception {
        TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
        ArrayList<TempletDo> templetDos = new ArrayList<>();
        String cpdailyInfo = userInfoDo.getCpdailyInfo();
        JSONObject cpdaily =parseCpdailyInfo(cpdailyInfo);
        String systemName = cpdaily.getString("systemName");
        String systemVersion = cpdaily.getString("systemVersion");
        String model = cpdaily.getString("model");
        String deviceId = cpdaily.getString("deviceId");
        String appVersion = cpdaily.getString("appVersion");
        String userId = cpdaily.getString("userId");
        String latitude =cpdaily.getString("lat");
        String longitude = cpdaily.getString("lon");
        JSONObject detailCollector = collector.getDetailCollector(wid);
        JSONObject datas = detailCollector.getJSONObject("datas");
        JSONObject collector = datas.getJSONObject("collector");
        String address = collector.getString("address");
        String signAddress =address;
        for (JSONArray rows : templets) {
            String collectorPhotoUrl = "";
            for (int i = 0; i < rows.size(); i++) {
                JSONObject row = rows.getJSONObject(i);
                Integer fieldType = row.getInteger("fieldType");
                if (fieldType.equals(4)) {
                    collectorPhotoUrl = row.getString("value");
                    break;
                }
            }
            TempletDo templetDo = new TempletDo(null, userInfoDo.getId(), cpdailyInfo, rows.toString(), longitude, latitude, model, signAddress, appVersion, systemName, deviceId, userId, "", collectorPhotoUrl,"",2);
            templetMapper.insert(templetDo);
            templetDos.add(templetDo);
        }
        return templetDos;
    }

    /**
     * 从历史签到任务中 寻找模板  要求是自己签到的
     * @param rows
     * @return
     */
    public void findMeetConditionsignedTasks(JSONArray rows) throws Exception {
        ArrayList<TimeRange>  timeRangeList = new ArrayList<>();
        ArrayList<JSONObject> templets = new ArrayList<>();
        ArrayList<String>  signPhotoUrls = new ArrayList<>();
        Integer taskCount=0;
        Integer count = 0;
        boolean flag = true;
        for (int i = 0; i < rows.size(); i++) {
            JSONObject row = rows.getJSONObject(i);
            //获取这是那一天
            String dayInMonthStr = row.getString("dayInMonth");
            LocalDate dayInMonth = LocalDate.parse(dayInMonthStr);
            LocalDate now = LocalDate.now();
            //计算今天和dayInMonth 差了几天
            long day = now.until(dayInMonth, ChronoUnit.DAYS);
            long day2  = dayInMonth.until(now,ChronoUnit.DAYS);
            //如果和当前日期差了有十二天则跳过这一天
            if (day2>7){
                continue;
            }

            JSONArray signedTasks = row.getJSONArray("signedTasks");

            for (int j = 0; j <signedTasks.size(); j++) {
                JSONObject signedTask = signedTasks.getJSONObject(j);
                //检测当前时间段任务是否已经添加 没添加则添加到 timeRangeList 里
                TimeRange timeRange = getTimeRange(signedTask);
                if(Objects.isNull(timeRange)){
                    continue;
                }
                if (checkSignTaskIsExists(timeRange,timeRangeList)){
                    continue;
                }

                //最坏的情况一个时间段一个模板 ,最好的情况多个时间同一个模板
                // 第一步根据 满足条件的  条件 1.此签到任务据今天12天及以内 2.是自己签到的 3.当前时间段的任务没有被创建过

                //第二步首先根据满足条件的签到任务创建模板 ,然后把任务放到列表里 ,再把模板写入到数据库 再从数据库读取出来
                // 再把签到任务和模板匹配 确定签到任务用哪个模板 在把任务写入到数据库
                //创建模板的适配器
                boolean isSuccess = autoCreateAdapter(signedTask, templets,signPhotoUrls);

                //判断模板是否创建成功
                if (isSuccess){
                    //把当前任务先存储
//                    scheduleTask.add(signedTask);
                    int lastTemplet = templets.size() - 1;
                    timeRange.data = templets.get(lastTemplet);
                    //把当前时间段存储起来
                    timeRangeList.add(timeRange);
                }

            }
        }

        if (flag){
            System.out.println("任务未采集完成,当前任务数为:"+templets.size());
        }else {
            System.out.println("任务采集完成,当前任务数为:"+templets.size());
        }
        delRepeat(templets);

        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            //在这把模板写入到数据库
            ArrayList<TempletDo> templetDos = insertTemplet(templets,signPhotoUrls,sqlSession);
            //在把任务写入到数据库
            insertSchedule(timeRangeList,templetDos,sqlSession);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            sqlSession.close();
        }
    }


    //插入任务 任务属于那个模板是根据模板的选择项比较的 没有比较 isPhoto 和 isneedExrt 可能会出现问题 后期出现注意排查
    private void insertSchedule(ArrayList<TimeRange> timeRangeList, ArrayList<TempletDo> templetDos, SqlSession sqlSession) {
        ScheduleMapper sqlSessionMapper = sqlSession.getMapper(ScheduleMapper.class);
        timeRangeList.forEach(timeRange -> {
            JSONObject data = timeRange.data;
            JSONArray extraField = data.getJSONArray("extraField");
            Integer isPhoto = data.getInteger("isPhoto");

            if (Objects.isNull(extraField)){
                extraField = new JSONArray();
            }

            for (int i = 0; i < templetDos.size() ; i++) {
                TempletDo templetDo = templetDos.get(i);
                String templet = templetDo.getData();
                JSONArray tempExtraField= JSONArray.parseArray(templet);
                //比较 isPhoto
                String signPhotoUrl = templetDo.getSignPhotoUrl();
                Integer signIsPhoto = Objects.isNull(signPhotoUrl) || signPhotoUrl.isEmpty()?0:1;
                if (!isPhoto.equals(signIsPhoto)){
                    continue;
                }
                if (compareTemplet(extraField,tempExtraField)){
                    Integer uid = userInfoDo.getId();
                    Integer tid = templetDo.getId();
                    Time rateTaskBeginTime = Time.valueOf(timeRange.beginTime);
                    Time rateTaskEndTime = Time.valueOf(timeRange.endTime);
                    LocalTime now = LocalTime.now();
                    Integer state = now.isBefore(timeRange.endTime)?0:2;
                    ScheduleDo scheduleDo = new ScheduleDo(null,uid,tid, rateTaskBeginTime,rateTaskEndTime,1,0,"",state);
                    Integer insert = sqlSessionMapper.insert(scheduleDo);
                    if (insert<=0){
                        System.out.println(rateTaskBeginTime+"~"+rateTaskEndTime+":任务创建失败");
                    }
                    break;

                }

            }
        });
    }



    public  ArrayList<TempletDo> insertTemplet(ArrayList<JSONObject> templets, ArrayList<String> signPhotoUrls, SqlSession sqlSession) throws Exception {
        TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
        ArrayList<TempletDo>  templetDos = new ArrayList<>();
            String cpdailyInfo = userInfoDo.getCpdailyInfo();
            JSONObject cpdaily =parseCpdailyInfo(cpdailyInfo);
            String systemName = cpdaily.getString("systemName");
            String systemVersion = cpdaily.getString("systemVersion");
            String model = cpdaily.getString("model");
            String deviceId = cpdaily.getString("deviceId");
            String appVersion = cpdaily.getString("appVersion");
            String userId = cpdaily.getString("userId");

            templets.forEach(templet->{
                String signAddress = templet.getString("signAddress");
                String latitude = templet.getString("latitude");
                String longitude = templet.getString("longitude");
                JSONArray data = templet.getJSONArray("extraField");
                if (Objects.isNull(data)){
                    data = new JSONArray();
                }
                Integer isPhoto = templet.getInteger("isPhoto");
                String signUrl = "";
                if (isPhoto.equals(1)){
                    signUrl = signPhotoUrls.get(Util.randomRangeNumber(0,signPhotoUrls.size()));
                }
                cpdaily.put("lon",longitude);
                cpdaily.put("lat",latitude);
                String cpdailyExtension = null;
                try {
                    cpdailyExtension = Util.encryptDES(cpdaily.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TempletDo templetDo = new TempletDo(null, userInfoDo.getId(), cpdailyExtension, data.toString(), longitude, latitude, model, signAddress, appVersion, systemName, deviceId, userId,signUrl,"","",1);
                Integer insert = templetMapper.insert(templetDo);
                if (insert>0){
                    templetDos.add(templetDo);
                }
            });
        return templetDos;
    }


    public JSONObject parseCpdailyInfo(String cpdailyInfo) throws Exception {
        JSONObject cpdailyInfoJson = Util.decryptDESUserInfo(cpdailyInfo);
        String systemName = cpdailyInfoJson.getString("systemName");
        String systemVersion = cpdailyInfoJson.getString("systemVersion");
        String model = cpdailyInfoJson.getString("model");
        String deviceId = cpdailyInfoJson.getString("deviceId");
        String appVersion = cpdailyInfoJson.getString("appVersion");
        String userId = cpdailyInfoJson.getString("userId");
        String latitude =cpdailyInfoJson.getString("lat");
        String longitude = cpdailyInfoJson.getString("lon");
        JSONObject cpdaily = new JSONObject();
        cpdaily.put("systemName",systemName);
        cpdaily.put("systemVersion",systemVersion);
        cpdaily.put("model",model);
        cpdaily.put("deviceId",deviceId);
        cpdaily.put("appVersion",appVersion);
        cpdaily.put("userId",userId);
        cpdaily.put("lon",longitude);
        cpdaily.put("lat",latitude);
        return cpdaily;
    }

    private void delRepeat(ArrayList<JSONObject> templets) {
        int size = templets.size();

        if (size <= 1){
            return;
        }

        JSONObject templet = templets.get(0);
        JSONArray extraField = templet.getJSONArray("extraField");
        Integer isNeedExtra = templet.getInteger("isNeedExtra");
        Integer isPhoto = templet.getInteger("isPhoto");
        Iterator<JSONObject> iterator = templets.iterator();

        while (iterator.hasNext()){
            JSONObject next = iterator.next();
            if (next.equals(templet)){
                continue;
            }
            Integer nextIsNeedExtra = next.getInteger("isNeedExtra");
            Integer nextIsPhoto = next.getInteger("isPhoto");
            //检测两个模板 是否要求图片 和表单 如果要求都不同那么一定不是相同的模板
            boolean isEquals = nextIsNeedExtra.equals(isNeedExtra) && nextIsPhoto.equals(isPhoto);
            if (!isEquals){
                continue;
            }
            JSONArray extraField1 = next.getJSONArray("extraField");
            boolean compareTemplet = compareTemplet(extraField, extraField1);
            if (compareTemplet){
                iterator.remove();
            }
        }


    }

    private void delRepeatCollector(ArrayList<JSONArray> templets){
        int size = templets.size();

        if (size <= 1){
            return;
        }


        for (int i = 0; i < size; i++) {
            JSONArray templet = templets.get(i);
            Iterator<JSONArray> it = templets.iterator();
            while(it.hasNext()){
                JSONArray next = it.next();
                if (templet == next){
                    continue;
                }
                boolean b = Util.compareCollectorTemplet(templet, next);
                if (b){
                    it.remove();
                    size--;
                }
            }
        }



    }


    /**
     * 判断 当前签到任务  是自己签到的 还是其他操作 自己签到的返回false
     * @param datas  签到任务的详细信息
     * @return
     */
    private boolean isReplaceSigned(JSONObject datas){
        Integer signType = datas.getInteger("signType");
        if (signType == null || signType.intValue() !=1){
            return true;
        }
        return false;
    }


    private boolean autoCreateAdapter(JSONObject signedTask, ArrayList<JSONObject> templets, ArrayList<String> signPhotoUrls){
        JSONObject detailSignTaskInst = null;
        try {
            detailSignTaskInst = sign.detailSignInstance(signedTask);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("自动创建任务网络IO异常");
        }
        //验证结果是正确 访问是够成功
        if (!OkHttpUtil.CheckRequestSuccess(detailSignTaskInst,"autoCreateTask")){
            return false;
        }
        JSONObject datas = detailSignTaskInst.getJSONObject("datas");

        if (isReplaceSigned(datas)){
            return false;
        }

        Integer isNeedExtra = datas.getInteger("isNeedExtra");
        Integer isPhoto = datas.getInteger("isPhoto");
        boolean isBuildTemplteSuccess = false;

        if (isNeedExtra.equals(0) && isPhoto.equals(0)){
            templets.add(datas);
            return true;
        }

        if (isNeedExtra.equals(1)){
            JSONArray extraField = datas.getJSONArray("extraField");
            //构建模板
            isBuildTemplteSuccess = buildTemplte(extraField);
            if (isBuildTemplteSuccess){
                templets.add(datas);
            }

        }

        if (isPhoto.equals(1)){
            String signPhotoUrl = datas.getString("signPhotoUrl");
            signPhotoUrl = signPhotoUrl == null ? "" : signPhotoUrl;
            signPhotoUrls.add(signPhotoUrl);
        }

        return isBuildTemplteSuccess;
    };


    /**
     * 构建模板内容
     * @param extraField
     *
     */
    public boolean buildTemplte(JSONArray extraField){
        //此循环删除不需要的选项
        for (int i = 0; i < extraField.size(); i++) {
            JSONObject field = extraField.getJSONObject(i);
            JSONArray extraFieldItems = field.getJSONArray("extraFieldItems");
            int size = extraFieldItems.size();
//            if (size>1){
                boolean[] booleans = new boolean[size];
                boolean result =false;
                for (int j = 0; j < size; j++) {
                    JSONObject items = extraFieldItems.getJSONObject(j);
                    Boolean isSelected = items.getBoolean("isSelected");
//                    if (isSelected == null || !isSelected){
//                        extraFieldItems.remove(j);
//                    }
                    if (isSelected !=null){
                        booleans[j] = isSelected;
                    }

                }

                for (int j = 0; j < size; j++) {
                    result = result || booleans[j];
                }
                if (!result){
                    return result;
                }
//            }

//            if (size == 1){
//                JSONObject items = extraFieldItems.getJSONObject(0);
//                Boolean isSelected = items.getBoolean("isSelected");
//                if (isSelected == null || !isSelected){
//                    return false;
//                }
//            }
        }
        //判断模板在模板集合中是否已经创建
//        boolean templetIsExists = templets.stream().anyMatch(templet -> {
//            return compareTemplet(extraField, templet);
//        });
//        if (templetIsExists){
//            return false;
//        }
        //当集合中不存在模板时 把改模板放到集合中
//        templets.add(extraField);
        return true;
    }

    /**
     * 比较模板是否相同
     * @param templet
     * @param templ
     * @return
     */
    public boolean compareTemplet(JSONArray templet,JSONArray templ){
        int templetSize = templet.size();
        int templSize = templ.size();
        if (templSize != templetSize) {
            return false;
        }

        for (int i = 0; i < templSize; i++) {
            JSONObject templField = templ.getJSONObject(i);
            Integer templHasOtherItems = templField.getInteger("hasOtherItems");
            JSONObject templetField = null;

            //循环用于查找相同序号的表单项
            for (int j = 0; j < templetSize; j++) {
                JSONObject innerTempletField = templet.getJSONObject(i);
                Integer templetHasOtherItems = innerTempletField.getInteger("hasOtherItems");
                if (templetHasOtherItems.equals(templHasOtherItems)) {
                    templetField = innerTempletField;
                    break;
                }
            }

            if (templetField == null) {
                System.out.println("templetField:为null");
                return false;
            }

            //选项问题的个数
            JSONArray templetExtraFieldItems = templetField.getJSONArray("extraFieldItems");
            JSONArray templExtraFieldItems = templField.getJSONArray("extraFieldItems");

            //选项的问题 title
            String templetTitle = templetField.getString("title");
            String templTitle = templField.getString("title");
            //选项的问题 value

            //检测 两个 同一个选项问题的个数是否相等 不相等一定不是同一个模板
            boolean conditionOne = templetExtraFieldItems.size() == templExtraFieldItems.size();
            if (!conditionOne) {
                return false;
            }

            //检测 两个 同一个选项问题的title 是否相似不相似视为不同
            double similarity = ParticipleUtils.findSimilarity(templetTitle, templTitle);
            boolean conditionTwo =similarity>0.55D;
            if (!conditionTwo) {
                return false;
            }
            JSONObject templetItem = (JSONObject)templetExtraFieldItems.stream().filter(item -> {
                JSONObject it = (JSONObject) item;
                Boolean isSelected = it.getBoolean("isSelected");
                return Objects.nonNull(isSelected) && isSelected;
            }).findFirst().get();

            JSONObject templItem = (JSONObject)templExtraFieldItems.stream().filter(item -> {
                JSONObject it = (JSONObject) item;
                Boolean isSelected = it.getBoolean("isSelected");
                return Objects.nonNull(isSelected) && isSelected;
            }).findFirst().get();

            if (Objects.nonNull(templetItem) && Objects.nonNull(templItem)) {
                String templetItemContent = templetItem.getString("content");
                String templItemContent = templItem.getString("content");
                return templetItemContent.equals(templItemContent);
            }
        }
        return true;
    }


}
