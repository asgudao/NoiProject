package com.tjetc.controller;


import com.tjetc.dao.InformationMapper;
import com.tjetc.service.InformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class InformationController {
    @Autowired
    private InformationService informationService;




}
