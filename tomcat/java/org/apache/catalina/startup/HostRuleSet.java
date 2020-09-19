package org.apache.catalina.startup;

import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;

/**
 * <p><strong>RuleSet</strong> for processing the contents of a
 * Host definition element.  This <code>RuleSet</code> does NOT include
 * any rules for nested Context which should be added via instances of
 * <code>ContextRuleSet</code>.</p>
 *
 * @author Craig R. McClanahan
 */
public class HostRuleSet implements RuleSet {

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
    public HostRuleSet() {
        this("");
    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the trailing slash character)
     */
    public HostRuleSet(String prefix) {
        this.prefix = prefix;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     *
     * @param digester Digester instance to which the new Rule instances should be added.
     */
    @Override
    public void addRuleInstances(Digester digester) {
        // 创建Host实例，通过addChild()方法添加到Engine上。同时还为Host添加了一个生命周期监听器HostConfig，同样，该监听器由Catalina默认添加，而不是server.xml配置。
        digester.addObjectCreate(prefix + "Host", "org.apache.catalina.core.StandardHost", "className");
        digester.addSetProperties(prefix + "Host");
        digester.addRule(prefix + "Host", new CopyParentClassLoaderRule());
        digester.addRule(prefix + "Host",
                         new LifecycleListenerRule("org.apache.catalina.startup.HostConfig", "hostConfigClass"));
        digester.addSetNext(prefix + "Host", "addChild", "org.apache.catalina.Container");

        digester.addCallMethod(prefix + "Host/Alias", "addAlias", 0);

        //Cluster configuration start
        digester.addObjectCreate(prefix + "Host/Cluster",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Cluster");
        digester.addSetNext(prefix + "Host/Cluster",
                            "setCluster",
                            "org.apache.catalina.Cluster");
        //Cluster configuration end

        digester.addObjectCreate(prefix + "Host/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Listener");
        digester.addSetNext(prefix + "Host/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addRuleSet(new RealmRuleSet(prefix + "Host/"));

        digester.addObjectCreate(prefix + "Host/Valve",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Valve");
        digester.addSetNext(prefix + "Host/Valve",
                            "addValve",
                            "org.apache.catalina.Valve");
    }
}
