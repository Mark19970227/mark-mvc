package com.mark.controller;


import com.mark.annotation.AnnMarkAutowired;
import com.mark.annotation.AnnMarkController;
import com.mark.annotation.AnnMarkRequestMapping;
import com.mark.annotation.AnnMarkRequestParam;
import com.mark.service.MarkService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 名称:
 * Created with IntelliJ IDEA.
 * User: IT666_Gj
 */
@AnnMarkController
@AnnMarkRequestMapping("/mark")
public class MarkController {

    @AnnMarkAutowired("markServiceImpl")
    private MarkService markService;

    @AnnMarkRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @AnnMarkRequestParam("name") String name,
                      @AnnMarkRequestParam("age") String age) throws IOException {
        PrintWriter ps = response.getWriter();
        String result = markService.query(name,age);
        ps.write(result);

    }
}
