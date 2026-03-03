/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：基于文件的后备存储区的提供程序,提供使用文件的后备存储.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-13 14:18:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 基于文件的后备存储区的提供程序
 *
 * @param <T> 元素类型
 */
public class FileStorageProvider<T> implements IBackupStorageProvider<T> {

    /**
     * 用于存储的文件路径
     */
    private final String filePath;

    /**
     * 元素起始位置集合
     */
    private final List<Long> itemPositions;

    /**
     * 当前元素起始位置索引 未读取前为-1 完全读取后为元素个数
     */
    private int currentPosition = -1;

    /**
     * 构造一个基于文件的后备存储提供程序
     *
     * @param filePath 文件路径 传入空字符串则默认为当前运行文件夹下Obase/BackStorage/Guid.storage
     */
    public FileStorageProvider(String filePath) {
        //无文件路径 构造文件路径
        if (filePath.isEmpty()) {
            String fileName = UUID.randomUUID().toString().replace("-", "") + ".storage";
            filePath = System.getProperty("user.dir") + "//" + fileName;
        }

        this.filePath = filePath;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        this.itemPositions = new ArrayList<>();
    }

    /**
     * 构造一个基于文件的后备存储提供程序
     */
    public FileStorageProvider() {
        this("");
    }

    /**
     * 从后备存储区当前位置读取指定个数的元素
     *
     * @param count 要读取的元素个数
     * @return 读取到的元素的集合，未读取到任何元素返回null。当后备存储区中当前位置之后的元素数少于请求数时，实际读取到的元素数会小于请求数
     */
    public T[] read(int count) {
        //读取0个
        if (count == 0) return null;
        //没有任何元素记录
        if (this.itemPositions.size() == 0) return null;

        //真正的长度
        int realReadCount = count;
        if (this.currentPosition + count > this.itemPositions.size() - 1)
            realReadCount = this.itemPositions.size() - 1 - this.currentPosition;
        //没有元素 返回空
        if (realReadCount <= 0) {
            //已无元素可读 游标推进至总个数之后
            if (this.currentPosition == this.itemPositions.size() - 1) this.currentPosition++;
            return null;
        }

        ArrayList<T> result = new ArrayList<>();

        //从文件中读取
        try (FileInputStream fileStream = new FileInputStream(this.filePath)) {

            //先寻址到当前元素位置
            if (this.currentPosition > -1) {
                fileStream.reset();
                fileStream.skip(this.itemPositions.get(this.currentPosition + 1));
            }

            for (int i = 0; i < realReadCount; i++) {
                //对象长度
                long itemLength;
                if (this.currentPosition + 2 < this.itemPositions.size()) {
                    if (this.currentPosition == -1)
                        itemLength = this.itemPositions.get(this.currentPosition + 2);
                    else
                        itemLength = this.itemPositions.get(this.currentPosition + 2) - this.itemPositions.get(this.currentPosition + 1);
                } else {
                    itemLength = fileStream.available() - this.itemPositions.get(this.currentPosition + 1);
                }

                byte[] itemBytes = new byte[Math.toIntExact(itemLength)];
                //读取
                fileStream.read(itemBytes, 0, itemBytes.length);
                //反序列化
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(itemBytes))) {
                    result.add((T) inputStream.readObject());
                }

                //当前元素++
                this.currentPosition++;
            }

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("从文件中读取错误" + e.getMessage(), e);
        }

        T[] resultArray = (T[]) Array.newInstance(result.get(0).getClass(), result.size());
        return result.toArray(resultArray);
    }

    /**
     * 从后备存储区当前位置反向读取（从后往前）指定个数的元素
     *
     * @param count 要读取的元素个数
     * @return 读取到的元素的集合，未读取到任何元素返回null。当后备存储区中当前位置之后的元素数少于请求数时，实际读取到的元素数会小于请求数
     */
    public T[] reverselyRead(int count) {
        //读取0个
        if (count == 0) return null;

        //没有任何元素记录
        if (this.itemPositions.size() == 0) return null;

        //真正的长度
        int realReadCount = count;
        if (this.currentPosition + 1 - count <= 0) realReadCount = this.currentPosition;
        //没有元素 返回空
        if (realReadCount <= 0) {
            //已无元素可读 游标移动到-1
            if (this.currentPosition == 0) this.currentPosition--;
            return null;
        }

        ArrayList<T> result = new ArrayList<>();

        //从文件中读取
        try (FileInputStream fileStream = new FileInputStream(this.filePath)) {
            for (int i = 0; i < realReadCount; i++) {
                //对象长度
                long itemLength;
                if (this.currentPosition - 1 >= 0) {
                    //先寻址到当前元素位置
                    fileStream.reset();
                    fileStream.skip(this.itemPositions.get(this.currentPosition - 1));
                    if (this.currentPosition > this.itemPositions.size() - 1)
                        //求出元素长度
                        itemLength = fileStream.available() - this.itemPositions.get(this.currentPosition - 1);
                    else
                        //求出元素长度
                        itemLength = this.itemPositions.get(this.currentPosition) - this.itemPositions.get(this.currentPosition - 1);
                } else {
                    itemLength = this.itemPositions.get(this.currentPosition + 1);
                }

                byte[] itemBytes = new byte[Math.toIntExact(itemLength)];
                //读取
                fileStream.read(itemBytes, 0, itemBytes.length);
                //反序列化
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(itemBytes))) {
                    result.add((T) inputStream.readObject());
                }
                //当前元素--
                this.currentPosition--;
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("从文件中读取错误" + e.getMessage(), e);
        }

        T[] resultArray = (T[]) Array.newInstance(result.get(0).getClass(), result.size());
        return result.toArray(resultArray);

    }

    /**
     * 检测后备存储区中是否存在指定的元素
     *
     * @param item 要检查的元素
     * @return 如果存在返回true，否则返回false
     */
    public boolean contains(T item) {
        //序列化当前元素
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(item);
            byte[] targetItemBytes = byteArrayOutputStream.toByteArray();

            try (FileInputStream fileStream = new FileInputStream(this.filePath)) {

                //循环元素
                for (int i = 0; i < this.itemPositions.size(); i++) {
                    //对象长度
                    long itemLength;
                    if (i + 1 < this.itemPositions.size())
                        itemLength = this.itemPositions.get(i + 1) - this.itemPositions.get(i);
                    else
                        itemLength = fileStream.available() - this.itemPositions.get(i);
                    byte[] itemBytes = new byte[Math.toIntExact(itemLength)];
                    //读取
                    fileStream.read(itemBytes, 0, itemBytes.length);
                    //挨个比较
                    if (Arrays.equals(itemBytes, targetItemBytes)) return true;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("从文件中读取错误" + e.getMessage(), e);
        }

        return false;
    }

    /**
     * @param item 元素
     */
    public void append(Iterable<T> item) {
        //序列化当前元素
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
            try (FileOutputStream fileStream = new FileOutputStream(this.filePath)) {

                FileInputStream fileInputStream = new FileInputStream(this.filePath);

                //循环元素
                for (T i : item) {
                    //记录起始位置
                    this.itemPositions.add((long) fileInputStream.available());
                    out.writeObject(i);
                    byte[] iBytes = byteArrayOutputStream.toByteArray();
                    //写入文件
                    fileStream.write(iBytes, 0, iBytes.length);
                    //重置内存流
                    out.reset();
                }

                fileInputStream.close();
            }

        } catch (IOException e) {
            throw new RuntimeException("写入文件错误" + e.getMessage(), e);
        }
    }

    /**
     * 将后备存储区中的位置移动到存储区开始处，即第一个元素之前
     */
    public void reset() {
        this.currentPosition = -1;
    }

    /**
     * 将后备存储区中的位置移动到存储区末尾，即最后一个元素之后
     */
    public void reverselyReset() {
        this.currentPosition = this.itemPositions.size();
    }
}
