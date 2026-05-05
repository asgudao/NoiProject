package com.tjetc.service.impl;


import com.tjetc.common.JsonResult;
import com.tjetc.dao.InformationMapper;
import com.tjetc.entity.Information;
import com.tjetc.service.InformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InformationServiceImpl implements InformationService {

    @Autowired
    private InformationMapper informationMapper;

    @Override
    public JsonResult<List<Information>> selectAll() {
        List<Information> informations = informationMapper.selectList(null);
        return JsonResult.success(informations);
    }
}
