package org.feuyeux.dhyana.domain;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
public enum DhyanaState {
    /**
     * 初始化
     */
    INIT,
    /**
     * 选主中，无主
     */
    ELECTING,
    /**
     * 选主完毕
     */
    ELECTED,
    /**
     * 关闭
     */
    CLOSE
}
