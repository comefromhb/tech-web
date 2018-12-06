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
import springboot.modal.cust.AdvContent;
import springboot.modal.cust.Project;
import springboot.modal.vo.AttachVo;
import springboot.modal.vo.ContentVo;
import springboot.modal.vo.ContentVoExample;
import springboot.modal.vo.UserVo;
import springboot.service.*;
import springboot.util.Commons;
import springboot.util.MyUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 页面管理
 *
 * @author lincl
 * @date 2018/12/06 12:43
 */
@Controller
@RequestMapping("admin/page")
public class PageController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    @Resource
    private IContentService contentService;

    @Resource
    private ILogService logService;

    @Resource
    private IAttachService attachService;
    @Resource
    private IProjectService projectService;
    @Resource
    private IAdvPageService advPageService;

    @GetMapping(value = "")
    public String index(HttpServletRequest request) {
        ContentVoExample contentVoExample = new ContentVoExample();
        contentVoExample.setOrderByClause("created desc");
        contentVoExample.createCriteria().andTypeEqualTo(Types.PAGE.getType());
        PageInfo<ContentVo> contentVoPageInfo = contentService.getArticlesWithpage(contentVoExample, 1, WebConst.MAX_POSTS);
        request.setAttribute("articles", contentVoPageInfo);
        return "admin/page_list";
    }

    @GetMapping(value = "new")
    public String newPage(HttpServletRequest request) {
        return "admin/page_edit";
    }

    @GetMapping(value = "/{cid}")
    public String editPage(@PathVariable String cid, HttpServletRequest request) {
        ContentVo contents = contentService.getContents(cid);
        request.setAttribute("contents", contents);
        return "admin/page_edit";
    }

    @PostMapping(value = "publish")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo publishPage(@RequestParam String title, @RequestParam String content,
                                      @RequestParam String status, @RequestParam String slug,
                                      @RequestParam(required = false) Integer allowComment, @RequestParam(required = false) Integer allowPing, HttpServletRequest request) {

        UserVo users = this.user(request);
        ContentVo contents = new ContentVo();
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment) {
            contents.setAllowComment(allowComment == 1);
        }
        if (null != allowPing) {
            contents.setAllowPing(allowPing == 1);
        }
        contents.setAuthorId(users.getUid());

        try {
            contentService.publish(contents);
        } catch (Exception e) {
            String msg = "页面发布失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok();
    }

    @PostMapping(value = "modify")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo modifyArticle(@RequestParam Integer cid, @RequestParam String title,
                                        @RequestParam String content,
                                        @RequestParam String status, @RequestParam String slug,
                                        @RequestParam(required = false) Integer allowComment, @RequestParam(required = false) Integer allowPing, HttpServletRequest request) {

        UserVo users = this.user(request);
        ContentVo contents = new ContentVo();
        contents.setCid(cid);
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment) {
            contents.setAllowComment(allowComment == 1);
        }
        if (null != allowPing) {
            contents.setAllowPing(allowPing == 1);
        }
        contents.setAuthorId(users.getUid());
        try {
            contentService.updateArticle(contents);
        } catch (Exception e) {
            String msg = "页面编辑失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok();
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam int cid, HttpServletRequest request) {
        try {
            contentService.deleteByCid(cid);
            logService.insertLog(LogActions.DEL_PAGE.getAction(), cid + "", request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e) {
            String msg = "页面删除失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok();
    }

    @GetMapping(value = {"", "/toEditAdv/{projId}"})
    public String editAdvPage(@PathVariable String projId, HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "12") int limit) {
        //图片附件展示
        AdvContent advContent = advPageService.getAdvPageContentByProjId(Integer.valueOf(projId));
        String img1 = advContent.getImg1();
        String img2 = advContent.getImg2();
        String img3 = advContent.getImg3();

        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        List<Integer> list3 = new ArrayList<>();

        //  PageInfo<AttachVo> attachPagination = attachService.getAttachs(page, limit);
        if (list1!=null&&list1.size()==0){
            list1.add(1);//默认一个
        }
        if (list2!=null&&list2.size()==0){
            list2.add(1);//默认一个
        }
        if (list3!=null&&list3.size()==0){
            list3.add(1);//默认一个
        }
        if (img1!=null&&!img1.equals("")) MyUtils.parseStr2List(list1,img1);
        if (img2!=null&&!img2.equals(""))MyUtils.parseStr2List(list2,img2);
        if (img3!=null&&!img3.equals(""))MyUtils.parseStr2List(list3,img3);
        PageInfo<AttachVo> attachPagination1 = attachService.getAttachsByArr(page, limit,list1);
        request.setAttribute("attachs1", attachPagination1);
        PageInfo<AttachVo> attachPagination2 = attachService.getAttachsByArr(page, limit,list2);
        PageInfo<AttachVo> attachPagination3 = attachService.getAttachsByArr(page, limit,list3);
        request.setAttribute("attachs2", attachPagination2);
        request.setAttribute("attachs3", attachPagination3);
        request.setAttribute("advContent", advContent);
        request.setAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType()));
        request.setAttribute("max_file_size", WebConst.MAX_TEXT_COUNT / 1024);
        //选择关联的项目
        Project proj = projectService.getProjectObj(projId);
        request.setAttribute("proj",proj);
        return "admin/advs_edit";
    }
    /**
     * 保存数据post
     *
     * @param request
     * @return
     * @throws
     */
    @PostMapping(value = "saveAdvPage")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo save(HttpServletRequest request,@RequestParam String summary,
                               @RequestParam String activitySchedule,@RequestParam String stuComments,
                               @RequestParam String teacherInt,@RequestParam Integer projId,@RequestParam Integer id,
                               @RequestParam String img1,@RequestParam String img2,@RequestParam String img3)  {
        UserVo users = this.user(request);
        Integer uid = users.getUid();
        AdvContent advContent = new AdvContent();
        try {
            advContent.setId(id);
            advContent.setCreator(uid.toString());
            advContent.setProjId(projId);
            advContent.setActivitySchedule(activitySchedule);
            advContent.setTeacherInt(teacherInt);
            advContent.setImg1(img1);
            advContent.setStuComments(stuComments);
            advContent.setImg2(img2);
            advContent.setImg3(img3);
            advContent.setSummary(summary);
            if(id!=null && id.toString().length()>0){
                advContent.setUpdateDate(new Date());
                advContent.setUpdatePerson(uid.toString());
                advPageService.updateAdvContent(advContent);
            }else {
                advPageService.saveAdvContent(advContent);
            }
        } catch (Exception e) {
            return RestResponseBo.fail("保存失败");
        }
        return RestResponseBo.ok(advContent);
    }
}
