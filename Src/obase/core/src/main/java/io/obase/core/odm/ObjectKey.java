/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象标识.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 15:26:40
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.Utils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象标识
 */
public class ObjectKey implements Serializable {

    /**
     * 对象类型（模型类型）的名称
     */
    private final String typeName;
    /**
     * 对象类型（模型类型）的命名空间
     */
    private final String typeNamespace;
    /**
     * 对象的模型类型
     */
    private transient final StructuralType objectType;
    /**
     * 键为ObjectKeyMember 的Attribute，值为键为ObjectKeyMember对象
     */
    private Map<String, ObjectKeyMember> members;

    /**
     * 创建对象标识实例
     *
     * @param modelType 对象的类型
     * @param members   成员集合
     * @throws IllegalArgumentException 标识列不能为空
     */
    public ObjectKey(StructuralType modelType, List<ObjectKeyMember> members) {
        if (members == null || members.size() == 0)
            throw new IllegalArgumentException("构造对象标识失败," + modelType.getFullName() + "标识列不能为空");
        this.members = new HashMap<>();
        for (ObjectKeyMember member : members) {
            this.members.put(member.getAttribute(), member);
        }

        String fullName = modelType.getClrType().getName();

        int index = fullName.lastIndexOf(".");

        if (index == -1)
            throw new IllegalArgumentException("不支持为定义于默认包内的类" + fullName + "生成对象标识");

        String nameSpace = fullName.substring(0, index);
        String name = modelType.getClrType().getSimpleName();

        this.typeNamespace = nameSpace;
        this.typeName = name;
        this.objectType = modelType;
    }

    /**
     * 构造对象标识实例
     *
     * @param typeNameStr 字符串
     * @param members     成员
     */
    private ObjectKey(String typeNameStr, List<ObjectKeyMember> members) {
        String[] typeNameSplits = typeNameStr.split("\\.");
        this.members = new HashMap<>();
        for (ObjectKeyMember member : members) {
            this.members.put(member.getAttribute(), member);
        }
        this.typeNamespace = typeNameSplits[0];
        this.typeName = typeNameSplits[1];
        this.objectType = null;
    }

    /**
     * 根据按既定规则编码的字符串生成ObjectKey实例。
     *
     * @param keyString 对象键字符串
     * @return 对象键
     */
    public static ObjectKey fromString(String keyString) {
        //用正则切分字符串
        String[] keySplits = keyString.split("(\\[[^]]*])");
        //构造标识成员
        List<ObjectKeyMember> keyMemberList = new ArrayList<>();

        for (String keySplit : keySplits) {
            //空的和不含冒号的不是要处理的
            if (!keySplit.contains(":"))
                continue;
            //此keySplit即为[a:1-b:2...z:26]
            String[] memberSplit = keySplit.replace("[", "").replace("]", "").split("-");
            for (String member : memberSplit) {
                String[] filedSplits = member.split(":");
                if (filedSplits.length == 2) keyMemberList.add(new ObjectKeyMember(filedSplits[0], filedSplits[1]));
            }
        }

        return new ObjectKey(keySplits[0], keyMemberList);
    }

    /**
     * members访问器
     *
     * @return members访问器1
     */
    private Map<String, ObjectKeyMember> getMembersAcc() {
        if (this.members == null)
            this.members = new HashMap<>();
        return this.members;
    }

    /**
     * 获取对象类型（模型类型）的命名空间
     *
     * @return 获取对象类型（模型类型）的命名空间
     */
    public String getTypeNamespace() {
        return this.typeNamespace;
    }

    /**
     * 获取对象类型（模型类型）的名称
     *
     * @return 获取对象类型（模型类型）的名称
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * 获取对象的模型类型
     *
     * @return 获取对象的模型类型
     */
    @Deprecated
    public StructuralType getObjectType() {
        return this.objectType;
    }

    /**
     * 获取对象标识的成员
     *
     * @return 获取对象标识的成员
     */
    public List<ObjectKeyMember> getMembers() {
        return new ArrayList<>(this.members.values());
    }

    /**
     * 根据属性名获取属性值
     *
     * @param attrName 属性名
     * @return 属性值
     */
    public Object get(String attrName) {
        return this.getMembersAcc().containsKey(attrName) ? this.getMembersAcc().get(attrName).getValue() : null;
    }

    /**
     * 根据属性名设置属性值
     *
     * @param attrName 属性名
     * @param value    属性值
     */
    public void set(String attrName, Object value) {
        if (!this.getMembersAcc().containsKey(attrName))
            return;
        this.getMembersAcc().remove(attrName);
        this.getMembersAcc().put(attrName, new ObjectKeyMember(attrName, value));
    }

    /**
     * 重写相等方法
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {

        //（1）如果成员数不相等，判定为不相等；
        //（2）如果成员数相等，将两者的成员根据Attribute排序，顺次调用各成员的Equals方法，当且仅当全部成员的Equals方法返回true时判为相等。

        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        ObjectKey objectKey = (ObjectKey) o;
        boolean isEquals = true;
        if (objectKey.members.size() != this.members.size()) return false;
        if (!Objects.equals(this.typeNamespace, objectKey.typeNamespace) || !Objects.equals(this.typeName, objectKey.typeName))
            return false;

        List<ObjectKeyMember> otherMembers = objectKey.members.values().stream().sorted(Comparator.comparing(ObjectKeyMember::getAttribute)).collect(Collectors.toList());
        List<ObjectKeyMember> members = this.members.values().stream().sorted(Comparator.comparing(ObjectKeyMember::getAttribute)).collect(Collectors.toList());

        for (int i = 0; i < otherMembers.size(); i++)
            if (!otherMembers.get(i).getAttribute().equals(members.get(i).getAttribute()) ||
                    !otherMembers.get(i).getValue().equals(members.get(i).getValue())) {
                isEquals = false;
                break;
            }
        return isEquals;
    }

    /**
     * 重写返回哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        if (this.members == null) throw new NullPointerException("Key属性不可为空.");
        List<ObjectKeyMember> objectKeyMember = new ArrayList<>(this.members.values());
        List<ObjectKeyMember> temp = objectKeyMember.stream().sorted(Comparator.comparing(ObjectKeyMember::getAttribute)).collect(Collectors.toList());
        StringBuilder contentBuilder = new StringBuilder();
        for (ObjectKeyMember tempKeyMember : temp) {
            contentBuilder.append(tempKeyMember.getAttribute()).append(tempKeyMember.getValue());
        }
        StringBuilder resultBuilder = new StringBuilder();
        if (!Utils.getStringIsEmpty(this.typeNamespace)) resultBuilder.append(this.typeNamespace);
        if (!Utils.getStringIsEmpty(this.typeName)) resultBuilder.append(this.typeName);
        resultBuilder.append(contentBuilder);
        return resultBuilder.toString().hashCode();
    }

    /**
     * 重写转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        if (null == this.members) return "";
        List<ObjectKeyMember> temp = this.getMembers().stream().sorted(Comparator.comparing(ObjectKeyMember::getAttribute)).collect(Collectors.toList());
        StringBuilder contentBuilder = new StringBuilder();
        for (ObjectKeyMember tempKeyMember : temp) {
            contentBuilder.append(tempKeyMember).append("-");
        }
        String content = contentBuilder.toString();
        content = content.substring(0, content.lastIndexOf("-"));

        StringBuilder resultBuildr = new StringBuilder();
        if (!Utils.getStringIsEmpty(this.typeNamespace)) {
            resultBuildr.append(this.typeNamespace).append(".");
        }
        if (!Utils.getStringIsEmpty(this.typeName)) {
            resultBuildr.append(this.typeName).append("[");
        }
        resultBuildr.append(content);
        resultBuildr.append("]");
        return resultBuildr.toString();
    }
}
