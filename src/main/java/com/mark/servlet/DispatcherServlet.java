package com.mark.servlet;

import com.mark.annotation.*;
import com.mark.controller.MarkController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 名称:
 * Created with IntelliJ IDEA.
 * User: IT666_Gj
 */
public class DispatcherServlet extends HttpServlet {

    List<String> className = new ArrayList<String>();

    Map<String ,Object> beans = new HashMap<String, Object>();

    Map<String ,Object> handerMap = new HashMap<String, Object>();

    // tomcat启动时需要初始化.创建java/map...
    @Override
    public void init(ServletConfig config){
        // 扫描 com.mark.controller.markController
        doScan("com.mark");

        // 已经得到.class文件路径 进行实例化
        doInstance();
        
        doAutowired();

        urlMapping();
    }

    private void urlMapping() {
        for (Map.Entry<String,Object> entry:beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(AnnMarkController.class)){
                AnnMarkRequestMapping requestMapping = clazz.getAnnotation(AnnMarkRequestMapping.class);
                String classPath = requestMapping.value();

                Method[] methods = clazz.getMethods();
                for (Method method:methods){
                    if (method.isAnnotationPresent(AnnMarkRequestMapping.class)){
                        AnnMarkRequestMapping requestMapping1 = method.getAnnotation(AnnMarkRequestMapping.class);
                        String methonPath = requestMapping1.value();

                        //mark/query ----> method
                        handerMap.put(classPath+methonPath,method);
                    }else {
                        continue;
                    }
                }
            }
        }
    }

    private void doAutowired() {
        for (Map.Entry<String,Object> entry:beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(AnnMarkController.class)){
                Field[] fields = clazz.getDeclaredFields();
                for (Field field:fields){
                    if (field.isAnnotationPresent(AnnMarkAutowired.class)){
                        AnnMarkAutowired auto = field.getAnnotation(AnnMarkAutowired.class);
                        String key = auto.value();

                        Object value = beans.get(key);
                        field.setAccessible(true);
                        try {
                            field.set(instance,value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }


                    }else {
                        continue;
                    }
                }
            }else {
                continue;
            }
        }
    }

    // 扫描方法 mvc.xml ---> basePackage = ""
    public void doScan(String basePackage){

        // 扫描编译好的所有类的路径 com/mark
        URL url = this.getClass().getClassLoader().getResource("/" +basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        String[] filesStr = file.list();// 得到com/mark下的所有.class文件&文件夹
        for (String path:filesStr){
            File filePath = new File(fileStr+path);
            if (filePath.isDirectory()){
                // 如果寻找的依旧是文件夹.递归继续查找
                doScan(basePackage+"."+path);
            }else {
                // 如果是.class文件,则获取路径
                className.add(basePackage+"."+filePath.getName());
            }
        }
    }

    public void doInstance(){
        for (String className:className){
            String cn = className.replace(".class","");
            try {
                Class<?> clazz = Class.forName(cn);

                if (clazz.isAnnotationPresent(AnnMarkController.class)){
                    try {
                        Object value = clazz.newInstance();// 实例化对象
                        // map.put(key,instance);
                        AnnMarkRequestMapping reqMap = clazz.getAnnotations(AnnMarkRequestMapping.class);
                        String key = reqMap.value();
                        beans.put(key,value);

                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }else if (clazz.isAnnotationPresent(AnnMarkService.class)){
                    Object value = null;// 实例化对象
                    try {
                        value = clazz.newInstance();

                        // map.put(key,instance);
                        AnnMarkService reqMap = clazz.getAnnotations(AnnMarkService.class);
                        String key = reqMap.value();
                        beans.put(key,value); // 放到IOC容器中
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 获取请求  /mark/query ---> method
        String uri = req.getRequestURI(); // /mark-mvc/mark/query
        String context = req.getContextPath(); // /mark-mvc

        String path = uri.replace(context,""); // /mark/query --->key

        Method method = (Method) handerMap.get(path);

        MarkController instance = (MarkController) beans.get("/"+path.split("/")[1]); // mark--->value

        Object args[] = hand(req,resp,method);
        try {
            method.invoke(instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static Object[] hand(HttpServletRequest request,HttpServletResponse response,Method method){
        Class<?>[] paramClazzs = method.getParameterTypes();
        Object[] args = new Object[paramClazzs.length];
        int args_i = 0;
        int index = 0;
        for(Class<?> paramClazz:paramClazzs){
            if (ServletRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = response;
            }
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0){
                for (Annotation paramAn:paramAns){
                    if (AnnMarkRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                        AnnMarkRequestParam rp = (AnnMarkRequestParam) paramAn;

                        args[args_i++] = request.getParameter(rp.value());
                    }
                }
            }
            index++;
        }
        return args;

    }




















}
