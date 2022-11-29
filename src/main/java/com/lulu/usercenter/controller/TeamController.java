package com.lulu.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lulu.usercenter.Model.domain.Team;
import com.lulu.usercenter.Model.domain.User;
import com.lulu.usercenter.Model.domain.UserTeam;
import com.lulu.usercenter.Model.dto.TeamQuery;
import com.lulu.usercenter.Model.request.TeamAddRequest;
import com.lulu.usercenter.Model.request.TeamJoinRequest;
import com.lulu.usercenter.Model.request.TeamQuitRequest;
import com.lulu.usercenter.Model.request.TeamUpdateRequest;
import com.lulu.usercenter.Model.vo.TeamUserVO;
import com.lulu.usercenter.common.BaseResponse;
import com.lulu.usercenter.common.DeleteRequest;
import com.lulu.usercenter.common.ErrorCode;
import com.lulu.usercenter.common.ResultUtils;
import com.lulu.usercenter.exception.BusinessException;
import com.lulu.usercenter.service.TeamService;
import com.lulu.usercenter.service.UserService;
import com.lulu.usercenter.service.UserTeamService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 用户接口
 * @author lulu
 */
@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000",allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

   @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
       if(teamAddRequest == null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       User loginUser = userService.getLoginUser(request);
       Team team = new Team();
       BeanUtils.copyProperties(teamAddRequest,team);
       long teamId = teamService.addTeam(team,loginUser);
       return ResultUtils.success(teamId);
   }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean update = teamService.updateTeam(teamUpdateRequest,loginUser) ;
        if(!update){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"查询失败");
        }
        return ResultUtils.success(team);
    }


    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean admin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, admin);
        // 判断当前用户是否已加入队伍
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入的队伍 的 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam :: getTeamId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}
        // 查询加入队伍的用户信息（人数）
//        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
//        userTeamJoinQueryWrapper.in("teamId",teamIdList);
//        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
//        // 队伍 id => 加入这个队伍的用户列表
//        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
//        teamList.forEach(team ->{
//            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
//        });
        return ResultUtils.success(teamList);
    }


    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeam(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     *  获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeam(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long,List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(team,teamQuery);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page,queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
            if(teamJoinRequest == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User loginUser = userService.getLoginUser(request);
            boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
            return ResultUtils.success(result);
    }


    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

}
