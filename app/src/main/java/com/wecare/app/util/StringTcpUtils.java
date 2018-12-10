package com.wecare.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringTcpUtils {
    /**
     * 获取初始化数据
     *
     * @param imei
     * @return
     */
    public static String buildGetDataReq(String imei) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.GET_DATA_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(Constact.GET_DATA_CONTENT.length()).append(Constact.VERTICAL_LINE);
        sb.append(Constact.GET_DATA_CONTENT).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }

    /**
     * 初始化指令
     *
     * @param imei
     * @return
     */
    public static String buildInitReq(String imei) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.CONNECT_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT.length()).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }

    /**
     * 初始化指令
     *
     * @param imei
     * @return
     */
    public static String buildInitResp(String imei) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.CONNECT_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT.length()).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        sb.append(Constact.END_MARK);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }

    /**
     * client request
     * 心跳连接请求指令
     *
     * @param imei
     * @return
     */
    public static String buildHeartReq(String imei) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.CONNECT_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_HEART).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT.length()).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }

    /**
     * sever response
     * 服务器心跳回应指令
     *
     * @param imei
     * @return
     */
    public static String buildHeartResp(String imei) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.CONNECT_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_HEART).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT.length()).append(Constact.VERTICAL_LINE);
        sb.append(Constact.CONNECT_CONTENT).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        sb.append(Constact.END_MARK);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }


    public static String buildGpsContent(double latitude, double longtitude,
                                         double height, float speed, float direction,
                                         int signalnum, float signal, long pisitionType,
                                         long positionTime, long lastPositionTime,
                                         String baseStationInfo) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.GPS_CONTENT_TITLE).append(Constact.SEPARATOR);
        sb.append(latitude).append(Constact.SEPARATOR);
        sb.append(longtitude).append(Constact.SEPARATOR);
        sb.append(height).append(Constact.SEPARATOR);
        sb.append(speed).append(Constact.SEPARATOR);
        sb.append(direction).append(Constact.SEPARATOR);
        sb.append(signalnum).append(Constact.SEPARATOR);
        sb.append(signal).append(Constact.SEPARATOR);
        sb.append(pisitionType).append(Constact.SEPARATOR);
        sb.append(simpleDateFormat.format(new Date(positionTime))).append(Constact.SEPARATOR);
        sb.append(simpleDateFormat.format(new Date(lastPositionTime))).append(Constact.SEPARATOR);
        sb.append(baseStationInfo);
        return sb.toString();
    }


    public static String buildGpsString(String imei, String content) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.GPS_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(content.length()).append(Constact.VERTICAL_LINE);
        sb.append(content).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }

    public static String buildSuccessString(String imei, String dateStr) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.COMMAND_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_SUCCESS_CONTENT.length()).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_SUCCESS_CONTENT).append(Constact.VERTICAL_LINE);
        sb.append(dateStr).append(Constact.VERTICAL_LINE);
        sb.append(Constact.END_MARK);
        return sb.toString();
    }

    /**
     * 上传文件string帮助类
     *
     * @param type   文件类型
     * @param imei   设备imei号
     * @param length 内容大小
     * @return
     */
    public static String buildUploadString(int type, String imei, long length) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.UPLOAD_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(length).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date())).append(Constact.VERTICAL_LINE);
//        sb.append(Constact.FILE_TITLE);
        sb.append(type).append(Constact.VERTICAL_LINE);
        sb.append(type).append(Constact.VERTICAL_LINE);
        sb.append(length).append(Constact.VERTICAL_LINE);
        return sb.toString();
    }

    public static String buildUploadString(int type, String imei, long length, long timeTemp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.UPLOAD_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(length).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date(timeTemp))).append(Constact.VERTICAL_LINE);
//        sb.append(Constact.FILE_TITLE);
        sb.append(type).append(Constact.VERTICAL_LINE);
        sb.append(type).append(Constact.VERTICAL_LINE);
        sb.append(length).append(Constact.VERTICAL_LINE);
        return sb.toString();
    }

    /**
     * 上传带上用户id
     * @param userId
     * @param type
     * @param imei
     * @param length
     * @param timeTemp
     * @return
     */
    public static String buildUploadString(String userId, int type, String imei, long length, long timeTemp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(Constact.UPLOAD_TITLE).append(Constact.VERTICAL_LINE);
        sb.append(Constact.COMMAND_INIT).append(Constact.VERTICAL_LINE);
        sb.append(Constact.APP_KEY).append(Constact.VERTICAL_LINE);
        sb.append(imei).append(Constact.VERTICAL_LINE);
        sb.append(length).append(Constact.VERTICAL_LINE);
        sb.append(simpleDateFormat.format(new Date(timeTemp))).append(Constact.VERTICAL_LINE);
//        sb.append(Constact.FILE_TITLE);
        sb.append(type + userId).append(Constact.VERTICAL_LINE);
        sb.append(type).append(Constact.VERTICAL_LINE);
        sb.append(length).append(Constact.VERTICAL_LINE);
        return sb.toString();
    }

    /**
     * int整数转换为4字节的byte数组
     *
     * @param i 整数
     * @return byte数组
     */
    public static byte[] intToByte4(int i) {
        byte[] targets = new byte[4];
        targets[3] = (byte) (i & 0xFF);
        targets[2] = (byte) (i >> 8 & 0xFF);
        targets[1] = (byte) (i >> 16 & 0xFF);
        targets[0] = (byte) (i >> 24 & 0xFF);
        return targets;
    }

    public static void main(String[] args) {

        String[] strings = new String[8];
        strings[0] = Constact.GPS_TITLE + Constact.VERTICAL_LINE;
        strings[1] = Constact.COMMAND_INIT + Constact.VERTICAL_LINE;
        strings[2] = "2" + Constact.VERTICAL_LINE;
        strings[3] = "3" + Constact.VERTICAL_LINE;
        strings[4] = "4" + Constact.VERTICAL_LINE;
        strings[5] = "5" + Constact.VERTICAL_LINE;
        strings[6] = "6" + Constact.VERTICAL_LINE;
        strings[7] = "7";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
        }
        System.out.println(sb.toString());

        System.out.println(buildGpsString("355855624415838", "D06:1,3,114.01954666666667,22.533786666666668,63.1,0.801,294.68,7,1,20180613133900,20180613133500,"));

        System.out.println(buildHeartReq("355855624415839"));

        String response = "C08|1|4a5f9cdc8ec1557f0b8fa2456145439c|000000000000000|6|D01:72|20180625164213|\u0001\u0001\u0001";
        String[] results = response.split("\\|");
        if (results.length > 0) {
            if (results[5].startsWith("D01")) {
                String result = results[5].substring(4);
                System.out.println(result);
            }
            if ("D01:72".equals(results[5])) {
                System.out.println("UPLOAD SUCCESS");
            }
        }

        String a1 = "C01|1|4a5f9cdc8ec1557f0b8fa2456145439c|000000000000000|5|D01:1|20180626034421|\u0001\u0001\u0001";
        String a2 = "C01|1|4a5f9cdc8ec1557f0b8fa2456145439c|000000000000000|5|D01:1|20180626034421|\u0001\u0001\u0001";

        String a3 = "C02|1|4a5f9cdc8ec1557f0b8fa2456145439c|000000000000000|76|D04:47.106.148.192,2993,sit.wecarelove.com,15,108000,1800;D05:20180703145316|20180703065318|\u0001\u0001\u0001";

        String[] a3s = a3.split("\\|");
        System.out.println(a3s[5]);

        a3s = a3s[5].split("\\;");
        System.out.println(a3s[0]);
        String s = a3s[0].substring(4);
        System.out.println(s);
        a3s = s.split("\\,");
        for (int i = 0; i < a3s.length; i++) {
            System.out.print(a3s[i] + " | ");
        }

        System.out.println();
        System.out.println(buildSuccessString("000000000000000", "20180710105320"));
//        C16|1|4a5f9cdc8ec1557f0b8fa2456145439c|000000000000000|6|D01:72|20180710105320|
//        C16|1|4a5f9cdc8ec1557f0b8fa2456145439c|000000000000000|6|D01:61|20180710105320|
    }
}
