package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;

    @Autowired
    public DishServiceImpl(DishMapper dishMapper, DishFlavorMapper dishFlavorMapper, SetmealDishMapper setmealDishMapper) {
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
        this.setmealDishMapper = setmealDishMapper;
    }

    /**
     * 新增菜品及口味
     */
    @Transactional()
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入1条菜品数据
        dishMapper.insert(dish);

        //获取Insert语句生成的主键值
        Long dashId = dish.getId();

        //插入n条口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            //为每一个口味设置菜品id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dashId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     */
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //1. 设置PageHelper分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        List<DishVO> dishList = dishMapper.pageQuery(dishPageQueryDTO);
        //2. 封装分页结果
        Page<DishVO> p = (Page<DishVO>)dishList;
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    /**
     * 批量删除菜品
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("菜品ID列表不能为空");
        }
        try {
            // 校验菜品ID列表的有效性
            List<Dish> dishes = dishMapper.getByIds(ids);
            List<Long> validIds = dishes.stream()
                    .filter(Objects::nonNull)
                    .map(Dish::getId)
                    .toList();
            if (validIds.size() != ids.size()) {
                throw new IllegalArgumentException("存在无效的菜品ID");
            }

            // 检查菜品是否关联套餐
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(validIds);
            if (setmealIds != null && !setmealIds.isEmpty()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }

            // 检查菜品状态
            List<Long> enabledDishIds = dishes.stream()
                    .filter(dish -> Objects.equals(dish.getStatus(), StatusConstant.ENABLE))
                    .map(Dish::getId)
                    .toList();
            if (!enabledDishIds.isEmpty()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

            // 批量删除菜品及其口味
            dishMapper.deleteByIds(validIds);
            dishFlavorMapper.deleteByDishIds(validIds);
        } catch (Exception e) {
            log.error("批量删除菜品失败", e);
            throw e; // 将异常重新抛出，确保调用方能感知错误
        }
    }
    /**
     * 根据id查询菜品及口味
     */
    public DishVO getByIdWithFlavor(Long id) {
        //1. 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        //2. 根据菜品id查询口味数据
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(id);

        //3. 将查询结果封装到dishVO中并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorList);

        return dishVO;
    }

    /**
     * 修改菜品基本信息和口味信息
     */
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        if (dishDTO == null || dishDTO.getId() == null) {
            throw new IllegalArgumentException("dishDTO or its ID cannot be null");
        }
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 修改菜品表基本信息
        dishMapper.update(dish);
        // 删除原有菜品口味表
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dish.getId()));
        // 重新插入菜品口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            // 为每一个口味设置菜品id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品起售停售
     */
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * C端-条件查询菜品和口味
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}