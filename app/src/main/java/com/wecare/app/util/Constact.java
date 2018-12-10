package com.wecare.app.util;

public interface Constact {
    String VERTICAL_LINE = "|";

    String GPS_TITLE = "C04";

    String APP_KEY = "4a5f9cdc8ec1557f0b8fa2456145439c";

    String DEFAULT_LENGTH = "1024";

    String END_MARK = "\u0001";

    String GPS_CONTENT_TITLE = "D06:1,3";

    String SEPARATOR = ",";

    String CONNECT_TITLE = "C01";

    String CONNECT_CONTENT = "D01:1";

    String GET_DATA_TITLE = "C02";

    String GET_DATA_CONTENT = "D01:6";

    String COMMAND_TITLE = "C16";

    String COMMAND_SUCCESS_CONTENT = "D01:72";

    String UPLOAD_TITLE = "C08";

    String FILE_TITLE = "D10:";

    int FILE_TYPE_IMAGE = 1;

    int FILE_TYPE_VOICE = 2;

    int FILE_TYPE_VEDIO = 3;

    int FILE_TYPE_COLLISION = 4;

    int FILE_TYPE_IMAGE_FORMAT = 1;

    int FILE_TYPE_VOICE_FORMAT = 2;

    int FILE_TYPE_VEDIO_FORMAT = 3;


    //指令集
    int COMMAND_INIT = 1;

    int COMMAND_HEART = 2;

    int COMMAND_LOCATION = 3;

    int COMMAND_GET_DATA = 9;

    int COMMAND_TACK_IMAGE = 61;

    int COMMAND_TACK_VOICE = 62;

    int COMMAND_TACK_VIDEO = 63;

    int COMMAND_SUCCESS = 72;

    int COMMAND_GO_NAVI = 91;

    int COMMAND_WX_NAVI = 92;

    int COMMAND_START_LOCATION = 99;

    //cameraid
    //前摄像头
    int CAMERA_FRONT = 0;

    int CAMERA_REAR = 1;

    String MY_KEY = "my_demo";
}
