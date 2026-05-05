package com.tjetc.service;

import com.tjetc.common.JsonResult;
import com.tjetc.entity.Information;

import java.util.List;

public interface InformationService {

    JsonResult<List<Information>> selectAll();
}
