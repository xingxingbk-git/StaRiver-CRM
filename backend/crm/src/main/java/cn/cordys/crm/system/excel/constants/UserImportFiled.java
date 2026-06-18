package cn.cordys.crm.system.excel.constants;


import cn.cordys.crm.system.excel.domain.UserExcelData;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author wx
 */

public enum UserImportFiled {

    NAME("name", "姓名", "Name", UserExcelData::getName),
    GENDER("gender", "性别", "Gender", UserExcelData::getGender),
    DEPARTMENT("department", "部门", "Department", UserExcelData::getDepartment),
    PHONE("phone", "手机号", "Phone", UserExcelData::getPhone),
    EMAIL("email", "邮箱", "Email", UserExcelData::getEmail);

    @Getter
    private final Map<Locale, String> filedLangMap;
    private final Function<UserExcelData, String> parseFunc;
    @Getter
    private final String value;

    UserImportFiled(String value, String zn, String us, Function<UserExcelData, String> parseFunc) {
        this.filedLangMap = new HashMap<>();
        filedLangMap.put(Locale.SIMPLIFIED_CHINESE, zn);
        filedLangMap.put(Locale.US, us);
        this.value = value;
        this.parseFunc = parseFunc;
    }

    public String parseExcelDataValue(UserExcelData excelData) {
        return parseFunc.apply(excelData);
    }

    public boolean containsHead(String head) {
        return filedLangMap.containsValue(head);
    }
}
