package com.wecare.app.data.entity;

public class GetImageResp {
    /**
     * data : {"file_http_path":"https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=gQFN8DwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyWFVnMmhyTWNjZGsxMWN0VGhyMUkAAgTMyzVbAwSAUQEA"}
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
         * file_http_path : https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=gQFN8DwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyWFVnMmhyTWNjZGsxMWN0VGhyMUkAAgTMyzVbAwSAUQEA
         */

        private String file_http_path;

        public String getFile_http_path() {
            return file_http_path;
        }

        public void setFile_http_path(String file_http_path) {
            this.file_http_path = file_http_path;
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
