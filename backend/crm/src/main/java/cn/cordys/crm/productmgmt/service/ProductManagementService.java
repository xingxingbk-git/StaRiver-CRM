package cn.cordys.crm.productmgmt.service;

import cn.cordys.common.dto.BasePageRequest;
import cn.cordys.common.dto.OptionDTO;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.productmgmt.domain.ProductManagementDocument;
import cn.cordys.crm.productmgmt.domain.ProductManagementModule;
import cn.cordys.crm.productmgmt.domain.ProductManagementProduct;
import cn.cordys.crm.productmgmt.domain.ProductManagementRequirement;
import cn.cordys.crm.productmgmt.domain.ProductManagementVersion;
import cn.cordys.crm.productmgmt.dto.request.ProductManagementSaveRequest;
import cn.cordys.crm.productmgmt.dto.request.ProductRequirementSaveRequest;
import cn.cordys.crm.productmgmt.dto.request.ProductVersionSaveRequest;
import cn.cordys.crm.productmgmt.mapper.ExtPmProductMapper;
import cn.cordys.crm.system.domain.Attachment;
import cn.cordys.crm.system.domain.User;
import cn.cordys.crm.system.dto.request.UploadTransferRequest;
import cn.cordys.crm.system.mapper.ExtUserMapper;
import cn.cordys.crm.system.service.AttachmentService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProductManagementService {

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
    private ExtPmProductMapper extPmProductMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private AttachmentService attachmentService;

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
                .sorted(Comparator.comparing(ProductManagementRequirement::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed())
                .map(this::requirementRow)
                .toList();
        return page(rows, request);
    }

    public Map<String, Object> getRequirement(String idOrNo) {
        ProductManagementRequirement requirement = findRequirement(idOrNo);
        return requirement == null ? null : requirementRow(requirement);
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
        fillCreateBase(requirement);
        requirementMapper.insert(requirement);
        return requirementRow(requirement);
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

    private Map<String, Object> requirementRow(ProductManagementRequirement requirement) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("rawId", requirement.getId());
        row.put("id", requirement.getRequirementNo());
        row.put("title", requirement.getTitle());
        row.put("detailTitle", requirement.getTitle());
        row.put("product", requirement.getProductName());
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

    private String nextRequirementNo() {
        int seq = listRequirementsByOrg().size() + 101;
        return "PRM-%s-%04d-01".formatted(Year.now().getValue(), seq);
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
        if ("需求评估".equals(status)) {
            return "需求评估";
        }
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
            case "待发布", "产品设计" -> "pending";
            case "需求评估", "技术评审" -> "evaluating";
            default -> "planning";
        };
    }
}
