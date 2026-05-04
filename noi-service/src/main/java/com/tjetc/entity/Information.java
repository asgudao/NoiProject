package com.tjetc.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("information")
public class Information {
    private Integer id;
    private String studentName;
    private String studentNum;
    private String examFileAddress;
    private String examPassword;
    private String computerIp;
    private String classroomNum;
    private String examNum;
//    private byte[] examFile;
}
