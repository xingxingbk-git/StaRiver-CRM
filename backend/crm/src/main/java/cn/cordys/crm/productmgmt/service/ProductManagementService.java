package cn.cordys.crm.productmgmt.service;

import cn.cordys.common.constants.FormKey;
import cn.cordys.common.dto.BasePageRequest;
import cn.cordys.common.dto.OptionDTO;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.JSON;
import cn.cordys.common.util.Translator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.approval.constants.ApprovalStatus;
import cn.cordys.crm.approval.domain.ApprovalInstance;
import cn.cordys.crm.approval.domain.ApprovalRecord;
import cn.cordys.crm.approval.dto.ApprovalResourceBaseParam;
import cn.cordys.crm.approval.dto.ResourceApprovalPostUpdateParam;
import cn.cordys.crm.approval.dto.ResourceSnapshotApprovalParam;
import cn.cordys.crm.approval.service.ApprovalFlowService;
import cn.cordys.crm.approval.service.ApprovalResourceService;
import cn.cordys.crm.productmgmt.domain.ProductManagementDocument;
import cn.cordys.crm.productmgmt.domain.ProductManagementModule;
import cn.cordys.crm.productmgmt.domain.ProductManagementProduct;
import cn.cordys.crm.productmgmt.domain.ProductManagementRequirement;
import cn.cordys.crm.productmgmt.domain.ProductManagementVersion;
import cn.cordys.crm.productmgmt.dto.request.ProductManagementSaveRequest;
import cn.cordys.crm.productmgmt.dto.request.ProductRequirementAdvanceStageRequest;
import cn.cordys.crm.productmgmt.dto.request.ProductRequirementSaveRequest;
import cn.cordys.crm.productmgmt.dto.request.ProductVersionSaveRequest;
import cn.cordys.crm.productmgmt.dto.ProductRequirementWorkflowConfig;
import cn.cordys.crm.productmgmt.mapper.ExtPmProductMapper;
import cn.cordys.crm.system.domain.Attachment;
import cn.cordys.crm.system.domain.OrganizationUser;
import cn.cordys.crm.system.domain.User;
import cn.cordys.crm.system.dto.request.UploadTransferRequest;
import cn.cordys.crm.system.mapper.ExtUserMapper;
import cn.cordys.crm.system.service.AttachmentService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ProductManagementService {

    private static final Pattern REQUIREMENT_NO_PATTERN = Pattern.compile("^PRM-(\\d{4})-(\\d{4})-01$");

    @Resource
    private BaseMapper<ProductManagementProduct> productMapper;
    @Resource
    private BaseMapper<ProductManagementModule> moduleMapper;
    @Resource
    private BaseMapper<ProductManagementVersion> versionMapper;
    @Resource
    private BaseMapper<ProductManagementRequirement> requirementMapper;
    @Resource
    private BaseMapper<ProductManagementDocument> documentMapper;
    @Resource
    private BaseMapper<Attachment> attachmentMapper;
    @Resource
    private BaseMapper<User> userMapper;
    @Resource
    private BaseMapper<OrganizationUser> organizationUserMapper;
    @Resource
    private BaseMapper<ApprovalInstance> approvalInstanceMapper;
    @Resource
    private BaseMapper<ApprovalRecord> approvalRecordMapper;
    @Resource
    private ExtPmProductMapper extPmProductMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private AttachmentService attachmentService;
    @Resource
    private ApprovalResourceService approvalResourceService;
    @Resource
    private ApprovalFlowService approvalFlowService;

    public void checkProductList(List<String> products) {
        if (products != null && products.size() > 20) {
            throw new GenericException(Translator.get("product.length"));
        }
    }

    public List<OptionDTO> listOption(String organizationId) {
        return extPmProductMapper.getOptions(organizationId);
    }

    public List<OptionDTO> getProductOptions(String keyword, String organizationId) {
        return extPmProductMapper.getProductOptions(keyword, organizationId);
    }

    public Pager<List<Map<String, Object>>> listProducts(BasePageRequest request) {
        List<ProductManagementProduct> products = productMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementProduct>().eq(ProductManagementProduct::getOrganizationId, orgId())
        );
        products.sort(Comparator.comparing(ProductManagementProduct::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed());
        List<ProductManagementModule> modules = listModules(null);
        List<ProductManagementRequirement> requirements = listRequirementsByOrg();

        Map<String, Long> moduleCountMap = modules.stream()
                .collect(Collectors.groupingBy(ProductManagementModule::getProductId, Collectors.counting()));
        Map<String, Long> requirementCountMap = requirements.stream()
                .collect(Collectors.groupingBy(ProductManagementRequirement::getProductId, Collectors.counting()));

        List<Map<String, Object>> rows = products.stream().map(product -> {
            Map<String, Object> row = productRow(product);
            row.put("moduleCount", moduleCountMap.getOrDefault(product.getId(), 0L));
            row.put("requirementCount", requirementCountMap.getOrDefault(product.getId(), 0L));
            row.put("iconBg", iconBg(product.getCode()));
            row.put("iconColor", iconColor(product.getCode()));
            row.put("iconText", iconText(product.getCode()));
            row.put("statusBg", statusBg(product.getStatus()));
            row.put("statusColor", statusColor(product.getStatus()));
            return row;
        }).toList();
        return page(rows, request);
    }

    public Map<String, Object> getProduct(String id) {
        ProductManagementProduct product = productMapper.selectByPrimaryKey(id);
        if (product == null) {
            return null;
        }
        Map<String, Object> row = productRow(product);
        List<ProductManagementModule> modules = listModules(product.getId());
        List<ProductManagementRequirement> requirements = listRequirements(product.getId());
        List<ProductManagementVersion> versions = listVersions(product.getId());
        List<ProductManagementDocument> documents = listDocuments(product.getId());

        row.put("modules", buildModuleTree(modules));
        row.put("moduleCount", modules.size());
        row.put("requirementCount", requirements.size());
        row.put("requirements", requirements.stream().map(this::requirementRow).toList());
        row.put("roadmap", versions.stream().map(version -> roadmapRow(version, product)).toList());
        row.put("documents", documents.stream().map(this::documentRow).toList());
        return row;
    }

    public Map<String, Object> saveProduct(ProductManagementSaveRequest request) {
        if (StringUtils.isBlank(request.getCode()) || StringUtils.isBlank(request.getName())) {
            throw new GenericException("产品代号和产品名称不能为空");
        }

        ProductManagementProduct product = StringUtils.isBlank(request.getId())
                ? new ProductManagementProduct()
                : productMapper.selectByPrimaryKey(request.getId());
        boolean isCreate = product == null || StringUtils.isBlank(product.getId());
        if (isCreate) {
            product = new ProductManagementProduct();
            product.setId(IDGenerator.nextStr());
            product.setCreateTime(System.currentTimeMillis());
            product.setCreateUser(userId());
            product.setOrganizationId(orgId());
        }
        product.setCode(request.getCode().trim().toUpperCase());
        product.setName(request.getName().trim());
        product.setVersion(StringUtils.defaultIfBlank(request.getVersion(), "v1.0"));
        product.setNextVersion(StringUtils.defaultIfBlank(product.getNextVersion(), product.getVersion()));
        product.setStatus(StringUtils.defaultIfBlank(request.getStatus(), "规划中"));
        product.setReleaseDate(request.getReleaseDate());
        product.setSlogan(request.getSlogan());
        product.setProductOwnerId(request.getProductOwnerId());
        product.setProductOwnerName(request.getProductOwner());
        product.setDevOwnerId(request.getDevOwnerId());
        product.setDevOwnerName(request.getDevOwner());
        product.setUpdateTime(System.currentTimeMillis());
        product.setUpdateUser(userId());

        if (isCreate) {
            productMapper.insert(product);
        } else {
            productMapper.updateById(product);
        }

        replaceModules(product.getId(), request.getModules());
        ensureVersion(product);
        return getProduct(product.getId());
    }

    public List<Map<String, Object>> roadmap() {
        List<ProductManagementProduct> products = productMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementProduct>().eq(ProductManagementProduct::getOrganizationId, orgId())
        );
        Map<String, ProductManagementProduct> productMap = products.stream()
                .collect(Collectors.toMap(ProductManagementProduct::getId, Function.identity(), (a, b) -> a));
        return versionMapper.selectListByLambda(
                        new LambdaQueryWrapper<ProductManagementVersion>().eq(ProductManagementVersion::getOrganizationId, orgId())
                ).stream()
                .sorted(Comparator.comparing(ProductManagementVersion::getReleaseDate, Comparator.nullsLast(String::compareTo)).reversed())
                .map(version -> roadmapRow(version, productMap.get(version.getProductId())))
                .toList();
    }

    public Map<String, Object> saveVersion(ProductVersionSaveRequest request) {
        if (StringUtils.isBlank(request.getProductId()) || StringUtils.isBlank(request.getVersion())) {
            throw new GenericException("产品ID和版本号不能为空");
        }
        ProductManagementProduct product = productMapper.selectByPrimaryKey(request.getProductId());
        if (product == null) {
            throw new GenericException("产品不存在");
        }

        ProductManagementVersion version = StringUtils.isBlank(request.getId())
                ? new ProductManagementVersion()
                : versionMapper.selectByPrimaryKey(request.getId());
        boolean isCreate = version == null || StringUtils.isBlank(version.getId());
        if (isCreate) {
            version = new ProductManagementVersion();
            version.setId(IDGenerator.nextStr());
            version.setProductId(product.getId());
            fillCreateBase(version);
        }
        version.setVersion(request.getVersion().trim());
        version.setStatus(StringUtils.defaultIfBlank(request.getStatus(), "规划中"));
        version.setReleaseDate(request.getReleaseDate());
        version.setDescription(request.getDescription());
        version.setAttachmentIds(serializeAttachmentIds(request.getAttachmentIds()));
        if (version.getPendingCount() == null) {
            version.setPendingCount(0);
        }
        User productOwner = resolveActiveOrgUser(
                StringUtils.defaultIfBlank(request.getProductOwnerId(), product.getProductOwnerId()),
                "产品负责人不在当前组织或已被移除"
        );
        User devOwner = resolveActiveOrgUser(
                StringUtils.defaultIfBlank(request.getDevOwnerId(), product.getDevOwnerId()),
                "研发负责人不在当前组织或已被移除"
        );
        version.setProductOwnerId(productOwner == null ? null : productOwner.getId());
        version.setProductOwnerName(productOwner == null ? null : productOwner.getName());
        version.setDevOwnerId(devOwner == null ? null : devOwner.getId());
        version.setDevOwnerName(devOwner == null ? null : devOwner.getName());
        version.setOrganizationId(orgId());
        version.setUpdateTime(System.currentTimeMillis());
        version.setUpdateUser(userId());

        if (isCreate) {
            versionMapper.insert(version);
        } else {
            versionMapper.updateById(version);
        }
        if (CollectionUtils.isNotEmpty(request.getAttachmentIds())) {
            attachmentService.appendTemp(new UploadTransferRequest(orgId(), version.getId(), userId(), request.getAttachmentIds()));
        }
        syncProductVersion(product, version);
        return roadmapRow(version, productMapper.selectByPrimaryKey(product.getId()));
    }

    public Map<String, Object> updateVersionStatus(String id, String status) {
        ProductManagementVersion version = versionMapper.selectByPrimaryKey(id);
        if (version == null) {
            throw new GenericException("版本不存在");
        }
        String targetStatus = normalizeVersionTargetStatus(status);
        if (!canMoveVersionStatus(version.getStatus(), targetStatus)) {
            throw new GenericException("当前版本状态不允许该操作");
        }
        version.setStatus(targetStatus);
        version.setUpdateTime(System.currentTimeMillis());
        version.setUpdateUser(userId());
        versionMapper.updateById(version);

        ProductManagementProduct product = productMapper.selectByPrimaryKey(version.getProductId());
        if (product != null) {
            syncProductVersion(product, version);
        }
        return roadmapRow(version, product);
    }

    public void deleteVersion(String id) {
        ProductManagementVersion version = versionMapper.selectByPrimaryKey(id);
        if (version == null || !Objects.equals(version.getOrganizationId(), orgId())) {
            throw new GenericException("版本不存在");
        }
        ProductManagementProduct product = productMapper.selectByPrimaryKey(version.getProductId());
        List<ProductManagementVersion> productVersions = listVersions(version.getProductId());
        boolean isCurrentVersion = product != null && Objects.equals(product.getVersion(), version.getVersion());
        if (!canDeleteVersion(isCurrentVersion, productVersions.size())) {
            throw new GenericException("产品至少需要保留一个版本");
        }
        versionMapper.deleteByPrimaryKey(id);
        if (isCurrentVersion) {
            List<ProductManagementVersion> remainingVersions = productVersions.stream()
                    .filter(item -> !Objects.equals(item.getId(), id))
                    .toList();
            ProductManagementVersion replacement = selectCurrentVersionReplacement(remainingVersions);
            if (replacement != null) {
                product.setVersion(replacement.getVersion());
                product.setStatus(normalizeOnlineStatus(replacement.getStatus()));
                product.setReleaseDate(replacement.getReleaseDate());
                product.setNextVersion(remainingVersions.stream()
                        .filter(item -> !StringUtils.equalsAny(normalizeOnlineStatus(item.getStatus()), "已发布", "已上线"))
                        .max(versionOrder())
                        .map(ProductManagementVersion::getVersion)
                        .orElse(null));
                product.setUpdateTime(System.currentTimeMillis());
                product.setUpdateUser(userId());
                productMapper.updateById(product);
            }
        }
    }

    public Pager<List<Map<String, Object>>> listRequirements(BasePageRequest request) {
        List<Map<String, Object>> rows = listRequirementsByOrg().stream()
                .peek(this::syncStatusFromApproval)
                .sorted(Comparator.comparing(ProductManagementRequirement::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed())
                .map(this::requirementRow)
                .toList();
        return page(rows, request);
    }

    public Map<String, Object> getRequirement(String idOrNo) {
        ProductManagementRequirement requirement = findRequirement(idOrNo);
        if (requirement == null) {
            return null;
        }
        syncStatusFromApproval(requirement);
        return requirementRow(requirement);
    }

    /**
     * 获取产品需求简要信息（审批资源反射调用）。
     */
    public Map<String, Object> getSimple(String id) {
        return getRequirement(id);
    }

    /**
     * 批量获取产品需求简要信息（审批资源反射调用）。
     */
    public List<Map<String, Object>> batchGetSimpleByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(this::getRequirement)
                .filter(Objects::nonNull)
                .toList();
    }

    public Map<String, Object> saveRequirement(ProductRequirementSaveRequest request) {
        if (StringUtils.isBlank(request.getTitle())) {
            throw new GenericException("需求标题不能为空");
        }
        ProductManagementProduct product = resolveProduct(request.getProductId(), request.getProduct());
        ProductManagementRequirement requirement = new ProductManagementRequirement();
        requirement.setId(IDGenerator.nextStr());
        requirement.setRequirementNo(nextRequirementNo());
        requirement.setTitle(request.getTitle().trim());
        requirement.setType(StringUtils.defaultIfBlank(request.getType(), "功能新增"));
        requirement.setSource(StringUtils.defaultIfBlank(request.getSource(), "客户"));
        requirement.setProductId(product == null ? null : product.getId());
        requirement.setProductName(product == null ? request.getProduct() : productTag(product));
        requirement.setTargetVersion(StringUtils.defaultIfBlank(request.getRelease(), "--"));
        requirement.setPriority(StringUtils.defaultIfBlank(request.getPriority(), "P2"));
        requirement.setStatus("需求池");
        requirement.setStage("需求池");
        requirement.setExpectedRelease(request.getRelease());
        requirement.setOwnerId(product == null ? null : product.getProductOwnerId());
        requirement.setOwnerName(product == null ? "Administrator" : StringUtils.defaultIfBlank(product.getProductOwnerName(), "Administrator"));
        requirement.setDescription(request.getDescription());
        requirement.setAcceptanceCriteria(request.getAcceptance());
        requirement.setApprovalStatus(ApprovalStatus.PENDING.name());
        fillCreateBase(requirement);
        requirement.setStageRecordJson(JSON.toJSONString(initRequirementRecords(requirement)));
        requirementMapper.insert(requirement);
        submitRequirementToReview(requirement);
        return requirementRow(requirement);
    }

    public Map<String, Object> updateRequirement(ProductRequirementSaveRequest request) {
        if (StringUtils.isBlank(request.getId())) {
            throw new GenericException("需求ID不能为空");
        }
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(request.getId());
        if (requirement == null || !Objects.equals(requirement.getOrganizationId(), orgId())) {
            throw new GenericException("需求不存在");
        }
        if (!canEditRequirement(requirement.getStatus(), requirement.getApprovalStatus())) {
            throw new GenericException("当前需求状态不允许编辑");
        }
        ProductManagementProduct product = resolveProduct(request.getProductId(), request.getProduct());
        requirement.setTitle(request.getTitle().trim());
        requirement.setType(StringUtils.defaultIfBlank(request.getType(), requirement.getType()));
        requirement.setSource(StringUtils.defaultIfBlank(request.getSource(), requirement.getSource()));
        requirement.setProductId(product == null ? requirement.getProductId() : product.getId());
        requirement.setProductName(product == null ? request.getProduct() : productTag(product));
        requirement.setTargetVersion(StringUtils.defaultIfBlank(request.getRelease(), requirement.getTargetVersion()));
        requirement.setPriority(StringUtils.defaultIfBlank(request.getPriority(), requirement.getPriority()));
        requirement.setDescription(request.getDescription());
        requirement.setAcceptanceCriteria(request.getAcceptance());
        requirement.setUpdateTime(System.currentTimeMillis());
        requirement.setUpdateUser(userId());
        requirementMapper.updateById(requirement);
        return requirementRow(requirement);
    }

    public void deleteRequirement(String id) {
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(id);
        if (requirement == null || !Objects.equals(requirement.getOrganizationId(), orgId())) {
            throw new GenericException("需求不存在");
        }
        if (!canDeleteRequirement(requirement.getStatus(), requirement.getApprovalStatus())) {
            throw new GenericException("当前需求状态不允许删除");
        }
        requirementMapper.deleteByPrimaryKey(id);
    }

    public Map<String, Object> submitForReview(String id) {
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(id);
        if (requirement == null || !Objects.equals(requirement.getOrganizationId(), orgId())) {
            throw new GenericException("需求不存在");
        }
        if (!canSubmitForReview(requirement.getStatus(), requirement.getApprovalStatus())) {
            throw new GenericException("当前需求状态不允许重新提交评审");
        }
        submitRequirementToReview(requirement);
        return getRequirement(requirement.getId());
    }

    public Map<String, Object> revokeRequirementReview(String id) {
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(id);
        if (requirement == null || !Objects.equals(requirement.getOrganizationId(), orgId())) {
            throw new GenericException("需求不存在");
        }
        if (!canRevokeRequirementReview(requirement.getStatus(), requirement.getApprovalStatus(), requirement.getCreateUser(), userId())) {
            throw new GenericException("当前需求状态不允许撤回");
        }
        ApprovalResourceBaseParam param = new ApprovalResourceBaseParam();
        param.setResourceId(requirement.getId());
        param.setFormKey(FormKey.PRODUCT_REQUIREMENT.getKey());
        approvalResourceService.revoke(param, userId());
        applyRequirementStatusByApproval(requirement, ApprovalStatus.REVOKED.name(), resolveUserDisplayName(userId(), requirement.getOwnerName()));
        return requirementRow(requirement);
    }

    public Map<String, Object> advanceStage(String id, ProductRequirementAdvanceStageRequest request) {
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(id);
        if (requirement == null || !Objects.equals(requirement.getOrganizationId(), orgId())) {
            throw new GenericException("需求不存在");
        }
        ProductRequirementWorkflowConfig workflow = workflowConfig(requirement);
        ProductRequirementWorkflowConfig.Stage currentStage = workflow.stage(requirement.getStage());
        if (currentStage == null || !canCurrentUserOperate(currentStage, requirement)) {
            throw new GenericException("你不是当前阶段负责人，无法推进阶段");
        }
        ProductRequirementWorkflowConfig.Stage nextStage = workflow.nextStage(requirement.getStage());
        if (nextStage == null) {
            throw new GenericException("当前需求已到最后阶段");
        }
        if (currentStage.requiresProductLink()) {
            bindAcceptanceProduct(requirement, request);
        }
        List<Map<String, Object>> records = readStageRecords(requirement);
        removeLastPending(records);
        appendDoneRecord(
                records,
                currentStage.name() + "提交",
                resolveUserDisplayName(userId(), currentStage.ownerLabel(requirement.getOwnerName())),
                request == null ? null : request.getContent(),
                attachmentRows(request == null ? List.of() : request.getAttachmentIds())
        );
        appendPendingStageRecord(records, nextStage.name(), nextStage.ownerLabel(requirement.getOwnerName()));
        requirement.setStageRecordJson(JSON.toJSONString(records));
        applyCurrentStage(requirement, nextStage);
        requirement.setUpdateTime(System.currentTimeMillis());
        requirement.setUpdateUser(userId());
        requirementMapper.updateById(requirement);
        return requirementRow(requirement);
    }

    public Map<String, Object> returnStage(String id, ProductRequirementAdvanceStageRequest request) {
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(id);
        if (requirement == null || !Objects.equals(requirement.getOrganizationId(), orgId())) {
            throw new GenericException("需求不存在");
        }
        ProductRequirementWorkflowConfig workflow = workflowConfig(requirement);
        ProductRequirementWorkflowConfig.Stage currentStage = workflow.stage(requirement.getStage());
        ProductRequirementWorkflowConfig.Stage previousStage = workflow.previousStage(requirement.getStage());
        if (currentStage == null || previousStage == null || !currentStage.returnable()) {
            throw new GenericException("当前阶段不允许退回");
        }
        if (!canCurrentUserOperate(currentStage, requirement)) {
            throw new GenericException("你不是当前阶段负责人，无法退回需求");
        }
        List<Map<String, Object>> records = readStageRecords(requirement);
        removeLastPending(records);
        records.add(buildRecord(
                currentStage.name() + "退回",
                resolveUserDisplayName(userId(), currentStage.ownerLabel(requirement.getOwnerName())),
                System.currentTimeMillis(),
                "rejected",
                request == null ? null : request.getContent(),
                attachmentRows(request == null ? List.of() : request.getAttachmentIds())
        ));
        appendPendingStageRecord(records, previousStage.name(), previousStage.ownerLabel(requirement.getOwnerName()));
        requirement.setStageRecordJson(JSON.toJSONString(records));
        applyCurrentStage(requirement, previousStage);
        requirement.setUpdateTime(System.currentTimeMillis());
        requirement.setUpdateUser(userId());
        requirementMapper.updateById(requirement);
        return requirementRow(requirement);
    }

    private void submitRequirementToReview(ProductManagementRequirement requirement) {
        List<Map<String, Object>> records = readStageRecords(requirement);
        if (CollectionUtils.isEmpty(records)) {
            records = initRequirementRecords(requirement);
        }
        if (!hasCreateApprovalFlow()) {
            log.warn("产品需求未命中创建审批流, requirementId={}", requirement.getId());
            requirement.setApprovalStatus(ApprovalStatus.PENDING.name());
            requirement.setStatus("需求池");
            requirement.setStage("需求池");
            appendPendingStageRecord(records, "需求池", StringUtils.defaultIfBlank(requirement.getOwnerName(), "审批流程角色"));
            requirement.setStageRecordJson(JSON.toJSONString(records));
            requirement.setUpdateTime(System.currentTimeMillis());
            requirement.setUpdateUser(userId());
            requirementMapper.updateById(requirement);
            return;
        }
        ApprovalResourceBaseParam param = new ApprovalResourceBaseParam();
        param.setResourceId(requirement.getId());
        param.setFormKey(FormKey.PRODUCT_REQUIREMENT.getKey());
        ProductRequirementWorkflowConfig workflow = enabledWorkflowConfig();
        requirement.setWorkflowConfigJson(JSON.toJSONString(workflow));
        approvalResourceService.push(param, orgId(), userId());
        applyRequirementStatusByApproval(requirement, ApprovalStatus.APPROVING.name(), StringUtils.defaultIfBlank(requirement.getOwnerName(), "审批流程角色"));
    }

    private boolean hasCreateApprovalFlow() {
        var approvalFlow = approvalFlowService.getEnabledFlow(FormKey.PRODUCT_REQUIREMENT.getKey(), orgId());
        return approvalFlow != null && Boolean.TRUE.equals(approvalFlow.getCreateExecute());
    }

    static boolean canEditRequirement(String status, String approvalStatus) {
        return Strings.CI.equals(status, "需求池")
                && Strings.CI.equalsAny(approvalStatus, ApprovalStatus.UNAPPROVED.name(), ApprovalStatus.REVOKED.name());
    }

    static boolean canDeleteRequirement(String status, String approvalStatus) {
        return canEditRequirement(status, approvalStatus);
    }

    static boolean canSubmitForReview(String status, String approvalStatus) {
        return canEditRequirement(status, approvalStatus);
    }

    static boolean canRevokeRequirementReview(String status, String approvalStatus, String creatorId, String currentUserId) {
        return Strings.CI.equals(status, "需求评审")
                && Strings.CI.equals(approvalStatus, ApprovalStatus.APPROVING.name())
                && StringUtils.isNotBlank(currentUserId)
                && StringUtils.equals(creatorId, currentUserId);
    }

    static boolean canAdvanceRequirementStage(String status) {
        return Strings.CI.equals(status, "产品设计");
    }

    static String resolveRequirementStatusByApproval(String currentStatus, String approvalStatus) {
        boolean reviewStage = Strings.CI.equalsAny(currentStatus, "需求池", "需求评审");
        if (reviewStage && Strings.CI.equals(approvalStatus, ApprovalStatus.APPROVED.name())) {
            return "产品设计";
        }
        if (reviewStage && Strings.CI.equals(approvalStatus, ApprovalStatus.APPROVING.name())) {
            return "需求评审";
        }
        if (reviewStage && Strings.CI.equalsAny(approvalStatus, ApprovalStatus.UNAPPROVED.name(), ApprovalStatus.REVOKED.name())) {
            return "需求池";
        }
        return StringUtils.defaultIfBlank(currentStatus, "需求池");
    }

    private void applyRequirementStatusByApproval(ProductManagementRequirement requirement, String approvalStatus, String actorName) {
        String previousStatus = requirement.getStatus();
        String previousApprovalStatus = requirement.getApprovalStatus();
        String resolvedStatus = resolveRequirementStatusByApproval(requirement.getStatus(), approvalStatus);
        requirement.setApprovalStatus(approvalStatus);
        requirement.setStatus(resolvedStatus);
        requirement.setStage(resolvedStatus);
        if (Strings.CI.equals(resolvedStatus, "产品设计")) {
            ProductRequirementWorkflowConfig.Stage stage = workflowConfig(requirement).stage(resolvedStatus);
            if (stage != null) {
                applyCurrentStage(requirement, stage);
            }
        }
        requirement.setStageRecordJson(JSON.toJSONString(syncRequirementRecords(requirement, previousStatus, previousApprovalStatus, actorName)));
        requirement.setUpdateTime(System.currentTimeMillis());
        requirement.setUpdateUser(userId());
        requirementMapper.updateById(requirement);
    }

    public void updateSnapshotApprovalStatus(ResourceSnapshotApprovalParam param) {
        if (param == null || StringUtils.isBlank(param.getResourceId()) || StringUtils.isBlank(param.getApprovalStatus())) {
            return;
        }
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(param.getResourceId());
        if (requirement != null) {
            applyRequirementStatusByApproval(requirement, param.getApprovalStatus(), resolveLatestApprovalActorName(requirement));
        }
    }

    public void updateApprovalPostField(ResourceApprovalPostUpdateParam postFieldParam) {
        if (postFieldParam == null || StringUtils.isBlank(postFieldParam.getResourceId())) {
            return;
        }
        ProductManagementRequirement requirement = requirementMapper.selectByPrimaryKey(postFieldParam.getResourceId());
        if (requirement == null) {
            return;
        }
        applyRequirementStatusByApproval(requirement, requirement.getApprovalStatus(), resolveLatestApprovalActorName(requirement));
    }

    private void replaceModules(String productId, List<ProductManagementSaveRequest.ModulePayload> modules) {
        moduleMapper.deleteByLambda(new LambdaQueryWrapper<ProductManagementModule>().eq(ProductManagementModule::getProductId, productId));
        if (CollectionUtils.isEmpty(modules)) {
            return;
        }
        int pos = 1;
        for (ProductManagementSaveRequest.ModulePayload module : modules) {
            saveModule(productId, null, module, pos++);
        }
    }

    private void saveModule(String productId, String parentId, ProductManagementSaveRequest.ModulePayload payload, int pos) {
        if (payload == null || StringUtils.isBlank(payload.getName())) {
            return;
        }
        ProductManagementModule module = new ProductManagementModule();
        module.setId(IDGenerator.nextStr());
        module.setProductId(productId);
        module.setParentId(parentId);
        module.setName(payload.getName().trim());
        module.setOwnerId(payload.getOwnerId());
        module.setOwnerName(payload.getOwnerName());
        module.setPendingCount(payload.getPendingCount() == null ? 0 : payload.getPendingCount());
        module.setPos(pos);
        fillCreateBase(module);
        moduleMapper.insert(module);

        if (CollectionUtils.isEmpty(payload.getChildren())) {
            return;
        }
        int childPos = 1;
        for (ProductManagementSaveRequest.ModulePayload child : payload.getChildren()) {
            saveModule(productId, module.getId(), child, childPos++);
        }
    }

    private void ensureVersion(ProductManagementProduct product) {
        List<ProductManagementVersion> versions = listVersions(product.getId());
        boolean exists = versions.stream().anyMatch(version -> Objects.equals(version.getVersion(), product.getVersion()));
        if (exists) {
            return;
        }
        ProductManagementVersion version = new ProductManagementVersion();
        version.setId(IDGenerator.nextStr());
        version.setProductId(product.getId());
        version.setVersion(product.getVersion());
        version.setStatus(product.getStatus());
        version.setReleaseDate(product.getReleaseDate());
        version.setDescription("");
        version.setPendingCount(0);
        version.setProductOwnerId(product.getProductOwnerId());
        version.setProductOwnerName(product.getProductOwnerName());
        version.setDevOwnerId(product.getDevOwnerId());
        version.setDevOwnerName(product.getDevOwnerName());
        version.setAttachmentIds("");
        fillCreateBase(version);
        versionMapper.insert(version);
    }

    private void syncProductVersion(ProductManagementProduct product, ProductManagementVersion version) {
        if (product == null || version == null) {
            return;
        }
        if (Objects.equals(version.getStatus(), "已发布") || Objects.equals(version.getStatus(), "已上线")) {
            product.setVersion(version.getVersion());
            product.setStatus("已发布");
            product.setReleaseDate(version.getReleaseDate());
        } else {
            product.setNextVersion(version.getVersion());
            product.setStatus(version.getStatus());
        }
        product.setUpdateTime(System.currentTimeMillis());
        product.setUpdateUser(userId());
        productMapper.updateById(product);
    }

    private String normalizeVersionTargetStatus(String status) {
        if (StringUtils.equalsAny(status, "developing", "开发", "开发中")) {
            return "开发中";
        }
        if (StringUtils.equalsAny(status, "released", "online", "发布", "已发布", "已上线")) {
            return "已发布";
        }
        return "规划中";
    }

    static boolean canMoveVersionStatus(String currentStatus, String targetStatus) {
        String current = normalizeOnlineStatus(currentStatus);
        if (Objects.equals(current, targetStatus)) {
            return true;
        }
        if (Objects.equals(current, "规划中")) {
            return Objects.equals(targetStatus, "开发中");
        }
        if (Objects.equals(current, "开发中")) {
            return Objects.equals(targetStatus, "已发布");
        }
        return false;
    }

    static boolean canDeleteVersion(boolean currentVersion, int versionCount) {
        return !currentVersion || versionCount > 1;
    }

    static ProductManagementVersion selectCurrentVersionReplacement(List<ProductManagementVersion> versions) {
        Comparator<ProductManagementVersion> order = versionOrder();
        return versions.stream()
                .filter(version -> StringUtils.equalsAny(normalizeOnlineStatus(version.getStatus()), "已发布", "已上线"))
                .max(order)
                .orElseGet(() -> versions.stream().max(order).orElse(null));
    }

    private static Comparator<ProductManagementVersion> versionOrder() {
        return Comparator.comparing(ProductManagementVersion::getReleaseDate, Comparator.nullsFirst(String::compareTo))
                .thenComparing(ProductManagementVersion::getCreateTime, Comparator.nullsFirst(Long::compareTo));
    }

    private List<Map<String, Object>> buildModuleTree(List<ProductManagementModule> modules) {
        Map<String, List<ProductManagementModule>> children = modules.stream()
                .filter(module -> StringUtils.isNotBlank(module.getParentId()))
                .collect(Collectors.groupingBy(ProductManagementModule::getParentId));
        return modules.stream()
                .filter(module -> StringUtils.isBlank(module.getParentId()))
                .sorted(Comparator.comparing(ProductManagementModule::getPos, Comparator.nullsLast(Integer::compareTo)))
                .map(module -> moduleRow(module, children.getOrDefault(module.getId(), List.of())))
                .toList();
    }

    private Map<String, Object> moduleRow(ProductManagementModule module, List<ProductManagementModule> childModules) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", module.getId());
        row.put("name", module.getName());
        row.put("ownerId", module.getOwnerId());
        row.put("ownerName", module.getOwnerName());
        row.put("pendingCount", module.getPendingCount());
        row.put("children", childModules.stream()
                .sorted(Comparator.comparing(ProductManagementModule::getPos, Comparator.nullsLast(Integer::compareTo)))
                .map(child -> moduleRow(child, List.of()))
                .toList());
        return row;
    }

    private Map<String, Object> productRow(ProductManagementProduct product) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", product.getId());
        row.put("code", product.getCode());
        row.put("name", product.getName());
        row.put("version", product.getVersion());
        row.put("nextVersion", product.getNextVersion());
        row.put("status", product.getStatus());
        row.put("releaseDate", product.getReleaseDate());
        row.put("slogan", product.getSlogan());
        row.put("description", product.getSlogan());
        row.put("productOwner", product.getProductOwnerName());
        row.put("productOwnerId", product.getProductOwnerId());
        row.put("devOwner", product.getDevOwnerName());
        row.put("devOwnerId", product.getDevOwnerId());
        return row;
    }

    private Map<String, Object> roadmapRow(ProductManagementVersion version, ProductManagementProduct product) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", version.getId());
        row.put("productId", version.getProductId());
        row.put("product", product == null ? "" : productTag(product));
        row.put("productType", product == null ? "" : productKey(product));
        row.put("version", version.getVersion());
        row.put("releaseDate", version.getReleaseDate());
        row.put("status", normalizeOnlineStatus(version.getStatus()));
        row.put("statusType", statusType(version.getStatus()));
        row.put("pendingCount", version.getPendingCount());
        row.put("productOwnerId", version.getProductOwnerId());
        row.put("productOwner", StringUtils.defaultIfBlank(version.getProductOwnerName(), product == null ? "" : product.getProductOwnerName()));
        row.put("devOwnerId", version.getDevOwnerId());
        row.put("devOwner", StringUtils.defaultIfBlank(version.getDevOwnerName(), product == null ? "" : product.getDevOwnerName()));
        Map<String, Boolean> ownerActiveMap = userActiveMap(List.of(version.getProductOwnerId(), version.getDevOwnerId()));
        row.put("productOwnerActive", StringUtils.isBlank(version.getProductOwnerId()) || ownerActiveMap.containsKey(version.getProductOwnerId()));
        row.put("devOwnerActive", StringUtils.isBlank(version.getDevOwnerId()) || ownerActiveMap.containsKey(version.getDevOwnerId()));
        row.put("description", version.getDescription());
        List<String> attachmentIds = parseAttachmentIds(version.getAttachmentIds());
        row.put("attachmentIds", attachmentIds);
        row.put("attachments", attachmentRows(attachmentIds));
        return row;
    }

    private User resolveActiveOrgUser(String userId, String errorMessage) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        List<User> users = extUserMapper.getOrgUserByUserIds(orgId(), List.of(userId));
        if (CollectionUtils.isEmpty(users)) {
            throw new GenericException(errorMessage);
        }
        return users.getFirst();
    }

    private Map<String, Boolean> userActiveMap(List<String> userIds) {
        List<String> ids = userIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        return extUserMapper.getOrgUserByUserIds(orgId(), ids).stream()
                .collect(Collectors.toMap(User::getId, user -> true, (a, b) -> a));
    }

    private String serializeAttachmentIds(List<String> attachmentIds) {
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return "";
        }
        return attachmentIds.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private List<String> parseAttachmentIds(String attachmentIds) {
        if (StringUtils.isBlank(attachmentIds)) {
            return List.of();
        }
        return List.of(StringUtils.split(attachmentIds, ',')).stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

    private List<Map<String, Object>> attachmentRows(List<String> attachmentIds) {
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return List.of();
        }
        Map<String, Attachment> attachmentMap = attachmentMapper.selectByIds(attachmentIds).stream()
                .collect(Collectors.toMap(Attachment::getId, Function.identity(), (a, b) -> a));
        return attachmentIds.stream().map(id -> {
            Attachment attachment = attachmentMap.get(id);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", id);
            row.put("name", attachment == null ? id : attachment.getName());
            row.put("size", attachment == null ? null : attachment.getSize());
            return row;
        }).toList();
    }

    private ProductRequirementWorkflowConfig enabledWorkflowConfig() {
        var flow = approvalFlowService.getEnabledFlow(FormKey.PRODUCT_REQUIREMENT.getKey(), orgId());
        if (flow == null || StringUtils.isBlank(flow.getBusinessConfig())) {
            return ProductRequirementWorkflowConfig.defaultConfig();
        }
        ProductRequirementWorkflowConfig config = JSON.parseObject(flow.getBusinessConfig(), ProductRequirementWorkflowConfig.class);
        return config == null || CollectionUtils.isEmpty(config.stages())
                ? ProductRequirementWorkflowConfig.defaultConfig()
                : config;
    }

    private ProductRequirementWorkflowConfig workflowConfig(ProductManagementRequirement requirement) {
        if (requirement == null || StringUtils.isBlank(requirement.getWorkflowConfigJson())) {
            return enabledWorkflowConfig();
        }
        ProductRequirementWorkflowConfig config = JSON.parseObject(
                requirement.getWorkflowConfigJson(),
                ProductRequirementWorkflowConfig.class
        );
        return config == null || CollectionUtils.isEmpty(config.stages())
                ? ProductRequirementWorkflowConfig.defaultConfig()
                : config;
    }

    private void applyCurrentStage(ProductManagementRequirement requirement, ProductRequirementWorkflowConfig.Stage stage) {
        requirement.setStatus(stage.name());
        requirement.setStage(stage.name());
        requirement.setCurrentAssigneeIds(JSON.toJSONString(stage.assigneeIds()));
        requirement.setCurrentAssigneeNames(stage.ownerLabel(requirement.getOwnerName()));
    }

    private boolean canCurrentUserOperate(
            ProductRequirementWorkflowConfig.Stage stage,
            ProductManagementRequirement requirement
    ) {
        if (stage == null) {
            return false;
        }
        List<String> activeAssigneeIds = stage.assigneeIds().isEmpty()
                ? List.of()
                : organizationUserMapper.selectListByLambda(
                        new LambdaQueryWrapper<OrganizationUser>()
                                .eq(OrganizationUser::getOrganizationId, orgId())
                                .eq(OrganizationUser::getEnable, true)
                                .in(OrganizationUser::getUserId, stage.assigneeIds())
                ).stream().map(OrganizationUser::getUserId).distinct().toList();
        String fallbackOwnerId = StringUtils.defaultIfBlank(requirement.getOwnerId(), requirement.getCreateUser());
        return activeAssigneeIds.isEmpty()
                ? StringUtils.equals(userId(), fallbackOwnerId)
                : activeAssigneeIds.contains(userId());
    }

    private void bindAcceptanceProduct(ProductManagementRequirement requirement, ProductRequirementAdvanceStageRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getModuleId(), request.getVersionId())) {
            throw new GenericException("产品验收时必须选择关联模块和预发布版本");
        }
        ProductManagementModule module = moduleMapper.selectByPrimaryKey(request.getModuleId());
        ProductManagementVersion version = versionMapper.selectByPrimaryKey(request.getVersionId());
        if (module == null || version == null
                || !Objects.equals(module.getProductId(), requirement.getProductId())
                || !Objects.equals(version.getProductId(), requirement.getProductId())) {
            throw new GenericException("关联模块或预发布版本不属于当前产品");
        }
        requirement.setModuleId(module.getId());
        requirement.setModuleName(module.getName());
        requirement.setTargetVersionId(version.getId());
        requirement.setTargetVersion(version.getVersion());
    }

    private Map<String, Object> requirementRow(ProductManagementRequirement requirement) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("rawId", requirement.getId());
        row.put("id", requirement.getRequirementNo());
        row.put("title", requirement.getTitle());
        row.put("detailTitle", requirement.getTitle());
        row.put("product", requirement.getProductName());
        row.put("productId", requirement.getProductId());
        row.put("productKey", productKey(requirement.getProductName()));
        row.put("version", StringUtils.defaultIfBlank(requirement.getTargetVersion(), "--"));
        row.put("source", requirement.getSource());
        row.put("priority", requirement.getPriority());
        row.put("priorityType", priorityType(requirement.getPriority()));
        row.put("status", normalizeRequirementStatus(requirement.getStatus()));
        row.put("statusType", statusType(requirement.getStatus()));
        row.put("stage", requirement.getStage());
        row.put("owner", requirement.getOwnerName());
        row.put("type", requirement.getType());
        row.put("module", requirement.getModuleName());
        row.put("moduleId", requirement.getModuleId());
        row.put("description", requirement.getDescription());
        row.put("acceptance", requirement.getAcceptanceCriteria());
        row.put("expectedRelease", requirement.getExpectedRelease());
        row.put("approvalStatus", requirement.getApprovalStatus());
        row.put("createUser", requirement.getCreateUser());
        row.put("records", buildRequirementRecordsView(requirement));
        ProductRequirementWorkflowConfig workflow = workflowConfig(requirement);
        ProductRequirementWorkflowConfig.Stage currentStage = workflow.stage(requirement.getStage());
        boolean currentOwner = canCurrentUserOperate(currentStage, requirement);
        row.put("workflowStages", workflow.stages());
        row.put("currentAssigneeNames", requirement.getCurrentAssigneeNames());
        row.put("targetVersionId", requirement.getTargetVersionId());
        row.put("availableActions", Map.of(
                "edit", canEditRequirement(requirement.getStatus(), requirement.getApprovalStatus()),
                "delete", canDeleteRequirement(requirement.getStatus(), requirement.getApprovalStatus()),
                "revoke", canRevokeRequirementReview(requirement.getStatus(), requirement.getApprovalStatus(), requirement.getCreateUser(), userId()),
                "resubmit", canSubmitForReview(requirement.getStatus(), requirement.getApprovalStatus()),
                "advance", currentOwner && workflow.nextStage(requirement.getStage()) != null,
                "return", currentOwner && currentStage != null && currentStage.returnable() && workflow.previousStage(requirement.getStage()) != null
        ));
        return row;
    }

    private Map<String, Object> documentRow(ProductManagementDocument document) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", document.getId());
        row.put("name", document.getName());
        row.put("type", document.getType());
        row.put("size", document.getSizeText());
        row.put("updated", document.getUpdateTime());
        return row;
    }

    private ProductManagementRequirement findRequirement(String idOrNo) {
        ProductManagementRequirement byId = requirementMapper.selectByPrimaryKey(idOrNo);
        if (byId != null) {
            return byId;
        }
        List<ProductManagementRequirement> list = requirementMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementRequirement>()
                        .eq(ProductManagementRequirement::getOrganizationId, orgId())
                        .eq(ProductManagementRequirement::getRequirementNo, idOrNo)
        );
        return list.isEmpty() ? null : list.getFirst();
    }

    private void syncStatusFromApproval(ProductManagementRequirement requirement) {
        if (requirement == null) {
            return;
        }
        String resolvedStatus = resolveRequirementStatusByApproval(requirement.getStatus(), requirement.getApprovalStatus());
        if (!Strings.CI.equals(requirement.getStatus(), resolvedStatus) || !Strings.CI.equals(requirement.getStage(), resolvedStatus)) {
            List<Map<String, Object>> records = syncRequirementRecords(
                    requirement,
                    requirement.getStatus(),
                    requirement.getApprovalStatus(),
                    resolveLatestApprovalActorName(requirement)
            );
            requirement.setStatus(resolvedStatus);
            requirement.setStage(resolvedStatus);
            requirement.setStageRecordJson(JSON.toJSONString(records));
            requirement.setUpdateTime(System.currentTimeMillis());
            requirement.setUpdateUser(userId());
            requirementMapper.updateById(requirement);
        }
    }

    private List<Map<String, Object>> buildRequirementRecordsView(ProductManagementRequirement requirement) {
        List<Map<String, Object>> records = readStageRecords(requirement);
        if (CollectionUtils.isEmpty(records)) {
            records = initRequirementRecords(requirement);
        }
        return records;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readStageRecords(ProductManagementRequirement requirement) {
        if (requirement == null || StringUtils.isBlank(requirement.getStageRecordJson())) {
            return new ArrayList<>();
        }
        List<Map> rawRecords = JSON.parseArray(requirement.getStageRecordJson(), Map.class);
        return rawRecords.stream()
                .map(record -> new LinkedHashMap<String, Object>(record))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Map<String, Object>> initRequirementRecords(ProductManagementRequirement requirement) {
        List<Map<String, Object>> records = new ArrayList<>();
        records.add(buildRecord(
                "创建需求",
                resolveUserDisplayName(requirement.getCreateUser(), requirement.getOwnerName()),
                requirement.getCreateTime(),
                "done",
                null,
                List.of()
        ));
        appendPendingStageRecord(records, "需求评审", StringUtils.defaultIfBlank(requirement.getOwnerName(), "审批流程角色"));
        return records;
    }

    private List<Map<String, Object>> syncRequirementRecords(
            ProductManagementRequirement requirement,
            String previousStatus,
            String previousApprovalStatus,
            String actorName
    ) {
        List<Map<String, Object>> records = readStageRecords(requirement);
        if (CollectionUtils.isEmpty(records)) {
            records = initRequirementRecords(requirement);
        }
        String approvalStatus = requirement.getApprovalStatus();
        String resolvedStatus = resolveRequirementStatusByApproval(previousStatus, approvalStatus);
        if (Strings.CI.equals(approvalStatus, ApprovalStatus.APPROVING.name())) {
            replaceLastPendingStage(records, "需求评审", StringUtils.defaultIfBlank(actorName, "审批流程角色"));
            return records;
        }
        if (Strings.CI.equals(approvalStatus, ApprovalStatus.APPROVED.name())
                && Strings.CI.equalsAny(previousStatus, "需求池", "需求评审")) {
            removeLastPending(records);
            appendDoneRecord(records, "需求通过", StringUtils.defaultIfBlank(actorName, requirement.getOwnerName()), null, List.of());
            appendPendingStageRecord(records, resolvedStatus, StringUtils.defaultIfBlank(requirement.getOwnerName(), "审批流程角色"));
            return records;
        }
        if (Strings.CI.equals(approvalStatus, ApprovalStatus.UNAPPROVED.name())
                && Strings.CI.equals(previousStatus, "需求评审")) {
            removeLastPending(records);
            records.add(buildRecord("需求驳回", StringUtils.defaultIfBlank(actorName, "审批流程角色"), System.currentTimeMillis(), "rejected", null, List.of()));
            appendPendingStageRecord(records, "需求池", resolveUserDisplayName(requirement.getCreateUser(), requirement.getOwnerName()));
            return records;
        }
        if (Strings.CI.equals(approvalStatus, ApprovalStatus.REVOKED.name())
                && Strings.CI.equals(previousStatus, "需求评审")) {
            removeLastPending(records);
            records.add(buildRecord("需求撤回", StringUtils.defaultIfBlank(actorName, requirement.getOwnerName()), System.currentTimeMillis(), "rejected", null, List.of()));
            appendPendingStageRecord(records, "需求池", resolveUserDisplayName(requirement.getCreateUser(), requirement.getOwnerName()));
        }
        return records;
    }

    private void appendPendingStageRecord(List<Map<String, Object>> records, String title, String owner) {
        removeLastPending(records);
        records.add(buildRecord(title, owner, null, "pending", null, List.of()));
    }

    private void replaceLastPendingStage(List<Map<String, Object>> records, String title, String owner) {
        removeLastPending(records);
        records.add(buildRecord(title, owner, null, "pending", null, List.of()));
    }

    private void appendDoneRecord(
            List<Map<String, Object>> records,
            String title,
            String owner,
            String content,
            List<Map<String, Object>> attachments
    ) {
        records.add(buildRecord(title, owner, System.currentTimeMillis(), "done", content, attachments));
    }

    private void removeLastPending(List<Map<String, Object>> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        Map<String, Object> last = records.getLast();
        if (Strings.CI.equals(String.valueOf(last.get("state")), "pending")) {
            records.removeLast();
        }
    }

    private Map<String, Object> buildRecord(
            String title,
            String owner,
            Long time,
            String state,
            String content,
            List<Map<String, Object>> attachments
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("title", title);
        row.put("owner", owner);
        row.put("time", time);
        row.put("state", state);
        row.put("content", content);
        row.put("attachments", attachments == null ? List.of() : attachments);
        return row;
    }

    private String resolveLatestApprovalActorName(ProductManagementRequirement requirement) {
        if (requirement == null || StringUtils.isBlank(requirement.getId())) {
            return "审批流程角色";
        }
        List<ApprovalInstance> instances = approvalInstanceMapper.selectListByLambda(
                new LambdaQueryWrapper<ApprovalInstance>()
                        .eq(ApprovalInstance::getResourceId, requirement.getId())
                        .orderByDesc(ApprovalInstance::getSubmitTime)
        );
        if (CollectionUtils.isEmpty(instances)) {
            return StringUtils.defaultIfBlank(requirement.getOwnerName(), "审批流程角色");
        }
        ApprovalInstance instance = instances.getFirst();
        List<ApprovalRecord> records = approvalRecordMapper.selectListByLambda(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getInstanceId, instance.getId())
                        .orderByDesc(ApprovalRecord::getCreateTime)
        );
        if (CollectionUtils.isEmpty(records)) {
            return StringUtils.defaultIfBlank(requirement.getOwnerName(), "审批流程角色");
        }
        return resolveUserDisplayName(records.getFirst().getCreateUser(), requirement.getOwnerName());
    }

    private String resolveUserDisplayName(String userId, String fallback) {
        if (StringUtils.isBlank(userId)) {
            return StringUtils.defaultIfBlank(fallback, "审批流程角色");
        }
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null || StringUtils.isBlank(user.getName())) {
            return StringUtils.defaultIfBlank(fallback, "审批流程角色");
        }
        return user.getName();
    }

    private ProductManagementProduct resolveProduct(String productId, String productName) {
        if (StringUtils.isNotBlank(productId)) {
            ProductManagementProduct product = productMapper.selectByPrimaryKey(productId);
            if (product != null) {
                return product;
            }
        }
        return productMapper.selectListByLambda(
                        new LambdaQueryWrapper<ProductManagementProduct>().eq(ProductManagementProduct::getOrganizationId, orgId())
                ).stream()
                .filter(product -> StringUtils.equalsIgnoreCase(product.getCode(), productName)
                        || StringUtils.equalsIgnoreCase(product.getName(), productName)
                        || StringUtils.equalsIgnoreCase(productTag(product), productName))
                .findFirst()
                .orElse(null);
    }

    private List<ProductManagementModule> listModules(String productId) {
        LambdaQueryWrapper<ProductManagementModule> wrapper = new LambdaQueryWrapper<ProductManagementModule>()
                .eq(ProductManagementModule::getOrganizationId, orgId());
        if (StringUtils.isNotBlank(productId)) {
            wrapper.eq(ProductManagementModule::getProductId, productId);
        }
        return moduleMapper.selectListByLambda(wrapper);
    }

    private List<ProductManagementVersion> listVersions(String productId) {
        return versionMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementVersion>()
                        .eq(ProductManagementVersion::getOrganizationId, orgId())
                        .eq(ProductManagementVersion::getProductId, productId)
        );
    }

    private List<ProductManagementRequirement> listRequirements(String productId) {
        return requirementMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementRequirement>()
                        .eq(ProductManagementRequirement::getOrganizationId, orgId())
                        .eq(ProductManagementRequirement::getProductId, productId)
        );
    }

    private List<ProductManagementRequirement> listRequirementsByOrg() {
        return requirementMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementRequirement>().eq(ProductManagementRequirement::getOrganizationId, orgId())
        );
    }

    private List<ProductManagementDocument> listDocuments(String productId) {
        return documentMapper.selectListByLambda(
                new LambdaQueryWrapper<ProductManagementDocument>()
                        .eq(ProductManagementDocument::getOrganizationId, orgId())
                        .eq(ProductManagementDocument::getProductId, productId)
        );
    }

    private <T> Pager<List<T>> page(List<T> list, BasePageRequest request) {
        int current = request == null || request.getCurrent() <= 0 ? 1 : request.getCurrent();
        int pageSize = request == null || request.getPageSize() <= 0 ? 100 : request.getPageSize();
        int from = Math.min((current - 1) * pageSize, list.size());
        int to = Math.min(from + pageSize, list.size());
        return new Pager<>(list.subList(from, to), list.size(), pageSize, current);
    }

    private void fillCreateBase(cn.cordys.common.domain.BaseModel model) {
        long now = System.currentTimeMillis();
        model.setCreateTime(now);
        model.setUpdateTime(now);
        model.setCreateUser(userId());
        model.setUpdateUser(userId());
        if (model instanceof ProductManagementModule module) {
            module.setOrganizationId(orgId());
        } else if (model instanceof ProductManagementVersion version) {
            version.setOrganizationId(orgId());
        } else if (model instanceof ProductManagementRequirement requirement) {
            requirement.setOrganizationId(orgId());
        } else if (model instanceof ProductManagementDocument document) {
            document.setOrganizationId(orgId());
        }
    }

    private synchronized String nextRequirementNo() {
        List<String> existingNumbers = requirementMapper.selectListByLambda(new LambdaQueryWrapper<ProductManagementRequirement>())
                .stream()
                .map(ProductManagementRequirement::getRequirementNo)
                .toList();
        return nextRequirementNo(Year.now().getValue(), existingNumbers);
    }

    static String nextRequirementNo(int year, List<String> existingNumbers) {
        int maxSequence = existingNumbers.stream()
                .filter(StringUtils::isNotBlank)
                .map(REQUIREMENT_NO_PATTERN::matcher)
                .filter(Matcher::matches)
                .filter(matcher -> Integer.parseInt(matcher.group(1)) == year)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(2)))
                .max()
                .orElse(100);
        return "PRM-%s-%04d-01".formatted(year, maxSequence + 1);
    }

    private String orgId() {
        String orgId = OrganizationContext.getOrganizationId();
        return StringUtils.defaultIfBlank(orgId, OrganizationContext.DEFAULT_ORGANIZATION_ID);
    }

    private String userId() {
        return StringUtils.defaultIfBlank(SessionUtils.getUserId(), "admin");
    }

    private String productTag(ProductManagementProduct product) {
        if (product == null) {
            return "";
        }
        if (StringUtils.equalsIgnoreCase(product.getCode(), "STARIVER")) {
            return "StaRiver";
        }
        if (StringUtils.equalsIgnoreCase(product.getCode(), "OPTIQA")) {
            return "OptiQA";
        }
        return StringUtils.defaultIfBlank(product.getCode(), product.getName());
    }

    private String productKey(ProductManagementProduct product) {
        return productKey(productTag(product));
    }

    private String productKey(String product) {
        if (StringUtils.containsIgnoreCase(product, "optiqa")) {
            return "optiqa";
        }
        if (StringUtils.containsIgnoreCase(product, "datex")) {
            return "datex";
        }
        return "stariver";
    }

    private String iconText(String code) {
        return StringUtils.isBlank(code) ? "P" : code.substring(0, 1).toUpperCase();
    }

    private String iconBg(String code) {
        return StringUtils.containsIgnoreCase(code, "optiqa") ? "#eef2ff" : "#f1f5f9";
    }

    private String iconColor(String code) {
        return StringUtils.containsIgnoreCase(code, "optiqa") ? "#4f46e5" : "#4f46e5";
    }

    private String statusBg(String status) {
        return "已上线".equals(status) || "已发布".equals(status) ? "#dcfce7" : "#eef2ff";
    }

    private String statusColor(String status) {
        return "已上线".equals(status) || "已发布".equals(status) ? "#16a34a" : "#4f46e5";
    }

    private static String normalizeOnlineStatus(String status) {
        return "已发布".equals(status) ? "已上线" : status;
    }

    private String normalizeRequirementStatus(String status) {
        return status;
    }

    private String priorityType(String priority) {
        return switch (StringUtils.defaultString(priority)) {
            case "P0" -> "high";
            case "P1" -> "middle";
            default -> "normal";
        };
    }

    private String statusType(String status) {
        return switch (StringUtils.defaultString(status)) {
            case "已上线", "已发布" -> "online";
            case "开发中", "测试中" -> "developing";
            case "待发布", "产品设计", "需求评审" -> "pending";
            case "需求评估", "技术评审" -> "evaluating";
            default -> "planning";
        };
    }
}
