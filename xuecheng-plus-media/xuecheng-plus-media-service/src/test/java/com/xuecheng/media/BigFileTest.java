package com.xuecheng.media;

import com.sun.nio.sctp.PeerAddressChangeNotification;
import io.minio.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.Bytes;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {

    @Test
    public void chunkFile() throws IOException {

        // 读取文件，然后设置分块大小，分块数量
        // 创建分块路径，读取文件然后写对相应的分块
        File file = new File("D:\\1.mp4");
        int chunkSize = 1024 * 1024 * 100;
        int chunkNumber = (int) Math.ceil(file.length() * 1.0 / chunkSize);

        RandomAccessFile raw_file = new RandomAccessFile(file, "r");


        String chunkPath = "D:\\test\\";
        File chunkFolder = new File(chunkPath);
        if (chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        byte[] b = new byte[1024];
        for (int i = 0; i < chunkNumber; i++) {
            File chunkFile = new File(chunkPath + i);
            if (chunkFile.exists()) {
                chunkFile.delete();
            }

            boolean newFile = chunkFile.createNewFile();
            if (newFile) {
                RandomAccessFile target_file = new RandomAccessFile(chunkFile, "rw");
                int len = -1;
                while ((len = raw_file.read(b)) != -1) {
                    target_file.write(b, 0, len);
                    if (chunkFile.length() >= chunkSize) {
                        break;
                    }
                }
                target_file.close();
            }

        }

        raw_file.close();

    }


    @Test
    public void mergeFile() throws IOException {
        String chunkPath = "D:/test/";
        String mergePath = "D:/test1/1.mp4";

        File file = new File(chunkPath);

        File mergeFile = new File(mergePath);


        File[] files = file.listFiles();
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });


        RandomAccessFile target_file = new RandomAccessFile(mergeFile, "rw");
        byte[] b = new byte[1024];
        for (File chunkFile : list) {
            //遍历chunkpath读取其中的文件，然后同时写入到merge中
            RandomAccessFile source_file = new RandomAccessFile(chunkFile, "r");
            int len = -1;

            while ((len = source_file.read(b)) != -1) {
                target_file.write(b, 0, len);
            }
            source_file.close();
        }
        target_file.close();


        File file1 = new File("D:/1.mp4");
        File file2 = new File("D:/test1/1.mp4");
        FileInputStream fileInputStream = new FileInputStream(file1);
        String s = DigestUtils.md5Hex(fileInputStream);

        FileInputStream fileInputStream1 = new FileInputStream(file2);
        String s1 = DigestUtils.md5Hex(fileInputStream1);


        System.err.println("{dfasf}" + s + "  " + s1);

    }


}
