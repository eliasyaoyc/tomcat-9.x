package org.apache.catalina.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Executor;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.mapper.Mapper;
import org.apache.catalina.mapper.MapperListener;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

/**
 * Standard implementation of the <code>Service</code> interface.  The
 * associated Container is generally an instance of Engine, but this is
 * not required.
 *
 * @author Craig R. McClanahan
 */
public class StandardService extends LifecycleMBeanBase implements Service {

    private static final Log log = LogFactory.getLog(StandardService.class);


    // ----------------------------------------------------- Instance Variables

    /**
     * 该 Service 的名称
     */
    private String name = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * Service 所属的 Server（如果有）。
     */
    private Server server = null;

    /**
     * The property change support for this component.
     * 组件对属性改变的支持，JDK自带的观察者模式，主要是观察Java Bean对象的属性更改的
     */
    protected final PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * 与 Service 关联的 Connector 集合。
     * 一个Servlet服务接受两个不同的协议连接，只不过不同的协议通过对应的Connector都被处理成了一个Request对象，这样对于Engine来说，都是一样的请求。
     */
    protected Connector connectors[] = new Connector[0];
    private final Object connectorsLock = new Object();

    /**
     * The list of executors held by the service.
     */
    protected final ArrayList<Executor> executors = new ArrayList<>();

    /**
     * Servlet引擎，就是专门用来处理请求的，其他的都不管。
     */
    private Engine engine = null;

    private ClassLoader parentClassLoader = null;

    /**
     * Mapper. 保存了一个映射关系，不同请求路径对应哪一个Servlet的API。
     */
    protected final Mapper mapper = new Mapper();


    /**
     * Mapper listener.
     */
    protected final MapperListener mapperListener = new MapperListener(this);


    // ------------------------------------------------------------- Properties

    @Override
    public Mapper getMapper() {
        return mapper;
    }


    @Override
    public Engine getContainer() {
        return engine;
    }


    @Override
    public void setContainer(Engine engine) {
        Engine oldEngine = this.engine;
        if (oldEngine != null) {
            oldEngine.setService(null);
        }
        this.engine = engine;
        if (this.engine != null) {
            this.engine.setService(this);
        }
        if (getState().isAvailable()) {
            if (this.engine != null) {
                try {
                    this.engine.start();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardService.engine.startFailed"), e);
                }
            }
            // Restart MapperListener to pick up new engine.
            try {
                mapperListener.stop();
            } catch (LifecycleException e) {
                log.error(sm.getString("standardService.mapperListener.stopFailed"), e);
            }
            try {
                mapperListener.start();
            } catch (LifecycleException e) {
                log.error(sm.getString("standardService.mapperListener.startFailed"), e);
            }
            if (oldEngine != null) {
                try {
                    oldEngine.stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardService.engine.stopFailed"), e);
                }
            }
        }

        // Report this property change to interested listeners
        support.firePropertyChange("container", oldEngine, this.engine);
    }


    /**
     * Return the name of this Service.
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Set the name of this Service.
     *
     * @param name The new service name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Return the <code>Server</code> with which we are associated (if any).
     */
    @Override
    public Server getServer() {
        return this.server;
    }


    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     *
     * @param connector The Connector to be added
     */
    @Override
    public void addConnector(Connector connector) {

        synchronized (connectorsLock) {
            connector.setService(this);
            Connector results[] = new Connector[connectors.length + 1];
            System.arraycopy(connectors, 0, results, 0, connectors.length);
            results[connectors.length] = connector;
            connectors = results;
        }

        try {
            if (getState().isAvailable()) {
                connector.start();
            }
        } catch (LifecycleException e) {
            throw new IllegalArgumentException(
                    sm.getString("standardService.connector.startFailed", connector), e);
        }

        // Report this property change to interested listeners
        support.firePropertyChange("connector", null, connector);
    }


    public ObjectName[] getConnectorNames() {
        ObjectName results[] = new ObjectName[connectors.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = connectors[i].getObjectName();
        }
        return results;
    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }


    /**
     * Find and return the set of Connectors associated with this Service.
     */
    @Override
    public Connector[] findConnectors() {
        return connectors;
    }


    /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     *
     * @param connector The Connector to be removed
     */
    @Override
    public void removeConnector(Connector connector) {

        synchronized (connectorsLock) {
            int j = -1;
            for (int i = 0; i < connectors.length; i++) {
                if (connector == connectors[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;
            if (connectors[j].getState().isAvailable()) {
                try {
                    connectors[j].stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString(
                            "standardService.connector.stopFailed",
                            connectors[j]), e);
                }
            }
            connector.setService(null);
            int k = 0;
            Connector results[] = new Connector[connectors.length - 1];
            for (int i = 0; i < connectors.length; i++) {
                if (i != j)
                    results[k++] = connectors[i];
            }
            connectors = results;

            // Report this property change to interested listeners
            support.firePropertyChange("connector", connector, null);
        }
    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }


    /**
     * Return a String representation of this component.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StandardService[");
        sb.append(getName());
        sb.append("]");
        return sb.toString();
    }


    /**
     * Adds a named executor to the service
     *
     * @param ex Executor
     */
    @Override
    public void addExecutor(Executor ex) {
        synchronized (executors) {
            if (!executors.contains(ex)) {
                executors.add(ex);
                if (getState().isAvailable()) {
                    try {
                        ex.start();
                    } catch (LifecycleException x) {
                        log.error(sm.getString("standardService.executor.start"), x);
                    }
                }
            }
        }
    }


    /**
     * Retrieves all executors
     *
     * @return Executor[]
     */
    @Override
    public Executor[] findExecutors() {
        synchronized (executors) {
            Executor[] arr = new Executor[executors.size()];
            executors.toArray(arr);
            return arr;
        }
    }


    /**
     * Retrieves executor by name, null if not found
     *
     * @param executorName String
     * @return Executor
     */
    @Override
    public Executor getExecutor(String executorName) {
        synchronized (executors) {
            for (Executor executor : executors) {
                if (executorName.equals(executor.getName()))
                    return executor;
            }
        }
        return null;
    }


    /**
     * Removes an executor from the service
     *
     * @param ex Executor
     */
    @Override
    public void removeExecutor(Executor ex) {
        synchronized (executors) {
            if (executors.remove(ex) && getState().isAvailable()) {
                try {
                    ex.stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardService.executor.stop"), e);
                }
            }
        }
    }


    /**
     * Start nested components ({@link Executor}s, {@link Connector}s and
     * {@link Container}s) and implement the requirements of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @throws LifecycleException if this component detects a fatal error that prevents this component from being used
     *                            <p>
     *                            调用自己的子组件的 start() 方法。主要包括 Engine，Executor，Mapper，Connector 这四个。
     */
    @Override
    protected void startInternal() throws LifecycleException {
        if (log.isInfoEnabled())
            log.info(sm.getString("standardService.start.name", this.name));
        // 设置生命周期的状态
        setState(LifecycleState.STARTING);

        // Start our defined Container first
        if (engine != null) {
            synchronized (engine) {
                engine.start();
            }
        }

        synchronized (executors) {
            for (Executor executor : executors) {
                executor.start();
            }
        }

        mapperListener.start();

        // Start our defined Connectors second
        synchronized (connectorsLock) {
            for (Connector connector : connectors) {
                // If it has already failed, don't try and start it
                if (connector.getState() != LifecycleState.FAILED) {
                    connector.start();
                }
            }
        }
    }


    /**
     * Stop nested components ({@link Executor}s, {@link Connector}s and
     * {@link Container}s) and implement the requirements of
     * {@link org.apache.catalina.util.LifecycleBase#stopInternal()}.
     *
     * @throws LifecycleException if this component detects a fatal error that needs to be reported
     */
    @Override
    protected void stopInternal() throws LifecycleException {

        // Pause connectors first 先暂停或者关闭 Connector
        synchronized (connectorsLock) {
            for (Connector connector : connectors) {
                // 暂停
                connector.pause();
                // Close server socket if bound on start
                // Note: test is in AbstractEndpoint
                // 如果有绑定socket，就关闭掉
                connector.getProtocolHandler().closeServerSocketGraceful();
            }
        }

        if (log.isInfoEnabled())
            log.info(sm.getString("standardService.stop.name", this.name));
        setState(LifecycleState.STOPPING);

        // Stop our defined Container second
        if (engine != null) {
            synchronized (engine) {
                engine.stop();
            }
        }

        // Now stop the connectors
        synchronized (connectorsLock) {
            for (Connector connector : connectors) {
                if (!LifecycleState.STARTED.equals(
                        connector.getState())) {
                    // Connectors only need stopping if they are currently
                    // started. They may have failed to start or may have been
                    // stopped (e.g. via a JMX call)
                    continue;
                }
                connector.stop();
            }
        }

        // If the Server failed to start, the mapperListener won't have been
        // started
        if (mapperListener.getState() != LifecycleState.INITIALIZED) {
            mapperListener.stop();
        }

        synchronized (executors) {
            for (Executor executor : executors) {
                executor.stop();
            }
        }

    }


    /**
     * Invoke a pre-startup initialization. This is used to allow connectors to bind to restricted ports under Unix operating environments.
     * 调用自己的子组件的 init() 方法。主要包括 Engine，Executor，Mapper，Connector 这四个
     */
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();

        if (engine != null) {
            engine.init();
        }

        // Initialize any Executors
        for (Executor executor : findExecutors()) {
            if (executor instanceof JmxEnabled) {
                ((JmxEnabled) executor).setDomain(getDomain());
            }
            executor.init();
        }

        // Initialize mapper listener
        mapperListener.init();

        // Initialize our defined Connectors
        synchronized (connectorsLock) {
            for (Connector connector : connectors) {
                connector.init();
            }
        }
    }


    @Override
    protected void destroyInternal() throws LifecycleException {
        mapperListener.destroy();

        // Destroy our defined Connectors
        synchronized (connectorsLock) {
            for (Connector connector : connectors) {
                connector.destroy();
            }
        }

        // Destroy any Executors
        for (Executor executor : findExecutors()) {
            executor.destroy();
        }

        if (engine != null) {
            engine.destroy();
        }

        super.destroyInternal();
    }


    /**
     * Return the parent class loader for this component.
     */
    @Override
    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null)
            return parentClassLoader;
        if (server != null) {
            return server.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }


    /**
     * Set the parent class loader for this server.
     *
     * @param parent The new parent class loader
     */
    @Override
    public void setParentClassLoader(ClassLoader parent) {
        ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
        support.firePropertyChange("parentClassLoader", oldParentClassLoader, this.parentClassLoader);
    }


    @Override
    protected String getDomainInternal() {
        String domain = null;
        Container engine = getContainer();

        // Use the engine name first
        if (engine != null) {
            domain = engine.getName();
        }

        // No engine or no engine name, use the service name
        if (domain == null) {
            domain = getName();
        }

        // No service name, return null which will trigger the use of the
        // default
        return domain;
    }


    @Override
    public final String getObjectNameKeyProperties() {
        return "type=Service";
    }
}
