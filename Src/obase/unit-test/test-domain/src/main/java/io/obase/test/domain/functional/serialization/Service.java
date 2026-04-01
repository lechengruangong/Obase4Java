package io.obase.test.domain.functional.serialization;

import java.util.List;

/**
 * 模拟某种服务
 */
public class Service {

    /**
     * 分析器
     */
    private Analyser analyser;

    /**
     * 代码
     */
    private String code;

    /**
     * 身份
     */
    private Identity identity;

    /**
     * 路由
     */
    private Route route;

    /**
     * 子路由
     */
    private List<Route> subRoute;

    /**
     * 组件
     */
    private List<Component> components;

    /**
     * 分析器
     *
     * @return 分析器
     */
    public Analyser getAnalyser() {
        return this.analyser;
    }

    /**
     * 分析器
     *
     * @param analyser 分析器
     */
    public void setAnalyser(Analyser analyser) {
        this.analyser = analyser;
    }

    /**
     * 代码
     *
     * @return 代码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 代码
     *
     * @param code 代码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 身份
     *
     * @return 身份
     */
    public Identity getIdentity() {
        return this.identity;
    }

    /**
     * 身份
     *
     * @param identity 身份
     */
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    /**
     * 路由
     *
     * @return 路由
     */
    public Route getRoute() {
        return this.route;
    }

    /**
     * 路由
     *
     * @param route 路由
     */
    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * 子路由
     *
     * @return 子路由
     */
    public List<Route> getSubRoute() {
        return this.subRoute;
    }

    /**
     * 子路由
     *
     * @param subRoute 子路由
     */
    public void setSubRoute(List<Route> subRoute) {
        this.subRoute = subRoute;
    }

    /**
     * 组件
     *
     * @return 组件
     */
    public List<Component> getComponents() {
        return this.components;
    }

    /**
     * 组件
     *
     * @param components 组件
     */
    public void setComponents(List<Component> components) {
        this.components = components;
    }
}
