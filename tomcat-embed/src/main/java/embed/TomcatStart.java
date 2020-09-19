package embed;

import embed.servlet.DemoServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class TomcatStart {
    public static int PORT = 80;

    public static String HOSTNAME = "localhost";

    // System.getProperty("user.dir")
    public static String BASE_DIR = "tomcat-embed/target";

    public static String DOC_BASE = "/ROOT";

    public static void main(String[] args) throws LifecycleException {
        TomcatStart.run();
    }

    public static void run() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(BASE_DIR);
        // 主机名, 将生成目录: {BASE_DIR}/work/Tomcat/{HOSTNAME}/{DOC_BASE}
        tomcat.setHostname(HOSTNAME);
        // System.out.println("工作目录: " + tomcat.getServer().getCatalinaBase().getAbsolutePath());

        if (!new File(BASE_DIR + "/webapps" + DOC_BASE).exists()) {
            new File(BASE_DIR + "/webapps" + DOC_BASE).mkdirs();
        }
        // contextPath 要使用的上下文映射，"" 表示根上下文
        // docBase 上下文的基础目录，用于静态文件。相对于服务器主目录必须存在 ({BASE_DIR}/webapps/{DOC_BASE})
        Context ctx = tomcat.addContext("", DOC_BASE);

        // 该文件夹下包含 /WEB-INF/web.xml
        // StandardContext ctx = (StandardContext) tomcat.addWebapp("", DOC_BASE);

        // true 时：相关 classes 修改时，会重新加载资源，不过资源消耗很大
        ctx.setReloadable(false);

        // ctx.addLifecycleListener(event -> {
        //     System.out.println(event.getLifecycle().getState().name());
        //     if (event.getLifecycle().getState() == LifecycleState.STARTING_PREP) {
        //         try {
        //             new SpringServletContainerInitializer().onStartup(new HashSet<Class<?>>() {
        //                 private static final long serialVersionUID = 1L;
        //                 {
        //                     add(WebApplicationInitializer.class);
        //                 }
        //             }, ctx.getServletContext());
        //         } catch (Throwable e) {
        //             e.printStackTrace();
        //         }
        //     }
        // });

        // 注册 servlet
        Tomcat.addServlet(ctx, "globalServlet", new DemoServlet());
        ctx.addServletMappingDecoded("/", "globalServlet");

        // Tomcat 9.0 必须调用 Tomcat#getConnector() 方法之后才会监听端口
        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
    }

    public static void start() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        // tomcat.setBaseDir("/temp");

        Server server = tomcat.getServer();
        server.setPort(8080);

        Connector connector = tomcat.getConnector();
        // connector.setPort(8888);
        connector.setURIEncoding("UTF-8");

        Service service = tomcat.getService();
        service.setName("tomcat-embbeded");

        Context context = tomcat.addContext("", "/");

        tomcat.addServlet("", "test", new DemoServlet());
        context.addServletMappingDecoded("/*", "test");

        server.start();
        server.await();
    }
}