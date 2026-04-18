package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.constants.Status;
import com.jins.aimentor.domain.entity.Resource;
import com.jins.aimentor.domain.vo.ResourceVO;
import com.jins.aimentor.service.ResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Api(tags = "资源推荐", description = "学习资源推荐管理接口")
public class ResourceController {

    private final ResourceService resourceService;

    @ApiOperation("获取推荐资源列表")
    @GetMapping("/recommend")
    public Result<List<ResourceVO>> getRecommendResources() {
        log.info("[ResourceController] 获取推荐资源列表");
        List<ResourceVO> list = resourceService.getRecommendResources();
        return Result.success(list);
    }

    @ApiOperation("按科目筛选资源")
    @GetMapping("/subject/{subject}")
    public Result<List<ResourceVO>> getBySubject(@PathVariable String subject) {
        log.info("[ResourceController] 按科目筛选资源，subject={}", subject);
        List<ResourceVO> list = resourceService.getResourcesBySubject(subject);
        return Result.success(list);
    }

    @ApiOperation("按类型筛选资源")
    @GetMapping("/type/{type}")
    public Result<List<ResourceVO>> getByType(@PathVariable String type) {
        log.info("[ResourceController] 按类型筛选资源，type={}", type);
        List<ResourceVO> list = resourceService.getResourcesByType(type);
        return Result.success(list);
    }

    /**
     * 资源详情（使用 /detail/{id}，避免与静态资源解析或路径模式在部分环境下的匹配问题）
     */
    @ApiOperation("按 ID 获取资源详情")
    @GetMapping("/detail/{id}")
    public Result<ResourceVO> getById(@PathVariable Long id) {
        log.info("[ResourceController] 获取资源详情，id={}", id);
        ResourceVO vo = resourceService.getResourceById(id);
        if (vo == null) {
            return Result.error(Status.CODE_404, "资源不存在");
        }
        return Result.success(vo);
    }

    private final RestTemplate restTemplate = new RestTemplate();

    @ApiOperation("修复失效的B站视频封面")
    @PostMapping("/fix-covers")
    public Result<Map<String, Object>> fixBilibiliCovers() {
        log.info("[ResourceController] 开始修复B站视频封面");
        List<Resource> resources = resourceService.listAll();

        int total = 0;
        int success = 0;
        int failed = 0;

        for (Resource resource : resources) {
            String url = resource.getUrl();
            String coverImage = resource.getCoverImage();

            // 只处理B站视频链接，且封面为空或已失效
            if (url != null && url.contains("bilibili.com/video/")) {
                total++;
                String bvid = extractBvid(url);

                if (bvid != null) {
                    String newCover = fetchBilibiliCover(bvid);
                    if (newCover != null) {
                        resource.setCoverImage(newCover);
                        resourceService.updateById(resource);
                        success++;
                        log.info("[ResourceController] 修复成功，id={}, bvid={}, 新封面={}",
                                resource.getId(), bvid, newCover);
                    } else {
                        failed++;
                        log.warn("[ResourceController] 获取封面失败，id={}, bvid={}", resource.getId(), bvid);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("failed", failed);
        log.info("[ResourceController] 封面修复完成，总计={}, 成功={}, 失败={}", total, success, failed);

        return Result.success(result);
    }

    @ApiOperation("根据B站视频URL获取封面")
    @GetMapping("/fetch-cover")
    public Result<String> fetchCover(@RequestParam String url) {
        String bvid = extractBvid(url);
        if (bvid == null) {
            return Result.error(Status.CODE_400, "无效的B站视频链接");
        }

        String cover = fetchBilibiliCover(bvid);
        if (cover == null) {
            return Result.error(Status.CODE_500, "获取封面失败");
        }

        return Result.success(cover);
    }

    @ApiOperation("新增资源（教师端）")
    @PostMapping("/create")
    public Result<ResourceVO> createResource(@RequestBody Resource resource) {
        log.info("[ResourceController] 新增资源，title={}", resource.getTitle());
        // 设置初始推荐分数
        if (resource.getRecommendScore() == null) {
            resource.setRecommendScore(50);
        }
        boolean success = resourceService.saveResource(resource);
        if (success) {
            ResourceVO vo = toVO(resource);
            return Result.success(vo);
        }
        return Result.error(Status.CODE_500, "添加失败");
    }

    @ApiOperation("更新资源（教师端）")
    @PutMapping("/update/{id}")
    public Result<ResourceVO> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        log.info("[ResourceController] 更新资源，id={}", id);
        ResourceVO existing = resourceService.getResourceById(id);
        if (existing == null) {
            return Result.error(Status.CODE_404, "资源不存在");
        }

        resource.setId(id);
        boolean success = resourceService.updateById(resource);
        if (success) {
            ResourceVO vo = resourceService.getResourceById(id);
            return Result.success(vo);
        }
        return Result.error(Status.CODE_500, "更新失败");
    }

    @ApiOperation("删除资源（教师端）")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        log.info("[ResourceController] 删除资源，id={}", id);
        boolean success = resourceService.deleteResource(id);
        if (success) {
            return Result.success();
        }
        return Result.error(Status.CODE_500, "删除失败");
    }

    private String extractBvid(String url) {
        Pattern pattern = Pattern.compile("[Bb][Vv]1[A-Za-z0-9]{10}");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }
        return null;
    }

    private String fetchBilibiliCover(String bvid) {
        try {
            String apiUrl = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
            String response = restTemplate.getForObject(apiUrl, String.class);

            // 简单解析JSON获取封面URL
            Pattern pattern = Pattern.compile("\"pic\":\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.error("[ResourceController] 调用B站API失败，bvid={}", bvid, e);
        }
        return null;
    }

    private ResourceVO toVO(Resource entity) {
        ResourceVO vo = new ResourceVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setType(entity.getType());
        vo.setUrl(entity.getUrl());
        vo.setCoverImage(entity.getCoverImage());
        vo.setSubject(entity.getSubject());
        vo.setRecommendScore(entity.getRecommendScore());
        return vo;
    }
}
