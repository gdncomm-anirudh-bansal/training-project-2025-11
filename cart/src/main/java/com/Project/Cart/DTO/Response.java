package com.Project.Cart.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {


    private T data;
    private String message;
    private int code;
    private boolean success;
    private ErrorDTO error;
}
