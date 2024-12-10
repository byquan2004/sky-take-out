package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDto) {
        log.info("新增用户:{}", employeeDto);
        employeeService.save(employeeDto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO pageQueryDto) {
        log.info("查询用户列表: {}", pageQueryDto);
        return Result.success(employeeService.page(pageQueryDto));
    }

    @PostMapping("/status/{status}")
    public Result isDisable(@PathVariable String status, @RequestParam String id) {
        log.info("启用或禁用员工: {}--{}",status,id);
        employeeService.isDisable(Integer.valueOf(status), Long.valueOf(id));
        return Result.success();
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工:{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> queryById(@PathVariable Long id) {
        log.info("根据id查询员工: {}", id);
        return Result.success(employeeService.queryById(id));
    }

    /**
     * 退出
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

}
