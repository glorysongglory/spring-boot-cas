package com.yellowcong.cas.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yellowcong.cas.CasServerUtil;

@Controller
@RequestMapping("/user")
public class LoginController {
	/**
	 * 创建日期:2018年2月3日<br/>
	 * 创建时间:下午5:32:41<br/>
	 * 创建用户:yellowcong<br/>
	 * 机能概要:单点登出
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping("/loginOut1")
	public String loginOut(HttpSession session) {
		session.invalidate();
		// http://yellowcong.com:8080/cas-client-maven/user/loginOut/success

		// 这个是直接退出，走的是默认退出方式
		return "redirect:https://case.example.org:9000/logout";
	}

	@RequestMapping("/loginOut2")
	public String loginOut2(HttpSession session) {
		session.invalidate();
		// 退出登录后，跳转到退成成功的页面，不走默认页面
		return "redirect:https://case.example.org:9000/logout?service=http://case.example.org:9001/user/loginOut/success";
	}
	
	@RequestMapping("/loginOut/success")
	@ResponseBody
	public String loginOut2(HttpServletRequest req,HttpServletResponse resp) throws Exception {
		return "退出成功";
	}
	
	@RequestMapping("/login/input")
	public String login(HttpServletRequest req,HttpServletResponse resp) throws Exception {
		
		//获取票据信息
		String ticket = CasServerUtil.getInstance().getSt("yellowcong", "yellowcong");
		return "redirect:http://case.example.org:9001?ticket="+ticket;
	}

}
