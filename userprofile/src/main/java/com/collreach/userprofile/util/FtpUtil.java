package com.collreach.userprofile.util;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Component
public class FtpUtil {

    @Value("${ftp.host}")
    private String host;

    @Value("${ftp.user}")
    private String user;

    @Value("${ftp.pwd}")
    private String pwd;

    @Value("${ftp.host-dir}")
    private String hostDir;

    FTPClient ftp = null;
    InputStream inputStream = null;

    public String ftpUpload(MultipartFile file, String fileName) throws Exception{
        System.out.println("Start");
        FTPConnect(host, user, pwd);
            //FTP server path is relative. So if FTP account HOME directory is
            // "/home/pankaj/public_html/" and you need to upload
            // files to "/home/pankaj/public_html/wp-content/uploads/image2/",
            // you should pass directory parameter as "/wp-content/uploads/image2/"
        uploadFile(file, fileName);
        disconnect();
        System.out.println("Done");
        return "Done successfully.";
    }


    public String deletingFile(String fileName) {
        boolean fileDeleted = false;
        try {
            FTPConnect(host,user,pwd);
            fileDeleted = deleteFile(fileName);
            disconnect();
        }catch(Exception e){
            return "Could not connect.";
        }
        if(fileDeleted)
            return "deleted Successfully.";
        else
            return "No such file exists.";
    }

    public void FTPConnect(String host, String user, String pwd) throws Exception{
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    public InputStream downloadFile(String path) throws Exception {
        inputStream = new ByteArrayInputStream(new byte[0]);
        try{
            FTPConnect(host, user, pwd);
            download(path);
        }
        catch(Exception e){
            System.out.println("Could not connect: " + e);
        }
        return inputStream;
    }

    public void download(String path) throws IOException {
        inputStream =  this.ftp.retrieveFileStream(path);
    }

    public void uploadFile(MultipartFile file, String fileName)
            throws Exception {
        try(InputStream input = file.getInputStream()){
            this.ftp.storeFile(fileName, input);
        }
    }

    public boolean deleteFile(String fileName) {
        boolean deleted = false;
        try {
            deleted = this.ftp.deleteFile(hostDir + fileName);
        }catch(Exception e){
            System.out.println("-> :" + e);
        }
        return deleted;
    }

    public void disconnect(){
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {
                // do nothing as file is already saved to server
            }
        }
    }

}

//class Ftp{
//    private static Ftp singletonFTP = null;
//    public FTPClient ftpClient;
//
//    private Ftp(){
//        ftpClient = new FTPClient();
//    }
//    public static Ftp getInstance() {
//        if (singletonFTP == null) {
//            singletonFTP = new Ftp();
//        }
//        return singletonFTP;
//    }
//}
