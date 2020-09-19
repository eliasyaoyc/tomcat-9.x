package util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

class EventSource {
    private String name;
    /*
     * 在事件源的地方添加一个PropertyChangeSupport对象，间接地由它负责添加监听、激发事件
     */
    private PropertyChangeSupport listernts = new PropertyChangeSupport(this);

    /**
     * 在事件源上添加监听，实际上是在PropertyChangeSupport对象上添加监听
     */
    public void addListner(PropertyChangeListener listern) {
        listernts.addPropertyChangeListener(listern);
    }

    /**
     * 同上
     */
    public void removeListner(PropertyChangeListener listern) {
        listernts.removePropertyChangeListener(listern);
    }

    public String getName() {
        return name;
    }

    /**
     * 事件源发生变化时，也是通过PropertyChangeSupport对象把事件发送到监听者上的
     */
    public void setName(String name) {
        this.name = name;
        // 触发事件源，监听者得到触发变化
        listernts.firePropertyChange(null, null, getName());
    }
}

class Monitor implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("事件源 发生变化，请做相应处理！");
    }
}

public class TestPropertyChangeSupport {
    public static void main(String[] args) {
        EventSource eventSource = new EventSource();
        Monitor monitor = new Monitor();
        //在事件源上添加监听，发生变化时就会调用propertyChange方法
        eventSource.addListner(monitor);

        eventSource.setName("更改name属性值");
    }
}