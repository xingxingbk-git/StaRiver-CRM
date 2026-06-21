package cn.cordys.crm.productmgmt.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public record ProductRequirementWorkflowConfig(List<Stage> stages) {

    public ProductRequirementWorkflowConfig {
        stages = stages == null ? new ArrayList<>() : new ArrayList<>(stages);
    }

    public static ProductRequirementWorkflowConfig defaultConfig() {
        return new ProductRequirementWorkflowConfig(List.of(
                new Stage("PRODUCT_DESIGN", "产品设计", List.of(), List.of(), false, false),
                new Stage("TECH_REVIEW", "技术评审", List.of(), List.of(), true, false),
                new Stage("DEVELOPMENT", "开发", List.of(), List.of(), true, false),
                new Stage("TESTING", "测试", List.of(), List.of(), true, false),
                new Stage("ACCEPTANCE", "产品验收", List.of(), List.of(), true, true),
                new Stage("RELEASE", "发布", List.of(), List.of(), true, false),
                new Stage("COMPLETED", "完成", List.of(), List.of(), false, false)
        ));
    }

    public Stage stage(String name) {
        return stages.stream().filter(stage -> StringUtils.equals(stage.name(), name)).findFirst().orElse(null);
    }

    public Stage nextStage(String name) {
        int index = indexOf(name);
        return index >= 0 && index + 1 < stages.size() ? stages.get(index + 1) : null;
    }

    public Stage previousStage(String name) {
        int index = indexOf(name);
        return index > 0 ? stages.get(index - 1) : null;
    }

    public boolean requiresProductLink(String name) {
        Stage stage = stage(name);
        return stage != null && stage.requiresProductLink();
    }

    private int indexOf(String name) {
        for (int index = 0; index < stages.size(); index++) {
            if (StringUtils.equals(stages.get(index).name(), name)) {
                return index;
            }
        }
        return -1;
    }

    public record Stage(
            String key,
            String name,
            List<String> assigneeIds,
            List<String> assigneeNames,
            boolean returnable,
            boolean requiresProductLink
    ) {
        public Stage {
            assigneeIds = assigneeIds == null ? List.of() : List.copyOf(assigneeIds);
            assigneeNames = assigneeNames == null ? List.of() : List.copyOf(assigneeNames);
        }

        public boolean canOperate(String userId, String fallbackOwnerId) {
            if (StringUtils.isBlank(userId)) {
                return false;
            }
            return assigneeIds.isEmpty()
                    ? StringUtils.equals(userId, fallbackOwnerId)
                    : assigneeIds.contains(userId);
        }

        public String ownerLabel(String fallback) {
            return assigneeNames.isEmpty() ? fallback : String.join("、", assigneeNames);
        }
    }
}
