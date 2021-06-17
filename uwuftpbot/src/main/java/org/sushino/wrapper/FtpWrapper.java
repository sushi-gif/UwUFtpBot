package org.sushino.wrapper;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FtpWrapper {

    private final String ipAddress;
    private final int port;

    private final String username;
    private final String password;
    private String curdir;

    private boolean anonymous = false;
    private FTPClient ftpClient;

    /**
     * This constructor is used for non-anonymous authentication
     * @param ipAddress ipAddress of the server we want to connect to
     * @param port  port number on which the ftp server is listening
     * @param username username for auth
     * @param password  password for auth
     */

    public FtpWrapper(String ipAddress, int port, String username, String password) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * This constructor is used for anonymous authentication
     * @param ipAddress ipAddress of the server we want to connect to
     * @param port  port number on which the ftp server is listening
     */

    public FtpWrapper(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;

        anonymous = true;
        username = "anonymous";
        password = "";
    }

    /**
     *
     * @param tls can be either true or false. if true it means we are using a tls channel
     * @throws IOException If the socket could not be opened.
     */

    public void connect(boolean tls) throws IOException {

        if(tls) ftpClient=new FTPSClient();
        else ftpClient = new FTPClient();

        ftpClient.connect(ipAddress, port);
        ftpClient.login(username, password);

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();

        curdir = ftpClient.printWorkingDirectory();

    }

    /**
     * Used to close the ftp connection
     * @throws IOException If an error occurs while disconnecting.
     */

    public void close() throws IOException {
        ftpClient.disconnect();
    }

    /**
     * Used to get the remote directories
     * @return the directories
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */

    public String getDirs() throws IOException {

        FTPFile[] files = ftpClient.listFiles();
        StringBuilder out = new StringBuilder("*** " + curdir + " ***");
        for (FTPFile file : files) {
            String details = file.getName();
            if (file.isDirectory()) {
                details = "-> [" + details + "]";
            }else details += " " + file.getSize() / 1024 + "kb";
            out.append("\n")
                    .append(details);
        }

        return out.toString();

    }

    /**
     *
     * @param dir the remote directory we want to go to
     * @return true if the directory existed, otherwise false
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */

    public boolean changeDir(String dir) throws IOException {
        boolean b = ftpClient.changeWorkingDirectory(dir);
        if (b) curdir = ftpClient.printWorkingDirectory();
        return b;
    }

    /**
     *
     * @param filename the name of the remote file we want to output
     * @return custom value if the size is > 4096, null if the file doesn't exist or is a directory, otherwise it returns the file content
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String catFile(String filename) throws IOException {

        File tempFile = File.createTempFile("randomprefx-", "-randomsufx");
        FileOutputStream fos = new FileOutputStream(tempFile);
        if(!ftpClient.retrieveFile(curdir + "/" + filename, fos)) return "Can't output a directory!";
        if(tempFile.length() > 4096) return "File is too big to cat, download it instead.";

        fos.close();
        FileInputStream fis = new FileInputStream(tempFile);

        byte[] data = new byte[(int) tempFile.length()];
        fis.read(data);

        fis.close();
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     *
     * @param chatId id of the current chat/session
     * @param filename name of the remote file we want to retrieve
     * @return null if the file doesn't exist or is a directory, otherwise it returns the file itself
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public File getFile(Long chatId, String filename) throws IOException {
        File tempFile = new File(chatId + "-" + filename);
        FileOutputStream fos = new FileOutputStream(tempFile);
        if(!ftpClient.retrieveFile(curdir + "/" + filename, fos)) return null;

        fos.close();
        return tempFile;
    }

    /**
     *
     * @param document file that we want to upload
     * @param originalFileName original file name
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public void uploadFile(File document,String originalFileName) throws IOException {
        FileInputStream fis = new FileInputStream(document);
        ftpClient.storeFile(originalFileName,fis);
    }

    @Override
    public String toString() {
        return "org.sushino.wrapper.FtpWrapper{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", anonymous=" + anonymous +
                '}';
    }
}
