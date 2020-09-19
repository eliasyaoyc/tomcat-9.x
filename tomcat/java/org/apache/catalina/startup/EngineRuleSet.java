package org.apache.catalina.startup;

import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;

/**
 * <p><strong>RuleSet</strong> for processing the contents of a
 * Engine definition element.  This <code>RuleSet</code> does NOT include
 * any rules for nested Host elements, which should be added via instances of
 * <code>HostRuleSet</code>.</p>
 *
 * @author Craig R. McClanahan
 */
public class EngineRuleSet implements RuleSet {

    // ----------------------------------------------------- Instance Variables

    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected final String prefix;

    // ------------------------------------------------------------ Constructor

    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public EngineRuleSet() {
        this("");
    }

    /**
     * Construct an instance of this <code>RuleSet</code> with the specified matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the trailing slash character)
     */
    public EngineRuleSet(String prefix) {
        this.prefix = prefix;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called by a Digester instance.</p>
     *
     * @param digester Digester instance to which the new Rule instances should be added.
     */
    @Override
    public void addRuleInstances(Digester digester) {
        // 创建 Engine 实例，并通过setContainer添加Service中。还为Engine添加了一个生命周期的监听器，代码里写死，不是通过server.xml配置的。用于打印Engine启动和停止日志。
        digester.addObjectCreate(prefix + "Engine", "org.apache.catalina.core.StandardEngine", "className");
        digester.addSetProperties(prefix + "Engine");
        digester.addRule(prefix + "Engine",
                         new LifecycleListenerRule("org.apache.catalina.startup.EngineConfig", "engineConfigClass"));
        digester.addSetNext(prefix + "Engine", "setContainer", "org.apache.catalina.Engine");

        //Cluster configuration start
        digester.addObjectCreate(prefix + "Engine/Cluster", null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Engine/Cluster");
        digester.addSetNext(prefix + "Engine/Cluster", "setCluster", "org.apache.catalina.Cluster");
        //Cluster configuration end

        digester.addObjectCreate(prefix + "Engine/Listener", null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Engine/Listener");
        digester.addSetNext(prefix + "Engine/Listener", "addLifecycleListener", "org.apache.catalina.LifecycleListener");


        digester.addRuleSet(new RealmRuleSet(prefix + "Engine/"));

        digester.addObjectCreate(prefix + "Engine/Valve", null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Engine/Valve");
        digester.addSetNext(prefix + "Engine/Valve", "addValve", "org.apache.catalina.Valve");
    }
}
