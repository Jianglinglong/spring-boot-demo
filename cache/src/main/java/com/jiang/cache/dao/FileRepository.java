package com.jiang.cache.dao;

import com.jiang.cache.web.entity.YuansujuFile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Jiang
 * @date 2018/11/27
 * @time 15:56
 */
public interface FileRepository extends JpaRepository<YuansujuFile,Long> {
}
