package com.github.plugin.common;


import java.util.ArrayList;
import java.util.List;

public class InjectManager {
    private List<IComponent> components;
    private static volatile InjectManager injectManager;

    private InjectManager() {
        components = new ArrayList<>();
    }

    public static InjectManager getInstance() {
        if (injectManager == null) {
            synchronized (InjectManager.class) {
                if (injectManager == null) {
                    injectManager = new InjectManager();
                }
            }
        }
        return injectManager;
    }


    public synchronized void initComponent() {
    }


    public List<IComponent> getComponents() {
        return components;
    }
}
