package io.obase.test.service;

import com.alibaba.fastjson2.JSON;
import io.obase.core.mapping.pipeline.ChangeNotice;
import io.obase.core.mapping.pipeline.IChangeNoticeSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 消息队列 用于发送/接收消息
 */
public class MessageSender implements IChangeNoticeSender {

    /**
     * 用于模拟消息队列的Txt文件路径
     */
    private final String path;

    /**
     * 构造消息队列对象
     */
    public MessageSender() {
        var dir = new File("");
        this.path = dir.getAbsolutePath() + "/TestQueue.txt";
    }

    /**
     * 发送变更通知
     *
     * @param notice 变更通知
     */
    @Override
    public void send(ChangeNotice notice) {
        var saveFile = new File(this.path);
        if (!saveFile.getParentFile().exists()) {
            var flag = saveFile.getParentFile().mkdirs();
            if (!flag)
                System.out.println("MessageSender错误 无法创建文件夹");
        }
        if (!saveFile.exists()) {
            try {
                var flag = saveFile.createNewFile();
                if (!flag)
                    System.out.println("MessageSender错误 无法创建文件" + this.path);
            } catch (IOException e) {
                System.out.println("MessageSender错误 无法创建文件" + this.path);
                throw new RuntimeException(e);
            }
        }
        try (var fileStream = new FileOutputStream(saveFile)) {
            fileStream.write(JSON.toJSONString(notice).getBytes());
            fileStream.flush();
        } catch (IOException e) {
            System.out.println("MessageSender错误 无法写入文件" + this.path);
            throw new RuntimeException(e);
        }
    }
}
