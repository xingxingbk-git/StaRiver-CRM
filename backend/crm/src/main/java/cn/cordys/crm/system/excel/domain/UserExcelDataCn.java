package cn.cordys.crm.system.excel.domain;

import cn.cordys.crm.system.excel.annotation.NotRequired;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Locale;

/**
 * @author wx
 */
@Data
@ColumnWidth(15)
public class UserExcelDataCn extends UserExcelData {

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("姓名")
    private String name;

    @ColumnWidth(50)
    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("部门")
    @ColumnWidth(50)
    private String department;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(50)
    @Length(max = 255)
    @ExcelProperty("手机号")
    private String phone;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(50)
    @ExcelProperty("邮箱")
    private String email;


    @Override
    public List<List<String>> getHead() {
        return super.getHead(Locale.SIMPLIFIED_CHINESE);
    }
}
