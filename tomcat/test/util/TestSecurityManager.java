package util;

public class TestSecurityManager {
    /**
     * 默认的安全管理器配置文件是 $JAVA_HOME/jre/lib/security/java.policy，即当未指定配置文件时，将会使用该配置。
     * -Djava.security.manager -Djava.security.policy="E:/java.policy"
     */
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
    }
}