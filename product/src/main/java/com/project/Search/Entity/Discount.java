package com.project.Search.Entity;

import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class Discount {

    private String type;
    private Long value;
    private Date startDate;
    private Date endDate;
}
