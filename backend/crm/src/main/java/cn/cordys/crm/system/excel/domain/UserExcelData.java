package cn.cordys.crm.system.excel.domain;

import cn.cordys.crm.system.excel.constants.UserImportFiled;
import cn.idev.excel.annotation.ExcelIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author wx
 */
@Getter
@Setter
public class UserExcelData {
    /**
     * 姓名
     */
    @ExcelIgnore
    private String name;
    /**
     * 性别
     */
    @ExcelIgnore
    private String gender;
    /**
     * 部门
     */
    @ExcelIgnore
    private String department;
    /**
     * 手机号
     */
    @ExcelIgnore
    private String phone;
    /**
     * 邮箱
     */
    @ExcelIgnore
    private String email;


    public List<List<String>> getHead() {
        return new ArrayList<>();
    }

    public List<List<String>> getHead(Locale lang) {
        List<List<String>> heads = new ArrayList<>();
        UserImportFiled[] fields = UserImportFiled.values();
        for (UserImportFiled field : fields) {
            heads.add(Collections.singletonList(field.getFiledLangMap().get(lang)));
        }
        return heads;
    }
}
