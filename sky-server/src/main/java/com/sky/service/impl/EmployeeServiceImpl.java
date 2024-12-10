package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.config.RedisConfig;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final String LOGIN_ERROR_KEY_PREFIX = "login:error:";

    private final String LOGIN_LOCK_KEY_PREFIX = "login:lock:";

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 判断账号是否已经锁定
        if(redisTemplate.hasKey(LOGIN_LOCK_KEY_PREFIX + employee.getUsername())) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 检查密码错误次数
        Set<Object> keys = redisTemplate.keys(LOGIN_ERROR_KEY_PREFIX +  employee.getUsername() + "*");
        if(keys != null && keys.size() >= 5) {
            redisTemplate.opsForValue().set(LOGIN_LOCK_KEY_PREFIX+employee.getUsername(), "locked", 30, TimeUnit.MINUTES);
            throw new AccountLockedException(MessageConstant.LOGIN_ERROR_FOR_TIMES);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        validatePassword(password, employee);

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 登陆成功清空错误次数
        for (Object key : keys) {
            redisTemplate.delete(key);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDto
     */
    @Override
    public void save(EmployeeDTO employeeDto) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDto, employee);

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.save(employee);
    }

    /**
     * 查询用户列表
     * pagehelper使用步骤
     * 设置分页参数
     * 执行查询
     * 返回结果
     * */
    @Override
    public PageResult page(EmployeePageQueryDTO pageQueryDto) {
        PageHelper.startPage(pageQueryDto.getPage(),pageQueryDto.getPageSize());

        List<Employee> employees = employeeMapper.list(pageQueryDto.getName());

        Page<Employee> pageInfo = (Page<Employee>) employees;
        return new PageResult(pageInfo.getTotal(), pageInfo.getResult());
    }

    @Override
    public void isDisable(Integer status, Long id) {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Override
    public Employee queryById(Long id) {
        return employeeMapper.query(id);
    }

    public void validatePassword(String password, Employee employee) {
        if (!password.equals(employee.getPassword())) {
            //密码错误
            redisTemplate.opsForValue().set(LOGIN_ERROR_KEY_PREFIX+employee.getUsername()+ RandomStringUtils.randomAlphabetic(4),
                    "-", 5, TimeUnit.MINUTES);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
    }

}
