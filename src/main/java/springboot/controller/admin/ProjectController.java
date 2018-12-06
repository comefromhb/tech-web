package springboot.controller.admin;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springboot.constant.WebConst;
import springboot.controller.AbstractController;
import springboot.controller.helper.ExceptionHelper;
import springboot.dto.LogActions;
import springboot.dto.Types;
import springboot.exception.TipException;
import springboot.modal.bo.RestResponseBo;
import springboot.modal.cust.Project;
import springboot.modal.vo.ContentVo;
import springboot.modal.vo.ContentVoExample;
import springboot.modal.vo.UserVo;
import springboot.service.IContentService;
import springboot.service.ILogService;
import springboot.service.IProjectService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 项目管理
 *
 * @author lincl
 * @date
 */
@Controller
@RequestMapping("admin/project")
public class ProjectController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Resource
    private IProjectService projectService;

    @GetMapping(value = "")
    public String index(HttpServletRequest request) {
        //所有项目
        Project pro = new Project();
        pro.setDeleteFlag("0");
        List<Project> projlist = projectService.getProjectList(pro);
        request.setAttribute("projs",projlist);
        return "admin/proj_list";
    }


}
