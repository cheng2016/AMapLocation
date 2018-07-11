package com.wecare.app.data.entity;


public class QueryBusinessResp {


    /**
     * data : {"head_image_url":"http://thirdwx.qlogo.cn/mmopen/JMiaRzR1XSxq1nZv8DtckcjrxOF5afRl6aNnwBDbJ6TNWV5cUwCVFdz9Rsuiah9etzR9f8sQThibyicIstCNr0oUaJDv8iaset80l/132","lng":"","dev":"","nick_name":"永尚＆理想","name":"","style":"","lat":""}
     * info : {"code":"200","info":"请求成功"}
     */

    private DataBean data;
    private InfoBean info;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }

    public static class DataBean {
        /**
         * head_image_url : http://thirdwx.qlogo.cn/mmopen/JMiaRzR1XSxq1nZv8DtckcjrxOF5afRl6aNnwBDbJ6TNWV5cUwCVFdz9Rsuiah9etzR9f8sQThibyicIstCNr0oUaJDv8iaset80l/132
         * lng :
         * dev :
         * nick_name : 永尚＆理想
         * name :
         * style :
         * lat :
         */

        private String head_image_url;
        private String lng;
        private String dev;
        private String nick_name;
        private String name;
        private String style;
        private String lat;

        public String getHead_image_url() {
            return head_image_url;
        }

        public void setHead_image_url(String head_image_url) {
            this.head_image_url = head_image_url;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public String getDev() {
            return dev;
        }

        public void setDev(String dev) {
            this.dev = dev;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }
    }

    public static class InfoBean {
        /**
         * code : 200
         * info : 请求成功
         */

        private String code;
        private String info;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
