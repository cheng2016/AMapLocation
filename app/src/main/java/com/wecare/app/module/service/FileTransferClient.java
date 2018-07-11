package com.wecare.app.module.service;

import android.os.Handler;

import com.wecare.app.App;
import com.wecare.app.util.Constact;
import com.wecare.app.util.StringTcpUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * 文件传输Client端<br>
 * 功能说明：
 *
 * @author 大智若愚的小懂
 * @author chengzj
 * * @version 1.0
 * @version 1.0
 * @Date 2018年06月25日
 * @Date 2016年09月01日
 */
public class FileTransferClient extends Socket {
    public static final String TAG = "FileTransferClient";

    private static final String SERVER_IP = "sit.wecarelove.com"; // 服务端IP
    private static final int SERVER_PORT = 2993 + 4; // 服务端端口

    private Socket client;

    private FileInputStream fis;

    private DataOutputStream dos;

    private OutputStream fos;

    private InputStream is;

    /**
     * 构造函数<br/>
     * 与服务器建立连接
     *
     * @throws Exception
     */
    public FileTransferClient() throws Exception {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        client.setSoTimeout(5000);
        System.out.println("Cliect[port:" + client.getLocalPort() + "] 成功连接服务端");
    }

    /**
     * 向服务端传输文件
     *
     * @param type 文件类型
     * @param path 文件路径
     * @throws Exception
     */
    public void sendFile(int type, String path) throws Exception {
        try {
            File file = new File(path);
            if (file.exists()) {
                fis = new FileInputStream(file);
//                dos = new DataOutputStream(client.getOutputStream());
                fos = client.getOutputStream();

                String content = StringTcpUtils.buildUploadString(type, "358732036574479", file.length());
                //总包长度 = 文本length +　文件length　＋ 1
                long allByteLength = content.length();
                allByteLength += file.length() + 1;
                System.out.println("======== content length：" + content.getBytes().length + "  ==========");
                System.out.println("======== file length：" + file.length() + "  ==========");
                System.out.println("======== all length：" + allByteLength + "  ==========");
                System.out.println("======== content：" + content + " ==========");
                fos.write(StringTcpUtils.intToByte4((int) allByteLength));
                fos.flush();
                fos.write((byte) content.length());
                fos.flush();
                fos.write(content.getBytes());
                fos.flush();
                // 开始传输文件
                System.out.println("======== 开始传输文件 ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                    progress += length;
                    System.out.print("| " + (100 * progress / file.length()) + "% |");
                }
                System.out.println();
                System.out.println("======== 文件传输成功 ========");

                //接收服务器消息
                is = client.getInputStream();
                System.out.println("======== getInputStream success ==========");
                byte[] buffer = new byte[1024];
                length = 0;
                System.out.println("======== client.isClosed() + " + client.isClosed());

                System.out.println("======== client.isInputShutdown() + " + client.isInputShutdown());
                while (!client.isClosed() && !client.isInputShutdown()
                        && ((length = is.read(buffer)) != -1)) {
                    System.out.println("======== getInputStream read success ==========");
                    if (length > 0) {
                        String message = new String(Arrays.copyOf(buffer,
                                length));
                        //收到服务器过来的消息，就通过Broadcast发送出去
                        System.out.println("======== 服务器返回 ：" + message);
                        String[] results = message.split("\\|");
                        if (results.length > 0) {
                            for (int i = 0; i < results.length; i++) {
                                System.out.print("| " + results[i] + " |");
                            }
                            System.out.println();
                            if ("D01:72".equals(results[5].trim())) {
                                System.out.println("======== upload success ==========");
                            }
                        }
                    }
                    closelAll();
                }
                System.out.println("======== InputStream is close：" + client.isInputShutdown());
                System.out.println("======== outputStream is close：" + client.isOutputShutdown());
                System.out.println("======== socket is close：" + client.isClosed());
                System.out.println("==========  end ===========");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closelAll() throws Exception {
        System.out.println("======== closelAll ==========");
        if (client != null && !client.isClosed()) {
            client.shutdownOutput();
            client.shutdownInput();
            fis.close();
            is.close();
//            dos.close();
            fos.close();
            client.close();
        }
    }


    public void sendFile() throws Exception {
        sendFile(Constact.FILE_TYPE_IMAGE, "F:\\20180515112012.png");
    }

    /**
     * 入口
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            FileTransferClient client = new FileTransferClient(); // 启动客户端连接
            client.sendFile(); // 传输文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
