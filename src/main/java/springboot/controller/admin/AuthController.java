package springboot.controller.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springboot.constant.WebConst;
import springboot.controller.AbstractController;
import springboot.controller.helper.ExceptionHelper;
import springboot.dto.LogActions;
import springboot.exception.TipException;
import springboot.modal.bo.RestResponseBo;
import springboot.modal.vo.UserVo;
import springboot.service.ILogService;
import springboot.service.IUserService;
import springboot.util.Commons;
import springboot.util.ImageUtil;
import springboot.util.MyUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 登录控制
 *
 * @author tangj
 * @date 2018/1/21 14:07
 */
@Controller
@RequestMapping("/admin")
@Transactional(rollbackFor = TipException.class)
public class AuthController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Resource
    private IUserService userService;

    @Resource
    private ILogService logService;

    @GetMapping(value = "/login")
    public String login() {
        return "admin/login";
    }
    @GetMapping(value = {"", "/register"})
    //@GetMapping(value = "/register")
    public String register() {
        return "admin/register";
    }


    @PostMapping(value = "register")
    @ResponseBody
    public RestResponseBo doRegister(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String code,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        try{
            String sessioncode =  request.getSession().getAttribute("imageCode").toString();
            if (sessioncode!=null && !code.equalsIgnoreCase(sessioncode)){
                return RestResponseBo.fail("验证码错误");
            }
            String recommend = request.getParameter("recommend");
            System.out.println(recommend);
            Integer recommendId = Integer.parseInt(recommend);
            UserVo recUser = userService.queryUserById(recommendId);
            if (recUser==null){
                return RestResponseBo.fail("注册地址不准确，请核查");
            }
            String pwd2 = request.getParameter("password2");
            if (!pwd2.equals(password)){
                return RestResponseBo.fail("2次输入密码不一致，请重试");
            }
            boolean ret = userService.register(username, password,code,recommend);
            if (!ret){
                return RestResponseBo.fail("注册异常");
            }
        }catch (Exception e) {
            String msg = "注册失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok("注册成功。");
    }

    @PostMapping(value = "login")
    @ResponseBody
    public RestResponseBo doLogin(@RequestParam String username,
                                  @RequestParam String password,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        Integer error_count = cache.get("login_error_count");
        try {
            UserVo userVo = userService.login(username, password);
            request.getSession().setAttribute(WebConst.LOGIN_SESSION_KEY, userVo);
            // 设置12小时的cookie
            MyUtils.setCookie(response, userVo.getUid());
            logService.insertLog(LogActions.LOGIN.getAction(), null, request.getRemoteAddr(), userVo.getUid());
        } catch (Exception e) {
            error_count = null == error_count ? 1 : error_count + 1;
            if (error_count > 3) {
                return RestResponseBo.fail("您输入密码已经错误超过3次，请10分钟后尝试");
            }
            cache.set("login_error_count", error_count, 10 * 60);
            String msg = "登录失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok();
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        session.removeAttribute(WebConst.LOGIN_SESSION_KEY);
        Cookie cookie = new Cookie(WebConst.USER_IN_COOKIE, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        try {
            response.sendRedirect(Commons.site_login());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("注销失败", e);
        }
    }
    //生成验证码图片
    @RequestMapping("/register/valicode")
    public void valicode(HttpServletResponse response,HttpSession session) throws Exception{
        //利用图片工具生成图片
        //第一个参数是生成的验证码，第二个参数是生成的图片
        Object[] objs = ImageUtil.createImage();
        //将验证码存入Session
        session.setAttribute("imageCode",objs[0]);
        //将图片输出给浏览器
        BufferedImage image = (BufferedImage) objs[1];
        response.setContentType("image/png");
        OutputStream os = response.getOutputStream();
        ImageIO.write(image, "png", os);


    }
}
