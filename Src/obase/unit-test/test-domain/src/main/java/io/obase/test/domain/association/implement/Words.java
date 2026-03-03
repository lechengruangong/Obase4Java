package io.obase.test.domain.association.implement;

/**
 * 表示发言
 */
public class Words {

    /**
     * 发言ID
     */
    private long wordsId;

    /**
     * 内容
     */
    private String content;

    /**
     * 对话ID
     */
    private long dialogueId;

    /**
     * 所属对话
     */
    private Dialogue dialogue;

    /**
     * 获取发言ID
     *
     * @return 发言ID
     */
    public long getWordsId() {
        return this.wordsId;
    }

    /**
     * 设置发言ID
     *
     * @param wordsId 发言ID
     */
    public void setWordsId(long wordsId) {
        this.wordsId = wordsId;
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    public String getContent() {
        return this.content;
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
    }

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
     * 获取所属对话
     *
     * @return 所属对话
     */
    public Dialogue getDialogue() {
        return this.dialogue;
    }

    /**
     * 设置所属对话
     *
     * @param dialogue 所属对话
     */
    public void setDialogue(Dialogue dialogue) {
        this.dialogue = dialogue;
    }
}
