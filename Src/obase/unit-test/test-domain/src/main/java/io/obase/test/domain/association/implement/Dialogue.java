package io.obase.test.domain.association.implement;

/**
 * 表示对话
 */
public class Dialogue {

    /**
     * 对话ID
     */
    private long dialogueId;

    /**
     * 标题
     */
    private String title;

    /**
     * 获取对话ID
     *
     * @return 对话ID
     */
    public long getDialogueId() {
        return this.dialogueId;
    }

    /**
     * 设置对话ID
     *
     * @param dialogueId 对话ID
     */
    public void setDialogueId(long dialogueId) {
        this.dialogueId = dialogueId;
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
