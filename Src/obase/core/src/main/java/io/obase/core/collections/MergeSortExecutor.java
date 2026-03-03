/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：归并排序执行器,提供基于文件的归并排序.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-12 17:15:48
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.collections;

import io.obase.common.ObjectReferencePack;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 归并排序执行器 基于文件的归并排序
 *
 * @param <TItem> 元素类型
 */
public class MergeSortExecutor<TItem> {

    /**
     * 每块
     */
    private final List<TItem> blockItems;

    /**
     * 每块的大小 当读入数据超过此值时 写至文件
     */
    private final int blockSize;
    /**
     * 是否为倒序
     */
    private final Boolean isDesc;
    /**
     * 每块的对象表示
     */
    private final List<MergeSortFileBlock<TItem>> mergeSortFileBlocks;
    /**
     * 排序结果文件路径
     */
    private final String resultFilePath;
    /**
     * 每个元素在块文件内的位置
     */
    private final List<Long> resultItemPosition;
    /**
     * 存储块元素的临时文件名称
     */
    private final String tempFilePath;
    /**
     * 元素的比较器
     */
    private Comparator<TItem> comparator;
    /**
     * 当前元素起始位置索引 未读取前为-1 完全读取后为元素个数
     */
    private int currentPosition = -1;

    /**
     * 共有多少块
     */
    private int blockCount;

    /**
     * 是否为初次读取
     */
    private Boolean isFirstRead = true;

    /**
     * 构造一个归并排序执行器 并指定每块大小
     *
     * @param isDesc    是否倒排
     * @param blockSize 每个块的大小
     */
    private MergeSortExecutor(Boolean isDesc, int blockSize) {
        this.isDesc = isDesc;
        this.blockSize = blockSize;
        this.blockItems = new ArrayList<>(blockSize);
        //排序用临时文件路径
        String fileName = UUID.randomUUID().toString().replace("-", "");
        this.tempFilePath = System.getProperty("user.dir") + "\\MergeSortTempFile\\" + fileName;
        //临时文件所对应的块
        this.mergeSortFileBlocks = new ArrayList<>();
        //排序结果文件路径
        this.resultFilePath = System.getProperty("user.dir") + "\\" + fileName;
        this.resultItemPosition = new ArrayList<>();
        //创建临时文件夹
        File dir = new File(System.getProperty("user.dir") + "\\MergeSortTempFile");
        if (!dir.exists()) {
            boolean result = dir.mkdir();
            if (!result)
                throw new RuntimeException("无法创建文件夹" + dir.getAbsolutePath());
        }

    }

    /**
     * 构造一个归并排序执行器 并指定比较器 每块大小 是否为倒序排序
     *
     * @param comparator 比较器
     * @param isDesc     是否为倒序排序
     * @param blockSize  每块大小
     */
    public MergeSortExecutor(Comparator<TItem> comparator, Boolean isDesc, int blockSize) {
        this(isDesc, blockSize);

        this.comparator = comparator;
    }

    /**
     * 递归删文件夹和文件
     *
     * @param info 文件
     */
    private static void deleteFileByDirectory(File info) {
        if (info.isDirectory()) {
            File[] files = info.listFiles();
            if (files != null && files.length > 0) {
                for (File tmpFile : files) {
                    deleteFileByDirectory(tmpFile);
                }
            }
        }
        if (info.exists()) {
            info.delete();
        }
    }

    /**
     * 共有多少块
     *
     * @return 共有多少块
     */
    public int getBlockCount() {
        return this.blockCount;
    }

    /**
     * 将元素放入排序器 当放入的元素存满一块时 将存入文件
     *
     * @param item 元素
     */
    public void putIn(TItem item) {
        //当前块是否还有余量
        if (this.blockItems.size() >= this.blockSize) {
            this.saveBlock();
        }
        this.blockItems.add(item);
    }

    /**
     * 结束放入 并触发排序
     * <para>注意:如在放入时不正确指示结束放入 可能会丢失元素</para>
     */
    public void endPutIn() {
        //是否有剩余的未保存至外存的数据
        if (this.blockItems.size() > 0) this.saveBlock();

        this.sort();
    }

    /**
     * 保存某块的数据至外存
     */
    private void saveBlock() {
        //每块排序
        if (this.comparator != null)
            this.blockItems.sort(this.comparator);
        else
            this.blockItems.sort((o1, o2) -> 0);


        //每个元素在块文件内的位置
        ArrayList<Long> itemPosition = new ArrayList<>();

        //存储至每块文件
        String path = String.format("%s%d", this.tempFilePath, this.blockCount);
        this.writeInFile(this.blockItems, path, itemPosition);

        //构造成文件块
        this.mergeSortFileBlocks.add(new MergeSortFileBlock<>(this.blockCount, this.tempFilePath + this.blockCount,
                itemPosition));

        //块数增加
        this.blockCount++;
        //清除此块内容
        this.blockItems.clear();
    }

    /**
     * 对读入的结果进行排序
     */
    private void sort() {
        MinHeap<MergeSortFileBlock<TItem>.BlockItem> minHeap = new MinHeap<>((o1, o2) -> this.comparator.compare(o1.item, o2.item), this.mergeSortFileBlocks.size());
        //读取每个里面最小的元素
        for (MergeSortFileBlock<TItem> fileBlock : this.mergeSortFileBlocks) {
            ObjectReferencePack<Boolean> isSuccess = new ObjectReferencePack<>();
            MergeSortFileBlock<TItem>.BlockItem blockItem = fileBlock.read(isSuccess);
            if (isSuccess.realValue) minHeap.enqueue(blockItem);
        }

        //临时存储用
        ArrayList<TItem> tempList = new ArrayList<>();
        //读到全部读完
        while (!this.mergeSortFileBlocks.stream().allMatch(MergeSortFileBlock::isEnded)) {
            //弹出 放入临时存储
            MergeSortFileBlock<TItem>.BlockItem min = minHeap.dequeue(true);
            tempList.add(min.getItem());
            //放入对应的新元素
            MergeSortFileBlock<TItem> dequeueFrom = this.mergeSortFileBlocks.stream().filter(p -> p.getSequence() == min.getSequence()).findFirst().orElse(null);
            //从序号相同的块内读取
            ObjectReferencePack<Boolean> isSuccess = new ObjectReferencePack<>();
            if (dequeueFrom == null)
                throw new RuntimeException("未能找到序号为:" + min.getSequence() + "的块对象");
            MergeSortFileBlock<TItem>.BlockItem blockItem = dequeueFrom.read(isSuccess);
            if (isSuccess.realValue) minHeap.enqueue(blockItem);
            //如果读满临时存储的数量 则写入硬盘
            if (tempList.size() >= 5000) {
                this.writeInFile(tempList, this.resultFilePath, this.resultItemPosition);
                //存完这一批 清空
                tempList.clear();
            }

        }

        //补上最后一组
        if (tempList.size() > 0) this.writeInFile(tempList, this.resultFilePath, this.resultItemPosition);

        //清除临时文件
        deleteFileByDirectory(new File(System.getProperty("user.dir") + "\\MergeSortTempFile"));
    }

    /**
     * 将某一集合写入文件
     *
     * @param tempList     可枚举集合
     * @param path         路径
     * @param itemPosition 元素位置集合
     */
    private void writeInFile(List<TItem> tempList, String path, List<Long> itemPosition) {

        //序列化当前元素
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
            try (FileOutputStream fileStream = new FileOutputStream(path, true)) {

                FileInputStream fileInputStream = new FileInputStream(path);

                //循环元素
                for (TItem i : tempList) {
                    //记录起始位置
                    itemPosition.add((long) fileInputStream.available());
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
     * 取出结果
     *
     * @param isSuccess 是否成功
     * @return 返回结果
     */
    public TItem takeOut(ObjectReferencePack<Boolean> isSuccess) {
        //没有任何元素记录
        if (this.resultItemPosition.size() == 0)
            return null;

        //初次读取 重置读取器
        if (this.isFirstRead) {
            this.reset();
            this.isFirstRead = false;
        }


        //正序还是倒序读取
        return !this.isDesc ? this.read(isSuccess) : this.reverselyRead(isSuccess);
    }

    /**
     * 重置读取器
     */
    private void reset() {
        if (!this.isDesc)
            this.currentPosition = -1;
        else
            this.currentPosition = this.resultItemPosition.size();
    }

    /**
     * 正序读取一个元素
     *
     * @param isSuccess 是否成功
     * @return 一个元素
     */
    private TItem read(ObjectReferencePack<Boolean> isSuccess) {
        //没有任何元素记录
        if (this.resultItemPosition.size() == 0) {
            isSuccess.realValue = false;
            return null;
        }


        //真正的长度
        int realReadCount = 1;
        if (this.currentPosition + 1 > this.resultItemPosition.size() - 1)
            realReadCount = this.resultItemPosition.size() - 1 - this.currentPosition;
        //没有元素 返回空
        if (realReadCount <= 0) {
            //已无元素可读 游标推进至总个数之后
            if (this.currentPosition == this.resultItemPosition.size() - 1) this.currentPosition++;
            isSuccess.realValue = false;
            return null;
        }

        TItem result = null;

        try (FileInputStream fileStream = new FileInputStream(this.resultFilePath)) {
            //先寻址到当前元素位置
            if (this.currentPosition > -1)
                fileStream.reset();
            fileStream.skip(this.resultItemPosition.get(this.currentPosition + 1));

            for (int i = 0; i < realReadCount; i++) {
                //对象长度
                long itemLength;
                if (this.currentPosition + 2 < this.resultItemPosition.size()) {
                    if (this.currentPosition == -1)
                        itemLength = this.resultItemPosition.get(this.currentPosition + 2);
                    else
                        itemLength = this.resultItemPosition.get(this.currentPosition + 2) -
                                this.resultItemPosition.get(this.currentPosition + 1);
                } else {
                    itemLength = fileStream.available() - this.resultItemPosition.get(this.currentPosition + 1);
                }

                byte[] itemBytes = new byte[Math.toIntExact(itemLength)];
                //读取
                fileStream.read(itemBytes, 0, itemBytes.length);

                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(itemBytes))) {
                    result = (TItem) inputStream.readObject();
                }

                //当前元素++
                this.currentPosition++;
            }
            isSuccess.realValue = true;
            return result;

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("从文件中读取错误" + e.getMessage(), e);
        }
    }

    /**
     * 倒序读取一个元素
     *
     * @param isSuccess 是否成功
     * @return 一个元素
     */
    private TItem reverselyRead(ObjectReferencePack<Boolean> isSuccess) {
        //没有任何元素记录
        if (this.resultItemPosition.size() == 0) {
            isSuccess.realValue = false;
            return null;
        }

        //真正的长度
        int realReadCount = 1;
        if (this.currentPosition + 1 - 1 <= 0) realReadCount = this.currentPosition;
        //没有元素 返回空
        if (realReadCount <= 0) {
            //已无元素可读 游标移动到-1
            if (this.currentPosition == 0) this.currentPosition--;
            isSuccess.realValue = false;
            return null;
        }

        TItem result = null;

        try (FileInputStream fileStream = new FileInputStream(this.resultFilePath)) {

            for (int i = 0; i < realReadCount; i++) {
                //对象长度
                long itemLength;
                if (this.currentPosition - 1 >= 0) {
                    //先寻址到当前元素位置
                    fileStream.reset();
                    fileStream.skip(this.resultItemPosition.get(this.currentPosition - 1));
                    if (this.currentPosition > this.resultItemPosition.size() - 1)
                        //求出元素长度
                        itemLength = fileStream.available() - this.resultItemPosition.get(this.currentPosition - 1);
                    else
                        //求出元素长度
                        itemLength = this.resultItemPosition.get(this.currentPosition) -
                                this.resultItemPosition.get(this.currentPosition - 1);
                } else {
                    itemLength = this.resultItemPosition.get(this.currentPosition + 1);
                }

                byte[] itemBytes = new byte[Math.toIntExact(itemLength)];
                //读取
                fileStream.read(itemBytes, 0, itemBytes.length);
                //反序列化
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(itemBytes))) {
                    result = (TItem) inputStream.readObject();
                }


                //当前元素--
                this.currentPosition--;
            }

            isSuccess.realValue = true;
            return result;

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("从文件中读取错误" + e.getMessage(), e);
        }
    }

    /**
     * 表示一个归并排序用到的文件块
     *
     * @param <T> 元素
     */
    private static class MergeSortFileBlock<T> {

        /**
         * 文件路径
         */
        private final String filePath;

        /**
         * 元素起始位置集合
         */
        private final List<Long> itemPositions;

        /**
         * 块序号
         */
        private final int sequence;

        /**
         * 当前元素起始位置索引 未读取前为-1 完全读取后为元素个数
         */
        private int currentPosition = -1;

        /**
         * 是否已读完
         */
        private Boolean isEnded;

        /**
         * 构造一个归并排序用到的文件块
         *
         * @param sequence      序号
         * @param filePath      文件路径
         * @param itemPositions 每个元素在块内的位置
         */
        public MergeSortFileBlock(int sequence, String filePath, List<Long> itemPositions) {
            this.sequence = sequence;
            this.filePath = filePath;
            this.itemPositions = itemPositions;
        }

        /**
         * 块序号
         *
         * @return 块序号
         */
        public int getSequence() {
            return this.sequence;
        }

        /**
         * 是否已读完
         *
         * @return 是否已读完
         */
        public Boolean isEnded() {
            return this.isEnded;
        }

        /**
         * 从文件块内读取一个块元素
         *
         * @param isSuccess 包含一个布尔值 指示是否成功
         * @return 块内元素
         */
        public BlockItem read(ObjectReferencePack<Boolean> isSuccess) {
            //没有任何元素记录
            if (this.itemPositions.size() == 0) {
                isSuccess.realValue = false;
                return null;
            }

            //真正的长度
            int realReadCount = 1;
            if (this.currentPosition + 1 > this.itemPositions.size() - 1)
                realReadCount = this.itemPositions.size() - 1 - this.currentPosition;
            //没有元素 返回空
            if (realReadCount <= 0) {
                //已无元素可读 游标推进至总个数之后
                if (this.currentPosition == this.itemPositions.size() - 1) this.currentPosition++;
                isSuccess.realValue = false;
                this.isEnded = true;
                return null;
            }

            try (FileInputStream fileStream = new FileInputStream(this.filePath)) {

                //先寻址到当前元素位置
                if (this.currentPosition > -1) {
                    fileStream.reset();
                    fileStream.skip(this.itemPositions.get(this.currentPosition + 1));
                }

                //对象长度
                long itemLength;
                if (this.currentPosition + 2 < this.itemPositions.size()) {
                    if (this.currentPosition != -1)
                        itemLength = this.itemPositions.get(this.currentPosition + 2) - this.itemPositions.get(this.currentPosition + 1);
                    else
                        itemLength = this.itemPositions.get(this.currentPosition + 2);
                } else {
                    itemLength = fileStream.available() - this.itemPositions.get(this.currentPosition + 1);
                }

                byte[] itemBytes = new byte[Math.toIntExact(itemLength)];
                //读取
                fileStream.read(itemBytes, 0, itemBytes.length);
                //当前元素++
                this.currentPosition++;

                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(itemBytes))) {
                    isSuccess.realValue = true;

                    return new BlockItem((T) inputStream.readObject(), this.sequence);
                }

            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("从文件中读取错误" + e.getMessage(), e);
            }
        }

        /**
         * 表示一个块内元素 包括所属块的序号和元素
         */
        public class BlockItem {

            /**
             * 元素
             */
            private final T item;
            /**
             * 序号
             */
            private final int sequence;

            /**
             * 构造一个块内元素
             *
             * @param item     元素
             * @param sequence 所属的块的序号
             */
            public BlockItem(T item, int sequence) {

                this.item = item;
                this.sequence = sequence;
            }

            /**
             * 序号
             *
             * @return 序号
             */
            public int getSequence() {
                return this.sequence;
            }

            /**
             * 元素
             *
             * @return 元素
             */
            public T getItem() {
                return this.item;
            }
        }
    }


}

