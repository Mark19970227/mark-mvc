package com.mark.service.impl;

import com.mark.annotation.AnnMarkService;
import com.mark.service.MarkService;

/**
 * 名称:
 * Created with IntelliJ IDEA.
 * User: IT666_Gj
 */
@AnnMarkService("MarkServiceImpl")
public class MarkServiceImpl implements MarkService {
    public String query(String name, String age) {
        return "name"+name + "age==" +age;
    }
}
